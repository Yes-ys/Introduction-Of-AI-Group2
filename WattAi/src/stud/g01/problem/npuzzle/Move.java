package stud.g01.problem.npuzzle;

import core.problem.Action;
import stud.g01.problem.npuzzle.Direction;

/**
 *
// 空闲块的移动动作，有NSEW四个选择.
 */
public class Move extends Action {

    private final Direction direction;

    public Move(Direction direction) {
        this.direction = direction;
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public void draw() {
        System.out.println(this);
    }

    // 返回 direction 动作的cost
    @Override
    public int stepCost() {return Direction.cost(direction);}

    // 返回当前 direction 的动作的名称：N, E, S, W
    @Override
    public String toString() {
        return direction.name();
    }

    @Override
    public boolean equals(Object obj) {
        // obj 和当前对象是同一个引用
        if (obj == this) return true;

        // 尝试将 obj 强制转化为 Move 对象，比较它们的属性是否相同
        if (obj instanceof Move another) {
            //两个Node对象的状态相同，则认为是相同的
            return this.direction.equals(another.direction);
        }
        return false;
    }
}
