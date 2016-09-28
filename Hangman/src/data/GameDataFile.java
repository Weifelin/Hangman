package data;

import com.fasterxml.jackson.databind.ObjectMapper;
import components.AppDataComponent;
import components.AppFileComponent;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author Ritwik Banerjee
 * @author Weifeng Lin
 */
//@JsonIgnoreProperties({"appTemplate"})


public class GameDataFile implements AppFileComponent {

    public static final String TARGET_WORD  = "TARGET_WORD";
    public static final String GOOD_GUESSES = "GOOD_GUESSES";
    public static final String BAD_GUESSES  = "BAD_GUESSES";

    @Override
    public void saveData(AppDataComponent data, Path to) {


        ObjectMapper toSave = new ObjectMapper();
        toSave.writerWithDefaultPrettyPrinter();
        try {

            toSave.writeValue(new File(String.valueOf(to)),data);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    @Override
    public AppDataComponent loadData(AppDataComponent data, Path from) throws IOException {
        ObjectMapper toLoad = new ObjectMapper();
        GameData data1= (GameData)data;
        AppDataComponent file = toLoad.readValue(new File(String.valueOf(from)), data1.getClass());
        return file;
        //toLoad.readValue


    }

    /** This method will be used if we need to export data into other formats. */
    @Override
    public void exportData(AppDataComponent data, Path filePath) throws IOException { }
}
