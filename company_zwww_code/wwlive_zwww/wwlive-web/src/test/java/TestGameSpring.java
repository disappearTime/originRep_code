import com.chineseall.iwanvi.wwlive.common.constants.Constants;
import com.chineseall.iwanvi.wwlive.common.external.rongcloud.domain.SdkHttpResult;
import com.chineseall.iwanvi.wwlive.common.tools.HttpUtils;
import com.chineseall.iwanvi.wwlive.dao.wwlive.UserInfoMapper;
import com.chineseall.iwanvi.wwlive.web.game.service.CardGameService;
import com.zw.zcf.util.HttpClientUtils;
import org.junit.*;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.net.HttpURLConnection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Created by 云瑞 on 2017/11/1.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestGameSpring {
    @Autowired
    private UserInfoMapper userInfoMapper;

    @Autowired
    private CardGameService cardGameService;

    @Test
    public void test1 () {
        int max = 8;
        int min = 1;
        Random random = new Random();
        for (int i = 0;i < 30; i++) {
            int s = random.nextInt(max)%(max-min+1) + min;
            System.out.println(s);
        }
    }

    @Test
    public void test12 () {
        try {
            List<String> userList = userInfoMapper.findUserVideoCoinUserId();
            //1 金，2 银，3 铜
            int type = 3;
            //用户ID
            long userId = 123L;
            //用户名称
            String userName = "M10091";
            //主播名称
            long anchorId = 1633526L;
            //抽奖次数
            int count = 1;
            //视频ID
            long videoId = 10113;
            //房间号
            String roomNum = "17000137";
            //聊天室ID
            String chatroomId = "LIVE0010284";
            Date st = new Date();
            for (int i = 0; i < 5000; i++) {
                userId = Long.parseLong(userList.get(i%100));
                HttpURLConnection conn = HttpUtils.createPostHttpConnection("http://192.168.1.242:8082/app/game/card/pick.json");
                SdkHttpResult result = HttpUtils.returnResult(conn, "cnid=1062&IMEI=862630032615503&version=3.1.0&platform=android" +
                                "&model=MI_5&requestId=69372fa6ce854fb7af1485ef3580e269" +
                                "&coverKey=24e62a37bdfc945dfcb44f9c1d21049f&nonce=GD7QDXZZ&videoId=10113" +
                                "&chatroomId=LIVE0010284&cardType=" + type + "&pickTimes="+ count +"&roomNum=17000137" +
                                "&anchorId=1633526&userName=M10091&userId=" + userId);
                System.out.println(">>>>>>>>" + userId + "第"+(i+1)+"次抽奖结果信息" + result.toString());
            }
            Date et = new Date();
            long millis = et.getTime() - st.getTime();
            String val = millis > 1000 ? (millis / 1000) + "秒！" : (long) millis + "毫秒";
            System.out.println("总方法用时--->>>"+val);
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
