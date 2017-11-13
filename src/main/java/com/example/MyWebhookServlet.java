package com.example;

import java.util.HashMap;
import java.util.logging.Logger;
import org.json.simple.JSONObject;

import com.google.gson.JsonElement;

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
		output = queryForLeave(output, parameter);
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

private Fulfillment exitFlow(Fulfillment output) {
	// TODO Auto-generated method stub
	return null;
}

private Fulfillment confirmLeave(Fulfillment output, HashMap<String, JsonElement> parameter) {
/*
 * 	leave confirmed, deduct from balance
 * reset context
 */
	return null;
}

private Fulfillment fallbackCustomApply(Fulfillment output, HashMap<String, JsonElement> parameter) {
/* with all params except event go to custom leave apply
 * 
 * 	
 */
	return null;
}

private Fulfillment submitFeilds(Fulfillment output, HashMap<String, JsonElement> parameter) {
/* submited after getting all feilds
 *  check for leave balance. if  avil redirect to confirm_leave   else redirect to InsufficientBalance
 *  suggest if near by holiday comming and redirect to confirm leave
 * 
 */
	return null;
}

private Fulfillment queryForLeave(Fulfillment output,HashMap<String, JsonElement> parameter) {
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

}