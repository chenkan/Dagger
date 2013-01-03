package com.netease.dagger;

import java.awt.AWTException;
import java.awt.Robot;
import java.util.Arrays;
import java.io.File;
import java.io.IOException;
import org.apache.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriverBackedSelenium;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.testng.Assert;
import com.thoughtworks.selenium.Wait;

/**
 * BrowserEmulator is based on Selenium2 and adds some enhancements
 */
public class BrowserEmulator {

	RemoteWebDriver browserCore;
	WebDriverBackedSelenium browser;
	ChromeDriverService chromeServer;
	JavascriptExecutor javaScriptExecutor;
	
	int stepInterval = Integer.parseInt(GlobalSettings.StepInterval);
	int timeout = Integer.parseInt(GlobalSettings.Timeout);
	
	private static Logger logger = Logger.getLogger(BrowserEmulator.class.getName());

	public BrowserEmulator() {
		setupBrowserCoreType(GlobalSettings.BrowserCoreType);
		browser = new WebDriverBackedSelenium(browserCore, "www.163.com");
		javaScriptExecutor = (JavascriptExecutor) browserCore;
		logger.info("Started BrowserEmulator");
	}

	@SuppressWarnings("deprecation")
	private void setupBrowserCoreType(int type) {
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
			System.setProperty("webdriver.ie.driver", GlobalSettings.IEDriverPath);
			DesiredCapabilities capabilities = DesiredCapabilities.internetExplorer();
			capabilities.setCapability(InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS, true);
			browserCore = new InternetExplorerDriver(capabilities);
			logger.info("Using IE");
			return;
		}

