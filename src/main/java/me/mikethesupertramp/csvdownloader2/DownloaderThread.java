package me.mikethesupertramp.csvdownloader2;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;


public class DownloaderThread extends Thread {
    private static int id;
    private DownloadManager downloadManager;

    public DownloaderThread(DownloadManager downloadManager) {
        this.downloadManager = downloadManager;
        this.setDaemon(true);
        this.setName("Downloader " + id++);
    }

    @Override
    public void run() {
        while (downloadManager.hasNext() && !downloadManager.isCanceled()) {
            waitIfPaused();
            Link link = downloadManager.getNext();

            URL url;

            try {
                url = new URL(link.getUrl());
            } catch (MalformedURLException e) {
                downloadManager.downloadFailed(link, "Invalid URL");
                continue;
            }

            InputStream in;

            try {
                URLConnection connection = url.openConnection();
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                in = connection.getInputStream();
            } catch (IOException e) {
                downloadManager.downloadFailed(link, "Connection error");
                continue;
            }

            //Save file
            try (ReadableByteChannel rbc = Channels.newChannel(in);
                 FileOutputStream fos = new FileOutputStream(link.getTargetFile())) {
                 fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            } catch (IOException e) {
                downloadManager.downloadFailed(link, "Couldn't write file");
                continue;
            }

            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            downloadManager.downloadSuccessful(link);
        }
    }

    private void waitIfPaused() {
        while (downloadManager.isPaused()) {
            Thread.yield();
        }
    }
}
