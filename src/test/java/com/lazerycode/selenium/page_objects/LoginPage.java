package com.lazerycode.selenium.page_objects;

import static com.lazerycode.selenium.util.AssignDriver.initQueryObjects;

import org.openqa.selenium.By;
import org.openqa.selenium.By.ByCssSelector;

import com.lazerycode.selenium.DriverBase;
import com.lazerycode.selenium.util.Query;

public class LoginPage extends AbstractPage {

    private Query username = new Query().defaultLocator(By.name("username"));
    private Query password = new Query().defaultLocator(By.name("password"));
    private Query signIn = new Query().defaultLocator(ByCssSelector.cssSelector("button[type=submit]"));

    public LoginPage() throws Exception {
        initQueryObjects(this, DriverBase.getDriver());
    }

    public LoginPage enterUsername(String searchTerm) {
    	sendKeys(username, searchTerm);

        return this;
    }

    public LoginPage enterPassword(String pwd) {
    	sendKeys(password, pwd);

        return this;
    }
    
    public LoginPage signIn() {
    	signIn.findWebElement().submit();

        return this;
    }

}