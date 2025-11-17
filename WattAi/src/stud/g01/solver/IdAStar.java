package stud.g01.solver;

import algs4.util.StopwatchCPU;
import core.problem.Problem;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.algorithm.searcher.AbstractSearcher;
import core.solver.queue.Frontier;
import core.solver.queue.Node;

import java.sql.SQLException;
import java.util.Deque;
import java.util.List;
import java.util.Stack;

/**
 * IdAStar算法，基于A*的迭代加深搜索。
 * 通过设定估值剪枝阈值进行深度优先搜索，每轮迭代增加剪枝阈值。
 */
public class IdAStar extends AbstractSearcher {

    private final Predictor predictor; // 启发式估值函数
    private final Stack<Node> openStack; // 用于DFS的栈
    private int cutoff; // 当前剪枝阈值
    private int maxIteratorDepth = 8192; // 最大迭代深度
    private int newCutoff; // 下一轮迭代的剪枝阈值
    private int expanded; // 已扩展节点计数
    private int generatedNodes; // 生成的节点计数

    /**
     * 构造函数
     *
     * @param frontier 不用在此类中，保留以符合父类的构造签名
     * @param predictor 启发式估值函数
     */
    public IdAStar(Frontier frontier, Predictor predictor) {
        super(frontier);
        this.predictor = predictor;
        this.openStack = new Stack<>(); // 初始化栈
    }

    @Override
    public Deque<Node> search(Problem problem) {
        // 如果问题无解，直接返回null
        if (!problem.solvable()) {
            return null;
        }

        // 初始化栈和计数器
        openStack.clear();
        expanded = 0;
        generatedNodes = 0;

        // 获取根节点并设置初始剪枝阈值
        Node root = problem.root(predictor);
        StopwatchCPU timer1 = new StopwatchCPU();
        cutoff = root.evaluation(); // 初始状态的 f = g + h
//        double time1 = timer1.elapsedTime();
//        System.out.println("执行了" + time1 + "s");

        // 迭代搜索，直到达到最大深度或找到解
        while (cutoff < maxIteratorDepth) {
            // 每一轮都从起点开始重新 DFS 搜索
            openStack.push(root); // 根节点入栈
            newCutoff = Integer.MAX_VALUE; // 重置新阈值

            // 带剪枝的DFS搜索
            while (!openStack.isEmpty()) {
                Node node = openStack.pop();
                expanded++;

                // 如果找到目标节点，返回路径
                if (problem.goal(node.getState())) {
                    return generatePath(node);
                }

                // 扩展当前节点并统计生成的节点数
                List<Node> children = problem.childNodes(node, predictor);
                generatedNodes += children.size();

                for (Node child : children) {
                    int childEval = child.evaluation();
                    if (childEval <= cutoff) { // 若子节点在当前阈值内
                        // 防止回溯到父节点
                        if (node.getParent() == null || !node.getParent().equals(child)) {
                            openStack.push(child);
                        }
                    } else { // 更新新阈值
                        newCutoff = Math.min(childEval, newCutoff);
                    }
                }
            }
            // 更新剪枝阈值
            cutoff = newCutoff;
        }
        // 若达到最大深度且无解，返回null
        return null;
    }

    @Override
    public int nodesExpanded() {
        return expanded; // 返回扩展节点数
    }

    @Override
    public int nodesGenerated() {
        return generatedNodes; // 返回生成的节点数
    }
}
