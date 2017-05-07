import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by anderson on 2016/12/16.
 * 通过用户id获取其他信息
 */
public class SearchUserById {
    static DefaultHttpClient httpClient = new DefaultHttpClient();
    static final int TIMEOUT_MILLIS = 80000;
    static RequestConfig requestConfig = RequestConfig.custom()
            .setSocketTimeout(TIMEOUT_MILLIS)
            .setConnectTimeout(TIMEOUT_MILLIS)
            .setConnectionRequestTimeout(TIMEOUT_MILLIS)
            .build();
    public static void main(String args[]) throws IOException {
        String userId = "13101200";

        CookieStore cookieStore = new BasicCookieStore();
//         BasicClientCookie cookie = new BasicClientCookie("JSESSIONID", "C01028454DD8A6CB817DC8A539028940");
        BasicClientCookie cookie = new BasicClientCookie("iPlanetDirectoryPro", "AQIC5wM2LY4SfcwPAjfNtODz1u5ho1V0nW6UJG%2FvCBSYtx0%3D%40AAJTSQACMDI%3D%23");
//        cookie.setPath("/");
//        cookie.setDomain(".ahau.edu.cn");
        cookie.setVersion(1);
//        cookie.setSecure(false);
//        cookie.setComment("");
        // cookie.setAttribute("iPlanetDirectoryPro", "AQIC5wM2LY4SfczPP391VJTE2m4x9Z%2BSGjM0QUMHw6EQfWY%3D%40AAJTSQACMDI%3D%23");
//        cookie.setAttribute("JSESSIONID", "C01028454DD8A6CB817DC8A539028940");
        cookieStore.addCookie(cookie);
        httpClient.setCookieStore(cookieStore);

        final String URL_IDS_LOGIN = "https://ids1.ahau.edu.cn:82/amserver/UI/Login";
        HttpPost loginPost = new HttpPost(URL_IDS_LOGIN);

        String MIDS_LOGIN = "https://mids.ahau.edu.cn/_ids_mobile/login18_9";




        String queryBaseUrl = "http://mab.ahau.edu.cn/_ids_mobile/searchUser18";
        HttpPost queryPost = new HttpPost(queryBaseUrl);
        queryPost.setConfig(requestConfig);
        // queryPost.addHeader("User-Agent", "Mozilla/5.0 (Linux; U; Android 4.0.2; en-us; Galaxy Nexus Build/ICL53F) AppleWebKit/534.30 (KHTML, like Gecko) Version/4.0 Mobile Safari/534.30");
        // queryPost.addHeader("Host", "mab.ahau.edu.cn");
        try {
            URI uri = new URIBuilder(queryPost.getURI())
                    .addParameter("keyword", userId)
                    .build();
            queryPost.setURI(uri);
            HttpResponse queryResponse = httpClient.execute(queryPost);
            HttpEntity queryEntity = queryResponse.getEntity();
            String queryContent = EntityUtils.toString(queryEntity, "UTF-8");
            Document queryDocument = Jsoup.parse(queryContent);
            System.out.println("Debug");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }
}
