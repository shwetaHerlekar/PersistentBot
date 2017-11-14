package com.example;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.logging.Logger;

import org.json.simple.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ai.api.model.AIEvent;



import ai.api.model.AIOutputContext;
import ai.api.model.Fulfillment;
import ai.api.web.AIWebhookServlet;

// [START example]
@SuppressWarnings("serial")
public class MyWebhookServlet extends AIWebhookServlet {
	private static final Logger log = Logger.getLogger(MyWebhookServlet.class.getName());

@Override
protected void doWebhook(AIWebhookRequest input, Fulfillment output) {

	log.info("webhook call");
	String action = input.getResult().getAction();
	HashMap<String, JsonElement> parameter = input.getResult().getParameters();
	//add constants in file
	try{
	switch (action) {
	case "QUERY_LEAVE":
		log.info("in action : query_leave"  );
		output = queryLeave(output, parameter);
		break;
	case "SYSTEM_SUGESTION_SATISFIED_YES" :
		log.info(" intent SYSTEM_SUGESTION_SATISFIED_YES ");
		output = submitFeilds(output, parameter);
		break;
	case "SYSTEM_SUGESTION_SATISFIED_NO" : 
		log.info("intent : SYSTEM_SUGESTION_SATISFIED_NO");
		output = fallbackCustomApply(output,parameter);
		break;
	case "APPLY_LEAVE_CUSTOM" :
		log.info("intent APPLY_LEAVE_CUSTOM");
		output = submitFeilds(output, parameter);
		break;
	case "CONFIRM_LEAVE_YES" :
		log.info("intent CONFIRM_LEAVE_YES");
		output = confirmLeave(output, parameter);
		break;
	case "CONFIRM_LEAVE_NO" :
		log.info("CONFIRM_LEAVE_NO");
	//	output = 
		break;
	case "RESTART" :
		log.info("intent : restart");
		output = fallbackCustomApply(output,parameter);
		break;
	case "EXIT" :
		log.info("exit");
		output = exitFlow(output);
		break;
	case "input.welcome" :
		log.info("input.welcome");
		output = eventTriggered(output);
		break;
	default:
		 output.setSpeech("Default case");
		break;
	}
	}catch(Exception e){
		log.info("exception : " + e);
	}
	// output.setSpeech(input.getResult().toString());

//output.setSpeech("from webhook");	
}

private Fulfillment eventTriggered(Fulfillment output) {
	/* with all params except event go to custom leave apply
 * 
 * 	
 */
	log.info("event trig fun");
	Map<String,String> outParameter = new HashMap<>();
	AIEvent followupEvent = new AIEvent("event_triggered");
	String message = "Wanna do it yourself?  Okay! I would not give my suggestion, just let me know the details. I will apply for you." ;

	log.info("rerouting to event : evt trg");
	output.setFollowupEvent(followupEvent);
	/*output.setSpeech(message);
	output.setDisplayText(message);*/
	return output;
}

private Fulfillment queryLeave(Fulfillment output, HashMap<String, JsonElement> parameter) throws ParseException {
	// TODO Auto-generated method stub
	log.info("inside queryLeave");
	HashMap<String, Integer> holidayData = new HashMap<>( Data.getHolidays());
	log.info("holiday "+ holidayData.toString());
	AIOutputContext contextOut = new AIOutputContext();
	String message ="";
	int balance = holidayData.get("leave_balance");
	log.info("bal :"+balance);
	int days = 0;
	HashMap<String, JsonElement> outParameters = new HashMap<String, JsonElement>();
	
	if (parameter.containsKey("noOfDays") && !parameter.get("noOfDays").equals("") ){
		log.info("contains no of days");
		//days = Integer.parseInt(parameter.get("noOfDays"));
		JsonElement contextOutParameter;
		contextOutParameter = new JsonPrimitive(days);
		outParameters.put("noOfDays", contextOutParameter);
	}
	if (parameter.containsKey("startDate") && parameter.containsKey("endDate")) {
		if (!parameter.get("startDate").equals("")) {
			log.info("start date");
			JsonElement startDate = new JsonPrimitive(parameter.get("startDate").toString());
			outParameters.put("startDate", startDate);
			
		}
		if (!parameter.get("endDate").equals("")) {
			log.info("endDate");
			JsonElement endDate = new JsonPrimitive(parameter.get("endDate").toString());
			outParameters.put("endDate", endDate);
		}
		if (!parameter.get("endDate").equals("") && !parameter.get("startDate").equals("")) {
			days =  getDays(parameter.get("startDate").toString(), parameter.get("endDate").toString());
			JsonElement noOfDay = new JsonPrimitive(days);// fetched no of days
			outParameters.put("noOfDays", noOfDay);
			//fetch no of days
		}
		
	}
	if (parameter.containsKey("event") && !parameter.get("event").equals("")) {
		JsonElement contextOutParameter;
		contextOutParameter = new JsonPrimitive(parameter.get("event").equals(""));
		outParameters.put("event", contextOutParameter);
	}
	if (balance <= 0) {
		message = "Sorry dear, you have insufficient leave balance, you will need DP approval If want to apply for leave." ;
		contextOut.setLifespan(2);
		contextOut.setName("InsufficientBalance");
		contextOut.setParameters(outParameters);
		output.setContextOut(contextOut);
		log.info("insufficent balance");
	}
	else if (balance < 3 && days < 3) {
		message = "Your leave balance is low. You are having only " + balance + ". Do you still want to apply for leave ?" ;
		List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
		AIOutputContext contextOut1 = new AIOutputContext();
		contextOut1.setLifespan(2);
		contextOut1.setName("SystemSugestionSatisfied-Yes");
		contextOut1.setParameters(outParameters);
		contextOutList.add(contextOut1);
		AIOutputContext contextOut2 = new AIOutputContext();
		contextOut2.setLifespan(2);
		contextOut2.setName("SystemSugestionSatisfied-no");
		contextOut2.setParameters(outParameters);
		contextOutList.add(contextOut2);
		log.info("Context out parameters : if low balance");
		output.setContextOut(contextOutList);
	
	}
	else if(balance < days){
		message = "Your leave balance is less than :" + days +". You will need Delivery partner approval if you will apply. Still wanna apply? Or dear you can apply for "+days+ " days.";
		List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
		AIOutputContext contextOut1 = new AIOutputContext();
		contextOut1.setLifespan(2);
		contextOut1.setName("SystemSugestionSatisfied-Yes");
		contextOut1.setParameters(outParameters);
		contextOutList.add(contextOut1);
		AIOutputContext contextOut2 = new AIOutputContext();
		contextOut2.setLifespan(2);
		contextOut2.setName("SystemSugestionSatisfied-no");
		contextOut2.setParameters(outParameters);
		contextOutList.add(contextOut2);
		log.info("Context out parameters : if low balance");
		output.setContextOut(contextOutList);
		log.info("balance < req days");

	}
	else{
		//api call to check for event
		//String msg = Suggest(new Date());
		message = "Hurry you have " + balance + " leaves remaining and ----------" ; 
		contextOut.setLifespan(3);
		contextOut.setName("proceed");
		contextOut.setParameters(outParameters);
		output.setContextOut(contextOut);

	}
	output.setDisplayText(message);
	output.setSpeech(message);
	return output;
}

private Fulfillment submitFeilds(Fulfillment output, HashMap<String, JsonElement> parameter) {
	log.info("submit feilds");
	String message = "";
	String event = "";
	String comment = "";
	HashMap<String, JsonElement> outParameter = parameter;
	if (!parameter.get("event").equals("")) {
		event = parameter.get("event").getAsString();
		comment = "Leave for "+event;
		log.info("comment : "+comment);
	}
	if (!parameter.get("comment").equals("")) {
		comment = parameter.get("comment").getAsString();
	}
		message = "You want to apply for leave from " + parameter.get("startDate").getAsString() + " to " + parameter.get("endDate").getAsString() + comment;
		outParameter.put("comment",new JsonPrimitive(comment) );
	
	message += " \n please confirm ";
	log.info("message");
	output.setSpeech(message);
	output.setDisplayText(message);
	log.info("setting context");
	List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
	AIOutputContext contextOut1 = new AIOutputContext();
	contextOut1.setLifespan(2);
	contextOut1.setName("confirmLeave - yes");
	contextOut1.setParameters(outParameter);
	contextOutList.add(contextOut1);
	AIOutputContext contextOut2 = new AIOutputContext();
	contextOut2.setLifespan(2);
	contextOut2.setName("confirmLeave - no");
	contextOut2.setParameters(outParameter);
	contextOutList.add(contextOut2);
	log.info("Context out parameters set");
	output.setContextOut(contextOutList);
	return output;
}



private Fulfillment exitFlow(Fulfillment output) {
	
	AIOutputContext contextOut = new AIOutputContext();
	output.setContextOut(contextOut); // context reset to ""
	output.setDisplayText("Okay! no issues.");
	return output;
}

@SuppressWarnings("unchecked")
private Fulfillment confirmLeave(Fulfillment output, HashMap<String, JsonElement> parameter) {

	log.info("in confirm leave");
	HashMap<String, JsonElement> outParameters = parameter;
	String message = "";
	AIOutputContext contextOut = new AIOutputContext();
	output.setContextOut(contextOut); // context reset to ""
	HashMap<String, Integer> holidayData = new HashMap<>( Data.getHolidays());
	int leaveBalance = (int) holidayData.get("leave_balance");
	log.info("leave balance : " +leaveBalance );
	int days = getDays(parameter.get("startDate").getAsString(), parameter.get("endDate").getAsString());
	if (leaveBalance < days) {
		message = "Your leave balance is less than :" + days +". You will need Delivery partner approval for applying. Still wanna apply? Or dear you can apply for "+days+ " days or less.";
		List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
		AIOutputContext contextOut1 = new AIOutputContext();
		contextOut1.setLifespan(2);
		contextOut1.setName("SystemSugestionSatisfied-Yes");
		contextOut1.setParameters(outParameters);
		contextOutList.add(contextOut1);
		AIOutputContext contextOut2 = new AIOutputContext();
		contextOut2.setLifespan(2);
		contextOut2.setName("SystemSugestionSatisfied-no");
		contextOut2.setParameters(outParameters);
		contextOutList.add(contextOut2);
		log.info("Context out parameters : if low balance : while confirmation");
		output.setContextOut(contextOutList);
	
	}
	else{
		message = "Yeah! your leave has been applied :) ";
		holidayData.put("leave_balane", leaveBalance - 1);
		
	}
	
	output.setDisplayText(message);
	output.setSpeech(message);
	return output;
}

private Fulfillment fallbackCustomApply(Fulfillment output, HashMap<String, JsonElement> parameter) {
	/* with all params except event go to custom leave apply
 * 
 * 	
 */
	log.info("fallback custom apply");
	Map<String,String> outParameter = new HashMap<>();
	AIEvent followupEvent = new AIEvent("APPLY_LEAVE_CUSTOM");
	String message = "Wanna do it yourself?  Okay! I would not give my suggestion, just let me know the details. I will apply for you." ;
	if (parameter.containsKey("startDate") ){
		outParameter.put("startDate", parameter.get("startDate").getAsString());
	} if( parameter.containsKey("endDate") ) {
		outParameter.put("endDate", parameter.get("endDate").getAsString());
	}
	log.info("rerouting to event : APPLY_LEAVE_CUSTOM");
	followupEvent.setData(outParameter);
	output.setFollowupEvent(followupEvent);
	output.setSpeech(message);
	output.setDisplayText(message);
	return output;
}


