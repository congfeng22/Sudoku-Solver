import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Sudoku {
    private List<List<Integer>> myGrid;
    private Map<String, List<List<Integer>>> myRules;
    private Map<Integer, Set<Integer>> myCandidates;

    /**
     * Create a Sudoku from parameters
     * @param grid initial Sudoku grid
     * @param rules of the Sudoku grid
     * @param candidates of each entry in the grid
     */
    public Sudoku(List<List<Integer>> grid, Map<String, List<List<Integer>>> rules, Map<Integer, Set<Integer>> candidates){
        myGrid = grid;
        myRules = rules;
        myCandidates = candidates;
    }

    /**
     * @return current grid
     */
    public List<List<Integer>> getGrid(){
        return myGrid;
    }

    /**
     * @return all rules of the Sudoku
     */
    public Map<String, List<List<Integer>>> getRules(){
        return myRules;
    }

    /**
     * @return all candidates of the Sudoku
     */
    public Map<Integer, Set<Integer>> getCandidates(){
        return myCandidates;
    }

    /**
     * @return String representation of Sudoku
     */
    public String toString(){
        StringBuilder print = new StringBuilder();
        print.append("Grid: " + myGrid.get(0).toString() + "\n");
        for (int i=1; i<9; i++){
            print.append("      " + myGrid.get(i).toString() + "\n");
        }
        print.append("Rules: " + myRules.toString());
        return print.toString();
    }
}