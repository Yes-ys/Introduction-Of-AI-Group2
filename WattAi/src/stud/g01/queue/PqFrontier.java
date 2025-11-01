package stud.g01.queue;

import core.solver.queue.Frontier;
import core.solver.queue.Node;
import core.solver.queue.EvaluationType;

import stud.g01.utils.NpuzzleHash;
import core.problem.State;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * 使用哈希去重的Frontier实现
 */
public class PqFrontier implements Frontier {
    private final PriorityQueue<Node> priorityQueue;
    private final NpuzzleHash stateHash;
    private final EvaluationType type;
    private final Comparator<Node> comparator;

    public PqFrontier(EvaluationType type) {
        this.type = type;
        this.comparator = Node.evaluator(type);
        this.priorityQueue = new PriorityQueue<>(comparator);
        this.stateHash = new NpuzzleHash();
    }

    @Override
    public Node poll() {
        return priorityQueue.poll();
    }

    @Override
    public void clear() {
        priorityQueue.clear();
        stateHash.clear();
    }

    @Override
    public int size() {
        return priorityQueue.size();
    }

    /**
     * 获取已探索的状态数量
     */
    public int getExploredSize() {
        return stateHash.size();
    }

    @Override
    public boolean isEmpty() {
        return priorityQueue.isEmpty();
    }

    @Override
    public boolean contains(Node node) {
        String stateStr = node.getState().toString();
        return stateHash.contains(stateStr);
    }

    @Override
    public boolean offer(Node node) {
        String stateStr = node.getState().toString();
        int currentCost = node.getPathCost();

        // 检查并更新哈希表
        boolean shouldSkip = stateHash.checkAndUpdate(stateStr, currentCost);

        if (shouldSkip) {
            // 已存在更优路径，跳过此节点
            return false;
        }

        // 如果队列中已存在相同状态的节点，需要移除旧的
        removeExistingNode(node.getState());

        // 插入新节点
        return priorityQueue.offer(node);
    }

    /**
     * 移除队列中指定状态的节点
     */
    private void removeExistingNode(State state) {
        String targetStateStr = state.toString();

        // 直接使用方法引用，避免冗余的局部变量
        priorityQueue.removeIf(currentNode ->
                currentNode.getState().toString().equals(targetStateStr)
        );
    }

    @Override
    public String toString() {
        return "HashFrontier{" +
                "type=" + type +
                ", queueSize=" + priorityQueue.size() +
                ", exploredStates=" + stateHash.size() +
                '}';
    }
}