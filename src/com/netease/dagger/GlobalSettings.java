package com.netease.dagger;

import java.io.FileInputStream;
import java.util.Properties;

public class GlobalSettings {

	public static Properties prop = getProperties();

	public static int BrowserCoreType = Integer.parseInt(prop.getProperty("BrowserCoreType", "2"));

	public static String ChromeDriverPath = prop.getProperty("ChromeDriverPath", "res/chromedriver.exe");
	
	public static String IEDriverPath = prop.getProperty("IEDriverPath", "res/iedriver_32.exe");

	public static String StepInterval = prop.getProperty("StepInterval", "500");

	public static String Timeout = prop.getProperty("Timeout", "30000");

	public static String getProperty(String Property) {
		return prop.getProperty(Property);
	}
	
	public static Properties getProperties() {
		Properties prop = new Properties();
		try {
			FileInputStream file = new FileInputStream("prop.properties");
			prop.load(file);
			file.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop;
	}
}