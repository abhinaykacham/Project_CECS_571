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

        //This resource holds the data of each record of the input file
        Resource COVIDFatalityResource = model.createResource(Constants.HEALTH_CONDITION_URL+"#covid_fatality", RDFS.Class);

        Resource regionResource = model.createResource(Constants.HEALTH_CONDITION_URL+"#region",RDFS.Class);

        //This resource holds the state data of the dataset
        Resource stateResource = model.createResource(Constants.HEALTH_CONDITION_URL+"#state",RDFS.Class);
        stateResource.addProperty(RDFS.subClassOf, regionResource);

        //This resource holds the properties scale and fatalities count
        Resource deathsResource = model.createResource(Constants.HEALTH_CONDITION_URL+"#deaths",RDFS.Class);

        //This resource holds the properties such as health condition and its group
        Resource diseaseResource = model.createResource(Constants.HEALTH_CONDITION_URL+"#disease", RDFS.Class);
        diseaseResource.addProperty(OWL.disjointWith,regionResource);

        Property name = model.createProperty(Constants.HEALTH_CONDITION_URL+"/name");
        name.addProperty(RDFS.domain,regionResource);
        name.addProperty(RDFS.range,XSD.token);

        Property countryProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/belongsToCountry");
        countryProperty.addProperty(RDFS.domain,stateResource);
        countryProperty.addProperty(RDFS.range,XSD.token);

        //Creating and restricting occurredAt Property
        Property occurredAtProperty  = model.createProperty(Constants.HEALTH_CONDITION_URL+"/occurredAt");
        occurredAtProperty.addProperty(RDFS.domain,COVIDFatalityResource);
        occurredAtProperty.addProperty(RDFS.range,stateResource);
        occurredAtProperty.addProperty(OWL.someValuesFrom,stateResource);

        Property withDeathsProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/withDeaths");
        withDeathsProperty.addProperty(RDFS.domain,COVIDFatalityResource);
        withDeathsProperty.addProperty(RDFS.range,deathsResource);

        //Creating and restricting atScale Property
        Property atScaleProperty  = model.createProperty(Constants.HEALTH_CONDITION_URL+"/atScale");
        atScaleProperty.addProperty(RDFS.domain,deathsResource);
        atScaleProperty.addProperty(RDFS.range, XSD.token);

        Property havingCountProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/havingCount");
        havingCountProperty.addProperty(RDFS.domain,deathsResource);
        havingCountProperty.addProperty(RDFS.range,XSD.nonNegativeInteger);

        //Creating and restricting health condition group Property
        Property hasHealthConditionGroupProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/hasHealthConditionGroup");
        hasHealthConditionGroupProperty.addProperty(RDFS.domain,diseaseResource);
        hasHealthConditionGroupProperty.addProperty(RDFS.range,XSD.token);

        //Creating and restricting health condition Property
        Property hasHealthConditionProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/hasHealthCondition");
        hasHealthConditionProperty.addProperty(RDFS.domain,diseaseResource);
        hasHealthConditionProperty.addProperty(RDFS.range,XSD.token);

        Property affectedByProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"/affectedBy");
        affectedByProperty.addProperty(RDFS.domain,COVIDFatalityResource);
        affectedByProperty.addProperty(RDFS.range,diseaseResource);

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
            int stateCounter = 1;
            while ((line = csvReader.readNext()) != null) {
                //State,    Condition group,    Condition,  COVID-19 Deaths --order of columns in dataset
                String stateName="";
                String healthCondition="";
                String deathScale="";
                String healthConditionGroup="";
                int numberOfDeaths=0;
                for (int i=0; i< line.length;i++){
                    if(i==0){
                        stateName = line[i];
                    } else if(i==1){
                        healthConditionGroup = line[i];
                    } else if(i==2){
                        healthCondition = line[i];
                    }else if(i==3){
                        numberOfDeaths=line[i].equals("")?0:Integer.parseInt(line[i]);
                        if(numberOfDeaths<lowScale)
                            deathScale = "low";
                        else if(numberOfDeaths<mediumScale)
                            deathScale = "medium";
                        else
                            deathScale = "high";
                    }
                }
                Resource deathsStats = model.createResource("https://cdc.com"+"/#"+ j,deathsResource);
                deathsStats.addProperty(atScaleProperty,deathScale);
                deathsStats.addProperty(havingCountProperty,String.valueOf(numberOfDeaths));

                Resource diseaseData = model.createResource("https://cdc.com"+"/#"+ j,diseaseResource);
                diseaseData.addProperty(hasHealthConditionGroupProperty,healthConditionGroup);
                diseaseData.addProperty(hasHealthConditionProperty,healthCondition);

                Resource entry = model.createResource("https://cdc.com"+"/#"+ j,COVIDFatalityResource);
                entry.addProperty(withDeathsProperty,deathsStats);
                entry.addProperty(affectedByProperty,diseaseData);

                //Entering states data into RDF
                Resource stateValue = null;
                if(!stateResourceTracker.containsKey(stateName)){
                    stateValue = model.createResource("https://cdc.com"+"/#"+j + stateCounter,stateResource);
                    stateValue.addProperty(name,stateName);
                    stateResourceTracker.put(stateName,stateValue);
                    stateCounter++;
                    stateValue.addProperty(countryProperty,"USA");
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

    /**
     *  Helper method for reading the properties from application.properties file
     */
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
