package gui;

import apptemplate.AppTemplate;
import components.AppWorkspaceComponent;
import controller.HangmanController;
import data.GameData;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToolBar;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Shape;
import propertymanager.PropertyManager;
import ui.AppGUI;

import java.io.IOException;
import java.util.ArrayList;

import static hangman.HangmanProperties.*;

/**
 * This class serves as the GUI component for the Hangman game.
 *
 * @author Ritwik Banerjee
 * @author Weifeng Lin
 */
public class Workspace extends AppWorkspaceComponent {

    AppTemplate app; // the actual application
    AppGUI      gui; // the GUI inside which the application sits

    Label      guiHeadingLabel;   // workspace (GUI) heading label
    HBox       headPane;          // container to display the heading
    HBox       bodyPane;          // container for the main game displays
    ToolBar    footToolbar;       // toolbar for game buttons
    //BorderPane figurePane;        // container to display the namesake graphic of the (potentially) hanging person
    StackPane  figurePane;
    VBox       gameTextsPane;     // container to display the text-related parts of the game
    HBox       guessedLetters;    // text area displaying all the letters guessed so far
    HBox       remainingGuessBox; // container to display the number of remaining guesses
    Button     startGame;         // the button to start playing a game of Hangman

    HBox     goodAndBadGuessesLabel;
    GridPane goodAndBadGuesses;
    Canvas     canvas;
    GraphicsContext gc;
    //ArrayList<Shape> hangmancomponents;
    //Shape[]     hangmancomponents;
    Pane    hangmancomponents;

    /**
     * Constructor for initializing the workspace, note that this constructor
     * will fully setup the workspace user interface for use.
     *
     * @param initApp The application this workspace is part of.
     * @throws IOException Thrown should there be an error loading application
     *                     data for setting up the user interface.
     */
    public Workspace(AppTemplate initApp) throws IOException {
        app = initApp;
        gui = app.getGUI();
        layoutGUI();     // initialize all the workspace (GUI) components including the containers and their layout
        setupHandlers(); // ... and set up event handling
    }


    private void layoutGUI() {
        PropertyManager propertyManager = PropertyManager.getManager();
        guiHeadingLabel = new Label(propertyManager.getPropertyValue(WORKSPACE_HEADING_LABEL));

        headPane = new HBox();
        headPane.getChildren().add(guiHeadingLabel);
        headPane.setAlignment(Pos.CENTER);

        //figurePane = new BorderPane();      // container to display the namesake graphic of the (potentially) hanging person
        figurePane = new StackPane();
        renderHangman();



        guessedLetters = new HBox();
        guessedLetters.setStyle("-fx-background-color: transparent;");

        remainingGuessBox = new HBox();

        gameTextsPane = new VBox();         // container to display the text-related parts of the game

        //good and bad guesses
        goodAndBadGuessesLabel = new HBox();

        goodAndBadGuesses = new GridPane();
        //gameTextsPane.getChildren().addAll(remainingGuessBox, guessedLetters, goodAndBadGuessesLabel, goodAndBadGuesses);
        gameTextsPane.getChildren().setAll(remainingGuessBox, guessedLetters, goodAndBadGuessesLabel, goodAndBadGuesses);


        bodyPane = new HBox();
        bodyPane.getChildren().addAll(figurePane, gameTextsPane);
        bodyPane.setPadding(new Insets(1,0,1,gui.getSaveButton().snappedLeftInset()));

        startGame = new Button("Start Playing");
        HBox blankBoxLeft  = new HBox();
        HBox blankBoxRight = new HBox();
        HBox.setHgrow(blankBoxLeft, Priority.ALWAYS);
        HBox.setHgrow(blankBoxRight, Priority.ALWAYS);
        footToolbar = new ToolBar(blankBoxLeft, startGame, blankBoxRight);

        //workspace = new VBox();
        workspace = new VBox();
        workspace.getChildren().addAll(headPane, bodyPane, footToolbar);
    }

    private void setupHandlers() {
        //app.setController(app, startGame);
//        HangmanController controller = new HangmanController(app, startGame);
//        HangmanController controller = (HangmanController) app.getFileController();
//        controller.setGameButton(startGame);
        HangmanController controller = (HangmanController) gui.getFileController();
        controller.setGameButton(startGame);
        startGame.setOnMouseClicked(e -> controller.start());
    }

    /**
     * This function specifies the CSS for all the UI components known at the time the workspace is initially
     * constructed. Components added and/or removed dynamically as the application runs need to be set up separately.
     */
    @Override
    public void initStyle() {
        PropertyManager propertyManager = PropertyManager.getManager();

        gui.getAppPane().setId(propertyManager.getPropertyValue(ROOT_BORDERPANE_ID));
        gui.getToolbarPane().getStyleClass().setAll(propertyManager.getPropertyValue(SEGMENTED_BUTTON_BAR));
        gui.getToolbarPane().setId(propertyManager.getPropertyValue(TOP_TOOLBAR_ID));

        ObservableList<Node> toolbarChildren = gui.getToolbarPane().getChildren();
        toolbarChildren.get(0).getStyleClass().add(propertyManager.getPropertyValue(FIRST_TOOLBAR_BUTTON));
        toolbarChildren.get(toolbarChildren.size() - 1).getStyleClass().add(propertyManager.getPropertyValue(LAST_TOOLBAR_BUTTON));

        workspace.getStyleClass().add(CLASS_BORDERED_PANE);
        guiHeadingLabel.getStyleClass().setAll(propertyManager.getPropertyValue(HEADING_LABEL));

    }