 private String Suggest(HashMap<String, JsonElement> parameter) throws ParseException {
	//function to return if no inputs near by holiday/birthday, a mesage to ask if leave is for that
	//if all fields present redirect to confirm
	// if event present redirect to SYSTEM_SUGESTION_SATISFIED_YES
	//if start date & no of days, calc end date
	//
	JSONObject holidayData = Data.getHolidays();
	String bday = holidayData.get("birthday").toString();
	Date birthday = new SimpleDateFormat("dd/MM/yyyy").parse(bday);  
	String msg = "";
	
	if(isEventWithinRange(birthday))
	{
		msg = "Your birthday is coming on "+birthday+". Want to go out??";
		return msg;
	}
	else 
	{
		JSONObject holidays = (JSONObject) holidayData.get("holidays");
		for(Iterator iterator = holidays.keySet().iterator(); iterator.hasNext();) {
		    String key = (String) iterator.next();
		    Date date1 = new SimpleDateFormat("dd/MM/yyyy").parse(key);  
		    if(isEventWithinRange(date1))
		    {
		    	msg = holidays.get(key).toString()+" is coming. Do you want to take apply leave for"+holidays.get(key).toString();
		    	return msg;
		    }
		}
	}
	return msg;
}
private int getDays(String startDate , String endDate) {
	
	// TODO Auto-generated method stub
int days = 0;

return 0;
}
	public static boolean isEventWithinRange(Date testDate) throws ParseException {  
		String event_date="11/14/2017";
		Date today = new SimpleDateFormat("dd/MM/yyyy").parse(event_date);  
		event_date="31/01/2018";
		Date last = new SimpleDateFormat("dd/MM/yyyy").parse(event_date);  
		return testDate.before(today) && last.after(testDate);
	}

}
