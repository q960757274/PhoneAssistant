package com.libowen.assistant.test;

import java.util.List;
import java.util.Random;

import android.test.AndroidTestCase;

import com.libowen.assistant.db.dao.BlackNumberDao;
import com.libowen.assistant.domain.BlackNumber;

public class TestBlackNumberDao extends AndroidTestCase {
	//向数据库中添加50条数据
	public void testAdd() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		//第一个要被添加的号码
		int number = 100000;
		Random random = new Random();
		for (int i = 0; i < 50; i++) {
			int result = (number+i);
			//执行添加操作。random.nextInt(3)表示的随机数为0、1、2
			dao.add(result+"", random.nextInt(3)+"");
		}
	}
	//更新数据库中的数据
	public void testUpdate() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		dao.update("100000", "999999", "2");
	}
	//删除数据库中的数据
	public void testDelete() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		dao.delete("999999");
	}
	//查询数据库表中的所有的号码
	public void testFindAll() throws Exception {
		BlackNumberDao dao = new BlackNumberDao(getContext());
		List<BlackNumber> numbers = dao.findAll();
		System.out.println(numbers.size());
	}
}
