package model;

import java.util.Arrays;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;

public class Solution {

    private int[][] requestToCar;
    private int[][] carToZone;
    private Integer penalty;

    public Solution(int[][] requestToCar, int[][] carToZone, Integer penalty) {
        this.requestToCar = requestToCar;
        this.carToZone = carToZone;
        this.penalty = penalty;
    }

    public Solution(Solution s) {
        this.requestToCar = s.getRequestToCar();
        this.carToZone = s.getCarToZone();
        this.penalty = s.getPenalty();
    }

    public int[][] getRequestToCar() {
        return requestToCar;
    }

    public void setRequestToCar(int[][] requestToCar) {
        this.requestToCar = requestToCar;
    }

    public int[][] getCarToZone() {
        return carToZone;
    }

    public void setCarToZone(int[][] carToZone) {
        this.carToZone = carToZone;
    }

    public Integer getPenalty() {
        return penalty;
    }

    public void setPenalty(Integer penalty) {
        this.penalty = penalty;
    }

    public void saveToCSV(int cars, int requests, int zones){
        PrintWriter pw = null;
        try{
            pw = new PrintWriter(new File("solution.csv"));
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        sb.append(penalty.toString());
        sb.append('\n');
        //vehicle to zone assignments
        sb.append("+Vehicle assignments\n");
        for (int i = 0; i < cars; i++) {
            for (int j = 0; j < zones; j++) {
                if(carToZone[i][j] == 1){
                    sb.append("car");
                    sb.append(i);
                    sb.append(';');
                    sb.append("z");
                    sb.append(j);
                    sb.append('\n');
                }
            }
        }
        //request to car assignments
        sb.append("+Assigned requests\n");
        for (int i = 0; i < requests; i++) {
            for (int j = 0; j < cars ; j++) {
                if(requestToCar[i][j] ==1){
                    sb.append("req");
                    sb.append(i);
                    sb.append(';');
                    sb.append("car");
                    sb.append(j);
                    sb.append('\n');
                }
            }
        }
        //unassigned requests
        sb.append("+Unassigned requests\n");
        boolean assigned;
        for (int i = 0; i < requests; i++) {
            assigned = false;
            for (int j = 0; j < cars; j++) {
                if(requestToCar[i][j] ==1)
                    assigned = true;
            }
            if(!assigned){
                sb.append("req");
                sb.append(i);
                sb.append('\n');
            }
        }

        pw.write(sb.toString());
        pw.close();
        System.out.println("done!");

    }

    @Override
    public String toString() {
        String rc = "\n";
        for (int[] x : requestToCar) {
            for (int y : x) {
                rc+= y + " ";
            }
            rc+= '\n';
        }
        String cz = "\n";
        for (int[] x : carToZone) {
            for (int y : x) {
                cz+= (y + " ");
            }
            cz+='\n';
        }

        return "Solution{" +
                "requestToCar=" + rc +
                ", carToZone=" + cz +
                ", penalty=" + penalty +
                '}';
    }
}
