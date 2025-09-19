package group.bloodcellanalyser;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AppController implements Initializable {
    public ImageProcessor imageProcessor = new ImageProcessor();

    private Image baseImage;
    public Stage fileStage;

    @FXML
    AnchorPane imageAnchor;
    @FXML
    ImageView imageView;
    @FXML
    Label redLabel,whiteLabel,noiseLabel;

    @FXML
    TextField minField,maxField;
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle){

    }


    public void openImage(ActionEvent event) throws IOException, ClassNotFoundException {
        FileChooser imgChooser = new FileChooser();
        imgChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg", "*.gif")
        );

        imgChooser.setTitle("Open Image");

        File selectedImg = imgChooser.showOpenDialog(fileStage);
        Image myImage = new Image("file:///"+selectedImg.getAbsolutePath());
        imageView.setImage(myImage);
        baseImage = myImage;
    }

    public void imageRedBlue(ActionEvent e){
        Image image = baseImage;
        Image colorProcessedImage = imageProcessor.imageRedBlue(image);
        imageView.setImage(colorProcessedImage);
    }

    public void revertToBase(ActionEvent e){
        imageView.setImage(baseImage);
    }

    public void countCells(){
        int[]data=imageProcessor.cellsCounter(baseImage);
        redLabel.setText("Red Count: "+ data[0]);
        whiteLabel.setText("White Count: "+ data[1]);

    }

    public void removeNoise(){
        if (!maxField.getText().isEmpty() && !minField.getText().isEmpty()) {
            int max = Integer.parseInt(maxField.getText());
            int min = Integer.parseInt(minField.getText());

            int[] data = imageProcessor.removeNoise(max, min);
            redLabel.setText("Red Count: "+ data[0]);
            whiteLabel.setText("White Count: "+ data[1]);

        } else {
            noiseLabel.setText("These fields must be filled with integers!");
        }
    }

    public void boxCells(){
        Image image = baseImage;
        double imageWidth = image.getWidth();
        double imageHeight = image.getHeight();

        //View width/height + scale needed to translate the boxes
        //as the imageview stretch fits the image
        double viewWidth = imageView.getBoundsInParent().getWidth();
        double viewHeight = imageView.getBoundsInParent().getHeight();
        double scaleX = viewWidth / imageWidth;
        double scaleY = viewHeight / imageHeight;

        //Creates canvas same size as image
        Canvas canvas = new Canvas(image.getWidth(), image.getHeight());
        //Puts canvas on top of imageView
        imageAnchor.getChildren().add(canvas);

        //Graphics context lets you draw on camvas
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setLineWidth(2);



        HashMap<Integer, int[]> boxMap = imageProcessor.getBoxInfo();
        for(int i = 1; i <= boxMap.size(); i++){
            int[] data = boxMap.get(i);
            if (data[0] == 1){
                gc.setStroke(Color.RED); // is red cell
            } else {
                gc.setStroke(Color.BLUE); // is white cell
            }
            double x = data[1] * scaleX;
            double y = data[2] * scaleY;
            double w = data[3] * scaleX;
            double h = data[4] * scaleY;

            gc.strokeRect(x, y, w, h);

        }

    }


    public void runFullProcess(ActionEvent e){
        countCells();
        if (!maxField.getText().isEmpty() && !minField.getText().isEmpty()) {
            removeNoise();
            boxCells();
        } else {
            noiseLabel.setText("These fields must be filled with integers!");
        }
    }




}