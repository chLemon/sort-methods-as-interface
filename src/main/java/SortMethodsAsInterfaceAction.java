import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.google.common.collect.Lists;
import com.intellij.lang.FileASTNode;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.FileViewProvider;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.jdesktop.swingx.sort.SortUtils;
import org.jetbrains.annotations.NotNull;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.editor.actions.TextComponentEditorAction;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;

public class SortMethodsAsInterfaceAction extends TextComponentEditorAction {
	protected SortMethodsAsInterfaceAction() {
		super(new Handler());
	}

	private static class Handler extends EditorWriteActionHandler {
		// 一种朴素的方法
		public void executeWriteAction(Editor editor, DataContext dataContext) {

			// api测试
			// 拿到接口的内容
			PsiFile curFile = dataContext.getData(CommonDataKeys.PSI_FILE);

			PsiFile interfaceFile1 = curFile;
			String interfaceFileText = curFile.getText();
			FileViewProvider viewProvider = curFile.getViewProvider();
			Document document = viewProvider.getDocument();
			FileASTNode node = curFile.getNode();

			CharSequence contents = viewProvider.getContents();


			// 先拿到当前文件： .../impl/ServiceImpl.Java
//			PsiFile curFile = dataContext.getData(CommonDataKeys.PSI_FILE);
			if (curFile == null) {
				System.out.println("当前文件不存在");
				return;
			}
			// 父级目录：.../impl
			PsiDirectory implDir = curFile.getParent();
			if (implDir == null) {
				System.out.println("impl文件夹不存在");
				return;
			}
			// Service目录
			PsiDirectory serviceDir = implDir.getParent();
			if (serviceDir == null) {
				System.out.println("service目录不存在");
				return;
			}
			// 拿到对应的Service接口  xxxImpl.java
			String implName = curFile.getName();
			String interfaceName = implName.replace("Impl", "");
			PsiFile interfaceFile = serviceDir.findFile(interfaceName);
			if (interfaceFile == null) {
				System.out.println("未找到接口文件");
				return;
			}
			// 拿到接口的document
			Document interfaceDoc = interfaceFile.getViewProvider().getDocument();

			// 接口中的每一行遍历，存下来接口中的方法顺序
			List<String> interfaceLines = extractLines(interfaceDoc, 0, interfaceDoc.getLineCount() - 1);

			List<String> interfaceSortedMethods = new ArrayList<>();
			// 写一个方法名的正则：       
			//     若干个开始空格  返回值 一个空格以上的空格  方法名  括号  参数类型 空格 参数名 括号 分号
			// 注意该正则会匹配到空行，要加个判断
			String pattern = "^\\s*\\S+\\s+\\S+\\(.*\\);$";
			for (int i = 0; i < interfaceLines.size(); i++) {
				String line = interfaceLines.get(i);
				if (!line.equals("\n")) {
					String noN = line.replace("\n", "");
					if (noN.matches(pattern)) {
						// 这行非空行，并且满足方法名的正则，那么就是一个抽象方法
						// 去掉方法内的空格，去掉末尾的分号，存起来
						String noSpaceLine = line.replace(" ", "");
						String lineSingle = noSpaceLine.replace(";", "");
						interfaceSortedMethods.add(lineSingle.replace("\n", ""));
					}
				}
			}

			// 开始处理实现类中的代码
			Document implDoc = editor.getDocument();
			List<String> implLines = extractLines(implDoc, 0, implDoc.getLineCount() - 1);

			/*
				排序：
				1. 从上往下遍历，直到遇到第一个 @Override，把之前的行都存在新list里，记录下第一个 @Override 的index
				2. 开始@Override的方法块全部都提出来，存起来一个个的list。其他的正常进list
				3. 方法块排序
				4. 排序好的方法块放在第一个@Override的index处
			*/
			List<String> res = new ArrayList<>();
			Map<String, List<String>> methods = new HashMap<>();

			int firstIndex = 0;
			for (int i = 0; i < implLines.size(); i++) {
				String line = implLines.get(i);
				if (line.contains("@Override")) {
					if (firstIndex == 0) {
						firstIndex = i;
					}
					// 从当前行开始一直处理到本方法结束，然后控制i往前走
					List<String> thisMethod = new ArrayList<>();
					thisMethod.add(line);
					int k = 1;
					String methodLine = implLines.get(i + k);
					String astr = methodLine.replace("public", "");
					String bstr = astr.replace(" ", "");
					String methodLineSingle = bstr.replace("{", "");

					thisMethod.add(methodLine);
					int virtualStack = 1;
					while (virtualStack != 0) {
						k++;
						String innerLine = implLines.get(i + k);
						thisMethod.add(innerLine);
						int countLeft = 0;
						int countRight = 0;
						for (int j = 0; j < innerLine.length(); j++) {
							char c = innerLine.charAt(j);
							if (c == '{') {
								countLeft++;
							} else if (c == '}') {
								countRight++;
							}
						}
						virtualStack += countLeft;
						virtualStack -= countRight;
					}
					methods.put(methodLineSingle.replace("\n", ""), thisMethod);
					i = i + k;
				} else {
					res.add(line);
				}
			}

			// 方法排序
			List<String> sorted = new ArrayList<>();
			for (String method : interfaceSortedMethods) {
				List<String> aMethod = methods.get(method);
				sorted.addAll(aMethod);
				sorted.add("\n");
			}

			// 放回index的位置
			res.addAll(firstIndex, sorted);

			StringBuilder sortedText = joinLines(res);

			// end
			// Remove last \n is sort has been applied on whole file and the file did not end with \n
//			CharSequence charsSequence = doc.getCharsSequence();
//			if (charsSequence.charAt(charsSequence.length() - 1) != '\n') {
//				sortedText.deleteCharAt(sortedText.length() - 1);
//			}

			// Replace text
			int startOffset = implDoc.getLineStartOffset(0);
			int endOffset = implDoc.getLineEndOffset(implDoc.getLineCount() - 1) + implDoc.getLineSeparatorLength(implDoc.getLineCount() - 1);

			editor.getDocument().replaceString(startOffset, endOffset, sortedText);


		}

		private List<String> extractLines(Document doc, int startLine, int endLine) {
			List<String> lines = new ArrayList<String>(endLine - startLine);
			for (int i = startLine; i <= endLine; i++) {
				String line = extractLine(doc, i);

				lines.add(line);
			}

			return lines;
		}

		private String extractLine(Document doc, int lineNumber) {
			int lineSeparatorLength = doc.getLineSeparatorLength(lineNumber);
			int startOffset = doc.getLineStartOffset(lineNumber);
			int endOffset = doc.getLineEndOffset(lineNumber) + lineSeparatorLength;

			String line = doc.getCharsSequence().subSequence(startOffset, endOffset).toString();

			// If last line has no \n, add it one
			// This causes adding a \n at the end of file when sort is applied on whole file and the file does not end
			// with \n... This is fixed after.
			if (lineSeparatorLength == 0) {
				line += "\n";
			}

			return line;
		}

		private StringBuilder joinLines(List<String> lines) {
			StringBuilder builder = new StringBuilder();
			for (String line : lines) {
				builder.append(line);
			}

			return builder;
		}
	}
}
