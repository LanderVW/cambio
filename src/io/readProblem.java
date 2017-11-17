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
    Integer numberOfRequests;
    String[] tmp;

    public void readIn() {
        try {
            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            tmp = line.split(":");
            numberOfRequests = Integer.parseInt(tmp[1]);
            int request_id, day_index, start_time, duration, penalty1, penalty2;
            String zone_id;
            String [] possible_vehicle_list;
            for (int i = 0; i < numberOfRequests ; i++) {
                tmp = line.split(cvsSplitBy);
                request_id= Integer.parseInt(tmp[0]);
                zone_id = tmp[1];
            }


            while ((line = br.readLine()) != null) {
                String[] test = line.split(":");
                System.out.println(test[1]);
            }
        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
