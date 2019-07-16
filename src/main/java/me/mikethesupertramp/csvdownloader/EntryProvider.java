package me.mikethesupertramp.csvdownloader;

import com.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class EntryProvider {
    private CSVReader reader;

    public EntryProvider(String path) throws FileNotFoundException {
        this.reader = new CSVReader(new FileReader(path));
    }

    public synchronized String[] getNextEntry() {
        try {
            String[] next = reader.readNext();
            if(next == null) {
                reader.close();
            }
            return next;
        } catch (IOException e) {
            e.printStackTrace();
            //Todo handle
        }

        return null;
    }

}
