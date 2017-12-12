package model;

public class Car {
    private Integer id;
    private String plateNumber;

    public Car(Integer id) {
        this.id = id;
    }

    public Car(String plateNumber) {
        this.id = 0;
        this.plateNumber = plateNumber;
    }

    @Override
    public String toString() {
        return "Car{" +
                "id='" + id + '\'' +
                '}';
    }

    public Integer getId() {
        return id;
    }

    public String getPlateNumber(){
        return this.plateNumber;
    }
}
