import java.sql.*;

/**
 * Created by anderson on 2016/12/9.
 * 通过书籍ID计算每本书籍的累计被借阅次数
 *
 */
public class CalcBorrowCount {

    static String url = "jdbc:mysql://localhost:3306/ahaulib?"
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
        String query = "select DISTINCT stu_id from student";
        PreparedStatement preparedStmt = null;
        // try {
        //    preparedStmt = conn.prepareStatement(query);
        //    System.out.println("获取所有学生的ID号码....");
        //    ResultSet resultSet = preparedStmt.executeQuery();
        //    String stuId;
        //    int i=0;
        //    String updateSql = "update student set total_borrow=(select count(book_id) from borrow_book_record where stu_id = ?) where stu_id=?";
        //    while (resultSet.next()) {
        //        i++;
        //       System.out.println("修正第" + i + "个学生的借阅总数...");
        //        stuId = resultSet.getString("stu_id");
        //        preparedStmt = conn.prepareStatement(updateSql);
        //        preparedStmt.setString(1, stuId);
        //        preparedStmt.setString(2, stuId);
        //        preparedStmt.executeUpdate();
        //    }
        //} catch (SQLException e) {
        //    e.printStackTrace();
        //}

        query = "select book_id from books";

        try {
            preparedStmt = conn.prepareStatement(query);
            ResultSet resultSet = preparedStmt.executeQuery();
            String bookId;
            int j=0;
            String updateSql = "update books set borrowed_count=(select count(book_id) from borrow_book_record where book_id = ?) where book_id=?";
            while (resultSet.next()) {
                j++;
                if (j < 90103) {
                    continue;
                }
                System.out.println("修正第" + j + "条书籍数据的累计借阅信息......");
                bookId = resultSet.getString("book_id");
                preparedStmt = conn.prepareStatement(updateSql);
                preparedStmt.setString(1, bookId);
                preparedStmt.setString(2, bookId);
                preparedStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}