import java.sql.*;
import java.util.Vector;

/**
 * Created by anderson on 2016/12/13.
 */

public class CalcBookDetailTag {
    static String url = "jdbc:mysql://localhost:3306/ahaulib?"
            + "user=root&password=anderson&useUnicode=true&characterEncoding=UTF8";
    static Connection conn;
    static int connCount = 0;

    static Vector<Connection> pools = new Vector<>();

    public static Connection getDBConnection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            conn = DriverManager.getConnection(url);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static {
        int i = 0;
        while(i < 100){
            pools.add(getDBConnection());
            i++;
        }
    }

    public static synchronized Connection getPool(){
        if(pools != null && pools.size() > 0){
            int last_ind = pools.size() -1;
            return pools.remove(last_ind);
        }else{
            return getDBConnection();
        }
    }

    public static void main(String args[]) {
        String queryBooks = "select * from books ORDER BY isbn DESC LIMIT 1000";
        int bookCount = 0;
        try {
            conn = getPool();
            PreparedStatement queryBookstmt = conn.prepareStatement(queryBooks);
            ResultSet booksResult = queryBookstmt.executeQuery();
            String bookId, bookTitle, bookAuthor, storeArea, isbn, whereNum, bookType, bookTypeDetail;
            int borrowedCount;
            String[] detailTypes;
            String insertSql = "insert into test_frequency_books_detail_type(book_id, book_title, book_author, store_area, isbn, where_num, book_detail_type, frequency) " +
                    "values(?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE frequency=frequency+?";
            conn.setAutoCommit(false);
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            while (booksResult.next()) {
                bookCount++;
                System.out.println("解析第" + bookCount + "本书...");
                bookId = booksResult.getString("book_id");
                bookTitle = booksResult.getString("book_title");
                bookAuthor = booksResult.getString("book_author");
                storeArea = booksResult.getString("store_area");
                isbn = booksResult.getString("isbn");
                whereNum = booksResult.getString("where_num");
                bookType = booksResult.getString("book_type");
                borrowedCount = booksResult.getInt("borrowed_count");

                detailTypes = bookType.split("-");
                for (String detailType : detailTypes) {
                    insertStmt.setString(1, bookId);
                    insertStmt.setString(2, bookTitle);
                    insertStmt.setString(3, bookAuthor);
                    insertStmt.setString(4, storeArea);
                    insertStmt.setString(5, isbn);
                    insertStmt.setString(6, whereNum);
                    insertStmt.setString(7, detailType);
                    insertStmt.setInt(8, borrowedCount);
                    insertStmt.setInt(9, borrowedCount);
                    insertStmt.addBatch();
                }
                if (bookCount%100 == 0) {
                    // int[] result = insertStmt.executeBatch();
                    // insertStmt.clearBatch();
                    // insertStmt.close();
                    System.out.println("创建新进程...");
                    new Thread(new CalcBookDetailTag().new CommitRunnable(insertStmt, bookCount, conn)).start();
                    conn = getPool();

                    insertStmt = conn.prepareStatement(insertSql);
                    connCount++;
                }
            }
            // int[] result = insertStmt.executeBatch();
            // insertStmt.clearBatch();
            // insertStmt.close();
            System.out.println("创建新进程...");
            new Thread(new CalcBookDetailTag().new CommitRunnable(insertStmt, bookCount, conn)).start();
            conn = getPool();
            insertStmt = conn.prepareStatement(insertSql);
            connCount++;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    class CommitRunnable implements Runnable{

        PreparedStatement insertStmt;
        int countBook;
        Connection conn;

        public CommitRunnable(PreparedStatement insertStmt, int countBook, Connection conn) {
            this.insertStmt = insertStmt;
            this.countBook = countBook;
            this.conn = conn;
        }

        @Override
        public void run() {
            try {
                Long beginTime = System.currentTimeMillis();
                int[] result = insertStmt.executeBatch();
                System.out.println("进程里面的insertStmt执行了: " + result.length + "条Sql语句......");
                conn.commit();
                Long endTime = System.currentTimeMillis();
                insertStmt.clearBatch();
                System.out.println("第" + countBook + "  pst+bat用时："+(endTime-beginTime)+"毫秒");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    if (connCount > 2) {
                        insertStmt.close();
                        conn.close();
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}