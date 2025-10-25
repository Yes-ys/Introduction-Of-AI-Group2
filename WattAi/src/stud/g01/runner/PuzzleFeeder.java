package stud.g01.runner;

import core.problem.Problem;
import core.runner.EngineFeeder;
import core.solver.algorithm.heuristic.HeuristicType;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.queue.EvaluationType;
import core.solver.queue.Frontier;
import stud.g01.problem.npuzzle.NPuzzleProblem;

import java.util.ArrayList;
//Fix Me   //Fix Me
public class PuzzleFeeder extends EngineFeeder {
    @Override
    public ArrayList<Problem> getProblems(ArrayList<String> problemLines) {
        int size = Integer.parseInt(problemLines.get(0));
        int[][] map = getMap(problemLines, size);

        ArrayList<Problem> problems = new ArrayList<>();
        NPuzzleProblem problem = getNPuzzleProblem(size, map);
        problems.add(problem);
        return problems;
    }

    public NPuzzleProblem getNPuzzleProblem(int size, int[][] map) {return null;}

    public int[][] getMap(ArrayList<String> problemLines, int size) {return null;}
    @Override
    public Frontier getFrontier(EvaluationType type) {
        return null;
    }

    @Override
    public Predictor getPredictor(HeuristicType type) {
        return null;
    }
}
