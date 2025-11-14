package stud.g01.utils;

import stud.g01.pdb.SQLitePDB;

import java.util.*;

public class PatternDBBuilder {

    private static final int SIZE = 4;
    private static final int K = 5; // 每个 pattern tile 数量
    private final int[][] PATTERNS = {
            {1,2,3,4,5},
            {6,7,8,9,10},
            {11,12,13,14,15}
    };

    // 每个格子可移动到的相邻位置（不考虑 0）
    private static final int[][] ADJ = {
            {1,4},{0,2,5},{1,3,6},{2,7},
            {0,5,8},{1,4,6,9},{2,5,7,10},{3,6,11},
            {4,9,12},{5,8,10,13},{6,9,11,14},{7,10,15},
            {8,13},{9,12,14},{10,13,15},{11,14}
    };

    // ----------------------------
    // 公共接口
    // ----------------------------
    public void buildAll(SQLitePDB pdb) throws Exception {
        pdb.open();
        pdb.beginTransaction();

        for (int pid = 1; pid <= 3; pid++) {
            buildOne(pid, pdb);
        }

        pdb.commit();
        pdb.close();
    }

    // ----------------------------
    // 构建单个 pattern（压缩状态 BFS）
    // ----------------------------
    private void buildOne(int patternId, SQLitePDB pdb) throws Exception {
        int[] tiles = PATTERNS[patternId - 1];

        // 生成目标状态（只记录 pattern tile 的位置）
        int[] goal = new int[K];
        for (int i = 0; i < K; i++) goal[i] = tiles[i] - 1; // 0-based

        Queue<int[]> queue = new ArrayDeque<>();
        Map<Long,Integer> dist = new HashMap<>();

        long code = encode(goal);
        queue.add(goal.clone());
        dist.put(code, 0);

        while (!queue.isEmpty()) {
            int[] cur = queue.poll();
            int d = dist.get(encode(cur));

            // BFS 扩展：每个 tile 可与上下左右交换
            for (int i = 0; i < K; i++) {
                int pos = cur[i];
                for (int nxt : ADJ[pos]) {
                    // 构造新的 tile 位置数组
                    int[] next = cur.clone();
                    boolean occupied = false;
                    // 检查 nxt 是否已经被其他 tile 占用
                    for (int j = 0; j < K; j++) {
                        if (next[j] == nxt) {
                            occupied = true;
                            break;
                        }
                    }
                    if (occupied) continue;

                    next[i] = nxt;
                    long nextCode = encode(next);
                    if (!dist.containsKey(nextCode)) {
                        dist.put(nextCode, d + 1);
                        queue.add(next);
                    }
                }
            }
        }

        System.out.println("Pattern " + patternId + " 完成，状态数=" + dist.size());

        // BFS 完成后统一写入数据库
        int batchCount = 0;
        for (Map.Entry<Long,Integer> entry : dist.entrySet()) {
            int[] patternPos = decode(entry.getKey());
            String key = Arrays.toString(patternPos);
            int cost = entry.getValue();

            pdb.InsertPatternId(patternId, key, cost);
            batchCount++;
            if (batchCount % 5000 == 0) {
                pdb.commit();
                pdb.beginTransaction();
            }
        }
        pdb.commit();
    }

    // ----------------------------
    // 状态压缩为 long：只编码 pattern tile 位置
    // 每个位置 4 bit，可编码 0~15
    // ----------------------------
    private long encode(int[] pos) {
        long code = 0;
        for (int p : pos) {
            code <<= 4;
            code |= p;
        }
        return code;
    }

    // 从 long 解码回 tile 位置
    private int[] decode(long code) {
        int[] pos = new int[K];
        for (int i = K - 1; i >= 0; i--) {
            pos[i] = (int)(code & 0xF);
            code >>= 4;
        }
        return pos;
    }
}
