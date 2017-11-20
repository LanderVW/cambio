package model;

public class Car {
    private Integer id;

    public Car(Integer id) {
        this.id = id;
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
}
