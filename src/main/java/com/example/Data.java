package com.example;

import java.util.Calendar;
import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Data {

	@SuppressWarnings({ "unused", "deprecation", "unchecked" })
	public static JSONObject getHolidays() {
		// TODO Auto-generated method stub
		int leaveBalance = 4;
		
		JSONObject responseObject = new JSONObject();
		
		Calendar bday = Calendar.getInstance();
		bday.set(2017, 11, 21);
		responseObject.put("birthday", bday.toString());
		
		JSONObject holidays = new JSONObject();
		Calendar event_date= Calendar.getInstance();
		event_date.set(2017, 12, 25);
		holidays.put(event_date.toString(), "christmas");
		

		event_date= Calendar.getInstance();
		event_date.set(2017, 12, 31);
		holidays.put(event_date.toString(), "newyear");
		
		responseObject.put("holidays", holidays);
		responseObject.put("leave_balance", leaveBalance);
		return responseObject;
	}

}
