import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.print.Doc;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by anderson on 2016/12/9.
 */
public class FixIsbn {

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

    static CloseableHttpClient httpClient = HttpClients.createDefault();
    static final int TIMEOUT_MILLIS = 8000;
    static RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(TIMEOUT_MILLIS)
            .setConnectTimeout(TIMEOUT_MILLIS)
            .setConnectionRequestTimeout(TIMEOUT_MILLIS)
            .build();
    static int count = 0;
    static int ignoreCount = 0;

    public static void main(String args[]) {
        String query = "SELECT book_id, book_author, where_num, isbn FROM books WHERE length(isbn)<19";
        PreparedStatement preparedStmt = null;
        try {
            preparedStmt = conn.prepareStatement(query);
            ResultSet resultSet = preparedStmt.executeQuery();
            String bookAuthor, whereNum, isbn = null, oriIsbn, oriBookId;
            // String insertSql = "INSERT IGNORE INTO isbn_borrow(isbn, book_title, book_author, book_type, borrow_count, book_count) " +
            //         "VALUES (?, ?, ?, ?, ?, ?)";
            while (resultSet.next()) {
                count++;

                if (count < ignoreCount) {
                    continue;
                }
                oriBookId = resultSet.getString("book_id");
                bookAuthor = resultSet.getString("book_author");
                oriIsbn = resultSet.getString("isbn");
                whereNum = resultSet.getString("where_num");

                System.out.println("进行第" + count + "条数据的修正, 表中bookId 为" + oriBookId);

                String url = "http://opac.ahau.edu.cn/opac/openlink.php";
                HttpGet queryGet = new HttpGet(url);
                queryGet.setConfig(requestConfig);
                URI uri = new URIBuilder(queryGet.getURI())
                        .addParameter("author", bookAuthor)
                        .addParameter("callno", whereNum)
                        .addParameter("doctype", "ALL")
                        .addParameter("lang_code", "ALL")
                        .addParameter("displaypg", "20")
                        .addParameter("showmode", "list")
                        .addParameter("sort", "CATA_DATE")
                        .addParameter("orderby", "desc")
                        .addParameter("dept", "ALL")
                        .build();
                queryGet.setURI(uri);
                CloseableHttpResponse queryResponse = httpClient.execute(queryGet);
                HttpEntity queryEntity = queryResponse.getEntity();
                String queryContent = EntityUtils.toString(queryEntity, "UTF-8");
                Document queryDocument = Jsoup.parse(queryContent);
                Elements queryListInfo = queryDocument.getElementsByClass("book_list_info");
                String bookDetailUrl;
                String baseUrl = "http://opac.ahau.edu.cn/opac/";
                // 获取查询得到的几本书
                int queryCount = 0;
                for (Element queryElement : queryListInfo) {
                    // 每获取一本书，查询正确的isbn,以及这本书的条码号，依次修改books表中所有条码号的isbn数值
                    queryCount++;
                    String bookId;
                    ArrayList<String> bookIds = new ArrayList<>();
                    bookDetailUrl = baseUrl + queryElement.select("a[href]").attr("href");
                    HttpGet detailGet = new HttpGet(bookDetailUrl);
                    detailGet.setConfig(requestConfig);
                    // 查询每本书籍的详细信息
                    CloseableHttpResponse detailRespon = httpClient.execute(detailGet);
                    Document detail = Jsoup.parse(EntityUtils.toString(detailRespon.getEntity(), "UTF-8"));
                    Elements bookDetail = detail.getElementsByClass("booklist");
                    Elements bookList = detail.getElementsByClass("whitetext");
                    for (Element detailElem : bookDetail) {
                        // 获取该本书籍的isbn号码
                        Elements isbnElem = detailElem.getElementsContainingText("isbn");
                        if (isbnElem.size() > 0) {
                            isbn = isbnElem.get(0).text().split(":")[1];
                            System.out.println("获取得到的ISBN号为\n" + isbn);
                            break;
                        }
                    }

                    if (isbn.isEmpty()) {
                        System.out.println("找不到isbn号，跳过");
                        continue;
                    }  else if (isbn.length() < 15) {
                        System.out.println("获取的ISBN号码错误，跳过");
                        continue;
                    } else {

                        for (Element book : bookList) {
                            if (book.childNodes().size()>3) {
                                bookId = book.childNode(3).childNode(0).toString();
                                bookIds.add(bookId);
                            } else {
                                System.out.println("找不到书的ID");
                            }
                        }

                        if (queryCount == 1) {
                            bookIds.add(oriBookId);
                        }

                        for (String id : bookIds) {
                            System.out.println("修正bookId为" + id + "的isbn号，" + "原来的ISBN号码为" + oriIsbn);
                            String updateSql = "update books set isbn=? where book_id=?";
                            preparedStmt = conn.prepareStatement(updateSql);
                            preparedStmt.setString(1, isbn);
                            preparedStmt.setString(2, id);
                            preparedStmt.executeUpdate();
                        }
                        conn.commit();
                    }
                }
                // queryDocument.getElementsByClass("book_list_info").get(1).select("a[href]").attr("href")

//                preparedStmt.setString(1, );
//                preparedStmt.setString(2, stuId);
//                preparedStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (URISyntaxException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
