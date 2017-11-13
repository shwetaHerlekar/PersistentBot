package com.example;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;

import ai.api.model.AIOutputContext;
import ai.api.model.Fulfillment;
import ai.api.web.AIWebhookServlet;
import java.util.Calendar;

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
		output = checkBalance(output, parameter);
		break;
	/*case "PROCEED" :
		log.info("intent PROCEED ");
		output = suggest(output, parameter);*/
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
	case "RESTART" :
		log.info("intent : restart");
		output = fallbackCustomApply(output,parameter);
		break;
	case "EXIT" :
		log.info("exit");
		output = exitFlow(output);
		break;
	default:
		break;
	}
	}catch(Exception e){
		log.info("exception : " + e);
	}
	// output.setSpeech(input.getResult().toString());

	
}

private Fulfillment checkBalance(Fulfillment output, HashMap<String, JsonElement> parameter) {
	// TODO Auto-generated method stub
	HashMap<String, Integer> holidayData = new HashMap<>( Data.getHolidays());
	AIOutputContext contextOut = new AIOutputContext();
	String message ="";
	int balance = holidayData.get("leaveBalance");
	int days = 0;
	HashMap<String, JsonElement> outParameters = new HashMap<String, JsonElement>();
	
	if (parameter.containsKey("noOfDays")) {
		days = Integer.parseInt(parameter.get("noOfDays").toString());
		JsonElement contextOutParameter;
		contextOutParameter = new JsonPrimitive(days);
		outParameters.put("noOfDays", contextOutParameter);
	}
	if (parameter.containsKey("startDate") && parameter.containsKey("endDate")) {
		if (parameter.get("startDate") != null) {
			JsonElement startDate = new JsonPrimitive(parameter.get("startDate").toString());
			outParameters.put("startDate", startDate);
			
		}
		if (parameter.get("endDate").toString() != null) {
			JsonElement endDate = new JsonPrimitive(parameter.get("endDate").toString());
			outParameters.put("endDate", endDate);
		}
		if (parameter.get("endDate").toString() != null && parameter.get("startDate") != null) {
			days =  getDays(parameter.get("startdate").toString(), parameter.get("endDate").toString());
			JsonElement noOfDay = new JsonPrimitive(days);// fetched no of days
			outParameters.put("noOfDays", noOfDay);
			//fetch no of days
		}
		
	}
	if (parameter.containsKey("event")) {
		JsonElement contextOutParameter;
		contextOutParameter = new JsonPrimitive(parameter.get("event").toString());
		outParameters.put("event", contextOutParameter);
	}
	if (balance <= 0) {
		message = "You don't have sufficent leaves. You will need approval from your delivery partner, if you want to apply for leave" ;
		
	}
	else if (balance < 3 && days < 3) {
		message = "Your leave balance is low. You are having only " + balance + ". Do you still want to apply for leave ?" ;
		contextOut.setLifespan(2);
		contextOut.setName("proceed");
		contextOut.setParameters(outParameters);
	}
	else if(balance < days){
		message = "Your leave balance is less than :" + days +". You will need Delivery partner approval if you will apply. Still wanna apply? Or dear you can apply for "+days+ " days.";
		contextOut.setLifespan(3);
		contextOut.setName("proceed");
		contextOut.setParameters(outParameters);
	}
	else{
		//api call to check for event
		message = "Hurry you have " + days + "leaves remaining and ----------" ; 
				contextOut.setLifespan(3);
		contextOut.setName("proceed");
		contextOut.setParameters(outParameters);
	}
	output.setDisplayText(message);
	output.setSpeech(message);
	output.setContextOut(contextOut);
	return output;
}

private Fulfillment exitFlow(Fulfillment output) {
	
	AIOutputContext contextOut = new AIOutputContext();
	output.setContextOut(contextOut); // context reset to null
	output.setDisplayText("Okay! no issues.");
	return output;
}

