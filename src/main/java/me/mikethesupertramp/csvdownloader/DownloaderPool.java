package me.mikethesupertramp.csvdownloader;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;

public class DownloaderPool {
    private List<FileDownloader> downloaders = new ArrayList<>();
    private boolean finished;

    public void download(int threadCount, String input, String outDir) {
        new File(outDir).mkdirs();
        try {
            EntryProvider entryProvider = new EntryProvider(input);
            for(int i=0; i<threadCount; i++) {
                FileDownloader downloader = new FileDownloader(entryProvider, outDir);
                downloaders.add(downloader);
                downloader.start();
            }
            startMonitor();
        } catch (FileNotFoundException e) {
            App.console.print("Couldn't open csv file");
        }
    }

    private void startMonitor() {
        new Thread(() -> {
            while(!finished) {
                //Check if all threads are finished
                finished = true;
                for(FileDownloader downloader : downloaders) {
                    if(!downloader.isFinished()) {
                        finished = false;
                    }
                }

                if(finished) {
                    onFinished();
                }

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    public int getTotalFilesProcessed() {
        return downloaders.stream().mapToInt(FileDownloader::getProcessed).sum();
    }

    private void onFinished() {
        App.console.print("--- Finished downloading " + getTotalFilesProcessed() + " files ---");
    }

    public boolean isFinished() {
        return finished;
    }
}
