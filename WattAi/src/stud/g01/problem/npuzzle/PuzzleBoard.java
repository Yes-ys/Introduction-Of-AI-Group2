package stud.g01.problem.npuzzle;

import core.problem.Action;
import core.problem.State;
import core.runner.SearchTester;
import core.solver.algorithm.heuristic.HeuristicType;
import core.solver.algorithm.heuristic.Predictor;
import stud.g01.pdb.SQLitePDB;

import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

import static core.solver.algorithm.heuristic.HeuristicType.*;

public class PuzzleBoard extends State {
    int [][] board;
    int x, y;
    private final int hashcode;
    private final String tostring;

    //维护mht的估值，主要是在创建PuzzleBoard实例的时候修改下面两个成员；
    //于是需要修改用到了PuzzleBoard实例化的函数，主要是构造函数和next函数；
    //使用它们进行便捷的mht估值计算，需要修改mht估值函数；
    //是否用mht估值由PuzzleFeeder处确定
    private int manhattan_heuristics;
    private static int[][] GoalBoard = new int[16][2];//目标状态9/16个数字的坐标，先x后y
    private static boolean if_mht;

    //不相交模式数据库的A*算法用
//    private static final String pdbPath = "data.db";
//    private static final SQLitePDB pdb = new SQLitePDB(pdbPath,1024);

    private String buildToString() {
        StringBuilder sb = new StringBuilder();
        for (int[] ints: board) {
            sb.append(ints[0]);
            for (int j = 1; j < ints.length; j++) {
                sb.append(',');
                sb.append(ints[j]);
            }
            sb.append('\n');
        }
        return sb.toString();
    }

    public PuzzleBoard(int[][] b){
        this.board = b;
        int size = b.length;
        for (int i = 0; i < size; i++){
            for (int j = 0; j < size; j++){
                if (b[i][j] == 0){
                    this.x = i;
                    this.y = j;
                }
            }
        }
        hashcode = Arrays.deepHashCode(board);
        tostring = buildToString();
    }

