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

import java.io.*;
import java.sql.*;
import java.sql.Date;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by Enn on 2016/3/20.
 */
public class TestParse {

    public static final int MYSQL_DUPLICATE_PK = 1062;

    public static void main(String args[]) {
        try {

            String url = "jdbc:mysql://localhost:3306/ahaulib?"
                    + "user=root&password=anderson&useUnicode=true&characterEncoding=UTF8";
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            Connection conn = DriverManager.getConnection(url);
            String content = "";
            // parsRecent(conn, content);
            String pathName = "ResultData//temp.txt";
            File file = new File(pathName);
            FileInputStream reader = null;
            reader = new FileInputStream(file);
            String encoding = "utf-8";
            Long fileLength = file.length();
            byte[] fileContent = new byte[fileLength.intValue()];
            reader.read(fileContent);
            content = new String(fileContent, encoding);
            reader.close();
            // parsPersonInfo(conn, content);
            String stuId = "15104400";
            // parsRecent(stuId, conn, content);
            parseHist(stuId, conn, content);
        } catch (ClassNotFoundException | SQLException | IOException e) {
            e.printStackTrace();
        }
    }

    public static void parseHist(String stuId, Connection conn, String content) {
        System.out.println("开始解析历史借阅数据...");
        Document document = Jsoup.parse(content);
        Elements whiteText = document.getElementsByClass("whitetext");
        String bookTitle = null;
        String bookId = null;
        String authorName = null;
        java.util.Date borrowDate = null;
        java.util.Date returnDate = null;
        String storeArea = null;
        String tempName = null;
        String bookUri = null;
        String baseUri = "http://opac.ahau.edu.cn";
        String bookUrl = null;
        HashMap<String, String> moreBookInfo = new HashMap<>();
        DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
        int k = 0;
        String bookType = null;
        String isbn = null;
        String whereNum = null;
        ArrayList<Elements> allBooks = new ArrayList<>();
        Elements eachBook = new Elements();

        System.out.println("开始归并书籍信息...");
        for (Element element : whiteText) {
            eachBook.add(element);
            k++;
            if (k == 7) {
                allBooks.add(eachBook);
                eachBook = new Elements();
                k = 0;
            }
        }

        System.out.println("开始解析每本书并存储...");
        int i=0;
        for (Elements book : allBooks) {
            i++;
            bookId = book.get(1).text();
            bookTitle = book.get(2).text().split("/")[0];
            bookUri = book.get(2).childNode(0).attr("href");
            bookUri = bookUri.substring(2, bookUri.length());
            bookUrl = baseUri + bookUri;
            System.out.println("获取第" + i + "本书的额外信息...");
            moreBookInfo = parsBookInfo(bookUrl);
            isbn = moreBookInfo.get("isbn");
            whereNum = moreBookInfo.get("whereNum");
            bookType = moreBookInfo.get("bookType");
            authorName = book.get(3).text();
            storeArea = book.get(6).text();

            String sqlInsert = "INSERT INTO books(book_id, book_title, book_author, store_area, isbn, where_num, book_type) " +
                    "VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE borrowed_count=borrowed_count+1";
            PreparedStatement preparedStmt = null;
            try {
                preparedStmt = conn.prepareStatement(sqlInsert);

                preparedStmt.setString(1, bookId);
                preparedStmt.setString(2, bookTitle);
                preparedStmt.setString(3, authorName);
                preparedStmt.setString(4, storeArea);
                preparedStmt.setString(5, isbn);
                preparedStmt.setString(6, whereNum);
                preparedStmt.setString(7, bookType);
                int result = preparedStmt.executeUpdate();

                sqlInsert = "INSERT IGNORE INTO student_book(stu_id, book_id, borrow_date, return_date)" +
                        "VALUES (?, ?, ?, ?)";
                preparedStmt = conn.prepareStatement(sqlInsert);
                preparedStmt.setString(1, stuId);
                preparedStmt.setString(2, bookId);
                borrowDate = dateFmt.parse(book.get(4).text());
                returnDate = dateFmt.parse(book.get(5).text());
                java.sql.Date sqlBorrowDate = new java.sql.Date(borrowDate.getTime());
                java.sql.Date sqlReturnDate = new java.sql.Date(returnDate.getTime());
                preparedStmt.setDate(3, sqlBorrowDate);
                preparedStmt.setDate(4, sqlReturnDate);
                result = preparedStmt.executeUpdate();

            } catch (SQLException e) {
                int errorCode = e.getErrorCode();
                if (errorCode == MYSQL_DUPLICATE_PK) {
                    System.out.println(e.getSQLState());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private static HashMap<String, String> parsBookInfo(String bookUrl) {

        CloseableHttpClient httpClient = HttpClients.createDefault();
        final int TIMEOUT_MILLIS = 8000;
        RequestConfig requestConfig = RequestConfig.custom()
                .setSocketTimeout(TIMEOUT_MILLIS)
                .setConnectTimeout(TIMEOUT_MILLIS)
                .setConnectionRequestTimeout(TIMEOUT_MILLIS)
                .build();
        HttpGet httpGet = new HttpGet(bookUrl);
        httpGet.setConfig(requestConfig);
        CloseableHttpResponse infoGetResponse = null;
        Document document = null;
        Elements elements = null;
        HashMap<String, String> moreBookInfo = new HashMap<>();


        try {
            infoGetResponse = httpClient.execute(httpGet);
            HttpEntity infoGetEntity = infoGetResponse.getEntity();
            String infoContent = EntityUtils.toString(infoGetEntity, "UTF-8");
            document = Jsoup.parse(infoContent);
            elements = document.getElementsByClass("booklist");
            String bookType = null;
            String whereNum=null;
            for (Element element : elements) {
                Elements elements1 = element.getElementsContainingText("学科主题");
                Elements elements3 = element.getElementsContainingText("个人名称主题");
                if (elements1.size() > 0) {
                    bookType = elements1.get(0).text().split(":")[1];
                } else if (elements3.size() > 0) {
                    bookType = elements3.get(0).text().split(":")[1];
                }
                Elements elements2 = element.getElementsContainingText("中图法分类号");
                if (elements2.size() > 0) {
                    whereNum = elements2.get(0).text().split(":")[1];
                }
            }
            String isbn = elements.get(2).text().split(":")[1].trim();
            moreBookInfo.put("isbn", isbn);
            moreBookInfo.put("bookType", bookType);
            moreBookInfo.put("whereNum", whereNum);


        } catch (IOException e) {
            e.printStackTrace();
        }
        return moreBookInfo;
    }

    public static void parsPersonInfo(Connection conn, String content) {
        System.out.println("开始解析用户数据...");
        Document document = Jsoup.parse(content);
        Elements blueText = document.getElementsByClass("bluetext");

        String id = blueText.get(1).parent().childNode(1).toString();
        String name = blueText.get(0).parent().childNode(1).toString();
        ;
        // 读者类型，本科生或者研究生
        String type = blueText.get(9).parent().childNode(1).toString();
        ;
        // 累计借阅书籍数量
        int totalBorrow = Integer.parseInt(blueText
                .get(11)
                .parent()
                .childNode(1)
                .toString()
                .split("册")[0]);
        String department = blueText.get(14).parent().childNode(1).toString();
        // 职业/职称/大学专业
        String major = blueText.get(18).parent().childNode(1).toString();
        String sex = blueText.get(20).parent().childNode(1).toString();
        String phoneNum = blueText.get(24).parent().childNode(1).toString();
        String sqlInsert = "INSERT IGNORE INTO student(id, name, sex, type, department, major, phone_num, total_borrow) " +
                "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = conn.prepareStatement(sqlInsert);
            preparedStmt.setString(1, id);
            preparedStmt.setString(2, name);
            preparedStmt.setString(3, sex);
            preparedStmt.setString(4, type);
            preparedStmt.setString(5, department);
            preparedStmt.setString(6, major);
            preparedStmt.setString(7, phoneNum);
            preparedStmt.setInt(8, totalBorrow);
            int result = preparedStmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void parsRecent(String stuID, Connection conn, String content) {
        System.out.println("开始解析最近借阅数据...");
        try {
            Document document = Jsoup.parse(content);

            // 解析借阅的书名，作者，借书,还书时间和馆藏地址
            Elements whiteText = document.getElementsByClass("whitetext");
            String bookTitle = null;
            String bookId = null;
            String authorName = null;
            java.util.Date borrowDate = null;
            java.util.Date returnDate = null;
            String storeArea = null;
            String tempName = null;
            String bookUri = null;
            String bookUrl = null;
            String baseUri = "http://opac.ahau.edu.cn";
            HashMap<String, String> moreBookInfo = new HashMap<>();
            DateFormat dateFmt = new SimpleDateFormat("yyyy-MM-dd");
            int k = 0;
            String bookType = null;
            String isbn = null;
            String whereNum = null;
            ArrayList<Elements> allBooks = new ArrayList<>();
            Elements eachBook = new Elements();

            for (Element element : whiteText) {
                eachBook.add(element);
                k++;
                if (k == 8) {
                    allBooks.add(eachBook);
                    eachBook = new Elements();
                    k = 0;
                }
            }

            for (Elements book : allBooks) {
                bookId = book.get(0).text();
                bookTitle = book.get(1).text().split("/")[0];
                bookUri = book.get(1).childNode(0).attr("href");
                bookUri = bookUri.substring(2, bookUri.length());
                bookUrl = baseUri + bookUri;
                moreBookInfo = parsBookInfo(bookUrl);
                isbn = moreBookInfo.get("isbn");
                whereNum = moreBookInfo.get("whereNum");
                bookType = moreBookInfo.get("bookType");
                authorName = book.get(1)
                        .text()
                        .split("/")[1];
                storeArea = book.get(5).text();
                String sqlInsert = "INSERT INTO books(book_id, book_title, book_author, store_area, isbn, where_num, book_type) " +
                        "VALUES(?, ?, ?, ?, ?, ?, ?) ON DUPLICATE KEY UPDATE borrowed_count=borrowed_count+1";
                PreparedStatement preparedStmt = null;
                preparedStmt = conn.prepareStatement(sqlInsert);

                preparedStmt.setString(1, bookId);
                preparedStmt.setString(2, bookTitle);
                preparedStmt.setString(3, authorName);
                preparedStmt.setString(4, storeArea);
                preparedStmt.setString(5, isbn);
                preparedStmt.setString(6, whereNum);
                preparedStmt.setString(7, bookType);

                int result = preparedStmt.executeUpdate();

                sqlInsert = "INSERT IGNORE INTO student_book(stu_id, book_id, borrow_date, return_date)" +
                        "VALUES (?, ?, ?, ?)";
                preparedStmt = conn.prepareStatement(sqlInsert);
                preparedStmt.setString(1, stuID);
                preparedStmt.setString(2, bookId);
                borrowDate = dateFmt.parse(book.get(2).text());
                returnDate = dateFmt.parse(book.get(3).text());
                java.sql.Date sqlBorrowDate = new java.sql.Date(borrowDate.getTime());
                java.sql.Date sqlReturnDate = new java.sql.Date(returnDate.getTime());
                preparedStmt.setDate(3, sqlBorrowDate);
                preparedStmt.setDate(4, sqlReturnDate);
                result = preparedStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}