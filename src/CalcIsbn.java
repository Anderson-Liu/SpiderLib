import java.sql.*;

/**
 * Created by anderson on 2016/12/10.
 */
public class CalcIsbn {
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
        String queryIsbn = "select DISTINCT isbn from books";
        try {
            ResultSet isbnResult = conn.prepareStatement(queryIsbn).executeQuery();
            String isbn, bookId, bookTitle = null, bookAuthor = null, bookType=null;
            int totalCount = 0;
            while (isbnResult.next()){
                totalCount++;
                int totalBorrow = 0;
                isbn = isbnResult.getString("isbn");
                System.out.println("开始统计第" + totalCount + "个ISBN号码，为" + isbn + "的书籍。。。");
                String queryBookIdByIsbn = "Select book_id, book_title, book_author, book_type from books where isbn=?";
                PreparedStatement queryIdStatmt = conn.prepareStatement(queryBookIdByIsbn);
                queryIdStatmt.setString(1, isbn);
                ResultSet bookIdResult = queryIdStatmt.executeQuery();
                int bookCount = 0;
                while (bookIdResult.next()) {
                    bookCount++;
                    System.out.println("开始统计该ISBN的第" + bookCount + "本书......");
                    String queryCountById = "select count(book_id) AS borrow_count from student_book where book_id=?";
                    bookId = bookIdResult.getString("book_id");
                    PreparedStatement queryCountStatmt = conn.prepareStatement(queryCountById);
                    queryCountStatmt.setString(1, bookId);
                    ResultSet countResult = queryCountStatmt.executeQuery();

                    while (countResult.next()){
                        totalBorrow += countResult.getInt("borrow_count");
                        bookTitle = bookIdResult.getString("book_title");
                        bookAuthor = bookIdResult.getString("book_author");
                        bookType = bookIdResult.getString("book_type");
                    }
                }

                System.out.println("这本书的信息为: " + bookTitle +"\t" + bookAuthor +"\t"  + bookType +"\t" + totalBorrow);
                String insertSql = "INSERT IGNORE INTO isbn_borrow(isbn, book_title, book_author, book_type, borrowed_count)" +
                        "VALUES (?, ?, ?, ?, ?)";
                PreparedStatement insertStatmt = conn.prepareStatement(insertSql);
                insertStatmt.setString(1, isbn);
                insertStatmt.setString(2, bookTitle);
                insertStatmt.setString(3, bookAuthor);
                insertStatmt.setString(4, bookType);
                insertStatmt.setInt(5, totalBorrow);
                insertStatmt.executeUpdate();
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

    }
}
