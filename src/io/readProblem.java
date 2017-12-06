package io;

import model.Car;
import model.Request;
import model.Solution;
import model.Zone;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/*
type of csv:
    +Requests: num_requests
    request_id;zone_id;day_index;start_time;duration;possible_vehicle_id,...possible_vehicle_id;penalty1;penalty2
    ...
    request_id;zone_id;day_index;start_time;duration;possible_vehicle_id,...possible_vehicle_id;penalty1;penalty2
    +Zones: num_zones
    zone_id;adjacent_zone_id,...adjacent_zone_id
    ...
    zone_id;adjacent_zone_id,...adjacent_zone_id
    +Vehicles: num_vehicles
    vehicle_id
    ...
    vehicle_id
    +Days: num_days
 */

public class readProblem {
    //read in problem and make requests, zones, and zone tot zone matrix

//    String csvFile = "1_100_7_10_25.csv";
//    String csvFile = "2_210_3_40_25.csv";
//    String csvFile = "3_300_6_40_25.csv";
    String csvFile = "toy1.csv";
    BufferedReader br = null;
    String line = "";
    String cvsSplitBy = ";";
    Integer numberOfRequests, numberOfZones, numberOfVehicles;
    String[] tmp;
    private List<Request> requestList;
    private List<Integer> zoneList;
    private List<Car> carList;
    private Integer days;
    private int[][] adjacentZone;
    private static final Random random = new Random(0);

//    private Integer[][] requestToCar;

    public void readIn() {
        try {
            requestList = new ArrayList<>();
            zoneList = new ArrayList<>();
            carList = new ArrayList<>();
            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            tmp = line.split(":");
            //read all requests
            numberOfRequests = Integer.parseInt(tmp[1].replaceAll("\\s+", ""));
            int day_index, start_time, duration, penalty1, penalty2, zone_id;
            Integer request_id;
            String[] possible_vehicle_list;
            ArrayList possible_car_list;
            for (int i = 0; i < numberOfRequests; i++) {
                line = br.readLine();
                tmp = line.split(cvsSplitBy);
                request_id = Integer.parseInt(tmp[0].replaceAll("\\D+", ""));
                zone_id = Integer.parseInt(tmp[1].replaceAll("\\D+", ""));
                day_index = Integer.parseInt(tmp[2]);
                start_time = Integer.parseInt(tmp[3]);
                duration = Integer.parseInt(tmp[4]);
                penalty1 = Integer.parseInt(tmp[6]);
                penalty2 = Integer.parseInt(tmp[7]);
                possible_vehicle_list = tmp[5].split(",");
                possible_car_list = new ArrayList();
                for (Integer j = 0; j < possible_vehicle_list.length; j++) {
                    possible_car_list.add(Integer.parseInt(possible_vehicle_list[j].replaceAll("\\D+", "")));
                }
                requestList.add(new Request(request_id, day_index, start_time, duration, penalty1, penalty2, zone_id, possible_car_list));
            }
            //read all adjacent zones
            line = br.readLine();
            tmp = line.split(":");
            numberOfZones = Integer.parseInt(tmp[1].replaceAll("\\s+", ""));
            adjacentZone = new int[numberOfZones][numberOfZones];
            for (int i = 0; i < numberOfZones; i++) {
                line = br.readLine();
                tmp = line.split(cvsSplitBy);
                Integer headZone = Integer.parseInt(tmp[0].replaceAll("\\D+", ""));
                Integer zone;
                zoneList.add(headZone);
                tmp = tmp[1].split(",");
                for (int j = 0; j < tmp.length; j++) {
                    zone = Integer.parseInt(tmp[j].replaceAll("\\D+", ""));
                    adjacentZone[headZone][zone] = 1;
                }
            }

            //read all vehicles vehicle is just a number
            line = br.readLine();
            tmp = line.split(":");
            numberOfVehicles = Integer.parseInt(tmp[1].replaceAll("\\s+", ""));
            for (int i = 0; i < numberOfVehicles; i++) {
                line = br.readLine();
                tmp = line.split("");
                carList.add(new Car(Integer.parseInt(line.replaceAll("\\D+", ""))));
            }


            line = br.readLine();
            tmp = line.split(":");
            days = Integer.parseInt(tmp[1].replaceAll("\\s+", ""));

        } catch (FileNotFoundException e) {
            System.out.println(e);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        solve();
    }

    public Integer getNumberOfRequests() {
        return numberOfRequests;
    }

    public Integer getNumberOfZones() {
        return numberOfZones;
    }

    public Integer getNumberOfVehicles() {
        return numberOfVehicles;
    }

    public String[] getTmp() {
        return tmp;
    }

    public List<Request> getRequestList() {
        return requestList;
    }

    public List<Integer> getZoneList() {
        return zoneList;
    }

    public List<Car> getCarList() {
        return carList;
    }

    public Integer getDays() {
        return days;
    }

    public int[][] getAdjacentZone() {
        return adjacentZone;
    }
}
