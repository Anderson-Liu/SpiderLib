import java.sql.*;

/**
 * Created by anderson on 2016/12/14.
 */
public class CalcFreqByMarc {

    static String url = "jdbc:mysql://localhost:3306/ahaulib?"
            + "user=root&password=anderson&useUnicode=true&characterEncoding=UTF8";
    static Connection conn;

    public static void main(String args[]){
        try {
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }

        new CalcFreqByMarc().calcFreqncPerBook();
        new CalcFreqByMarc().calcFreqncPerMarc();
    }


    public void calcFreqncPerMarc(){
        String queryMarc = "select distinct marc_no from book_marc_id where frequency>0";
        try {
            String updateSql = "update all_books set frequency = ? where marc_no = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);

            PreparedStatement queryMarcStmt = conn.prepareStatement(queryMarc);
            ResultSet marcNums = queryMarcStmt.executeQuery();
            String marcNum;
            int marcCount = 0;
            while (marcNums.next()){
                marcCount++;
                System.out.println("执行第" + marcCount + "个marcNum...");
                marcNum = marcNums.getString("marc_no");
                String calcFreqncy = "select sum(frequency) as total_frequency from book_marc_id where marc_no =?";
                PreparedStatement calcFreqncyStmt = conn.prepareStatement(calcFreqncy);
                calcFreqncyStmt.setString(1, marcNum);
                ResultSet totalFrequencys = calcFreqncyStmt.executeQuery();
                while (totalFrequencys.next()) {
                    int totalFrequency = totalFrequencys.getInt("total_frequency");
                    updateStmt.setInt(1, totalFrequency);
                    updateStmt.setString(2, marcNum);
                    updateStmt.addBatch();
                }
                if (marcCount%10000==0){
                    System.out.println("执行插入数据...");
                    Long beginTime = System.currentTimeMillis();
                    int[] result = updateStmt.executeBatch();
                    conn.commit();
                    updateStmt.clearBatch();
                    Long stopTime = System.currentTimeMillis();
                    System.out.println("执行了" + result.length + "条Sql语句，耗时" + (stopTime - beginTime) + "毫秒...");
                }
            }
            System.out.println("执行插入数据...");
            Long beginTime = System.currentTimeMillis();
            int[] result = updateStmt.executeBatch();
            conn.commit();
            updateStmt.clearBatch();
            Long stopTime = System.currentTimeMillis();
            System.out.println("执行了" + result.length + "条Sql语句，耗时" + (stopTime - beginTime) + "毫秒...");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void calcFreqncPerBook(){
        String queryBookId = "select distinct book_id from books";
        String bookId;
        int frequency;
        try {
            PreparedStatement queryBookIdStmt = conn.prepareStatement(queryBookId);
            ResultSet bookIds = queryBookIdStmt.executeQuery();
            String updateSql = "update book_marc_id set frequency=? where book_id = ?";
            PreparedStatement updateStmt = conn.prepareStatement(updateSql);
            int bookCount = 0;
            while (bookIds.next()){
                bookCount++;
                bookId = bookIds.getString("book_id");
                System.out.println("开始处理第" + bookCount + "本书:" + bookId);
                String calcFreqnc = "select count(*) as frequency from borrow_book_record where book_id = ?";
                PreparedStatement calcFreqncStmt = conn.prepareStatement(calcFreqnc);
                calcFreqncStmt.setString(1, bookId);
                ResultSet freqncPerBook = calcFreqncStmt.executeQuery();
                while (freqncPerBook.next()){
                    frequency = freqncPerBook.getInt("frequency");
                    updateStmt.setInt(1, frequency);
                    updateStmt.setString(2, bookId);
                    updateStmt.addBatch();
                }
                if (bookCount%10000==0){
                    System.out.println("执行插入数据...");
                    Long beginTime = System.currentTimeMillis();
                    int[] result = updateStmt.executeBatch();
                    conn.commit();
                    updateStmt.clearBatch();
                    Long stopTime = System.currentTimeMillis();
                    System.out.println("执行了" + result.length + "条Sql语句，耗时" + (stopTime - beginTime) + "毫秒...");
                }
            }
            System.out.println("执行插入数据...");
            Long beginTime = System.currentTimeMillis();
            int[] result = updateStmt.executeBatch();
            conn.commit();
            updateStmt.clearBatch();
            Long stopTime = System.currentTimeMillis();
            System.out.println("执行了" + result.length + "条Sql语句，耗时" + (stopTime - beginTime) + "毫秒...");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
