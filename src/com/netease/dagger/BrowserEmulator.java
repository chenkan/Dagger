package com.netease.dagger;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.internal.seleniumemulation.ElementFinder;
import org.openqa.selenium.internal.seleniumemulation.JavascriptLibrary;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import com.thoughtworks.selenium.SeleniumException;
import com.thoughtworks.selenium.Wait;

/**
 * BrowserEmulator is based on Selenium2(Webdriver) and adds some enhancements
 * @author ChenKan
 */
public class BrowserEmulator {

	RemoteWebDriver browserCore;
	WebDriverBackedSelenium browser;
	ChromeDriverService chromeServer;
	
	private static Logger logger = Logger.getLogger(BrowserEmulator.class.getName());

	public BrowserEmulator() {
		chooseBrowserCoreType(GlobalSettings.BrowserCoreType);
		browser = new WebDriverBackedSelenium(browserCore, "www.163.com");
		logger.info("Browser started :)");
	}

	public RemoteWebDriver getBrowserCore() {
		return browserCore;
	}

	public WebDriverBackedSelenium getBrowser() {
		return browser;
	}

	/**
	 * Setup browser type
	 * @param type
	 */
	@SuppressWarnings("deprecation")
	private void chooseBrowserCoreType(int type) {

		if (type == 1) {
			browserCore = new FirefoxDriver();
			logger.info("Using Firefox");
			return;
		}

		if (type == 2) {
			chromeServer = new ChromeDriverService.Builder().usingChromeDriverExecutable(new File(GlobalSettings.ChromeDriverPath)).usingAnyFreePort().build();
			try {
				chromeServer.start();
			} catch (IOException e) {
				e.printStackTrace();
			}
			DesiredCapabilities capabilities = DesiredCapabilities.chrome();
			capabilities.setCapability("chrome.switches", Arrays.asList("--start-maximized"));
			browserCore = new RemoteWebDriver(chromeServer.getUrl(), capabilities);
			logger.info("Using Chrome");
			return;
		}

		if (type == 3) {
			String ieDriverExe = "res/IEDriverServer_Win32_2.25.3.exe";	//TODO Read from prop.properties
			System.setProperty("webdriver.ie.driver", ieDriverExe);
			DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			browserCore = new InternetExplorerDriver(capabilities);
			logger.info("Using IE");
			return;
		}

		Assert.fail("Pls setup correct browser type :(");
	}

	/**
	 * Open a url
	 * @param url
	 */
	public void open(String url) {
		pause();
		try {
			browser.open(url);
		} catch (Exception e) {
			e.printStackTrace();
			handleFailIssue("Open url " + url + " failed :(");
		}
		logger.info("Open url " + url);
	}

	/**
	 * Quit browser
	 */
	public void quit() {
		pause();
		browserCore.quit();
		if (GlobalSettings.BrowserCoreType == 2) {
			chromeServer.stop();
		}
		logger.info("Quit browser :)");
	}

	/**
	 * Click a element
	 * @param xpath
	 */
	public void click(String xpath) {
		pause();
		waitForElementPresent(xpath);
		try {
			clickTheClickable(xpath, System.currentTimeMillis(), 2500);
		} catch (Exception e) {
			e.printStackTrace();
			handleFailIssue("Click " + xpath + " failed :(");
		}
		logger.info("Click " + xpath);
	}

	/**
	 * Click a element until it's clickable or timeout
	 * @param xpath
	 * @param startTime
	 * @param timeout
	 * @throws Exception
	 */
	private void clickTheClickable(String xpath, long startTime, int timeout) throws Exception {
		try {
			browserCore.findElementByXPath(xpath).click();
		} catch (Exception e) {
			if (System.currentTimeMillis() - startTime > timeout) {
				logger.error("Element " + xpath + " is unclickable :(");
				throw new Exception(e);
			} else {
				Thread.sleep(500);
				logger.info("Element " + xpath + " is unclickable, try again");
				clickTheClickable(xpath, startTime, timeout);
			}
		}
	}

