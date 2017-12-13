import io.readProblem;
import solver.solver;

public class Main {
    public static void main(String[] args) {
        String inputfilepath = args[0];
        String solutionfilepath = args[1];
        String randomSeed = args[2];
        String timeLimit = args[3];
        String num_threads = args[4];
        System.out.println(inputfilepath + "\n"
                + solutionfilepath + "\n"
                + randomSeed + "\n"
                + timeLimit + "\n"
                + num_threads + "\n");
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
        while(duration < 1000){
            startTime = System.currentTimeMillis();
            solver.reset();
            solver.assignCarsToZones();
            //solver.solveRequestsToCars(solver.bestSolution);
            endTime = System.currentTimeMillis();
            duration += (endTime - startTime);
            System.out.println("duration:" + duration);
            System.out.println(solver.getAcceptedSolutions().size());
        }
        solver.reset();
        solver.assignCarsToZones();


//        solver.assignCarsToZones();
//        solver.assignCarsToZones();
        System.out.println(duration);
//        solver.finish();
//        System.out.println(duration);
    }
}
