package me.mikethesupertramp.csvdownloader.presentation;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.Duration;
import me.mikethesupertramp.csvdownloader.Console;
import me.mikethesupertramp.csvdownloader.DownloaderPool;

import javax.inject.Inject;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class MainScreenPresenter implements Initializable, Console {
    public TextField tfNumberOfThreads;
    public TextField tfOutputDirectory;
    public TextField tfCSVFile;
    public TextArea console;

    @Inject
    Stage mainStage;

    public void browseCSV(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV FILE","*.csv"));
        fileChooser.setInitialDirectory(new File("./"));
        File file = fileChooser.showOpenDialog(mainStage);
        if(file != null) {
            tfCSVFile.setText(file.getPath());
        }
    }

    public void browseOutputDir(ActionEvent actionEvent) {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setInitialDirectory(new File("./"));
        File dir = directoryChooser.showDialog(mainStage);
        if(dir != null) {
            tfOutputDirectory.setText(dir.getPath());
        }
    }

    public void download(ActionEvent actionEvent) {
        File csvFile = new File(tfCSVFile.getText());
        if(!csvFile.exists()) {
            showAlert("Please choose a csv file");
            return;
        }
        File outDir = new File(tfOutputDirectory.getText());
        if(!outDir.exists()) {
            showAlert("please choose output directory");
            return;
        }

        int threads = 0;
        try {
            threads = Integer.parseInt(tfNumberOfThreads.getText());
        } catch (NumberFormatException e) {
            showAlert("Please specify number of threads");
            return;
        }


        print("--- started download ---");
        DownloaderPool downloaderPool = new DownloaderPool();
        downloaderPool.download(threads, csvFile.getPath(), outDir.getPath());

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
            if(downloaderPool.isFinished()) {
                timeline.stop();
            } else {
                print("downloaded " + downloaderPool.getTotalFilesProcessed() + " files");
            }

        }));
        timeline.playFromStart();
    }

    @Override
    public void print(String message) {
        Platform.runLater(() -> {
            console.setText(console.getText() + message + System.lineSeparator());
            console.setScrollTop(Integer.MAX_VALUE);
        });
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        tfNumberOfThreads.setText("10");
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("error");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.show();
    }
}
