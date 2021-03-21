package com.csulb;

import java.io.File;

public class App {
    public static void main(String[] args) {
        CovidHealthDataset covidHealthDataset = new CovidHealthDataset(Constants.HEALTH_CONDITION_FILE_PATH);
        covidHealthDataset.run();
    }
}
