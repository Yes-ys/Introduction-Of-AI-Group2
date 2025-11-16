package stud.g01.solver;


import java.util.*;

import core.problem.Problem;
import core.problem.State;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.algorithm.searcher.AbstractSearcher;
import core.solver.queue.Frontier;
import core.solver.queue.Node;
import stud.g01.problem.npuzzle.Direction;

public class IdAStar extends AbstractSearcher {

    private static final int[] DX = {0, 1, -1, 0};
    private static final int[] DY = {1, 0, 0, -1};

    private int maxDepth;
    private final Predictor predictor;

    // 显式调用父类构造函数
    public IdAStar(Frontier frontier, Predictor predictor) {
        super(frontier);  // 传递null，因为IDA*不需要Frontier
        this.predictor = predictor;
    }

    @Override
    public Deque<Node> search(Problem problem) {
        String initialState = problem.initialState.toString();
        String GOAL_STATE = problem.goal.toString();Node root = null;
        try {
            root = problem.root(predictor);
        } catch (java.sql.SQLException e) {}

        //System.out.println(root.getState().toString());

        if (initialState.equals(GOAL_STATE)) {
            Deque<Node> result = new ArrayDeque<>();
            result.add(new Node(root.getState(), null, null, 0, 0));
            return result;
        }

        for (maxDepth = 1; ; maxDepth++) {
            Node resultNode = depthLimitedSearch(root, 0, GOAL_STATE, problem);
            if (resultNode != null) {
                return buildPath(resultNode);
            }
        }
    }

    private Node depthLimitedSearch(Node now, int depth, String GOAL_STATE, Problem problem) {
        if (now.getState().toString().equals(GOAL_STATE)) {
            return now;
        }

        if (depth + now.getHeuristic() - 1 > maxDepth) {
            return null;
        }
        //System.out.println(now.getAction());
        try {

            for (Node child : problem.childNodes(now, predictor)) {
                if (now.getParent() != null) {
                    if (child.getState().toString().equals(now.getParent().getState().toString()))
                        continue;
                }
                Node next = depthLimitedSearch(child, depth + 1, GOAL_STATE, problem);
                if (next != null) return next;
            }
        } catch (java.sql.SQLException e) {}

        return null;
    }

    private String swap(String state, int pos1, int pos2) {
        char[] chars = state.toCharArray();
        char temp = chars[pos1];
        chars[pos1] = chars[pos2];
        chars[pos2] = temp;
        return new String(chars);
    }

    private Deque<Node> buildPath(Node goalNode) {
        Deque<Node> path = new ArrayDeque<>();
        Node current = goalNode;

        while (current.getParent() != null) {
            path.addFirst(current);
            current = current.getParent();
        }

        return path;
    }
}