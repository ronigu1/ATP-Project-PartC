package View;

import algorithms.mazeGenerators.Maze;
import algorithms.mazeGenerators.Position;
import algorithms.search.AState;
import algorithms.search.MazeState;
import algorithms.search.Solution;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Optional;

public class MazeDisplayer extends Canvas {

    private int playerRowPos;
    private int playerColPos;
    private Solution solution;
    private Maze maze;
    private int[][] grid;
    private StringProperty imageFileNameWall = new SimpleStringProperty();
    private Image wallImage;
    private StringProperty imageFileSolPath = new SimpleStringProperty();
    private static StringProperty ChoosenPlayerAudio = new SimpleStringProperty();
    private Image solutionPathImage;
    private Image playerImage;
    private Image GoalImage;
    private Image playerGoalImage;
    private Image GoalGifImage;
    private static final Logger LOG = LogManager.getLogger();

    public static MediaPlayer mediaPlayer;
    public static Boolean winScene = false;

    public String getImageFileNameWall() {
        return imageFileNameWall.get();
    }

    public void setImageFileNameWall(String imageFileNameWall) {
        this.imageFileNameWall.set(imageFileNameWall);
    }

    public String getImageFileSolPath() {
        return imageFileSolPath.get();
    }

    public void setImageFileSolPath(String imageFileSolPath) {
        this.imageFileSolPath.set(imageFileSolPath);
    }


    public MazeDisplayer() {
        widthProperty().addListener(e -> redraw(MyViewController.solButton));
        heightProperty().addListener(e -> redraw(MyViewController.solButton));
    }


   public void setWall() {
       try {
           wallImage = new Image(new FileInputStream(imageFileNameWall.get()));
       }catch (FileNotFoundException e){
           System.out.println("There is no wall image file");}
    }

    public void setSolutionPathImage() {
       try {
           solutionPathImage = new Image(new FileInputStream(imageFileSolPath.get()));
       }catch (FileNotFoundException e){
           System.out.println("There is no solPath image file");}
    }

   public void setPlayer(String path){
       playerImage = new Image(Paths.get(path).toUri().toString());
   }
   public void setGoal(String path){
       GoalImage = new Image(Paths.get(path).toUri().toString());
   }
   public void setPlayerGoal(String path){
       playerGoalImage = new Image(Paths.get(path).toUri().toString());
   }
   public void setGoalGif(String path){
       GoalGifImage = new Image(Paths.get(path).toUri().toString());
   }
    public void setChoosenPlayerAudio(String choosenPlayeAudio) {
        this.ChoosenPlayerAudio.set(choosenPlayeAudio);
    }

    public static String getChoosenPlayerAudio() {
        return ChoosenPlayerAudio.get();
    }



