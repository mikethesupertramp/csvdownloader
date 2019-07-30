package me.mikethesupertramp.csvdownloader.presentation;

import com.opencsv.CSVWriter;
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
import me.mikethesupertramp.csvdownloader2.DownloadManagerImpl;
import me.mikethesupertramp.csvdownloader2.Link;

import javax.inject.Inject;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

public class MainScreenPresenter implements Initializable {
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

        int threads;
        try {
            threads = Integer.parseInt(tfNumberOfThreads.getText());
        } catch (NumberFormatException e) {
            showAlert("Please specify number of threads");
            return;
        }


        print("--- started download ---");
        /*DownloaderPool downloaderPool = new DownloaderPool();
        downloaderPool.download(threads, csvFile.getPath(), outDir.getPath());*/

        DownloadManagerImpl dm = new DownloadManagerImpl(csvFile.getPath(), outDir.getPath());
        dm.startDownload(threads);

        Timeline timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(new KeyFrame(Duration.seconds(1), e -> {
                print("progress: " + dm.getSuccessCount() + "/" + dm.getTotalCount() + " files");
        }));
        timeline.playFromStart();

        dm.setOnFinished(() -> {
            timeline.stop();
            print("--- Download finished ---");
            print("Successful: " + dm.getSuccessCount());
            print("Failed: " + dm.getFailCount());
            print("Skipped: " + dm.getSkippedCount());


            writeCSVExperimental(dm.getFailedLinks());
        });
    }

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

    private void writeCSVExperimental(Map<Link, String> fails) {
        try(CSVWriter writer = new CSVWriter(new FileWriter("./fails.csv"))) {
            fails.forEach((link, message) -> {
                writer.writeNext(new String[] {new File(link.getTargetFile()).getName(), link.getUrl(), message});
            });
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
