import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

/**
 * Created by anderson on 2016/12/9.
 */
public class CalcFrequ {

    final String TYPE_TITLE = "book_title";
    final String TYPE_AUTHOR = "book_author";
    final String TYPE_TYPE = "book_type";
    String TYPE_TYPE_DETAIL = "book_type_detail";

    static String url = "jdbc:mysql://localhost:3306/ahaulib?"
            + "user=root&password=anderson&useUnicode=true&characterEncoding=UTF8";
    static Connection conn;
    int connCount = 0;

    static {
        try {
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            conn = DriverManager.getConnection(url);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
    }

    static Vector<Connection> pools = new Vector<Connection>();

    public static Connection getDBConnection(){
        try {
            Class.forName("com.mysql.jdbc.Driver");// 动态加载mysql驱动
            conn = DriverManager.getConnection(url);
            return conn;
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    static {
        int i = 0;
        while(i < 4){
            pools.add(getDBConnection());
            i++;
        }
    }

    public static synchronized Connection getPool(){
        if(pools != null && pools.size() > 0){
            int last_ind = pools.size() -1;
            return pools.remove(last_ind);
        }else{
            return getDBConnection();
        }
    }

    public static Map<String, ResultSet> getStuIdFromDepart() {
        System.out.println("执行分别对各个学院人群 的兴趣分析.......");
        String queryDepartSql = "SELECT DISTINCT department FROM student";
        PreparedStatement preparedStatement;
        ResultSet departResultSet;
        String department = null;

        HashMap<String, ResultSet> resultMap = new HashMap<>();
        try {
            preparedStatement = conn.prepareStatement(queryDepartSql);
            departResultSet = preparedStatement.executeQuery();
            while (departResultSet.next()) {
                department = departResultSet.getString("department");
                String queryIdSql = "SELECT stu_id FROM student WHERE department=?";
                preparedStatement = conn.prepareStatement(queryIdSql);
                preparedStatement.setString(1, department);
                ResultSet idResultSet = preparedStatement.executeQuery();
                resultMap.put(department, idResultSet);
            }
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return resultMap;
    }

    public static Map<String, ResultSet> getAllStuId() {
        System.out.println("执行全校师生的兴趣分析.......");
        PreparedStatement preparedStatement;
        HashMap<String, ResultSet> resultMap = new HashMap<>();
        try{
            String queryIdSql = "SELECT stu_id, name, sex, type, department, major FROM student";
            preparedStatement = conn.prepareStatement(queryIdSql);
            ResultSet idResultSet = preparedStatement.executeQuery();
            resultMap.put("All", idResultSet);
        } catch (SQLException e) {
            e.printStackTrace();

        }
        return resultMap;
    }

    public static Map<String, String> getStuIdFromFile() {
        Map<String, String> stuList = LoadData.getStuList();
        return stuList;
    }

    private static Map<String, ResultSet> getStuIdFromGrade() {
        System.out.println("开始根据学生年级进行兴趣分析。。。");
        String queryIdByGrade = "select stu_id from student where stu_id regexp ?";
        String[] labels = {"^131", "^141", "^151", "^161"};
        ResultSet resultSet = null;
        HashMap<String, ResultSet> resultMap = new HashMap<>();
        try {
            for (String label : labels) {
                PreparedStatement statement = conn.prepareStatement(queryIdByGrade);
                statement.setString(1, label);
                resultSet = statement.executeQuery();
                resultMap.put(label, resultSet);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return resultMap;
    }

    public static void main(String args[]) {

        System.out.println("测试............");
        String label;
        ResultSet idResultSet;
        String log1, log2;

        conn = getPool();

        final String LOG_DEPARTMENT_1 = "本次采集的学院是";
        final String LOG_DEPARTMENT_2 = "本批次学院";
        // Map<String, ResultSet> resultMap1 = getStuIdFromDepart();
        log1 = LOG_DEPARTMENT_1;
        log2 = LOG_DEPARTMENT_2;
        String updateSequc1 = "INSERT INTO test_frequency_department(type, name, department) VALUES(?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE frequency=frequency+1";
        // new Thread(new CalcFrequ().new CalcByDepartThread(log1, log2, updateSequc1, resultMap1)).start();

        final String LOG_GRADE_1 = "本次采集的年级是";
        final  String LOG_GRADE_2 = "本批次年级";
        // Map<String, ResultSet> resultMap2 = getStuIdFromGrade();
        log1 = LOG_GRADE_1;
        log2 = LOG_GRADE_2;
        String updateSequc2 = "INSERT INTO test_frequency_grade(type, name, grade) VALUES(?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE frequency=frequency+1";
        // new Thread(new CalcFrequ().new CalcByGradeThread(log1, log2, updateSequc2, resultMap2)).start();

        final  String LOG_ALL = "";
        Map<String, ResultSet> resultMap3 = getAllStuId();
        log2 = LOG_ALL;
        String updateSequc3 = "INSERT INTO frequency_all(stu_id, stu_name, sex, stu_type, stu_department, stu_major, label_type, label_value, book_id) VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?) " +
                "ON DUPLICATE KEY UPDATE frequency=frequency+1";
        new Thread(new CalcFrequ().new CalcAllThread(log2, updateSequc3, resultMap3)).start();

    }

    public  void calcFromFile(String fileName) {
        int countStudent = 0;
        String stuName, stuId, bookTitle, bookType, bookAuthor;
        Map<String, String> stuList = getStuIdFromFile();

        for (Map.Entry<String, String> entry : stuList.entrySet()) {
            // for (int i=0; i<1; i++){
            countStudent ++;
            int countBook = 0;
            stuName = entry.getKey();
            stuId = entry.getValue();
            String temp;
            if (stuId.length() != 8) {
                temp = stuName;
                stuName = stuId;
                stuId = temp;
            }

            System.out.println("获取第"+ countStudent + "个学生的兴趣标签...");
            // 从mysql获取数据
            String query = "SELECT book_title, book_type, book_author FROM books " +
                    "WHERE book_id IN (SELECT book_id FROM student_book WHERE stu_id = ?)";

            PreparedStatement preparedStmt = null;

            // stuId = "14101733";

            try {
                preparedStmt = conn.prepareStatement(query);
                preparedStmt.setString(1, stuId);
                ResultSet resultSet = preparedStmt.executeQuery();

                String updateSequc = "INSERT INTO test_frequency_study_good(type, name) VALUES(?, ?) " +
                        "ON DUPLICATE KEY UPDATE frequency=frequency+1";

                String[] bookTypes = null;
                while (resultSet.next()) {
                    countBook++;
                    System.out.println("获取第"+ countStudent + "个学生的第" + countBook + "本书兴趣标签...");
                    bookTitle = resultSet.getString("book_title");
                    preparedStmt = conn.prepareStatement(updateSequc);
                    preparedStmt.setString(1, TYPE_TITLE);
                    preparedStmt.setString(2, bookTitle);
                    preparedStmt.executeUpdate();

                    bookType = resultSet.getString("book_type");
                    bookTypes = bookType.split("-");
                    for (String type : bookTypes) {
                        type = type.trim();
                        preparedStmt = conn.prepareStatement(updateSequc);
                        preparedStmt.setString(1, TYPE_TYPE_DETAIL);
                        preparedStmt.setString(2, type);
                        preparedStmt.executeUpdate();
                    }

                    preparedStmt = conn.prepareStatement(updateSequc);
                    preparedStmt.setString(1, TYPE_TYPE);
                    preparedStmt.setString(2, bookType);
                    preparedStmt.executeUpdate();

                    bookAuthor = resultSet.getString("book_author");
                    preparedStmt = conn.prepareStatement(updateSequc);
                    preparedStmt.setString(1, TYPE_AUTHOR);
                    preparedStmt.setString(2, bookAuthor);
                    preparedStmt.executeUpdate();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void calcAllFromSql(String updateSequc, Map<String, ResultSet> resultMap, String log) {
        final int COUNT_TO_IGNORE=0;
        String bookTitle, bookTypeAll, bookAuthor, bookId;
        String label, stuId, stuName, department, major, stuType, sex;
        int countStudent = 0;
        ResultSet stuResultSet;
        conn = getDBConnection();
        for (Map.Entry<String, ResultSet> entry : resultMap.entrySet()) {
            label = entry.getKey();
            stuResultSet = entry.getValue();
            try {

                conn.setAutoCommit(false);
                Long beginTime = System.currentTimeMillis();
                PreparedStatement insertStmt = conn.prepareStatement(updateSequc);

                // 针对每一个学生进行分析
                while (stuResultSet.next()) {
                    stuId = stuResultSet.getString("stu_id");
                    stuName = stuResultSet.getString("name");
                    sex = stuResultSet.getString("sex");
                    stuType = stuResultSet.getString("type");
                    department = stuResultSet.getString("department");
                    major = stuResultSet.getString("major");

                    countStudent++;
                    if (countStudent < COUNT_TO_IGNORE) {
                        continue;
                    }
                    System.out.println(label + ": 获取第" + countStudent + "个学生" + stuId + "的兴趣标签...");
                    // 从mysql获取数据，获取每本书的标题，类型，作者进行分别统计
                    String query = "SELECT book_id, book_title, book_type, book_author FROM books " +
                            "WHERE book_id IN (SELECT book_id FROM student_book WHERE stu_id = ?)";
                    PreparedStatement preparedStmt = conn.prepareStatement(query);
                    preparedStmt.setString(1, stuId);
                    ResultSet resultSet = preparedStmt.executeQuery();

                    String[] bookTypeDetails = null;
                    int countBook = 0;

                    while (resultSet.next()) {
                        countBook++;
                        System.out.println(label + ":获取第" + countStudent + "个学生的第" + countBook + "本书兴趣标签...");
                        bookId = resultSet.getString("book_id");

                        bookTitle = resultSet.getString("book_title");
                        insertStmt.setString(1, stuId);
                        insertStmt.setString(2, stuName);
                        insertStmt.setString(3, sex);
                        insertStmt.setString(4, stuType);
                        insertStmt.setString(5, department);
                        insertStmt.setString(6, major);
                        insertStmt.setString(7, TYPE_TITLE);
                        insertStmt.setString(8, bookTitle);
                        insertStmt.setString(9, bookId);

                        insertStmt.addBatch();

                        bookTypeAll = resultSet.getString("book_type");
                        bookTypeDetails = bookTypeAll.split("-");
                        for (String bookTypeDetail : bookTypeDetails) {
                            bookTypeDetail = bookTypeDetail.trim();
                            insertStmt.setString(1, stuId);
                            insertStmt.setString(2, stuName);
                            insertStmt.setString(3, sex);
                            insertStmt.setString(4, stuType);
                            insertStmt.setString(5, department);
                            insertStmt.setString(6, major);
                            insertStmt.setString(7, TYPE_TYPE_DETAIL);
                            insertStmt.setString(8, bookTypeDetail);
                            insertStmt.setString(9, bookId);
                            insertStmt.addBatch();
                        }


                        insertStmt.setString(1, stuId);
                        insertStmt.setString(2, stuName);
                        insertStmt.setString(3, sex);
                        insertStmt.setString(4, stuType);
                        insertStmt.setString(5, department);
                        insertStmt.setString(6, major);
                        insertStmt.setString(7, TYPE_TYPE);
                        insertStmt.setString(8, bookTypeAll);
                        insertStmt.setString(9, bookId);
                        insertStmt.addBatch();

                        bookAuthor = resultSet.getString("book_author");
                        insertStmt.setString(1, stuId);
                        insertStmt.setString(2, stuName);
                        insertStmt.setString(3, sex);
                        insertStmt.setString(4, stuType);
                        insertStmt.setString(5, department);
                        insertStmt.setString(6, major);
                        insertStmt.setString(7, TYPE_AUTHOR);
                        insertStmt.setString(8, bookAuthor);
                        insertStmt.setString(9, bookId);
                        insertStmt.addBatch();
                    }
                    if (countStudent%500 == 0) {
                        System.out.println("启动一个新进程...");
                        new Thread(new CalcFrequ().new CommitRunnable(insertStmt, label, countStudent, log, conn)).start();
                        insertStmt = conn.prepareStatement(updateSequc);
                    }
                }
                System.out.println("启动一个新进程...");
                new Thread(new CalcFrequ().new CommitRunnable(insertStmt, label, countStudent, log, conn)).start();
                insertStmt = conn.prepareStatement(updateSequc);
                Long endTime = System.currentTimeMillis();
                System.out.println("总共pst+batch用时：" + (endTime - beginTime) + "毫秒");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    public  void calcByLabelFromSql(String log1, String updateSequc, Map<String, ResultSet> resultMap, String log2){

        final int COUNT_TO_IGNORE=0;
        String label, stuId, bookTitle, bookType, bookAuthor;
        int countStudent = 0;
        ResultSet idResultSet;
        conn = getDBConnection();
        for (Map.Entry<String, ResultSet> entry : resultMap.entrySet()) {
            label = entry.getKey();
            idResultSet = entry.getValue();

            System.out.println(log1 + label);
            try {

                conn.setAutoCommit(false);
                Long beginTime = System.currentTimeMillis();

                PreparedStatement insertStmt = conn.prepareStatement(updateSequc);

                // 针对每一个学生进行分析
                while (idResultSet.next()) {
                    stuId = idResultSet.getString("stu_id");
                    countStudent++;
                    if (countStudent < COUNT_TO_IGNORE) {
                        continue;
                    }
                    System.out.println(label + ": 获取第" + countStudent + "个学生" + stuId + "的兴趣标签...");
                    // 从mysql获取数据，获取每本书的标题，类型，作者进行分别统计
                    String query = "SELECT book_title, book_type, book_author FROM books " +
                            "WHERE book_id IN (SELECT book_id FROM student_book WHERE stu_id = ?)";
                    PreparedStatement preparedStmt = null;
                    preparedStmt = conn.prepareStatement(query);
                    preparedStmt.setString(1, stuId);
                    ResultSet resultSet = preparedStmt.executeQuery();

                    String[] bookTypes = null;
                    int countBook = 0;

                    while (resultSet.next()) {
                        countBook++;
                        System.out.println(label + ":获取第" + countStudent + "个学生的第" + countBook + "本书兴趣标签...");
                        bookTitle = resultSet.getString("book_title");

                        insertStmt.setString(1, TYPE_TITLE);
                        insertStmt.setString(2, bookTitle);
                        insertStmt.setString(3, label);
                        insertStmt.addBatch();

                        bookType = resultSet.getString("book_type");
                        bookTypes = bookType.split("-");
                        for (String type : bookTypes) {
                            type = type.trim();
                            insertStmt.setString(1, TYPE_TYPE_DETAIL);
                            insertStmt.setString(2, type);
                            insertStmt.setString(3, label);
                            insertStmt.addBatch();
                        }


                        insertStmt.setString(1, TYPE_TYPE);
                        insertStmt.setString(2, bookType);
                        insertStmt.setString(3, label);
                        insertStmt.addBatch();

                        bookAuthor = resultSet.getString("book_author");
                        insertStmt.setString(1, TYPE_AUTHOR);
                        insertStmt.setString(2, bookAuthor);
                        insertStmt.setString(3, label);
                        insertStmt.addBatch();
                    }
                    if (countStudent%500 == 0) {
                        System.out.println("启动一个新进程...");
                        new Thread(new CalcFrequ().new CommitRunnable(insertStmt, label, countStudent, log2, conn)).start();
                        insertStmt = conn.prepareStatement(updateSequc);
                        }
                }
                System.out.println("启动一个新进程...");
                new Thread(new CalcFrequ().new CommitRunnable(insertStmt, label, countStudent, log2, conn)).start();
                insertStmt = conn.prepareStatement(updateSequc);
                Long endTime = System.currentTimeMillis();
                System.out.println("总共pst+batch用时：" + (endTime - beginTime) + "毫秒");
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    class CommitRunnable implements Runnable{

        PreparedStatement insertStmt;
        String label;
        int countStudent;
        String log2;
        Connection conn;

        public CommitRunnable(PreparedStatement insertStmt, String label, int countStudent, String log2, Connection conn) {
            this.insertStmt = insertStmt;
            this.label = label;
            this.countStudent = countStudent;
            this.log2 = log2;
            this.conn = conn;
        }

        @Override
        public void run() {
            try {
                Long beginTime = System.currentTimeMillis();
                int[] result = insertStmt.executeBatch();
                System.out.println("进程里面的insertStmt执行了: " + result.length + "条Sql语句......");
                conn.commit();
                insertStmt.clearBatch();
                Long endTime = System.currentTimeMillis();
                // System.out.println("本批次年级" + label + "第" + countBook + "pst+bat用时："+(endTime-beginTime)+"毫秒");
                System.out.println(log2 + label + "  第" + countStudent + "  pst+bat用时："+(endTime-beginTime)+"毫秒");
            } catch (SQLException e) {
                e.printStackTrace();
            } finally {
                try {
                    // if (connCount > 2) {
                        insertStmt.close();
                        // conn.close();
                    // }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    class CalcByDepartThread implements Runnable{
        String log1, log2, updateSequc;
        Map<String, ResultSet> resultMap;

        CalcByDepartThread(String log1, String log2, String updateSequc, Map<String, ResultSet> resultMap){
            this.log1 = log1;
            this.log2 = log2;
            this.updateSequc = updateSequc;
            this.resultMap = resultMap;
        }

        @Override
        public void run() {
            new CalcFrequ().calcByLabelFromSql(log1, updateSequc, resultMap, log2);
        }
    }

    class CalcByGradeThread implements Runnable{
        String log1, log2, updateSequc;
        Map<String, ResultSet> resultMap;

        CalcByGradeThread(String log1, String log2, String updateSequc, Map<String, ResultSet> resultMap){
            this.log1 = log1;
            this.log2 = log2;
            this.updateSequc = updateSequc;
            this.resultMap = resultMap;
        }

        @Override
        public void run() {
            new CalcFrequ().calcByLabelFromSql(log1, updateSequc, resultMap, log2);
        }
    }

    class CalcAllThread implements Runnable{
        String log, updateSequc;
        Map<String, ResultSet> resultMap;

        CalcAllThread(String log, String updateSequc, Map<String, ResultSet> resultMap){
            this.log = log;
            this.updateSequc = updateSequc;
            this.resultMap = resultMap;
        }

        @Override
        public void run() {
            new CalcFrequ().calcAllFromSql(updateSequc, resultMap, log);
        }
    }
}