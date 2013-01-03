package com.netease.dagger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;

public class LogTools {

	public static void log(String logText) {
		System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())) + "] " + logText);
	}

	public static String screenShot(BrowserEmulator be) {
		String dir = "screenshot"; // TODO
		String time = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
		String screenShotPath = dir + File.separator + time + ".png";

		WebDriver augmentedDriver = null;
		if (GlobalSettings.BrowserCoreType == 1 || GlobalSettings.BrowserCoreType == 3) {
			augmentedDriver = be.getBrowserCore();
			augmentedDriver.manage().window().setPosition(new Point(0, 0));
			augmentedDriver.manage().window().setSize(new Dimension(9999, 9999));
		} else if (GlobalSettings.BrowserCoreType == 2) {
			augmentedDriver = new Augmenter().augment(be.getBrowserCore());
		} else {
			return "Incorrect browser type";
		}

		try {
			File sourceFile = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(sourceFile, new File(screenShotPath));
		} catch (Exception e) {
			e.printStackTrace();
			return "Failed to screenshot";
		}

		return screenShotPath;
	}

}
