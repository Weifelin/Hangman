package controller;

import apptemplate.AppTemplate;
import data.GameData;
import gui.Workspace;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.BlendMode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;
import propertymanager.PropertyManager;
import ui.AppMessageDialogSingleton;
import ui.YesNoCancelDialogSingleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Set;

import static hangman.HangmanProperties.GOOD_AND_BAD_GUESSES_LABEL;
import static settings.AppPropertyType.*;
import static settings.InitializationParameters.APP_WORKDIR_PATH;


/**
 * @author Ritwik Banerjee
 * @author Weifeng Lin
 */
public class HangmanController implements FileController {

    private AppTemplate appTemplate; // shared reference to the application
    private GameData    gamedata;    // shared reference to the game being played, loaded or saved
    private Text[]      progress;    // reference to the text area for the word
    private boolean     success;     // whether or not player was successful
    private int         discovered;  // the number of letters already discovered
    private Button      gameButton;  // shared reference to the "start game" button
    private Label       remains;     // dynamically updated label that indicates the number of remaining guesses
    private boolean     gameover;    // whether or not the current game is already over
    private boolean     savable;
    private File        workFile;
    //text bar

    private Text[]      alphabet;
    protected Rectangle[] squares;        // squares for alphabet
    private Button      hintButton;
    //private boolean     hintButtonUsed;
    //private boolean     hintButtonDisplayed;



    PropertyManager propertyManager = PropertyManager.getManager();

    public HangmanController(AppTemplate appTemplate, Button gameButton) {
        this(appTemplate);
        this.gameButton = gameButton;   // this gameButton is the startGame button from workspace
    }


    public HangmanController(AppTemplate appTemplate) {
        this.appTemplate = appTemplate;
    }


    public void setGameButton(Button gameButton){
        this.gameButton = gameButton;
    }


//    public void setGameButton(Button gameButton){
//        this.gameButton = gameButton;
//    }

    public void enableGameButton() {
        if (gameButton == null) {
            Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameButton = workspace.getStartGame();
        }
        gameButton.setDisable(false);

    }

    public void start() {
        gamedata = new GameData(appTemplate);
        appTemplate.setDataComponent(gamedata);

        //enable save button
        appTemplate.getGUI().getNewButton().setDisable(false);

        gameover = false;
        success = false;
        savable = true;
        discovered = 0;

//        hintButtonUsed = gamedata.getHindButtonUsed();
//        hintButtonDisplayed = gamedata.getHintButtonDisplayed();

        Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();

        appTemplate.getGUI().updateWorkspaceToolbar(savable);   // savable is true.
        HBox remainingGuessBox = gameWorkspace.getRemainingGuessBox();
        HBox guessedLetters    = (HBox) gameWorkspace.getGameTextsPane().getChildren().get(1);


        HBox     goodAndBadGuessesLabel = (HBox) gameWorkspace.getGameTextsPane().getChildren().get(2);
        GridPane goodAndBadGuesses = (GridPane) gameWorkspace.getGameTextsPane().getChildren().get(3);

        //Label typeOfGuesses = new Label(propertyManager.getPropertyValue())
        goodAndBadGuessesLabel.getChildren().addAll(new Label(propertyManager.getPropertyValue(GOOD_AND_BAD_GUESSES_LABEL)));

        remains = new Label(Integer.toString(GameData.TOTAL_NUMBER_OF_GUESSES_ALLOWED));
        remainingGuessBox.getChildren().addAll(new Label("Remaining Guesses: "), remains);
        hintButton = gameWorkspace.getHintButton();
//        hintButton.setVisible(true);
        hintButton.setOnMouseClicked(event -> hintProcess());
        String targetWord = gamedata.getTargetWord();
//        hintButton.setDisable(true);
        //check if the gameData has 7 unique letters
        gamedata.setHintButtonDisabled(true);
        gamedata.setHintButtonVisable(false);

        String simpfiedTarget = "";
        for (int i=0; i<targetWord.length();i++){
            if (!simpfiedTarget.contains(String.valueOf(targetWord.charAt(i)))){
                simpfiedTarget += targetWord.charAt(i);
            }
        }
        if (simpfiedTarget.length() >=7 ){
//            hintButton.setVisible(true);
//            hintButton.setDisable(false);
            gamedata.setHintButtonVisable(true);
            gamedata.setHintButtonDisabled(false);
            gameWorkspace.setHintButtonVisable(gamedata.getHintButtonVisable());
            gameWorkspace.setHintButtonDisabled(gamedata.getHintButtonDisabled());
        }

        initWordGraphics(guessedLetters);
        initAlphabet(goodAndBadGuesses);

        play();
        //gameButton.setOnAction(e-> gameWorkspace.reinitialize()); // change the function of start playing button to reset.
        gameButton.setDisable(true);

    }



