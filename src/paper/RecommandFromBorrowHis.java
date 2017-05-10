package paper;

import java.sql.*;
import java.util.*;


/**
 * Created by anderson on 17-5-10.
 * 计算得出可以代表读者个性的书籍
 */
public class RecommandFromBorrowHis {
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

    public static void main(String args[]) throws SQLException {
        Map<String, Double> books = getBooksFormUser();
        List<String> top10Books = new ArrayList<>();
        int count = 0;
        //遍历map中的键,获取top10
        for (String key : books.keySet()) {
            System.out.println("Key = " + key);
            if (count < 10) {
                top10Books.add(key);
            } else {
                break;
            }
            count++;
        }
//        String marc_no = top10Books.get(0);
        for (String marc_no : top10Books) {
            String getStuSql = "select stu_id from student_borrow_record where marc_no = ?";
            PreparedStatement getStuStmt = conn.prepareStatement(getStuSql);
            getStuStmt.setString(1, marc_no);
            ResultSet stuResult = getStuStmt.executeQuery();
            String stuId;
            String getMarcSql = "select marc_no, book_title from student_borrow_record where stu_id=?";
            PreparedStatement getMarcStmt = conn.prepareStatement(getMarcSql);
            List<String> totalRecommandBooksMarc = new ArrayList<>();
            List<String> totalRecommandBooksTitle = new ArrayList<>();
            String bookAttrSql = "select total_read_time, avg_read_time, variance_read from all_booksid_with_title " +
                    "where marc_no=? and total_read_time>=1 and avg_read_time<=250 and variance_read<=30000 group by marc_no";
            PreparedStatement bookAttrStmt = conn.prepareStatement(bookAttrSql);
            int minTotalReadedTime = 2, maxTotalReadedTime = 2735, minAvgRead = 0, maxAvgRead=250, minVariance=0, maxVariance=30000;
            Map<String, Double> recommandResultMap = new HashMap<>();
            int countStu = 0;
            while (stuResult.next()) {
                countStu ++;
                stuId = stuResult.getString("stu_id");
                // 剔除自己
                if (Objects.equals(stuId, "13100501")) {
                    continue;
                }
                getMarcStmt.setString(1, stuId);
                ResultSet marcResult = getMarcStmt.executeQuery();
                // 获取其他用户看了这本书后的其他书
                // todo: 把看过的这本书剔除掉
                while (marcResult.next()) {
                    String marcNo = marcResult.getString("marc_no");
                    String title = marcResult.getString("book_title");
                    totalRecommandBooksMarc.add(marcNo);
                    totalRecommandBooksTitle.add(title);
                    // 计算score
                    bookAttrStmt.setString(1, marcNo);
                    ResultSet bookAttrs = bookAttrStmt.executeQuery();
                    int totalReadTime;
                    double totalReadTimeDouble=0;
                    double avgReadTimeDouble=0;
                    double variance = 0;
                    while (bookAttrs.next()) {
                        totalReadTime = bookAttrs.getInt("total_read_time");
                        totalReadTimeDouble = ((totalReadTime - minTotalReadedTime)/(double)(maxTotalReadedTime - minTotalReadedTime));
                        int avg_read_time = bookAttrs.getInt("avg_read_time");
                        avgReadTimeDouble = (avg_read_time - minAvgRead)/(double)(maxAvgRead - minAvgRead);
                        variance = bookAttrs.getDouble("variance_read");
                        variance = (variance - minVariance)/(maxVariance - minVariance);
                    }
                    double score = -20*totalReadTimeDouble + 4*avgReadTimeDouble - (10*variance);
                    recommandResultMap.put(title, score);
                }
                // 第i个学生阅读的其他书籍
                recommandResultMap = MapUtil.sortByValue(recommandResultMap);
                System.out.println("第" + countStu + "个学生阅读的其他书籍: " + recommandResultMap);
            }
            // 所有学生阅读的其他书籍
            // todo 去重，整体排序
            System.out.println("所有学生阅读的其他书籍: " + recommandResultMap);
        }
    }

