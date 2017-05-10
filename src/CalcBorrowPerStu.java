import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.sql.*;

/**
 * Created by anderson on 2017/05/07.
 * 计算每个读者的借阅详情数据
 */

// todo: 增加作者/类别标签
public class CalcBorrowPerStu {
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

    public static void main(String args[]) {
        int bookCount = 0;
        try {
            conn.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            String queryRecordSql = "select * from borrow_book_record";
            PreparedStatement queryRecordStmt = conn.prepareStatement(queryRecordSql);
            ResultSet recordResult = queryRecordStmt.executeQuery();
            String author = null, stu_id, marcNo = null, bookTitle = null, book_id, borrow_date, return_date, name = null, sex = null, type = null, department = null, major = null;
            String insertSql = "insert into student_borrow_record(stu_id, name, sex, type, department, major, book_id, marc_no, book_title, author, borrow_date, return_date) " +
                    "values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE frequency=frequency+1";
            PreparedStatement insertStmt = conn.prepareStatement(insertSql);
            while (recordResult.next()) {
                bookCount++;
                if(bookCount < 200000) {
                    continue;
                }
                if (bookCount >= 250001) {
                    System.exit(0);
                }
                System.out.println("解析第" + bookCount + "条记录...");
                stu_id = recordResult.getString("stu_id");
                book_id = recordResult.getString("book_id");
                borrow_date = recordResult.getString("borrow_date");
                return_date = recordResult.getString("return_date");

                String queryStu = "select * from student where stu_id = ?";
                PreparedStatement queryStuStmt =  conn.prepareStatement(queryStu);
                queryStuStmt.setString(1, stu_id);
                ResultSet stuResult = queryStuStmt.executeQuery();
                if (stuResult.next()) {
                    name = stuResult.getString("name");
                    sex = stuResult.getString("sex");
                    type = stuResult.getString("type");
                    department = stuResult.getString("department");
                    major = stuResult.getString("major");
                }

                String queryBook = "select * from all_booksid_with_title where book_id=?";
                PreparedStatement queryBookStmt = conn.prepareStatement(queryBook);
                queryBookStmt.setString(1, book_id);
                ResultSet bookResult = queryBookStmt.executeQuery();
                if (bookResult.next()) {
                    marcNo = bookResult.getString("marc_no");
                    bookTitle = bookResult.getString("book_title");
                    author = bookResult.getString("author");

                }

                insertStmt.setString(1, stu_id);
                insertStmt.setString(2, name);
                insertStmt.setString(3, sex);
                insertStmt.setString(4, type);
                insertStmt.setString(5, department);
                insertStmt.setString(6, major);
                insertStmt.setString(7, book_id);
                insertStmt.setString(8, marcNo);
                insertStmt.setString(9, bookTitle);
                insertStmt.setString(10, author);
                insertStmt.setString(11, borrow_date);
                insertStmt.setString(12, return_date);
                insertStmt.addBatch();
                if (bookCount%20000 == 0) {
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