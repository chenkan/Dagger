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
	JavascriptExecutor javaScriptExecutor;
	
	private static Logger logger = Logger.getLogger(BrowserEmulator.class.getName());

	public BrowserEmulator() {
		chooseBrowserCoreType(GlobalSettings.BrowserCoreType);
		browser = new WebDriverBackedSelenium(browserCore, "www.163.com");
		javaScriptExecutor = (JavascriptExecutor) browserCore;
		logger.info("Browser started :)");
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
	 * Switch window/tab
	 * @param windowTitle
	 */
	public void selectWindow(String windowTitle) {
		pause();
		browser.selectWindow(windowTitle);
		logger.info("Switch to window " + windowTitle);
	}

	private void pause() {
		int stepInterval = Integer.parseInt(GlobalSettings.StepInterval);
		if (stepInterval > 0) {
			try {
				Thread.sleep(stepInterval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} else {
			return;
		}
	}

	private void waitForElementPresent(final String xpath) {
		int timeout = Integer.parseInt(GlobalSettings.Timeout);
		waitForElementPresent(xpath, timeout);
	}
	
	private void waitForElementPresent(final String xpath, int timeout) {
		try {
			new Wait() {
				public boolean until() {
					return isElementPresent(browserCore, xpath);
				}
			}.wait("Can't find element " + xpath, timeout);
		} catch (Exception e) {
			e.printStackTrace();
			handleFailIssue("Can't find element " + xpath);
		}
		logger.info("Found element " + xpath);
	}

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
						return isTextPresent(browserCore, text);
					}
				}.wait("Can't find text " + text, timeout);
			} catch (Exception e) {
				e.printStackTrace();
				handleFailIssue("Can't find text " + text);
			}
			logger.info("Found text " + text);
		}

		else {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (isTextPresent(browserCore, text)) {
				handleFailIssue("Found text " + text);
			} else {
				logger.info("Can't find text " + text);
			}
		}
	}

	/**
	 * Expect element exist or not on the page
	 * @param expectExist
	 * @param xpath
	 * @param timeout in millisecond
	 */
	public void expectElementExistOrNot(boolean expectExist, final String xpath, int timeout) {
		if (expectExist) {
			waitForElementPresent(xpath, timeout);
			try {
				new Wait() {
					public boolean until() {
						return browserCore.findElementByXPath(xpath).isDisplayed();
					}
				}.wait("Can't find element " + xpath, timeout);
			} catch (Exception e) {
				e.printStackTrace();
				handleFailIssue("Can't find element " + xpath);
			}
			logger.info("Found element " + xpath);
		}

		else {
			try {
				Thread.sleep(timeout);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (browserCore.findElementByXPath(xpath).isDisplayed()) {
				handleFailIssue("Found element " + xpath);
			} else {
				logger.info("Can't find element " + xpath);
			}
		}
	}

	public boolean isTextPresent(String text) {
		if (isTextPresent(browserCore, text)) {
			logger.info("Found text " + text);
		} else {
			logger.info("Can't find text " + text);
		}
		return isTextPresent(browserCore, text);
	}

	public boolean isElementPresent(String xpath) {
		if (browserCore.findElementByXPath(xpath).isDisplayed()) {
			logger.info("Found element " + xpath);
		} else {
			logger.info("Can't find element" + xpath);
		}
		return browserCore.findElementByXPath(xpath).isDisplayed();
	}

	/**
	 * Copy from Selenium source code 
	 * - WebDriverCommandProcessor.java
	 * - IsElementPresent.java
	 * @param driver
	 * @param xpath
	 * @return
	 */
	private boolean isElementPresent(RemoteWebDriver driver, String xpath) {
		try {
			JavascriptLibrary javascriptLibrary = new JavascriptLibrary();
			ElementFinder finder = new ElementFinder(javascriptLibrary);
			finder.findElement(driver, xpath);
			return true;
		} catch (SeleniumException e) {
			return false;
		} catch (RuntimeException e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Copy from Selenium source code
	 * - WebDriverCommandProcessor.java
	 * - IsTextPresent.java
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

	public void refresh() {
		browserCore.navigate().refresh();
		logger.info("Refreshed");
	}

	public void enterFrame(String xpath) {
		pause();
		browserCore.switchTo().frame(browserCore.findElementByXPath(xpath));
		logger.info("Enter iframe " + xpath);
	}

	public void leaveFrame() {
		pause();
		browserCore.switchTo().defaultContent();
		logger.info("Back top iframe");
	}
	
	private void handleFailIssue(String notice) {
		String png = LogTools.screenShot(this);
		String log = notice + " screenshot at " + png;
		logger.error(log);
		Assert.fail(log);
	}
}
