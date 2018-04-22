import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring/application.xml"})
public class TestRedis {

    @Autowired
    RedisClientAdapter redisAdapter;
	
    @Test
    public void test1() {
    	List<LiveVideoInfo> videos = new ArrayList<LiveVideoInfo>();
    	for (long i = 0; i < 10; i++) {
        	LiveVideoInfo video = new LiveVideoInfo();
        	video.setVideoId(i);
        	video.setChatroomId("ChatroomId" + i);
        	video.setAnchorId(i + 5);
        	video.setFormatType((int)(i % 2));
        	video.setCoverImg("http://imgstest.ikanshu.cn/images-wwlive/video/1703/c342179c26214522b020f53766e5f9c0_400.jpg");
        	videos.add(video);
    	}
    	
    	System.out.println("---------------------------------------------------------------------");
    }
	
}
