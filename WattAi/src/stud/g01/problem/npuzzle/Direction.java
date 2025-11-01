package stud.g01.problem.npuzzle;

import java.util.EnumMap;
import java.util.List;

/**
 * 
 * 地图中可以移动的8个方向，及其箭头符号
 */
public enum Direction {
    N('↑'),  //北
    E('→'),  //东
    S('↓'),  //南
    W('←');  //西

    private final char symbol;

    /**
     * 构造函数
     * @param symbol 枚举项的代表符号--箭头
     */
    Direction(char symbol){
        this.symbol = symbol;
    }

    public char symbol(){
        return symbol;
    }

    /**
     * 移动方向的4个方向。
     */
    public static final List<Direction> FOUR_DIRECTIONS = List.of(Direction.values());
    
    /**
     * 不同方向的耗散值
     */
    public static int cost(Direction dir){
        return 1;
    }

    //各个方向移动的坐标位移量，{0, 0} 在左上角
    private static final EnumMap<Direction, int[]> DIRECTION_OFFSET = new EnumMap<>(Direction.class);
    static{
        //列号（或横坐标）增加量；行号（或纵坐标）增加量
        DIRECTION_OFFSET.put(N, new int[]{0, -1});
        DIRECTION_OFFSET.put(E, new int[]{1, 0});
        DIRECTION_OFFSET.put(S, new int[]{0, 1});
        DIRECTION_OFFSET.put(W, new int[]{-1, 0});
    }
    
    public static int[] offset(Direction dir){
        return DIRECTION_OFFSET.get(dir);
    }
}
