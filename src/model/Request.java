package model;

import java.util.ArrayList;

public class Request {
    private int day_index, start_time, duration, penalty1, penalty2, zone_id, request_id;
    private ArrayList possible_vehicle_list;
    private Integer currentPenalty;

    public Request(Integer request_id, int day_index, int start_time, int duration, int penalty1, int penalty2, int zone_id, ArrayList possible_vehicle_list) {
        this.request_id = request_id;
        this.day_index = day_index;
        this.start_time = start_time;
        this.duration = duration;
        this.penalty1 = penalty1;
        this.penalty2 = penalty2;
        this.zone_id = zone_id;
        this.possible_vehicle_list = possible_vehicle_list;
    }

    @Override
    public String toString() {
        return "Request{" +
                "day_index=" + day_index +
                ", start_time=" + start_time +
                ", duration=" + duration +
                ", penalty1=" + penalty1 +
                ", penalty2=" + penalty2 +
                ", zone_id='" + zone_id + '\'' +
                ", request_id='" + request_id + '\'' +
                ", possible vehicles= "+possible_vehicle_list +

                '}';
    }

    public Integer getRequest_id() {
        return request_id;
    }

    public void setRequest_id(Integer request_id) {
        this.request_id = request_id;
    }

    public void setZone_id(int zone_id) {
        this.zone_id = zone_id;
    }

    public void setRequest_id(int request_id) {
        this.request_id = request_id;
    }

    public Integer getCurrentPenalty() {
        return currentPenalty;
    }

    public void setCurrentPenalty(Integer currentPenalty) {
        this.currentPenalty = currentPenalty;
    }

    public int getDay_index() {
        return day_index;
    }

    public void setDay_index(int day_index) {
        this.day_index = day_index;
    }

    public int getStart_time() {
        return start_time;
    }

    public void setStart_time(int start_time) {
        this.start_time = start_time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getPenalty1() {
        return penalty1;
    }

    public void setPenalty1(int penalty1) {
        this.penalty1 = penalty1;
    }

    public int getPenalty2() {
        return penalty2;
    }

    public void setPenalty2(int penalty2) {
        this.penalty2 = penalty2;
    }

    public Integer getZone_id() {
        return zone_id;
    }

    public void setZone_id(Integer zone_id) {
        this.zone_id = zone_id;
    }

    public ArrayList getPossible_vehicle_list() {
        return possible_vehicle_list;
    }

    public void setPossible_vehicle_list(ArrayList possible_vehicle_list) {
        this.possible_vehicle_list = possible_vehicle_list;
    }

    public boolean overlap(Request r) {
        //nog niet met doorlopende dagen gewerkt
        boolean one = (this.getDay_index() == r.getDay_index() && this.getStart_time() < (r.getStart_time() + r.getDuration()));
        boolean two = (r.getDay_index() == this.getDay_index() && r.getStart_time() < this.getStart_time() + this.getDuration());
        return one && two;
    }

    public boolean before(Request request) {
        if (this.getDay_index() < request.getDay_index()) {
            return true;
        }
        if (this.getDay_index() == request.getDay_index() && this.getStart_time() < request.getStart_time()) {
            return true;
        }
        return false;
    }
}
