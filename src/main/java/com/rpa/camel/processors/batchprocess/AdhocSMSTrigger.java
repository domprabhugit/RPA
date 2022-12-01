package com.rpa.camel.processors.batchprocess;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.openqa.selenium.WebDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.AdhocSMS;
import com.rpa.service.processors.AdhocSMSPolicyService;


public class AdhocSMSTrigger implements Processor {
	
	private static final Logger logger = LoggerFactory.getLogger(AdhocSMSTrigger.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	WebDriver driver;
	
	@Autowired
	AdhocSMSPolicyService adhocSMSPolicyService;
	
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of AdhocSMSTrigger Called ************");
		logger.info("BEGIN : AdhocSMSTrigger Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty(RPAConstants.TRANSACTION_INFO);
		transactionInfo.setProcessPhase(RPAConstants.AUTOMATER);
		AdhocSMSTrigger whatsappAutomater = new AdhocSMSTrigger();
		adhocSMSPolicyService = applicationContext.getBean(AdhocSMSPolicyService.class);
		whatsappAutomater.doProcess( exchange, transactionInfo, driver,adhocSMSPolicyService);
		logger.info("*********** inside Camel Process of AdhocSMSTrigger Processor Ended ************");

	}

	private void doProcess(Exchange exchange, TransactionInfo transactionInfo, WebDriver driver, AdhocSMSPolicyService adhocSMSPolicyService) throws Exception {
		
        //String message = " Dear Customer,\nDue to some technical glitch, incorrect SMS was triggered to you. We request you to ignore the message and inconvenience caused is highly regretted.\nRoyal Sundaram";
        String message ="Dear Customer, please enter the OTP 000430 to continue with the buying process. Do not share the OTP with anyone for security reasons";
        String enterpriseid = "enterpriseid";
        String subEnterpriseid = "subEnterpriseid";
        String pusheid = "rosunotp";
        String pushepwd = "rosunotp3";
        String sender = "RSHRMS";
        
        
		List<AdhocSMS> msgTobSentList =  adhocSMSPolicyService.findPoliciesTobeSentSms();
		
		logger.info("list count-->"+msgTobSentList.size());
		String number="";
		for(AdhocSMS obj :msgTobSentList){
			number="";
			number = obj.getPhoneNumber();
			if(!obj.getIsSmsSent().equals("Y")){
				logger.info("Sms to be sent to -->"+number);
				if(sendSms(message,enterpriseid,subEnterpriseid,pusheid,pushepwd,sender,number)){
					logger.info("Sms successfully be sent to -->"+number);
					obj.setIsSmsSent("Y");
					adhocSMSPolicyService.save(obj);
				}
			}
			
			
		}
		
	}

	private boolean  sendSms(String message, String enterpriseid, String subEnterpriseid, String pusheid, String pushepwd, String sender, String number) {
		
		try {
            String requestUrl = "https://otp2.maccesssmspush.com/OTP_ACL_Web/OtpRequestListener?"
            + "enterpriseid="+ URLEncoder.encode(enterpriseid, "UTF-8") +"&"
            + "subEnterpriseid="+URLEncoder.encode(subEnterpriseid, "UTF-8")+"&"
            + "pusheid="+URLEncoder.encode(pusheid, "UTF-8")+"&"
            + "pushepwd="+URLEncoder.encode(pushepwd, "UTF-8")+"&"
            + "sender="+URLEncoder.encode(sender, "UTF-8")+"&"
            + "msgtext="+URLEncoder.encode(message, "UTF-8")+"&"
            + "msisdn="+URLEncoder.encode(number, "UTF-8");
            

            URL url = new URL(requestUrl);
            HttpURLConnection uc = (HttpURLConnection)url.openConnection();
            	logger.info("getResponseMessage-->"+uc.getResponseMessage());
            	logger.info("getResponseCode-->"+uc.getResponseCode());

            uc.disconnect();

            return true;
    } catch(Exception ex) {
            System.out.println(ex.getMessage());
            return false;
    }
		
	}

	
	

}
