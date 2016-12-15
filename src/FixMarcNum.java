import java.sql.*;

/**
 * Created by anderson on 2016/12/14.
 */
public class FixMarcNum {
    static String url = "jdbc:mysql://localhost:3306/ahaulib?"
            + "user=root&password=anderson&useUnicode=true&characterEncoding=UTF8";
    static Connection conn;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }
    public static void main(String args[]) {
        String getAllSql = "select DISTINCT book_id from test_borrow_book_record where marc_no=?";
        String bookId;
        try {
            PreparedStatement getAllStmt = conn.prepareStatement(getAllSql);
            getAllStmt.setString(1, "");
            ResultSet allBookIds = getAllStmt.executeQuery();
            int bookCount = 0;
            int marcCount = 0;
            String updateSql = "update test_borrow_book_record set marc_no=? " +
                    "where book_id =?";
            System.out.println(updateSql);
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            while (allBookIds.next()){
                bookCount++;
                System.out.println("修复第" + bookCount + "条数据...");
                bookId = allBookIds.getString("book_id");

                String getMarcSql = "select marc_no from book_marc_id where book_id=?";
                PreparedStatement getMarcStmt = conn.prepareStatement(getMarcSql);
                getMarcStmt.setString(1, bookId);
                ResultSet marcResult = getMarcStmt.executeQuery();
                while (marcResult.next()) {
                    String marcNum = marcResult.getString("marc_no");
                    if (marcNum.isEmpty()){
                        System.out.println("找不到MarcNum,跳过...");
                        continue;
                    } else {
                        marcCount++;
                        System.out.println("找到了第" + marcCount + "本书的MarcNum... ");
                    }
                    updateStmt.setString(1, marcNum);
                    updateStmt.setString(2, bookId);
                    updateStmt.addBatch();
                    if (marcCount%500==0) {
                        System.out.println("开始执行更新Sql");
                        long beginTime = System.currentTimeMillis();
                        int[] result = updateStmt.executeBatch();
                        conn.commit();
                        updateStmt.clearBatch();
                        long stopTime = System.currentTimeMillis();
                        System.out.println("修复第" + bookCount + "本书的MarcNum，执行了" + result.length + "条Sql语句，" +
                                "耗时" + (stopTime - beginTime) + "毫秒...");
                    }
                }
            }
            System.out.println("开始执行更新Sql");
            long beginTime = System.currentTimeMillis();
            int[] result = updateStmt.executeBatch();
            conn.commit();
            updateStmt.clearBatch();
            long stopTime = System.currentTimeMillis();
            System.out.println("修复第" + bookCount + "本书的MarcNum，执行了" + result.length + "条Sql语句，" +
                    "耗时" + (stopTime - beginTime) + "毫秒...");
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
