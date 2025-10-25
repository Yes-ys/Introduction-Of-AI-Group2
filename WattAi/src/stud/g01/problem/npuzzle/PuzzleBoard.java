package stud.g01.problem.npuzzle;

import core.problem.Action;
import core.problem.State;

public class PuzzleBoard extends State {
    @Override
    public void draw() { // 在Console上，输出该状态

    }

    @Override
    public State next(Action action) { // 当前状态下，采用某个action而进入的下一个状态
        return null;
    }

    @Override
    public Iterable<? extends Action> actions() { // 当前状态下所有可能的状态
        return null;
    }
}