    private void end() {
//        System.out.println(success ? "You win!" : "Ah, close but not quite there. The word was \"" + gamedata.getTargetWord() + "\".");
//
//        appTemplate.getGUI().getPrimaryScene().setOnKeyTyped(null);
//        gameover = true;
//        gameButton.setDisable(true);
//        savable = false; // cannot save a game that is already over
//        appTemplate.getGUI().updateWorkspaceToolbar(savable);

        Platform.runLater(()->{

            if (success == true){
                AppMessageDialogSingleton dialogSingleton = AppMessageDialogSingleton.getSingleton();
                dialogSingleton.showEnd(propertyManager.getPropertyValue(WIN_LABEL_TITLE), propertyManager.getPropertyValue(WIN_LABEL_MESSAGE));
            } else if (success == false){
                Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
                HBox targetword = (HBox) gameWorkspace.getGameTextsPane().getChildren().get(1);
                String goodguess = gamedata.getGoodGuesses().toString();
                String guessed = "";
                for (int i=0; i<goodguess.length(); i++){
                    if (Character.isLetter(goodguess.charAt(i))){
                        guessed += goodguess.charAt(i);
                    }
                }

                for (int i=0; i<progress.length;i++){
                    if (!guessed.contains(progress[i].getText())) {
                        StackPane stackPane = (StackPane) targetword.getChildren().get(i);
                        Rectangle textBox = (Rectangle) stackPane.getChildren().get(0);
                        textBox.setFill(Color.RED);
                        textBox.setBlendMode(BlendMode.SOFT_LIGHT);
                        progress[i].setVisible(true);
                    }
                }

                gamedata.setHintButtonDisabled(true);
                gameWorkspace.setHintButtonDisabled(gamedata.getHintButtonDisabled());

                AppMessageDialogSingleton dialogSingleton1 = AppMessageDialogSingleton.getSingleton();
                dialogSingleton1.showEnd(propertyManager.getPropertyValue(LOST_LABEL_TITLE), propertyManager.getPropertyValue(LOST_LABEL_MESSAGE ));
            }
        });

        appTemplate.getGUI().getPrimaryScene().setOnKeyTyped(null);
        gameover = true;
        gameButton.setDisable(true);
        savable = false; // cannot save a game that is already over
        appTemplate.getGUI().updateWorkspaceToolbar(savable);


    }

    private void initWordGraphics(HBox guessedLetters) {
        char[] targetword = gamedata.getTargetWord().toCharArray();

        progress = new Text[targetword.length];
        for (int i = 0; i < progress.length; i++) {
            progress[i] = new Text(Character.toString(targetword[i]));
            progress[i].setVisible(false);
        }

        //creating target word bar.
        StackPane[] rectangles = new StackPane[targetword.length];


        for (int i=0; i < rectangles.length; i++){
            rectangles[i] = new StackPane();
            Rectangle rectangle = new Rectangle(25,25, Color.WHITE);
            rectangle.setStroke(Color.GRAY);
            rectangle.setBlendMode(BlendMode.SOFT_LIGHT);
            rectangles[i].getChildren().addAll(rectangle, progress[i]);
            rectangles[i].setPadding(new Insets(1,1,2,0));
            guessedLetters.getChildren().addAll(rectangles[i]);
        }
        //guessedLetters.getChildren().addAll(rectangles);
    }

