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
 * PathFinding�����״̬
 * λ��״̬����ʾѰ·��������ʲôλ��
 */
public class PuzzleBoard extends State {
    // ��ǰ������״̬�����̵� size ����ͨ�� board.length ��ȡ
    int [][] board;
    // ��ǰ��λ������(�±�� 0 ��ʼ�����Ͻ�Ϊ (0, 0))
    int x, y;

    // ���캯��
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

    // ���ƹ��캯��
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
     * ��ǰ״̬����action���������һ��״̬
     *
     * @param action ��ǰ״̬�£�һ�����е�action
     * @return ��һ��״̬
     */
    @Override
    public State next(Action action) {
        //��ǰAction��������λ����
        Direction dir = ((Move)action).getDirection();
        int[] offsets = Direction.offset(dir);

        // ���� action ֮���λת�Ƶ���λ��
        int col = y + offsets[0];
        int row = x + offsets[1];
        // ԭ�����ϵ� [row, col] λ���ϵ���
        int dest_val = board[row][col];

        // �����¶���
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

//    //ö��ӳ�䣬��Ų�ͬ���͵���������
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
//    //������֮���Grid���룬�����߶Խ���
//    private int gridDistance(Position goal) {
//        int width = Math.abs(this.col - goal.col);
//        int height = Math.abs(this.row - goal.row);
//
//        return Math.abs(width - height) * SCALE + Math.min(width, height) * (int) (SCALE * ROOT2);
//    }
//
//    //������֮��������پ������SCALE
//    private int manhattan(Position goal) {
//        return (Math.abs(this.row - goal.row) + Math.abs(this.col - goal.col)) * SCALE;
//    }
//
//    //������֮���ŷ����¾���
//    private int euclid(Position goal) {
//        int width = this.col - goal.col;
//        int height = this.row - goal.row;
//        return (int) Math.sqrt(height * height + width * width) * SCALE;
//    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;

        if (obj instanceof PuzzleBoard another) {
            //����Board��������������Զ�����ͬ�ģ�����Ϊ�������
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

    // ֻ��Ҫ������ǰ�����̾���Ψһȷ����ǰ��״̬������ֻ�� board �� hash ����.
    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }

    // ���ص�ǰ����
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
