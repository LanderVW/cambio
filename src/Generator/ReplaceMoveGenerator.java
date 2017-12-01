package Generator;

import com.sun.org.apache.regexp.internal.RE;
import model.Request;
import model.Solution;
import solver.solver;
import sun.nio.cs.Surrogate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ReplaceMoveGenerator implements Generator{

    private Solution solution;
    private int[][] adjacentZone; //aanliggende zone matrix
    List<ReplaceMove> moveList;

    public List<Integer> getAdjacentZone(int zone){
        int [] zonearray = adjacentZone[zone];
        List<Integer> adjacentzonelist = new ArrayList<>();
        int lengte = zonearray.length;
        for (int i = 0; i < zone ; i++) {
            if(zonearray[i] == 1){
                adjacentzonelist.add(zonearray[i]);
            }
        }
        return adjacentzonelist;
    }

    public Integer getZoneForCar(int car, int [][]carToZone) {
        for (int i = 0; i < carToZone.length; i++) {
            if (carToZone[car][i] == 1) {
                return i;
            }
        }
        return null;
    }

    @Override
    public List<ReplaceMove> generateRandom(Solution s, List<Request> requestList, int[][] adjacentZone ) {
        //je genereert alle mogelijke oplossingen: in de huidige cartozone eens kijken als je een bepaalde request kan toekennen aan een andere auto in die zone
        // of in de aanliggende zone
        solution = s;
        this.adjacentZone = adjacentZone;
        //hier bepaalde moves doen waar hij kan aan assignen
        moveList = new ArrayList<>();
        boolean self = false;
        for (Request request: requestList) {
            for(Integer possibleCarID : request.getPossible_vehicle_list()){
                //weet waar de auto nu staat
                //als de auto staat in adj zone of in de zone dat moet --> in de lijst
                int currentZoneForCar = getZoneForCar(possibleCarID, s.getCarToZone());
                if(currentZoneForCar == request.getZone_id()){
                    System.out.println("We kunnen request " + request.getRequest_id() + " ook zetten bij car: " + possibleCarID + " in de zone: " + currentZoneForCar);
                    moveList.add(new ReplaceMove(possibleCarID, request.getRequest_id()));
                    self = true;
                }
                for (int adjacentzone : getAdjacentZone(currentZoneForCar)) {
                    if (request.getZone_id() == adjacentzone) {
                        if(!self) {
                            System.out.println("Adjacent: We kunnen request " + request.getRequest_id() + " ook zetten bij car: " + possibleCarID + " in de zone: " + currentZoneForCar);

                            moveList.add(new ReplaceMove(possibleCarID, request.getRequest_id()));
                        }
                    }
                }
                self = false;
            }
        }
        return moveList;
    }

    @Override
    public String toString() {
        StringBuffer printout = new StringBuffer ();
        for (ReplaceMove r: moveList) {
            printout.append(r.toString());
        }
        return "ReplaceMoveGenerator{" +
                ", adjacentZone=" + Arrays.toString(adjacentZone) +
                printout+
                '}';
    }
}
