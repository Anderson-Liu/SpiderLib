package zwgk;

import com.hankcs.hanlp.HanLP;
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
public class TestNlpPlaceRec {
    public static void main(String args[]){
        HanLP.Config.enableDebug(true);
        // 地名识别
        String [] testCase = new String[]{
                "市总工会对《宣城市人民政府关于 促进慈善事业健康发展的实施意见》 （征求意见稿）的反馈意见 ",
                "市残联对《宣城市人民政府关于 促进慈善事业健康发展的实施意见》 （征求意见稿）的反馈意见 ",
                "市发展改革委对《宣城市人民政府关于 促进慈善事业健康发展的实施意见》 （征求意见稿）的反馈意见 ",
                "《宣城市临时救助实施办法》意见征集情况 ",
                "宣城市民政局就《宣城市临时救助实施办法》对法制办审查意见的回复 ",
                "旌德县民政局关于征求《宣城市人民政府关于印发的通知(征求意见稿)》意见的函 ",
                "泾县民政局关于对《宣城市人民政府关于印发宣城市临时救助实施办法的通知》（征求意见稿）的反馈建议 ",
                "市财政局关于对《宣城市人民政府办公室关于印发＜宣城市临时救助实施办法＞的通知》的修改意见 ",
                "宣城市社会救助家庭经济状况核对办法（试行）征求意见汇总 ",
                "市工商质监局关于征求《宣城市人民政府办公室关于印发 〈宣城市申请救助家庭经济状况核对办法〉的通知（征求意见稿）》意见的复函 ",
                "市国税局关于《宣城市申请救助家庭经济 状况核对办法》（征求意见稿） 的修改建议 "
        };
        Segment segment = HanLP.newSegment().enablePlaceRecognize(true);
        for (String sentence : testCase)
        {
            List<Term> termList = segment.seg(sentence);
            System.out.println(termList);
        }

        segment = HanLP.newSegment().enableOrganizationRecognize(true);
        for (String sentence : testCase)
        {
            List<Term> termList = segment.seg(sentence);
            System.out.println(termList);
        }
    }
}
