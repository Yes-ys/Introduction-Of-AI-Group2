package stud.g01.pdb;

import java.sql.*;
import java.util.*;

/**
 * 单表实现：entries(pattern_id, key, cost)
 * pattern_id: 模式 ID
 * key: 状态的字符串表示
 * cost: 启发值
 * 主键 (pattern_id, key)
 * 调用时需要传入数据库文件路径和缓存容量
 */
public final class SQLitePDB implements AutoCloseable {
    private final String dbPath;
    private final int cacheCapacity;

    // 单一连接
    private Connection conn;

    // 线程安全的简单缓存（使用 synchronized(map) 在需要迭代时保护）
    private final Map<String, Integer> cache = Collections.synchronizedMap(new HashMap<>());

    // 超过 capacity * TRIM_FACTOR 时触发修剪
    private static final double TRIM_FACTOR = 1.2;

    public SQLitePDB(String dbPath, int cacheCapacity) {
        this.dbPath = Objects.requireNonNull(dbPath, "dbPath 不能为空");
        this.cacheCapacity = Math.max(1024, cacheCapacity);
    }

    private Connection createConnection() throws SQLException {
        String url = "jdbc:sqlite:" + dbPath;
        Connection c = DriverManager.getConnection(url);
        try (Statement s = c.createStatement()) {
            s.execute("PRAGMA journal_mode=WAL;");
            s.execute("PRAGMA synchronous=NORMAL;");
            s.execute("PRAGMA temp_store=MEMORY;");
            s.execute("PRAGMA busy_timeout=5000;");
        }
        return c;
    }

    public synchronized void open() throws SQLException {
        if (conn != null && !conn.isClosed()) return;
        conn = createConnection();
        ensureSchema(conn);
    }

    private void ensureSchema(Connection c) throws SQLException {
        try (Statement s = c.createStatement()) {
            s.execute("CREATE TABLE IF NOT EXISTS entries (" +
                    "pattern_id INTEGER NOT NULL, " +
                    "key TEXT NOT NULL, " +
                    "cost INTEGER NOT NULL, " +
                    "PRIMARY KEY(pattern_id, key));");
        }
    }

    @Override
    public synchronized void close() {
        if (conn != null) {
            try { conn.close(); } catch (SQLException ignored) {}
            conn = null;
        }
        synchronized (cache) {
            cache.clear();
        }
    }

    private Connection conn() {
        return conn;
    }

    private String cacheKey(int patternId, String key) {
        return patternId + "|" + key;
    }

    /**
     * 从 cache 读取；未命中则查询 DB 并写回 cache
     */
    public Integer getCost(int patternId, String key) throws SQLException {
        String ckey = cacheKey(patternId, key);
        Integer cached;
        synchronized (cache) {
            cached = cache.get(ckey);
        }
        if (cached != null) return cached;

        try (PreparedStatement ps = conn().prepareStatement(
                "SELECT cost FROM entries WHERE pattern_id = ? AND key = ?")) {
            ps.setInt(1, patternId);
            ps.setString(2, key);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int cost = rs.getInt(1);
                    putCacheIfNeeded(ckey, cost);
                    return cost;
                } else {
                    return null; // 未找到
                }
            }
        }
    }

    /**
     * 插入或更新 cost（使用 SQLite UPSERT），并更新 cache
     */
    public void InsertPatternId(int patternId, String key, int cost) throws SQLException {
        try (PreparedStatement ps = conn().prepareStatement(
                "INSERT INTO entries (pattern_id, key, cost) VALUES (?, ?, ?)" +
                        " ON CONFLICT(pattern_id, key) DO UPDATE SET cost = excluded.cost")) {
            ps.setInt(1, patternId);
            ps.setString(2, key);
            ps.setInt(3, cost);
            ps.executeUpdate();
        }
        putCacheIfNeeded(cacheKey(patternId, key), cost);
    }

    /**
     * 检查是否存在某个 key（复用 getCost）
     */
    public boolean hasKey(int patternId, String key) throws SQLException {
        return getCost(patternId, key) != null;
    }



    /**
     * 将值写入 cache；超过阈值时触发修剪
     */
    private void putCacheIfNeeded(String key, int value) {
        synchronized (cache) {
            cache.put(key, value);
            if (cache.size() > (int) (cacheCapacity * TRIM_FACTOR)) {
                trimCache();
            }
        }
    }

    /**
     * 简单修剪：迭代删除部分键直到回到目标容量
     * 注意：在调用此方法时 caller 应已持有 cache 的监视器
     */
    private void trimCache() {
        int target = cacheCapacity;
        Iterator<String> it = cache.keySet().iterator();
        while (cache.size() > target && it.hasNext()) {
            it.next();
            it.remove();
        }
    }
}
