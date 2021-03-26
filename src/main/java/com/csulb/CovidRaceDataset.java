package com.csulb;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.OWL;
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

        //This resource holds the data of each record of the input file
        Resource COVIDFatalityResource = model.createResource(Constants.COVID_RACE_URL+"#covid_fatality", RDFS.Class);

        //Creating resources for the dataset
        Resource deathsResource = model.createResource(Constants.COVID_RACE_URL+"#deaths", RDFS.Class);

        Resource regionResource = model.createResource(Constants.COVID_RACE_URL+"#region",RDFS.Class);
        Property name = model.createProperty(Constants.COVID_RACE_URL+"/name");
        name.addProperty(RDFS.range,XSD.normalizedString);
        name.addProperty(RDFS.domain,regionResource);

        Resource stateResource = model.createResource(Constants.COVID_RACE_URL+"#state",RDFS.Class);
        stateResource.addProperty(RDFS.subClassOf, regionResource);

        Property countryProperty = model.createProperty(Constants.COVID_RACE_URL+"/belongsToCountry");
        countryProperty.addProperty(RDFS.domain,stateResource);
        countryProperty.addProperty(RDFS.range,XSD.token);

        //Creating properties for the data set
        Property atScaleProperty  = model.createProperty(Constants.COVID_RACE_URL+"/atScale");
        atScaleProperty.addProperty(RDFS.domain,deathsResource);
        atScaleProperty.addProperty(RDFS.range, XSD.token);

        Property occurredAtProperty  = model.createProperty(Constants.COVID_RACE_URL+"/occurredAt");
        occurredAtProperty.addProperty(RDFS.domain,COVIDFatalityResource);
        occurredAtProperty.addProperty(RDFS.range,stateResource);
        occurredAtProperty.addProperty(OWL.someValuesFrom,stateResource);

        Property withDeathsProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/withDeaths");
        withDeathsProperty.addProperty(RDFS.domain,COVIDFatalityResource);
        withDeathsProperty.addProperty(RDFS.range,deathsResource);

        Property havingCountProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/havingCount");
        havingCountProperty.addProperty(RDFS.domain,deathsResource);
        havingCountProperty.addProperty(RDFS.range,XSD.nonNegativeInteger);

        Property hasBelongsToRaceProperty = model.createProperty(Constants.COVID_RACE_URL+"/belongsToRace");
        hasBelongsToRaceProperty.addProperty(RDFS.domain,deathsResource);
        hasBelongsToRaceProperty.addProperty(RDFS.range,XSD.normalizedString);

        Property distributionProperty = model.createProperty(Constants.COVID_RACE_URL+"/covidDistribution");
        distributionProperty.addProperty(RDFS.domain,deathsResource);
        distributionProperty.addProperty(RDFS.range,XSD.decimal);

        Property affectedWithProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/affectedWith");
        affectedWithProperty.addProperty(RDFS.domain,stateResource);
        affectedWithProperty.addProperty(RDFS.range,COVIDFatalityResource);
        affectedWithProperty.addProperty(OWL.inverseOf,occurredAtProperty);

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
                double numberOfDeaths =0;
                for (int i=0; i< line.length;i++){
                    if(i==0){
                        stateName = line[i];
                    }else if(i==1){
                        race = line[i];
                    }else if(i==2){
                        numberOfDeaths=line[i].equals("")?0:Double.parseDouble(line[i]);
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

                //Defined properties for covid Fatality with count and scale properties
                Resource entry = model.createResource("https://cdc.com"+"/#"+ j,COVIDFatalityResource);
                Resource deathsStats = model.createResource("https://cdc.com"+"/#"+ j,deathsResource);
                deathsStats.addProperty(atScaleProperty,deathScale);
                deathsStats.addProperty(havingCountProperty,String.valueOf(numberOfDeaths));
                entry.addProperty(hasBelongsToRaceProperty,race);
                entry.addProperty(withDeathsProperty,deathsStats);
                entry.addProperty(distributionProperty, String.valueOf(distribution));

                //Created state instance with country Property
                Resource stateValue = model.createResource("https://cdc.com"+"/#" + j,stateResource);
                stateValue.addProperty(name,stateName);
                stateValue.addProperty(countryProperty,"USA");
                entry.addProperty(occurredAtProperty,stateValue);
                j++;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    /* Helper method for reading the properties from application.properties file*/
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
