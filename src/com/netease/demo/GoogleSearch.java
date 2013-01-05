package com.netease.demo;

import com.netease.dagger.BrowserEmulator;

public class GoogleSearch {

	public static void main(String[] args) {

		String googleUrl = "http://www.google.com";
		String searchBox = "//input[@name='q']";
		String searchBtn = "//input[@name='btnK']";
		BrowserEmulator be = new BrowserEmulator();

		be.open(googleUrl);
		be.type(searchBox, "github");
		be.click(searchBtn);
		be.expectTextExistOrNot(true, "https://github.com/", 5000);
		be.quit();
	}
}
