package test;

import java.util.Date;
import java.util.Set;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import redis.clients.jedis.Tuple;

import com.chineseall.iwanvi.wwlive.common.cache.RedisClientAdapter;
import com.chineseall.iwanvi.wwlive.common.cache.RedisKey;
import com.chineseall.iwanvi.wwlive.dao.wwlive.BlackListMapper;
import com.chineseall.iwanvi.wwlive.dao.wwlive.LiveVideoInfoMapper;
import com.chineseall.iwanvi.wwlive.domain.wwlive.BlackList;
import com.chineseall.iwanvi.wwlive.domain.wwlive.LiveVideoInfo;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestSpring220 {

	@Autowired
	BlackListMapper blackListMapper;

    @Autowired
    private LiveVideoInfoMapper liveVideoMapper;
    
	@Autowired
	RedisClientAdapter redisAdapter;
	
	public void test1() {
		BlackList black = new BlackList();
		black.setSuperAdminId(1L);
		black.setUserId(3L);
		black.setCreateTime(new Date());
		System.out.println(blackListMapper.insertBlack(black));
	}

	public void test2() {
		System.out.println(blackListMapper.findBlackStatusByUserIdAndSuperId(1L, 1L));
		System.out.println(blackListMapper.findBlackInfoByUserIdAndSuperId(1L, 1L));
	}

	public void test3() {
		System.out.println(redisAdapter.zsetScore("canihelpyou", "911"));
		System.out.println(redisAdapter.zsetRem("canihelpyou", "911"));
		System.out.println(redisAdapter.setIsMember("canihelpyou", "911"));
	}

	public void test4() {
		String videoKey = RedisKey.LIVE_VIDEO_INFO_ + 5609;
		if (redisAdapter.existsKey(videoKey)) {
			redisAdapter.hashIncrBy(videoKey, "viewers", 1L);
		}
	}

	public void test5() {
		System.out.println(redisAdapter.strGet("areyouexist"));
	}
	
	/**
	 * 测试zset中score小数
	 */
	public void test6() {
//		System.out.println(redisAdapter.zsetScore("test_zset_score", "test_1"));
		Set<Tuple> set = redisAdapter.zsetRevrangeByScoreWithScores("test_zset_score", Double.MAX_VALUE, 0, 0, 10);
		
		for (Tuple t : set) {
			System.out.println(t.getElement() + ", " + t.getScore());
		}
		
		
	}

	public strictfp void test7() {
		redisAdapter.zsetIncrBy("test_fp", -0.001D, "dkp");
	}

	public strictfp void test8() {
		redisAdapter.strIncrByFloat("video_gift_1633366_170524", 10.0D);
	}

	public strictfp void test9() {
		System.out.println(redisAdapter.setCard("meiyou"));
	}

	@Test
	public void test10() {
		LiveVideoInfo video = new LiveVideoInfo();
		video.setVideoId(100000000000L);
		video.setAnchorId(10000000000L);
		System.out.println(liveVideoMapper.updateByPKAndAnchorId(video));
	}
	
}
