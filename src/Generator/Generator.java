package Generator;

import model.Request;
import model.Solution;

import java.util.List;

public interface Generator {
    List<ReplaceMove> generateRandom(Solution s, List<Request> requestlijst, int[][] adjacentZone );
}
