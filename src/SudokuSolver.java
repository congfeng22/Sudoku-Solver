import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SudokuSolver {
    public static Sudoku backTracking(Sudoku s, int idx){
        // If assignment is complete, return result
        if (isDone(s)) return s;
        // Move pointer to next empty square
        while (s.getGrid().get(idx/9).get(idx%9) != -1) idx++;
        // Row and column for easy access
        int row = idx/9; int col = idx%9;
        // Copy current candidates for this square
        Set<Integer> tempCandidates = new HashSet<>(s.getCandidates().get(idx));
        // Replacement candidate for this square
        Set<Integer> newCandidates = new HashSet<>();
        // If no candidates exist for this square, backtrack
        if (tempCandidates.size() == 0) return null;
        // Iterate through candidates
        for (int value : tempCandidates){
            // Try out each candidate
            newCandidates.add(value);
            s.getCandidates().put(idx, newCandidates);
            s.getGrid().get(row).set(col, value);
            // If grid is valid
            if (isValid(s, row, col)){
                Sudoku newS = backTracking(s, idx);
                // If correct choice, return result
                if (isDone(newS)){
                    return newS;
                }
            }
            // Backtrack if doesn't work
            s.getCandidates().put(idx, tempCandidates);
            s.getGrid().get(row).set(col, -1);
            newCandidates.clear();
        }
        // No solution
        return null;
    }

    /**
     * Check if a Sudoku is valid against its rules
     * @param s the Sudoku
     * @param row the row of the new digit
     * @param col the column of the new digit
     * @return true iff it violates no rules
     */
    public static boolean isValid(Sudoku s, int row, int col){
        Map<String, List<List<Integer>>> rules = s.getRules();
        List<List<Integer>> grid = s.getGrid();
        int value = grid.get(row).get(col);

        // Check normal sudoku rules
        if (rules.containsKey("normal")){
            for (int k=0; k<9; k++){
                if (k == row) continue;
                if (grid.get(k).get(col) == value) return false;
            }
            for (int k=0; k<9; k++){
                if (k == col) continue;
                if (grid.get(row).get(k) == value) return false;
            }
            for (int k=(row/3)*3; k<(row/3+1)*3; k++){
                for (int l=(col/3)*3; l<(col/3+1)*3; l++){
                    if (k==row && l==col) continue;
                    if (grid.get(k).get(l) == value) return false;
                }
            }
        }

        // Check knight constraint
        if (rules.containsKey("knight")){
            int[] xdelta = {2, 1, -1, -2, -2, -1, 1, 2};
            int[] ydelta = {1, 2, 2, 1, -1, -2, -2, -1};
            for (int k=0; k<8; k++){
                if (isContained(row+ydelta[k], col+xdelta[k]) && grid.get(row+ydelta[k]).get(col+xdelta[k])==value){
                    return false;
                }
            }
        }

        // Check king constraint
        if (rules.containsKey("king")){
            int[] xdelta = {1, -1, -1, 1};
            int[] ydelta = {1, 1, -1, -1};
            for (int k=0; k<4; k++){
                if (isContained(row+ydelta[k], col+xdelta[k]) && grid.get(row+ydelta[k]).get(col+xdelta[k])==value){
                    return false;
                }
            }
        }

        // Check thermo constraint
        if (rules.containsKey("thermo")){
            List<List<Integer>> thermoRules = rules.get("thermo");
            for (List<Integer> thermo : thermoRules){
                int location = thermo.indexOf(row*9+col);
                if (location == -1) continue;
                for (int i=0; i<thermo.size(); i++){
                    int neighbor = grid.get(thermo.get(i)/9).get(thermo.get(i)%9);
                    if (neighbor != -1){
                        if (i<location && neighbor >= value - (location-i-1)) return false;
                        if (i>location && value + (i-location-1) >= neighbor) return false;
                    }
                }
            }
        }

        // Check diagonal constraint
        if (rules.containsKey("diagonal")){
            if (row == col){
                for (int i=0; i<9; i++){
                    if (i == row) continue;
                    if (grid.get(i).get(i) == value) return false;
                }
            }
            if (row == 8-col){
                for (int i=0; i<9; i++){
                    if (i == row) continue;
                    if (grid.get(i).get(8-i) == value) return false;
                }
            }
        }

        // Check disjoint constraint
        if (rules.containsKey("disjoint")){
            int[] xdelta = {0, 3, 6, 0, 3, 6, 0, 3, 6};
            int[] ydelta = {0, 0, 0, 3, 3, 3, 6, 6, 6};
            for (int i=0; i<9; i++){
                if (row%3+ydelta[i] == row && col%3+xdelta[i] == col) continue;
                if (grid.get(row%3+ydelta[i]).get(col%3+xdelta[i]) == value) return false;
            }
        }

        // Check nonconsecutive constraint
        if (rules.containsKey("nonconsecutive")){
            int[] xdelta = {1, 0, -1, 0};
            int[] ydelta = {0, 1, 0, -1};
            for (int k=0; k<4; k++){
                if (isContained(row+ydelta[k], col+xdelta[k]) && Math.abs(grid.get(row+ydelta[k]).get(col+xdelta[k])-value)==1){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if a row and column is valid
     * @param row the row
     * @param col the column
     * @return true if the entry exists in the grid
     */
    public static boolean isContained(int row, int col){
        return (0 <= row) && (row < 9) && (0 <= col) && (col < 9);
    }

    /**
     * Check if a sudoku is finished
     * @param s the sudoku
     * @return true iff the sudoku is completely filled and valid
     */
    public static boolean isDone(Sudoku s){
        if (s == null) return false;
        for (List<Integer> row : s.getGrid()){
            for (int entry : row){
                if (entry == -1){
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Generation of a grid that the code can parse from a string
     * @param grid a 81-length string
     * @return A grid as a list of lists of integers
     */
    public static List<List<Integer>> gridParser(String grid){
        List<List<Integer>> newGrid = new ArrayList<>();
        for (int i=0; i<9; i++){
            List<Integer> row = new ArrayList<>();
            for (int j=0; j<9; j++){
                char c = grid.charAt(9*i+j);
                if (c == '0') row.add(-1);
                else row.add(c-'0');
            }
            newGrid.add(row);
        }
        return newGrid;
    }

    public static void main(String[] args){
        // normal
        List<List<Integer>> grid5 = gridParser("005009400407010002008020095004000070000607000060000200980060300300090104001800600");
        List<List<Integer>> grid1 = gridParser("000000000058020760036000480007109800300000006010805020000010000702000508005204300");
        // knight
        List<List<Integer>> grid6 = gridParser("000000000002300000010040000080050000007600000000000870000009006000002005000000340");
        // king
        List<List<Integer>> grid3 = gridParser("000628000009000300060000010700000008400000005600000003080000090005000700000971000");
        // thermo
        //////////////////////////WARNING TIMEOUT/////////////////////////////////
        List<List<Integer>> grid4 = gridParser("000000000000000000000900000000000005000000000000000300000000000000000800000009000");
        List<List<Integer>> thermoRules = new ArrayList<>();
        thermoRules.add(Arrays.asList(0, 10, 20, 30, 38, 46));
        thermoRules.add(Arrays.asList(8, 16, 24, 32, 22, 12));
        thermoRules.add(Arrays.asList(72, 64, 56, 48, 58, 68));
        thermoRules.add(Arrays.asList(80, 70, 60, 50, 42, 34));
        // diagonal
        List<List<Integer>> grid7 = gridParser("000902000000807000900050003007409300800205007060000050200301004000504000009000700");
        // disjoint
        List<List<Integer>> grid8 = gridParser("090040010100305006006000400000901050207030901000604000009000300700403009080060047");
        // nonconsecutive and knight
        List<List<Integer>> grid9 = gridParser("000000000000000000000407000006000500000000000004000300000205000000000000000000000");
        // nonconsecutive and knight and king
        List<List<Integer>> grid = gridParser("000000000000000000000000000000000000001000000000000200000000000000000000000000000");

        Map<String, List<List<Integer>>> rules = new HashMap<>();
        rules.put("normal", null);
        rules.put("nonconsecutive", null);
        rules.put("knight", null);
        rules.put("king", null);
        //rules.put("thermo", thermoRules);

        Map<Integer, Set<Integer>> candidates = new HashMap<>();
        for (int i=0; i<81; i++){
            if (grid.get(i/9).get(i%9) == -1) {
                candidates.put(i, Stream.of(1, 2, 3, 4, 5, 6, 7, 8, 9).collect(Collectors.toCollection(HashSet::new)));
            }
            else {
                candidates.put(i, new HashSet(Arrays.asList(grid.get(i/9).get(i%9))));
            }
        }

        Sudoku sudoku = new Sudoku(grid, rules, candidates);
        System.out.println(sudoku);
        System.out.println(backTracking(sudoku, 0));
    }
}

