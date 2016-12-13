import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by anderson on 2016/12/13.
 */
public class GetAllBooks {

    static CloseableHttpClient httpClient = HttpClients.createDefault();
    static final int TIMEOUT_MILLIS = 8000;
    static RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(TIMEOUT_MILLIS)
            .setConnectTimeout(TIMEOUT_MILLIS)
            .setConnectionRequestTimeout(TIMEOUT_MILLIS)
            .build();
    static String url = "jdbc:mysql://localhost:3306/ahaulib?"
            + "user=root&password=anderson&useUnicode=true&characterEncoding=UTF8";
    static Connection conn;

    static int connCount = 0;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            conn = DriverManager.getConnection(url);
            conn.setAutoCommit(false);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    static PreparedStatement insertAllStmt, insertRelationStmt;

    static String insertAllSql = "insert IGNORE into all_books(marc_no, book_title, book_author, book_type, book_publisher, book_isbn, store_area, where_num, queryTimes)" +
            "values(?, ?, ?, ?, ?, ?, ?, ?, ?)";
    static String insertRelationSql = "insert IGNORE into book_marc_id(book_id, marc_num, is_borrowable) VALUES(?, ?, ?)";

    public static void main(String args[]){
        String url_10 = "http://opac.ahau.edu.cn/opac/item.php?marc_no=000000000";
        String url_100 = "http://opac.ahau.edu.cn/opac/item.php?marc_no=00000000";
        String url_1000 = "http://opac.ahau.edu.cn/opac/item.php?marc_no=0000000";
        String url_10000 = "http://opac.ahau.edu.cn/opac/item.php?marc_no=000000";
        String url_100000 = "http://opac.ahau.edu.cn/opac/item.php?marc_no=00000";
        String url_257301 = "http://opac.ahau.edu.cn/opac/item.php?marc_no=0000";

        int count_10 = 10;
        int count_100 = 100;
        int count_1000 = 1000;
        int count_10000 = 10000;
        int count_100000 = 100000;
        int count_257301 = 257302;
        int totalBookCount = 257301;

        try {
            insertAllStmt = conn.prepareStatement(insertAllSql);
            PreparedStatement insertRelationStmt = conn.prepareStatement(insertRelationSql);
            new Thread(new GetAllBooks().new ThreadInsert(count_10, url_10, insertAllStmt, insertRelationStmt)).start();
            new Thread(new GetAllBooks().new ThreadInsert(count_100, url_100, insertAllStmt, insertRelationStmt)).start();
            new Thread(new GetAllBooks().new ThreadInsert(count_1000, url_1000, insertAllStmt, insertRelationStmt)).start();
            new Thread(new GetAllBooks().new ThreadInsert(count_10000, url_10000, insertAllStmt, insertRelationStmt)).start();
            new Thread(new GetAllBooks().new ThreadInsert(count_100000, url_100000, insertAllStmt, insertRelationStmt)).start();
            new Thread(new GetAllBooks().new ThreadInsert(count_257301, url_257301, insertAllStmt, insertRelationStmt)).start();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void process(int count, String queryPrefix, PreparedStatement insertAllStmt, PreparedStatement insertRelationStmt) {
        String isbn = "", marc_no;
        String bookId = null, bookTitle = null, bookAuthor=null, storeArea=null,
                isBorrowable=null, whereNum=null, bookType=null, publisher=null;
        String queryUrl;
        int countBooks=0;
        int startAcount = 1;
        switch (count) {
            case 10:
                startAcount = 1;
                break;
            case 100:
                startAcount = 10;
                break;
            case 1000:
                startAcount = 100;
                break;
            case 10000:
                startAcount = 1000;
                break;
            case 100000:
                startAcount = 10000;
                break;
            case 257302:
                startAcount = 100000;
                break;
        }


        int i;
        for (i=startAcount; i < count; i++) {
            countBooks++;
            System.out.println("\n\n这是第" + i + "本书...");
            queryUrl = queryPrefix + i;
            HttpGet queryGet = new HttpGet(queryUrl);
            queryGet.setConfig(requestConfig);
            try {
                CloseableHttpResponse queryResponse = httpClient.execute(queryGet);
                HttpEntity queryEntity = queryResponse.getEntity();
                String queryContent = EntityUtils.toString(queryEntity, "UTF-8");
                Document queryDocument = Jsoup.parse(queryContent);
                Elements bookDetail = queryDocument.getElementsByClass("booklist");
                Elements bookList = queryDocument.getElementsByClass("whitetext");
                int queryTime = Integer.parseInt(queryDocument.getElementById("marc")
                        .text()
                        .split("浏览次数：")[1]
                        .split("　")[0]);
                System.out.println("本书被浏览" + queryTime + "次...");
                marc_no = queryUrl.split("=")[1];
                System.out.println("本页面的marc_no为:" + marc_no);

                for (Element detailElem : bookDetail) {
                    // 获取该本书籍的isbn号码
                    Elements isbnElems = detailElem.getElementsContainingText("isbn");
                    if (isbnElems.size() > 0) {
                        isbn = isbnElems.get(0).text().split(":")[1];
                        System.out.println("获取得到的ISBN号为\n" + isbn);
                    }

                    Elements titleElems = detailElem.getElementsContainingText("题名/责任者");
                    if(titleElems.size() > 0) {
                        String[] tmp = titleElems.get(0).text().split("题名/责任者:")[1].split("/");
                        if (tmp.length > 1) {
                            bookTitle = tmp[0];
                            bookAuthor = tmp[1];
                            System.out.println("获取得到的标题和作者是:" + bookTitle + "  " + bookAuthor);
                        } else {
                            bookTitle = tmp[0];
                            bookAuthor = "佚名";
                        }

                    }

                    Elements typeElems = detailElem.getElementsContainingText("学科主题");
                    if (typeElems.size() > 0) {
                        bookType = typeElems.get(0).text().split(":")[1];
                        System.out.println("获取得到的学科主题是 " + bookType);
                    }

                    Elements publisherElems = detailElem.getElementsContainingText("出版发行项");
                    if (publisherElems.size() > 0) {
                        publisher = publisherElems.get(0).text().split("出版发行项: ")[1];
                        System.out.println("获取得到的出版发行项是 " +  publisher);
                    }
                }

                if (isbn.isEmpty()) {
                    System.out.println("找不到isbn号，跳过");
                    continue;
                }  else if (isbn.length() < 15) {
                    System.out.println("获取的ISBN号码错误，跳过");
                    continue;
                } else {

                    int countChildBook = 0;
                    HashMap<String, String> idBorrowable = new HashMap<>();
                    for (Element book : bookList) {
                        countChildBook++;
                        System.out.println("\n获取该页面的第" + countChildBook + "本子书籍...");
                        if (book.childNodes().size() > 9) {

                            whereNum = book.childNode(1).childNode(0).toString();
                            System.out.println("获取得到的索书号是:" + whereNum);

                            bookId = book.childNode(3).childNode(0).toString();
                            System.out.println("获取得到的书籍ID是:" + bookId);

                            storeArea = book.childNode(7).childNode(0).toString().split(" ")[1];
                            System.out.println("获取得到的存储地址是:" + storeArea);

                            if (book.childNode(9).childNode(0).childNodeSize() > 0) {
                                isBorrowable = book.childNode(9).childNode(0).childNode(0).toString();
                                System.out.println("获取得到的可借状态是：" + isBorrowable);

                            } else {
                                isBorrowable = book.childNode(9).childNode(0).toString();
                                System.out.println("获取得到的可借状态是：" + isBorrowable);

                            }
                            idBorrowable.put(bookId, isBorrowable);

                        } else {
                            System.out.println("找不到该页面几本书籍的信息...");
                        }
                    }
                    for (Map.Entry<String, String> entry : idBorrowable.entrySet()) {
                        bookId = entry.getKey();
                        isBorrowable = entry.getValue();
                        insertRelationStmt.setString(1, bookId);
                        insertRelationStmt.setString(2, marc_no);
                        insertRelationStmt.setString(3, isBorrowable);
                        insertRelationStmt.addBatch();
                    }

                }

                insertAllStmt.setString(1, marc_no);
                insertAllStmt.setString(2, bookTitle);
                insertAllStmt.setString(3, bookAuthor);
                insertAllStmt.setString(4, bookType);
                insertAllStmt.setString(5, publisher);
                insertAllStmt.setString(6, isbn);
                insertAllStmt.setString(7, storeArea);
                insertAllStmt.setString(8, whereNum);
                insertAllStmt.setInt(9, queryTime);
                insertAllStmt.addBatch();

            } catch (IOException | SQLException e) {
                e.printStackTrace();
            }
            if (countBooks%500 == 0){
                try{
                    System.out.println("从第" + i + "开始执行插入书籍");
                    Long beginTime = System.currentTimeMillis();
                    int[] result1 = insertAllStmt.executeBatch();
                    int[] result2 = insertRelationStmt.executeBatch();
                    conn.commit();
                    Long stopTime = System.currentTimeMillis();
                    System.out.println("两句执行的Sql数目是:" + result1.length + "， " + result2.length);
                    System.out.println("执行耗时" + (stopTime - beginTime) + "毫秒");
                    insertAllStmt.clearBatch();
                    insertRelationStmt.clearBatch();

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        try{
            System.out.println("从第" + i + "开始执行插入书籍");
            Long beginTime = System.currentTimeMillis();
            int[] result1 = insertAllStmt.executeBatch();
            int[] result2 = insertRelationStmt.executeBatch();
            conn.commit();
            Long stopTime = System.currentTimeMillis();
            System.out.println("两句执行的Sql数目是:" + result1.length + "， " + result2.length);
            System.out.println("执行耗时" + (stopTime - beginTime) + "毫秒");
            insertAllStmt.clearBatch();
            insertRelationStmt.clearBatch();

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    class ThreadInsert implements Runnable{
        int count;
        String queryUrl;
        PreparedStatement insertAllStmt;
        PreparedStatement insertRelationStmt;

        ThreadInsert(int count, String queryUrl, PreparedStatement insertAllStmt, PreparedStatement insertRelationStmt){
            this.count = count;
            this.queryUrl = queryUrl;
            this.insertAllStmt = insertAllStmt;
            this.insertRelationStmt = insertRelationStmt;
        }

        @Override
        public void run() {
            process(count, queryUrl, insertAllStmt, insertRelationStmt);
        }
    }
}
