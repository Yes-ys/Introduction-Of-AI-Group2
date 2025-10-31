package stud.g01.queue;

import core.solver.queue.Frontier;
import core.solver.queue.Node;
import stud.queue.QueueFrontier;
import stud.g01.utils.NpuzzleHash;
import stud.g01.problem.npuzzle.PuzzleBoard;

import java.util.PriorityQueue;

public class PqFrontier extends PriorityQueue<Node> implements Frontier {

    private final NpuzzleHash HashBook;

    public PqFrontier(){
        HashBook = new NpuzzleHash();
    }

    @Override
    public boolean contains(Node node) {
        return HashBook.check(node.getState().toString());
    }

    @Override
    public boolean offer(Node node) {
        return false;
    }
}
