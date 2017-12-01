package Generator;

import model.Request;
import model.Solution;

import java.util.List;

public class ReplaceMove extends Move {

    private int oldCarID;
    private int oldRequestID;
    private int newCarID;
    private int newRequestID;

    public ReplaceMove(Integer oldCarID, Integer oldRequestID, Integer newCarID, Integer newRequestID) {
        this.oldCarID = oldCarID;
        this.oldRequestID = oldRequestID;
        this.newCarID = newCarID;
        this.newRequestID = newRequestID;
    }

    public ReplaceMove(int newCarID, int newRequestID) {
        this.newCarID = newCarID;
        this.newRequestID = newRequestID;
    }

    public Integer getOldCarID() {
        return oldCarID;
    }

    public void setOldCarID(Integer oldCarID) {
        this.oldCarID = oldCarID;
    }

    public Integer getOldRequestID() {
        return oldRequestID;
    }

    public void setOldRequestID(Integer oldRequestID) {
        this.oldRequestID = oldRequestID;
    }

    public Integer getNewCarID() {
        return newCarID;
    }

    public void setNewCarID(Integer newCarID) {
        this.newCarID = newCarID;
    }

    public Integer getNewRequestID() {
        return newRequestID;
    }

    public void setNewRequestID(Integer newRequestID) {
        this.newRequestID = newRequestID;
    }

    @Override
    public String toString() {
        return "ReplaceMove{" +
                "newCarID=" + newCarID +
                ", newRequestID=" + newRequestID +
                '}';
    }
}
