package stud.g01.runner;

import core.problem.Problem;
import core.runner.EngineFeeder;
import core.solver.algorithm.heuristic.HeuristicType;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.queue.EvaluationType;
import core.solver.queue.Frontier;
import stud.g01.problem.npuzzle.NPuzzleProblem;
import stud.g01.problem.npuzzle.PuzzleBoard;

import java.util.ArrayList;

public class PuzzleFeeder extends EngineFeeder {
    // 将从文件中读取到的字符串 Array 转化为 Problem Array
    @Override
    public ArrayList<Problem> getProblems(ArrayList<String> problemLines) {
        int lineNo = 0;
        ArrayList<Problem> problems = new ArrayList<>();

        while (lineNo < problemLines.size()) {
            int size = Integer.parseInt(problemLines.get(lineNo));
            int[][] originBoard = getBoard(problemLines, lineNo, size);
            lineNo += size;
            int[][] targetBoard = getBoard(problemLines, lineNo, size);
            lineNo += size;
            NPuzzleProblem problem = getNPuzzleProblem(size, originBoard, targetBoard);
            problems.add(problem);
        }
        return problems;
    }

    // 根据必要的信息初始化一个 NPuzzleProblem 对象
    public NPuzzleProblem getNPuzzleProblem(int size, int[][] originBoard, int[][] targetBoard) {
        PuzzleBoard origin = new PuzzleBoard(originBoard);
        PuzzleBoard target = new PuzzleBoard(targetBoard);
        return new NPuzzleProblem(origin, target, size);
    }

    // 从输入中读取需要的 board
    // 需要读取的 board 从输入 problemLines 的第 startLine 行开始，占据 size 行
    public int[][] getBoard(ArrayList<String> problemLines, int startLine, int size) {
        int[][] map = new int[size][];
        for (int i = 0; i < size; i++){
            map[i] = new int[size];
            String[] cells = problemLines.get(i + startLine).split(" ");
            for (int j = 0; j < size; j++){
                int element = Integer.parseInt(cells[j]);
                map[i][j] = element;
            }
        }
        return map;
    }

    // todo: wx
    @Override
    public Frontier getFrontier(EvaluationType type) {
        return null;
    }

    // todo: wx
    @Override
    public Predictor getPredictor(HeuristicType type) {
        return null;
    }
}
