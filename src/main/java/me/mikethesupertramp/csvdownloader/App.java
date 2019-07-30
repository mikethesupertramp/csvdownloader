package me.mikethesupertramp.csvdownloader;

import com.airhacks.afterburner.injection.Injector;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import me.mikethesupertramp.csvdownloader.presentation.MainScreenPresenter;
import me.mikethesupertramp.csvdownloader.presentation.MainScreenView;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class App extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        Map<Object, Object> injectSource = new HashMap<>();
        injectSource.put("mainStage", primaryStage);
        Injector.setConfigurationSource(injectSource::get);

        MainScreenView view = new MainScreenView();
        MainScreenPresenter presenter = (MainScreenPresenter) view.getPresenter();

        primaryStage.setScene(new Scene(view.getView()));
        primaryStage.show();

        primaryStage.setOnCloseRequest(e -> System.exit(0));
    }
}
