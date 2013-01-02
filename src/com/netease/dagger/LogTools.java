package com.netease.dagger;

import java.io.File;
//import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.Point;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.remote.Augmenter;

/**
 * Log工具类
 * @author chenkan
 */
public class LogTools {

	public static String CustomScreenShotFolder;
	
	public static void log(String logText) {
		System.out.println("[" + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(System.currentTimeMillis())) + "] " + logText);
	}

	/**
	 * 浏览器截屏
	 * @param be	浏览器实例
	 * @return		截屏文件名
	 */
	public static String screenShot(BrowserEmulator be) {

		String dir_name = "screenshot";
		LogTools.limitFiles(dir_name);
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String time = sdf.format(new Date());

		WebDriver augmentedDriver = null;
		if (GlobalSettings.BrowserCoreType == 1 || GlobalSettings.BrowserCoreType == 3) {
			augmentedDriver = be.getBrowserCore();
			be.getBrowserCore().manage().window().setPosition(new Point(0, 0));
			be.getBrowserCore().manage().window().setSize(new Dimension(9999, 9999));
		} else if (GlobalSettings.BrowserCoreType == 2 || GlobalSettings.BrowserCoreType == 21) {
			augmentedDriver = new Augmenter().augment(be.getBrowserCore());
		} else {
			return "Unknown Browser Type";
		}

		try {
			File source_file = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(source_file, new File(dir_name + File.separator + time + ".png"));
		} catch (Exception e) {
			e.printStackTrace();
			LogTools.log("浏览器截屏失败,使用系统级截屏");
			return screenShotForWindows(be);
		}

		LogTools.log("生成截屏文件 " + time + ".png");
		return time + ".png";
	}
	
	/**
	 * 系统级截屏
	 * - 支持Windows
	 * - Grid模式下Node节点截屏回传Hub节点还未实现
	 * @param be
	 * @return
	 */
	public static String screenShotForWindows(BrowserEmulator be) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd-HHmmss");
		String time = sdf.format(new Date());

		String ProjectPath = be.projectRootPath;
		String SeleniumCommandPath = ProjectPath + "res/SeleniumCommand.exe";
		String PngPath = ProjectPath + "screenshot";
		
		LogTools.limitFiles(PngPath);
		// TODO Limit PNG files on grid node
		
		String cmd = SeleniumCommandPath + " takeScreenshot " + PngPath + File.separator + time + ".png";
		
		// Grid Mode
		if (GlobalSettings.BrowserCoreType == 21) {
			be.topMost();
			be.getBrowserCore().neCommand(cmd);
		}

		// Non-Grid Mode
		else {
			Process p = null;
			try {
				be.topMost();
				p = Runtime.getRuntime().exec(cmd);
//				Thread.sleep(500);
				p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
			p.destroy();
		}

		LogTools.log("生成截屏文件 " + time + ".png");
		return time + ".png";
	}
	
	/**
	 * 限制PNG文件数目
	 * @param dir
	 */
	public static void limitFiles(String dir) {
		try {
			File file = new File(dir);
			String[] filelist = file.list();
			if (filelist.length > GlobalSettings.MaxPng) {
				java.util.Arrays.sort(filelist);
				for (int i = 1; i < filelist.length / 2; i++) {
					if (!filelist[i].equalsIgnoreCase(".svn")) {
						File delfile = new File(dir + File.separator + filelist[i]);
						delfile.delete();
						LogTools.log("deleted " + filelist[i]);
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 自定义截屏
	 * @param be
	 * @param msg
	 * @return
	 */
	public static String customScreenShot(BrowserEmulator be, String msg) {

		if (!GlobalSettings.CustomScreenshotEnable) {
			LogTools.log("自定义截屏关闭中");
			return "custom-screenshot-is-closed-now";
		}
		
		String dir = "screenshot_custom";
		String time = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
		
		if (CustomScreenShotFolder == null) { // TODO 用例并发执行时这里有非常小的概率会生成2个截屏文件夹
			CustomScreenShotFolder = dir + File.separator + "Test_Start_At_" + time;
			new File(be.projectRootPath + CustomScreenShotFolder).mkdirs();
			LogTools.log("生成截屏文件夹 " + CustomScreenShotFolder);
			
			// 限制截屏文件夹数量
			// TODO 这个功能在mac环境下异常，Win环境下正常，待研究
			File screenshot_custom = new File(be.projectRootPath + dir);
			String[] filelist = screenshot_custom.list();
			if (filelist.length > GlobalSettings.CustomScreenshotMaxFolders) {
				java.util.Arrays.sort(filelist);
				for (int i = 0; i < filelist.length - GlobalSettings.CustomScreenshotMaxFolders; i++) {
					File delfile = new File(screenshot_custom + File.separator + filelist[i]);
					deleteFile(delfile);
					LogTools.log("deleted " + delfile.getName());
				}
			}
		}
		
		WebDriver augmentedDriver = null;
		if (GlobalSettings.BrowserCoreType == 1 || GlobalSettings.BrowserCoreType == 3) {
			augmentedDriver = be.getBrowserCore();
			be.getBrowserCore().manage().window().setPosition(new Point(0, 0));
			be.getBrowserCore().manage().window().setSize(new Dimension(9999, 9999));
		} else if (GlobalSettings.BrowserCoreType == 2 || GlobalSettings.BrowserCoreType == 21) {
			augmentedDriver = new Augmenter().augment(be.getBrowserCore());
		} else {
			return "Unknown Browser Type";
		}

		String pngFile = CustomScreenShotFolder + File.separator + msg + "_" + time + ".png";
		try {
			File source_file = ((TakesScreenshot) augmentedDriver).getScreenshotAs(OutputType.FILE);
			FileUtils.copyFile(source_file, new File(pngFile));
		} catch (Exception e) {
			e.printStackTrace();
			LogTools.log("浏览器截屏失败");
			return "custom-screenshot-is-failed";	// TODO 补充系统级自定义截屏函数
		}

		LogTools.log("生成截屏文件 " + pngFile);
		return pngFile;
	}
	
	/**
	 * 用于删除非空文件夹
	 * @param file
	 */
	public static void deleteFile(File file) {
		if (file.exists()) {
			if (file.isFile()) {
				file.delete();
			} else if (file.isDirectory()) {
				File files[] = file.listFiles();
				for (int i = 0; i < files.length; i++) {
					deleteFile(files[i]);
				}
			}
			file.delete();
		} else {
			System.out.println("所删除的文件不存在！" + '\n');
		}
	}
}
