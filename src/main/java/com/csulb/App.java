package com.csulb;

public class App {
    public static void main(String[] args) {
   //     CovidHealthDataset covidHealthDataset = new CovidHealthDataset(Constants.HEALTH_CONDITION_INPUT_PATH);
     //   covidHealthDataset.run();

        CovidRaceDataset covidRaceDataset = new CovidRaceDataset(Constants.COVID_RACE_INPUT_PATH);
        covidRaceDataset.run();
    }
}