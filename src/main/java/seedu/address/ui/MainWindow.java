package seedu.address.ui;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Optional;
import java.util.logging.Logger;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SplitPane;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import seedu.address.commons.core.GuiSettings;
import seedu.address.commons.core.LogsCenter;
import seedu.address.logic.Logic;
import seedu.address.logic.commands.CommandResult;
import seedu.address.logic.commands.DemoCommand;
import seedu.address.logic.commands.ExitCommand;
import seedu.address.logic.commands.exceptions.CommandException;
import seedu.address.logic.parser.exceptions.ParseException;
import seedu.address.logic.view.DetailView;

/**
 * The Main Window. Provides the basic application layout containing
 * a menu bar and space where other JavaFX elements can be placed.
 */
public class MainWindow extends UiPart<Stage> {

    private static final String FXML = "MainWindow.fxml";
    private static final String MESSAGE_NEEDS_CONFIRMATION = "This needs your confirmation. "
            + "Nothing has been changed yet.";

    private final Logger logger = LogsCenter.getLogger(getClass());

    private Stage primaryStage;
    private Logic logic;
    private Path dataFilePath;

    // Independent Ui parts residing in this Ui container
    private PersonListPanel personListPanel;
    private LessonListPanel lessonListPanel;
    private HomeworkListPanel homeworkListPanel;
    private PanelTabs panelTabs;
    private DetailPane detailPane;
    private DemoBanner demoBanner;
    private ConfirmBar confirmBar;
    private CommandBox commandBox;
    private TourOverlay tourOverlay;
    private ResultDisplay resultDisplay;
    private HelpWindow helpWindow;

    /** Whether the detail pane shows the dashboard, which is redrawn after every command. */
    private boolean isDashboardShown;

    @FXML
    private StackPane commandBoxPlaceholder;

    @FXML
    private StackPane confirmBarPlaceholder;

    @FXML
    private MenuItem helpMenuItem;

    @FXML
    private StackPane windowRoot;

    @FXML
    private StackPane panelTabsPlaceholder;

    @FXML
    private StackPane detailPanePlaceholder;

    @FXML
    private StackPane demoBannerPlaceholder;

    @FXML
    private SplitPane splitPane;

    @FXML
    private StackPane resultDisplayPlaceholder;

    @FXML
    private StackPane statusbarPlaceholder;

