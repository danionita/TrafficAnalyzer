/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nl.utwente.trafficanalyzer;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Dan
 */
public class GeoTagger {
    //CSV file header

    private static final HashMap<String,LatLong> sensors = new HashMap<>();
    public static void tagFilesInFolder(Path dir) {
        
        Path path = Paths.get("D:\\work\\NetBeans projects\\trafficanalyzer\\sampledata\\alles_untar\\sensors.csv");
        List list1 = readCsvFile(path.toFile());
        for (Object line : list1) {
                        CSVRecord record = (CSVRecord) line;
            sensors.put(record.get(0), new LatLong(Double.parseDouble(record.get(1)),Double.parseDouble(record.get(2))));
        }
        
        FileWriter writer = null;

        File folder = dir.toFile();
        File[] listOfFiles = folder.listFiles();
        Arrays.sort(listOfFiles);

        System.err.println("sorting files...");
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                List list2 = readCsvFile(listOfFiles[i]);
                try {
                    writer = new FileWriter(listOfFiles[i]);

                    for (Object line : list2) {
                        CSVRecord record = (CSVRecord) line;
                        for (int j = 0; j < 6; j++) {
                            writer.append(record.get(j));
                            writer.append(',');
                        }
                        //end line
                        writer.append('\n');
                    }

                    writer.flush();
                    writer.close();
                } catch (IOException ex) {
                    Logger.getLogger(GeoTagger.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        }

    }

    public static List readCsvFile(File fileName) {

        FileReader fileReader = null;
        CSVParser csvFileParser = null;

        //Create the CSVFormat object with the header mapping
        CSVFormat csvFileFormat = CSVFormat.DEFAULT;

        try {

            //initialize FileReader object
            fileReader = new FileReader(fileName);

            //initialize CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);

            //Get a list of CSV file records
            List csvRecords = csvFileParser.getRecords();

            return csvRecords;
        } catch (Exception e) {
            System.out.println("Error in CsvFileReader !!!");
            e.printStackTrace();

        } finally {
            try {
                fileReader.close();
                csvFileParser.close();
            } catch (IOException e) {
                System.out.println("Error while closing fileReader/csvFileParser !!!");
                e.printStackTrace();
            }
        }
        return null;

    }

    public static void main(String[] args) throws Exception {
        Path path = Paths.get("D:\\work\\NetBeans projects\\trafficanalyzer\\sampledata\\alles_untar\\csv");
        tagFilesInFolder(path);
    }
}
