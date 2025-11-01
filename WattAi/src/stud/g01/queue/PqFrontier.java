package stud.g01.queue;

import core.solver.queue.Frontier;
import core.solver.queue.Node;
import core.solver.queue.EvaluationType;
import stud.g01.utils.NpuzzleHash;
import core.problem.State;

import java.util.Comparator;
import java.util.PriorityQueue;

public class PqFrontier extends PriorityQueue<Node> implements Frontier {

    private final NpuzzleHash hashBook;
    private final EvaluationType type;

    public PqFrontier(EvaluationType type) {
        super(Node.evaluator(type));
        this.type = type;
        this.hashBook = new NpuzzleHash();
    }

    @Override
    public boolean contains(Node node) {
        // 使用正确的方法名
        return hashBook.contains(node.getState().toString());
    }

    @Override
    public boolean offer(Node node) {
        String stateStr = node.getState().toString();
        int currentCost = node.getPathCost();

        // 使用 checkAndUpdate 方法检查并更新哈希表
        boolean shouldSkip = hashBook.checkAndUpdate(stateStr, currentCost);

        if (shouldSkip) {
            // 已存在更优路径，跳过此节点
            return false;
        }

        // 如果队列中已存在相同状态的节点，需要移除旧的
        removeExistingNode(node.getState());

        // 插入新节点
        return super.offer(node);
    }

    /**
     * 移除队列中指定状态的节点
     */
    private void removeExistingNode(State state) {
        String targetStateStr = state.toString();
        this.removeIf(currentNode ->
                currentNode.getState().toString().equals(targetStateStr)
        );
    }

    /**
     * 获取评估类型
     */
    public EvaluationType getEvaluationType() {
        return type;
    }
//调试
//    @Override
//    public String toString() {
//        return "PqFrontier{" +
//                "type=" + type +
//                ", size=" + this.size() +
//                ", exploredStates=" + hashBook.size() +
//                '}';
//    }
}