@SuppressWarnings("unchecked")
private Fulfillment confirmLeave(Fulfillment output, HashMap<String, JsonElement> parameter) {
/*
 * 	leave confirmed, deduct from balance
 * reset context
 */
	HashMap<String, JsonElement> outParameters = new HashMap<String, JsonElement>();
	String message = "";
	AIOutputContext contextOut = new AIOutputContext();
	output.setContextOut(contextOut); // context reset to null
	HashMap<String, Integer> holidayData = new HashMap<>( Data.getHolidays());
	int leaveBalance = (int) holidayData.get("leaveBalance");
	int days = getDays(parameter.get("startdate").toString(), parameter.get("endDate").toString());
	if (leaveBalance < days) {
		message = "Your leave balance is less than :" + days +". You will need Delivery partner approval for applying. Still wanna apply? Or dear you can apply for "+days+ " days.";
		contextOut.setLifespan(2);
		contextOut.setName("proceed");
		contextOut.setParameters(outParameters);
	}
	else{
		message = "Yeah! your leave has been applied :) ";
	}
	holidayData.put("leaveBalane", leaveBalance - 1);
	output.setDisplayText(message);
	output.setSpeech(message);
	return output;
}

private Fulfillment fallbackCustomApply(Fulfillment output, HashMap<String, JsonElement> parameter) {
/* with all params except event go to custom leave apply
 * 
 * 	
 */
	String message = "Wanna do it yourself?  Okay! I would not give my suggestion, just let me know the details. I will apply for you." ;
	AIOutputContext contextOut = new AIOutputContext();
	contextOut.setName("applyForLeave-custom");
	contextOut.setLifespan(2);
	HashMap<String, JsonElement> outParameters = new HashMap<String, JsonElement>();
	if (parameter.containsKey("startDate") ){
		JsonElement startDate = new JsonPrimitive(parameter.get("startDate").toString());
		outParameters.put("startDate", startDate);
	} if( parameter.containsKey("endDate") ) {
		JsonElement endDate = new JsonPrimitive(parameter.get("endDate").toString());
		outParameters.put("endDate", endDate);
	}
	contextOut.setParameters(outParameters);
	output.setContextOut(contextOut);
	output.setSpeech(message);
	output.setDisplayText(message);
	return output;
}

private Fulfillment submitFeilds(Fulfillment output, HashMap<String, JsonElement> parameter) {
/* submited after getting all feilds
 *  check for leave balance. if  avil redirect to confirm_leave   else redirect to InsufficientBalance
 *  suggest if near by holiday comming and redirect to confirm leave
 * 
 */
	String message = "";
	if (parameter.get("comment") != null) {
		message = "You want to apply for leave from " + parameter.get("startDate").toString() + " to " + parameter.get("endDate").toString() + " as "+ parameter.get("comment").toString();
	}
	else{
		message = "You want to apply for leave from " + parameter.get("startDate").toString() + " to " + parameter.get("endDate").toString() + " for "+ parameter.get("event").toString();

	}
	message += " \n please confirm ";
	output.setSpeech(message);
	output.setDisplayText(message);
	
	List<AIOutputContext> contextOutList = new LinkedList<AIOutputContext>();
	AIOutputContext contextOut1 = new AIOutputContext();
	contextOut1.setLifespan(2);
	contextOut1.setName("confirmLeave - yes");
	contextOut1.setParameters(parameter);
	contextOutList.add(contextOut1);
	AIOutputContext contextOut2 = new AIOutputContext();
	contextOut2.setLifespan(2);
	contextOut2.setName("confirmLeave - no");
	contextOut2.setParameters(parameter);
	contextOutList.add(contextOut2);

	log.info("Context out parameters");
	output.setContextOut(contextOutList);
	return output;
}

private Fulfillment suggest(Fulfillment output,HashMap<String, JsonElement> parameter) {
	//function to return if no inputs near by holiday/birthday, a mesage to ask if leave is for that
	//if all fields present redirect to confirm
	// if event present redirect to SYSTEM_SUGESTION_SATISFIED_YES
	//if start date & no of days, calc end date
	//
	JSONObject holidayData = Data.getHolidays();
	if (parameter.get("startDate") != null) {
		
	}
	return null;
}
private int getDays(String startDate , String endDate) {
	
	// TODO Auto-generated method stub
int days = 0;

return 0;
}
}