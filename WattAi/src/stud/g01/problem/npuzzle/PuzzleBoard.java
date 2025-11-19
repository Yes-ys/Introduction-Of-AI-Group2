package stud.g01.problem.npuzzle;

import core.problem.Action;
import core.problem.State;
import core.runner.SearchTester;
import core.solver.algorithm.heuristic.HeuristicType;
import core.solver.algorithm.heuristic.Predictor;
import stud.g01.utils.PatternDBBuilder;
import java.util.*;

import static core.solver.algorithm.heuristic.HeuristicType.*;

public class PuzzleBoard extends State {
    int [][] board;
    int x, y;
    private final int hashcode;
    private final String tostring;

    private int manhattan_heuristics;
    private static final int[][] GoalBoard = new int[16][2];//目标状态9/16个数字的坐标，先x后y
    private static boolean if_mht;
    private static int[][] PATTERNS = {
            {1, 5, 6, 9, 10, 13},
            {7, 8, 11, 12, 14, 15},
            {2, 3, 4}
    };

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
            for(int i = 0;i < size;i++){
                for(int j = 0;j < size;j++)
                {
                    GoalBoard[tar[i][j]][0] = i;
                    GoalBoard[tar[i][j]][1] = j;
                }
            }
        }else if(h == 0){
            //�����ĳ�ʼ��
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

    private static final EnumMap<HeuristicType, Predictor> predictors = new EnumMap<>(HeuristicType.class);
    static{
        predictors.put(MISPLACED, PuzzleBoard::misplacedTiles);
        predictors.put(MANHATTAN, PuzzleBoard::manhattanDistance);
        predictors.put(DISJOINT_PATTERN,PuzzleBoard::DisjointPatternDatabase);
        predictors.put(LINEAR_CONFLICT, PuzzleBoard::linearConflict);
    }

    public static Predictor predictor(HeuristicType type){
        return predictors.get(type);
    }

    // ���㵱ǰ״̬��Ŀ��״̬�� Misplaced Tiles ����
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

    // ���㵱ǰ״̬��Ŀ��״̬�� Manhattan ����
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

    private static int[] extractPattern(int[] pos, int[] tiles) {
        int K = tiles.length;
        int[] res = new int[K];
        for (int i = 0; i < K; i++) {
            res[i] = pos[tiles[i]];
        }
        return res;
    }


    private static int DisjointPatternDatabase(State state, State goal) {
        PuzzleBoard current = (PuzzleBoard) state;
        int size = current.board.length;
        if(size != 4)
            throw new IllegalArgumentException("DisjointPatternDatabase should be used in size of 4");

        String[] Patterns = new String[3];
        for (int i = 0; i < Patterns.length; i++) {
            Patterns[i] = ""; // 初始化 Patterns
        }
        int[] rowBoard = new int[16]; // rowBoard[i]：i 出现的位置
        for(int i = 0;i < 16;i++)
        {
            int num = current.board[i/4][i%4];
            rowBoard[num] = i;
        }

        for (int i = 0; i < 3; i++)
        {
            Patterns[i] = String.valueOf(PatternDBBuilder.encode(extractPattern(rowBoard, PATTERNS[i])));
        }

        int pdb_heuristics = 0;
        try {
            //System.out.println(pdbPath);
            for(int patternId = 1; patternId <= 3; patternId++)
            {
                String key = Patterns[patternId - 1];
                if (SearchTester.pdb.hasKey(patternId, key)) {
                    pdb_heuristics += SearchTester.pdb.getCost(patternId, key);
                } else {
                    System.out.println("查找错误");
                }
            }
        } catch (Exception e) {
            SearchTester.pdb.rollback();
        }

        return Math.max(pdb_heuristics, linearConflict(state, goal));
    }

    // 计算线性冲突数量
    public static int nLinearConflicts(State state, State goal) {
        PuzzleBoard cur = (PuzzleBoard) state;
        int size = cur.board.length;
        int count = 0; // 用于记录线性冲突的数量

        // 用于存储每个数字对应的实际行和列
        int[] pR = new int[size * size + 1];
        int[] pC = new int[size * size + 1];

        // 初始化pR和pC数组
        for (int r = 0; r < size; r++) {
            for (int c = 0; c < size; c++) {
                pR[cur.board[r][c]] = r; // 存储数字对应的行
                pC[cur.board[r][c]] = c; // 存储数字对应的列
            }
        }

        // 检查每一行的线性冲突
        for (int r = 0; r < size; r++) {
            for (int cl = 0; cl < size; cl++) {
                for (int cr = cl + 1; cr < size; cr++) {
                    // 检查行中的线性冲突
                    if ( (r * size + cl + 1) != 0 && (r * size + cr + 1) != 0 && // 确保数字不是0（空位）
                            r == pR[(r * size + cl + 1)] && // 左边的数字在当前行
                            pR[(r * size + cl + 1)] == pR[(r * size + cr + 1)] && // 右边的数字也在当前行
                            pC[(r * size + cl + 1)] > pC[(r * size + cr + 1)]) { // 左边的数字在右边的数字的目标位置之后
                        count++;
                    }

                    // 检查列中的线性冲突
                    if ((cl * size + r + 1) != 0 && (cr * size + r + 1) != 0 && // 确保数字不是0（空位）
                            r == pC[(cl * size + r + 1)] && // 上边的数字在当前列
                            pC[(cl * size + r + 1)] == pC[(cr * size + r + 1)] && // 下边的数字也在当前列
                            pR[(cl * size + r + 1)] > pR[(cr * size + r + 1)]) { // 上边的数字在下边的数字的目标位置之下
                        count++;
                    }
                }
            }
        }

        return count;
    }
    // 计算当前状态到目标状态的 Linear Conflict 距离
private static int linearConflict(State state, State goal) {
    return manhattanDistance(state, goal) + 2 * nLinearConflicts(state,goal);
}

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PuzzleBoard other)) return false;
        return Arrays.deepEquals(this.board, other.board);
    }


    @Override
    public int hashCode() { return hashcode; }

    @Override
    public String toString() { return tostring; }
}
