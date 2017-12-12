import io.readProblem;
import solver.solver;

public class Main {
    public static void main(String[] args) {
        long startUpTime = 0;
        long startTime = System.currentTimeMillis();
        //inlezen
        readProblem r = new readProblem();
        r.readIn();
        //oplossen
        solver solver = new solver(r.getRequestList(), r.getZoneList(), r.getCarList(), r.getDays(), r.getAdjacentZone());
        solver.init();
        long endTime = System.currentTimeMillis();
        startUpTime = endTime - startTime;
        long duration = startUpTime;
        System.out.println("start");
        while(duration < 10000){
            startTime = System.currentTimeMillis();
            solver.reset();
            solver.assignCarsToZones();
            endTime = System.currentTimeMillis();
            duration += (endTime - startTime);
            System.out.println("duration:" + duration);
        }
        solver.reset();
        solver.assignCarsToZones();

    }
}
