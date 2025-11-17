package stud.g01.utils;

import stud.g01.pdb.SQLitePDB;

import java.util.*;

public class PatternDBBuilder {
    // 移动增量，用于向四个方向移动
    public final static int[] dx = {0, 1, 0, -1};
    public final static int[] dy = {1, 0, -1, 0};

    private static final int[] SIZE = {0, 6, 6, 3};
    private final int[][] PATTERNS = {
            {1, 5, 6, 9, 10, 13},
            {7, 8, 11, 12, 14, 15},
            {2, 3, 4}
    };

    // 每个格子可移动到的相邻位置（不考虑 0）
    private static final int[][] ADJ = {
            {1,4},{0,2,5},{1,3,6},{2,7},
            {0,5,8},{1,4,6,9},{2,5,7,10},{3,6,11},
            {4,9,12},{5,8,10,13},{6,9,11,14},{7,10,15},
            {8,13},{9,12,14},{10,13,15},{11,14}
    };

    public void buildAll(SQLitePDB pdb) throws Exception {
        pdb.open();
        pdb.beginTransaction();

        for (int pid = 1; pid <= 3; pid++) {
            buildOne(pid, pdb);
        }

        pdb.commit();
        pdb.close();
    }

    // 当前子问题模式 - pattenId，数据库对象 - pdb
    private void buildOne(int patternId, SQLitePDB pdb) throws Exception {
        int[] tiles = PATTERNS[patternId - 1];
        int K = SIZE[patternId];
        // 生成目标状态（只记录 pattern tile 的位置）
        int[] goal = new int[K + 1];
        for (int i = 0; i < K; i++) goal[i] = tiles[i] - 1; // 0-based
        goal[K] = 15; // 空格初始在右下角

        Deque<int[]> queue = new LinkedList<>();
        Map<Long,Integer> dist = new HashMap<>();

        long code = encode(goal);
        queue.addLast(goal.clone());
        dist.put(code, 0);

        int epoch = 0;
        while (!queue.isEmpty()) {
            int[] cur = queue.removeFirst();
            long curCode = encode(cur);
            int curDist = dist.get(curCode);
            epoch++;
            if (epoch % 1000000 == 0)
                System.out.println("Processing epoch: " + epoch);

            int zeroPos = cur[K]; // 空格的位置（0-15）

            // 尝试将空格移动到 4 个方向
            for (int d = 0; d < 4; d++) {
                // 计算空格新位置
                int zx = zeroPos / 4;
                int zy = zeroPos % 4;
                int nx = zx + dx[d];
                int ny = zy + dy[d];

                // 边界检查
                if (nx < 0 || nx >= 4 || ny < 0 || ny >= 4) continue;

                int newZeroPos = nx * 4 + ny;

                // 检查新位置是否是 pattern 中的 tile
                int tileIndex = -1;
                for (int i = 0; i < K; i++) {
                    if (cur[i] == newZeroPos) {
                        tileIndex = i;
                        break;
                    }
                }

                // 生成新状态
                int[] next = cur.clone();
                int stepCost;

                if (tileIndex != -1) {
                    // 空格移动到 pattern tile：交换位置，cost = 1
                    next[tileIndex] = zeroPos;
                    stepCost = 1;
                } else {
                    // 空格移动到非 pattern tile：只更新空格位置，cost = 0
                    stepCost = 0;
                }
                next[K] = newZeroPos;

                long nextCode = encode(next);
                int nextDist = curDist + stepCost;

                // 如果未访问过或找到更短路径，更新距离
                if (!dist.containsKey(nextCode) || dist.get(nextCode) > nextDist) {
                    dist.put(nextCode, nextDist);

                    // 0-1 BFS: cost=0 放队首，cost=1 放队尾
                    if (stepCost == 0) {
                        queue.addFirst(next);
                    } else {
                        queue.addLast(next);
                    }
                }
            }
        }

        System.out.println("Pattern " + patternId + " 完成，状态数=" + dist.size());

        Map<Long,Integer> true_dist = new HashMap<>();
        for (Map.Entry<Long,Integer> entry : dist.entrySet()) {
            long state = entry.getKey() >> 4;
            int cost = entry.getValue();
            true_dist.put(state, true_dist.containsKey(state) ? Math.min(cost, true_dist.get(state)) : cost);
        }

        System.out.println("准备完成。");
        // BFS 完成后统一写入数据库
        int batchCount = 0;
        for (Map.Entry<Long,Integer> entry : true_dist.entrySet()) {
            long fullCode = entry.getKey();
            String key = String.valueOf(fullCode);
            int cost = entry.getValue();

            pdb.InsertPatternId(patternId, key, cost);
            batchCount++;
            if (batchCount % 5000 == 0) {
                System.out.println("Epoch " + batchCount / 5000 + " completed");
                pdb.commit();
                pdb.beginTransaction();
            }
        }
        pdb.commit();
    }

    public static long encode(int[] pos) {
        long code = 0;
        for (int p : pos) {
            code <<= 4;
            code |= p;
        }
        return code;
    }

    // 从 long 解码回 tile 位置
    public static int[] decode(long code, int K) {
        int[] pos = new int[K];
        for (int i = K - 1; i >= 0; i--) {
            pos[i] = (int)(code & 0xF);
            code >>= 4;
        }
        return pos;
    }
}
