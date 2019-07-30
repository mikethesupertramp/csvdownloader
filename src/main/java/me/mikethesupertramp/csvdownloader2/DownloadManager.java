package me.mikethesupertramp.csvdownloader2;

public interface DownloadManager {
    boolean hasNext();
    Link getNext();
    void setPaused(boolean paused);
    boolean isPaused();
    void cancel();
    boolean isCanceled();
    void downloadSuccessful(Link link);
    void downloadFailed(Link link, String message);
    void setOnFinished(Runnable r);
    void startDownload(int threadCount);
}
