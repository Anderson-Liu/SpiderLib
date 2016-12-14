import java.sql.*;
import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by anderson on 2016/12/14.
 */
public class RecommendBook {

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

    }
    public void recommendByPerson(String stuId){
        final String LABEL_TYPE= "book_type_detail";
        int countLowFreq=0, countLowTag=0;
        // 获取学生频数最高的兴趣, 只获取一个兴趣标签
        String getInterestTag = "select label_value, frequency from frequency_stuid_tag " +
                "where stu_id=? and label_type=? order by frequency desc limit 1";

        try {
            PreparedStatement getInterestStmt = conn.prepareStatement(getInterestTag);
            getInterestStmt.setString(1, stuId);
            getInterestStmt.setString(2, LABEL_TYPE);
            ResultSet interestTags = getInterestStmt.executeQuery();
            while (interestTags.next()){
                String intrsTag = interestTags.getString("label_value");
                int frequency = interestTags.getInt("frequency");
                if (frequency > 1) {
                    if (Objects.equals(intrsTag, "中国")) {
                        countLowTag++;
                        System.out.println("第" + countLowTag + "个出现中国等区分度很低的兴趣标签...");
                    }
                    // 获取在这个标签下频数最高的 Top 3 学生
                    String getTop3StudentByTag = "select stu_id from frequency_stuid_tag " +
                            "where label_value=? order by frequency desc limit 3";
                    PreparedStatement getTop3StuByTagStmt = conn.prepareStatement(getTop3StudentByTag);
                    getTop3StuByTagStmt.setString(1, intrsTag);
                    ResultSet top3Stu = getTop3StuByTagStmt.executeQuery();
                    while (top3Stu.next()){
                        String top3StuId = top3Stu.getString("stu_id");
                        String getRecmdBooks = "select * from all_books where marc_no in" +
                                "(select marc_num from book_marc_id where book_id in" +
                                    "(select book_id from stuid_label_bookids where stu_id=? and label_value=?))";
                        PreparedStatement getRecmdBooksStmt = conn.prepareStatement(getRecmdBooks);
                        getRecmdBooksStmt.setString(1, top3StuId);
                        getRecmdBooksStmt.setString(2, intrsTag);
                        ResultSet recmdBooks = getRecmdBooksStmt.executeQuery();
                        ArrayList<Book> bookList = new ArrayList<>();
                        while (recmdBooks.next()){
                            String marcNo = recmdBooks.getString("marc_no");
                            String bookTitle = recmdBooks.getString("book_title");

                        }
                    }
                } else {
                    countLowFreq++;
                    System.out.println("第" + countLowFreq + "个兴趣标签的最高频数等于1，无法获取焦点兴趣...");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
    public void recommendByContent(){

    }
}
