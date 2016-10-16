package data;

import apptemplate.AppTemplate;
import com.fasterxml.jackson.annotation.JsonIgnore;
import components.AppDataComponent;
import controller.GameError;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.stream.Stream;


/**
 * @author Ritwik Banerjee
 * @author  Weifeng Lin
 */
public class GameData implements AppDataComponent {

    public static final  int TOTAL_NUMBER_OF_GUESSES_ALLOWED = 10;
    private static final int TOTAL_NUMBER_OF_STORED_WORDS    = 330622;

    private String         targetWord;
    private Set<Character> goodGuesses;
    private Set<Character> badGuesses;

    private int            remainingGuesses;
    private boolean        hintButtonDisabled;
    private boolean        hintButtonVisable;
            ;

    @JsonIgnore
    public  AppTemplate    appTemplate;

    public GameData(AppTemplate appTemplate) {
        this.appTemplate = appTemplate;
        this.targetWord = setTargetWord();
        this.goodGuesses = new HashSet<>();
        this.badGuesses = new HashSet<>();
        this.remainingGuesses = TOTAL_NUMBER_OF_GUESSES_ALLOWED;
        this.hintButtonDisabled = true;
        this.hintButtonVisable = false;

    }

    public GameData(){

    }

//    public GameData(){
////        this.appTemplate = getAppTemplate();
//    }

    public void setAppTemplate(AppTemplate appTemplate){
        this.appTemplate = appTemplate;
    }

    public void setRemainingGuesses(int remainingGuesses){
        this.remainingGuesses = remainingGuesses;
    }


    private AppTemplate getAppTemplate(){
        return appTemplate;
    }

    @Override
    public void reset() {
        this.targetWord = null;
        this.goodGuesses = new HashSet<>();
        this.badGuesses = new HashSet<>();
        this.remainingGuesses = TOTAL_NUMBER_OF_GUESSES_ALLOWED;
        hintButtonVisable = false;
        hintButtonDisabled = true;
        appTemplate.getWorkspaceComponent().reloadWorkspace();

    }

    public String getTargetWord() {
        return targetWord;
    }

    /*
        check if the potential target word contains non-alpha letters. False if contains
     */
    private boolean isWord(String potentialTarget){
        for (int i=0; i<potentialTarget.length();i++){
            int asciiCode = (int) potentialTarget.charAt(i);
            if (asciiCode<65 || asciiCode>122){
                return false;
            }else if (asciiCode > 90 && asciiCode < 97){
                return false;
            }
        }

        return true;
    }

    private String setTargetWord() {
        URL wordsResource = getClass().getClassLoader().getResource("words/words.txt");
        assert wordsResource != null;

        int toSkip = new Random().nextInt(TOTAL_NUMBER_OF_STORED_WORDS);
        try (Stream<String> lines = Files.lines(Paths.get(wordsResource.toURI()))) {
            String potentialTarget = lines.skip(toSkip).findFirst().get();
            while (!isWord(potentialTarget)){
                toSkip = new Random().nextInt(TOTAL_NUMBER_OF_STORED_WORDS);
                potentialTarget = lines.skip(toSkip).findFirst().get();
            }
            return potentialTarget;
        } catch (IOException | URISyntaxException e) {
            e.printStackTrace();
            System.exit(1);
        }

        throw new GameError("Unable to load initial target word.");
    }

    public GameData setTargetWord(String targetWord) {
        this.targetWord = targetWord;
        return this;
    }

    public Set<Character> getGoodGuesses() {
        return goodGuesses;
    }

    @SuppressWarnings("unused")
    public GameData setGoodGuesses(Set<Character> goodGuesses) {
        this.goodGuesses = goodGuesses;
        return this;
    }

    public Set<Character> getBadGuesses() {
        return badGuesses;
    }

    @SuppressWarnings("unused")
    public GameData setBadGuesses(Set<Character> badGuesses) {
        this.badGuesses = badGuesses;
        return this;
    }

    public int getRemainingGuesses() {
        return remainingGuesses;
    }

    public void addGoodGuess(char c) {
        goodGuesses.add(c);
    }

    public void addBadGuess(char c) {
        if (!badGuesses.contains(c)) {
            badGuesses.add(c);
            remainingGuesses--;
        }
    }

    public void setHintButtonDisabled(boolean hintButtonDisabled){
        this.hintButtonDisabled = hintButtonDisabled;
    }

    public boolean getHintButtonDisabled(){
        return hintButtonDisabled;
    }
    public void setHintButtonVisable(boolean hintButtonVisable){
        this.hintButtonVisable = hintButtonVisable;
    }

    public boolean getHintButtonVisable(){
        return hintButtonVisable;
    }
}
