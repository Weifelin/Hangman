package hangman;

import apptemplate.AppTemplate;
import components.AppComponentsBuilder;
import components.AppDataComponent;
import components.AppFileComponent;
import components.AppWorkspaceComponent;
import controller.FileController;
import controller.HangmanController;
import data.GameData;
import data.GameDataFile;
import gui.Workspace;

/**
 * @author Ritwik Banerjee
 * @author Weifeng Lin
 */
public class Hangman extends AppTemplate {



    public static void main(String[] args) {
        launch(args);
    }

    public String getFileControllerClass() {
        return "HangmanController";
    }

//    public HangmanController getFileController(){
//        return new HangmanController(this);
//    }



    @Override
    public AppComponentsBuilder makeAppBuilderHook() {
        return new AppComponentsBuilder() {
            @Override
            public AppDataComponent buildDataComponent() throws Exception {
                return new GameData(Hangman.this);
            }
    
            @Override
            public AppFileComponent buildFileComponent() throws Exception {
                return new GameDataFile();
            }
    
            @Override
            public AppWorkspaceComponent buildWorkspaceComponent() throws Exception {
                return new Workspace(Hangman.this);
            }


        };
    }
}
