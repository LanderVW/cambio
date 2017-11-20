import io.readProblem;
import solver.solver;
public class Main {
    public static void main(String [] args){
        //inlezen
        readProblem r = new readProblem();
        r.readIn();
        //oplossen
        solver solver = new solver(r.getRequestList(),r.getZoneList(), r.getCarList(), r.getDays(),r.getAdjacentZone() );
        solver.solve();
    }
}
