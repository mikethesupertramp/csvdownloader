package me.mikethesupertramp.csvdownloader2;

public class Link {
    private String url;
    private String targetFile;

    public Link(String url, String targetFile) {
        this.url = url;
        this.targetFile = targetFile;
    }

    public String getUrl() {
        return url;
    }

    public String getTargetFile() {
        return targetFile;
    }
}
