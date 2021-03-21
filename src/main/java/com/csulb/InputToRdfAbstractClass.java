package com.csulb;


import com.opencsv.CSVReader;
import org.apache.jena.rdf.model.Model;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;

abstract public class InputToRdfAbstractClass {
    public Model model;
    CSVReader csvReader;
    /**
     * Method takes
     * @param filePath as input parameter and tries to convert csv to machine understandable Strings
     */
    public InputToRdfAbstractClass(String filePath){
        try {
            csvReader = new CSVReader(new FileReader(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    abstract void run();

    abstract void buildRdf();

    public void transferRdfToFile(String outputFilePath){
        try{
            model.write(new FileOutputStream(outputFilePath), "RDF/XML");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }
}