    /** This function reloads the entire workspace */
    @Override
    public void reloadWorkspace() {
       GameData data= (GameData)app.getDataComponent();
    }

    public VBox getGameTextsPane() {
        return gameTextsPane;
    }


    public HBox getRemainingGuessBox() {
        return remainingGuessBox;
    }

    public Button getStartGame() {
        return startGame;
    }

    public void reinitialize() {

        //reinitialize hangmancomponent
        for (int i=0;i<10;i++){
            hangmancomponents.getChildren().get(i).setVisible(false);
        }

        guessedLetters = new HBox();
        guessedLetters.setStyle("-fx-background-color: transparent;");
        remainingGuessBox = new HBox();
        gameTextsPane = new VBox();
        //figurePane = new BorderPane();      // container to display the namesake graphic of the (potentially) hanging person
        //figurePane = new StackPane();

        goodAndBadGuessesLabel = new HBox();
        goodAndBadGuesses = new GridPane();
        //renderHangman();
        gameTextsPane.getChildren().setAll(remainingGuessBox, guessedLetters, goodAndBadGuessesLabel, goodAndBadGuesses);

        bodyPane.getChildren().setAll(figurePane, gameTextsPane);
        //bodyPane.getChildren().addAll(figurePane, gameTextsPane);
        bodyPane.setPadding(new Insets(1,0,1,gui.getSaveButton().snappedLeftInset()));
    }


    public void renderHangman(){


        String black = "#000000";
        canvas = new Canvas();
        canvas.setHeight(600);
        canvas.setWidth(400);
        gc = canvas.getGraphicsContext2D();
        Group group = new Group();



        //hangmancomponents = new Shape[10];

       //hangmancomponents  = new ArrayList<>();
        hangmancomponents = new Pane();

        //0
        Shape buttonHorizontol = new Line(5,450, 55,450);
        buttonHorizontol.setStrokeWidth(5);
        buttonHorizontol.setStroke(Paint.valueOf(black));
        buttonHorizontol.setVisible(false);
        hangmancomponents.getChildren().add(buttonHorizontol);
        //1
        Shape mainVertical     = new Line(5,5,5,450);
        mainVertical.setStrokeWidth(5);
        mainVertical.setStroke(Paint.valueOf(black));
        mainVertical.setVisible(false);
        hangmancomponents.getChildren().add(mainVertical);
        //2
        Shape topHorizontal = new Line(5,5,205,5);
        topHorizontal.setStrokeWidth(5);
        topHorizontal.setStroke(Paint.valueOf(black));
        topHorizontal.setVisible(false);
        hangmancomponents.getChildren().add(topHorizontal);
        //3
        Shape topVertical   = new Line(205,5,205,25);
        topVertical.setStrokeWidth(5);
        topVertical.setStroke(Paint.valueOf(black));
        topVertical.setVisible(false);
        hangmancomponents.getChildren().add(topVertical);
        //4
        Shape head          = new Circle(205, 70, 45, Color.TRANSPARENT);
        head.setStroke(Paint.valueOf(black));
        head.setStrokeWidth(5);
        head.setVisible(false);
        hangmancomponents.getChildren().add(head);
        //5
        Shape bodyLine      = new Line(205,115, 205, 205);
        bodyLine.setStrokeWidth(5);
        bodyLine.setStroke(Paint.valueOf(black));
        bodyLine.setVisible(false);
        hangmancomponents.getChildren().add(bodyLine);
        //6
        Shape leftArm       = new Line(160, 160, 205, 160);
        leftArm.setStrokeWidth(5);
        leftArm.setStroke(Paint.valueOf(black));
        leftArm.setVisible(false);
        hangmancomponents.getChildren().add(leftArm);
        //7
        Shape rightArm      = new Line(205, 160,250, 160);
        rightArm.setStrokeWidth(5);
        rightArm.setStroke(Paint.valueOf(black));
        rightArm.setVisible(false);
        hangmancomponents.getChildren().add(rightArm);
        //8
        Shape leftLeg       = new Line(205,205,160,275);
        leftLeg.setStroke(Paint.valueOf(black));
        leftLeg.setStrokeWidth(5);
        leftLeg.setVisible(false);
        hangmancomponents.getChildren().add(leftLeg);
        //9
        Shape rightLeg      = new Line(205,205, 250, 275);
        rightLeg.setStrokeWidth(5);
        rightLeg.setStroke(Paint.valueOf(black));
        rightLeg.setVisible(false);
        hangmancomponents.getChildren().add(rightLeg);






//        gc.beginPath();
//        gc.setStroke(Paint.valueOf(black));
//        gc.setLineWidth(5);
//        gc.setGlobalBlendMode(BlendMode.SOFT_LIGHT);


//        for (int i=0;i<10;i++){
//            gc.setStroke(hangmancomponents.get(i).getStroke());
//        }

        group.getChildren().addAll(canvas,hangmancomponents);

//        gc.strokeLine(5,5,5,450); //vertical
//        gc.strokeLine(5,450, 55,450); // button horizontal
//        gc.strokeLine(5,5,205,5);   // top horizontal
//        gc.strokeLine(205,5,205,25); // top vertical
//        gc.strokeOval(160, 25, 90,90);  // head
//        gc.strokeLine(205,115, 205, 205);       // body line
//        gc.strokeLine(160, 160, 250, 160);      // arms
//        gc.strokeLine(205,205,160,275);         // left leg
//        gc.strokeLine(205,205, 250, 275);       // right leg

        Pane scene = new Pane(group);

        figurePane.getChildren().addAll(scene);



    }

    public Pane getHangmancomponents(){
        return hangmancomponents;
    }

}
