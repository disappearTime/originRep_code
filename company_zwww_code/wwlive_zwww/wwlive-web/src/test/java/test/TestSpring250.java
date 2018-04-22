package test;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import com.chineseall.iwanvi.wwlive.dao.wwlive.WinningRecordsMapper;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:spring/application.xml" })
public class TestSpring250 {

	@Autowired
	private WinningRecordsMapper winningRecordsMapper;

	// TODO
	@Test
	public void test1() {
		System.out.println(winningRecordsMapper.sumWinningRecords4Manage());
//		System.out.println(winningRecordsMapper.sumWinningRecordsWithParams4Manage(10101L, "", ""));
	}
	
}
