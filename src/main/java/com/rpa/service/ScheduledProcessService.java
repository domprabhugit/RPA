/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth.M
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import com.rpa.camel.processors.common.HashMapToExcelLoader;
import com.rpa.constants.RPAConstants;
import com.rpa.service.processors.MarutiPolicyService;
import com.rpa.util.UtilityFile;


@Configuration
@EnableScheduling
public class ScheduledProcessService {

	@Autowired
	private Environment environment;
	
	@Autowired
	private MarutiPolicyService marutiPolicyService;
	
	@Autowired
	MailService mailService;
	
	@Autowired
	HashMapToExcelLoader hashMapToExcelLoader;
	
	private static final Logger logger = LoggerFactory.getLogger(ScheduledProcessService.class.getName());

	@Scheduled(cron = "0 30 0,8,15 ? * *")
	public void KillChromeProcess() {
		logger.info("KillChromeProcess scheduler triggerred");
		try {
			Process process = Runtime.getRuntime().exec("taskkill /f /im chrome.exe");
			process.waitFor(60, TimeUnit.SECONDS);
			if (process != null)
				process.destroy();
			Process process1 = Runtime.getRuntime().exec("taskkill /im chromedriver.exe /f");
			process1.waitFor(60, TimeUnit.SECONDS);
			if (process1 != null)
				process1.destroy();

		} catch (Exception e) {

			logger.error("Error in ScheduledProcessService " + e.getMessage(), e);
		}
	}
	
	@Scheduled(cron = "0 0/15 0/1 ? * *")
	public void executeCMD() {
		logger.info("executeCMD scheduler triggerred");
		try {
			String cmd = "";
			
			if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
				cmd = "ping 10.46.193.9";
			}else{
				 cmd = "ping 10.46.222.160";
			}
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(p.getInputStream()));

			String s = "";
			// reading output stream of the command
			while ((s = inputStream.readLine()) != null) {
				logger.info("pingDB -->"+s);
			}

			inputStream.close();
			
			cmd = "ipconfig";
			
			 p = Runtime.getRuntime().exec(cmd);
			 inputStream = new BufferedReader(
					new InputStreamReader(p.getInputStream()));

			 s = "";
			// reading output stream of the command
			while ((s = inputStream.readLine()) != null) {
				logger.info("ipconfig -->"+s);
			}

			inputStream.close();
		} catch (Exception e) {
			logger.error("Error in executeCMD  " + e.getMessage(), e);
		}
	}
	
	
	/*@Scheduled(cron = "0 0/15 18-23,0-7 ? * *")
	public void checkActiveConnections() {
		logger.info("checkActiveConnections scheduler triggerred");
		try {
			String cmd = "";
			
			if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
				cmd = "ping 10.46.193.9";
			}else{
				 cmd = "ping 10.46.222.160";
			}
			
			cmd = "netstat -a -n";
			
			Process p = Runtime.getRuntime().exec(cmd);
			BufferedReader inputStream = new BufferedReader(
					new InputStreamReader(p.getInputStream()));

			String s = "";
			// reading output stream of the command
			while ((s = inputStream.readLine()) != null) {
				logger.info("checkActiveConnections -->"+s);
			}

			inputStream.close();
			
		} catch (Exception e) {
			logger.error("Error in checkActiveConnections  " + e.getMessage(), e);
		}
	}*/
	
	
	@Scheduled(cron = "0 0/30 0/1 ? * *")
	public void executeQuery() {
		logger.info("executeQuery scheduler triggerred");
		try {
			
		String totalPolicies = marutiPolicyService.getTotalPolicies("Y", "2019");
		logger.info("totalPolicies-->"+totalPolicies);
			
		List<String> cartypeList = 	marutiPolicyService.getCarTypeList();
		
		for(String carType : cartypeList){
			logger.info("carType-->"+carType);
		}
		
		
		} catch (Exception e) {
			logger.error("Error in executeCMD  " + e.getMessage(), e);
		}
	}
	
	
	@Scheduled(cron = "0 0 5 ? * *")
	public void getPolicyPDFMailPendingstatus() {
		logger.info("getPolicyPDFMailPendingstatus scheduler triggerred");
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("uat")))) {
		try {
			 //Create Map for Excel Data 
			HashMap<String, Object[]> excel_data = new HashMap<String, Object[]>(); //create a map and define data
			Connection conn = null;
			DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
			String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_URL);
			String username = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_USERNAME);
			String password = UtilityFile.getDatabaseProperty(RPAConstants.FIRSTGEN_DOWNLOAD_DATASOURCE_PASSWORD);
			try{
				conn = DriverManager.getConnection(connInstance, username, password);
				logger.info("getPolicyPDFMailPendingstatus - connection object created :: " + conn);
				 int row_counter=0;
				 String sql = "SELECT quote_id quoteid FROM d_policy_purchased_details WHERE product IN ('MOTORSHIELDONLINE', 'TWOWHEELER') AND buy_date BETWEEN TO_DATE (to_char(sysdate-1,'mm/dd/yyyy')|| '00:00:00', 'MM/DD/YYYY HH24:MI:SS') AND TO_DATE (to_char(sysdate,'mm/dd/yyyy')|| '23:59:59','MM/DD/YYYY HH24:MI:SS') AND quote_id NOT IN (SELECT quote_id FROM mailstatus b WHERE mail_type = 'PolicyPurchasedPDFMail') AND quote_id NOT IN (SELECT quote_id FROM  D_PURCHASED_EMAIL_TRIGGER where status='Success') ";
					try (PreparedStatement ps = conn.prepareStatement(sql)) {
						try (ResultSet rs = ps.executeQuery()) {
							while (rs.next()) {
								row_counter=row_counter+1;
								excel_data.put(Integer.toString(row_counter), new Object[] {rs.getString(1)});
							}
							if (rs != null)
								rs.close();
						}
						if(ps!=null)
							ps.close();
					}
					
					String path = UtilityFile.getCodeBasePath() + UtilityFile.getCarPolicyProperty(RPAConstants.POLICY_PDFMAIL_PATH)+UtilityFile.dateToSting(new Date(), RPAConstants.dd_slash_MM_slash_YYYY)+"//Status";
					File folder = new File(path);
					folder.mkdirs();
					
					String fileName = "quote.xls";
					hashMapToExcelLoader.convertLoadHashMapValueInExcel(path+"/"+fileName, excel_data, "quote");
						mailService.getPolicyPDFMailPendingstatus(path,row_counter);
					
					
			}finally{
				if(conn!=null)
					conn.close();
			}
		
		
		} catch (Exception e) {
			logger.error("getPolicyPDFMailPendingstatus in executeCMD  " + e.getMessage(), e);
		}
	}else{
		logger.info("getPolicyPDFMailPendingstatus - Scheduler is not applicable for Prod ");
	}
	}
	
}
