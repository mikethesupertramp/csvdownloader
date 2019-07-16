package me.mikethesupertramp.csvdownloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class FileDownloader implements Runnable{
    private EntryProvider entryProvider;
    private String outDir;
    private boolean finished;
    private int processed;

    public FileDownloader(EntryProvider entryProvider, String outDir) {
        this.entryProvider = entryProvider;
        this.outDir = outDir;
    }

    public void start() {
        Thread thread = new Thread(this);
        thread.setDaemon(true);
        thread.start();
    }

    @Override
    public void run() {
        while(true) {
            String[] data = entryProvider.getNextEntry();

            //Finish if no mere entries available
            if(data == null) {
                finished = true;
                break;
            } else if(data.length < 2) {
                continue;
            }


            try {
                URL url = new URL(data[1]);
                InputStream in = url.openStream();
                //Add extension from url
                String targetFile = data[0] + data[1].substring(data[1].lastIndexOf("."));
                Files.copy(in, Paths.get(outDir, targetFile), StandardCopyOption.REPLACE_EXISTING);
                processed++;
            } catch (IOException e) {
                App.console.print("Couldn't process entry " + data[0]);
            }
        }
    }

    public int getProcessed() {
        return processed;
    }

    public boolean isFinished() {
        return finished;
    }
}
