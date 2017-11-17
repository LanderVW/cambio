package io;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class readProblem {
    //read in problem and make requests, zones, and zone tot zone matrix

    String csvFile = "toy1.csv";
    BufferedReader br = null;
    String line = "";
    String cvsSplitBy = ";";

    public void readIn() {
        try {
            br = new BufferedReader(new FileReader(csvFile));

        } catch (FileNotFoundException e) {
            System.out.println(e);
        }
    }

}
