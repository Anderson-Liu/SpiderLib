import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Created by anderson on 2017/3/7.
 */
public class GetMySqlConn {
    // JDBC 驱动名及数据库 URL
    private final String JDBC_DRIVER = "com.mysql.jdbc.Driver";
    private final String DB_URL = "jdbc:mysql://localhost:3306/ahaulib?autoReconnect=true&useSSL=false";
    // 数据库的用户名与密码，需要根据自己的设置
    private final String USER = "root";
    private final String PASS = "anderson";
    private Connection conn = null;
    private Statement stmt = null;

    private boolean init() {
        try {
            if (conn == null) {
                Class.forName(JDBC_DRIVER);
                conn = DriverManager.getConnection(DB_URL, USER, PASS);
            }
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn != null;
    }

    public Connection getConn() {
        boolean isOk = init();
        if (isOk) {
            return conn;
        } else{
            try {
                conn =  DriverManager.getConnection(DB_URL, USER, PASS);
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return conn;
        }
    }
}