    private void initAlphabet(GridPane goodAndBadGuesses) {

        goodAndBadGuesses.setHgap(2);
        goodAndBadGuesses.setVgap(2);

        //set Text
        alphabet = new Text[30];
        for (int i=0; i<30; i++){
            alphabet[i] = new Text(Character.toString((char)(i+65)));
            if (i>25){
                alphabet[i].setVisible(false);
            }
        }

        // set for squares
        squares = new Rectangle[30];
        for (int i=0; i<30; i++){
            squares[i] = new Rectangle(50,50, Color.WHITE);
            squares[i].setStroke(Color.GRAY);
            squares[i].setBlendMode(BlendMode.SOFT_LIGHT);
            if (i>25){
                squares[i].setVisible(false);
            }
        }


        // set for stackPane
        StackPane[] stackPane = new StackPane[30];
        for (int i = 0; i<30; i++) {
            stackPane[i] = new StackPane();
            stackPane[i].getChildren().addAll(squares[i], alphabet[i]);
        }

        int alphaOrder = 0;
        for (int rows=0; rows < 6; rows++){
            for (int columns=0;columns<5; columns++){
                goodAndBadGuesses.add(stackPane[alphaOrder], columns, rows);
                alphaOrder++;
            }
        }

    }


    private void hintProcess(){

        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                String targetWord = gamedata.getTargetWord();
                int charIndex     = new Random().nextInt(targetWord.length());
                while (alreadyGuessed(targetWord.charAt(charIndex))){
                    charIndex     = new Random().nextInt(targetWord.length());
                }

                for (int i=0; i<progress.length; i++){
                    if (targetWord.charAt(charIndex) == progress[i].getText().charAt(0)) {
                        progress[i].setVisible(true);
                        discovered++;
                    }

                }

                gamedata.addGoodGuess(targetWord.charAt(charIndex));
                char capitilized = Character.toUpperCase(targetWord.charAt(charIndex));
                int alphaOrder = (int) capitilized - 65;
                squares[alphaOrder].setFill(Color.AQUA);
                squares[alphaOrder].setBlendMode(BlendMode.SOFT_LIGHT);
//                hintButton.setDisable(true);
                gamedata.setHintButtonDisabled(true);
                Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
                workspace.setHintButtonDisabled(gamedata.getHintButtonDisabled());


                success = (discovered == progress.length);
                remains.setText(Integer.toString(gamedata.getRemainingGuesses()));
                stop();
                if (gamedata.getRemainingGuesses() <= 0 || success)
                    end();

            }

