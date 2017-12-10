package solver;

import Generator.*;
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
    private static final Random random = new Random(1); //die 0 zorgt er voor dat random telkens zelfde is
    private int bestpenaltyBeforelocalsearchonrequest = 0;
    private Solution bestSolution = null;
    private Solution solution = null;
    private int restartCount = 0;
    private List<Integer> acceptedSolutions;
    private List<Integer> bounds;
    private int[][] bestRequestTocar;
    private int globalBestSolution = Integer.MAX_VALUE;

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


    public void reset() {
        requestToRequest = new int[requestList.size()][requestList.size()];
        for (int i = 0; i < requestList.size(); i++) {
            for (int j = 0; j < requestList.size(); j++) {
                if (requestList.get(i).overlap(requestList.get(j))) {
                    requestToRequest[i][j] = 1;
                }
            }
        }
        bestSolution = null;
        randomCarToZoneAssignment();
    }

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
//        Collections.sort(requestList, new RequestComparator());
//        Collections.reverse(requestList);
//        Collections.shuffle(requestList);
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
        bestRequestTocar = new int[requestList.size()][carList.size()];

        solution = new Solution(requestToCar, carToZone);
        int penalty = assignRequestsToVehicles(carToZone, requestToCar);
        solution.setPenalty(penalty);
        solution.setRequestList(this.requestList);

        if (bestSolution == null) {
            bestSolution = new Solution(solution);
            saveBest(carList.size(), requestList.size(), zoneList.size());
        }
        System.out.println("init met cost: " + bestSolution.getPenalty());
        for (int i = 0; i < requestToCar.length; i++) {
            this.bestRequestTocar[i] = requestToCar[i].clone();
        }
    }

    public void assignCarsToZones() {

        int MAX_IDLE = 100;
        int L = 5000;

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
                    for (int i = 0; i < requestToCar.length; i++) {
                        this.bestRequestTocar[i] = requestToCar[i].clone();
                    }
                    saveBest(carList.size(), requestList.size(), zoneList.size());
                    bestpenaltyBeforelocalsearchonrequest = bestSolution.getPenalty();
                    solveRequestsToCars(bestSolution);
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
                bestpenaltyBeforelocalsearchonrequest = bestSolution.getPenalty();
                solveRequestsToCars(bestSolution);
                while (restartCount < 0) {
                    randomCarToZoneAssignment();
                    restartCount++;
                    assignCarsToZones();
                }
                saveBest(carList.size(), requestList.size(), zoneList.size());
//                bestSolution.saveToCSV(carList.size(), requestList.size(), zoneList.size());
                break;
            }
        }
    }

    private void saveBest(int numberOfCars, int numberOfRequests, int numberOfZones) {
        if(bestSolution.getPenalty() < globalBestSolution){
            bestSolution.setRequestToCar(bestRequestTocar);
            bestSolution.saveToCSV(numberOfCars, numberOfRequests, numberOfZones);

            globalBestSolution = bestSolution.getPenalty();
        }

    }

    public void solveRequestsToCars(Solution bsolution) {
        //generate al possible moves
        Generator generator = new ReplaceMoveGenerator();
        List<ReplaceMove> possibleMoveList = generator.generateRandom(bsolution, bsolution.getRequestList(), adjacentZone);
        int[][] tmpRequestToCar = new int[this.bestRequestTocar.length][];
        ReplaceMove replaceMove;
        int iteration = 0;
        int idle = 0;
        List<Request> overlappingList;
        int newPenalty = 0;
        Request tmprequest;
        boolean better = false;
        System.out.println("kost net voor ls request to car: " + getCost(bestRequestTocar));
        while (iteration < 1000) {
            for (int i = 0; i < this.bestRequestTocar.length; i++) {
                tmpRequestToCar[i] = this.bestRequestTocar[i].clone();
            }
            iteration++;
            idle = 0;
            better = false;
            Integer oldcar = 0;
//            System.out.println("new");
            while (idle < 3 && !better) {
                idle++;
                //pick random een move uit
//                System.out.println("current penalty: " + getCost(tmpRequestToCar));
                replaceMove = possibleMoveList.get(random.nextInt(possibleMoveList.size()));
//                while(tmpRequestToCar[replaceMove.getNewRequestID()][replaceMove.getNewCarID()] == 1){
//                    replaceMove = possibleMoveList.get(random.nextInt(possibleMoveList.size()));
//                }

                for (int i = 0; i < carList.size(); i++) {
                    if (tmpRequestToCar[replaceMove.getNewRequestID()][i] == 1) {
                        tmpRequestToCar[replaceMove.getNewRequestID()][i] = 0;
                    }
                }
                tmpRequestToCar[replaceMove.getNewRequestID()][replaceMove.getNewCarID()] = 1;

                overlappingList = getOverlappingRequestList(replaceMove.getNewCarID(), replaceMove.getNewRequestID(), tmpRequestToCar);
                if (overlappingList != null) {
                    //er is overlap en die moeten allemaal op nul gezet worden
                    for (Request r : overlappingList) {
                        if (replaceMove.getNewRequestID() != r.getRequest_id()) {
                            tmpRequestToCar[r.getRequest_id()][replaceMove.getNewCarID()] = 0;
                        }
                    }
                }
                //nieuwe penalty berekenen
                newPenalty = getCost(tmpRequestToCar);

//                System.out.println("Cost: " + newPenalty);
                if (newPenalty < bestSolution.getPenalty()) {
//                    System.out.println(replaceMove);
//                    System.out.println("hoe was het vroeger: " + bestSolution);
                    better = true;
//                    System.out.println("better: " + newPenalty);
                    for (int i = 0; i < tmpRequestToCar.length; i++) {
                        this.bestRequestTocar[i] = tmpRequestToCar[i].clone();
                    }
//                    List<Request> testlijst = new ArrayList<>();
//                    boolean assigned = false;
//                    for (int request = 0; request < requestList.size(); request++) {
//                        assigned = false;
//                        for (int car = 0; car < carList.size(); car++) {
//                            if(this.bestRequestTocar[request][car] != 0){
//                                assigned = true;
//                                break;
//                            }
//                        }
//                        if(!assigned){
//                            testlijst.add(requestList.get(request));
//                        }
//                    }
//                    System.out.println(testlijst.size());
//                    if(getCost(bestRequestTocar) < bestSolution.getPenalty()){
//                        bestSolution = new Solution(this.bestRequestTocar, carToZone, newPenalty, testlijst);
//                    }


                    bestpenaltyBeforelocalsearchonrequest = newPenalty;

                }

                if (newPenalty > bestpenaltyBeforelocalsearchonrequest + 150) {
                    //discard changes becaut too bad
                    for (int i = 0; i < this.bestRequestTocar.length; i++) {
                        tmpRequestToCar[i] = this.bestRequestTocar[i].clone();
                    }
                }
            }
            //terugzetten van bewegingen
        }
//        System.out.println(bsolution);

        System.out.println("done with local search");
        bestSolution = new Solution(bsolution);
        bestSolution.setPenalty(bestpenaltyBeforelocalsearchonrequest);

        saveBest(carList.size(), requestList.size(), zoneList.size());

    }


    public void finish() {
//        todo bereken tijdscost om deze functie uit te voeren (alles moet stoppen voor maxtijd - finishtijd)
        System.out.println("best solution na shift car to zone: " + bestSolution);
        System.out.println("Cars to zone met cost: " + bestSolution.getPenalty());
        bestpenaltyBeforelocalsearchonrequest = bestSolution.getPenalty();
//        bestSolution.setRequestList(requestList);
//        solveRequestsToVehicles(bestSolution);
        solveRequestsToCars(bestSolution);
//        System.out.println("the best was: " + bestpenaltyBeforelocalsearchonrequest);
        System.out.println("the best is after local search: " + bestSolution.getPenalty());
        bestSolution.setRequestToCar(bestRequestTocar);
        saveBest(carList.size(), requestList.size(), zoneList.size());
//        bestSolution.saveToCSV(carList.size(), requestList.size(), zoneList.size());
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
    public Integer getZoneForCar(int car, int[][] carToZone) {
        for (int i = 0; i < zoneList.size(); i++) {
            if (carToZone[car][i] == 1) {
                return i;
            }
        }
        return null;
    }

    public solver() {
    }


    private Request findRequestForId(int s) {
        for (Request r : requestList
                ) {
            if (r.getRequest_id() == s) {
                return r;
            }
        }
        return null;
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
        if (tmpList.size() != 0) {
            return tmpList;
        }
        return null;
    }

    private List<Request> getOverlappingRequestList(Integer vehicleID, Integer requestID, int[][] tmpRequestToCar) {
        List<Request> tmpList = new ArrayList<>();
        for (int i = 0; i < requestList.size(); i++) {
            //car vervult nog een andere request; checken indien deze overlapt met de request die we nu willen toekennen
            if (tmpRequestToCar[i][vehicleID] == 1 && requestToRequest[i][requestID] == 1) {
                tmpList.add(findRequestForId(i));
            }
        }
        if (tmpList.size() != 0) {
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

    public Integer getCost(int[][] requesttocar) {
        int penalty = 0;
        Request tmp;
        boolean found = false;
        for (int req = 0; req < requesttocar.length; req++) {
            found = false;
            tmp = findRequestForId(req);
            for (int car = 0; car < requesttocar[0].length; car++) {
                if (requesttocar[req][car] == 1) {
                    found = true;
//                    System.out.println(getZoneForCar(car, bestSolution.getCarToZone()));
                    if (getZoneForCar(car, bestSolution.getCarToZone()) != tmp.getZone_id()) {
                        penalty += tmp.getPenalty2();
                    }
                }
            }
            if (found == false) {
                penalty += tmp.getPenalty1();
            }
        }
        return penalty;
    }

    //because otherwise we got equal refereenes
    private int[][] copyRequestToCar(int[][] requestToCar) {
        int[][] tmp = new int[requestToCar.length][requestToCar[0].length];
        for (int i = 0; i < requestToCar.length; i++) {
            for (int j = 0; j < requestToCar[0].length; j++) {
                tmp[i][j] = requestToCar[i][j];
            }
        }

        return tmp;
    }

    public Integer calculateCost(Solution s) {
        int penalty = 0;
        for (Request r : s.getRequestList()) {
            penalty += r.getCurrentlyPenalty();
        }
        return penalty;
    }

    public void resetPenalties() {
        for (Request r : requestList) {
            r.setCurrentlyPenalty(0);
        }
    }

    //omdat verkeerd
    public void solveRequestsToVehicles(Solution bsolution) {
        //generate al possible moves
        Generator generator = new ReplaceMoveGenerator();
        List<ReplaceMove> possibleMoveList = generator.generateRandom(bsolution, bsolution.getRequestList(), adjacentZone);
        int[][] tmpRequestToCar = new int[this.bestRequestTocar.length][];
        Solution tmpSolution;
        ReplaceMove replaceMove;
        int iteration = 0;
        int idle = 0;
        List<Request> overlappingList;
        int newPenalty = 0;
        Request tmprequest;
        boolean better = false;
        System.out.println("mijn cost met nieuwe methode: " + getCost(bestRequestTocar));
        while (iteration < 10000) {
            tmpSolution = new Solution(bestSolution);
//            System.out.println("begin: " + calculateCost(bestSolution));

//            System.out.println(calculateCost(tmpSolution));
            for (int i = 0; i < this.bestRequestTocar.length; i++) {
                tmpRequestToCar[i] = this.bestRequestTocar[i].clone();
            }

            iteration++;
            idle = 0;
            better = false;
//            System.out.println("new");
            while (idle < 8 && !better) {
                idle++;
//                System.out.println("hey");
                //pick random een move uit
                replaceMove = possibleMoveList.get(random.nextInt(possibleMoveList.size()));
//                while(tmpRequestToCar[replaceMove.getNewRequestID()][replaceMove.getNewCarID()] == 1){
//                    replaceMove = possibleMoveList.get(random.nextInt(possibleMoveList.size()));
//                }
                tmpRequestToCar[replaceMove.getNewRequestID()][replaceMove.getNewCarID()] = 1;
                //requestlistid komt overeen met request id?
                tmprequest = tmpSolution.getRequestList().get(replaceMove.getNewRequestID());
                if (replaceMove.isNeededZone()) {
                    tmprequest.setCurrentlyPenalty(0);
                } else {
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
                newPenalty = getCost(tmpRequestToCar);
//                System.out.println("Cost: " + newPenalty);
                if (newPenalty < bsolution.getPenalty()) {
//                    System.out.println(replaceMove);
//                    System.out.println("hoe was het vroeger: " + bestSolution);
                    tmpSolution.setRequestToCar(tmpRequestToCar);
                    tmpSolution.setPenalty(newPenalty);
                    better = true;
                    System.out.println("better: " + newPenalty);
                    bsolution = new Solution(tmpSolution);
                    bsolution.setRequestToCar(tmpRequestToCar);
                    System.out.println("cost met axels ding op de 1e manier: " + getCost(bsolution.getRequestToCar()));
                    bestSolution = new Solution(bsolution);
                    bestSolution.setRequestToCar(tmpRequestToCar);
                    tmpSolution.saveToCSV(carList.size(), requestList.size(), zoneList.size());

                }
            }
            //terugzetten van bewegingen
        }
//        System.out.println(bsolution);
        System.out.println("done with local search");
        System.out.println("cost met axels ding op de 1e manier: 2  " + getCost(bestSolution.getRequestToCar()));

    }

}
