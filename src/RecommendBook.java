import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Created by anderson on 2016/12/14.
 */
// TODO: 在生成的推荐书籍中，去掉自己看过的书(考虑是否有必要?)
// TODO: 增加"看过这本书的人还看了..."
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
        String stuId = "13100501";
        RecommendBook recommendBook = new RecommendBook();
        Map<String, Integer> resultMap = recommendBook.getTagByStu(stuId);
        for (Map.Entry<String, Integer> map : resultMap.entrySet()){
            String tag = map.getKey();
            int frequency = map.getValue();
            recommendBook.recommendByTagTopPerson(stuId, tag, frequency);
            recommendBook.recommendByContent(tag, frequency);
        }
    }
    public void recommendByTagTopPerson(String stuId, String intrsTag, int frequency){
        int type = 1;
        int toStringType = 1;
        System.out.println("通过相似的人推荐...");
        try {
            int countLowTag=0, countLowFreq=0;

            if (frequency > 1) {
                if (Objects.equals(intrsTag, "中国")) {
                    countLowTag++;
                    System.out.println("第" + countLowTag + "个出现中国等区分度很低的兴趣标签...");
                }
                System.out.println("获取到该学生的最大兴趣标签为: " + intrsTag);
                // 获取在这个标签下频数最高的 Top 3 学生
                String getTop3StudentByTag = "select stu_id from frequency_stuid_tag " +
                        "where label_value=? order by frequency desc limit 3";
                PreparedStatement getTop3StuByTagStmt = conn.prepareStatement(getTop3StudentByTag);
                getTop3StuByTagStmt.setString(1, intrsTag);
                ResultSet top3Stu = getTop3StuByTagStmt.executeQuery();
                System.out.println("开始获取该标签下频数最高的三个学生...");
                int countStu = 0;
                while (top3Stu.next()){
                    countStu++;
                    String top3StuId = top3Stu.getString("stu_id");
                    if (Objects.equals(stuId, top3StuId)) {
                        // 如果前三名里面有自己，跳过
                        System.out.println("在前三名中发现自己，跳过...");
                        continue;
                    }
                    System.out.println("第" + countStu + "个top3学生为： " +  top3StuId);
                    String getRecmdBooks = "select * from all_books where marc_no in" +
                            "(select marc_no from book_marc_id where book_id in" +
                                "(select book_id from stuid_label_bookids where stu_id=? and label_value=?))" +
                            "ORDER BY query_times desc";
                    PreparedStatement getRecmdBooksStmt = conn.prepareStatement(getRecmdBooks);
                    getRecmdBooksStmt.setString(1, top3StuId);
                    getRecmdBooksStmt.setString(2, intrsTag);
                    ResultSet recmdBooks = getRecmdBooksStmt.executeQuery();
                    ArrayList<Book> bookListPerStu = new ArrayList<>();
                    while (recmdBooks.next()){
                        // 这里考虑能否使用OCM框架或者类似Gson简化Book对象的构造
                        Book book = getBookFromResult(recmdBooks, type);
                        if (book != null) {
                            bookListPerStu.add(book);
                        }
                    }
                    // 是否这里只取前面三本书？还是第一个人取全部，其他人取浏览量前三的书
                    System.out.println("\n看这方面的书籍最多的人，他们看的书籍为");
                    for (Book book : bookListPerStu){
                        System.out.println(book.toString(toStringType));
                    }
                }
            } else {
                countLowFreq++;
                System.out.println("第" + countLowFreq + "个兴趣标签的最高频数等于1，无法获取焦点兴趣...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }


    public void recommendByContent(String intrsTag, int frequency){
        // 根据内容推荐，获取在全校范围内，该标签下最热门的5本书
        int countLowTag=0, countLowFreq=0;
        int type = 2;
        System.out.println("通过相似的内容推荐...");
        try{
            if (frequency > 1) {
                if (Objects.equals(intrsTag, "中国")) {
                    countLowTag++;
                    System.out.println("第" + countLowTag + "个出现中国等区分度很低的兴趣标签...");
                }
                System.out.println("获取到该学生的最大兴趣标签为: " + intrsTag);
                String getBooksByContentFreq = "select * from frequency_marc_books_detail_type where book_type = ? order by frequency desc limit 5";
                String getBooksByContentQuery = "select * from frequency_marc_books_detail_type where book_type = ? order by query_times desc limit 5";

                PreparedStatement getBoooksByContentFreqStmt = conn.prepareStatement(getBooksByContentFreq);
                PreparedStatement getBooksByContentQueryStmt = conn.prepareStatement(getBooksByContentQuery);

                getBoooksByContentFreqStmt.setString(1, intrsTag);
                getBooksByContentQueryStmt.setString(1, intrsTag);

                ResultSet booksByContentFreq = getBoooksByContentFreqStmt.executeQuery();
                ResultSet booksByContentQuery = getBooksByContentQueryStmt.executeQuery();

                ArrayList<Book> bookListByFreq = new ArrayList<>();
                ArrayList<Book> bookListByQuery = new ArrayList<>();

                while (booksByContentFreq.next()) {
                    Book book = getBookFromResult(booksByContentFreq, type);
                    if (book != null){
                        bookListByFreq.add(book);
                    }
                }


                while (booksByContentQuery.next()) {
                    Book book = getBookFromResult(booksByContentQuery, type);
                    if (book!=null) {
                        bookListByQuery.add(book);
                    }
                }

                int toStringType;

                System.out.println("\n该标签下的热门Top5借阅书籍为:");
                for (Book book : bookListByFreq) {
                    toStringType = 2;
                    System.out.println(book.toString(toStringType));
                }

                System.out.println("\n该标签下的热门Top5浏览书籍为:");
                for (Book book : bookListByQuery){
                    toStringType = 1;
                    System.out.println(book.toString(toStringType));
                }
            } else {
                countLowFreq++;
                System.out.println("第" + countLowFreq + "个兴趣标签的最高频数等于1，无法获取焦点兴趣...");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // 看过这本书的人也看了...
    public void recommendByReadBothPerson(String marc_no){

    }

    public Book getBookFromResult(ResultSet bookResult, int type){
        Book book = null;
        try {
            String marcNo = bookResult.getString("marc_no");
            String bookTitle = bookResult.getString("book_title");
            String bookAuthor = bookResult.getString("book_author");
            String bookType = bookResult.getString("book_type");
            String bookIsbn = bookResult.getString("book_isbn");
            String storeArea = bookResult.getString("store_area");
            String whereNum = bookResult.getString("where_num");
            int queryTimes = bookResult.getInt("query_times");
            int frequency = bookResult.getInt("frequency");
            if (type == 1) {
                String bookPublisher = bookResult.getString("book_publisher");
                book = new Book(marcNo, bookTitle, bookAuthor, bookType,
                        bookPublisher, bookIsbn, storeArea, whereNum, queryTimes, frequency);
            } else if (type == 2){
                book = new Book(marcNo, bookTitle, bookAuthor, bookType,
                        bookIsbn, storeArea, whereNum, queryTimes, frequency);
            }
            return book;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Map<String, Integer> getTagByStu(String stuId){
        ArrayList<String> tagList = new ArrayList<>();
        HashMap<String, Integer> resultMap = new HashMap();
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
            int frequency = 0;
            while (interestTags.next()) {
                String intrsTag = interestTags.getString("label_value");
                frequency = interestTags.getInt("frequency");
                if (frequency > 1) {
                    if (Objects.equals(intrsTag, "中国")) {
                        countLowTag++;
                        System.out.println("第" + countLowTag + "个出现中国等区分度很低的兴趣标签...");
                    }
                    tagList.add(intrsTag);
                    resultMap.put(intrsTag,frequency);
                }
            }
            return resultMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