    public PuzzleBoard(int[][] cur,int[][] tar, int h){
        if(h == -1){
            if_mht = true;
            //基本的初始化
            this.board = cur;
            int size = cur.length;
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){
                    if (cur[i][j] == 0){
                        this.x = i;
                        this.y = j;
                    }
                }
            }
            //初始化时传入h=-1，对应ini需要通过遍历计算mht距离
            int distance = 0;
            Map<Integer, int[]> targetPositions = new HashMap<>();
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    int value = tar[i][j];
                    if (value != 0) {
                        targetPositions.put(value, new int[]{i, j});
                    }
                }
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    int value = cur[i][j];
                    if (value != 0) {
                        int[] targetPos = targetPositions.get(value);
                        if (targetPos != null) {
                            distance += Math.abs(i - targetPos[0]) + Math.abs(j - targetPos[1]);
                        }
                    }
                }
            }
            manhattan_heuristics = distance;
            //初始化目标状态各数字的坐标
            for(int i = 0;i < size;i++){
                for(int j = 0;j < size;j++)
                {
                    GoalBoard[tar[i][j]][0] = i;
                    GoalBoard[tar[i][j]][1] = j;
                }
            }
        }else if(h == 0){
            //基本的初始化
            this.board = cur;
            int size = cur.length;
            for (int i = 0; i < size; i++){
                for (int j = 0; j < size; j++){
                    if (cur[i][j] == 0){
                        this.x = i;
                        this.y = j;
                    }
                }
            }
        }else{
            throw new RuntimeException("invalid h value means invalid use of constructor!");
        }
        hashcode = Arrays.deepHashCode(board);
        tostring = buildToString();
    }

    public PuzzleBoard(PuzzleBoard another){
        this.x = another.x;
        this.y = another.y;
        int size = another.board.length;
        this.board = new int[size][size];
        for (int i = 0; i < size; i++){
            System.arraycopy(another.board[i], 0, this.board[i], 0, size);
        }
        this.manhattan_heuristics = another.manhattan_heuristics;
        this.hashcode = Arrays.deepHashCode(board);
        this.tostring = buildToString();
    }

    @Override
    public void draw() {
        System.out.println(this);
    }

    @Override
    public State next(Action action) {
        Direction dir = ((Move)action).getDirection();
        int[] offsets = Direction.offset(dir);
        int col = y + offsets[0];
        int row = x + offsets[1];
        int dest_val = board[row][col];
        int [][] new_board = new int[this.board.length][];
        for (int i = 0; i < this.board.length; i++) {
            new_board[i] = Arrays.copyOf(this.board[i], this.board[i].length);
        }

        new_board[row][col] = 0;
        new_board[x][y] = dest_val;
        PuzzleBoard result = new PuzzleBoard(new_board);

        //xy是dest_val现在的坐标，row col是dest_val原来的坐标
        if(if_mht){
            result.manhattan_heuristics = manhattan_heuristics -
                    (Math.abs(row-GoalBoard[dest_val][0])+Math.abs(col-GoalBoard[dest_val][1]))+
                    (Math.abs(x - GoalBoard[dest_val][0])+Math.abs(y - GoalBoard[dest_val][1]));
        }
        return result;
    }

    @Override
    public Iterable<? extends Action> actions() {
        Collection<Move> moves = new ArrayList<>();
        for (Direction d : Direction.FOUR_DIRECTIONS)
            moves.add(new Move(d));
        return moves;
    }

    // 枚举映射，存放不同类型的启发函数
    private static final EnumMap<HeuristicType, Predictor> predictors = new EnumMap<>(HeuristicType.class);
    static{
        predictors.put(MISPLACED, PuzzleBoard::misplacedTiles);
        predictors.put(MANHATTAN, PuzzleBoard::manhattanDistance);
        predictors.put(DISJOINT_PATTERN,PuzzleBoard::DisjointPatternDatabase);
    }

    public static Predictor predictor(HeuristicType type){
        return predictors.get(type);
    }

    // 计算当前状态到目标状态的 Misplaced Tiles 距离
    private static int misplacedTiles(State state, State goal) {
        PuzzleBoard current = (PuzzleBoard) state;
        PuzzleBoard target = (PuzzleBoard) goal;
        int misplacedCount = 0;
        int size = current.board.length;

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (current.board[i][j] != 0 && current.board[i][j] != target.board[i][j]) {
                    misplacedCount++;
                }
            }
        }
        return misplacedCount;
    }

    // 计算当前状态到目标状态的 Manhattan 距离
    private static int manhattanDistance(State state, State goal) {
        if (!if_mht) {
            PuzzleBoard current = (PuzzleBoard) state;
            PuzzleBoard target = (PuzzleBoard) goal;
            int manhattanSum = 0;
            int size = current.board.length;

            int[][] goalPosition = new int[size*size][2];
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    int value = target.board[i][j];
                    goalPosition[value][0] = i;
                    goalPosition[value][1] = j;
                }
            }

            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    int value = current.board[i][j];
                    if (value != 0) {
                        manhattanSum += Math.abs(i - goalPosition[value][0]) + Math.abs(j - goalPosition[value][1]);
                    }
                }
            }
            return manhattanSum;
        }else{
            System.out.println("Manhattan calc type: " + (if_mht ? "CACHED" : "FULL"));
            PuzzleBoard current = (PuzzleBoard) state;
            return current.manhattan_heuristics;
        }
    }

    private static int DisjointPatternDatabase(State state, State goal) throws SQLException {
        PuzzleBoard current = (PuzzleBoard) state;
        int size = current.board.length;
        if(size != 4)
            throw new IllegalArgumentException("DisjointPatternDatabase should be used in size of 4");

        String[] Patterns = new String[3];
        for (int i = 0; i < Patterns.length; i++) {
            Patterns[i] = ""; //初始化Patterns
        }
        int[] rowBoard = new int[15]; // rowBoard[i]：i + 1 出现的位置
        for(int i = 0;i < 16;i++)
        {
            int num = current.board[i/4][i%4];
            if(num == 0) continue;
            rowBoard[num - 1] = i;
        }

        for(int i = 0;i < 3;i++)
        {
            Patterns[i] += "[";
            for(int j = 0;j < 5;j++)
            {
                Patterns[i] += String.valueOf(rowBoard[i*5 + j]);
                if(j != 4)Patterns[i] += ", ";
            }
            Patterns[i] += "]";
        }

        int pdb_heuristics = 0;
        try {
            //System.out.println(pdbPath);
            for(int patternId = 1; patternId <= 3; patternId++)
            {
                String key = Patterns[patternId - 1];
//                System.out.println("key:"+key);
//                System.out.println("PatternId:"+patternId);
                if (SearchTester.pdb.hasKey(patternId, key)) {
                    pdb_heuristics += SearchTester.pdb.getCost(patternId, key);
                } else {
                    System.out.println("查找失败，模式不存在");
                }
            }
        } catch (Exception e) {
            try { SearchTester.pdb.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
        }

//        for debug：打印数据库中内容
//        try (SQLitePDB pdb = new SQLitePDB(pdbPath, 1024)) {
//            pdb.open();
//            List<Map<String, Object>> entries = pdb.viewAllEntries();
//            for (Map<String, Object> entry : entries) {
//                System.out.println(entry);
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }

        return pdb_heuristics;
    }


    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof PuzzleBoard another) {
            if (this.x == another.x && this.y == another.y){
                int crt_size = board.length;
                for (int i = 0; i < crt_size; i++){
                    for (int j = 0; j < crt_size; j++){
                        if (this.board[i][j] != another.board[i][j]) return false;
                    }
                }
                return true;
            }
        }
        return false;
    }


    @Override
    public int hashCode() { return hashcode; }

    @Override
    public String toString() { return tostring; }
}
