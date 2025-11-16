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
        Node nd;
        while(true){
            nd = priorityQueue.poll();
            assert nd != null;
            String stateStr = nd.getState().toString();
            if (stateHash.getCost(stateStr) == nd.getPathCost()) break;
        }
        return nd;
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

    // 试图插入新 node，返回是否更新了队列
    @Override
    public boolean offer(Node node) {
        String stateStr = node.getState().toString();
        int currentCost = node.getPathCost();
        // 检查并更新哈希表，返回是否更新frontier
        boolean updated = stateHash.checkAndUpdate(stateStr, currentCost);

        // 插入新节点
        if (updated)
            return priorityQueue.offer(node);
        else return false;
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