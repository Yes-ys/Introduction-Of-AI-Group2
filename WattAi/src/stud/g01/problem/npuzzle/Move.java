package stud.g01.problem.npuzzle;

import core.problem.Action;
import stud.g01.problem.npuzzle.Direction;

/**
 *
 * ���п���ƶ���������NSEW�ĸ�ѡ��.
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

    // ���� direction ������cost
    @Override
    public int stepCost() {return Direction.cost(direction);}

    // ���ص�ǰ direction �Ķ��������ƣ�N, E, S, W
    @Override
    public String toString() {
        return direction.name();
    }

    @Override
    public boolean equals(Object obj) {
        // obj �͵�ǰ������ͬһ������
        if (obj == this) return true;

        // ���Խ� obj ǿ��ת��Ϊ Move ���󣬱Ƚ����ǵ������Ƿ���ͬ
        if (obj instanceof Move another) {
            //����Node�����״̬��ͬ������Ϊ����ͬ��
            return this.direction.equals(another.direction);
        }
        return false;
    }
}
