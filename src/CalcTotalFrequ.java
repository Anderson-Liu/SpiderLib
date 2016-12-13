import java.sql.*;

/**
 * Created by anderson on 2016/12/10.
 * 有待后续改造成多线程模式
 */
public class CalcTotalFrequ {
    private static final String TYPE_TITLE = "title";
    private static final String TYPE_AUTHOR = "author";
    private static final String TYPE_TYPE = "type";
    private static final int COUNT_TO_IGNORE = 0;
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

    static class TypeRunnable implements Runnable{

        PreparedStatement insertStat, getTypeStatmt;
        TypeRunnable(PreparedStatement insertStat, PreparedStatement getTypeStatmt){
            this.insertStat = insertStat;
            this.getTypeStatmt = getTypeStatmt;
        }

        @Override
        public void run() {
            try{
                int countType = 0;
                Long beginTime = System.currentTimeMillis();
                ResultSet typeResult = getTypeStatmt.executeQuery();
                while (typeResult.next()) {
                    countType++;
                    if (countType < COUNT_TO_IGNORE){
                        continue;
                    }
                    String bookType = typeResult.getString("book_type");
                    String querySumCountByType = "select sum(borrowed_count) as total_count from isbn_borrow where book_type=? LIMIT 1";
                    PreparedStatement statSumCountByType = conn.prepareStatement(querySumCountByType);
                    statSumCountByType.setString(1, bookType);
                    ResultSet resultSumCountByType =  statSumCountByType.executeQuery();

                    while (resultSumCountByType.next()) {
                        // PreparedStatement insertTypeStat = conn.prepareStatement(insertSql);
                        int totalCount = resultSumCountByType.getInt("total_count");
                        insertStat.setString(1, TYPE_TYPE);
                        insertStat.setString(2, bookType);
                        insertStat.setInt(3, totalCount);
                        insertStat.setInt(4, totalCount);
                        System.out.println("正在处理第" + countType + "个书籍种类的热度信息...");
                        System.out.println(bookType + "\t" +  totalCount);
                        // insertTypeStat.executeUpdate();
                        insertStat.addBatch();
                    }
                    if (countType%1000 == 0) {
                        insertStat.executeBatch();
                        conn.commit();
                        insertStat.clearBatch();
                        Long endTime = System.currentTimeMillis();
                        System.out.println("本批次标题： " + countType + "pst+bat用时："+(endTime-beginTime)+"毫秒");
                    }
                }
                insertStat.executeBatch();
                conn.commit();
                insertStat.clearBatch();
                Long endTime = System.currentTimeMillis();
                System.out.println("总共批次标题： " + countType + "pst+bat用时："+(endTime-beginTime)+"毫秒");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    static class AuthorRunnable implements Runnable {
        PreparedStatement insertStat, getAuthorStatmt;

        AuthorRunnable(PreparedStatement insertStat, PreparedStatement getAuthorStatmt){
            this.insertStat = insertStat;
            this.getAuthorStatmt = getAuthorStatmt;
        }

        @Override
        public void run() {
            try{
                int countAuthor=0;
                Long beginTime = System.currentTimeMillis();
                ResultSet authorResult = getAuthorStatmt.executeQuery();
                while (authorResult.next()){
                    countAuthor++;
                    if (countAuthor < COUNT_TO_IGNORE){
                        continue;
                    }
                    String bookAuthor = authorResult.getString("book_author");
                    String querySumCountByAuthor = "select sum(borrowed_count) as total_count from isbn_borrow where book_author=? LIMIT 1";
                    PreparedStatement statSumCountByAuthor = conn.prepareStatement(querySumCountByAuthor);
                    statSumCountByAuthor.setString(1, bookAuthor);
                    ResultSet resultSumCountByAuthor =  statSumCountByAuthor.executeQuery();
                    while (resultSumCountByAuthor.next()){
                        // PreparedStatement insertAuthorStat = conn.prepareStatement(insertSql);
                        int totalCount = resultSumCountByAuthor.getInt("total_count");
                        insertStat.setString(1, TYPE_AUTHOR);
                        insertStat.setString(2, bookAuthor);
                        insertStat.setInt(3, totalCount);
                        insertStat.setInt(4, totalCount);
                        System.out.println("正在处理第" + countAuthor + "个作者的热度信息...");
                        System.out.println(bookAuthor + "\t" +  totalCount);
                        // insertAuthorStat.executeUpdate();
                        insertStat.addBatch();
                    }
                    if (countAuthor%1000 == 0) {
                        insertStat.executeBatch();
                        conn.commit();
                        insertStat.clearBatch();
                        Long endTime = System.currentTimeMillis();
                        System.out.println("本批次标题： " + countAuthor + "pst+bat用时："+(endTime-beginTime)+"毫秒");
                    }
                }
                insertStat.executeBatch();
                conn.commit();
                insertStat.clearBatch();
                Long endTime = System.currentTimeMillis();
                System.out.println("总共批次标题： " + countAuthor + "pst+bat用时："+(endTime-beginTime)+"毫秒");
            } catch (SQLException e) {
                e.printStackTrace();
            }

        }
    }

    static class TitleRunnable implements Runnable {
        PreparedStatement insertStat;
        PreparedStatement getTitleStatmt;

        TitleRunnable(PreparedStatement insertStat, PreparedStatement getTitleStatmt) {
            this.insertStat = insertStat;
            this.getTitleStatmt = getTitleStatmt;
        }

        @Override
        public void run() {
            try{
                int countTitle = 0;
                Long beginTime = System.currentTimeMillis();
                ResultSet titleResult = getTitleStatmt.executeQuery();
                while (titleResult.next()){
                    countTitle++;
                    if (countTitle < COUNT_TO_IGNORE){
                        continue;
                    }
                    String bookTitle = titleResult.getString("book_title");
                    String querySumCountByTitle = "select sum(borrowed_count) as total_count from isbn_borrow where book_title=? LIMIT 1";
                    PreparedStatement statSumCountByTitle = conn.prepareStatement(querySumCountByTitle);
                    statSumCountByTitle.setString(1, bookTitle);
                    ResultSet resultSumCountByTitle = statSumCountByTitle.executeQuery();
                    while (resultSumCountByTitle.next()){
                        int totalCount = resultSumCountByTitle.getInt("total_count");
                        insertStat.setString(1, TYPE_TITLE);
                        insertStat.setString(2, bookTitle);
                        insertStat.setInt(3, totalCount);
                        insertStat.setInt(4, totalCount);
                        System.out.println("正在处理第" + countTitle + "个书名的热度信息...");
                        System.out.println(bookTitle + "\t" +  totalCount);
                        // insertTitleStat.executeUpdate();
                        insertStat.addBatch();
                    }
                    if (countTitle%1000 == 0) {
                        insertStat.executeBatch();
                        conn.commit();
                        insertStat.clearBatch();
                        Long endTime = System.currentTimeMillis();
                        System.out.println("本批次标题： " + countTitle + "pst+bat用时："+(endTime-beginTime)+"毫秒");
                    }
                }
                insertStat.executeBatch();
                conn.commit();
                insertStat.clearBatch();
                Long endTime = System.currentTimeMillis();
                System.out.println("总共批次标题： " + countTitle + "pst+bat用时："+(endTime-beginTime)+"毫秒");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String args[]) {
        // System.out.println("运行测试...");

        String queryTitle = "select distinct book_title from isbn_borrow";
        String queryAuthor = "select distinct book_author from isbn_borrow";
        String queryType = "select distinct book_type from isbn_borrow";

        try {

            String insertSql = "insert into total_frequency(type, value, total_count)" +
                    "values(?, ?, ?) ON DUPLICATE KEY UPDATE total_count=total_count + ?";

            conn.setAutoCommit(false);
            Long beginTime = System.currentTimeMillis();
            PreparedStatement insertStat = conn.prepareStatement(insertSql);

            PreparedStatement getTitleStatmt = conn.prepareStatement(queryTitle);
            PreparedStatement getAuthorStatmt = conn.prepareStatement(queryAuthor);
            PreparedStatement getTypeStatmt = conn.prepareStatement(queryType);

            TitleRunnable titleRunnable = new TitleRunnable(insertStat, getTitleStatmt);
            AuthorRunnable authorRunnable =  new AuthorRunnable(insertStat, getAuthorStatmt);
            TypeRunnable typeRunnable = new TypeRunnable(insertStat, getTypeStatmt);

            titleRunnable.run();
            authorRunnable.run();
            typeRunnable.run();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
