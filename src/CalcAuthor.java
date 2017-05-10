import java.sql.*;

/**
 * Created by anderson on 17-5-7.
 */
public class CalcAuthor {
    static String url = "jdbc:mysql://0.0.0.0:3306/ahaulib?"
            + "user=root&password=anderson&useUnicode=true&characterEncoding=UTF8";
    static Connection conn;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            conn = DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String args[]) throws SQLException {
        String queryBook = "select marc_no,book_title from all_booksid_with_title GROUP BY marc_no";
        PreparedStatement bookStmt = conn.prepareStatement(queryBook);
        ResultSet booksResult = bookStmt.executeQuery();
        String title, marc_no, author;
        String[] tmp;
        String updateSql = "update student_borrow_record set author=? where marc_no=?";
        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
        int count = 0;
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        while (booksResult.next()) {
            title = booksResult.getString("book_title");
            marc_no = booksResult.getString("marc_no");
            tmp = title.split("/");
            if (tmp.length > 1) {
                author = tmp[1];
                updateStmt.setString(1, author);
                updateStmt.setString(2, marc_no);
//                updateStmt.executeUpdate();
                updateStmt.addBatch();
                System.out.println(author);
            } else {
                System.out.println("title: " + title);
            }
            if (count%50000 == 0) {
                System.out.println("执行插入数据...");
                Long beginTime = System.currentTimeMillis();
                int[] result = updateStmt.executeBatch();
                conn.commit();
                updateStmt.clearBatch();
                Long stopTime = System.currentTimeMillis();
                System.out.println("执行了" + result.length + "条Sql语句，耗时" + (stopTime - beginTime) + "毫秒...");
            }
            count ++;
            System.out.println(count);
        }

        System.out.println("执行插入数据...");
        Long beginTime = System.currentTimeMillis();
        int[] result = updateStmt.executeBatch();
        conn.commit();
        updateStmt.clearBatch();
        Long stopTime = System.currentTimeMillis();
        System.out.println("执行了" + result.length + "条Sql语句，耗时" + (stopTime - beginTime) + "毫秒...");
    }
}
