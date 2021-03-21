package com.csulb;

import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Resource;

import java.io.IOException;

public class CovidHealthDataset extends InputToRdfAbstractClass{

    /**
     * Method takes
     * @param filePath as input parameter and tries to convert csv to machine understandable Strings
     */
    public CovidHealthDataset(String filePath) {
        super(filePath);
    }

    @Override
    void run() {
        buildRdf();
        transferRdfToFile("/home/abhinay/Documents/CECS571/Project_CECS_571/src/main/resources/output files/health_condition.rdf");
    }

    @Override
    void buildRdf() {
        model = ModelFactory.createDefaultModel();
        model.setNsPrefix("healthConditionRdf",Constants.HEALTH_CONDITION_URL);
        Property stateProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"state");
        Property healthConditionProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"health_condition");
        Property deathScaleProperty = model.createProperty(Constants.HEALTH_CONDITION_URL+"death_scale");
        String[] line;
        try {
            csvReader.skip(1);
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            int j = 0;
            while ((line = csvReader.readNext()) != null) {
                //State,	Condition,	COVID-19 Deaths
                String stateName="";
                String healthCondition="";
                String deathScale="";
                for (int i=0; i< line.length;i++){
                    if(i==0){
                        stateName = line[i];
                    }else if(i==1){
                        healthCondition = line[i];
                    }else if(i==2){
                        deathScale = line[i];
                    }
                }
                Resource stateURI = model.createResource("https://data.edd.ca.gov" + j);
                stateURI.addProperty(stateProperty,stateName);
                stateURI.addProperty(healthConditionProperty,healthCondition);
                stateURI.addProperty(deathScaleProperty,deathScale);
            }
        }catch (IOException e){
            e.printStackTrace();
        }


    }
}
