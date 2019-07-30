package me.mikethesupertramp.csvdownloader2;

import com.opencsv.CSVReader;
import javafx.application.Platform;
import javafx.scene.Parent;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

public class DownloadManagerImpl implements DownloadManager {
    private String csvPath;
    private String outputDir;
    private List<String[]> entries;
    private Iterator<String[]> iterator;

    private int linkColumn = 1;
    private int nameColumn = 0;
    private boolean renamePolicy = true;

    private boolean started;
    private boolean canceled;
    private boolean paused;

    private int successCount;
    private int failCount;
    private int skippedCount;

    private Map<Link, String> failedLinks = new HashMap<>();

    private Runnable onFinishedHandler;

    public DownloadManagerImpl(String csvPath, String outputDir) {
        this.csvPath = csvPath;
        this.outputDir = outputDir;
    }

    //Heavy operation
    public boolean readFile(){
        try(CSVReader reader = new CSVReader(new FileReader(csvPath))) {
            entries = reader.readAll();
            reader.close();
            iterator = entries.iterator();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void startDownload(int threadCount) {
        if(!started) {
            started = true;

            Thread th = new Thread(() -> {
                boolean success = readFile();

                if(!success) {
                    onFailed();
                } else {
                    for (int i = 0; i < threadCount; i++) {
                        new DownloaderThread(this).start();
                    }
                }
            });
            th.setDaemon(true);
            th.setName("Reader");
            th.start();
        }
    }

    private void onFailed() {
        //todo implement
    }

    @Override
    public synchronized boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public synchronized Link getNext() {
        if(!iterator.hasNext()) {
            return new Link("", "");
        }

        String[] data = iterator.next();
        if(data.length <= linkColumn || data.length <= nameColumn) {
            entrySkipped();
            return new Link("", "");
        }

        String url = data[linkColumn];
        String fileName;

        if(renamePolicy) {
            if(url.contains(".")) {
                fileName = data[nameColumn] + url.substring(url.lastIndexOf('.'));
            } else {
                fileName = data[nameColumn];
            }
        } else {
            if(url.contains("/")) {
                fileName = url.substring(url.lastIndexOf("/"));
            } else {
                fileName = url;
            }
        }

        fileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");

        String targetFile = Paths.get(outputDir, fileName).toString();

        return new Link(url, targetFile);
    }

    @Override
    public void setPaused(boolean paused) {
        this.paused = paused;
    }

    @Override
    public boolean isPaused() {
        return paused;
    }

    @Override
    public void cancel() {
        canceled = true;
    }

    @Override
    public boolean isCanceled() {
        return canceled;
    }

    public int getSuccessCount() {
        return successCount;
    }

    public int getFailCount() {
        return failCount;
    }

    public int getSkippedCount() {
        return skippedCount;
    }

    public int getTotalCount() {
        return entries.size();
    }



    public int getLinkColumn() {
        return linkColumn;
    }

    public void setLinkColumn(int linkColumn) {
        this.linkColumn = linkColumn;
    }

    public int getNameColumn() {
        return nameColumn;
    }

    public void setNameColumn(int nameColumn) {
        this.nameColumn = nameColumn;
    }

    public boolean getRenamePolicy() {
        return renamePolicy;
    }

    public void setRenamePolicy(boolean renamePolicy) {
        this.renamePolicy = renamePolicy;
    }

    private void entrySkipped() {
        skippedCount++;
        entryProcessed();
    }

    @Override
    public void downloadSuccessful(Link link) {
        successCount++;
        entryProcessed();
    }

    @Override
    public void downloadFailed(Link link, String message) {
        failedLinks.put(link, message);
        failCount++;
        entryProcessed();
        System.out.println(message);
    }

    @Override
    public void setOnFinished(Runnable r) {
        this.onFinishedHandler = r;
    }

    private void entryProcessed() {
        if(skippedCount + failCount + successCount >= entries.size()) {
            finished();
        }
    }

    private void finished() {
        if(onFinishedHandler != null) {
            Platform.runLater(onFinishedHandler::run);
        }
    }

    public Map<Link, String> getFailedLinks() {
        return failedLinks;
    }
}
