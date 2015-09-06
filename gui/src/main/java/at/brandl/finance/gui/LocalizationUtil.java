package at.brandl.finance.gui;

import java.util.ResourceBundle;

public class LocalizationUtil {

	public static String getLocalized(String key) {
		return ResourceBundle.getBundle("messages").getString(key);
	}
}
