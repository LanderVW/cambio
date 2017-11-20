package io;

import model.Car;
import model.Request;
import model.Zone;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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

    String csvFile = "toy1.csv";
    BufferedReader br = null;
    String line = "";
    String cvsSplitBy = ";";
    Integer numberOfRequests, numberOfZones, numberOfVehicles;
    String[] tmp;
    private List<Request> requestList = new ArrayList<>();
    private List<Integer> zoneList = new ArrayList<>();
    private List<Car> carList = new ArrayList<>();
    private Integer days;
    private Integer[][] adjacentZone;
    private Integer[][] carToZone;
//    private Integer[][] requestToCar;

    public void readIn() {
        try {
            br = new BufferedReader(new FileReader(csvFile));
            line = br.readLine();
            tmp = line.split(":");
            //read all requests
            numberOfRequests = Integer.parseInt(tmp[1].replaceAll("\\s+", ""));
            int day_index, start_time, duration, penalty1, penalty2, zone_id;
            Integer request_id;
            String[] possible_vehicle_list;
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
                requestList.add(new Request(request_id, day_index, start_time, duration, penalty1, penalty2, zone_id, possible_vehicle_list));
            }
            //read all adjacent zones
            line = br.readLine();
            tmp = line.split(":");
            numberOfZones = Integer.parseInt(tmp[1].replaceAll("\\s+", ""));
            adjacentZone = new Integer[numberOfZones][numberOfZones];
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
            //to print out and see what happens:
//            for (Integer[] x : adjacentZone)
//            {
//                for (Integer y : x)
//                {
//                    if(y ==null){
//                        y = 0;
//                    }
//                    System.out.print(y + " ");
//                }
//                System.out.println();
//            }
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
        solve();
    }

    private Integer[][] requestToCar;
    private Integer[][] requestToRequest;

    public void solve() {
        int MAX_IDLE = 100000;
        int L = 1000;

        /* create initial solution ------------------------- */
        carToZone = new Integer[carList.size()][zoneList.size()];
        requestToCar = new Integer[requestList.size()][carList.size()];
        for (int i = 0; i < carList.size(); i++) {
            carToZone[i][0] = 1;
        }

        //iedere request heeft een integerid onze r-r matrix werkt dan ook met die id's
        requestToRequest = new Integer[requestList.size()][requestList.size()];
        for (int i = 0; i < requestList.size(); i++) {
            for (int j = 0; j < requestList.size(); j++) {
                if (requestList.get(i).Overlap(requestList.get(j))) {
                    requestToRequest[i][j] = 1;
                }

            }
        }



        //requests aan cars toekennen
        for (int i = 0; i < carList.size(); i++) {
            if (requestList.get(i).getZone_id() == getZoneForCar(carList.get(i).getId())) {
                requestToCar[requestList.get(i).getRequest_id()][carList.get(i).getId()] = 1;
            }
        }
        //check als goeie opl
        for (Integer[] x : requestToCar) {
            for (Integer y : x) {
                if (y == null) {
                    y = 0;
                }
                System.out.print(y + " ");
            }
            System.out.println();
        }

    }

    private Integer getZoneForCar(Integer car) {
        for (int i = 0; i < zoneList.size(); i++) {
            if (carToZone[car][i] == 1) {
                return i;
            }
        }
        return 0;
    }

    /* [meta] init ------------------------------------- */
    int idle = 0;
    int count = 0;

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

    public Integer[][] getAdjacentZone() {
        return adjacentZone;
    }
}
