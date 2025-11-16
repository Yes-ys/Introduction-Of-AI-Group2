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

    private Connection conn;
    private boolean inTransaction = false; // 新增：事务状态

    private final Map<String, Integer> cache = Collections.synchronizedMap(new HashMap<>());
    private static final double TRIM_FACTOR = 1.2;

    public SQLitePDB(String dbPath, int cacheCapacity) {
        this.dbPath = Objects.requireNonNull(dbPath, "dbPath 不能为空");
        this.cacheCapacity = Math.max(1024, cacheCapacity);
    }
    private void handleSQLException(SQLException e) {
        throw new RuntimeException("数据库操作失败: " + e.getMessage(), e);
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

    public synchronized void open()  {
        try {
            if (conn != null && !conn.isClosed()) return;
            conn = createConnection();
            ensureSchema(conn);
        } catch (SQLException e) {
            handleSQLException(e);
        }
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
        synchronized (cache) { cache.clear(); }
    }

    private Connection conn() { return conn; }
    private String cacheKey(int patternId, String key) { return patternId + "|" + key; }

    public Integer getCost(int patternId, String key) {
        String ckey = cacheKey(patternId, key);
        Integer cached;
        synchronized (cache) {
            cached = cache.get(ckey);
        }
        if (cached != null) return cached;
        try {
            try (PreparedStatement ps = conn().prepareStatement(
                    "SELECT cost FROM entries WHERE pattern_id = ? AND key = ?")) {
                ps.setInt(1, patternId);
                ps.setString(2, key);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        int cost = rs.getInt(1);
                        putCacheIfNeeded(ckey, cost);
                        return cost;
                    } else return null;
                }
            }
        }catch (SQLException e) {
            handleSQLException(e);
            return null; // unreachable
        }
    }

    /**
     * 插入或更新 cost（SQLite UPSERT），支持事务
     */
    public void InsertPatternId(int patternId, String key, int cost) {
        try {
            try (PreparedStatement ps = conn().prepareStatement(
                    "INSERT INTO entries (pattern_id, key, cost) VALUES (?, ?, ?)" +
                            " ON CONFLICT(pattern_id, key) DO UPDATE SET cost = excluded.cost")) {
                ps.setInt(1, patternId);
                ps.setString(2, key);
                ps.setInt(3, cost);
                ps.executeUpdate();
            }
        }catch (SQLException e) {handleSQLException(e);}
    }

    public boolean hasKey(int patternId, String key) throws SQLException { return getCost(patternId, key) != null; }

    private void putCacheIfNeeded(String key, int value) {
        synchronized (cache) {
            cache.put(key, value);
            if (cache.size() > (int) (cacheCapacity * TRIM_FACTOR)) trimCache();
        }
    }

    private void trimCache() {
        int target = cacheCapacity;
        Iterator<String> it = cache.keySet().iterator();
        while (cache.size() > target && it.hasNext()) { it.next(); it.remove(); }
    }

    public synchronized void beginTransaction(){
        try {
            if (conn == null) throw new SQLException("数据库未打开");
            if (!inTransaction) {
                conn.setAutoCommit(false);
                inTransaction = true;
            }
        }catch (SQLException e) {handleSQLException(e);}
    }

    public synchronized void commit() {
        try {
            if (conn == null) throw new SQLException("数据库未打开");
            if (inTransaction) {
                conn.commit();
                conn.setAutoCommit(true);
                inTransaction = false;
            }
        }catch (SQLException e) {handleSQLException(e);}
    }

    public synchronized void rollback() {
        try {
            if (conn == null) throw new SQLException("数据库未打开");
            if (inTransaction) {
                conn.rollback();
                conn.setAutoCommit(true);
                inTransaction = false;
            }
        }catch (SQLException e) {handleSQLException(e);}
    }

    /**
     * 查看数据库中的所有条目
     * @return 数据库中的所有条目列表
     * @throws SQLException 如果数据库访问错误
     *
     */
    public List<Map<String, Object>> viewAllEntries() throws SQLException {
        if (conn == null) throw new SQLException("数据库未打开");
        List<Map<String, Object>> entries = new ArrayList<>();
        try (Statement stmt = conn().createStatement();
             ResultSet rs = stmt.executeQuery("SELECT pattern_id, key, cost FROM entries")) {
            while (rs.next()) {
                Map<String, Object> entry = new HashMap<>();
                entry.put("pattern_id", rs.getInt("pattern_id"));
                entry.put("key", rs.getString("key"));
                entry.put("cost", rs.getInt("cost"));
                entries.add(entry);
            }
        }
        return entries;
    }
}