    /**
     * Creates a {@code MainWindow} with the given {@code Stage}, {@code Logic},
     * and the data file path to show in the status bar.
     */
    public MainWindow(Stage primaryStage, Logic logic, Path dataFilePath) {
        super(FXML, primaryStage);

        // Set dependencies
        this.primaryStage = primaryStage;
        this.logic = logic;
        this.dataFilePath = dataFilePath;

        // Configure the UI
        setWindowDefaultSize(logic.getGuiSettings());

        setAccelerators();
        setPaneScrollKeys();

        helpWindow = new HelpWindow();
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    private void setAccelerators() {
        setAccelerator(helpMenuItem, KeyCombination.valueOf("F1"));
    }

    /**
     * Sets the accelerator of a MenuItem.
     * @param keyCombination the KeyCombination value of the accelerator
     */
    private void setAccelerator(MenuItem menuItem, KeyCombination keyCombination) {
        menuItem.setAccelerator(keyCombination);

        /*
         * TODO: the code below can be removed once the bug reported here
         * https://bugs.openjdk.java.net/browse/JDK-8131666
         * is fixed in a later version of the SDK.
         *
         * According to the bug report, TextInputControl (TextField, TextArea) will
         * consume function-key events. Because CommandBox contains a TextField and
         * ResultDisplay contains a TextArea, some accelerators (e.g., F1) will
         * not work when the focus is in them because the key event is consumed by
         * the TextInputControl(s).
         *
         * For now, we add the following event filter to capture such key events and open
         * the help window purposely so as to support accelerators even when focus is
         * in CommandBox or ResultDisplay.
         */
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getTarget() instanceof TextInputControl && keyCombination.match(event)) {
                menuItem.getOnAction().handle(new ActionEvent());
                event.consume();
            }
        });
    }

    /**
     * Lets Page Up and Page Down scroll the detail pane by most of a screen, and Ctrl+Up and Ctrl+Down by a few
     * lines, while the cursor stays in the command box.
     */
    private void setPaneScrollKeys() {
        getRoot().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (detailPane != null && event.getTarget() instanceof TextInputControl && scrollDetailPane(event)) {
                event.consume();
            }
        });
    }

    /** Scrolls the detail pane if {@code event} is one of its scroll keys, and returns whether it was. */
    private boolean scrollDetailPane(KeyEvent event) {
        KeyCode code = event.getCode();
        if (code == KeyCode.PAGE_UP || code == KeyCode.PAGE_DOWN) {
            detailPane.scrollPage(code == KeyCode.PAGE_DOWN);
            return true;
        }
        if (event.isControlDown() && (code == KeyCode.UP || code == KeyCode.DOWN)) {
            detailPane.scrollLines(code == KeyCode.DOWN);
            return true;
        }
        return false;
    }

    /**
     * Fills up all the placeholders of this window.
     */
    void fillInnerParts() {
        PanelActions panelActions = new WindowActions();
        personListPanel = new PersonListPanel(logic.getFilteredPersonList(), panelActions);
        lessonListPanel = new LessonListPanel(logic.getFilteredLessonList(), panelActions);
        homeworkListPanel = new HomeworkListPanel(panelActions);
        homeworkListPanel.show(logic.getHomeworkRows());
        panelTabs = new PanelTabs(personListPanel, lessonListPanel, homeworkListPanel);
        panelTabsPlaceholder.getChildren().add(panelTabs.getRoot());
        setTabShortcuts();

        detailPane = new DetailPane();
        detailPanePlaceholder.getChildren().add(detailPane.getRoot());
        DetailView dashboard = logic.getDashboardView();
        detailPane.show(dashboard);
        isDashboardShown = true;

        resultDisplay = new ResultDisplay();
        resultDisplayPlaceholder.getChildren().add(resultDisplay.getRoot());
        String warning = logic.getStartupWarning();
        resultDisplay.setFeedbackToUser(warning.isEmpty() ? dashboard.resultBoxText()
                : warning + "\n\n" + dashboard.resultBoxText());

        StatusBarFooter statusBarFooter = new StatusBarFooter(dataFilePath);
        statusbarPlaceholder.getChildren().add(statusBarFooter.getRoot());

        commandBox = new CommandBox(this::executeCommand);
        commandBoxPlaceholder.getChildren().add(commandBox.getRoot());

        confirmBar = new ConfirmBar(panelActions, () -> resultDisplay.setFeedbackToUser(ConfirmBar.MESSAGE_CANCELLED));
        confirmBarPlaceholder.getChildren().add(confirmBar.getRoot());
        if (!logic.isTourSeen()) {
            Platform.runLater(this::handleTour);
        }
        getRoot().getScene().addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && confirmBar.isAsking()) {
                confirmBar.cancel();
                event.consume();
            }
        });

        demoBanner = new DemoBanner(this::exitDemo);
        demoBannerPlaceholder.getChildren().add(demoBanner.getRoot());
        demoBanner.setDemoMode(logic.isDemoMode());
    }

    /** Lets Ctrl+1, Ctrl+2 and Ctrl+3 show the students, the lessons and the homework. */
    private void setTabShortcuts() {
        PanelTabs.Panel[] panels = PanelTabs.Panel.values();
        KeyCode[] keys = {KeyCode.DIGIT1, KeyCode.DIGIT2, KeyCode.DIGIT3};
        for (int i = 0; i < panels.length; i++) {
            PanelTabs.Panel panel = panels[i];
            getRoot().getScene().getAccelerators().put(
                    new KeyCodeCombination(keys[i], KeyCombination.SHORTCUT_DOWN), () -> panelTabs.select(panel));
        }
    }

    /** Runs {@code demo exit} for the banner's button, whose result or error the result box shows. */
    private void exitDemo() {
        try {
            executeCommand(DemoCommand.COMMAND_WORD + " " + DemoCommand.EXIT);
        } catch (CommandException | ParseException e) {
            logger.info("Leaving the demo was refused"); // the result box shows why
        }
    }

    /**
     * Sets the default size based on {@code guiSettings}.
     */
    private void setWindowDefaultSize(GuiSettings guiSettings) {
        primaryStage.setHeight(guiSettings.getWindowHeight());
        primaryStage.setWidth(guiSettings.getWindowWidth());
        if (guiSettings.getWindowCoordinates() != null) {
            primaryStage.setX(guiSettings.getWindowCoordinates().getX());
            primaryStage.setY(guiSettings.getWindowCoordinates().getY());
        }
    }

    /**
     * Opens the help window or focuses on it if it's already opened.
     */
    @FXML
    public void handleHelp() {
        if (!helpWindow.isShowing()) {
            helpWindow.show();
        } else {
            helpWindow.focus();
        }
    }

    /**
     * Starts the guided tour, from its first step, unless it is on screen already.
     */
    @FXML
    public void handleTour() {
        if (tourOverlay != null && tourOverlay.isShowing()) {
            return;
        }
        panelTabs.select(PanelTabs.Panel.STUDENTS);
        tourOverlay = new TourOverlay(GuidedTour.steps(LocalDate.now()),
                selector -> Optional.ofNullable(primaryStage.getScene().lookup(selector)),
                commandBox::prefill, this::endTour);
        windowRoot.getChildren().add(tourOverlay.getRoot());
        tourOverlay.start();
    }

    /** Takes the tour off the window and remembers that the user has seen it, so that it is not offered again. */
    private void endTour() {
        windowRoot.getChildren().remove(tourOverlay.getRoot());
        logic.markTourSeen();
        commandBox.focus();
    }

    void show() {
        primaryStage.show();
    }

    /**
     * Handles the window's close button and File > Exit. Both run the {@code exit} command, which refuses to close
     * TutorFlow while the latest changes could not be saved, so that closing the window cannot lose data.
     */
    @FXML
    private void handleExitRequest(Event event) {
        event.consume(); // the window is only closed by handleExit(), once the exit command has succeeded
        try {
            executeCommand(ExitCommand.COMMAND_WORD);
        } catch (CommandException | ParseException e) {
            logger.info("Window close refused: pending changes could not be saved"); // the result box shows why
        }
    }

    /**
     * Closes the application.
     */
    @FXML
    private void handleExit() {
        GuiSettings guiSettings = new GuiSettings(primaryStage.getWidth(), primaryStage.getHeight(),
                (int) primaryStage.getX(), (int) primaryStage.getY());
        logic.setGuiSettings(guiSettings);
        helpWindow.hide();
        primaryStage.hide();
    }

    public PersonListPanel getPersonListPanel() {
        return personListPanel;
    }

    public DetailPane getDetailPane() {
        return detailPane;
    }

    public DemoBanner getDemoBanner() {
        return demoBanner;
    }

    public PanelTabs getPanelTabs() {
        return panelTabs;
    }

    public LessonListPanel getLessonListPanel() {
        return lessonListPanel;
    }

    public HomeworkListPanel getHomeworkListPanel() {
        return homeworkListPanel;
    }

    public ConfirmBar getConfirmBar() {
        return confirmBar;
    }

    public SplitPane getSplitPane() {
        return splitPane;
    }

    /** Shows what {@code commandResult} has for the detail pane, or keeps the dashboard in step with the data. */
    private void showResult(CommandResult commandResult) {
        Optional<DetailView> view = commandResult.getDetailView();
        if (view.isPresent()) {
            detailPane.show(view.get());
            isDashboardShown = view.get().isLive();
            resultDisplay.setFeedbackToUser(view.get().resultBoxText());
        } else {
            resultDisplay.setFeedbackToUser(commandResult.getFeedbackToUser());
            refreshDashboard();
        }
        personListPanel.highlight(commandResult.getAddedStudents());
        demoBanner.setDemoMode(logic.isDemoMode());
        homeworkListPanel.show(logic.getHomeworkRows());
        showPanelFor(commandResult);
        commandResult.getConfirmationCommand().ifPresentOrElse(
                command -> confirmBar.ask(MESSAGE_NEEDS_CONFIRMATION, command), confirmBar::clear);
    }

    /** Shows the list that {@code commandResult} filled with numbered rows, so that the user sees the numbers. */
    private void showPanelFor(CommandResult commandResult) {
        if (commandResult.getDisplayedHomework().isPresent()) {
            panelTabs.select(PanelTabs.Panel.HOMEWORK);
        } else if (commandResult.getDisplayedLessons().isPresent()) {
            panelTabs.select(PanelTabs.Panel.LESSONS);
        } else if (!commandResult.getAddedStudents().isEmpty()) {
            panelTabs.select(PanelTabs.Panel.STUDENTS);
        }
    }

    /** Redraws the dashboard after a command, as long as the detail pane shows it, so that it never goes stale. */
    private void refreshDashboard() {
        if (isDashboardShown) {
            detailPane.refresh(logic.getDashboardView());
        }
    }

    /**
     * Executes the command and returns the result.
     *
     * @see seedu.address.logic.Logic#execute(String)
     */
    private CommandResult executeCommand(String commandText) throws CommandException, ParseException {
        try {
            CommandResult commandResult = logic.execute(commandText);
            logger.fine("Command succeeded");
            showResult(commandResult);

            if (commandResult.isShowHelp()) {
                handleHelp();
            }

            if (commandResult.isTourRequested()) {
                handleTour();
            }

            if (commandResult.isExit()) {
                handleExit();
            }

            return commandResult;
        } catch (CommandException | ParseException e) {
            logger.fine("Command rejected: " + e.getClass().getSimpleName());
            resultDisplay.setErrorToUser(e.getMessage());
            refreshDashboard();
            homeworkListPanel.show(logic.getHomeworkRows());
            confirmBar.clear();
            throw e;
        }
    }

    /** What the buttons of the panels can ask of this window. */
    private class WindowActions implements PanelActions {
        @Override
        public void run(String command) {
            try {
                executeCommand(command);
            } catch (CommandException | ParseException e) {
                logger.fine("Command from a button rejected"); // the result box shows why
            }
        }

        @Override
        public void prefill(String text) {
            confirmBar.clear();
            commandBox.prefill(text);
        }

        @Override
        public void ask(String question, String command) {
            confirmBar.ask(question, command);
        }
    }
}
