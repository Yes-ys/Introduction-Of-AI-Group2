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

public class PuzzleBoard extends State {
    int [][] board;
    int x, y;

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

    @Override
    public State next(Action action) {
        Direction dir = ((Move)action).getDirection();
        int[] offsets = Direction.offset(dir);
        int col = y + offsets[0];
        int row = x + offsets[1];
        int dest_val = board[row][col];

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

    // 枚举映射，存放不同类型的启发函数
    private static final EnumMap<HeuristicType, Predictor> predictors = new EnumMap<>(HeuristicType.class);
    static{
        predictors.put(MISPLACED, PuzzleBoard::misplacedTiles);
        predictors.put(MANHATTAN, PuzzleBoard::manhattanDistance);
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
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

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
