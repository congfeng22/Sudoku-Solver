import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Sudoku {
    private List<List<Integer>> myGrid;
    private Map<String, List<List<Integer>>> myRules;
    private Map<Integer, Set<Integer>> myCandidates;

    /**
     * Create a Sudoku from parameters
     * @param grid initial Sudoku grid
     * @param rules of the Sudoku grid
     */
    public Sudoku(List<List<Integer>> grid, Map<String, List<List<Integer>>> rules){
        myGrid = grid;
        myRules = rules;

        Map<Integer, Set<Integer>> candidates = new HashMap<>();
        for (int i=0; i<81; i++){
            if (grid.get(i/9).get(i%9) == 0) {
                candidates.put(i, Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).collect(Collectors.toCollection(HashSet::new)));
            }
            else {
                candidates.put(i, new HashSet(Arrays.asList(grid.get(i/9).get(i%9))));
            }
        }
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
     * @param newCandidates updated new candidates of the Sudoku
     */
    public void updateCandidates(Map<Integer, Set<Integer>> newCandidates){
        myCandidates = newCandidates;
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