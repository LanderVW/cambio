package model;

import java.util.Comparator;

public class RequestComparator implements Comparator<Request> {
    @Override
    public int compare(Request o1, Request o2) {
        return o1.getPenalty1() - o2.getPenalty1();
    }
}
