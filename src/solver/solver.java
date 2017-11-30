package solver;

import model.Car;
import model.Request;
import model.Solution;

import java.util.*;

public class solver {
    private List<Request> requestList;
    private List<Integer> zoneList;
    private List<Car> carList;
    private Integer days;
    private int[][] adjacentZone; //aanliggende zone matrix
    private int[][] requestToCar; //uiteindelijk doel: een request aan een auto toekennen
    private int[][] requestToRequest; //Wordt in het begin op gesteld om de overlappende requests makkelijk op te vragen
    private int[][] carToZone; //ook een doel: auto's in een bepaalde zone plaatsen
    private static final Random random = new Random(17); //die 0 zorgt er voor dat random telkens zelfde is

    private Solution bestSolution = null;
    private Solution solution = null;
    private int restartCount = 0;

    public solver(List<Request> requestList, List<Integer> zoneList, List<Car> carList, Integer days, int[][] adjacentZone) {
        this.requestList = requestList;
        this.zoneList = zoneList;
        this.carList = carList;
        this.days = days; //weet niet wat nut hiervan is
        this.adjacentZone = adjacentZone;
    }

    /**
     * 1) initieer reqToReq voor overlappende requests
     * 2) initieer een oplossing(solution)
     * - car to zone
     * - via een eerste assignRequestsToVehicles() wordt reqToCar geintitieerd
     * <p>
     * best solution wordt opgeslagen
     * last solution wordt gelijkgesteld aan bestsolution
     */
    public void init() {
        //overlappende requests
        requestToRequest = new int[requestList.size()][requestList.size()];
        for (int i = 0; i < requestList.size(); i++) {
            for (int j = 0; j < requestList.size(); j++) {
                if (requestList.get(i).overlap(requestList.get(j))) {
                    requestToRequest[i][j] = 1;
                }
            }
        }
        //randomCarToZoneAssignment();
    }

    public void randomCarToZoneAssignment() {
         /* create initial solution ------------------------- */
        carToZone = new int[carList.size()][zoneList.size()];

        for (int i = 0; i < carList.size(); i++) {
            //iedere auto in een random zone zetten
            carToZone[i][random.nextInt(zoneList.size())] = 1;
        }

        //lege requestToCar aanmaken zodat die kan worden gebruikt in de assignRequestsToVehicles()
        requestToCar = new int[requestList.size()][carList.size()];
        solution = new Solution(carToZone, requestToCar);
        int penalty = assignRequestsToVehicles(carToZone, requestToCar);
        solution.setPenalty(penalty);

        if (bestSolution == null)
            bestSolution = new Solution(solution);
        System.out.print("init met cost: " + bestSolution.getPenalty());


        //System.out.println("print na init()");
        //printInfo();
    }


    public void assignCarsToZones() {
        int MAX_IDLE = 20000;
        int L = 1000;

        //initial solution
        randomCarToZoneAssignment();

        int idle = 0;
        int count = 0;
        int bound = solution.getPenalty();  //penalty van initiële oplossing

        //loop
        while (true) {

            int oldPenalty = solution.getPenalty();

            //clearen requestToCar array
            for (int i = 0; i < requestToCar.length; i++) {
                for (int j = 0; j < requestToCar[0].length; j++) {
                    requestToCar[i][j] = 0;
                }
            }

            //random move
            int carSelected = random.nextInt(carList.size());
            int zone1 = getZoneForCar(carSelected);
            int zone2 = random.nextInt(carToZone[0].length);

            //omwisselen cars
            int temp = carToZone[carSelected][zone1];
            carToZone[carSelected][zone1] = carToZone[carSelected][zone2];
            carToZone[carSelected][zone2] = temp;

            //accepteren nieuwe oplossing?
            int newPenalty = assignRequestsToVehicles(carToZone, requestToCar);
            solution.setPenalty(newPenalty);
            solution.setCarToZone(carToZone);
            solution.setRequestToCar(requestToCar);

            if (newPenalty < oldPenalty || newPenalty < bound) {
                if (newPenalty < bestSolution.getPenalty()) {
                    bestSolution = new Solution(solution);
                    System.out.println("het is verbeterd!!");
                    System.out.println(bestSolution);
                    idle = 0;
                }
            } else {
                //niet accepteren
                temp = carToZone[carSelected][zone1];
                carToZone[carSelected][zone1] = carToZone[carSelected][zone2];
                carToZone[carSelected][zone2] = temp;
                solution.setPenalty(oldPenalty);
                idle++;
            }

            //update
            count++;
            //System.out.println("idle " + idle + "  " + bestSolution.getPenalty() + "  bound " + bound + "  count " + count + " current " + newPenalty);

            if (count == L) {
                count = 0;
                bound = newPenalty;
            }
            if (idle >= MAX_IDLE) {
                //TODO local search binnen assignment van requests aan cars


                while (restartCount < 0) {
                    randomCarToZoneAssignment();
                    restartCount++;
                    assignCarsToZones();
                }
                bestSolution.saveToCSV(carList.size(), requestList.size(), zoneList.size());
                break;
            }

        }

    }

