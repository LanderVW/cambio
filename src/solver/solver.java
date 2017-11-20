package solver;

import model.Car;
import model.Request;
import model.Zone;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class solver {

    private List<Request> requestList = new ArrayList<>();
    private List<Zone> zoneList = new ArrayList<>();
    private List<Car> carList = new ArrayList<>();
    private Integer days;
    private Integer[][] adjacentZone;
    private Integer[][] vehicleToZone;
    private Integer[][] requestToVehicle;

    public static final int DELAY_MS = 100;
    public static final int PROB_SIZE = 500;

    private static final Random random = new Random(0);

    public solver(List<Request> requestList, List<Zone> zoneList, List<Car> carList, Integer days, Integer[][] adjacentZone) {
        this.requestList = requestList;
        this.zoneList = zoneList;
        this.carList = carList;
        this.days = days;
        this.adjacentZone = adjacentZone;
    }

    public void solve() {
        int MAX_IDLE = 100000;
        int L = 1000;
        System.out.println(zoneList.size());
        vehicleToZone = new Integer[zoneList.size()][carList.size()];
        System.out.println(vehicleToZone.length);
        for (int i = 0; i < carList.size(); i++) {
            System.out.println(carList.get(i));
            vehicleToZone[0][0] = carList.get(i).getId();
        }

        for (Integer[] x : vehicleToZone) {
            for (Integer y : x) {
                if (y == null) {
                    y = 0;
                }
                System.out.print(y + " ");
            }
            System.out.println();
        }
    }

    //alle auto's toekennen aan zone's


}
