package com.lazerycode.selenium.tests;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import com.lazerycode.selenium.DriverBase;
import com.lazerycode.selenium.page_objects.LoginPage;
import com.lazerycode.selenium.page_objects.PortfolioPage;
import com.titu.Row;
import com.titu.WriteCSVFile;

//@Ignore
public class RobinhoodIT extends DriverBase {
	
	Properties login = new Properties();
	
	@BeforeTest
	public void setup() {
		
		Class<?> aClass = RobinhoodIT.class;

		InputStream inputStream =
		    aClass.getResourceAsStream("/login.properties");

		try {
			login.load(inputStream);
		} catch (IOException e) {
		    e.printStackTrace();
		}
	}

    @Test
    public void login() throws Exception {
        // Create a new WebDriver instance
        // Notice that the remainder of the code relies on the interface,
        // not the implementation.
        WebDriver driver = getDriver();

        driver.get("https://robinhood.com/login");

        LoginPage loginPage = new LoginPage();

        // Check the title of the page
        System.out.println("Page title is: " + driver.getTitle());
        
        login.get("rh.username");

        loginPage.enterUsername(login.get("rh.name").toString())
        		.enterPassword(login.get("rh.pzwd").toString())
                .signIn();

        try {
			getPositions();
		} catch (Exception e) {
			e.printStackTrace();
			throw e;
		}
    }
    
    public void getPositions() throws Exception{
    	
        PortfolioPage pfPage = new PortfolioPage();
        List<Row> rows = pfPage.getPositions(0);

        System.out.println("Positions: " + rows);
        new WriteCSVFile(rows).write();
    	
    }
    
}