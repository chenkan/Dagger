package com.netease.demo;

import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import com.netease.dagger.BrowserEmulator;

public class TestNg {

	String googleUrl = "http://www.google.com";
	String searchBox = "//input[@name='q']";
	String searchBtn = "//input[@name='btnK']";
	BrowserEmulator be;

	@BeforeClass
	public void doBeforeClass() throws Exception {
		be = new BrowserEmulator();
	}

	@Test(dataProvider = "data")
	public void doTest(String keyword, String result) {
		be.open(googleUrl);
		be.type(searchBox, keyword);
		be.click(searchBtn);
		be.expectTextExistOrNot(true, result, 5000);
	}

	@AfterClass(alwaysRun = true)
	public void doAfterClass() {
		be.quit();
	}

	@DataProvider(name = "data")
	public Object[][] data() {
		return new Object[][] { 
				{ "java", "www.java.com" }, 
				{ "github", "https://github.com/" }, 
			};
	}
}