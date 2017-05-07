import java.sql.*;
import java.util.Vector;

/**
 * Created by anderson on 2016/12/13.
 * Calculate book detail tag by marcNo
 * 计算书籍详细类别标签
 */

public class CalcBookDetailTag_V2 {
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
        String queryBooks = "select * from all_books where frequency>0";
        int bookCount = 0;
        try {
            PreparedStatement queryBookStmt = conn.prepareStatement(queryBooks);
            ResultSet booksResult = queryBookStmt.executeQuery();
            String marcNo, bookTitle, bookAuthor, storeArea, isbn, whereNum, bookType, bookTypeDetail;
            int frequency, queryTimes;
            String[] detailTypes;
            String insertSql = "insert IGNORE into all_books_detail_type(marc_no, book_title, book_author, store_area, isbn, where_num, book_detail_type, query_times, frequency)" +
                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
            conn.setAutoCommit(false);
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            while (booksResult.next()) {
                bookCount++;
                System.out.println("解析第" + bookCount + "本书...");
                marcNo = booksResult.getString("marcNo");
                bookTitle = booksResult.getString("book_title");
                bookAuthor = booksResult.getString("book_author");
                storeArea = booksResult.getString("store_area");
                isbn = booksResult.getString("book_isbn");
                whereNum = booksResult.getString("where_num");
                bookType = booksResult.getString("book_type");
                queryTimes = booksResult.getInt("query_times");
                frequency = booksResult.getInt("frequency");

                detailTypes = bookType.split("-");
                for (String detailType : detailTypes) {
                    insertStmt.setString(1, marcNo);
                    insertStmt.setString(2, bookTitle);
                    insertStmt.setString(3, bookAuthor);
                    insertStmt.setString(4, storeArea);
                    insertStmt.setString(5, isbn);
                    insertStmt.setString(6, whereNum);
                    insertStmt.setString(7, detailType);
                    insertStmt.setInt(8, queryTimes);
                    insertStmt.setInt(9, frequency);
                    insertStmt.addBatch();
                }
                if (bookCount%10000 == 0) {
                    System.out.println("执行插入数据...");
                    Long beginTime = System.currentTimeMillis();
                    int[] result = insertStmt.executeBatch();
                    conn.commit();
                    insertStmt.clearBatch();
                    Long stopTime = System.currentTimeMillis();
                    System.out.println("执行了" + result.length + "条Sql语句，耗时" + (stopTime - beginTime) + "毫秒...");
                }
            }
            System.out.println("执行插入数据...");
            Long beginTime = System.currentTimeMillis();
            int[] result = insertStmt.executeBatch();
            conn.commit();
            insertStmt.clearBatch();
            Long stopTime = System.currentTimeMillis();
            System.out.println("执行了" + result.length + "条Sql语句，耗时" + (stopTime - beginTime) + "毫秒...");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}