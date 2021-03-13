import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SudokuSolver {
    public static Sudoku backTracking(Sudoku s, int idx){
        // If assignment is complete, return result
        if (isDone(s)) return s;
        // Move pointer to next empty square
        while (s.getGrid().get(idx/9).get(idx%9) != 0) idx++;
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
            s.getGrid().get(row).set(col, 0);
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
                    if (neighbor != 0){
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

        // Check arrow constraint
        if (rules.containsKey("arrow")){
            List<List<Integer>> arrowRules = rules.get("arrow");
            for (List<Integer> arrow : arrowRules){
                if (arrow.contains(row*9+col)){
                    int arrowHead = grid.get(arrow.get(0)/9).get(arrow.get(0)%9)==0?9:grid.get(arrow.get(0)/9).get(arrow.get(0)%9);
                    for (int i=1; i<arrow.size(); i++){
                        arrowHead -= grid.get(arrow.get(i)/9).get(arrow.get(i)%9)==0?1:grid.get(arrow.get(i)/9).get(arrow.get(i)%9);
                        if (arrowHead<0){
                            return false;
                        }
                    }
                }
            }
        }

        // Check palindrome constraint
        if (rules.containsKey("palindrome")){
            List<List<Integer>> palindromeRules = rules.get("palindrome");
            for (List<Integer> palindrome : palindromeRules){
                if (palindrome.contains(row*9+col)){
                    for (int i=0; i<palindrome.size()/2; i++){
                        int start = grid.get(palindrome.get(i)/9).get(palindrome.get(i)%9);
                        int end = grid.get(palindrome.get(palindrome.size()-i-1)/9).get(palindrome.get(palindrome.size()-i-1)%9);
                        if (start != 0 && end != 0 && start != end) return false;
                    }
                }
            }
        }

        // Check XV constraint
        if (rules.containsKey("xv")){
            List<List<Integer>> xvRules = rules.get("xv");
            for (List<Integer> xv : xvRules){
                int one = grid.get(xv.get(1)/9).get(xv.get(1)%9);
                int two = grid.get(xv.get(2)/9).get(xv.get(2)%9);
                if ((xv.get(1) == row*9+col && two != 0) || (xv.get(2) == row*9+col && one != 0)) {
                    if (one + two != xv.get(0)) return false;
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
                if (entry == 0){
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
                row.add(c-'0');
            }
            newGrid.add(row);
        }
        return newGrid;
    }

    public static void main(String[] args){
        // normal
        List<List<Integer>> grid1 = gridParser("005009400407010002008020095004000070000607000060000200980060300300090104001800600");
        List<List<Integer>> grid2 = gridParser("000000000058020760036000480007109800300000006010805020000010000702000508005204300");

        // knight
        List<List<Integer>> grid3 = gridParser("000000000002300000010040000080050000007600000000000870000009006000002005000000340");

        // king
        List<List<Integer>> grid4 = gridParser("000628000009000300060000010700000008400000005600000003080000090005000700000971000");

        // thermo
        //////////////////////////WARNING TIMEOUT/////////////////////////////////
        List<List<Integer>> grid5 = gridParser("000000000000000000000900000000000005000000000000000300000000000000000800000009000");
        List<List<Integer>> thermoRules = new ArrayList<>();
        thermoRules.add(Arrays.asList(0, 10, 20, 30, 38, 46));
        thermoRules.add(Arrays.asList(8, 16, 24, 32, 22, 12));
        thermoRules.add(Arrays.asList(72, 64, 56, 48, 58, 68));
        thermoRules.add(Arrays.asList(80, 70, 60, 50, 42, 34));

        // diagonal
        List<List<Integer>> grid6 = gridParser("000902000000807000900050003007409300800205007060000050200301004000504000009000700");

        // disjoint
        List<List<Integer>> grid7 = gridParser("090040010100305006006000400000901050207030901000604000009000300700403009080060047");

        // nonconsecutive and knight
        List<List<Integer>> grid8 = gridParser("000000000000000000000407000006000500000000000004000300000205000000000000000000000");

        // nonconsecutive and knight and king
        List<List<Integer>> grid9 = gridParser("000000000000000000000000000000000000001000000000000200000000000000000000000000000");

        // arrow
        List<List<Integer>> grid10 = gridParser("000000000034700060000000070000000090000000000080000000090000000060009850000000000");
        List<List<Integer>> arrowRules = new ArrayList<>();
        arrowRules.add(Arrays.asList(10, 19, 28));
        arrowRules.add(Arrays.asList(11, 20, 29));
        arrowRules.add(Arrays.asList(12, 21, 30));
        arrowRules.add(Arrays.asList(16, 15, 14));
        arrowRules.add(Arrays.asList(25, 24, 23));
        arrowRules.add(Arrays.asList(34, 33, 32));
        arrowRules.add(Arrays.asList(46, 47, 48));
        arrowRules.add(Arrays.asList(55, 56, 57));
        arrowRules.add(Arrays.asList(64, 65, 66));
        arrowRules.add(Arrays.asList(68, 59, 50));
        arrowRules.add(Arrays.asList(69, 60, 51));
        arrowRules.add(Arrays.asList(70, 61, 52));

        // palindrome
        List<List<Integer>> grid11 = gridParser("002030400000905000000000000000000000320000078000000000000000000000504000506000809");
        List<List<Integer>> palindromeRules = new ArrayList<>();
        palindromeRules.add(Arrays.asList(9, 19, 29));
        palindromeRules.add(Arrays.asList(11, 21, 31));
        palindromeRules.add(Arrays.asList(13, 23, 33));
        palindromeRules.add(Arrays.asList(15, 25, 35));
        palindromeRules.add(Arrays.asList(45, 55, 65));
        palindromeRules.add(Arrays.asList(47, 57, 67));
        palindromeRules.add(Arrays.asList(49, 59, 69));
        palindromeRules.add(Arrays.asList(51, 61, 71));

        // XV
        List<List<Integer>> grid = gridParser("000000000000000000002000405000000000000000000000000000504000100000000000000000000");
        List<List<Integer>> xvRules = new ArrayList<>();
        xvRules.add(Arrays.asList(5,0,9));
        xvRules.add(Arrays.asList(5,6,15));
        xvRules.add(Arrays.asList(5,14,15));
        xvRules.add(Arrays.asList(5,21,30));
        xvRules.add(Arrays.asList(5,28,37));
        xvRules.add(Arrays.asList(5,44,53));
        xvRules.add(Arrays.asList(5,49,58));
        xvRules.add(Arrays.asList(5,6,15));
        xvRules.add(Arrays.asList(10,2,3));
        xvRules.add(Arrays.asList(10,7,8));
        xvRules.add(Arrays.asList(10,4,13));
        xvRules.add(Arrays.asList(10,15,16));
        xvRules.add(Arrays.asList(10,23,32));
        xvRules.add(Arrays.asList(10,31,32));
        xvRules.add(Arrays.asList(10,34,35));
        xvRules.add(Arrays.asList(10,36,37));
        xvRules.add(Arrays.asList(10,42,43));
        xvRules.add(Arrays.asList(10,40,49));
        xvRules.add(Arrays.asList(10,41,50));
        xvRules.add(Arrays.asList(10,57,58));
        xvRules.add(Arrays.asList(10,61,62));
        xvRules.add(Arrays.asList(10,63,64));
        xvRules.add(Arrays.asList(10,66,67));
        xvRules.add(Arrays.asList(10,70,71));
        xvRules.add(Arrays.asList(10,79,80));

        Map<String, List<List<Integer>>> rules = new HashMap<>();
        rules.put("normal", null);
        //rules.put("nonconsecutive", null);
        rules.put("xv", xvRules);

        Map<Integer, Set<Integer>> candidates = new HashMap<>();
        for (int i=0; i<81; i++){
            if (grid.get(i/9).get(i%9) == 0) {
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

