package com.example;

import java.util.Date;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class Data {

	@SuppressWarnings({ "unused", "deprecation", "unchecked" })
	public static JSONObject getHolidays() {
		// TODO Auto-generated method stub
		JSONObject responseObject = new JSONObject();
		
		Date event_date = new Date(2017, 11, 21);
		responseObject.put(event_date.toString(), "birthday");
		
		JSONArray holidays = new JSONArray();
		
		JSONObject christmas = new JSONObject();
		event_date = new Date(2017, 12, 25);
		christmas.put(event_date.toString(), "christmas");
		holidays.add(christmas);
		
		JSONObject new_year = new JSONObject();
		event_date = new Date(2017, 12, 31);
		new_year.put(event_date.toString(), "newyear");
		holidays.add(christmas);
		
		responseObject.put("holidays", holidays);
		return responseObject;
	}

}
