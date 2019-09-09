package com.lazerycode.selenium.page_objects;

import com.lazerycode.selenium.util.Query;

public abstract class AbstractPage {
	
	public void sendKeys(Query q, String term) {
		q.findWebElement().clear();
    	q.findWebElement().sendKeys(term);
	}

}
