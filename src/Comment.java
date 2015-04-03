import com.intellij.execution.ExecutionException;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.util.ExecUtil;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.apache.commons.lang.StringUtils;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by rskvirsky on 03/04/15.
 */
public class Comment extends AnAction {

    private static final String GITHUB_URL = "https://github.com/";

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
        DataContext dataContext = event.getDataContext();

        int currentLine = position.line + 1;

        SelectionModel selectionModel = editor.getSelectionModel();

        String currentLine2 = StringUtils.EMPTY;
        if (selectionModel.hasSelection()) {
            currentLine2 = "#L" + (selectionModel.getSelectionStartPosition().getLine() + 1) + "-L"
                    + (selectionModel.getSelectionEndPosition().getLine() + 1);
        } else {
            currentLine2 = "#L" + (selectionModel.getSelectionStartPosition().getLine() + 1);
        }

        Messages.showInputDialog(project, "Line: " + currentLine + ", ver: ",
                "Enter comment",
                Messages.getQuestionIcon());

        commentRevision(project, dataContext);

        Messages.showMessageDialog(project, "Comment sent", "Information", Messages.getInformationIcon());
    }

    private boolean commentRevision(Project project, DataContext dataContext) {
        VirtualFile current_file = (VirtualFile) PlatformDataKeys.VIRTUAL_FILE.getData(dataContext);
        String current_file_name = current_file.toString().replace("file://" + project.getBasePath() + "/", "");
        String basePath = project.getBasePath();

        BufferedReader br_config;
        BufferedReader br_head;

        String line;
        String group = "";
        String projectName = "";
        String head = "";
        String cursor_line;
        boolean is_set_github = false;

        try {
            br_config = new BufferedReader(new FileReader(basePath + "/.git/config"));
            br_head = new BufferedReader(new FileReader(basePath + "/.git/HEAD"));

            while ((line = br_config.readLine()) != null) {
                if (line.matches(".*url = .*")) {
                    Pattern pattern = Pattern.compile("(git@github.com:(.*?)/(.*?).git|https://github.com/(.*?)/(.*?).git)");
                    Matcher matcher = pattern.matcher(line);

                    if (matcher.find()) {
                        group = (matcher.group(2) != null) ? matcher.group(2) : matcher.group(4);
                        projectName = (matcher.group(3) != null) ? matcher.group(3) : matcher.group(5);
                        is_set_github = true;
                        break;
                    }
                }
            }

            while ((line = br_head.readLine()) != null) {
                Pattern pattern = Pattern.compile(".*?heads(.*?)$");
                Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    head = matcher.group(1);
                }
            }

        } catch (Exception exception) {
            // .git/config is empty or not found
            Notifications.Bus
                    .notify(new Notification("Open in GitHub", "Error", "Can not open [.git/config] file.", NotificationType.ERROR));
            return false;
        }

        if (!is_set_github) {
            // repository is not management in github
            Notifications.Bus.notify(new Notification("Open in GitHub", "Error", "Repository is not management in GitHub.", NotificationType.ERROR));
            return false;
        }

        Editor editor = (Editor) PlatformDataKeys.EDITOR.getData(dataContext);
        SelectionModel selectionModel = editor.getSelectionModel();

        if (selectionModel.hasSelection()) {
            cursor_line = "#L" + (selectionModel.getSelectionStartPosition().getLine() + 1) + "-L"
                    + (selectionModel.getSelectionEndPosition().getLine() + 1);
        } else {
            cursor_line = "#L" + (selectionModel.getSelectionStartPosition().getLine() + 1);
        }

        String request = GITHUB_URL + group + "/" + projectName + "/blob" + head + "/" + current_file_name + cursor_line;

        String[] command = new String[]{ExecUtil.getOpenCommandPath()};

        try {
            final GeneralCommandLine commandLine = new GeneralCommandLine(command);
            commandLine.addParameter(request);
            commandLine.createProcess();

        } catch (ExecutionException exception) {
            Notifications.Bus.notify(new Notification("Open in GitHub", "Error", "Error: " + exception.getMessage(),
                    NotificationType.ERROR));
            return false;
        }
        return true;
    }
}
