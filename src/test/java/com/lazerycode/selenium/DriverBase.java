package com.lazerycode.selenium;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import com.lazerycode.selenium.config.DriverFactory;
import com.lazerycode.selenium.listeners.ScreenshotListener;

@Listeners(ScreenshotListener.class)
public class DriverBase {

    private static List<DriverFactory> webDriverThreadPool = Collections.synchronizedList(new ArrayList<DriverFactory>());
    private static ThreadLocal<DriverFactory> driverFactoryThread;

    @BeforeSuite(alwaysRun = true)
    public static void instantiateDriverObject() {
        driverFactoryThread = ThreadLocal.withInitial(() -> {
            DriverFactory driverFactory = new DriverFactory();
            webDriverThreadPool.add(driverFactory);
            return driverFactory;
        });
    }

    public static RemoteWebDriver getDriver() throws Exception {
        return driverFactoryThread.get().getDriver();
    }

    @AfterMethod(alwaysRun = true)
    public static void clearCookies() {
        try {
            driverFactoryThread.get().getStoredDriver().manage().deleteAllCookies();
        } catch (Exception ignored) {
            System.out.println("Unable to clear cookies, driver object is not viable...");
        }
    }

    @AfterSuite(alwaysRun = true)
    public static void closeDriverObjects() {
        for (DriverFactory driverFactory : webDriverThreadPool) {
            driverFactory.quitDriver();
        }
    }
    
    public static void waitForLoad(WebDriver driver) {
        ExpectedCondition<Boolean> pageLoadCondition = new
                ExpectedCondition<Boolean>() {
                    public Boolean apply(WebDriver driver) {
                        return ((JavascriptExecutor)driver).executeScript("return document.readyState").equals("complete");
                    }
                };
        WebDriverWait wait = new WebDriverWait(driver, 30);
        wait.until(pageLoadCondition);
    }
    
    public ExpectedCondition<Boolean> exists(final String cssSelector, String titleText) {
        return driver -> (driver.findElements(ByCssSelector.cssSelector(cssSelector)).size() > 0
        				&& driver.getTitle().toLowerCase().startsWith(titleText.toLowerCase()));
    }
    
    public ExpectedCondition<Boolean> pageTitleStartsWith(final String searchString) {
        return driver -> driver.getTitle().toLowerCase().startsWith(searchString.toLowerCase());
    }
    
    public ExpectedCondition<Boolean> exists(final String cssSelector) {
        return driver -> driver.findElements(ByCssSelector.cssSelector(cssSelector)).size() > 0;
    }
    
    public static ExpectedCondition<Boolean> existsCss(final String cssSelector) {
        return driver -> driver.findElements(ByCssSelector.cssSelector(cssSelector)).size() > 0;
    }
    
    public static ExpectedCondition<Boolean> existsCssAndTitle(final String cssSelector, String titleText) {
        return driver -> (driver.findElements(ByCssSelector.cssSelector(cssSelector)).size() > 0
        				&& driver.getTitle().toLowerCase().startsWith(titleText.toLowerCase()));
    }
    
    public static ExpectedCondition<Boolean> checkTitleStart(final String titleText) {
    	return driver -> driver.getTitle().toLowerCase().startsWith(titleText.toLowerCase());
    }
    
    public static void tryOnTimeout(RemoteWebDriver driver, String css, String title, boolean exists, int times) {
		try {
			WebDriverWait wait = new WebDriverWait(driver, 100, 100);
			if (exists) {
				wait.until(DriverBase.existsCssAndTitle(css, title));
			} else {
				wait.until(DriverBase.checkTitleStart(title));
			}
		} catch (org.openqa.selenium.TimeoutException e) {
			System.out.println(e + " Continuing...");
			driver.navigate().to(driver.getCurrentUrl());
			if (times == 0) return;
			tryOnTimeout(driver, css, title, exists, --times);
		}
	}
}