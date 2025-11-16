package stud.g01.utils;
import stud.g01.pdb.SQLitePDB;

public class Main {
    public static void main(String[] args) {
        String dbFile = "data.db";
        int cacheSize = 1000;

        SQLitePDB db = new SQLitePDB(dbFile, cacheSize);

        try {
            db.open();  // main 控制数据库连接
            PatternDBBuilder pre = new PatternDBBuilder();
            pre.buildAll(db);  // buildAll 内部不要重复 open/close
            System.out.println("数据库预处理完成！");
        } catch (Exception e) {
            e.printStackTrace();
            try {
                db.rollback(); // 出错回滚事务
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        } finally {
            db.close();  // 确保数据库关闭
        }
    }
}
