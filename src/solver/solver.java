package solver;

import Generator.*;
import model.Car;
import model.Request;
import model.RequestComparator;
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
    private static final Random random = new Random(); //die 0 zorgt er voor dat random telkens zelfde is
    private int bestpenaltyBeforelocalsearchonrequest = 0;
    private Solution bestSolution = null;
    private Solution solution = null;
    private int restartCount = 0;
    private List<Integer> acceptedSolutions;
    private List<Integer> bounds;


    public solver(List<Request> requestList, List<Integer> zoneList, List<Car> carList, Integer days, int[][] adjacentZone) {
        this.requestList = requestList;
        this.zoneList = zoneList;
        this.carList = carList;
        this.days = days; //weet niet wat nut hiervan is
        this.adjacentZone = adjacentZone;
        this.acceptedSolutions = new ArrayList<>();
        this.bounds = new ArrayList<>();
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
        //sorteren van requestlist op basis van duur
        Collections.sort(requestList, new RequestComparator());
        Collections.reverse(requestList);
//        Collections.shuffle(requestList);

    }
    public List<Integer> getAcceptedSolutions() {
        return acceptedSolutions;
    }

    public void setAcceptedSolutions(List<Integer> acceptedSolutions) {
        this.acceptedSolutions = acceptedSolutions;
    }

    public List<Integer> getBounds() {
        return bounds;
    }

    public void setBounds(List<Integer> bounds) {
        this.bounds = bounds;
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
        solution.setRequestList(this.requestList);

        if (bestSolution == null) {
            bestSolution = new Solution(solution);
        }
        System.out.println("init met cost: " + bestSolution.getPenalty());
        System.out.println("init met mijne kost: " + calculateCost(bestSolution));


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
            int zone1 = getZoneForCar(carSelected, carToZone);
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
                acceptedSolutions.add(newPenalty);
                idle = 0;
                //System.out.println(idle);
                if (newPenalty < bestSolution.getPenalty()) {
                    solution.setRequestList(requestList);
                    bestSolution = new Solution(solution);

                }
            } else {
                //niet accepteren
                acceptedSolutions.add(oldPenalty);
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
                bound = solution.getPenalty();
                bounds.add(bound);
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

    public void solveRequestsToVehicles(Solution bsolution) {
        //generate al possible moves
        Generator generator = new ReplaceMoveGenerator();
        List<ReplaceMove> possibleMoveList = generator.generateRandom(bsolution, bsolution.getRequestList(), adjacentZone);
        int [][] tmpRequestToCar ;
        Solution tmpSolution;
        ReplaceMove replaceMove;
        int iteration = 0;
        int idle = 0;
        List<Request> overlappingList;
        int newPenalty = 0;
        Request tmprequest;
        boolean better = false;
        while(iteration < 1000){
            tmpSolution = new Solution(bsolution);
//            System.out.println(calculateCost(tmpSolution));
            tmpRequestToCar = copyRequestToCar(tmpSolution.getRequestToCar());
            iteration++;
            idle = 0;
//            better = false;
//            System.out.println("new");
            while(idle < 8 && !better) {
                idle++;
                 better = false;
                //pick random een move uit
                replaceMove = possibleMoveList.get(random.nextInt(possibleMoveList.size()));
//                while(tmpRequestToCar[replaceMove.getNewRequestID()][replaceMove.getNewCarID()] == 1){
//                    replaceMove = possibleMoveList.get(random.nextInt(possibleMoveList.size()));
//                }
                tmpRequestToCar[replaceMove.getNewRequestID()][replaceMove.getNewCarID()] = 1;
                //requestlistid komt overeen met request id?
                tmprequest = tmpSolution.getRequestList().get(replaceMove.getNewRequestID());
                if(replaceMove.isNeededZone()) {
                    tmprequest.setCurrentlyPenalty(0);
                }else{
                    tmprequest.setCurrentlyPenalty(tmprequest.getPenalty2());
                }
                overlappingList = getOverlappingRequests(replaceMove.getNewCarID(), replaceMove.getNewRequestID(), tmpSolution);
                if (overlappingList != null) {
                    //er is overlap en die moeten allemaal op nul gezet worden
                    for (Request r : overlappingList) {
//                        System.out.println("trow out");
                        tmpRequestToCar[r.getRequest_id()][replaceMove.getNewCarID()] = 0;
                        //penalty ook zetten
                        tmpSolution.getRequestList().get(r.getRequest_id()).setCurrentlyPenalty(r.getPenalty1());
                    }
                }
                //nieuwe penalty berekenen
                newPenalty = calculateCost(tmpSolution);
//                System.out.println("Cost: " + newPenalty);
                if (newPenalty < bsolution.getPenalty()) {
                    tmpSolution.setRequestToCar(tmpRequestToCar);
                    System.out.println("oude best solution" + bsolution);
                    tmpSolution.setPenalty(newPenalty);
                    better = true;
                    System.out.println("better: " + newPenalty);
                    bsolution = new Solution(tmpSolution);
                    bsolution.setRequestToCar(tmpRequestToCar);
                    System.out.println("nieuwe best solution" + tmpSolution);

                }
            }
            //terugzetten van bewegingen
        }
//        System.out.println(bsolution);
        System.out.println("done with local search");
        bestSolution = new Solution(bsolution);
    }

    //because otherwise we got equal refereenes
    private int[][] copyRequestToCar(int[][] requestToCar) {
        int [][] tmp = new int[requestToCar.length][requestToCar[0].length];
        for (int i = 0; i < requestToCar.length; i++) {
            for (int j = 0; j < requestToCar[0].length; j++) {
                tmp[i][j] = requestToCar[i][j];
            }
        }

        return tmp;
    }

    public Integer calculateCost(Solution s){
        int penalty = 0;
        for (Request r: s.getRequestList()) {
            penalty += r.getCurrentlyPenalty();
        }
        return penalty;
    }

    public void resetPenalties(){
        for (Request r: requestList) {
            r.setCurrentlyPenalty(0);
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
        resetPenalties();
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
                    if (!checkOverlappingRequests(vehicleID, requestList.get(i).getRequest_id())) {
                        requestToCar[requestList.get(i).getRequest_id()][vehicleID] = 1;
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
                    int carZone = getZoneForCar(vehicleID, carToZone);
                    int requestZone = requestList.get(i).getZone_id();
                    //checken indien voertuig zich in adjacent zone van de request bevindt

                    if (adjacentZone[carZone][requestZone] == 1) {
                        //zone waarin car zich bevindt is een aangrenzende zone van requestZone
                        if (!checkOverlappingRequests(vehicleID, requestList.get(i).getRequest_id())) {
                            requestToCar[requestList.get(i).getRequest_id()][vehicleID] = 1;
                            assigned = true;
                            penalty += requestList.get(i).getPenalty2();
                            requestList.get(i).setCurrentlyPenalty(requestList.get(i).getPenalty2());

                            break;
                        }
                    }

                }
            }
            if (!assigned) {
                requestList.get(i).setCurrentlyPenalty(requestList.get(i).getPenalty1());
                penalty += requestList.get(i).getPenalty1();
                solution.addUnassignedRequest(requestList.get(i));
            }
        }

        return penalty;

    }

    public void finish() {
//        todo bereken tijdscost om deze functie uit te voeren (alles moet stoppen voor maxtijd - finishtijd)
//        System.out.println("best solution: " + bestSolution);
        System.out.println("Cars to zone met cost: " + bestSolution.getPenalty());
//        System.out.println(bestSolution.getRequestList());
        bestpenaltyBeforelocalsearchonrequest = bestSolution.getPenalty();
        solveRequestsToVehicles(bestSolution);
//        System.out.println("the best was: " + bestpenaltyBeforelocalsearchonrequest);
        System.out.println("the best is after local search: " + bestSolution.getPenalty());
        bestSolution.saveToCSV(carList.size(), requestList.size(), zoneList.size());
//        System.out.println("na het optimal");
//        System.out.println(bestSolution);

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
    public Integer getZoneForCar(int car, int [][]carToZone) {
        for (int i = 0; i < zoneList.size(); i++) {
            if (carToZone[car][i] == 1) {
                return i;
            }
        }
        return null;
    }

    public solver() {

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

    private List<Request> getOverlappingRequests(int vehicleID, int requestID, Solution solution) {
        List<Request> tmpList = new ArrayList<>();
        for (int i = 0; i < requestList.size(); i++) {
            //car vervult nog een andere request; checken indien deze overlapt met de request die we nu willen toekennen
            if (solution.getRequestToCar()[i][vehicleID] == 1 && requestToRequest[i][requestID] == 1) {
                tmpList.add(requestList.get(i));
            }
        }
        if(tmpList.size() != 0) {
            return tmpList;
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