    public void solveRequestsToVehicles(Solution solution){
        //TODO mogelijk fout: aanpassen lijst terwijl doorlopen
        requestLoop:
        for(Request request : solution.getUnassignedRequests()){
            if(!request.isCarAvailable()){
                break;
            }
            if(request.isOverlap()){
                for(int vehicleIDUnassigned : request.getPossible_vehicle_list()){
                    Request currentlyAssignedRequest = getOverlappingRequest(vehicleIDUnassigned,request.getRequest_id(), solution);

                    //vehicle overlappende request komt overeen met currently assigned request
                    if(vehicleIDUnassigned == solution.getRequestToCar()[currentlyAssignedRequest.getRequest_id()][vehicleIDUnassigned]){
                        //checken indien mogelijk om currently assigned toe te wijzen aan adjacent zone
                        for (int j = 0; j < request.getPossible_vehicle_list().size(); j++) {
                            int vehicleIDCurrently = request.getPossible_vehicle_list().get(j);
                            int carZone = getZoneForCar(vehicleIDCurrently);
                            int requestZone = requestList.get(requestList.indexOf(request)).getZone_id();

                            //checken indien voertuig zich in adjacent zone van de request bevindt

                            if (adjacentZone[carZone][requestZone] == 1) {
                                if (!checkOverlappingRequests(vehicleIDCurrently, currentlyAssignedRequest.getRequest_id())) {
                                    //mogelijk om om te wisselen
                                    requestToCar[currentlyAssignedRequest.getRequest_id()][vehicleIDCurrently] = 1;
                                    requestToCar[request.getRequest_id()][vehicleIDUnassigned] = 1;
                                    solution.increasePenalty(currentlyAssignedRequest.getPenalty2());
                                    solution.decreasePenalty(request.getPenalty1());
                                    System.out.println(" gewisseld "+request.getRequest_id() + " met " + currentlyAssignedRequest.getRequest_id());
                                    break requestLoop;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    /**
     * bereken cost van huidige carToZone
     *
     * @return cost
     */
    public Integer assignRequestsToVehicles(int[][] carToZone, int[][] requestToCar) {
          /* [meta] init ------------------------------------- */
        int penalty = 0;
        for (int i = 0; i < requestList.size(); i++) {
            ArrayList<Integer> possibleVehicles = requestList.get(i).getPossible_vehicle_list();
            boolean assigned = false;
            boolean carAvailable = false;
            for (int j = 0; j < possibleVehicles.size(); j++) {
                int vehicleID = possibleVehicles.get(j);
                //vehicle is beschikbaar in gevraagde zone, checken indien mogelijk om toe te kennen
                //er wordt geen penalty aangerekend
                if (carToZone[vehicleID][requestList.get(i).getZone_id()] == 1) {
                    carAvailable = true;
                    if (!checkOverlappingRequests(vehicleID, i)) {
                        requestToCar[i][vehicleID] = 1;
                        assigned = true;
                        break;
                    } else {
                        requestList.get(i).setOverlap(true);
                    }
                }
            }
            requestList.get(i).setCarAvailable(carAvailable);

            //checken indien vehicle uit possiblevehiclelist vrij in adjacent zone
            if (!assigned) {
                for (int j = 0; j < possibleVehicles.size(); j++) {
                    int vehicleID = possibleVehicles.get(j);
                    int carZone = getZoneForCar(vehicleID);
                    int requestZone = requestList.get(i).getZone_id();
                    //checken indien voertuig zich in adjacent zone van de request bevindt

                    if (adjacentZone[carZone][requestZone] == 1) {
                        //zone waarin car zich bevindt is een aangrenzende zone van requestZone
                        if (!checkOverlappingRequests(vehicleID, i)) {
                            requestToCar[i][vehicleID] = 1;
                            assigned = true;
                            penalty += requestList.get(i).getPenalty2();
                            break;
                        }
                    }

                }
            }
            if (!assigned) {
                penalty += requestList.get(i).getPenalty1();
                solution.addUnassignedRequest(requestList.get(i));
            }
        }

        return penalty;

    }

    public void finish() {
//        todo bereken tijdscost om deze functie uit te voeren (alles moet stoppen voor maxtijd - finishtijd)
        System.out.println("best solution: " + bestSolution);
//        bestSolution.saveToCSV(carList.size(), requestList.size(), zoneList.size());
        System.out.println(bestSolution);
        System.out.print("finished met cost: " + bestSolution.getPenalty());
        solveRequestsToVehicles(bestSolution);
        System.out.println("na het optimal");
        System.out.println(bestSolution);
        System.out.print("finished met cost: " + bestSolution.getPenalty());

    }


    /**
     * hulpmethodes
     */


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

    /**
     * request the zone for a car
     *
     * @param car
     * @return ZoneId if car is in a zone, null if car is not in a zone
     */
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

    /**
     * overlap checken tussen request en de requests die al zijn toegewezen aan de auto
     *
     * @param vehicleID
     * @param requestID
     * @return true als er overlap is, false als er geen ovelap is
     */
    private boolean checkOverlappingRequests(int vehicleID, int requestID) {
        for (int i = 0; i < requestList.size(); i++) {
            //car vervult nog een andere request; checken indien deze overlapt met de request die we nu willen toekennen
            if (requestToCar[i][vehicleID] == 1 && requestToRequest[i][requestID] == 1) {
                return true;
            }
        }
        return false;
    }

    private Request getOverlappingRequest(int vehicleID, int requestID, Solution solution) {
        for (int i = 0; i < requestList.size(); i++) {
            //car vervult nog een andere request; checken indien deze overlapt met de request die we nu willen toekennen
            if (solution.getRequestToCar()[i][vehicleID] == 1 && requestToRequest[i][requestID] == 1) {
                return requestList.get(i);
            }
        }
        return null;
    }


    /**
     * printen van de informatie zoals die momenteel is
     * - alle requests
     * - cartozone matrix
     * -adjacentzone matrix
     */
    public void printInfo() {
//        printen info
        System.out.println("alle requests");
        for (Request r : requestList) System.out.println(r);

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
    }
}
