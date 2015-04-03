import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vcs.FilePath;
import com.intellij.openapi.vcs.FilePathImpl;
import com.intellij.openapi.vcs.changes.CurrentBinaryContentRevision;
import com.intellij.openapi.vcs.history.VcsRevisionNumber;
import com.intellij.openapi.vfs.VirtualFile;

/**
 * Created by rskvirsky on 03/04/15.
 */
public class Comment extends AnAction {
    // If you register the action from Java code, this constructor is used to set the menu item name
    // (optionally, you can specify the menu description and an icon to display next to the menu item).
    // You can omit this constructor when registering the action in the plugin.xml file.
    public Comment() {
        // Set the menu item name.
        super("Text _Boxes");
        // Set the menu item name, description and icon.
        // super("Text _Boxes","Item description",IconLoader.getIcon("/Mypackage/icon.png"));
    }

    public void actionPerformed(AnActionEvent event) {
        Project project = event.getData(PlatformDataKeys.PROJECT);
        Editor editor = event.getData(PlatformDataKeys.EDITOR);
        LogicalPosition position = editor.getCaretModel().getLogicalPosition();

        int currentLine = position.line + 1;

        VirtualFile currentFile = (VirtualFile)event.getData(PlatformDataKeys.VIRTUAL_FILE);
        FilePath filePath = new FilePathImpl(currentFile);
        CurrentBinaryContentRevision contentRevision = new CurrentBinaryContentRevision(filePath);
        //VcsRevisionNumber revision =

        String text = Messages.showInputDialog(project, "Line: " + currentLine + ", ver: " + revision,
                "Enter comment",
                Messages.getQuestionIcon());

        Messages.showMessageDialog(project, "Comment sent", "Information", Messages.getInformationIcon());
    }
}
