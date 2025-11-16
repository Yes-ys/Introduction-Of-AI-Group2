package stud.g01.utils;

import stud.g01.pdb.SQLitePDB;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PreProcess {

    private final int size = 4; // 4x4 棋盘
    private final int k = 5;    // 每个模式选择5个位置
    private final int numSubmodel = 3; // 子模式数量
    private final int N = 16; // 总元素个数

    private final int[][] targetPos = new int[numSubmodel][k * 2]; // 存储每个子模式的目标位置坐标

    private final int[][] manhattan = new int[16][16]; // 位置到目标位置曼哈顿距离缓存

    public PreProcess() {
        // 初始化子模式目标坐标 (x,y) 一维存储为 x,y交替
        // 子模式1 (1~5)
        targetPos[0] = new int[]{0,0, 0,1, 0,2, 0,3, 1,0};
        // 子模式2 (6~10)
        targetPos[1] = new int[]{1,1, 1,2, 1,3, 2,0, 2,1};
        // 子模式3 (11~15)
        targetPos[2] = new int[]{2,2, 2,3, 3,0, 3,1, 3,2};

        // 预计算所有位置到所有目标位置的曼哈顿距离
        for (int pos = 0; pos < 16; pos++) {
            int x = pos / size;
            int y = pos % size;
            for (int target = 0; target < 16; target++) {
                int tx = target / size;
                int ty = target % size;
                manhattan[pos][target] = Math.abs(x - tx) + Math.abs(y - ty);
            }
        }
    }

    // 计算子模式成本
    private int computeCost(int submodelNo, int[] pattern) {
        int cost = 0;
        int[] targets = targetPos[submodelNo];
        for (int i = 0; i < pattern.length; i++) {
            int pos = pattern[i];
            int tx = targets[i*2];
            int ty = targets[i*2+1];
            int targetIndex = tx * size + ty;
            cost += manhattan[pos][targetIndex];
        }
        return cost;
    }

    // DFS 生成组合
    private void dfs(int start, int depth, List<Integer> path, List<int[]> result, boolean[] used) {
        if (depth == k) {
            int[] pattern = path.stream().mapToInt(Integer::intValue).toArray();
            result.add(pattern);
            return;
        }
        for (int i = 0; i < N; i++) {
            if (!used[i]) {
                path.add(i);
                used[i] = true;
                dfs(i + 1, depth + 1, path, result, used);
                path.removeLast();
                used[i] = false;
            }
        }
    }

    // 批量插入数据库
    public void generatePatterns(SQLitePDB pdb, Logger LOGGER) {
        List<int[]> patterns = new ArrayList<>();
        boolean[] used = new boolean[size * size];
        dfs(0, 0, new ArrayList<>(), patterns, used);
        LOGGER.info("组合生成完成，总数：" + patterns.size());

        try {
            pdb.open();
            pdb.beginTransaction();
            for (int[] pattern : patterns) {
                for (int sub = 0; sub < numSubmodel; sub++) {
                    int cost = computeCost(sub, pattern);
                    String key = Arrays.toString(pattern);
                    pdb.InsertPatternId(sub + 1, key, cost);
                }
            }
            pdb.commit();
            LOGGER.info("数据库插入完成");
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "数据库操作失败", e);
        } finally {
            pdb.close();
        }
    }

    // 主函数
    public static void main(String[] args) {
        PreProcess pre = new PreProcess();
        Logger LOGGER = Logger.getLogger(PreProcess.class.getName());
        String dbFile = "data.db";
        int cacheSize = 1000;

        SQLitePDB pdb = new SQLitePDB(dbFile, cacheSize);
//        pre.generatePatterns(pdb, LOGGER);
        try {
            pdb.open();

            int patternId = 1;
            String key = "[4, 3, 2, 1, 0]";

            if (pdb.hasKey(patternId, key)) {
                Integer storedCost = pdb.getCost(patternId, key);
                System.out.println("查找成功！模式ID=" + patternId + ", key=" + key + ", cost=" + storedCost);
            } else {
                System.out.println("查找失败，模式不存在");
            }

        } catch (Exception e) {
            pdb.rollback();
            LOGGER.log(Level.SEVERE, "数据库操作失败", e);
        } finally {
            pdb.close();
        }
    }
}
