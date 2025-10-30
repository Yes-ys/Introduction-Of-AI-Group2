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
     * �ж� NPuzzle �����Ƿ��н�
     *
     */

    @Override
    public boolean solvable() {
        // �ٶ� initialState �� goal ������ PuzzleBoard
        if (!(initialState instanceof PuzzleBoard) || !(goal instanceof PuzzleBoard))
            throw new IllegalArgumentException("initialState �� goal ������ PuzzleBoard");

        PuzzleBoard ib = (PuzzleBoard) initialState;
        PuzzleBoard gb = (PuzzleBoard) goal;

        int parityInit = parityWithBlank(ib);
        int parityGoal = parityWithBlank(gb);
        return parityInit == parityGoal;
    }

    /**
     * ����״̬�ġ���ż��ֵ����
     * - �����Ϊ���������� inversions % 2 = 1
     * - �����Ϊż�������� inversions % 2 = 0
     *
     */
    private int parityWithBlank(PuzzleBoard pb) {
        int n = pb.board.length;

        // ���ǿո�����ְ�������չƽ��������
        int[] arr = new int[n * n - 1];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (pb.board[i][j] != 0) {
                    arr[idx++] = pb.board[i][j];
                }
            }
        }

        // ���㵹������inversions��
        int inversions = 0;
        for (int i = 0; i < idx; i++) {
            for (int j = i + 1; j < idx; j++) {
                if (arr[i] > arr[j])
                    inversions++;
            }
        }
        inversions = inversions + n;

        return inversions % 2;
    }

    @Override
    public int stepCost(State state, Action action) {
        return 1;
    }

    /**
     * �ж� action �� state ״̬���Ƿ����
     *
     */
    @Override
    public boolean applicable(State state, Action action) {
        // ���ͼ�飺state ������ PuzzleBoard��action ������ Move
        if (!(state instanceof PuzzleBoard) || !(action instanceof Move))
            return false;

        PuzzleBoard pb = (PuzzleBoard) state;
        Direction dir = ((Move) action).getDirection();

        // Direction.offset(dir) ���� {colOffset, rowOffset}���� PuzzleBoard �е�ʵ�ֶ�Ӧ��
        int[] off = Direction.offset(dir);

        // �����ƶ���ո�Ӧ�õ����λ�ã�row, col��
        int col = pb.y + off[0];
        int row = pb.x + off[1];

        int n = pb.board.length;
        // �ж�Ŀ��λ���Ƿ������̷�Χ�ڣ�0..n-1��
        return row >= 0 && row < n && col >= 0 && col < n;
    }

    @Override
    public void showSolution(Deque<Node> path) {

    }
}
