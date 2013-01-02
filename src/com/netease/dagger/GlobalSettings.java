package com.netease.dagger;

import java.io.FileInputStream;
import java.util.Properties;

/**
 * 全局变量设置
 * @author ChenKan
 */
public class GlobalSettings {

	public static Properties prop = getProp();

	// 设置浏览器类型
	// 1 >> FireFox
	// 2 >> Chrome
	// 3 >> IE
	public static int BrowserCoreType = Integer.parseInt(prop.getProperty("BrowserCoreType", "2"));

	// 根据操作系统类型自动设置ChromeDriver
	public static String ChromeDriverPath = setChromedriver();

	// BrowserEmulator各次操作之间的时间间隔，单位毫秒
	public static String StepInterval = prop.getProperty("StepInterval", "500");

	// BrowserEmulator等待超时时间，单位毫秒
	public static String Timeout = prop.getProperty("Timeout", "30000");

	// 截屏文件上限
	public static int MaxPng = Integer.parseInt(prop.getProperty("MaxPng", "250"));

	// 自定义截屏开关
	public static boolean CustomScreenshotEnable = true;

	// 自定义截屏文件夹上限
	public static int CustomScreenshotMaxFolders = 3;

	public static String setChromedriver() {

		Properties props = System.getProperties(); // 获得系统属性集
		String osName = props.getProperty("os.name"); // 操作系统名称

		osName = osName.toLowerCase();
		System.out.println("osName");
		System.out.println(osName);

		if (osName.contains("win")) {
			System.out.println("the OS is win");
			return "res/chromedriver.exe";
		} else {
			System.out.println("the OS is linux");
			return "res/chromedriver";
		}
	}

	public static Properties getProp() {
		Properties prop = new Properties();
		FileInputStream fis;
		try {
			fis = new FileInputStream("prop.properties");
			prop.load(fis);
			fis.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return prop;
	}

	public static void main(String[] args) {
		System.out.println(BrowserCoreType);
		System.out.println(StepInterval);
		System.out.println(Timeout);
		System.out.println(MaxPng);
	}
}