package me.mikethesupertramp.csvdownloader;


import java.io.FileNotFoundException;


public class Test {
    public static void main(String[] args) throws FileNotFoundException {
        DownloaderPool pool = new DownloaderPool();
        pool.download(1, "cee-es-vee.csv", "temp");

        while (true) {
            Thread.yield();
        }
    }
}
