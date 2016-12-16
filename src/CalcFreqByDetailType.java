import org.apache.http.auth.AUTH;

import java.sql.*;

/**
 * Created by anderson on 2016/12/15.
 */
public class CalcFreqByDetailType {

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

    public static void main(String args[]){
        CalcFreqByDetailType object = new CalcFreqByDetailType();
        object.calcAuthorFreq();
        // object.calcTypeFreq();
    }

    public void calcAuthorFreq(){
        final String TYPE_AUTHOR = "author";
        int authorCount = 0;
        String getAuthors = "select distinct book_author from books";
        try {
            String insertSql = "insert ignore into total_frequency(type, value, total_borrow, total_query_times) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            PreparedStatement getAuthorStmt = conn.prepareStatement(getAuthors);
            ResultSet authorResult = getAuthorStmt.executeQuery();
            while (authorResult.next()){
                authorCount++;
                System.out.println("开始处理第" + authorCount + "位作者...");
                String author = authorResult.getString("book_author");
                String getSumFreq = "select sum(frequency) as sum_freq from all_books where book_author=?";
                String getSumQueryTimes = "select sum(query_times) as sum_query_times from all_books where book_author=?";
                PreparedStatement getSumFreqStmt = conn.prepareStatement(getSumFreq);
                PreparedStatement getSumQueryStmt = conn.prepareStatement(getSumQueryTimes);
                getSumFreqStmt.setString(1, author);
                getSumQueryStmt.setString(1, author);
                ResultSet sumFreqResult = getSumFreqStmt.executeQuery();
                ResultSet sumQueryResult = getSumQueryStmt.executeQuery();
                int sumQueryTimes = 0;
                int sumFreq = 0;
                if (sumQueryResult.next() &&
                        sumFreqResult.next()){
                    sumFreq = sumFreqResult.getInt("sum_freq");
                    sumQueryTimes = sumQueryResult.getInt("sum_query_times");
                }
                insertStmt.setString(1, TYPE_AUTHOR);
                insertStmt.setString(2, author);
                insertStmt.setInt(3, sumQueryTimes);
                insertStmt.setInt(4, sumFreq);
                insertStmt.addBatch();
                if (authorCount%5000==0){
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

    public void calcTypeFreq(){
        final String TYPE_TYPE = "type";
        int typeCount = 0;
        String getBookDetailTypes = "select distinct book_type from all_books_detail_type";
        try {
            String insertSql = "insert ignore into total_frequency(type, value, total_borrow, total_query_times) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);

            PreparedStatement getBookDetailTypeStmt = conn.prepareStatement(getBookDetailTypes);
            ResultSet bookDetailTypes = getBookDetailTypeStmt.executeQuery();
            while (bookDetailTypes.next()){
                typeCount++;
                System.out.println("开始处理第" + typeCount + "条book type");
                String bookDetailType = bookDetailTypes.getString("book_type");
                String getSumFreq = "select sum(frequency) as sum_freq from all_books_detail_type where book_type=?";
                String getSumQueryTimes = "select sum(query_times) as sum_query_times from all_books_detail_type where book_type=?";
                PreparedStatement getSumFreqStmt = conn.prepareStatement(getSumFreq);
                PreparedStatement getSumQueryStmt = conn.prepareStatement(getSumQueryTimes);
                getSumFreqStmt.setString(1, bookDetailType);
                getSumQueryStmt.setString(1, bookDetailType);
                ResultSet sumFreqResult = getSumFreqStmt.executeQuery();
                ResultSet sumQueryResult = getSumQueryStmt.executeQuery();
                int sumQueryTimes = 0;
                int sumFreq = 0;
                if (sumQueryResult.next() &&
                        sumFreqResult.next()){
                    sumFreq = sumFreqResult.getInt("sum_freq");
                    sumQueryTimes = sumQueryResult.getInt("sum_query_times");
                }
                insertStmt.setString(1, TYPE_TYPE);
                insertStmt.setString(2, bookDetailType);
                insertStmt.setInt(3, sumQueryTimes);
                insertStmt.setInt(4, sumFreq);
                insertStmt.addBatch();
                if (typeCount%1000==0){
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
