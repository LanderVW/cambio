package solver;

import model.Car;
import model.Request;
import model.Solution;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class solver {
    private List<Request> requestList;
    private List<Integer> zoneList;
    private List<Car> carList;
    private Integer days;
    private int[][] adjacentZone; //aanliggende zone matrix
    private int[][] requestToCar; //uiteindelijk doel: een request aan een auto toekennen
    private int[][] requestToRequest; //Wordt in het begin op gesteld om de overlappende requests makkelijk op te vragen
    private int[][] carToZone; //ook een doel: auto's in een bepaalde zone plaatsen
    private static final Random random = new Random(0); //die 0 zorgt er voor dat random telkens zelfde is

    public solver(List<Request> requestList, List<Integer> zoneList, List<Car> carList, Integer days, int[][] adjacentZone) {
        this.requestList = requestList;
        this.zoneList = zoneList;
        this.carList = carList;
        this.days = days; //weet niet wat nut hiervan is
        this.adjacentZone = adjacentZone;
    }

    public void solve() {
        //overlappende requests berekenen
        //iedere request heeft een integerid onze r-r matrix werkt dan ook met die id's
        requestToRequest = new int[requestList.size()][requestList.size()];
        for (int i = 0; i < requestList.size(); i++) {
            for (int j = 0; j < requestList.size(); j++) {
                if (requestList.get(i).overlap(requestList.get(j))) {
                    requestToRequest[i][j] = 1;
                }
            }
        }

         /* create initial solution ------------------------- */
        carToZone = new int[carList.size()][zoneList.size()];
        requestToCar = new int[requestList.size()][carList.size()];

        for (int i = 0; i < carList.size(); i++) {
            //iedere auto in een random zone zetten
            carToZone[i][random.nextInt(zoneList.size())] = 1;
        }


        //printen info
        for(Request r : requestList) System.out.println(r);
        System.out.println("cartozone matrix");
        for (int[] x : carToZone) {
            for (int y : x) {
                System.out.print(y + " ");
            }
            System.out.println();
        }
        System.out.println("adjacentZone matrix");
        for (int[] x : adjacentZone) {
            for (int y : x) {
                System.out.print(y + " ");
            }
            System.out.println();
        }

         /* [meta] init ------------------------------------- */
        int idle = 0;
        int count = 0;
        int penalty = 0;

        //initiële oplossing requests toekennen aan cars
        for (int i = 0; i < requestList.size(); i++) {
            ArrayList<Integer> possibleVehicles = requestList.get(i).getPossible_vehicle_list();
            boolean assigned = false;
            for (int j = 0; j < possibleVehicles.size(); j++) {
                int vehicleID = possibleVehicles.get(j);

                //vehicle is beschikbaar in gevraagde zone, checken indien mogelijk om toe te kennen
                //er wordt geen penalty aangerekend
                if(carToZone[vehicleID][requestList.get(i).getZone_id()] == 1){
                    if(!checkOverlappingRequests(vehicleID, i)){
                        requestToCar[i][vehicleID] =1;
                        assigned = true;
                        System.out.println("request "+i+"toegekend aan vehicleid "+vehicleID);
                        break;
                    }
                }
            }

            //checken indien vehicle uit possiblevehiclelist vrij in adjacent zone
            if(!assigned){
                for (int j = 0; j < possibleVehicles.size(); j++) {
                    int vehicleID = possibleVehicles.get(j);
                    int carZone = getZoneForCar(vehicleID);
                    int requestZone = requestList.get(i).getZone_id();
                    //checken indien voertuig zich in adjacent zone van de request bevindt

                     if(adjacentZone[carZone][requestZone] == 1){
                         //zone waarin car zich bevindt is een aangrenzende zone van requestZone
                         if(!checkOverlappingRequests(vehicleID, i)){
                             requestToCar[i][vehicleID] =1;
                             assigned = true;
                             System.out.println("request "+i+"toegekend aan vehicleid "+vehicleID+" adjacent zone");
                             penalty += 20;
                             break;
                         }
                     }

                }
            }
        }


        penalty += 100 * (requestList.size() - calculateRequest(requestToCar));
        System.out.println("initial penalty"+penalty);
        int currentPenalty = penalty;

        Solution currentSolution = new Solution(requestToCar, carToZone, penalty);
        Solution bestSolution = new Solution(currentSolution);
        /* loop -------------------------------------------- */
        for (int l = 0; l < 999; l++) {


            penalty = 0;

            //clearen
            carToZone = new int[carList.size()][zoneList.size()];
            requestToCar = new int[requestList.size()][carList.size()];

            //nieuwe random toekenning
            for (int i = 0; i < carList.size(); i++) {
                //iedere auto in een random zone zetten
                carToZone[i][random.nextInt(zoneList.size())] = 1;
            }

            for (int i = 0; i < requestList.size(); i++) {
                ArrayList<Integer> possibleVehicles = requestList.get(i).getPossible_vehicle_list();
                boolean assigned = false;
                for (int j = 0; j < possibleVehicles.size(); j++) {
                    int vehicleID = possibleVehicles.get(j);

                    //vehicle is beschikbaar in gevraagde zone, checken indien mogelijk om toe te kennen
                    //er wordt geen penalty aangerekend
                    if(carToZone[vehicleID][requestList.get(i).getZone_id()] == 1){
                        if(!checkOverlappingRequests(vehicleID, i)){
                            requestToCar[i][vehicleID] =1;
                            assigned = true;
                            //System.out.println("request "+i+"toegekend aan vehicleid "+vehicleID);
                            break;
                        }
                    }
                }

                //checken indien vehicle uit possiblevehiclelist vrij in adjacent zone
                if(!assigned){
                    for (int j = 0; j < possibleVehicles.size(); j++) {
                        int vehicleID = possibleVehicles.get(j);
                        int carZone = getZoneForCar(vehicleID);
                        int requestZone = requestList.get(i).getZone_id();
                        //checken indien voertuig zich in adjacent zone van de request bevindt

                        if(adjacentZone[carZone][requestZone] == 1){
                            //zone waarin car zich bevindt is een aangrenzende zone van requestZone
                            if(!checkOverlappingRequests(vehicleID, i)){
                                requestToCar[i][vehicleID] =1;
                                assigned = true;
                                //System.out.println("request "+i+"toegekend aan vehicleid "+vehicleID+" adjacent zone");
                                penalty += 20;
                                break;
                            }
                        }

                    }
                }
            }
            penalty += 100 * (requestList.size() - calculateRequest(requestToCar));
            System.out.println("new penalty "+penalty);
            if(penalty < currentPenalty) {
                currentPenalty = penalty;
                bestSolution = new Solution(requestToCar, carToZone, penalty);
            }

        }
        System.out.println("best solution: "+bestSolution);

        bestSolution.saveToCSV(carList.size(),requestList.size(), zoneList.size());
        /*while (true) {
            Integer oldPenalty = currentSolution.getPenalty();
            //probeer voor alle requests eens toe te kennen aan een andere auto
            for (int i = 0; i < requestList.size(); i++) {
                if (!assigned(requestToCar[i])) {
                    //als de request nog niet toegekend is --> probeer hem toe te kennen aan een andere, of een buur...
                    ArrayList adjacentZones = searchAdjacentZone(adjacentZone[requestList.get(i).getZone_id()], i);
                    //neem random een adjacent zone
                    int zone = random.nextInt(adjacentZones.size());
                    //kijk als er een auto staat en probeer die auto als mogelijk toe te kennen aan die request
                    if (getCarforZone(zone) != null) {
                        //er staat een auto in die zone met dit id
                        Integer car = getCarforZone(zone);
                        //als deze auto mag toegekend worden
                        if(requestList.get(i).getPossible_vehicle_list().contains(car)) {
                            requestToCar[i][car] = 1;
                            //TODO hoe die penalty's bijhouden??
                            //snel 2d array bekijken
                            System.out.println("new solution!");
                            for (int[] x : requestToCar) {
                                for (int y : x) {
                                    System.out.print(y + " ");
                                }
                                System.out.println();
                            }
                        }

                    }
                }
                //todo check solution..?
                //todo change cars to zones
            }
        }*/

    }

    //hulpmethodes
    private ArrayList searchAdjacentZone(int[] integers, int self) {
        ArrayList lijst = new ArrayList();
        for (int i = 0; i < integers.length; i++) {
            if (integers[i] == 1 && i != self) {
                lijst.add(i);
            }
        }
        return lijst;
    }

    private boolean assigned(int[] integers) {
        for (int i = 0; i < integers.length; i++) {
            if (integers[i] == 1)
                return true;
        }
        return false;
    }

    private Integer calculateRequest(int[][] requestToCar) {
        int total = 0;
        for (int[] x : requestToCar) {
            for (Integer y : x) {
                if (y == null) {
                    y = 0;
                } else {
                    if (y == 1) {
                        total += 1;
                    }
                }
            }
        }
        return total;
    }

    private Integer getZoneForCar(int car) {
        for (int i = 0; i < zoneList.size(); i++) {
            if (carToZone[car][i] == 1) {
                return i;
            }
        }
        return null;
    }

    private Integer getCarforZone(int zone) {
        for (int i = 0; i < carList.size(); i++) {
            if (carToZone[i][zone] == 1) {
                return i;
            }
        }
        return null;
    }

    private boolean checkOverlappingRequests(int vehicleID, int requestID){
        for (int i = 0; i < requestList.size(); i++) {
            //car vervult nog een andere request; checken indien deze overlapt met de request die we nu willen toekennen
            if(requestToCar[i][vehicleID] == 1 && requestToRequest[i][requestID] == 1) {
                    return true;
            }

        };

        return false;
    }




}