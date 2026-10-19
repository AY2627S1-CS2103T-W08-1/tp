package seedu.address.ui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeFalse;

import java.awt.GraphicsEnvironment;
import java.util.List;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.scene.control.TextField;
import seedu.address.logic.commands.CommandResult;

public class CommandBoxTest {

    private static final long TOOLKIT_TIMEOUT_SECONDS = 5;

    /**
     * Starts JavaFX when a display is available.
     */
    @BeforeAll
    public static void startToolkit() {
        assumeFalse(GraphicsEnvironment.isHeadless(), "JavaFX requires a display");
        try {
            Platform.startup(() -> {});
        } catch (IllegalStateException e) {
            // Another UI test has already started the toolkit.
        }
    }

    @Test
    public void handleCommandEntered_blankInput_notExecuted() throws Exception {
        runOnFxThread(() -> {
            CommandBox commandBox = new CommandBox(command -> fail("Blank input must not execute a command."));
            TextField input = (TextField) commandBox.getRoot().lookup("#commandTextField");

            for (String text : List.of("", " ", "   ", "\u2003")) {
                input.setText(text);
                input.fireEvent(new ActionEvent());

                assertEquals(text, input.getText());
                assertFalse(input.getStyleClass().contains(CommandBox.ERROR_STYLE_CLASS));
            }
        });
    }

    @Test
    public void handleCommandEntered_nonBlankInput_executedAndCleared() throws Exception {
        runOnFxThread(() -> {
            AtomicReference<String> executedCommand = new AtomicReference<>();
            CommandBox commandBox = new CommandBox(command -> {
                executedCommand.set(command);
                return new CommandResult("");
            });
            TextField input = (TextField) commandBox.getRoot().lookup("#commandTextField");
            String command = " list ";

            input.setText(command);
            input.fireEvent(new ActionEvent());

            assertEquals(command, executedCommand.get());
            assertEquals("", input.getText());
        });
    }

    /**
     * Runs UI assertions on the JavaFX application thread with a bounded wait.
     */
    private static void runOnFxThread(Runnable action) throws Exception {
        FutureTask<Void> task = new FutureTask<>(action, null);
        Platform.runLater(task);
        task.get(TOOLKIT_TIMEOUT_SECONDS, TimeUnit.SECONDS);
    }
}