            @Override
            public void stop() {
                super.stop();
            }
        };
        timer.start();
    }


    public void play() {
        AnimationTimer timer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                appTemplate.getGUI().getPrimaryScene().setOnKeyTyped((KeyEvent event) -> {
                    char guess = event.getCharacter().charAt(0);
                    if (!Character.isLetter(guess))
                        return;




                    if (!alreadyGuessed(guess)) {
                        boolean goodguess = false;
                        for (int i = 0; i < progress.length; i++) {
                            if (gamedata.getTargetWord().charAt(i) == guess) {
                                progress[i].setVisible(true);
                                gamedata.addGoodGuess(guess);
                                //change the color in alphabet
                                char capitilized = Character.toUpperCase(guess);
                                int alphaOrder = (int) capitilized - 65;
                                squares[alphaOrder].setFill(Color.AQUA);
                                squares[alphaOrder].setBlendMode(BlendMode.SOFT_LIGHT);
                                goodguess = true;
                                discovered++;
                            }
                        }
                        if (!goodguess) {
                            gamedata.addBadGuess(guess);
                            char capitilized = Character.toUpperCase(guess);
                            int alphaOrder = (int) capitilized - 65;
                            squares[alphaOrder].setFill(Color.RED);
                            squares[alphaOrder].setBlendMode(BlendMode.SOFT_LIGHT);

                            for (int bads=0; bads < (gamedata.TOTAL_NUMBER_OF_GUESSES_ALLOWED-gamedata.getRemainingGuesses()); bads++){
                                Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
                                gameWorkspace.getHangmancomponents().getChildren().get(bads).setVisible(true);
                            }


                        }
                        success = (discovered == progress.length);
                        remains.setText(Integer.toString(gamedata.getRemainingGuesses()));
                    }
                });
                if (gamedata.getRemainingGuesses() <= 0 || success)
                    stop();
            }

            @Override
            public void stop() {
                super.stop();
                end();
            }
        };
        timer.start();
    }

    private boolean alreadyGuessed(char c) {
        return gamedata.getGoodGuesses().contains(c) || gamedata.getBadGuesses().contains(c);
    }
    
    @Override
    public void handleNewRequest() {

        AppMessageDialogSingleton messageDialog   = AppMessageDialogSingleton.getSingleton();
        PropertyManager           propertyManager = PropertyManager.getManager();
        boolean                   makenew         = true;
        boolean                   restart         = false;
        int saveOrNot;
        if (savable)
            try {
                saveOrNot = promptToSave();// 1 for yes, 2 for false. 0 for nothing.

                if (saveOrNot == 1)
                makenew = true;

                if (saveOrNot == 2){
                    makenew = false;
                    restart = true;
                    gameover = false;
                }
                if (saveOrNot == 0){
                    makenew = false;
                    gameover = false;
                }

            } catch (IOException e) {
                messageDialog.show(propertyManager.getPropertyValue(NEW_ERROR_TITLE), propertyManager.getPropertyValue(NEW_ERROR_MESSAGE));
            }
        if (makenew) {

            appTemplate.getDataComponent().reset();                // reset the data (should be reflected in GUI)
            //appTemplate.getWorkspaceComponent().reloadWorkspace(); // load data into workspace
            ensureActivatedWorkspace();                            // ensure workspace is activated
            workFile = null;                                       // new workspace has never been saved to a file

            Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameWorkspace.reinitialize();
            enableGameButton();

            //disable the save buttom
            appTemplate.getGUI().getNewButton().setDisable(true);

        }

        if (gameover) {
            savable = false;
            //hintButton.setVisible(false);
            gamedata.setHintButtonVisable(false);
            Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
            workspace.setHintButtonVisable(gamedata.getHintButtonVisable());
            appTemplate.getGUI().updateWorkspaceToolbar(savable);
            Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameWorkspace.reinitialize();
            enableGameButton();
        }

        if (restart) {

            appTemplate.getDataComponent().reset();                // reset the data (should be reflected in GUI)
            //appTemplate.getWorkspaceComponent().reloadWorkspace(); // load data into workspace
            ensureActivatedWorkspace();                            // ensure workspace is activated
            workFile = null;                                       // new workspace has never been saved to a file

            Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameWorkspace.reinitialize();
            enableGameButton();

            //disable the save buttom
            appTemplate.getGUI().getNewButton().setDisable(true);
            start();

        }


    }


    public boolean handleNewRequestInLoad(){
        AppMessageDialogSingleton messageDialog   = AppMessageDialogSingleton.getSingleton();
        PropertyManager           propertyManager = PropertyManager.getManager();
        boolean                   makenew         = true;
        int saveOrNot;
        if (savable)
            try {
                saveOrNot = promptToSave();// 1 for yes, 2 for false. 0 for nothing.

                if (saveOrNot == 1)
                    makenew = true;

                if (saveOrNot == 2){
                    makenew = false;
                    gameover = true;
                }
                if (saveOrNot == 0){
                    //makenew = false;
                    gameover = false;
                    return false;
                }

            } catch (IOException e) {
                messageDialog.show(propertyManager.getPropertyValue(NEW_ERROR_TITLE), propertyManager.getPropertyValue(NEW_ERROR_MESSAGE));
            }
        if (makenew) {

            appTemplate.getDataComponent().reset();                // reset the data (should be reflected in GUI)
            //appTemplate.getWorkspaceComponent().reloadWorkspace(); // load data into workspace
            ensureActivatedWorkspace();                            // ensure workspace is activated
            workFile = null;                                       // new workspace has never been saved to a file

            Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameWorkspace.reinitialize();
            enableGameButton();

            //disable the save buttom
            appTemplate.getGUI().getNewButton().setDisable(true);

        }

        if (gameover) {
            savable = false;
            //hintButton.setVisible(false);
            gamedata.setHintButtonVisable(false);
            Workspace workspace = (Workspace) appTemplate.getWorkspaceComponent();
            workspace.setHintButtonVisable(gamedata.getHintButtonVisable());
            appTemplate.getGUI().updateWorkspaceToolbar(savable);
            Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
            gameWorkspace.reinitialize();
            enableGameButton();
        }

        return true;
    }
    
    @Override
    public void handleSaveRequest() throws IOException {
//        PropertyManager propertyManager = PropertyManager.getManager();
        try {
            if (workFile != null)
                save(workFile);
            else {
                FileChooser fileChooser = new FileChooser();
                //String workD = "Hangman/resources/"+ AppTemplate.class.getClassLoader().getResource(APP_WORKDIR_PATH.getParameter());
                URL workDirtURL = AppTemplate.class.getClassLoader().getResource(APP_WORKDIR_PATH.getParameter());
                if (workDirtURL == null)
                    throw new FileNotFoundException("Work Folder Not Found under resources");

                File initialDir = new File(workDirtURL.getFile());
                fileChooser.setInitialDirectory(initialDir);
                fileChooser.setTitle(propertyManager.getPropertyValue(SAVE_WORK_TITLE));
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(propertyManager.getPropertyValue(WORK_FILE_EXT_DESC), propertyManager.getPropertyValue(WORK_FILE_EXT)));

                fileChooser.setInitialFileName("gameData.json");
                File selectedFile = fileChooser.showSaveDialog(appTemplate.getGUI().getWindow());

                if (selectedFile != null)
                    save(selectedFile);
            }

//            PropertyManager props = PropertyManager.getManager();
//
//            FileChooser saveWin = new FileChooser();
//            saveWin.setTitle(props.getPropertyValue("SAVE_WORK_TITLE"));
//
//            saveWin.getExtensionFilters().add(new FileChooser.ExtensionFilter(props.getPropertyValue("WORK_FILE_EXT"), ".json"));
//            saveWin.setInitialFileName("gameData.json");
//
//
//            File toSave = saveWin.showSaveDialog(appTemplate.getGUI().getWindow());
//
//            ObjectMapper data = new ObjectMapper();
//
//            GameDataFile file = new GameDataFile();
//
////            data.writeValue(toSave, gamedata.getTargetWord());
////            data.writeValue(toSave, gamedata.getRemainingGuesses());
////            data.writeValue(toSave, gamedata.getGoodGuesses());
////            data.writeValue(toSave, gamedata.getBadGuesses());
//            data.writeValue(toSave, gamedata);

        } catch (IOException ioex){
            //appTemplate.getGUI().getErrorPop("Something Went Wrong When Saving :( ").show();
            AppMessageDialogSingleton dialogSingleton = AppMessageDialogSingleton.getSingleton();
            dialogSingleton.show(propertyManager.getPropertyValue(SAVE_ERROR_TITLE), propertyManager.getPropertyValue(SAVE_ERROR_MESSAGE));
        }
    }

    @Override
    public void handleLoadRequest() {
//        PropertyManager propertyManager = PropertyManager.getManager();
        try {
            if (workFile != null){

                URL workDirtURL = AppTemplate.class.getClassLoader().getResource(APP_WORKDIR_PATH.getParameter());
                if (workDirtURL == null)
                    throw new FileNotFoundException("Work Folder Not Found under resources");

                File initialDir = new File(workDirtURL.getFile());
                FileChooser fileChooser = new FileChooser();

                fileChooser.setInitialDirectory(initialDir);

                fileChooser.setTitle(propertyManager.getPropertyValue(LOAD_WORK_TITLE));
                fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(propertyManager.getPropertyValue(WORK_FILE_EXT_DESC), propertyManager.getPropertyValue(WORK_FILE_EXT)));

                File selectedFile = fileChooser.showOpenDialog(appTemplate.getGUI().getWindow());

                if (selectedFile != null) {
                    //handleNewRequest();
                    boolean load = handleNewRequestInLoad();
                    if (!load){
                        return;
                    }
                    start();
                    GameData newData;

                    try {
                        newData = (GameData) appTemplate.getFileComponent().loadData(gamedata, Paths.get(selectedFile.getAbsolutePath()));
//                    gamedata.setBadGuesses(newData.getBadGuesses());
//                    gamedata.setGoodGuesses(newData.getGoodGuesses());
//                    gamedata.setTargetWord(newData.getTargetWord());
//                    gamedata.setRemainingGuesses(newData.getRemainingGuesses());
                        // appTemplate.getWorkspaceComponent()
                        resetData(newData);

                    } catch (IOException e) {
                        AppMessageDialogSingleton dialogSingleton = AppMessageDialogSingleton.getSingleton();
                        dialogSingleton.show(propertyManager.getPropertyValue(LOAD_ERROR_TITLE), propertyManager.getPropertyValue(LOAD_ERROR_MESSAGE));
                    }
                }

            } else {
                URL workDirtURL = AppTemplate.class.getClassLoader().getResource(APP_WORKDIR_PATH.getParameter());
                if (workDirtURL == null)
                    throw new FileNotFoundException("Work Folder Not Found under resources");

                File initialDir = new File(workDirtURL.getFile());

                FileChooser fileChoose2 = new FileChooser();
                fileChoose2.setInitialDirectory(initialDir);
                fileChoose2.setTitle(propertyManager.getPropertyValue(LOAD_WORK_TITLE));
                fileChoose2.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(propertyManager.getPropertyValue(WORK_FILE_EXT_DESC), propertyManager.getPropertyValue(WORK_FILE_EXT)));

                File selectedFile = fileChoose2.showOpenDialog(appTemplate.getGUI().getWindow());

                if (selectedFile != null) {
                    //handleNewRequest();
                    boolean load = handleNewRequestInLoad();
                    if (!load){
                        return;
                    }
                    start();
                    GameData newData;

                    try {
                        newData = (GameData) appTemplate.getFileComponent().loadData(appTemplate.getDataComponent(), Paths.get(selectedFile.getAbsolutePath()));
//                    gamedata.setBadGuesses(newData.getBadGuesses());
//                    gamedata.setGoodGuesses(newData.getGoodGuesses());
//                    gamedata.setTargetWord(newData.getTargetWord());
//
                        resetData(newData);

                    } catch (IOException e) {
                        AppMessageDialogSingleton dialogSingleton = AppMessageDialogSingleton.getSingleton();
                        dialogSingleton.show(propertyManager.getPropertyValue(LOAD_ERROR_TITLE), propertyManager.getPropertyValue(LOAD_ERROR_MESSAGE));
                    }
                }
            }
        } catch (Exception ae){
            AppMessageDialogSingleton dialogSingleton = AppMessageDialogSingleton.getSingleton();
            dialogSingleton.show(propertyManager.getPropertyValue(LOAD_ERROR_TITLE), propertyManager.getPropertyValue(LOAD_ERROR_MESSAGE));
        }


    }
    
    @Override
    public void handleExitRequest() {
        try {
            boolean leave = true;
            int result;
            if (savable) {
                result = promptToSave();
                if (result == 1){
                    handleSaveRequest();
                }
                if (result == 0){
                    leave = false;
                }
            }
            if (leave)
                System.exit(0);
        } catch (IOException ioe) {
            /**
             * Platform.runLater(()->{
             *
             * };
             */
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(SAVE_ERROR_TITLE), props.getPropertyValue(SAVE_ERROR_MESSAGE));
        }
    }

    private void ensureActivatedWorkspace() {
        appTemplate.getWorkspaceComponent().activateWorkspace(appTemplate.getGUI().getAppPane());
    }
    
    private int  promptToSave() throws IOException {

        YesNoCancelDialogSingleton dialogSingleton = YesNoCancelDialogSingleton.getSingleton();
        dialogSingleton.show(propertyManager.getPropertyValue(SAVE_UNSAVED_WORK_TITLE), propertyManager.getPropertyValue(SAVE_UNSAVED_WORK_MESSAGE));
        String selection = dialogSingleton.getSelection();

        if (selection.equalsIgnoreCase("cancel")){
            dialogSingleton.close();
            return 0;
        }

        if (selection.equalsIgnoreCase("yes")) {
            handleSaveRequest();
            return 1;   //
        }  else {
            return 2;
        }



       // dummy placeholder
    }

    /**
     * A helper method to save work. It saves the work, marks the current work file as saved, notifies the user, and
     * updates the appropriate controls in the user interface
     *
     * @param target The file to which the work will be saved.
     * @throws IOException
     */
    private void save(File target) throws IOException {
        //appTemplate.getFileComponent().saveData(appTemplate.getDataComponent(), Paths.get(target.getAbsolutePath()));
//        GameDataFile file = new GameDataFile();
//        file.saveData(gamedata, Paths.get(target.getAbsolutePath()));

        try {
            appTemplate.getFileComponent().saveData(appTemplate.getDataComponent(), Paths.get(target.getAbsolutePath()));
        }catch (IOException e){
            AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
            PropertyManager           props  = PropertyManager.getManager();
            dialog.show(props.getPropertyValue(SAVE_ERROR_TITLE), props.getPropertyValue(SAVE_ERROR_MESSAGE));
        }

        workFile = target;
//        appTemplate.getGUI().getSaveButton().setDisable(true);

        AppMessageDialogSingleton dialog = AppMessageDialogSingleton.getSingleton();
        PropertyManager props = PropertyManager.getManager();
        dialog.show(props.getPropertyValue(SAVE_COMPLETED_TITLE), props.getPropertyValue(SAVE_COMPLETED_MESSAGE));
    }


    private void resetData(GameData data){
        Workspace gameWorkspace = (Workspace) appTemplate.getWorkspaceComponent();
        gameWorkspace.reinitialize();

        gamedata.setBadGuesses(data.getBadGuesses());
        gamedata.setGoodGuesses(data.getGoodGuesses());
        gamedata.setTargetWord(data.getTargetWord());
        gamedata.setRemainingGuesses(data.getRemainingGuesses());
        gamedata.setAppTemplate(appTemplate);
        gamedata.setHintButtonVisable(data.getHintButtonVisable());
        gamedata.setHintButtonDisabled(data.getHintButtonDisabled());
        appTemplate.setDataComponent(gamedata);
        remains.setText(Integer.toString(gamedata.getRemainingGuesses()));


        gameWorkspace.setHintButtonVisable(gamedata.getHintButtonVisable());
        gameWorkspace.setHintButtonDisabled(gamedata.getHintButtonDisabled());


        appTemplate.getGUI().updateWorkspaceToolbar(savable);   // savable is true.
        HBox remainingGuessBox = gameWorkspace.getRemainingGuessBox();
        HBox guessedLetters    = (HBox) gameWorkspace.getGameTextsPane().getChildren().get(1);
        HBox     goodAndBadGuessesLabel = (HBox) gameWorkspace.getGameTextsPane().getChildren().get(2);
        GridPane goodAndBadGuesses = (GridPane) gameWorkspace.getGameTextsPane().getChildren().get(3);
        goodAndBadGuessesLabel.getChildren().addAll(new Label(propertyManager.getPropertyValue(GOOD_AND_BAD_GUESSES_LABEL)));



        remainingGuessBox.getChildren().setAll(new Label("Remaining Guesses: "), remains);
        initWordGraphics(guessedLetters);
        initAlphabet(goodAndBadGuesses);


//            char guess = event.getCharacter().charAt(0);

        Set<Character> goodGusses = gamedata.getGoodGuesses();
        String guessed = goodGusses.toString();
        String guess = "";
        for (int i=0; i<guessed.length(); i++){
            if (Character.isLetter(guessed.charAt(i))){
                guess += guessed.charAt(i);
            }

        }


        for (int j=0; j<guess.length(); j++) {
            for (int i = 0; i < gamedata.getTargetWord().length(); i++) {
                if (gamedata.getTargetWord().charAt(i) == guess.charAt(j)) {
                    progress[i].setVisible(true);
                    discovered++;

                    char capitilized = Character.toUpperCase(guess.charAt(j));
                    int alphaOrder = (int) capitilized - 65;
                    squares[alphaOrder].setFill(Color.AQUA);
                    squares[alphaOrder].setBlendMode(BlendMode.SOFT_LIGHT);
                }
            }
        }


        Set<Character> badGusses = gamedata.getBadGuesses();
        String bads = badGusses.toString();
        String badguesses = "";
        for (int i=0; i< bads.length(); i++){
            if (Character.isLetter(bads.charAt(i))) {
                badguesses += bads.charAt(i);
            }
        }

        for (int j=0; j< badguesses.length(); j++){
            char capitilized = Character.toUpperCase(badguesses.charAt(j));
            int alphaOrder = (int) capitilized - 65;
            squares[alphaOrder].setFill(Color.RED);
            squares[alphaOrder].setBlendMode(BlendMode.SOFT_LIGHT);

            for (int b=0; b < (gamedata.TOTAL_NUMBER_OF_GUESSES_ALLOWED-gamedata.getRemainingGuesses()); b++) {
                gameWorkspace.getHangmancomponents().getChildren().get(b).setVisible(true);
            }
        }


        play();

        gameButton.setDisable(true);

    }
}
