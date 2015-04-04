import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.AbstractVcs;
import com.intellij.openapi.vcs.ProjectLevelVcsManager;
import com.intellij.openapi.vcs.diff.DiffProvider;
import com.intellij.openapi.vfs.VirtualFile;

public class Comment extends AnAction {

    private static final String GITHUB_URL = "https://github.com/";

    public Comment() {
        // Set the menu item name.
        super("Text _Boxes");
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }


    private int getCurrentLine(final AnActionEvent event) {
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        return editor.getCaretModel().getLogicalPosition().line + 1;
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        int currentLine = getCurrentLine(event);
        String revision = getRevision(event, project);

        Messages.showInputDialog(project, "Line: " + currentLine + ", res: " + revision,
                "Enter comment",
                Messages.getQuestionIcon());

        Messages.showMessageDialog(project, "Comment sent", "Information", Messages.getInformationIcon());
    }

    private String getRevision(AnActionEvent event, Project project) {
        DataContext dataContext = event.getDataContext();
        VirtualFile currentFile = (VirtualFile) PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
        ProjectLevelVcsManager mgr = ProjectLevelVcsManager.getInstance(project);
        AbstractVcs vcs = mgr.getVcsFor(currentFile);
        DiffProvider diffProvider = vcs.getDiffProvider();
        return diffProvider.getCurrentRevision(currentFile).asString();
    }
}
