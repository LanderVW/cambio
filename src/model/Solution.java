package model;

public class Solution {

    private int[][] requestToCar;
    private int[][] carToZone;
    private Integer penalty;

    public Solution(int[][] requestToCar, int[][] carToZone, Integer penalty) {
        this.requestToCar = requestToCar;
        this.carToZone = carToZone;
        this.penalty = penalty;
    }

    public Solution(Solution s){
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
}
