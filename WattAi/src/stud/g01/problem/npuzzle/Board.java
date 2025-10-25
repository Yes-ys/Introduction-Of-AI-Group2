package stud.problem.pathfinding;

import core.problem.Action;
import core.problem.State;
import core.solver.algorithm.heuristic.HeuristicType;
import core.solver.algorithm.heuristic.Predictor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;

import static core.solver.algorithm.heuristic.HeuristicType.*;
import static stud.problem.pathfinding.Direction.ROOT2;
import static stud.problem.pathfinding.Direction.SCALE;

/**
 * PathFinding�����״̬
 * λ��״̬����ʾѰ·��������ʲôλ��
 */
public class Board extends State {
    int [][] board;
    int size;

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
    // todo:
    @Override
    public State next(Action action) {
        //��ǰAction��������λ����
        Direction dir = ((Move)action).getDirection();
        int[] offsets = Direction.offset(dir);

        //������״̬���ڵĵ�
        int col = getCol() + offsets[0];
        int row = getRow() + offsets[1];
        return new Position(row, col);
    }

    @Override
    public Iterable<? extends Action> actions() {
        Collection<Move> moves = new ArrayList<>();
        for (Direction d : Direction.FOUR_DIRECTIONS)
            moves.add(new Move(d));
        return moves;
    }
// todo:
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

        if (obj instanceof Position) {
            Position another = (Position) obj;
            //����Position���������������ͬ������Ϊ����ͬ��
            return this.row == another.row && this.col == another.col;
        }
        return false;
    }

    @Override
    public int hashCode() {
        return row << 3 | col;
    }

    @Override
    public String toString() {
        return "(" + row + "," + col + ")";
    }
}
