package paper;

import java.sql.*;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by anderson on 17-5-10.
 */
public class CalcTimePerStuPerBook {
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
        String recordSql = "select id, borrow_date, return_date from student_borrow_record";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        PreparedStatement recordStmt = conn.prepareStatement(recordSql);
        ResultSet resultSet = recordStmt.executeQuery();
        int id;
        String borrow_date, return_date;
        String updateSql = "update student_borrow_record set read_time=? where id=?";
        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
        int count=0;
        while (resultSet.next()) {
            count++;
            System.out.println("执行第" + count + "条记录...");
            id = resultSet.getInt("id");
            borrow_date = resultSet.getString("borrow_date");
            return_date = resultSet.getString("return_date");
            java.util.Date date_borrow = simpleDateFormat.parse(borrow_date);
            java.util.Date date_return = simpleDateFormat.parse(return_date);
            long diff = Math.abs(date_borrow.getTime() - date_return.getTime());
            int diffDays = (int) (diff / (24 * 60 * 60 * 1000));
            System.out.println("Difference between  " + borrow_date + " and "+ return_date +" is " + diffDays);
            updateStmt.setInt(1, diffDays);
            updateStmt.setInt(2, id);
            updateStmt.executeUpdate();
        }
    }
}