	/**
	 * Input text
	 * @param xpath
	 * @param text
	 */
	public void type(String xpath, String text) {
		pause();
		waitForElementPresent(xpath);

		WebElement we = browserCore.findElement(By.xpath(xpath));
		try {
			we.clear();
		} catch (Exception e) {
			logger.warn("Failed to clear this textarea :(");
		}
		try {
			we.sendKeys(text);
		} catch (Exception e) {
			e.printStackTrace();
			handleFailIssue("Input " + text + " at " + xpath + " failed :(");
		}

		logger.info("Input " + text + " at " + xpath);
	}

	/**
	 * Mimic system-level keyboard event
	 * @param keyCode
	 */
	public void pressKeyboard(int keyCode) {
		pause();
		Robot rb = null;
		try {
			rb = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		rb.keyPress(keyCode);	// press key
		rb.delay(100); 			// delay 100ms
		rb.keyRelease(keyCode);	// release key
		logger.info("press keyboard " + keyCode);
	}

	//TODO Mimic system-level mouse event
	
	/**
	 * Hover
	 * @param xpath
	 */
	public void mouseOver(String xpath) {
		pause();
		waitForElementPresent(xpath);

		if (GlobalSettings.BrowserCoreType == 1) {
			Assert.fail("Mouseover is not supported now for Firefox :(");
		}
		
		if (GlobalSettings.BrowserCoreType == 2) {
			// First make mouse out of browser
			Robot rb = null;
			try {
				rb = new Robot();
			} catch (AWTException e) {
				e.printStackTrace();
			}
			rb.mouseMove(0, 0);

			// Then hover
			WebElement we = browserCore.findElement(By.xpath(xpath));
			try {
				Actions builder = new Actions(browserCore);
				builder.moveToElement(we).build().perform();
			} catch (Exception e) {
				e.printStackTrace();
				handleFailIssue("Mouseover " + xpath + " failed :(");
			}

			logger.info("Mouseover " + xpath);
			return;
		} 

		if (GlobalSettings.BrowserCoreType == 3) {
			Assert.fail("Mouseover is not supported now for IE :(");
		}
		
		Assert.fail("Pls setup correct browser type :(");
	}

	/**
	 * Get text of a element
	 * @param xpath
	 * @return text
	 */
	public String getText(String xpath) {
		pause();
		waitForElementPresent(xpath);
		String text = browserCore.findElementByXPath(xpath).getText();
		logger.info("Get text " + text + " from " + xpath);
		return text;
	}

	/**
	 * Switch window/tab	//TODO more detailed info
	 * @param windowTitle
	 */
	public void selectWindow(String windowTitle) {
		pause();
		browser.selectWindow(windowTitle);
		logger.info("Switch to window " + windowTitle);
	}

	// /**
	// * 选中frame/iframe元素中嵌套着的html元素之前要先选中该frame/iframe
	// * @param frameId
	// */
	// public void selectFrame(String frameId) {
	//
	// pause();
	//
	// Browser.selectFrame(frameId);
	// }

	/**
	 * 设置BrowserEmulator各次操作之间的时间间隔，单位毫秒
	 */
	private void pause() {

		int StepInterval = Integer.parseInt(GlobalSettings.StepInterval);

		if (StepInterval > 0) {
			try {
				Thread.sleep(StepInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			return;
		}
	}

	/**
	 * 页面元素定位
	 * 
	 * @param locator
	 *            暂时只支持xpath，后续考虑支持更多形式，如：id，name
	 */
	private void waitForElementPresent(final String locator) {

		int Timeout = Integer.parseInt(GlobalSettings.Timeout);

		try {
			new Wait() {
				public boolean until() {
					// return Browser.isElementPresent(locator);
					return isElementPresent(browserCore, locator);
				}
			}.wait("*** 页面元素(" + locator + ")ㄎ皇О� ***", Timeout);
		} catch (Exception e) {
			e.printStackTrace();
			// LogTools.log("*** 页面元素(" + locator + ")定位失败 ***");
			String PNG = LogTools.screenShot(this);
			logger.error("*** 页面元素(" + locator + ")定位失败 *** 截屏文件 " + PNG);
			Assert.fail("*** 页面元素(" + locator + ")定位失败 *** 截屏文件 " + PNG);
		}

		logger.info("页面上存在元素 " + locator);
	}

	/**
	 * 页面元素定位
	 * 
	 * @param locator
	 * @param timeout
	 */
	private void waitForElementPresent(final String locator, int timeout) {

		// int Timeout = Integer.parseInt(GlobalSettings.Timeout);

		try {
			new Wait() {
				public boolean until() {
					// return Browser.isElementPresent(locator);
					return isElementPresent(browserCore, locator);
				}
			}.wait("*** 页面元素(" + locator + ")定位失败 ***", timeout);
		} catch (Exception e) {
			e.printStackTrace();
			// LogTools.log("*** 页面元素(" + locator + ")定位失败 ***");
			String PNG = LogTools.screenShot(this);
			logger.error("*** 页面元素(" + locator + ")定位失败 *** 截屏文件 " + PNG);
			Assert.fail("*** 页面元素(" + locator + ")定位失败 *** 截屏文件 " + PNG);
		}

		logger.info("页面上存在元素 " + locator);
	}

	/**
	 * 判断页面上是否存在（无论是否可见）指定文本
	 * 
	 * @param expectExist
	 * <br>
	 *            true：期望文本存在，若不存在，Assert.fail()<br>
	 *            false：期望文本不存在，若存在，Assert.fail()
	 * @param text
	 * @param timeout
	 *            以此为限在页面上查找文本，单位毫秒
	 * @rebuild ChenKan - 2012/06/05
	 */
	public void expectTextExistOrNot(boolean expectExist, final String text, int timeout) {

		// 期望文本存在
		if (expectExist) {
			try {
				new Wait() {
					public boolean until() {
						return isTextPresent(browserCore, text);
					}
				}.wait("*** 判断页面上是否存在指定文本 ***", timeout);
			} catch (Exception e) {
				e.printStackTrace();
				String PNG = LogTools.screenShot(this);
				logger.error("*** 页面上不存在文本：" + text + " *** 截屏文件 " + PNG);
				Assert.fail("*** 页面上不存在文本：" + text + " *** 截屏文件 " + PNG);
				return;
			}

			logger.info("*** 页面上存在文本：" + text + " ***");
			return;
		}

		// 期望文本不存在
		else {

			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (isTextPresent(browserCore, text)) {
				String PNG = LogTools.screenShot(this);
				logger.error("*** 文本：" + text + "在页面上存在 *** 截屏文件 " + PNG);
				Assert.fail("*** 文本：" + text + "在页面上存在 *** 截屏文件 " + PNG);
			} else {
				logger.info("文本：" + text + "在页面上不存在");
				return;
			}

		}

		// try {
		// new Wait() {
		// public boolean until() {
		// // return Browser.isTextPresent(Text);
		// return isTextPresent(BrowserCore, Text);
		// }
		// }.wait("*** 判断页面上是否存在指定文本 ***", Timeout);
		// } catch (Exception e) {
		//
		// e.printStackTrace();
		//
		// if (expectExist) {
		// // LogTools.log("*** 页面上不存在文本：" + Text + " ***");
		// String PNG = LogTools.screenShot(this);
		// logger.error("*** 页面上不存在文本：" + Text + " *** 截屏文件 " + PNG);
		// Assert.fail("*** 页面上不存在文本：" + Text + " *** 截屏文件 " + PNG);
		// } else {
		// // System.out.println("*** 页面上不存在文本：" + Text + " ***");
		// // LogTools.log("*** 页面上不存在文本：" + Text + " ***");
		// logger.info("*** 页面上不存在文本：" + Text + " ***");
		// }
		//
		// return;
		// }
		//
		// if (expectExist) {
		// // System.out.println("*** 页面上存在文本：" + Text + " ***");
		// // LogTools.log("*** 页面上存在文本：" + Text + " ***");
		// logger.info("*** 页面上存在文本：" + Text + " ***");
		// } else {
		// // LogTools.log("*** 页面上存在文本：" + Text + " ***");
		// String PNG = LogTools.screenShot(this);
		// logger.error("*** 页面上存在文本：" + Text + " *** 截屏文件 " + PNG);
		// Assert.fail("*** 页面上存在文本：" + Text + " *** 截屏文件 " + PNG);
		// }
		//
		// return;

	}

	/**
	 * 判断页面上是否可见指定元素
	 * 
	 * @param expectExist
	 * <br>
	 *            true：期望元素可见，若不可见，Assert.fail()<br>
	 *            false：期望元素不可见，若可见，Assert.fail()
	 * @param xPath
	 * @param timeout
	 *            以此为限在页面上查找元素，单位毫秒
	 * @rebuild ChenKan -- 2012-03-09
	 */
	public void expectElementExistOrNot(boolean expectExist, final String xPath, int timeout) {

		/**
		 * 预期元素可见
		 */
		if (expectExist) {

			// 先等待元素存在，再判断是否可见；元素不存在的情况下使用isVisible()函数，会直接抛出异常
			waitForElementPresent(xPath, timeout);

			try {
				new Wait() {
					public boolean until() {
						// return Browser.isVisible(XPath);
						return browserCore.findElementByXPath(xPath).isDisplayed();
					}
				}.wait("*** 判断页面上是否可见指定元素 ***", timeout);
			} catch (Exception e) {
				e.printStackTrace();
				// LogTools.log("*** 页面上不可见元素：" + XPath + " ***");
				String PNG = LogTools.screenShot(this);
				logger.error("*** 页面上不可见元素：" + xPath + " *** 截屏文件 " + PNG);
				Assert.fail("*** 页面上不可见元素：" + xPath + " *** 截屏文件 " + PNG);
				return;
			}

			// System.out.println("*** 页面上可见元素：" + XPath + " ***");
			// LogTools.log("*** 页面上可见元素：" + XPath + " ***");
			logger.info("*** 页面上可见元素：" + xPath + " ***");
			return;

		}

		/**
		 * 预期元素不可见
		 */
		else {

			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			// 先判断元素是否存在
			// if (!Browser.isElementPresent(XPath)) {
			if (!isElementPresent(browserCore, xPath)) {
				// System.out.println("元素：" + XPath + "在页面上不存在");
				// LogTools.log("元素：" + XPath + "在页面上不存在");
				logger.info("元素：" + xPath + "在页面上不存在");
				return;
			}

			// 再判断元素是否可见
			// if (Browser.isVisible(XPath)) {
			if (browserCore.findElementByXPath(xPath).isDisplayed()) {
				// LogTools.log("元素：" + XPath + "在页面上可见");
				String PNG = LogTools.screenShot(this);
				logger.error("*** 元素：" + xPath + "在页面上可见 *** 截屏文件 " + PNG);
				Assert.fail("*** 元素：" + xPath + "在页面上可见 *** 截屏文件 " + PNG);
			} else {
				// System.out.println("元素：" + XPath + "在页面上不可见");
				// LogTools.log("元素：" + XPath + "在页面上不可见");
				logger.info("元素：" + xPath + "在页面上不可见");
				return;
			}

		}
	}

	/**
	 * 判断是否存在指定文本（不管文本是否可见）
	 * 
	 * @param text
	 * @return
	 */
	public boolean isTextPresent(String text) {

		// if (Browser.isTextPresent(Text)) {
		if (isTextPresent(browserCore, text)) {
			logger.info("页面上存在文本 " + text);
		} else {
			logger.info("页面上不存在文本 " + text);
		}

		// return Browser.isTextPresent(Text);
		return isTextPresent(browserCore, text);
	}

	/**
	 * 判断是否存在（以及可见）指定元素
	 * 
	 * @param xPath
	 * @return
	 */
	public boolean isElementPresent(String xPath) {
		// return Browser.isElementPresent(Xpath);
		// return Browser.isVisible(Xpath);
		// if (Browser.isElementPresent(Xpath)) {
		if (isElementPresent(browserCore, xPath)) {
			// if (Browser.isVisible(Xpath)) {
			if (browserCore.findElementByXPath(xPath).isDisplayed()) {
				logger.info("页面上可见元素 " + xPath);
			} else {
				logger.info("页面上不可见元素 " + xPath);
			}
			// return Browser.isVisible(Xpath);
			return browserCore.findElementByXPath(xPath).isDisplayed();
		} else {
			logger.info("页面上不存在元素 " + xPath);
			return false;
		}
	}

	/**
	 * 在iframe中输入文本
	 * 
	 * @param nFrame
	 *            iframe在页面中的次序
	 * @param text
	 *            输入的文本
	 */
	public void typeInFrame(int nFrame, String text) {
		typeInFrameConsiderFocus(nFrame, text, true);
	}

	private void typeInFrameConsiderFocus(int nFrame, String text, Boolean needFocus) {

		pause();
		// TODO 须等待页面加载iframe节点

		// 进入指定iframe
		if (needFocus) {
			browser.windowFocus(); // TODO it only works for chrome
		}
		browserCore.switchTo().frame(nFrame);

		// 进入编辑节点
		WebElement editable = browserCore.switchTo().activeElement();
		editable.sendKeys(text);

		// 返回
		browserCore.switchTo().defaultContent();
		logger.info("在Frame " + nFrame + " 中输入文本 " + text);
	}

	/**
	 * 在Windows系统控件中输入文本 例如：Windows弹框
	 * 参见：http://www.cnblogs.com/190196539/archive/2011/02/11/1951707.html
	 * 
	 * @param text
	 */
	public void typeInWindows(String text) {

		String SeleniumCommandPath = projectRootPath + "res/SeleniumCommand.exe";
		String cmd = SeleniumCommandPath + " sendKeys " + text;

		logger.info(cmd);

		// Grid Mode
		if (GlobalSettings.BrowserCoreType == 21) {
			browserCore.neCommand(cmd);
		}

		// Non-Grid Mode
		else {
			Process p = null;
			try {
				p = Runtime.getRuntime().exec(cmd);
				p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
			p.destroy();
		}

		logger.info("input " + text + " in Windows");
	}

	/**
	 * 从Selenium源码复制isElementPresnt()实现 - WebDriverCommandProcessor.java -
	 * IsElementPresent.java
	 * 
	 * @param driver
	 * @param locator
	 * @return
	 */
	private boolean isElementPresent(RemoteWebDriver driver, String locator) {
		try {
			JavascriptLibrary javascriptLibrary = new JavascriptLibrary();
			ElementFinder finder = new ElementFinder(javascriptLibrary);
			finder.findElement(driver, locator);
			return true;
		} catch (SeleniumException e) {
			return false;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * 从Selenium源码复制isTextPresent()实现 - WebDriverCommandProcessor.java -
	 * IsTextPresent.java
	 * 
	 * @param driver
	 * @param text
	 * @return
	 */
	private boolean isTextPresent(RemoteWebDriver driver, String text) {
		JavascriptLibrary javascriptLibrary = new JavascriptLibrary();
		String script = javascriptLibrary.getSeleniumScript("isTextPresent.js");
		Boolean result = (Boolean) ((JavascriptExecutor) driver).executeScript("return (" + script + ")(arguments[0]);", text);
		return Boolean.TRUE == result;
	}

	/**
	 * 刷新页面
	 */
	public void refresh() {
		browserCore.navigate().refresh();
	}

	/**
	 * 获取工程根目录
	 * 
	 * @return path
	 */
	public String getRootPath() {
		String path = System.getProperty("user.dir").replaceAll("\\\\", "/").concat("/");
		logger.info("Project's root path is " + path);
		return path;
	}

	/**
	 * 截屏
	 * 
	 * @param type
	 * <br>
	 *            type = 0 浏览器截屏 <br>
	 *            type = 1 系统级截屏
	 * @return 截屏文件名
	 */
	public String screenShot(int type) {

		if (type == 0) {
			return LogTools.screenShot(this);
		}

		if (type == 1) {
			return LogTools.screenShotForWindows(this);
		}

		return "Pls input correct screenshot type!";
	}

	/**
	 * 浏览器置顶 - 支持Windows
	 */
	public void topMost() {

		String SeleniumCommandPath = projectRootPath + "res/SeleniumCommand.exe";
		String cmd = "";

		String currentTitle = browserCore.getTitle();
		String newTitle = String.valueOf(System.currentTimeMillis());

		js.executeScript("return document.title='" + newTitle + "'");
		try {
			// make sure js has been executed
			Thread.sleep(250);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		if (GlobalSettings.BrowserCoreType == 1) {
			cmd = SeleniumCommandPath + " windowFocus \"" + newTitle + " - Mozilla Firefox" + "\"";
		}

		if (GlobalSettings.BrowserCoreType == 2 || GlobalSettings.BrowserCoreType == 21) {
			cmd = SeleniumCommandPath + " windowFocus \"" + newTitle + " - Google Chrome" + "\"";
		}

		if (GlobalSettings.BrowserCoreType == 3) {
			cmd = SeleniumCommandPath + " windowFocus \"" + newTitle + " - Windows Internet Explorer" + "\"";
		}

		logger.info(cmd);

		// Grid Mode
		if (GlobalSettings.BrowserCoreType == 21) {
			browserCore.neCommand(cmd);
		}

		// Non-Grid Mode
		else {
			Process p = null;
			try {
				p = Runtime.getRuntime().exec(cmd);
				// Thread.sleep(500);
				p.waitFor();
			} catch (Exception e) {
				e.printStackTrace();
			}
			p.destroy();
		}

		js.executeScript("return document.title='" + currentTitle + "'");
		logger.info("浏览器 " + currentTitle + "置顶");
	}

	/**
	 * 进入指定iframe
	 */
	public void enterFrame(String locator) {
		pause();
		browserCore.switchTo().frame(browserCore.findElementByXPath(locator));
	}

	/**
	 * 返回顶层iframe
	 */
	public void leaveFrame() {
		pause();
		browserCore.switchTo().defaultContent();
	}

	/**
	 * 处理alert框
	 */
	public void handleAlert() {
		try {
			// 等待弹出框加载
			Thread.sleep(2500);
			Alert alert = browserCore.switchTo().alert();
			logger.info("需要确认的信息是：" + alert.getText());
			alert.accept();
			logger.info("你确认了此信息！");
		} catch (Exception e) {
			logger.error("确认信息失败！");
			e.printStackTrace();
			Assert.fail("确认信息失败！");
		}

	}

	/**
	 * 处理confirm框
	 * 
	 * @param expect
	 *            确认或取消
	 */
	public void handleConfirm(boolean expect) {
		try {
			Thread.sleep(2500);
			Alert alert = browserCore.switchTo().alert();
			if (expect) {
				logger.info("需要确认的信息是：" + alert.getText());
				alert.accept();
				logger.info("你确认了此信息！");
			} else {
				logger.info("需要取消确认的信息是：" + alert.getText());
				alert.dismiss();
				logger.info("你选择了取消确认此信息！");
			}
		} catch (Exception e) {
			if (expect) {
				logger.error("确认信息失败！");
				e.printStackTrace();
				Assert.fail("确认信息失败！");
			} else {
				logger.error("取消确认信息失败！");
				e.printStackTrace();
				Assert.fail("取消确认信息失败！");
			}

		}

	}

	/**
	 * 处理prompt框
	 * 
	 * @param prompt
	 *            输入信息
	 * @param expect
	 *            确认输入或取消输入
	 * 
	 */
	public void handlePrompt(String prompt, boolean expect) {
		try {
			Thread.sleep(2500);
			Alert alert = browserCore.switchTo().alert();
			alert.sendKeys(prompt);
			logger.info("你输入了信息：" + prompt);
			if (expect) {
				alert.accept();
				logger.info("你确认了输入！");
			} else {
				alert.dismiss();
				logger.info("你取消了输入！");
			}

		} catch (Exception e) {
			if (expect) {
				logger.error("确认输入信息失败！");
				e.printStackTrace();
				Assert.fail("确认输入信息失败！");
			} else {
				logger.error("取消输入信息失败！");
				e.printStackTrace();
				Assert.fail("取消输入信息失败！");
			}
		}

	}
	
	
	
	
	private void handleFailIssue(String notice) {
		String png = LogTools.screenShot(this);
		String log = notice + " screenshot at " + png;
		logger.error(log);
		Assert.fail(log);
	}
}