		Assert.fail("Incorrect browser type");
	}
	
	public RemoteWebDriver getBrowserCore() {
		return browserCore;
	}

	public WebDriverBackedSelenium getBrowser() {
		return browser;
	}
	
	public JavascriptExecutor getJavaScriptExecutor() {
		return javaScriptExecutor;
	}

	public void open(String url) {
		pause(stepInterval);
		try {
			browser.open(url);
		} catch (Exception e) {
			e.printStackTrace();
			handleFailure("Failed to open url " + url);
		}
		logger.info("Opened url " + url);
	}

	public void quit() {
		pause(stepInterval);
		browserCore.quit();
		if (GlobalSettings.BrowserCoreType == 2) {
			chromeServer.stop();
		}
		logger.info("Quitted BrowserEmulator");
	}

	public void click(String xpath) {
		pause(stepInterval);
		expectElementExistOrNot(true, xpath, timeout);
		try {
			clickTheClickable(xpath, System.currentTimeMillis(), 2500);
		} catch (Exception e) {
			e.printStackTrace();
			handleFailure("Failed to click " + xpath);
		}
		logger.info("Clicked " + xpath);
	}

	/**
	 * Click an element until it's clickable or timeout
	 * @param xpath
	 * @param startTime
	 * @param timeout in millisecond
	 * @throws Exception
	 */
	private void clickTheClickable(String xpath, long startTime, int timeout) throws Exception {
		try {
			browserCore.findElementByXPath(xpath).click();
		} catch (Exception e) {
			if (System.currentTimeMillis() - startTime > timeout) {
				logger.info("Element " + xpath + " is unclickable");
				throw new Exception(e);
			} else {
				Thread.sleep(500);
				logger.info("Element " + xpath + " is unclickable, try again");
				clickTheClickable(xpath, startTime, timeout);
			}
		}
	}

	/**
	 * Type text<br>
	 * Before typing, try to clear existed text
	 * @param xpath
	 * @param text
	 */
	public void type(String xpath, String text) {
		pause(stepInterval);
		expectElementExistOrNot(true, xpath, timeout);

		WebElement we = browserCore.findElement(By.xpath(xpath));
		try {
			we.clear();
		} catch (Exception e) {
			logger.warn("Failed to clear text at " + xpath);
		}
		try {
			we.sendKeys(text);
		} catch (Exception e) {
			e.printStackTrace();
			handleFailure("Failed to type " + text + " at " + xpath);
		}

		logger.info("Type " + text + " at " + xpath);
	}

	/**
	 * Hover/Mouseover
	 * @param xpath
	 */
	public void mouseOver(String xpath) {
		pause(stepInterval);
		expectElementExistOrNot(true, xpath, timeout);

		if (GlobalSettings.BrowserCoreType == 1) {
			Assert.fail("Mouseover is not supported for Firefox now");
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
				handleFailure("Failed to mouseover " + xpath);
			}

			logger.info("Mouseover " + xpath);
			return;
		} 
		if (GlobalSettings.BrowserCoreType == 3) {
			Assert.fail("Mouseover is not supported for IE now");
		}
		
		Assert.fail("Incorrect browser type");
	}

	/**
	 * Switch window/tab
	 * @param windowTitle
	 */
	public void selectWindow(String windowTitle) {
		pause(stepInterval);
		browser.selectWindow(windowTitle);
		logger.info("Switched to window " + windowTitle);
	}

	public void enterFrame(String xpath) {
		pause(stepInterval);
		browserCore.switchTo().frame(browserCore.findElementByXPath(xpath));
		logger.info("Entered iframe " + xpath);
	}

	public void leaveFrame() {
		pause(stepInterval);
		browserCore.switchTo().defaultContent();
		logger.info("Back default iframe");
	}
	
	public void refresh() {
		pause(stepInterval);
		browserCore.navigate().refresh();
		logger.info("Refreshed");
	}
	
	/**
	 * Mimic system-level keyboard event
	 * @param keyCode
	 */
	public void pressKeyboard(int keyCode) {
		pause(stepInterval);
		Robot rb = null;
		try {
			rb = new Robot();
		} catch (AWTException e) {
			e.printStackTrace();
		}
		rb.keyPress(keyCode);	// press key
		rb.delay(100); 			// delay 100ms
		rb.keyRelease(keyCode);	// release key
		logger.info("Pressed key with code " + keyCode);
	}

	//TODO Mimic system-level mouse event

	/**
	 * Expect some text exist or not on the page
	 * @param expectExist
	 * @param text
	 * @param timeout in millisecond
	 */
	public void expectTextExistOrNot(boolean expectExist, final String text, int timeout) {
		if (expectExist) {
			try {
				new Wait() {
					public boolean until() {
						return isTextPresent(text);
					}
				}.wait("Failed to find text " + text, timeout);
			} catch (Exception e) {
				e.printStackTrace();
				handleFailure("Failed to find text " + text);
			}
			logger.info("Found desired text " + text);
		} else {
			pause(timeout);
			if (isTextPresent(text)) {
				handleFailure("Found undesired text " + text);
			} else {
				logger.info("Not found undesired text " + text);
			}
		}
	}

	/**
	 * Expect an element exist or not on the page
	 * @param expectExist
	 * @param xpath
	 * @param timeout in millisecond
	 */
	public void expectElementExistOrNot(boolean expectExist, final String xpath, int timeout) {
		if (expectExist) {
			try {
				new Wait() {
					public boolean until() {
						return isElementPresent(xpath);
					}
				}.wait("Failed to find element " + xpath, timeout);
			} catch (Exception e) {
				e.printStackTrace();
				handleFailure("Failed to find element " + xpath);
			}
			logger.info("Found desired element " + xpath);
		} else {
			pause(timeout);
			if (isElementPresent(xpath)) {
				handleFailure("Found undesired element " + xpath);
			} else {
				logger.info("Not found undesired element " + xpath);
			}
		}
	}

	public boolean isTextPresent(String text) {
		boolean isPresent = browser.isTextPresent(text);
		if (isPresent) {
			logger.info("Found text " + text);
			return true;
		} else {
			logger.info("Not found text " + text);
			return false;
		}
	}

	public boolean isElementPresent(String xpath) {
		boolean isPresent = browser.isElementPresent(xpath) && browserCore.findElementByXPath(xpath).isDisplayed();
		if (isPresent) {
			logger.info("Found element " + xpath);
			return true;
		} else {
			logger.info("Not found element" + xpath);
			return false;
		}
	}
	
	/**
	 * Pause
	 * @param time in millisecond
	 */
	private void pause(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	private void handleFailure(String notice) {
		String png = LogTools.screenShot(this);
		String log = notice + " >> capture screenshot at " + png;
		logger.error(log);
		Assert.fail(log);
	}
}