    /** draws the maze scene including the player(this method should be invoked after each movement of the player) */
    public void redraw(boolean drawWithSol) {
        if (grid != null) {
            setWall();
            setSolutionPathImage();

            double canvasHeight = getHeight();
            double canvasWidth = getWidth();

            double cellHeight = canvasHeight / grid.length;
            double cellWidth = canvasWidth / grid[0].length;

            GraphicsContext graphicsContext2D = getGraphicsContext2D();
            clearMaze(graphicsContext2D);
            /*graphicsContext2D.clearRect(0, 0, getWidth(), getHeight());*/
            ArrayList<AState> path = null;
            if(drawWithSol==true)
                path = solution.getSolutionPath();
            for (int i = 0; i < grid.length; i++) {
                for (int j = 0; j < grid[i].length; j++) {
                    if (grid[i][j] == 1) {
                        graphicsContext2D.drawImage(wallImage, j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                    }
                    if(drawWithSol==true){
                        MazeState state = new MazeState(new Position(i,j),null);
                        if (path.contains(state)) {
                            if (state.getState().getRowIndex() == maze.getGoalPosition().getRowIndex() && state.getState().getColumnIndex() == maze.getGoalPosition().getColumnIndex())
                                continue;
                            else if (state.getState().getRowIndex() == playerRowPos && state.getState().getColumnIndex() == playerColPos)
                                continue;
                            else
                                graphicsContext2D.drawImage(solutionPathImage, j * cellWidth, i * cellHeight, cellWidth, cellHeight);
                        }
                    }
                }
            }
            Position goalPosition = maze.getGoalPosition();
            int goalPosRow = goalPosition.getRowIndex();
            int goalPosCol = goalPosition.getColumnIndex();
            if (playerRowPos != goalPosRow || playerColPos != goalPosCol) {
                graphicsContext2D.drawImage(GoalImage, goalPosCol * cellWidth, goalPosRow * cellHeight, cellWidth, cellHeight);
                /*draw the player's Image in player's current position */
                graphicsContext2D.drawImage(playerImage, playerColPos * cellWidth, playerRowPos * cellHeight, cellWidth, cellHeight);
            }
            if (playerRowPos == goalPosRow && playerColPos == goalPosCol) {
                MyViewController.solButton = false;
                graphicsContext2D.drawImage(playerGoalImage, goalPosCol * cellWidth, goalPosRow * cellHeight, cellWidth, cellHeight);
                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setContentText("You solved the maze!");
                alert.setHeaderText("Good job");
                alert.setTitle("Maze solved");
                Optional<ButtonType> result = alert.showAndWait();
                if (result.get() == ButtonType.OK || result.get()== ButtonType.CANCEL)
                    showStageForUserWinningTheGame();
            }
        }
    }

    private void showStageForUserWinningTheGame() {
        try{
            winScene = true;
            setAudio();
            Pane pane = new Pane();
            //pane.setBackground(new Background(new BackgroundImage(GoalGifImage, BackgroundRepeat.REPEAT,BackgroundRepeat.REPEAT, BackgroundPosition.CENTER,new BackgroundSize(100.0,100.0,true,true,false,true))));

            Stage newStage = new Stage();
            ImageView imageviewGoalGifImage = new ImageView(GoalGifImage);
            //add ImageView to Pane's children
            pane.getChildren().add(imageviewGoalGifImage);
            Scene scene = new Scene(pane);
            newStage.setScene(scene);
            newStage.show();
            newStage.setOnCloseRequest( event -> setAudio());//Sets the value of the property onCloseRequest
//            audioChooser(2);
/*
            newStage.setOnCloseRequest( event ->  mediaPlayer.stop() );//Sets the value of the property onCloseRequest
*/
        } catch (Exception e) {
            LOG.debug(e);
        }
    }

    public static void MuteORUnmuteMusic() {
        mediaPlayer.setMute(MyViewController.mute);
    }

    public static void setAudio(){
        /*stop the audio playing currently before switching */
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.pause();
        }
        String songPath = "src/main/resources/music/WhenYouWishUponAStar.mp3";
        if (winScene != false) {
            String songForWining = getChoosenPlayerAudio();
            songPath = songForWining;
            winScene = false;
        }
        String mediaUrl = new File(songPath).toURI().toString();
        Media player = new Media(mediaUrl);
        mediaPlayer = new MediaPlayer(player);
        mediaPlayer.play();
        mediaPlayer.setMute(MyViewController.mute);
    }

    public void setPlayerPositionAndRedraw(int startPosRow, int startPosCol){
        this.playerRowPos =  startPosRow;
        this.playerColPos = startPosCol;
        redraw(MyViewController.solButton);
    }

    public void setPlayerStartPositionWithoutRedraw(int startPosRow, int startPosCol) {
        this.playerRowPos =  startPosRow;
        this.playerColPos = startPosCol;
    }

    public int getPlayerPosRow() {
        return playerRowPos;
    }
    public int getPlayerPosCol() {
        return playerColPos;
    }

    public void setMazeGridAndRedraw(int[][] maze) {
        this.grid = maze;
        redraw(MyViewController.solButton);
    }

    public void setMazeGridWithoutRedraw(int[][] maze) {
        this.grid = maze;

    }

    public void setMaze(Maze maze) {
        this.maze = maze;
    }

    public void setSolution(Solution solution) {
        this.solution = solution;
    }


    public void clearMaze(GraphicsContext graphicsContext) {
        graphicsContext.clearRect(0, 0, getWidth(), getHeight());
    }
}

