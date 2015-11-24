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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

/**
 *
 * @author Dan
 */
public class Grouper {
    //CSV file header

    public static void groupFilesInFolder(Path dir) {
        FileWriter writer = null;
        try {

            File folder = dir.toFile();
            File[] listOfFiles = folder.listFiles();
            Arrays.sort(listOfFiles);

            HashMap<String, List<File>> hmap = new HashMap<>();
            System.err.println("sorting files...");
            for (int i = 0; i < listOfFiles.length; i++) {
                if (listOfFiles[i].isFile()) {
                    String streetname = listOfFiles[i].getName().split("_")[0];
                    if (hmap.keySet().contains(streetname)) {
                        System.out.println("\tAdded new day for: "+streetname);
                        hmap.get(streetname).add(listOfFiles[i]);
                    } else {
                        List<File> newList = new ArrayList<File>();
                        newList.add(listOfFiles[i]);
                        hmap.put(streetname, newList);                        
                        System.out.println("\tAdded new road : "+streetname);
                    }
                }
            }

            System.err.println("generating csvs...");
            for (String street : hmap.keySet()) {
                System.out.println("generating file: "+dir.toString() + "\\" + street+".csv");
                writer = new FileWriter(dir.toString() + "\\" + street+".csv");
                for (int i = 0; i < hmap.get(street).size(); i++) {
                    List list = readCsvFile(hmap.get(street).get(i));
                    for (Object line : list) {
                        CSVRecord record = (CSVRecord) line;
                        for (int j = 0; j < 5; j++) {
                            writer.append(record.get(j));
                            writer.append(',');
                        }
                        //end line
                        writer.append('\n');
                    }
                }                
            writer.flush();
            writer.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(Grouper.class.getName()).log(Level.SEVERE, null, ex);

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
        groupFilesInFolder(path);
    }
}
