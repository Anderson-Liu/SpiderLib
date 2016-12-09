import java.sql.*;

/**
 * Created by anderson on 2016/12/9.
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
        String query = "select stu_id from student";
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = conn.prepareStatement(query);
            ResultSet resultSet = preparedStmt.executeQuery();
            String stuId;
            String updateSql = "update student set total_borrow=(select count(book_id) from student_book where stu_id = ?) where stu_id=?";
            while (resultSet.next()) {
                stuId = resultSet.getString("stu_id");
                preparedStmt = conn.prepareStatement(updateSql);
                preparedStmt.setString(1, stuId);
                preparedStmt.setString(2, stuId);
                preparedStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        query = "select book_id from books";

        try {
            preparedStmt = conn.prepareStatement(query);
            ResultSet resultSet = preparedStmt.executeQuery();
            String bookId;
            String updateSql = "update books set borrowed_count=(select count(book_id) from student_book where book_id = ?) where book_id=?";
            while (resultSet.next()) {
                bookId = resultSet.getString("book_id");

            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
