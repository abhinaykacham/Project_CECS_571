package com.csulb;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


/**
 * This class is responsible for converting particular race people  who died because of COVID-19
 * to RDF
 */
public class CovidRaceDataset extends InputToRdfAbstractClass{
    int lowScale;
    int mediumScale;
    /**
     * Method takes
     * @param filePath as input parameter and tries to convert csv to machine understandable Strings
     */
    public CovidRaceDataset(String filePath) {
        super(filePath);
    }

    @Override
    void run() {
        readProperties();
        buildRdf();
        transferRdfToFile(Constants.COVID_RACE_OUTPUT_PATH);
    }

    /**
     * RDF is build using buildRDF
     */
    @Override
    void buildRdf() {
        model = ModelFactory.createDefaultModel();
        model.setNsPrefix("RaceConditionRdf",Constants.COVID_RACE_URL);
        //Creating resources for the dataset
        Resource deathsResource = model.createResource(Constants.COVID_RACE_URL+"#deaths", RDFS.Class);

        Resource regionResource = model.createResource(Constants.COVID_RACE_URL+"#region",RDFS.Class);
        Property name = model.createProperty(Constants.COVID_RACE_URL+"/name");
        name.addProperty(RDFS.range,XSD.normalizedString);
        name.addProperty(RDFS.domain,regionResource);

        Resource stateResource = model.createResource(Constants.COVID_RACE_URL+"#state",RDFS.Class);
        stateResource.addProperty(RDFS.subClassOf, regionResource);

        //Creating properties for the data set
        Property atScaleProperty  = model.createProperty(Constants.COVID_RACE_URL+"/atScale");
        atScaleProperty.addProperty(RDFS.domain,deathsResource);
        atScaleProperty.addProperty(RDFS.range, XSD.normalizedString);

        Property occurredAtProperty  = model.createProperty(Constants.COVID_RACE_URL+"/occurredAt");
        occurredAtProperty.addProperty(RDFS.domain,deathsResource);
        occurredAtProperty.addProperty(RDFS.range,stateResource);

        Property hasBelongsToRaceProperty = model.createProperty(Constants.COVID_RACE_URL+"/belongsToRace");
        hasBelongsToRaceProperty.addProperty(RDFS.domain,deathsResource);
        hasBelongsToRaceProperty.addProperty(RDFS.range,XSD.normalizedString);

        Property distributionProperty = model.createProperty(Constants.COVID_RACE_URL+"/covidDistribution");
        distributionProperty.addProperty(RDFS.domain,deathsResource);
        distributionProperty.addProperty(RDFS.range,XSD.decimal);

        String[] line;
        try {
            csvReader.skip(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            int j = 0;
            while ((line = csvReader.readNext()) != null) {
                //State,	Race,	COVID-19 Deaths
                String stateName="";
                String race="";
                String deathScale="";
                double distribution=0;
                for (int i=0; i< line.length;i++){
                    if(i==0){
                        stateName = line[i];
                    }else if(i==1){
                        race = line[i];
                    }else if(i==2){
                        double numberOfDeaths=line[i].equals("")?0:Double.parseDouble(line[i]);
                        if(numberOfDeaths<lowScale)
                            deathScale = "low";
                        else if(numberOfDeaths<mediumScale)
                            deathScale = "medium";
                        else
                            deathScale = "high";
                    } else if(i==3){
                        distribution = line[i].equals("")?0:Double.parseDouble(line[i]);
                    }
                }

                Resource entry = model.createResource("https://cdc.com"+"/#"+ j,deathsResource);
                entry.addProperty(atScaleProperty,deathScale);
                entry.addProperty(hasBelongsToRaceProperty,race);
                entry.addProperty(distributionProperty, String.valueOf(distribution));

                Resource stateValue = model.createResource("https://cdc.com"+"/#" + j,stateResource);
                stateValue.addProperty(name,stateName);
                entry.addProperty(occurredAtProperty,stateValue);
                j++;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        model.write(System.out, "RDF/XML");
    }

    /* Helper method for reading the properties from application.properities file*/
    public void readProperties(){
        try(InputStream in = new FileInputStream(Constants.APPLICATION_PROPERTIES_PATH)){
            Properties prop = new Properties();
            prop.load(in);
            lowScale = Integer.parseInt(prop.getProperty("race_low_scale"));
            mediumScale = Integer.parseInt(prop.getProperty("race_medium_scale"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
