package stud.g01.problem.npuzzle;

import core.problem.Action;
import core.problem.Problem;
import core.problem.State;
import core.solver.queue.Node;

import java.util.Deque;

public class NPuzzleProblem extends Problem {
    public NPuzzleProblem(State initialState, State goal) {
        super(initialState, goal);
    }

    public NPuzzleProblem(State initialState, State goal, int size) {
        super(initialState, goal, size);
    }

    /**
     * 判断 NPuzzle 问题是否有解
     *
     */

    @Override
    public boolean solvable() {
        // 假定 initialState 和 goal 必须是 PuzzleBoard
        if (!(initialState instanceof PuzzleBoard) || !(goal instanceof PuzzleBoard))
            throw new IllegalArgumentException("initialState 和 goal 必须是 PuzzleBoard");

        PuzzleBoard ib = (PuzzleBoard) initialState;
        PuzzleBoard gb = (PuzzleBoard) goal;

        int parityInit = parityWithBlank(ib);
        int parityGoal = parityWithBlank(gb);
        return parityInit == parityGoal;
    }

    /**
     * 计算状态的“奇偶性值”：
     * - 若宽度为奇数，返回 inversions % 2 = 1
     * - 若宽度为偶数，返回 inversions % 2 = 0
     *
     */
    private int parityWithBlank(PuzzleBoard pb) {
        int n = pb.board.length;

        // 将非空格的数字按行优先展平到数组中
        int[] arr = new int[n * n - 1];
        int idx = 0;
        int blankRow = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (pb.board[i][j] != 0) {
                    arr[idx++] = pb.board[i][j];

                }
                else{
                    blankRow = i;}
            }
        }

        // 计算倒置数（inversions）
        int inversions = 0;
        for (int i = 0; i < idx; i++) {
            for (int j = i + 1; j < idx; j++) {
                if (arr[i] > arr[j])
                    inversions++;
            }
        }

        inversions = inversions+blankRow+1;//加上空格所在行数

        return inversions % 2;
    }

    @Override
    public int stepCost(State state, Action action) {
        return 1;
    }

    /**
     * 判断 action 在 state 状态下是否可行
     *
     */
    @Override
    public boolean applicable(State state, Action action) {
        // 类型检查：state 必须是 PuzzleBoard，action 必须是 Move
        if (!(state instanceof PuzzleBoard) || !(action instanceof Move))
            return false;

        PuzzleBoard pb = (PuzzleBoard) state;
        Direction dir = ((Move) action).getDirection();

        // Direction.offset(dir) 返回 {colOffset, rowOffset}（与 PuzzleBoard 中的实现对应）
        int[] off = Direction.offset(dir);

        // 计算移动后空格应该到达的位置（row, col）
        int col = pb.y + off[0];
        int row = pb.x + off[1];

        int n = pb.board.length;
        // 判断目标位置是否在棋盘范围内（0..n-1）
        return row >= 0 && row < n && col >= 0 && col < n;
    }

    static java.io.PrintStream out;
    static {
        try{
            out = new java.io.PrintStream("display-ir");
        } catch (java.io.FileNotFoundException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void showSolution(Deque<Node> path) {
        for (var node: path) {
            PuzzleBoard pb = (PuzzleBoard) node.getState();
            int L = pb.board.length;
            //pb.draw();
            for (int[] row: pb.board) {
                out.print(row[0]);
                for (int i = 1; i < L; i++) {
                    out.print(',');
                    out.print(row[i]);
                }
                out.println();
            }
            out.print('*');
        }
    }

}
