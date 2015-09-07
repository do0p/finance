package at.brandl.finance.gui;

import java.util.ResourceBundle;

import org.apache.commons.lang3.StringUtils;

public class LocalizationUtil {

	public static String getLocalized(String key) {
		if(StringUtils.isBlank(key)) {
			return key;
		}
		return ResourceBundle.getBundle("messages").getString(key);
	}
}
