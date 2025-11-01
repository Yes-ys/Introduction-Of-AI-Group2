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
        // ʹ����ȷ�ķ�����
        return hashBook.contains(node.getState().toString());
    }

    @Override
    public boolean offer(Node node) {
        String stateStr = node.getState().toString();
        int currentCost = node.getPathCost();

        // ʹ�� checkAndUpdate ������鲢���¹�ϣ��
        boolean shouldSkip = hashBook.checkAndUpdate(stateStr, currentCost);

        if (shouldSkip) {
            // �Ѵ��ڸ���·���������˽ڵ�
            return false;
        }

        // ����������Ѵ�����ͬ״̬�Ľڵ㣬��Ҫ�Ƴ��ɵ�
        removeExistingNode(node.getState());

        // �����½ڵ�
        return super.offer(node);
    }

    /**
     * �Ƴ�������ָ��״̬�Ľڵ�
     */
    private void removeExistingNode(State state) {
        String targetStateStr = state.toString();
        this.removeIf(currentNode ->
                currentNode.getState().toString().equals(targetStateStr)
        );
    }

    /**
     * ��ȡ��������
     */
    public EvaluationType getEvaluationType() {
        return type;
    }
//����
//    @Override
//    public String toString() {
//        return "PqFrontier{" +
//                "type=" + type +
//                ", size=" + this.size() +
//                ", exploredStates=" + hashBook.size() +
//                '}';
//    }
}