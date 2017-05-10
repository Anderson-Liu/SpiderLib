package paper;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * Created by anderson on 17-5-11.
 * 计算借书的时间到现在的时长
 */
public class CalcBorrowDateFromNow {
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

    public static void main(String args[]) throws SQLException, ParseException {
        String queryBorrowDate = "select id, borrow_date from student_borrow_record";
        PreparedStatement queryStmt = conn.prepareStatement(queryBorrowDate);
        ResultSet borrowDateResult = queryStmt.executeQuery();
        int id = 0;
        String borrowDate;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String updateSql = "update student_borrow_record set time_from_now=? where id=?";
        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
        while (borrowDateResult.next()) {
            id = borrowDateResult.getInt("id");
            borrowDate = borrowDateResult.getString("borrow_date");
            java.util.Date now = new java.util.Date();
            java.util.Date date_borrow = simpleDateFormat.parse(borrowDate);
            long diff = Math.abs(now.getTime() - date_borrow.getTime());
            int diffDays = (int) (diff / (24 * 60 * 60 * 1000));
            System.out.println("Difference between  " + date_borrow + " and "+ now +" is " + diffDays);
            updateStmt.setInt(1, diffDays);
            updateStmt.setInt(2, id);
            updateStmt.executeUpdate();
        }
    }
}