    public static Map<String, Double> getBooksFormUser() throws SQLException {
        // todo: 获取所有学生的代表书籍=》获取单个学生的代表书籍
        String stuSql = "select distinct(stu_id) from student_borrow_record";
        PreparedStatement stuStmt = conn.prepareStatement(stuSql);
        ResultSet stuResult  = stuStmt.executeQuery();
        String stuId, marcNo, bookTitle;
        String marcNoSql = "select marc_no, book_title from student_borrow_record where stu_id=?";
        PreparedStatement marcNoStmt = conn.prepareStatement(marcNoSql);
        String readTimeSql = "select read_time, time_from_now from student_borrow_record where marc_no=?";
        PreparedStatement readTimeStmt = conn.prepareStatement(readTimeSql);
        int readTime = 0;
        String bookAttrSql = "select total_read_time, avg_read_time, variance_read from all_booksid_with_title " +
                "where marc_no=? and total_read_time>=1 and avg_read_time<=250 and variance_read<=30000 group by marc_no";
        PreparedStatement bookAttrStmt = conn.prepareStatement(bookAttrSql);
        int avg_read_time = 0, totalReadTime=0;
        double variance = 0;
        double score;
        int minReadTime = 0, maxReadTime = 0;
        int timeFromNow = 0;
        int minTotalReadedTime = 2, maxTotalReadedTime = 2735, minAvgRead = 0, maxAvgRead=250, minVariance=0, maxVariance=30000;

        String minReadTimeSql = "select min(read_time) as num from student_borrow_record where stu_id=?";
        String maxReadTimeSql = "select max(read_time) as num from student_borrow_record where stu_id=?";
        String minTimeFromNowSql = "select min(time_from_now) as num from student_borrow_record where stu_id=?";
        String maxTimeFromNowSql = "select max(time_from_now) as num from student_borrow_record where stu_id=?";
        PreparedStatement minReadStmt = conn.prepareStatement(minReadTimeSql);
        PreparedStatement maxReadStmt = conn.prepareStatement(maxReadTimeSql);
        PreparedStatement minTimeFromNowStmt = conn.prepareStatement(minTimeFromNowSql);
        PreparedStatement maxTimeFromNowStmt = conn.prepareStatement(maxTimeFromNowSql);
        Map<String, Double> bookScore = new HashMap<>();
        double totalReadTimeDouble = 0;
        double readTimeDouble = 0, avgReadTimeDouble=0, timeFromNowDouble=0;
        int minTimeFromNow = 0, maxTimeFromNow=0;
        while (stuResult.next()) {
//            stuId = stuResult.getString("stu_id");
            stuId = "13100501";
            marcNoStmt.setString(1, stuId);
            ResultSet marcNos = marcNoStmt.executeQuery();
            while (marcNos.next()) {
                marcNo = marcNos.getString("marc_no");
                bookTitle = marcNos.getString("book_title");
                readTimeStmt.setString(1, marcNo);
                ResultSet readTimeResult = readTimeStmt.executeQuery();

                minTimeFromNowStmt.setString(1, stuId);
                maxTimeFromNowStmt.setString(1, stuId);
                ResultSet minTimeFromNowResult = minTimeFromNowStmt.executeQuery();
                ResultSet maxTimeFromNowResult = maxTimeFromNowStmt.executeQuery();

                while (minTimeFromNowResult.next()) {
                    minTimeFromNow = minTimeFromNowResult.getInt("num");
                }
                while (maxTimeFromNowResult.next()) {
                    maxTimeFromNow = maxTimeFromNowResult.getInt("num");
                }

                while (readTimeResult.next()) {
                    readTime = readTimeResult.getInt("read_time");
                    timeFromNow = readTimeResult.getInt("time_from_now");
                }

                if (maxTimeFromNow == minTimeFromNow) {
                    // 当该读者只借了一本书的时候
                    timeFromNowDouble = 0.0;
                } else {
                    timeFromNowDouble = (timeFromNow-minTimeFromNow)/(double)(maxTimeFromNow - minTimeFromNow);
                }

                minReadStmt.setString(1, stuId);
                maxReadStmt.setString(1, stuId);

                ResultSet minReadTimeResult = minReadStmt.executeQuery();
                ResultSet maxReadTimeResult = maxReadStmt.executeQuery();

                while (minReadTimeResult.next()) {
                    minReadTime = minReadTimeResult.getInt("num");
                }
                while (maxReadTimeResult.next()) {
                    maxReadTime = maxReadTimeResult.getInt("num");
                }
                if (maxReadTime == minReadTime) {
                    // 书籍只被借了一次
//                    readTimeDouble = 1.0;
                    continue;
                } else {
                    readTimeDouble = (readTime-minReadTime)/(double)(maxReadTime - minReadTime);
                }

                bookAttrStmt.setString(1, marcNo);
                ResultSet bookAttrs = bookAttrStmt.executeQuery();
                while (bookAttrs.next()) {
                    totalReadTime = bookAttrs.getInt("total_read_time");
                    totalReadTimeDouble = ((totalReadTime - minTotalReadedTime)/(double)(maxTotalReadedTime - minTotalReadedTime));
                    avg_read_time = bookAttrs.getInt("avg_read_time");
                    avgReadTimeDouble = (avg_read_time - minAvgRead)/(double)(maxAvgRead - minAvgRead);
                    variance = bookAttrs.getDouble("variance_read");
                    variance = (variance - minVariance)/(maxVariance - minVariance);
                }
                score = 4*readTimeDouble - 10.0*totalReadTimeDouble + 6*avgReadTimeDouble - (8*variance) - 20.0*timeFromNowDouble;
//                bookScore.put(bookTitle, score);
                bookScore.put(marcNo, score);
            }
            System.out.println(bookScore);
            bookScore = MapUtil.sortByValue(bookScore);
            System.out.println(bookScore);
            break;
        }
        return bookScore;
    }
}
