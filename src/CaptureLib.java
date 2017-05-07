import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.NoHttpResponseException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.ConnectionPoolTimeoutException;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Enn on 2016/3/19.
 * 自动识别验证码，抓取图书馆信息
 */
public class CaptureLib {

    // private static CloseableHttpClient httpClient = HttpClients.createDefault();
    static int actiCount = 0;
    static int totalCount = 0;
    static int countToIgnor = 3950;
    // static int countToIgnor = 0;
    static int passwdChange = 0;
    static final int TIMEOUT_MILLIS = 8000;
    static RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(TIMEOUT_MILLIS)
            .setConnectTimeout(TIMEOUT_MILLIS)
            .setConnectionRequestTimeout(TIMEOUT_MILLIS)
            .build();

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


    public static void loginLib(String password) {

        System.out.println("--------Get Cookie for Login---------");

        int htmlCode = 0;
        String loginUrl = "http://opac.ahau.edu.cn/reader/redr_verify.php";
        HttpPost loginPost = new HttpPost(loginUrl);

        loginPost.setConfig(requestConfig);
        loginPost.addHeader("User-Agent", "Mozilla/5.0 (X11; Linux x86_64; rv:44.0) Gecko/20100101 Firefox/44.0");
        loginPost.addHeader("Referer", "http://lib.ahau.edu.cn/");

        List<NameValuePair> nvpsLogin = new ArrayList<>();
        nvpsLogin.add(new BasicNameValuePair("select", "cert_no"));
        nvpsLogin.add(new BasicNameValuePair("passwd", password));
        Map<String, String> stuList = LoadData.getStuList();
        String stuName = "";
        String stuId = "";
        for (Map.Entry<String, String> entry : stuList.entrySet()) {
        // for (int i=0; i<1; i++){
            totalCount++;
            // 跳过一定数目的数据
            if (totalCount < countToIgnor) {
                System.out.println("跳过第" + totalCount + "个");
                continue;
            }

            System.out.println("这是第" + totalCount + "个");

             // stuId = "13100501";
             // stuName = "刘宏达";

            stuName = entry.getKey();
            stuId = entry.getValue();

            String temp;
            if (stuId.length() != 8) {
                temp = stuName;
                stuName = stuId;
                stuId = temp;
            }

            System.out.println("开始获取名字为" + stuName + "，学号为" + stuId + "的用户的数据...");
            nvpsLogin.add(new BasicNameValuePair("number", stuId));
            // 每次都使用一个新的httpClient示例发送请求。
            CloseableHttpClient httpClient = HttpClients.createDefault();

            try {
                // 自动获取验证码
                System.out.println("开始自动识别验证码....");
                String imgCode = ImagePreProcess.getCode(httpClient);
                nvpsLogin.add(new BasicNameValuePair("captcha", imgCode));
            } catch (ConnectionPoolTimeoutException e) {
                System.out.println("获取用户" + stuName + "(学号" + stuId + ")的验证码超时，获取下一个用户的数据...");
                continue;
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("识别验证码出现异常，跳过" + stuId);
                httpClient = HttpClients.createDefault();
                continue;
            }

            try {
                loginPost.setEntity(new UrlEncodedFormEntity(nvpsLogin));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            // 执行Post请求登录
            CloseableHttpResponse loginPostResponse = null;
            try {
                loginPostResponse = httpClient.execute(loginPost);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // 获取登录返回数据
            if (loginPostResponse.getFirstHeader("Location") == null) {
                System.out.println("名字为" + stuName  + "，学号为" + stuId + "的学生获取不到Location, 密码错误，跳过");
                passwdChange++;
                System.out.println("这是第 " + passwdChange +  " 个更改了密码的，共" + totalCount + "个!");
                continue;
            }
            String location = loginPostResponse.getFirstHeader("Location").getValue();
            htmlCode = loginPostResponse.getStatusLine().getStatusCode();
            if (htmlCode == HttpStatus.SC_MOVED_TEMPORARILY && location.equals("redr_con.php")) {
                actiCount++;
                System.out.println("名字为" + stuName + ", 学号为" + stuId + "的学生未激活，将进行自动激活....");
                System.out.println("这是第" + actiCount + "个未激活的学生");
                HashMap<String, String> activeResultMap = activeCount(httpClient, stuName);
                if (activeResultMap.get("htmlCode") == null) {
                    System.out.println("无法获得htmlCode");
                    continue;
                }
                int getCode = Integer.parseInt(activeResultMap.get("htmlCode"));
                String activeContent = activeResultMap.get("content");
                Document content = Jsoup.parse(activeContent);
                // 解析是否含有“新密码”，有则说明是激活成功
                Elements chPasswd = content.select("form:contains(新密码)");
                boolean isOk = chPasswd.toString().contains("新密码");

                if (200 != getCode || !isOk) {
                    System.out.println(stuId + "激活失败, 跳过");
                    continue;
                }
                System.out.println("激活成功，开始获取用户信息");
                saveData(httpClient, stuId, stuName);

            } else if (htmlCode == HttpStatus.SC_MOVED_TEMPORARILY && location.equals("redr_info.php")) {
                System.out.println(stuId + "已激活...");
                System.out.println("将进行数据获取！");
                saveData(httpClient, stuId, stuName);
            } else {
                System.out.println("响应数据异常；进行下一个！");
                continue;
            }
        }
    }

    private static void saveData(CloseableHttpClient httpClient, String stuId, String stuName) {
        try {
            HashMap<String, String>  result = getInfo(httpClient, stuId);
            saveAsFile(stuId, result, actiCount);
            System.out.println("获取信息成功，已存到数据库和文件中。");
            HttpGet logout = new HttpGet("http://opac.ahau.edu.cn/reader/logout.php");
            logout.setHeader("Rerferer", "http://opac.ahau.edu.cn/reader/redr_info.php");
            CloseableHttpResponse response  = httpClient.execute(logout);

        } catch (ConnectionPoolTimeoutException e) {
            System.out.println("获取用户" + stuName + "(学号" + stuId + ")数据超时，获取下一个用户的数据...");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 获取详细信息，包括读者信息，当前借阅，历史借阅等等。
    public static HashMap<String, String> getInfo(CloseableHttpClient httpClient, String stuId) throws IOException {
        int htmlCode;
        String inforUrl = "http://opac.ahau.edu.cn/reader/redr_info_rule.php";
        HttpGet infoGet = new HttpGet(inforUrl);
        infoGet.setConfig(requestConfig);
        // 执行Get请求获取详细个人信息
        CloseableHttpResponse infoGetResponse = httpClient.execute(infoGet);
        // 获取得到的个人详细信息
        HttpEntity infoGetEntity = infoGetResponse.getEntity();
        htmlCode = infoGetResponse.getStatusLine().getStatusCode();
        String infoContent = EntityUtils.toString(infoGetEntity, "UTF-8");

        // 解析个人信息
        TestParse.parsPersonInfo(conn, infoContent);
        // 解析最近借阅信息
        String presentContent = getPresentBook(httpClient, stuId);
        // 解析历史借阅信息
        String histContent = getHistoryBook(httpClient, stuId);

        // String recommContent = getRecommendBook();
        HashMap<String, String> result = new HashMap<>();
        result.put("UserInfo", infoContent);
        result.put("PresentContent", presentContent);
        result.put("HistContent", histContent);
        // result.put("RecommContent", recommContent);
        return result;
    }

    public static HashMap<String, String> activeCount(CloseableHttpClient httpClient, String stuName) {
        String activeUrl = "http://opac.ahau.edu.cn/reader/redr_con_result.php";
        HttpPost activePost = new HttpPost(activeUrl);
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("name", stuName));
        int htmlCode = 0;
        HashMap<String, String> map = new HashMap<>();
        try {
            // 加上utf-8对于添加中文参数非常重要
            activePost.setEntity(new UrlEncodedFormEntity(pairs, "utf-8"));
            activePost.setHeader("Referer", "http://opac.ahau.edu.cn/reader/redr_con.php");
            CloseableHttpResponse activeResposne = httpClient.execute(activePost);
            String activeContent = EntityUtils.toString(activeResposne.getEntity(), "utf-8");
            htmlCode = activeResposne.getStatusLine().getStatusCode();
            // location = activeResposne.getFirstHeader("location").getValue();
            System.out.println(activeResposne.getStatusLine().getStatusCode());
            map.put("htmlCode", Integer.toString(htmlCode));
            map.put("content", activeContent);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (NoHttpResponseException e) {
            e.printStackTrace();
            System.out.println("网络无响应");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return map;
    }

    public static void saveAsFile(String userCode, HashMap<String, String> map) throws IOException {
        File writeTarget = new File("ResultData//" + "进程1" + "+" +  totalCount + "+" + userCode + ".txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(writeTarget));
        HashMap<String, String> result = map;
        String contentType = null;
        String resultContent = null;
        String pre = null;
        for (Map.Entry<String, String> infoEntry : result.entrySet()) {
            contentType = infoEntry.getKey();
            resultContent = infoEntry.getValue();
            pre = "\n\n#######################    " + contentType + "    #####################\n\n";
            out.append(pre);
            out.append(resultContent);
            out.flush();
        }
        out.flush();
        out.close();
    }

    public static void saveAsFile(String userCode, HashMap<String, String> map, int actiCount) throws IOException {
        File writeTarget = new File("ResultData//" + "进程1" + "+" +  totalCount + "+" + userCode + ".txt");
        BufferedWriter out = new BufferedWriter(new FileWriter(writeTarget));
        String pre = "\n\n##########    At the time of  " + actiCount + " that being active   ################\n\n";
        out.append(pre);
        out.flush();
        saveAsFile(userCode, map);
    }


    public static String getPresentBook(CloseableHttpClient httpClient, String stuId) {
        String presentUrl = "http://opac.ahau.edu.cn/reader/book_lst.php";
        HttpGet getPresent = new HttpGet(presentUrl);
        getPresent.setConfig(requestConfig);
        CloseableHttpResponse response = null;
        String content = null;
        try {
            response = httpClient.execute(getPresent);
            content = EntityUtils.toString(response.getEntity(), "utf-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        TestParse.parsRecent(stuId, conn, content);
        return content;
    }

    public static String getHistoryBook(CloseableHttpClient httpClient, String stuId) {
        String hisUrl = "http://opac.ahau.edu.cn/reader/book_hist.php";
        HttpPost hisBookPost = new HttpPost(hisUrl);
        hisBookPost.setConfig(requestConfig);
        List<NameValuePair> pairs = new ArrayList<>();
        pairs.add(new BasicNameValuePair("para_string", "all"));
        CloseableHttpResponse hisResponse = null;
        String content = null;
        try {
            hisBookPost.setEntity(new UrlEncodedFormEntity(pairs, "utf-8"));
            hisResponse = httpClient.execute(hisBookPost);
            content = EntityUtils.toString(hisResponse.getEntity(), "utf-8");
            TestParse.parseHist(stuId, conn, content);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return content;
    }

//    // 读者荐购
//    public static String getRecommendBook(){
//        String recommUrl = "http://opac.ahau.edu.cn/reader/asord_lst.php";
//        HttpGet getRecomm = new HttpGet(recommUrl);
//        getRecomm.setConfig(requestConfig);
//        String content = null;
//        try {
//            CloseableHttpResponse recommRespon = httpClient.execute(getRecomm);
//            content = EntityUtils.toString(recommRespon.getEntity(), "utf-8");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return content;
//    }

    public static void main (String args[]) {
        String passwd = "0000";
        CaptureLib.loginLib(passwd);
    }


}