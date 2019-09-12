package com.lazerycode.selenium.page_objects;

import static com.lazerycode.selenium.util.AssignDriver.initQueryObjects;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.openqa.selenium.By.ByCssSelector;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.lazerycode.selenium.DriverBase;
import com.lazerycode.selenium.util.Query;
import com.titu.Row;

public class PortfolioPage extends AbstractPage {

    private Query position = new Query().defaultLocator(ByCssSelector.cssSelector("a[href*=\"options\"]"));
    private Query marketValue = new Query().defaultLocator(ByCssSelector.cssSelector("div.grid-2 > div:nth-child(1) > header > h2"));
    private Query expiration = new Query().defaultLocator(ByCssSelector.cssSelector("div.grid-2 > div:nth-child(2) > header > h2"));
    private Query quantity = new Query().defaultLocator(ByCssSelector.cssSelector("div.grid-2 > div:nth-child(2) > table > tbody > tr:nth-child(1) > td:nth-child(3)"));
    private Query costOrCredit = new Query().defaultLocator(ByCssSelector.cssSelector("div.grid-2 > div:nth-child(1) > table > tbody > tr:nth-child(1) > td:nth-child(3)"));
    private Query symbol = new Query().defaultLocator(ByCssSelector.cssSelector("h1 > a"));
    private Query nextEarningsDate = new Query().defaultLocator(ByCssSelector.cssSelector("div.col-12 > div:nth-child(2) > div > div:nth-child(2)"));
    
    private List<Row> rows = new ArrayList<>();
    
    public PortfolioPage() throws Exception {
        initQueryObjects(this, DriverBase.getDriver());
    }

    /**
     * @param begin This parameter defines how many h4 elements to skip for performance improvement as this method is called recursively
     * @return
     * @throws Exception
     */
    public List<Row> getPositions() throws Exception {
    	
    	RemoteWebDriver driver = DriverBase.getDriver();
    	
    	// Wait for the page to load and 2FA, timeout after 100 seconds
    	DriverBase.tryOnTimeout(driver, "h4", "Portfolio | Robinhood", true, 3);
    	
        System.out.println("Page title is: " + driver.getTitle());
    	
    	List<WebElement> elements = position.findWebElements();
    	for (WebElement e : elements) {
    		
    		String url = e.getAttribute("href");
    		String position = e.findElement(ByCssSelector.cssSelector("h4")).getText();
    		if (Pattern.compile("call|put", Pattern.CASE_INSENSITIVE).matcher(position).find()
    				//ignoring symbols like HDB1
    				&& !Pattern.compile("[A-Z]+\\d").matcher(position).find()) {
	    			Row row = new Row(position, url);
	    			rows.add(row);
	    			System.out.println(rows.size() + ". " + row.getDescription());
    		}
		}
    	
    	System.out.println("Total rows: " + rows.size());
    	
    	int rowCount = 0;
    	for (Row r : rows) {
    		long start = System.currentTimeMillis();
			populateRow(r);
			long end = System.currentTimeMillis();
	        System.out.println("time taken to populateRow: " + (end - start) + " ms. row count:" + (++rowCount));
		}
    	
        return rows;
    }
    
    private void populateRow(Row r) throws Exception {
    	
    	RemoteWebDriver driver = DriverBase.getDriver();
    	driver.navigate().to(r.url);
    	DriverBase.tryOnTimeout(driver, "div.grid-2", r.symbol, true, 3);
        
        System.out.println("Page title is: " + driver.getTitle());
    	
    	//wait for one second if there is no price
    	String marketValueText = waitForSeconds(marketValue, 1);
    	r.marketValue = Float.valueOf(marketValueText.replaceAll("(\\$|,)", ""));
    	
    	String expDate = expiration.findWebElement().getText();
    	String[] dateParts = expDate.split("/");
    	String month = dateParts[0].length() == 1 ? "0" + dateParts[0] : dateParts[0];
    	String day = dateParts[1].length() == 1 ? "0" + dateParts[1] : dateParts[1];
    	int year = Calendar.getInstance().get(Calendar.YEAR);
    	expDate = year + "-" + month + "-" + day;
    	r.expirationDate = expDate;
    	
    	String qty = quantity.findWebElement().getText();
    	r.quantity = Math.abs(Integer.valueOf(qty));
    	
    	String strCredit = costOrCredit.findWebElement().getText();
    	Float costOrCredit = Float.valueOf(strCredit.replaceAll("(\\$|,)", ""));
    	if (r.isSpread) {
    		r.credit = costOrCredit;
    	} else {
    		r.cost = costOrCredit;
    	}
    	
    	symbol.findWebElement().click();
    	//driver.get("https://robinhood.com/stocks/" + r.symbol);
    	
    	DriverBase.tryOnTimeout(driver, "div.col-12", r.symbol + " - $", true, 3);
    	r.currentStockPrice = parseStockPrice(driver);
    	
    	r.setNextEarningsDate(parseNextEarningsDate(driver));
        
        System.out.println("Page title is: " + driver.getTitle());
        
        System.out.println(r);
        
    }
    
    private Float parseStockPrice(RemoteWebDriver driver) {
    	Pattern pattern = Pattern.compile("\\$[\\d|\\.|,]+");
        Matcher matcher = pattern.matcher(driver.getTitle());
        if(matcher.find()) {
        	return Float.valueOf(driver.getTitle().substring(matcher.start(), matcher.end())
        							.replaceAll("(\\$|,)", ""));
        }
        
        return null;
    }
    
    private String parseNextEarningsDate(RemoteWebDriver driver) {
    	
    	//Available Sep 12, After Hours or Expected Oct 29, After Hours
    	String text;
		try {
			text = nextEarningsDate.findWebElement().getText();
		} catch (org.openqa.selenium.NoSuchElementException e) {
			return null;
		}
		
    	String patternString = "[A-Z][a-z]{2} \\d(\\d)?";
        
        Pattern pattern = Pattern.compile(patternString);
        Matcher matcher = pattern.matcher(text);

        if(matcher.find()) {
            return text.substring(matcher.start(), matcher.end());
        }
    	
        return null;
    }
    
	private String waitForSeconds(Query q, int seconds) throws Exception {
		WebElement e = q.findWebElement();
		String text = e.getText();
		int counter = 0;
    	while (!"".equals(text) && counter < seconds*10) {
    		Thread.sleep(100);
    		text = q.findWebElement().getText();
    		++counter;
    	}
    	return text;
	}
	
}