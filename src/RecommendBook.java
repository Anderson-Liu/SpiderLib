import paper.MapUtil;

import java.sql.*;
import java.util.*;

/**
 * Created by anderson on 2016/12/14.
 * 推荐算法
 */
// TODO: 看过这本书的人，还看了什么？ 对这群人进行目标人物兴趣匹配
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

    public static void main(String args[]) {
        String stuId = "13100501";
        String bookMarcNo = "0000207226";
        RecommendBook recommendBook = new RecommendBook();
        Map<String, Integer> resultMap = recommendBook.getTagByStu(stuId);
        for (Map.Entry<String, Integer> map : resultMap.entrySet()) {
            String tag = map.getKey();
            int frequency = map.getValue();
            // 根据相似top20用户推荐
            // recommendBook.recommendByTagTopPerson(stuId, tag, frequency);
            // 根据内容推荐
            // recommendBook.recommendByContent(tag, frequency);
            // 看过这本书的人还看了...
            recommendBook.recommendByReadBothPerson(stuId, bookMarcNo);
        }
    }

    public void recommendByTagTopPerson(String stuId, String intrsTag, int frequency) {
        int toStringType = 1;
        System.out.println("通过相似的人推荐...");
        try {
            int countLowTag = 0, countLowFreq = 0;

            if (frequency > 1) {
                if (Objects.equals(intrsTag, "中国")) {
                    countLowTag++;
                    System.out.println("第" + countLowTag + "个出现中国等区分度很低的兴趣标签...");
                }
                System.out.println("获取到该学生的最大兴趣标签为: " + intrsTag);
                // 获取在这个标签下频数最高的 Top 3 学生
                String getTop3StudentByTag = "SELECT stu_id FROM frequency_stuid_tag " +
                        "WHERE label_value=? and stu_id !=? ORDER BY frequency DESC LIMIT 3";
                PreparedStatement getTop3StuByTagStmt = conn.prepareStatement(getTop3StudentByTag);
                getTop3StuByTagStmt.setString(1, intrsTag);
                getTop3StuByTagStmt.setString(2, stuId);
                ResultSet top3Stu = getTop3StuByTagStmt.executeQuery();
                System.out.println("开始获取该标签下频数最高的三个学生...");
                int countStu = 0;
                while (top3Stu.next()) {
                    countStu++;
                    String top3StuId = top3Stu.getString("stu_id");
                    System.out.println("第" + countStu + "个top3学生为： " + top3StuId);
                    String getRecmdBooks = "SELECT DISTINCT * FROM all_books WHERE marc_no IN" +
                            "(SELECT marc_no FROM book_marc_id WHERE book_id IN" +
                            "(SELECT book_id FROM stuid_label_bookids WHERE stu_id=? AND label_value=?))" +
                            "ORDER BY query_times DESC";
                    PreparedStatement getRecmdBooksStmt = conn.prepareStatement(getRecmdBooks);
                    getRecmdBooksStmt.setString(1, top3StuId);
                    getRecmdBooksStmt.setString(2, intrsTag);
                    ResultSet recmdBooks = getRecmdBooksStmt.executeQuery();
                    ArrayList<Book> bookListPerStu = new ArrayList<>();
                    while (recmdBooks.next()) {
                        // 这里考虑能否使用OCM框架或者类似Gson简化Book对象的构造
                        Book book = getBookFromResult(recmdBooks);
                        if (book != null) {
                            bookListPerStu.add(book);
                        }
                    }
                    // 是否这里只取前面三本书？还是第一个人取全部，其他人取浏览量前三的书
                    System.out.println("\n看这方面的书籍最多的人，他们看的书籍为");
                    for (Book book : bookListPerStu) {
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


    public void recommendByContent(String intrsTag, int frequency) {
        // 根据内容推荐，获取在全校范围内，该标签下最热门的5本书
        int countLowTag = 0, countLowFreq = 0;

        System.out.println("通过相似的内容推荐...");
        try {
            if (frequency > 1) {
                if (Objects.equals(intrsTag, "中国")) {
                    countLowTag++;
                    System.out.println("第" + countLowTag + "个出现中国等区分度很低的兴趣标签...");
                }
                System.out.println("获取到该学生的最大兴趣标签为: " + intrsTag);
                String getBooksByContentFreq = "SELECT * FROM all_books_detail_type WHERE book_type = ? ORDER BY frequency DESC LIMIT 5";
                String getBooksByContentQuery = "SELECT * FROM all_books_detail_type WHERE book_type = ? ORDER BY query_times DESC LIMIT 5";

                PreparedStatement getBoooksByContentFreqStmt = conn.prepareStatement(getBooksByContentFreq);
                PreparedStatement getBooksByContentQueryStmt = conn.prepareStatement(getBooksByContentQuery);

                getBoooksByContentFreqStmt.setString(1, intrsTag);
                getBooksByContentQueryStmt.setString(1, intrsTag);

                ResultSet booksByContentFreq = getBoooksByContentFreqStmt.executeQuery();
                ResultSet booksByContentQuery = getBooksByContentQueryStmt.executeQuery();

                ArrayList<Book> bookListByFreq = new ArrayList<>();
                ArrayList<Book> bookListByQuery = new ArrayList<>();

                while (booksByContentFreq.next()) {
                    Book book = getBookFromResult(booksByContentFreq);
                    if (book != null) {
                        bookListByFreq.add(book);
                    }
                }

                while (booksByContentQuery.next()) {
                    Book book = getBookFromResult(booksByContentQuery);
                    if (book != null) {
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
                for (Book book : bookListByQuery) {
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
    public void recommendByReadBothPerson(String oriStuId, String marcNo) {
        int toStringType = 1;
        int stuCount = 0;
        int borrowCount = 0;
        int finalStuCount = 0;
        // 获取看过这本书的学生列表
        String getStuId = "SELECT stu_id FROM borrow_book_record WHERE book_id IN" +
                "(SELECT book_id FROM book_marc_id WHERE marc_no=? AND stu_id!=?)";
        String getStuCount = "SELECT count(stu_id) AS count_stu FROM borrow_book_record WHERE book_id IN" +
                "(SELECT book_id FROM book_marc_id WHERE marc_no=? AND stu_id!=?)";

        try {
            PreparedStatement getStuCountStmt = conn.prepareStatement(getStuCount);
            getStuCountStmt.setString(1, marcNo);
            getStuCountStmt.setString(2, oriStuId);
            ResultSet stuCountResult = getStuCountStmt.executeQuery();
            if (stuCountResult.next()) {
                stuCount = stuCountResult.getInt("count_stu");
            }

            ArrayList<String> tagsList = getTagsByBook(marcNo);

            // 获取学生列表中学生总数，如果大于5，那么取总借阅数大于15的学生作为k-近邻学生
            PreparedStatement getStuIdStmt = conn.prepareStatement(getStuId);
            getStuIdStmt.setString(1, marcNo);
            getStuIdStmt.setString(2, oriStuId);
            ArrayList<Book> bookListAllStu = new ArrayList<>();
            ResultSet stuIds = getStuIdStmt.executeQuery();
            Map<Book, Integer> allRecmndBooks = new HashMap<>();

            String stuId = "";

            while (stuIds.next()) {
                finalStuCount++;
                for (String tag : tagsList) {
                    System.out.println("\n标签为:" + tag);
                    ArrayList<Book> bookListPerStuPerTag = new ArrayList<>();
                    stuId = stuIds.getString("stu_id");
                    String getBorrowCount = "SELECT total_borrow FROM student WHERE stu_id=?";
                    PreparedStatement getBorrowCountStmt = conn.prepareStatement(getBorrowCount);
                    getBorrowCountStmt.setString(1, stuId);
                    ResultSet borrowCountResult = getBorrowCountStmt.executeQuery();
                    if (borrowCountResult.next()) {
                        borrowCount = borrowCountResult.getInt("total_borrow");
                    }

                    // 如果学生数量大于5，那么取总借阅数大于15的学生作为k-近邻学生
                    if (stuCount > 5) {
                        if (borrowCount < 15) {
                            continue;
                        }
                    }

                    System.out.println("\n这是第" + finalStuCount + "个学生，他看了这本书后在标签-" + tag + "-也看了这些： ");
                    String getBooksByStu = "SELECT DISTINCT * FROM all_books_detail_type WHERE marc_no IN" +
                            "(SELECT marc_no FROM book_marc_id WHERE book_id IN" +
                            "(SELECT book_id FROM borrow_book_record WHERE stu_id = ?)) AND book_type = ? AND marc_no!=? ORDER BY query_times";
                    PreparedStatement getBookByStuStmt = conn.prepareStatement(getBooksByStu);
                    // Array tagsArray = conn.createArrayOf("VARCHAR(45)",  tags);

                    getBookByStuStmt.setString(1, stuId);
                    getBookByStuStmt.setString(2, tag);
                    getBookByStuStmt.setString(3, marcNo);
                    ResultSet booksResult = getBookByStuStmt.executeQuery();
                    while (booksResult.next()) {
                        Book book = getBookFromResult(booksResult);
                        bookListPerStuPerTag.add(book);
                        bookListAllStu.add(book);
                    }

                    for (Book book : bookListPerStuPerTag) {
                        System.out.println(book.toString(toStringType));
                    }
                }


            }

            int matchIndex;
            Map<Book, Integer> bookFreqncyMap = new HashMap<>();
            ArrayList<String> marcNoList = new ArrayList<>();

            List<Book> uniqueBooks = new ArrayList<>();

            // 去重
            for (Book book : bookListAllStu) {
                if (marcNoList.contains(book.getMarcNo())) {
                    continue;
                }
                marcNoList.add(book.getMarcNo());
                uniqueBooks.add(book);
            }

            // 计算频数
            for (Book book : uniqueBooks) {
                matchIndex = 0;
                for (Book book2 : bookListAllStu) {
                    if (book.getMarcNo().equals(book2.getMarcNo())) {
                        matchIndex++;
                    }
                }
                bookFreqncyMap.put(book, matchIndex);
            }

            // 按频数排行
            bookFreqncyMap = MapUtil.sortByValue(bookFreqncyMap);
            System.out.println("\n推荐书籍汇总:");
            for (Map.Entry<Book, Integer> entry : bookFreqncyMap.entrySet()) {
                Book book = entry.getKey();
                matchIndex = entry.getValue();
                System.out.println(book.toString(toStringType) + " 借阅热度:" + matchIndex );
                allRecmndBooks.put(book, matchIndex);
            }
            System.out.println("Debug");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Book getBookFromResult(ResultSet bookResult) {
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
            book = new Book(marcNo, bookTitle, bookAuthor, bookType,
                    bookIsbn, storeArea, whereNum, queryTimes, frequency);
            return book;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> getTagsByBook(String marcNo) {
        String getTag = "SELECT book_type FROM all_books_detail_type WHERE marc_no = ?";
        ArrayList<String> tagList = new ArrayList<>();
        try {
            PreparedStatement getTagStmt = conn.prepareStatement(getTag);
            getTagStmt.setString(1, marcNo);
            ResultSet tags = getTagStmt.executeQuery();
            while (tags.next()) {
                String tag = tags.getString("book_type");
                tagList.add(tag);
            }
            return tagList;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return tagList;
    }

    public Map<String, Integer> getTagByStu(String stuId) {
        ArrayList<String> tagList = new ArrayList<>();
        HashMap<String, Integer> resultMap = new HashMap();
        final String LABEL_TYPE = "book_type_detail";
        int countLowFreq = 0, countLowTag = 0;
        // 获取学生频数最高的兴趣, 只获取一个兴趣标签
        String getInterestTag = "SELECT label_value, frequency FROM frequency_stuid_tag " +
                "WHERE stu_id=? AND label_type=? ORDER BY frequency DESC LIMIT 1";

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
                    resultMap.put(intrsTag, frequency);
                }
            }
            return resultMap;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultMap;
    }
}
