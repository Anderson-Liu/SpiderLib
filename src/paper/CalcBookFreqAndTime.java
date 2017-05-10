package paper;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

/**
 * Created by anderson on 17-5-10.
 */
public class CalcBookFreqAndTime {
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
        String marcSql = "select marc_no, book_title from student_borrow_record GROUP BY marc_no, book_title";
        PreparedStatement marcStmt = conn.prepareStatement(marcSql);
        ResultSet marcs = marcStmt.executeQuery();
        String marc_no = null;
        String bookTitle = null;
        String borrow_date, return_date;
        String freqSql = "select count(id) as frequency from student_borrow_record where marc_no=?";
        PreparedStatement freqStmt = conn.prepareStatement(freqSql);
        String timeSql = "select borrow_date, return_date from student_borrow_record where marc_no=?";
        PreparedStatement timeStmt = conn.prepareStatement(timeSql);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        int frequency = 0;
        int avgReadTime = 0;
        int totalReadTime = 0;
        int bookCount = 0;
        String updateSql = "update all_booksid_with_title set frequency=?, total_read_time=?, avg_read_time=?, variance_read=?, read_time_min=?, read_time_max=?  where marc_no=?";
        PreparedStatement updateStmt = conn.prepareStatement(updateSql);
        Variance variance = new Variance();
        ArrayList<Double> doubleTimeArray = new ArrayList<>();
        ArrayList<Integer> intTimeArray = new ArrayList<>();
        while (marcs.next()) {
            doubleTimeArray.clear();
            intTimeArray.clear();
            bookCount++;
            System.out.println("处理第" + bookCount + "本书...");
            marc_no = marcs.getString("marc_no");
            bookTitle = marcs.getString("book_title");
            freqStmt.setString(1, marc_no);
            ResultSet freqResult = freqStmt.executeQuery();
            while (freqResult.next()) {
                frequency = freqResult.getInt("frequency");
            }
            System.out.println(bookTitle + " 被借了" + frequency + " 次");
            timeStmt.setString(1, marc_no);
            ResultSet timeResult = timeStmt.executeQuery();
            totalReadTime = 0;
            while (timeResult.next()) {
                borrow_date = timeResult.getString("borrow_date");
                return_date = timeResult.getString("return_date");
                Date date_borrow = simpleDateFormat.parse(borrow_date);
                Date date_return = simpleDateFormat.parse(return_date);
                long diff = Math.abs(date_borrow.getTime() - date_return.getTime());
                int diffDays = (int) (diff / (24 * 60 * 60 * 1000));
                System.out.println("Difference between  " + borrow_date + " and "+ return_date +" is " + diffDays);
                totalReadTime += diffDays;
                doubleTimeArray.add((double) diffDays);
                intTimeArray.add(diffDays);
            }
            double variance_read = variance.evaluate(convertDoubles(doubleTimeArray));
            System.out.println("variance read: " + variance_read);
            if (frequency!=0){
                avgReadTime = totalReadTime/frequency;
                updateStmt.setInt(1, frequency);
                updateStmt.setInt(2, totalReadTime);
                updateStmt.setInt(3, avgReadTime);
                updateStmt.setDouble(4, variance_read);
                updateStmt.setInt(5, Collections.min(intTimeArray));
                updateStmt.setInt(6, Collections.max(intTimeArray));
                updateStmt.setString(7, marc_no);
                System.out.println(bookTitle + " 总共被借了" + totalReadTime + " 天, 平均被借了" + avgReadTime + "天, 方差为" + variance_read + "\n");
                updateStmt.executeUpdate();
            } else {
                System.out.println("frequency is 0");
            }
        }
    }

    public static double[] convertDoubles(List<Double> doubles) {
        double[] ret = new double[doubles.size()];
        Iterator<Double> iterator = doubles.iterator();
        int i = 0;
        while(iterator.hasNext())
        {
            ret[i] = iterator.next();
            i++;
        }
        return ret;
    }
}
