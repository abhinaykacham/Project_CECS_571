package com.csulb;

import java.io.File;
import java.nio.file.FileSystems;
import java.nio.file.Path;


public final class Constants {
    public static final String FS = File.separator;
    public static final String ROOT_PATH = System.getProperty("user.dir");
    public static final String INPUT_PATH = ROOT_PATH+FS+"src"+FS+"main"+FS+"resources"+FS+"input_files"+FS;
    public static final String OUTPUT_PATH = ROOT_PATH+FS+"src"+FS+"main"+FS+"resources"+FS+"output_files"+FS;

    public static final String HEALTH_CONDITION_URL = "https://data.cdc.gov/api/views/hk9y-quqm/";
    public static final String HEALTH_CONDITION_INPUT_PATH = INPUT_PATH+"Health_conditions_contributing_to_covid 19_deaths.csv";
    public static final String HEALTH_CONDITION_OUTPUT_PATH = OUTPUT_PATH+"health_condition.rdf";
    public static final String HEALTH_CONDITION_PROPERTIES_PATH = INPUT_PATH+"health_condition.properties";
    public static final String COVID_RACE_URL = "https://data.cdc.gov/api/views/hk9y-quqm/";
    public static final String COVID_RACE_INPUT_PATH = INPUT_PATH+"Covid-19_deaths_based_on_race.csv";
    public static final String COVID_RACE_OUTPUT_PATH = OUTPUT_PATH+"race_covid.rdf";

}
