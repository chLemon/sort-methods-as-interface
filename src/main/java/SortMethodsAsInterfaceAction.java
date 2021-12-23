import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.CommonDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.EditorWriteActionHandler;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;

import java.util.ArrayList;
import java.util.List;

import org.jdesktop.swingx.sort.SortUtils;

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

	private static class Handler extends EditorWriteActionHandler{
		public void executeWriteAction(Editor editor, DataContext dataContext) {
			final Document doc = editor.getDocument();
			
			int startLine;
			int endLine;
			
			startLine = 0;
			endLine = doc.getLineCount() -1 ;

			// Ignore last lines (usually one) which are only '\n'
			endLine = ignoreLastEmptyLines(doc, endLine);

			if (startLine >= endLine)
			{
				return;
			}

			// Extract text as a list of lines
			List<String> lines = extractLines(doc, startLine, endLine);

			Project project = editor.getProject();
			editor.get

		}
	}

	@Override
	public void actionPerformed(AnActionEvent e) {
		/**
		 * 想根据接口的方法顺序来重新排序当前类中的方法
		 *
		 * 获取到当前类
		 * 找到当前类的接口
		 * 拿到接口的方法
		 * 根据接口的方法重新整理当前类方法的顺序
		 *
		 */
//		editor.getDocument().replaceString(startOffset, endOffset, sortedText);

		// 当前的类
		PsiFile curFile = e.getData(CommonDataKeys.PSI_FILE);
		Editor data = e.getData(CommonDataKeys.EDITOR);

		PsiJavaFile psiJavaFile = (PsiJavaFile) curFile;
		final PsiClass[] classes = psiJavaFile.getClasses();

		JavaPsiFacade.findClass()
		VirtualFile virtualFile = curFile.getVirtualFile();
		Class<? extends PsiFile> curFileCLass = curFile.getClass();
		Class<?> superclass = curFileCLass.getSuperclass();
		Class<?>[] interfaces = curFileCLass.getInterfaces();

		//获取当前类文件的路径
		PsiDirectory parent = curFile.getParent();
		int startOffsetInParent = curFile.getStartOffsetInParent();

		String info = curFile.getName() + "==" + superclass.getName() + "==" + interfaces.length;
//		Messages.showMessageDialog(project, classPath, title, Messages.getInformationIcon());
		Messages.showMessageDialog(info, "title", Messages.getInformationIcon());

//		SearchScope scope = GlobalSearchScope.packageScope(yourPackage, true);
//		ClassInheritorsSearch query = ClassInheritorsSearch.search(yourClass, scope);
	}


//			editor.getDocument().replaceString(startOffset, endOffset, sortedText);

	
	
	private static class Handler extends EditorWriteActionHandler {
		public void executeWriteAction(Editor editor, DataContext dataContext) {
			PsiFile data = dataContext.getData(CommonDataKeys.PSI_FILE);
			getpsi
			
			
			
			editor.getProject().getBasePath();
			final Document doc = editor.getDocument();

			int startLine;
			int endLine;

			startLine = 0;
			endLine = doc.getLineCount() - 1;


			// Ignore last lines (usually one) which are only '\n'
			endLine = ignoreLastEmptyLines(doc, endLine);

			if (startLine >= endLine) {
				return;
			}

			// start
			// 从这里开始改
			// Extract text as a list of lines
			List<String> lines = extractLines(doc, startLine, endLine);

			// dumb sort
//			SortUtils.defaultSort(lines);

			StringBuilder sortedText = joinLines(lines);


			// end
			// Remove last \n is sort has been applied on whole file and the file did not end with \n
			CharSequence charsSequence = doc.getCharsSequence();
			if (charsSequence.charAt(charsSequence.length() - 1) != '\n') {
				sortedText.deleteCharAt(sortedText.length() - 1);
			}

			// Replace text
			int startOffset = doc.getLineStartOffset(startLine);
			int endOffset = doc.getLineEndOffset(endLine) + doc.getLineSeparatorLength(endLine);

			editor.getDocument().replaceString(startOffset, endOffset, sortedText);
		}

		private int ignoreLastEmptyLines(Document doc, int endLine) {
			while (endLine >= 0) {
				if (doc.getLineEndOffset(endLine) > doc.getLineStartOffset(endLine)) {
					return endLine;
				}

				endLine--;
			}

			return -1;
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
