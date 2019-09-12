package com.titu;

import java.util.Calendar;

public class Util {
	
	public static Calendar getTodayWithoutTime() {
		Calendar now = Calendar.getInstance();
		now.set(Calendar.HOUR_OF_DAY, 0);
		now.set(Calendar.MINUTE, 0);
		now.set(Calendar.SECOND, 0);
		now.set(Calendar.MILLISECOND, 0);
		
		return now;
	}

}
