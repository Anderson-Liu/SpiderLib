package zwgk;

import com.hankcs.hanlp.HanLP;
import com.hankcs.hanlp.seg.CRF.CRFSegment;
import com.hankcs.hanlp.seg.Dijkstra.DijkstraSegment;
import com.hankcs.hanlp.seg.NShort.NShortSegment;
import com.hankcs.hanlp.seg.Segment;
import com.hankcs.hanlp.seg.common.Term;
import com.hankcs.hanlp.tokenizer.IndexTokenizer;
import com.hankcs.hanlp.tokenizer.NLPTokenizer;
import com.hankcs.hanlp.tokenizer.SpeedTokenizer;
import com.hankcs.hanlp.tokenizer.StandardTokenizer;

import java.util.List;

/**
 * Created by anderson on 2016/12/26.
 */
public class TestNlp {
    public static void main(String args[]){
        HanLP.Config.enableDebug(true);
        String[] testCase = new String[]{
                "签约仪式前，秦光荣、李纪恒、仇和等一同会见了参加签约的企业家。",
                "区长庄木弟新年致辞",
                "朱立伦：两岸都希望共创双赢 习朱历史会晤在即",
                "陕西首富吴一坚被带走 与令计划妻子有交集",
                "据美国之音电台网站4月28日报道，8岁的凯瑟琳·克罗尔（凤甫娟）和很多华裔美国小朋友一样，小小年纪就开始学小提琴了。她的妈妈是位虎妈么？",
                "凯瑟琳和露西（庐瑞媛），跟她们的哥哥们有一些不同。",
                "王国强、高峰、汪洋、张朝阳光着头、韩寒、小四",
                "张浩和胡健康复员回家了",
                "王总和小丽结婚了",
                "编剧邵钧林和稽道青说",
                "这里有关天培的有关事迹",
                "龚学平等领导说,邓颖超生前杜绝超生",
        };
        Segment segment = HanLP.newSegment().enableNameRecognize(true);
        for (String sentence : testCase)
        {
            List<Term> termList = segment.seg(sentence);
            System.out.println(termList);
        }

        // 地名识别
        testCase = new String[]{
                "武胜县新学乡政府大楼门前锣鼓喧天",
                "蓝翔给宁夏固原市彭阳县红河镇黑牛沟村捐赠了挖掘机",
        };
        segment = HanLP.newSegment().enablePlaceRecognize(true);
        for (String sentence : testCase)
        {
            List<Term> termList = segment.seg(sentence);
            System.out.println(termList);
        }

        testCase = new String[]{
                "我在上海林原科技有限公司兼职工作，",
                "我经常在台川喜宴餐厅吃饭，",
                "偶尔去地中海影城看电影。",
        };
        segment = HanLP.newSegment().enableOrganizationRecognize(true);
        for (String sentence : testCase)
        {
            List<Term> termList = segment.seg(sentence);
            System.out.println(termList);
        }
//        String text = "经向市卫计委、市教体局、市公安局、市住建委、市人社局、市财政局、市政府法制办、各县（市、区）民政局征求意见，除法制办外，共收到11条反馈意见，完全采纳4条，部分采纳2条，其余5条没有采纳。部分采纳及没有采纳具体如下：\n" +
//                "1、郎溪县民政局提出的第一点建议：没有实质意义，因原规定并没有要求实施全额救助。\n" +
//                "2、郎溪县民政局提出的第二点建议：在《社会救助暂行办法》中已有规定，且当时征求过县级人民政府及县级民政部门研究，当时表示无异议。\n" +
//                "3、宁国市民政局提出的第一点建议：2个工作日是省政府文件规定的，公示期建议予以采纳。\n" +
//                "4、宁国市民政局提出的第二点建议：“及时”概念抽象。\n" +
//                "5、泾县民政局提出的第一点建议：已有小额救助的规定。\n" +
//                "6、泾县民政局提出的第二点建议：没有依据，不符合临时救助政策。\n" +
//                "7、宣州区民政局提出的第二点建议：没有依据。";
//        // 短语提取
//        List<String> phraseList = HanLP.extractPhrase(text, 10);
//        System.out.println(phraseList);
//        // 自动摘要
//        List<String> sentenceList = HanLP.extractSummary(text, 3);
//        System.out.println(sentenceList);
//        // 关键词提取
//        List<String> keywordList = HanLP.extractKeyword(text, 5);
//        System.out.println(keywordList);
//        // 测试分词
//        System.out.println("测试分词");
//        System.out.println(HanLP.segment(text));
//        // 标准分词
//        System.out.println("标准分词");
//        List<Term> termList = StandardTokenizer.segment(text);
//        System.out.println(termList);
//        // NLP分词
//        System.out.println("NLP分词");
//        termList = NLPTokenizer.segment(text);
//        System.out.println(termList);
//        // 索引分词
//        System.out.println("索引分词");
//        termList = IndexTokenizer.segment(text);
//        for (Term term : termList)
//        {
//            System.out.println(term + " [" + term.offset + ":" + (term.offset + term.word.length()) + "]");
//        }
//        // N-最短路径分词
//        System.out.println("N-最短路径分词");
//        Segment nShortSegment = new NShortSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
//        Segment shortestSegment = new DijkstraSegment().enableCustomDictionary(false).enablePlaceRecognize(true).enableOrganizationRecognize(true);
//        String[] testCase = new String[]{
//                text
//        };
//        for (String sentence : testCase)
//        {
//            System.out.println("N-最短分词：" + nShortSegment.seg(sentence) + "\n最短路分词：" + shortestSegment.seg(sentence));
//        }
//        // CRF分词
//        System.out.println("CRF分词");
//        Segment segment = new CRFSegment();
//        segment.enablePartOfSpeechTagging(true);
//        termList = segment.seg(text);
//        System.out.println(termList);
//        for (Term term : termList)
//        {
//            if (term.nature == null)
//            {
//                System.out.println("识别到新词：" + term.word);
//            }
//        }

//        // 极速词典分词
//        System.out.println("极速词典分词");
//        System.out.println(SpeedTokenizer.segment(text));
//        long start = System.currentTimeMillis();
//        int pressure = 1000000;
//        for (int i = 0; i < pressure; ++i)
//        {
//            SpeedTokenizer.segment(text);
//        }
//        double costTime = (System.currentTimeMillis() - start) / (double)1000;
//        System.out.printf("分词速度：%.2f字每秒", text.length() * pressure / costTime);
    }
}
