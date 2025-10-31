package core.solver.queue;

import stud.g01.utils.NpuzzleHash;
import core.problem.State;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * ʹ�ù�ϣȥ�ص�Frontierʵ��
 */
public class HashFrontier implements Frontier {
    private final PriorityQueue<Node> priorityQueue;
    private final NpuzzleHash stateHash;
    private final EvaluationType type;
    private final Comparator<Node> comparator;

    public HashFrontier(EvaluationType type) {
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
     * ��ȡ��̽����״̬����
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

        // ��鲢���¹�ϣ��
        boolean shouldSkip = stateHash.checkAndUpdate(stateStr, currentCost);

        if (shouldSkip) {
            // �Ѵ��ڸ���·���������˽ڵ�
            return false;
        }

        // ����������Ѵ�����ͬ״̬�Ľڵ㣬��Ҫ�Ƴ��ɵ�
        removeExistingNode(node.getState());

        // �����½ڵ�
        return priorityQueue.offer(node);
    }

    /**
     * �Ƴ�������ָ��״̬�Ľڵ�
     */
    private void removeExistingNode(State state) {
        String targetStateStr = state.toString();

        // ֱ��ʹ�÷������ã���������ľֲ�����
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