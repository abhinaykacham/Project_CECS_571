package com.csulb;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * This class is responsible for converting Health condition of people who died because of COVID-19
 * to RDF
 */
public class CovidHealthDataset extends InputToRdfAbstractClass{
    int lowScale;
    int mediumScale;

    /**
     * Method takes
     * @param filePath as input parameter and tries to convert csv to machine understandable Strings
     */
    public CovidHealthDataset(String filePath) {
        super(filePath);
    }

    @Override
    void run() {
        readProperties();
        buildRdf();
        transferRdfToFile(Constants.HEALTH_CONDITION_OUTPUT_PATH);
    }

    /**
     * builds the model of the RDF
     */
    @Override
    void buildRdf() {
        Map<String,Resource> stateResourceTracker = new HashMap<>();
        Map<String,Property> healthConditionPropTracker = new HashMap<>();
        Map<String,Property> healthGroupPropTracker = new HashMap<>();
        model = ModelFactory.createDefaultModel();
        model.setNsPrefix("healthConditionRdf",Constants.HEALTH_CONDITION_URL);

        //Creating resources for the dataset
        Resource deathsResource = model.createResource(Constants.HEALTH_CONDITION_URL+"#deaths", RDFS.Class);

        Resource regionResource = model.createResource(Constants.HEALTH_CONDITION_URL+"#region",RDFS.Class);
        Property name = model.createProperty(Constants.HEALTH_CONDITION_URL+"/name");
        name.addProperty(RDFS.range,XSD.normalizedString);
        name.addProperty(RDFS.domain,regionResource);

        Resource stateResource = model.createResource(Constants.HEALTH_CONDITION_URL+"#state",RDFS.Class);
        stateResource.addProperty(RDFS.subClassOf, regionResource);

        //Creating and restricting atScale Property
        Property atScaleProperty  = model.createProperty(Constants.HEALTH_CONDITION_URL+"/atScale");
        atScaleProperty.addProperty(RDFS.domain,deathsResource);
        atScaleProperty.addProperty(RDFS.range, XSD.normalizedString);

        //Creating and restricting occuredAt Property
        Property occurredAtProperty  = model.createProperty(Constants.HEALTH_CONDITION_URL+"/occurredAt");
        occurredAtProperty.addProperty(RDFS.domain,deathsResource);
        occurredAtProperty.addProperty(RDFS.range,stateResource);

        //Creating, restricting and building IS-A relation for health condition Property
        Property hasHealthConditionGroupProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/hasHealthConditionGroup");
        hasHealthConditionGroupProperty.addProperty(RDFS.domain,deathsResource);
        hasHealthConditionGroupProperty.addProperty(RDFS.range,XSD.normalizedString);

        Property hasHealthConditionProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/hasHealthCondition");
        hasHealthConditionProperty.addProperty(RDFS.domain,deathsResource);
        hasHealthConditionProperty.addProperty(RDFS.range,XSD.normalizedString);

        String[] line;
        try {
            csvReader.skip(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            int j = 0;
            int stateCounter = 1;
            while ((line = csvReader.readNext()) != null) {
                //State,    Condition group,    Condition,  COVID-19 Deaths --order of columns in dataset
                String stateName="";
                String healthCondition="";
                String deathScale="";
                String healthConditionGroup="";
                for (int i=0; i< line.length;i++){
                    if(i==0){
                        stateName = line[i];
                    } else if(i==1){
                        healthConditionGroup = line[i];
                    } else if(i==2){
                        healthCondition = line[i];
                    }else if(i==3){
                        int numberOfDeaths=line[i].equals("")?0:Integer.parseInt(line[i]);
                        if(numberOfDeaths<lowScale)
                            deathScale = "low";
                        else if(numberOfDeaths<mediumScale)
                            deathScale = "medium";
                        else
                            deathScale = "high";
                    }
                }
                Resource entry = model.createResource("https://cdc.com"+"/#"+ j,deathsResource);
                entry.addProperty(atScaleProperty,deathScale);
                entry.addProperty(hasHealthConditionGroupProperty,healthConditionGroup);
                entry.addProperty(hasHealthConditionProperty,healthCondition);

                //Entering states data into RDF
                Resource stateValue = null;
                if(!stateResourceTracker.containsKey(stateName)){
                    stateValue = model.createResource("https://cdc.com"+"/#" + stateCounter,stateResource);
                    stateValue.addProperty(name,stateName);
                    stateResourceTracker.put(stateName,stateValue);
                    stateCounter++;
                }else{
                    stateValue = stateResourceTracker.get(stateName);
                }
                entry.addProperty(occurredAtProperty,stateValue);
                j++;
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void readProperties(){
        try(InputStream in = new FileInputStream(Constants.APPLICATION_PROPERTIES_PATH)){
            Properties prop = new Properties();
            prop.load(in);
            lowScale = Integer.parseInt(prop.getProperty("condition_low_scale"));
            mediumScale = Integer.parseInt(prop.getProperty("condition_medium_scale"));
        }catch (IOException e){
            e.printStackTrace();
        }
    }

}
