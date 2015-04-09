package com.gigaspaces.httpsession.qa.utils;

import java.io.File;
import java.io.IOException;
import java.util.*;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import com.gigaspaces.httpsession.qa.Contract;
import com.gigaspaces.httpsession.qa.DataUnit;
import com.gigaspaces.httpsession.qa.User;
import com.gigaspaces.httpsession.serialize.SerializeUtils;

public class DataGenerator {

	private static final String USER_KEY = "user";
    private static int lastIndex = 0;

	public static Contract generateContract(int index) {

		Contract contract = new Contract();

		return contract;
	}

	public static User generateUser(int index) {
		Random randomGenerator = new Random();

		User user = new User();
		user.setName(USER_KEY + index);
		user.setId(randomGenerator.nextLong());
		user.setAge(randomGenerator.nextInt(70));
		user.setBithday(new Date());

		return user;
	}

	public static User generateUserWithContract(int index) {

		User user = generateUser(index);

//		user.setContract(generateContract(index));

		return user;
	}

    public static Map<String, DataUnit> generateData()
            throws IOException {
        return generateData(4);
    }

    public static Map<String, DataUnit> generateData(int count)
            throws IOException {
        StringBuilder builder = new StringBuilder();

        Map<String, DataUnit> expected = new HashMap<String, DataUnit>();

        for (int i = 1; i <= count; i++) {
            if (i%2 == 0) {
                User user = generateUser(i);
                appendData(USER_KEY + (lastIndex + i), user, "B", builder, expected, true);
            } else {
                appendData(USER_KEY + (lastIndex + i), UUID.randomUUID().toString(), "", builder, expected, false);
            }
        }

        lastIndex += count;


        return expected;
    }

	public static Map<String, DataUnit> generateDataFile(String path)
			throws IOException {

		StringBuilder builder = new StringBuilder();

		Map<String, DataUnit> expected = new HashMap<String, DataUnit>();

		for (int i = 0; i < 1; i++) {

			User user = generateUser(i);

			appendData(USER_KEY + (i+i), user, "B", builder, expected, true);
        }

        FileUtils.writeStringToFile(new File(path), builder.toString());

		return expected;
	}

	public static void appendData(String key, Object data, String action,
			StringBuilder builder, Map<String, DataUnit> expected, boolean serialize) {

        if (serialize) {
            byte[] buff = SerializeUtils.serialize(data);

            byte[] buff1 = Base64.encodeBase64(buff);

            String value = new String(buff1);

            builder.append(key);
            builder.append(',');
            builder.append(value);
            builder.append(',');
            builder.append(action);
            builder.append('\n');

        } else {
            builder.append(key);
            builder.append(',');
            builder.append(data);
            builder.append(',');
            builder.append(action);
            builder.append('\n');
        }
        expected.put(key, new DataUnit(key, data, action, serialize));
	}

    public static void manipulateData(Map<String, DataUnit> stringDataUnitMap) {
        Iterator<String> it = stringDataUnitMap.keySet().iterator();
        while (it.hasNext()) {
            String key = it.next();
            DataUnit dataUnit = stringDataUnitMap.get(key);

            if (dataUnit.getDatavalue() instanceof String) {
                dataUnit.setDatavalue(UUID.randomUUID().toString());
            } else if (dataUnit.getDatavalue() instanceof User) {
                ((User) dataUnit.getDatavalue()).setAge(((User) dataUnit.getDatavalue()).getAge()*10);
            }
        }
    }
}
