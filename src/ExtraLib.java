/**
 * Created by anderson on 17-5-7.
 */

import us.codecraft.webmagic.selector.Html;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Created by anderson on 2017/4/5.
 */
public class ExtraLib {
    static int n=0;
    static Connection conn = new GetMySqlConn().getConn();
    public static void main(String args[]) throws IOException, SQLException {
        String filename = "/var/tmp/all_books_html.txt";
        FileInputStream inputStream = new FileInputStream(filename);
        Scanner sc = new Scanner(inputStream, "UTF-8");
        ArrayList<String> arrayList = new ArrayList<>();

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            arrayList.add(line);
            if (arrayList.get(arrayList.size()-1).contains("</html>")) {
                n++;
                String result = String.join("\n", arrayList).trim();
                arrayList.clear();
                if (!result.isEmpty()) {
                    parseAndSave(result);
                }
            }
        }
//        prepareCorpus();
    }

    public static void parseAndSave(String result) throws SQLException {
        Html html = new Html(result);
        String title = html.xpath("title").replace("(?is)<.*?>", "").toString();
        List<String> book_ids = html.xpath("div/table/tbody/tr[2]/td").all();
        if (book_ids.size() == 0) {
            return;
        } else {
            System.out.println("This book have " + book_ids.size() + " copies");
        }
        String marc_no = html.regex("marc_no=[0-9]{10}").get().split("=")[1];
        PreparedStatement stmt = null;
        for (String book_id : book_ids) {
            book_id = book_id.replace("<td>", "").replace("</td>", "");
            System.out.println("第" + n + "本书籍: " + title + ", id: " + book_id);
            String sql = "insert ignore into all_booksid_with_title(marc_no, book_id, book_title) " +
                    "values(?, ?, ?)";
            stmt = conn.prepareStatement(sql);
            stmt.setString(1, marc_no);
            stmt.setString(2, book_id);
            stmt.setString(3, title);
            stmt.execute();
        }
    }

    public static void prepareCorpus() throws SQLException, IOException {
        String queryBookId = "select stu_id, book_id from borrow_book_record order by stu_id";
        PreparedStatement bookIdStmt = conn.prepareStatement(queryBookId);
        ResultSet bookIdResult = bookIdStmt.executeQuery();
        String sql = "select book_title from all_booksid_with_title where book_id = ? ";
        PreparedStatement stmt = null;
        stmt = conn.prepareStatement(sql);
        FileWriter fileWriter = new FileWriter("borrow_book_title4.txt");
        int nBooks = 0;
        int nStu = 0;
        while (bookIdResult.next()) {
            String stuId = bookIdResult.getString(1);
            String bookId = bookIdResult.getString(2);
            stmt.setString(1, bookId);
            ResultSet result = stmt.executeQuery();
            if (result.wasNull()){
                System.out.println("Can't find StuId: " + stuId + " 's bookId  " + bookId + "  's title!");
            }
            while (result.next()) {
                nBooks++;
                String title = result.getString(1);
                fileWriter.append(title.replaceAll("[\\s]+", "_") + " ");
                System.out.println(nBooks + " " + title.replaceAll("[\\s]+", "_") + " " + bookId);
            }
        }
        fileWriter.flush();
        fileWriter.close();
    }

    private static void calcBorrowPerStudent() throws SQLException {
        String queryStuId = "select distinct(stu_id) from borrow_book_record";
        PreparedStatement stuIdStmt = conn.prepareStatement(queryStuId);
        ResultSet stuIdResult = stuIdStmt.executeQuery();

        String queryBookCount = "select count(book_id) from borrow_book_record where stu_id=?";
        PreparedStatement bookCountStmt = conn.prepareStatement(queryBookCount);


        while (stuIdResult.next()) {
            String stuId = stuIdResult.getString(1);
            bookCountStmt.setString(1, stuId);
            ResultSet bookCountResult = bookCountStmt.executeQuery();
            while (bookCountResult.next()) {
                int bookCount = bookCountResult.getInt(1);

            }
        }
    }
}




















