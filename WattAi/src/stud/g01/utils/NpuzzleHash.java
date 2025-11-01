package stud.g01.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * 改进的NpuzzleHash类，支持存储节点和快速查找
 */
public class NpuzzleHash {
    private final Map<String, Boolean> visitedMap;// 闭表
    private final Map<String, Integer> costMap; // 存储frontier中Node的状态对应的最小路径成本

    public NpuzzleHash() {
        visitedMap = new HashMap<>();
        costMap = new HashMap<>();
    }

    /**
     * 检查状态是否已访问过，并记录访问，返回是否没有进行更改
     * @param boardStr 状态字符串
     * @param pathCost 当前路径成本
     * @return true-已访问过且有更优路径, false-未访问过或需要更新
     */
    public boolean checkAndUpdate(String boardStr, int pathCost) {
        if(costMap.containsKey(boardStr)){
            int pre_cost = costMap.get(boardStr);
            if(pre_cost > pathCost){
                costMap.put(boardStr,pathCost);
            }else return true;
        }
        else{
            costMap.put(boardStr,pathCost);
        }
        return false;
    }

    /**
     * 检查状态是否已访问过
     */
    public boolean contains(String boardStr) {
        return visitedMap.containsKey(boardStr);
    }

    /**
     * 获取状态的最小路径成本
     */
    public int getCost(String boardStr) {
        return costMap.getOrDefault(boardStr, Integer.MAX_VALUE);
    }

    /**
     * 清空哈希表
     */
    public void clear() {
        visitedMap.clear();
        costMap.clear();
    }

    /**
     * 获取已访问状态数量
     */
    public int size() {
        return visitedMap.size();
    }

    public void visit(String str){
        visitedMap.put(str,true);
    }

    public void update_costMap(String str,Integer cost){
        costMap.put(str,cost);
    }
}