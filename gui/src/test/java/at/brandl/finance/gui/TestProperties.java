package at.brandl.finance.gui;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class TestProperties {

	private static Properties properties;

	static {
		properties = new Properties();
		try (InputStream iStream = TestProperties.class
				.getResourceAsStream("/test.properties")) {
			properties.load(iStream);
		} catch (IOException e) {
			throw new AssertionError(e);
		}

	}

	public static String getTestFile(String fileName) {
		return properties.get("fileDir") + File.separator + fileName;
	}
}
