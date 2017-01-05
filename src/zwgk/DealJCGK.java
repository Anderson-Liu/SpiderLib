package zwgk;

import com.hankcs.hanlp.HanLP;
import org.apache.http.HttpEntity;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.io.File;
import java.io.IOException;

/**
 * Created by anderson on 2016/12/18.
 */
public class DealJCGK {

    public static void main(String args[]){
        System.setProperty(
                "webdriver.chrome.driver",
                "C:\\Users\\anderson\\AppData\\Local\\Google\\Chrome\\Application\\chrome.exe");

        // 创建一个 ChromeDriver 的接口，用于连接 Chrome
        @SuppressWarnings("deprecation")
        ChromeDriverService service = new ChromeDriverService.Builder()
                .usingDriverExecutable(
                        new File(
                                "C:\\Users\\anderson\\driver\\chromedriver.exe"))
                .usingAnyFreePort().build();
        try {
            service.start();
        } catch (IOException e) {
            e.printStackTrace();
        }

        String bengBu_JCGK_JCGKZD = "http://zwgk.bengbu.gov.cn/com_list.jsp?serialnum=CA&LmId=614673301&DwId=86966862&OfNature=100000";



        // 创建一个 Chrome 的浏览器实例
        WebDriver driver = new RemoteWebDriver(service.getUrl(),
                DesiredCapabilities.chrome());

        driver.get("baidu.com");
        // 获取 网页的 title
        System.out.println("1 Page title is: " + driver.getTitle());
        // 通过 id 找到 input 的 DOM
        WebElement element = driver.findElement(By.id("kw"));

        // 输入关键字
        element.sendKeys("zTree");

        // 提交 input 所在的  form
        element.submit();

        // 通过判断 title 内容等待搜索页面加载完毕，Timeout 设置10秒
        (new WebDriverWait(driver, 10)).until(new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver d) {
                return d.getTitle().toLowerCase().endsWith("ztree");
            }
        });

        // 显示搜索结果页面的 title
        System.out.println("2 Page title is: " + driver.getTitle());

        //关闭浏览器
        driver.quit();

        HanLP.newSegment().enableNameRecognize( true);
    }
}
