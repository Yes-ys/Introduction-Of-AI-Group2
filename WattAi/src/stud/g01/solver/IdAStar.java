package stud.g01.solver;

import core.problem.Problem;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.algorithm.searcher.AbstractSearcher;
import core.solver.queue.Frontier;
import core.solver.queue.Node;

import java.util.Deque;
import java.util.List;
import java.util.Stack;

/**
 * IDA* 搜索器
 * 使用 f = g + h 的迭代加深 A* 搜索策略
 */
public class IdAStar extends AbstractSearcher {

    private final Predictor predictor;
    private final Stack<Node> dfsStack; // 深度优先搜索栈

    private int currentBound;   // 当前迭代的 f 限制
    private int nextBound;      // 下一次迭代的 f 限制
    private int expandedCount;  // 实际扩展的节点数
    private int generatedCount; // 生成的节点数

    private static final int MAX_F_BOUND = 8192; // 过大无意义，保护程序性能

    public IdAStar(Frontier frontier, Predictor predictor) {
        super(frontier);
        this.predictor = predictor;
        this.dfsStack = new Stack<>();
    }

    @Override
    public Deque<Node> search(Problem problem) {

        if (!problem.solvable()) {
            return null;
        }

        dfsStack.clear();
        expandedCount = 0;
        generatedCount = 0;

        Node root = problem.root(predictor);
        currentBound = root.evaluation(); // f = g + h

        while (currentBound < MAX_F_BOUND) {

            dfsStack.push(root);
            nextBound = Integer.MAX_VALUE;

            while (!dfsStack.isEmpty()) {
                Node node = dfsStack.pop();
                expandedCount++;

                if (problem.goal(node.getState())) {
                    return generatePath(node);
                }

                List<Node> successors = problem.childNodes(node, predictor);
                generatedCount += successors.size();

                final Node parent = node.getParent();

                for (Node succ : successors) {
                    int fSucc = succ.evaluation();

                    // 记录下一轮的最小 f 作为新 bound
                    if (fSucc > currentBound) {
                        nextBound = Math.min(nextBound, fSucc);
                        continue;
                    }

                    // 避免走回头路
                    if (parent != null && succ.equals(parent)) {
                        continue;
                    }

                    dfsStack.push(succ);
                }
            }

            currentBound = nextBound; // 进入下一轮迭代
        }

        return null; // 未找到解
    }

    @Override
    public int nodesExpanded() {
        return expandedCount;
    }

    @Override
    public int nodesGenerated() {
        return generatedCount;
    }
}
