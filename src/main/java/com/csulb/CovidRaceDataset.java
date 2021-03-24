package com.csulb;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CovidRaceDataset extends InputToRdfAbstractClass{

    /**
     * Method takes
     * @param filePath as input parameter and tries to convert csv to machine understandable Strings
     */
    public CovidRaceDataset(String filePath) {
        super(filePath);
    }

    @Override
    void run() {
        buildRdf();
        transferRdfToFile(Constants.COVID_RACE_OUTPUT_PATH);
    }

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
                for (int i=0; i< line.length;i++){
                    if(i==0){
                        stateName = line[i];
                    }else if(i==1){
                        race = line[i];
                    }else if(i==2){
                        int numberOfDeaths=line[i].equals("")?0:Integer.parseInt(line[i]);
                        if(numberOfDeaths<500)
                            deathScale = "low";
                        else if(numberOfDeaths<1000)
                            deathScale = "medium";
                        else
                            deathScale = "high";
                    }
                }
                Resource entry = model.createResource("https://cdc.com"+"/#"+ j,deathsResource);
                entry.addProperty(atScaleProperty,deathScale);
                entry.addProperty(hasBelongsToRaceProperty,race);

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
}
