package stud.g01.problem.npuzzle;

import core.problem.Action;
import core.problem.State;
import core.solver.algorithm.heuristic.HeuristicType;
import core.solver.algorithm.heuristic.Predictor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;

import static core.solver.algorithm.heuristic.HeuristicType.*;

/**
 * PathFinding问题的状态
 * 位置状态，表示寻路机器人在什么位置
 */
public class PuzzleBoard extends State {
    // 当前的棋盘状态，棋盘的 size 可以通过 board.length 获取
    int [][] board;
    // 当前空位的坐标(下标从 0 开始，左上角为 (0, 0))
    int x, y;

    // 构造函数
    public PuzzleBoard(int[][] b, int x0, int y0){
        this.board = b;
        this.x = x0;
        this.y = y0;
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
    }

    // 复制构造函数
    public PuzzleBoard(PuzzleBoard another){
        this.x = another.x;
        this.y = another.y;
        int size = another.board.length;
        this.board = new int[size][size];
        for (int i = 0; i < size; i++){
            System.arraycopy(another.board[i], 0, this.board[i], 0, size);
        }
    }

    @Override
    public void draw() {
        System.out.println(this);
    }

    /**
     * 当前状态采用action而进入的下一个状态
     *
     * @param action 当前状态下，一个可行的action
     * @return 下一个状态
     */
    @Override
    public State next(Action action) {
        //当前Action所带来的位移量
        Direction dir = ((Move)action).getDirection();
        int[] offsets = Direction.offset(dir);

        // 采用 action 之后空位转移到的位置
        int col = y + offsets[0];
        int row = x + offsets[1];
        // 原棋盘上的 [row, col] 位置上的数
        int dest_val = board[row][col];

        // 构建新对象
        PuzzleBoard result = new PuzzleBoard(this);
        result.board[row][col] = 0;
        result.board[x][y] = dest_val;
        result.x = row;
        result.y = col;
        return result;
    }

    @Override
    public Iterable<? extends Action> actions() {
        Collection<Move> moves = new ArrayList<>();
        for (Direction d : Direction.FOUR_DIRECTIONS)
            moves.add(new Move(d));
        return moves;
    }
// todo: wx

//    //枚举映射，存放不同类型的启发函数
//    private static final EnumMap<HeuristicType, Predictor> predictors = new EnumMap<>(HeuristicType.class);
//    static{
//        predictors.put(PF_EUCLID,
//                (state, goal) -> ((Position)state).euclid((Position)goal));
//        predictors.put(PF_MANHATTAN,
//                (state, goal) -> ((Position)state).manhattan((Position)goal));
//        predictors.put(PF_GRID,
//                (state, goal) -> ((Position)state).gridDistance((Position)goal));
//    }
//
//    public static Predictor predictor(HeuristicType type){
//        return predictors.get(type);
//    }
//
//    //两个点之间的Grid距离，尽量走对角线
//    private int gridDistance(Position goal) {
//        int width = Math.abs(this.col - goal.col);
//        int height = Math.abs(this.row - goal.row);
//
//        return Math.abs(width - height) * SCALE + Math.min(width, height) * (int) (SCALE * ROOT2);
//    }
//
//    //两个点之间的曼哈顿距离乘以SCALE
//    private int manhattan(Position goal) {
//        return (Math.abs(this.row - goal.row) + Math.abs(this.col - goal.col)) * SCALE;
//    }
//
//    //两个点之间的欧几里德距离
//    private int euclid(Position goal) {
//        int width = this.col - goal.col;
//        int height = this.row - goal.row;
//        return (int) Math.sqrt(height * height + width * width) * SCALE;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof PuzzleBoard another) {
            //两个Board对象如果所有属性都是相同的，则认为它们相等
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

    // 只需要给出当前的棋盘就能唯一确定当前的状态，所以只对 board 做 hash 即可.
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    // 返回当前棋盘
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int size = board.length;
        for (int[] ints : board) {
            for (int j = 0; j < size; j++) {
                sb.append(ints[j]).append(' ');
            }
            sb.append('\n');
        }
        return sb.toString();
    }
}
