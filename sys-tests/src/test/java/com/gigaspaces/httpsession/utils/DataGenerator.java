package com.gigaspaces.httpsession.utils;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import com.gigaspaces.httpsession.models.DataUnit;
import com.gigaspaces.httpsession.models.Node;
//import com.gigaspaces.httpsession.qa.User;
import com.gigaspaces.httpsession.serialize.SerializeUtils;

public class DataGenerator {

	private static final String USER_KEY = "user";

	public static Node genetateCircularReferences() {

		Node parent = new Node("parent", null);

		Node level1 = new Node("level1", parent);
		level1.add(level1);
		level1.add(parent);

		Node level2 = new Node("level2", level1);
		level2.add(parent);
		level2.add(level1);

		level1.add(level2);

		parent.add(level1);
		return parent;
	}

//	public static User generateUser(int index) {
//		Random randomGenerator = new Random();
//
//		User user = new User();
//		user.setName(USER_KEY + index);
//		user.setId(randomGenerator.nextLong());
//		user.setAge(randomGenerator.nextInt(70));
//		user.setBithday(new Date());
//
//		return user;
//	}

	public static Map<String, DataUnit> generateDataFile(String path) throws IOException {

		StringBuilder builder = new StringBuilder();
		
		Map<String, DataUnit> expected = new HashMap<String, DataUnit>();

		//appendData(USER_KEY, generateUser(10), "B", builder, expected);
		
		FileUtils.writeStringToFile(new File(path), builder.toString());
		
		return expected;
	}

	public static void appendData(String key, Object data, String action,
			StringBuilder builder, Map<String, DataUnit> expected) {

		byte[] buff = SerializeUtils.serialize(data);

		byte[] buff1 = Base64.encodeBase64(buff);

		String value = new String(buff1);

		builder.append(key);
		builder.append(',');
		builder.append(value);
		builder.append(',');
		builder.append(action);
		builder.append('\n');

		expected.put(key, new DataUnit(key, data, action));
	}
}
