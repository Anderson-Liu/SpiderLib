import java.sql.*;
import java.util.ArrayList;
import java.util.Map;

/**
 * Created by anderson on 2016/12/9.
 */
public class CalcFrequ {

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

        // 从文件获得学生列表
        String stuName = "";
        String stuId = "";
        String bookTitle = "";
        String bookType = "";
        String bookAuthor = "";
        Map<String, String> stuList = LoadData.getStuList();
        final String TYPE_TITLE = "book_title";
        final String TYPE_AUTHOR = "book_author";
        final String TYPE_TYPE = "book_type";
        String TYPE_TYPE_DETAIL = "book_type_detail";
        int countStudent = 0;

        for (Map.Entry<String, String> entry : stuList.entrySet()) {
            // for (int i=0; i<1; i++){
            countStudent ++;
            int countBook = 0;
            stuName = entry.getKey();
            stuId = entry.getValue();
            String temp;
            if (stuId.length() != 8) {
                temp = stuName;
                stuName = stuId;
                stuId = temp;
            }

            System.out.println("获取第"+ countStudent + "个学生的兴趣标签...");
            // 从mysql获取数据
            String query = "SELECT book_title, book_type, book_author FROM books " +
                    "WHERE book_id IN (SELECT book_id FROM student_book WHERE stu_id = ?)";

            PreparedStatement preparedStmt = null;

            // stuId = "14101733";

            try {
                preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString(1, stuId);
                ResultSet resultSet = preparedStmt.executeQuery();

                String updateSequc = "INSERT INTO frequency_study_good(type, name) VALUES(?, ?) " +
                        "ON DUPLICATE KEY UPDATE frequency=frequency+1";

                String[] bookTypes = null;
                while (resultSet.next()) {
                    countBook++;
                    System.out.println("获取第"+ countStudent + "个学生的第" + countBook + "本书兴趣标签...");
                    bookTitle = resultSet.getString("book_title");
                    preparedStmt = conn.prepareStatement(updateSequc);
                    preparedStmt.setString(1, TYPE_TITLE);
                    preparedStmt.setString(2, bookTitle);
                    preparedStmt.executeUpdate();

                    bookType = resultSet.getString("book_type");
                    bookTypes = bookType.split("-");
                    for (String type : bookTypes) {
                        type = type.trim();
                        preparedStmt = conn.prepareStatement(updateSequc);
                        preparedStmt.setString(1, TYPE_TYPE_DETAIL);
                        preparedStmt.setString(2, type);
                        preparedStmt.executeUpdate();
                    }

                    preparedStmt = conn.prepareStatement(updateSequc);
                    preparedStmt.setString(1, TYPE_TYPE);
                    preparedStmt.setString(2, bookType);
                    preparedStmt.executeUpdate();

                    bookAuthor = resultSet.getString("book_author");
                    preparedStmt = conn.prepareStatement(updateSequc);
                    preparedStmt.setString(1, TYPE_AUTHOR);
                    preparedStmt.setString(2, bookAuthor);
                    preparedStmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
