package com.rpa.camel.processors.batchprocess;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

import com.rpa.camel.config.SpringContext;
import com.rpa.constants.RPAConstants;
import com.rpa.model.TransactionInfo;
import com.rpa.model.processors.AgentResponse;
import com.rpa.service.processors.AgentIncentiveService;
import com.rpa.util.UtilityFile;

public class AgentIncentiveCalculator implements Processor {

	private static final Logger logger = LoggerFactory.getLogger(AgentIncentiveCalculator.class.getName());

	@Autowired
	ApplicationContext applicationContext;

	@Autowired
	AgentIncentiveService agentIncentiveService;

	@SuppressWarnings("static-access")
	@Override
	public void process(Exchange exchange) throws Exception {
		logger.info("*********** inside Camel Process of abiblPolicyPdfUploader Called ************");
		logger.info("BEGIN : abiblPolicyPdfUploader Processor - Started ");
		applicationContext = SpringContext.getAppContext();
		TransactionInfo transactionInfo = (TransactionInfo) exchange.getProperty("TRANSACTION_INFO_REQ");
		transactionInfo.setProcessPhase("CALCULATOR");
		agentIncentiveService = applicationContext.getBean(AgentIncentiveService.class);

		AgentIncentiveCalculator agentIncentiveCalculator = new AgentIncentiveCalculator();
		agentIncentiveCalculator.doProcess(exchange, transactionInfo, agentIncentiveService);
		logger.info("*********** inside Camel Process of abiblPolicyPdfUploader Processor Ended ************");
	}

	public void doProcess(Exchange exchange, TransactionInfo transactionInfo,
			AgentIncentiveService agentIncentiveService) throws Exception {
		logger.info("BEGIN : AgentIncentiveCalculator Processor - doProcess Method Called  ");

		Connection conn = null;
		conn = UtilityFile.getLocalRPAConnection();
		logger.info("AgentIncentiveCalculator - connection object created :: " + conn);
		try {
			String query = "select * from agent_config";
			
			String agentRupeesPerPolicy="0", agentTPRupeesPerPolicy="0",tlRupeesPerPolicy="0",tlTPRupeesPerPolicy="0",plRupeesPerPolicy="0",plTPRupeesPerPolicy="0";
			
			try (PreparedStatement ps = conn.prepareStatement(query)) {
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						if(rs.getString(1).equalsIgnoreCase("AGENT")){
							agentRupeesPerPolicy = rs.getString(2);
						}else if(rs.getString(1).equalsIgnoreCase("TP_AGENT")){
							agentTPRupeesPerPolicy = rs.getString(2);
						}else if(rs.getString(1).equalsIgnoreCase("TL")){
							tlRupeesPerPolicy = rs.getString(2);
						}else if(rs.getString(1).equalsIgnoreCase("TP_TL")){
							tlTPRupeesPerPolicy = rs.getString(2);
						}else if(rs.getString(1).equalsIgnoreCase("PL")){
							plRupeesPerPolicy = rs.getString(2);
						}else if(rs.getString(1).equalsIgnoreCase("TP_PL")){
							plTPRupeesPerPolicy = rs.getString(2);
						}
					}
					if (rs != null)
						rs.close();
				}
			}
			
			
			String sql="SELECT AGENT_NAME,ACCESS_TYPE,SEARCH_SLAB,ROUND(SEARCH),ROUND((search*SEARCH_SLAB)/100) SEARCH_INCENTIVE,WSI_SLAB,ROUND(WSI),ROUND((wsi*WSI_SLAB)/100) WSI_INCENTIVE,ROUND(TOT_OD_PREMIUM),ROUND((search*SEARCH_SLAB)/100 + (wsi*WSI_SLAB)/100) TOTAL_INCENTIVE,LATE_LOGIN, "+
					  "( "+
					  "CASE "+
					    "WHEN ((search*SEARCH_SLAB)/100 + (wsi*WSI_SLAB)/100) = 0 THEN 0 ELSE ROUND(((search*SEARCH_SLAB)/100 + (wsi*WSI_SLAB)/100)-(LATE_LOGIN)) END) FINAL_INCENTIVE,NO_OD_TWO_WHEELER_POLICIES,ROUND(two_wheeler_incentive),month_year,FLAG_TYPE "+
					"FROM "+
					  "(SELECT AGENT_NAME,ACCESS_TYPE,SUM(SEARCH_SLAB) AS SEARCH_SLAB,SUM(WSI_SLAB)    AS WSI_SLAB,search,WSI,(search+WSI) AS TOT_OD_PREMIUM,NO_OD_TWO_WHEELER_POLICIES,NO_OD_TWO_WHEELER_POLICIES*"+agentRupeesPerPolicy+" two_wheeler_incentive,month_year,'AG' FLAG_TYPE,LATE_LOGIN "+
					  "FROM "+
					    "(SELECT AGENT_NAME,ACCESS_TYPE,TO_NUMBER(PGQ_PERCENTAGE) AS SEARCH_SLAB,0 AS WSI_SLAB,search,WSI,(search+WSI) AS TOT_OD_PREMIUM,NO_OD_TWO_WHEELER_POLICIES,NO_OD_TWO_WHEELER_POLICIES*"+agentRupeesPerPolicy+" two_wheeler_incentive,month_year,'AG' FLAG_TYPE,late_login "+
					    "FROM "+
					 "(SELECT upper(A.AGENT_NAME) AS AGENT_NAME,NVL(LEAD_ACCESS,'NA') ACCESS_TYPE, "+
					 "NVL( SUM( "+
					 "CASE "+
					   "WHEN (PRODUCT NOT IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE NOT IN ('WSI','ECOM','TGQ')) THEN (OUR_SHARE_OF_PREMIUM-b.TP_PREMIUM) "+
					   "WHEN (PRODUCT NOT IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE   IN ('WSI')) THEN 0 "+
					   "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN 0 END ),0) search, "+
					 "NVL( SUM( "+
					 "CASE "+
					   "WHEN (PRODUCT NOT IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE   IN ('WSI')) THEN (OUR_SHARE_OF_PREMIUM-b.TP_PREMIUM) "+
					   "WHEN (PRODUCT NOT   IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE NOT IN ('WSI')) THEN 0 "+
					   "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN 0 END ),0) WSI, "+
					 "NVL( SUM( "+
					 "CASE "+
					   "WHEN PRODUCT NOT IN ('VMC','VMB','VMBL' ) THEN 0 "+
					   "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN to_number(POLICY_COUNT) "+
					 "END ),0) AS NO_OD_TWO_WHEELER_POLICIES,TO_CHAR(entry_date,'mm/yyyy') AS month_year, TO_NUMBER(late_login) late_login "+
					 "FROM rpa_admin.conversion_data A INNER JOIN rpa_admin.AGENT_ACCESS_MASTER E ON upper(A.AGENT_NAME) = upper(E.AGENT_NAME) LEFT JOIN rpa_admin.xgen_temp b ON A.POLICY_NO  =B.POLICY_NO "+
					 "WHERE TO_CHAR(entry_date,'mm/yyyy')=E.MONTH_YEAR AND  TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))=(SELECT max(TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))) FROM RPA_ADMIN.CONVERSION_DATA CD WHERE a.policy_no=CD.policy_no and  TO_NUMBER(REPLACE(TO_CHAR(CD.CONVERSION_dATE,'YYYY/MM'),'/',''))<=TO_NUMBER(REPLACE(TO_CHAR(B.ENTRY_DATE,'YYYY/MM'),'/',''))  ) GROUP BY upper(A.AGENT_NAME), NVL(LEAD_ACCESS,'NA'), TO_CHAR(entry_date,'mm/yyyy'), TO_NUMBER(late_login) "+
					/* ") C , "+
					 "rpa_admin.AGENT_SLAB_MASTER D WHERE (TO_NUMBER(search) >= TO_NUMBER(OD_MIN_SLAB) AND TO_NUMBER(search) <TO_NUMBER(OD_MAX_SLAB)) "+*/
					 ") C LEFT JOIN "+
					 "rpa_admin.AGENT_SLAB_MASTER D ON ((TO_NUMBER(search) >= TO_NUMBER(OD_MIN_SLAB) AND TO_NUMBER(search) <TO_NUMBER(OD_MAX_SLAB)) OR ( TO_NUMBER(search) >= TO_NUMBER(OD_MIN_SLAB) AND OD_MIN_SLAB =OD_MAX_SLAB ) ) "+
					    "UNION "+
					    "SELECT AGENT_NAME,ACCESS_TYPE, 0 AS SEARCH_SLAB, TO_NUMBER(WSI_PERCENTAGE) AS WSI_SLAB,search,WSI,(search+WSI) AS TOT_OD_PREMIUM,NO_OD_TWO_WHEELER_POLICIES,NO_OD_TWO_WHEELER_POLICIES*"+agentRupeesPerPolicy+" two_wheeler_incentive,month_year,'AG' FLAG_TYPE,late_login "+
					    "FROM "+
					 "(SELECT upper(A.AGENT_NAME) AS AGENT_NAME,NVL(LEAD_ACCESS,'NA') ACCESS_TYPE, "+
					"NVL( SUM( "+
					 "CASE "+
					   "WHEN (PRODUCT NOT IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE NOT IN ('WSI','ECOM','TGQ')) THEN (OUR_SHARE_OF_PREMIUM-b.TP_PREMIUM) "+
					   "WHEN (PRODUCT NOT IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE   IN ('WSI')) "+
					   "THEN 0 WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN 0 END ),0) search, "+
					 "NVL( SUM( "+
					 "CASE "+
					   "WHEN (PRODUCT NOT IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE   IN ('WSI')) THEN (OUR_SHARE_OF_PREMIUM-b.TP_PREMIUM) "+
					   "WHEN (PRODUCT NOT   IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE NOT IN ('WSI')) THEN 0 "+
					   "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN 0 END ),0) WSI, "+
					 "NVL( SUM( "+
					 "CASE "+
					   "WHEN PRODUCT NOT IN ('VMC','VMB','VMBL' ) THEN 0 "+
					   "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN to_number(POLICY_COUNT) "+
					 "END ),0)   AS NO_OD_TWO_WHEELER_POLICIES, TO_CHAR(entry_date,'mm/yyyy') AS month_year, TO_NUMBER(late_login) late_login "+
					 "FROM rpa_admin.conversion_data A INNER JOIN rpa_admin.AGENT_ACCESS_MASTER E ON upper(A.AGENT_NAME) = upper(E.AGENT_NAME) LEFT JOIN rpa_admin.xgen_temp b ON A.POLICY_NO  =B.POLICY_NO "+
					 "WHERE TO_CHAR(entry_date,'mm/yyyy')=E.MONTH_YEAR AND  TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))=(SELECT max(TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))) FROM RPA_ADMIN.CONVERSION_DATA CD WHERE a.policy_no=CD.policy_no and  TO_NUMBER(REPLACE(TO_CHAR(CD.CONVERSION_dATE,'YYYY/MM'),'/',''))<=TO_NUMBER(REPLACE(TO_CHAR(B.ENTRY_DATE,'YYYY/MM'),'/',''))  ) GROUP BY upper(A.AGENT_NAME), NVL(LEAD_ACCESS,'NA'), TO_CHAR(entry_date,'mm/yyyy'),TO_NUMBER(late_login) "+
					/* ") C , "+
					 "rpa_admin.AGENT_SLAB_MASTER D WHERE (TO_NUMBER(wsi) >= TO_NUMBER(OD_MIN_SLAB) AND TO_NUMBER(wsi) <TO_NUMBER(OD_MAX_SLAB)) "+*/
					 ") C LEFT JOIN "+
					 "rpa_admin.AGENT_SLAB_MASTER D ON ((TO_NUMBER(wsi) >= TO_NUMBER(OD_MIN_SLAB) AND TO_NUMBER(wsi) <TO_NUMBER(OD_MAX_SLAB)) OR ( TO_NUMBER(wsi) >= TO_NUMBER(OD_MIN_SLAB) AND OD_MIN_SLAB =OD_MAX_SLAB ) ) "+
					    ") "+
					  "GROUP BY AGENT_NAME,ACCESS_TYPE,search, WSI,(search+WSI),NO_OD_TWO_WHEELER_POLICIES,NO_OD_TWO_WHEELER_POLICIES*"+agentRupeesPerPolicy+",month_year,'AG',late_login "+
					  ")";
			try (PreparedStatement ps = conn.prepareStatement(sql)) {
				try (ResultSet rs = ps.executeQuery()) {
					while (rs.next()) {
						AgentResponse agentResponse = new AgentResponse();
						agentResponse.setName(rs.getString(1));
						agentResponse.setAccessType(rs.getString(2));
						agentResponse.setSearchSlab(rs.getString(3));
						agentResponse.setSearch(rs.getString(4));
						agentResponse.setSearchInsentive(rs.getString(5));
						agentResponse.setWsiSlab(rs.getString(6));
						agentResponse.setWsi(rs.getString(7));
						agentResponse.setWsiIncentive(rs.getString(8));
						agentResponse.setTotOdPremium(rs.getString(9));
						agentResponse.setTotalIncentive(rs.getString(10));
						agentResponse.setLateLogin(rs.getString(11));
						agentResponse.setFinalIncentive(rs.getString(12));
						//agentResponse.setMotorOdIncentive(rs.getString(11));
						agentResponse.setNoOfTwoWheelerPolicies(rs.getString(13));
						agentResponse.setTwoWheelerIncentive(rs.getString(14));
						agentResponse.setMonthYear(rs.getString(15));
						agentResponse.setFlagType(rs.getString(16));
						agentIncentiveService.save(agentResponse);
						// agentResponseList.add(agentResponse);

						agentResponse = null;
					}
					if (rs != null)
						rs.close();
				}
			}
			
			 sql="SELECT upper(A.AGENT_NAME) AS AGENT_NAME,"+agentRupeesPerPolicy+" as per_policy ,NVL(SUM(POLICY_COUNT),0)  AS NO_OD_TWO_WHEELER_POLICIES,NVL(SUM(POLICY_COUNT),0)*"+agentRupeesPerPolicy+" two_wheeler_incentive,TO_CHAR(entry_date,'mm/yyyy') AS month_year,'AG-2W' FLAG_TYPE "+
					 "FROM rpa_admin.conversion_data A LEFT JOIN rpa_admin.xgen_temp b ON A.POLICY_NO=B.POLICY_NO "+
					 "WHERE PRODUCT IN ('VMC','VMB','VMBL' ) and UPPER(AGENT_NAME) NOT IN (UPPER('ECOM'),UPPER('SBS')) AND TO_NUMBER(REPLACE(TO_CHAR(A.CONVERSION_dATE,'YYYY/MM'),'/',''))= (SELECT max(TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))) FROM RPA_ADMIN.CONVERSION_DATA CD WHERE a.policy_no=CD.policy_no and  TO_NUMBER(REPLACE(TO_CHAR(CD.CONVERSION_dATE,'YYYY/MM'),'/',''))<=TO_NUMBER(REPLACE(TO_CHAR(B.ENTRY_DATE,'YYYY/MM'),'/',''))  )   "+
					 "GROUP BY upper(A.AGENT_NAME), TO_CHAR(entry_date,'mm/yyyy') ";
			 
			 try (PreparedStatement ps = conn.prepareStatement(sql)) {
					try (ResultSet rs = ps.executeQuery()) {
						while (rs.next()) {
							AgentResponse agentResponse = new AgentResponse();
							agentResponse.setName(rs.getString(1));
							agentResponse.setNoOfTwoWheelerPolicies(rs.getString(2));
							agentResponse.setPerPolicy(rs.getString(3));
							agentResponse.setTwoWheelerIncentive(rs.getString(4));
							agentResponse.setMonthYear(rs.getString(5));
							agentResponse.setFlagType(rs.getString(6));
							agentIncentiveService.save(agentResponse);
							// agentResponseList.add(agentResponse);

							agentResponse = null;
						}
						if (rs != null)
							rs.close();
					}
				}
			
		sql = "SELECT TL_NAME, "+
				  "total_od AS TOT_OD_PREMIUM, "+
				  "ROUND(NVL( (( "+
				  "CASE "+
				    "WHEN (total_od/E.target)*100>100 THEN PERCENTAGE_100_ABOVE "+
				    "WHEN ((total_od/E.target)*100 >95 AND (total_od  /E.target)*100<=100 ) THEN PERCENTAGE_95_TO_100 "+
				    "WHEN ((total_od/E.target)*100 >90 AND (total_od  /E.target)*100<=95 ) THEN PERCENTAGE_90_TO_95 "+
				    "WHEN ((total_od/E.target)*100 >85 AND (total_od  /E.target)*100<=90 ) THEN PERCENTAGE_85_TO_90 "+
				    "WHEN ((total_od/E.target)*100>=70 AND (total_od  /E.target)*100<=85 ) THEN PERCENTAGE_70_TO_85 "+
				    "ELSE '0' END)*total_od)/100,0)) MOTOR_OD_INCENTIVE, NO_OD_TWO_WHEELER_POLICIES, NO_OD_TWO_WHEELER_POLICIES*"+tlRupeesPerPolicy+" two_wheeler_incentive, "+
				  "round(NVL( (( "+
				  "CASE "+
				    "WHEN (total_od/E.target)*100>100 THEN PERCENTAGE_100_ABOVE "+
				    "WHEN ((total_od/E.target)*100 >95 AND (total_od  /E.target)*100<=100 ) THEN PERCENTAGE_95_TO_100 "+
				    "WHEN ((total_od/E.target)*100 >90 AND (total_od  /E.target)*100<=95 ) THEN PERCENTAGE_90_TO_95 "+
				    "WHEN ((total_od/E.target)*100 >85 AND (total_od  /E.target)*100<=90 ) THEN PERCENTAGE_85_TO_90 "+
				    "WHEN ((total_od/E.target)*100>=70 AND (total_od  /E.target)*100<=85 ) THEN PERCENTAGE_70_TO_85 "+
					"ELSE '0' END)*total_od)/100,0)+(NO_OD_TWO_WHEELER_POLICIES*"+tlRupeesPerPolicy+")) final_incentive,month_year,'TL' FLAG_TYPE "+
				"FROM "+
				  "(SELECT upper(A.TL_NAME) AS TL_NAME, "+
				    "NVL( SUM( "+
				    "CASE "+
				      "WHEN (PRODUCT NOT        IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE NOT      IN ('ECOM','TGQ')) THEN (OUR_SHARE_OF_PREMIUM-b.TP_PREMIUM) "+
				      "WHEN PRODUCT             IN ('VMC','VMB','VMBL' ) THEN 0 "+
				    "END ),0) total_od, "+
				    "NVL( SUM( "+
				    "CASE "+
				      "WHEN PRODUCT NOT IN ('VMC','VMB','VMBL' ) THEN 0 "+
				      "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN to_number(POLICY_COUNT) "+
				    "END ),0) AS NO_OD_TWO_WHEELER_POLICIES,TO_CHAR(entry_date,'mm/yyyy') AS month_year "+
				  "FROM rpa_admin.conversion_data A INNER JOIN rpa_admin.AGENT_ACCESS_MASTER E ON upper(A.AGENT_NAME) = upper(E.AGENT_NAME) LEFT JOIN rpa_admin.xgen_temp b ON A.POLICY_NO=B.POLICY_NO "+
				  "WHERE TO_CHAR(entry_date,'mm/yyyy')=E.MONTH_YEAR  AND  TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))=(SELECT max(TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))) FROM RPA_ADMIN.CONVERSION_DATA CD WHERE a.policy_no=CD.policy_no and  TO_NUMBER(REPLACE(TO_CHAR(CD.CONVERSION_dATE,'YYYY/MM'),'/',''))<=TO_NUMBER(REPLACE(TO_CHAR(B.ENTRY_DATE,'YYYY/MM'),'/',''))  ) GROUP BY upper(A.TL_NAME),TO_CHAR(entry_date,'mm/yyyy') "+
				  ") C , "+
				  "RPA_ADMIN.TL_SLAB_MASTER D,RPA_ADMIN.LEAD_TARGET_MASTER E WHERE UPPER(c.TL_NAME) =UPPER(E.name) and UPPER(c.TL_NAME) not in ( select LEAD_NAME from RPA_ADMIN.IN_HOUSE_LEADS) AND TO_CHAR(MONTH_YEAR)=TO_CHAR(E.MONTH)|| '/'||TO_CHAR(E.year) AND lead_type='TL'  "+
				"union SELECT TL_NAME,total_od AS TOT_OD_PREMIUM,ROUND(NVL((D.INCENTIVE_PERCENTAGE*total_od)/100,0)) MOTOR_OD_INCENTIVE,NO_OD_TWO_WHEELER_POLICIES,NO_OD_TWO_WHEELER_POLICIES*"+tlRupeesPerPolicy+" two_wheeler_incentive,round(NVL((D.INCENTIVE_PERCENTAGE*total_od)/100,0)+( NO_OD_TWO_WHEELER_POLICIES*"+tlRupeesPerPolicy+")) final_incentive,month_year,'TL' FLAG_TYPE "+
				"FROM "+
				  "(SELECT upper(A.TL_NAME) AS TL_NAME, "+
				    "NVL( SUM( "+
				    "CASE "+
				      "WHEN (PRODUCT NOT IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE NOT IN ('ECOM','TGQ')) THEN (OUR_SHARE_OF_PREMIUM-b.TP_PREMIUM) "+
				      "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN 0 "+
					"END ),0) total_od, "+
				    "NVL( SUM( "+
				    "CASE "+
				      "WHEN PRODUCT NOT IN ('VMC','VMB','VMBL' ) THEN 0 "+
				      "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN to_number(POLICY_COUNT) "+
				    "END ),0) AS NO_OD_TWO_WHEELER_POLICIES,TO_CHAR(entry_date,'mm/yyyy') AS month_year "+
				  "FROM rpa_admin.conversion_data A INNER JOIN rpa_admin.AGENT_ACCESS_MASTER E ON upper(A.AGENT_NAME) = upper(E.AGENT_NAME) LEFT JOIN rpa_admin.xgen_temp b ON A.POLICY_NO=B.POLICY_NO "+
				  "WHERE TO_CHAR(entry_date,'mm/yyyy')=E.MONTH_YEAR AND  TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))=(SELECT max(TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))) FROM RPA_ADMIN.CONVERSION_DATA CD WHERE a.policy_no=CD.policy_no and  TO_NUMBER(REPLACE(TO_CHAR(CD.CONVERSION_dATE,'YYYY/MM'),'/',''))<=TO_NUMBER(REPLACE(TO_CHAR(B.ENTRY_DATE,'YYYY/MM'),'/',''))  )  GROUP BY upper(A.TL_NAME),TO_CHAR(entry_date,'mm/yyyy') "+
				  ") C , "+
				  "RPA_ADMIN.TL_INHOUSE_SLAB_MASTER D, "+
				  "RPA_ADMIN.LEAD_TARGET_MASTER E "+
				"WHERE UPPER(c.TL_NAME) =UPPER(E.name) and UPPER(c.TL_NAME) in ( select LEAD_NAME from RPA_ADMIN.IN_HOUSE_LEADS) AND (TO_NUMBER(total_od) >= TO_NUMBER(OD_MIN_SLAB) AND TO_NUMBER(total_od) <TO_NUMBER(OD_MAX_SLAB)) OR (TO_NUMBER(total_od) >= TO_NUMBER(OD_MIN_SLAB) AND OD_MIN_SLAB=OD_MAX_SLAB ) AND TO_CHAR(MONTH_YEAR)=TO_CHAR(E.MONTH)|| '/'||TO_CHAR(E.year) AND lead_type='TL'";
		sql +="union SELECT PL_NAME,total_od AS TOT_OD_PREMIUM,round(nvl( ((CASE "+
				"WHEN (total_od/E.target)*100>100 THEN PERCENTAGE_100_ABOVE "+
				"WHEN ((total_od/E.target)*100 >95 AND (total_od/E.target)*100<=100 ) THEN PERCENTAGE_95_TO_100 "+
				"WHEN ((total_od/E.target)*100 >90 AND (total_od/E.target)*100<=95 ) THEN PERCENTAGE_90_TO_95 "+
				"WHEN ((total_od/E.target)*100 >85 AND (total_od/E.target)*100<=90 ) THEN PERCENTAGE_85_TO_90 "+
				"WHEN ((total_od/E.target)*100>=70 AND (total_od/E.target)*100<=85 ) THEN PERCENTAGE_70_TO_85 ELSE '0' END)*total_od)/100,0)) MOTOR_OD_INCENTIVE, "+
				"NO_OD_TWO_WHEELER_POLICIES,NO_OD_TWO_WHEELER_POLICIES*"+plRupeesPerPolicy+" two_wheeler_incentive,"
				+ "round(nvl( ((CASE "+
				"WHEN (total_od/E.target)*100>100 THEN PERCENTAGE_100_ABOVE "+
				"WHEN ((total_od/E.target)*100 >95 AND (total_od/E.target)*100<=100 ) THEN PERCENTAGE_95_TO_100 "+
				"WHEN ((total_od/E.target)*100 >90 AND (total_od/E.target)*100<=95 ) THEN PERCENTAGE_90_TO_95 "+
				"WHEN ((total_od/E.target)*100 >85 AND (total_od/E.target)*100<=90 ) THEN PERCENTAGE_85_TO_90 "+
				"WHEN ((total_od/E.target)*100>=70 AND (total_od/E.target)*100<=85 ) THEN PERCENTAGE_70_TO_85 ELSE '0' END)*total_od)/100,0)+(NO_OD_TWO_WHEELER_POLICIES*"+plRupeesPerPolicy+")) final_incentive,month_year,'PL' FLAG_TYPE "+
			"FROM "+
			"(SELECT upper(A.PROCESS_TL) AS PL_NAME, "+
			  "NVL( SUM( "+
			  "CASE "+
			    "WHEN (PRODUCT NOT IN ('VMC','VMB','VMBL' ) AND LEAD_SOURCE NOT IN ('ECOM','TGQ')) THEN (OUR_SHARE_OF_PREMIUM-b.TP_PREMIUM) "+
			    "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN 0 "+
			  "END ),0) total_od, "+
			  "NVL( SUM( "+
			  "CASE "+
			    "WHEN PRODUCT NOT IN ('VMC','VMB','VMBL' ) THEN 0 "+
			    "WHEN PRODUCT IN ('VMC','VMB','VMBL' ) THEN to_number(POLICY_COUNT) END ),0) AS NO_OD_TWO_WHEELER_POLICIES, "+
			  "TO_CHAR(entry_date,'mm/yyyy') AS month_year "+
			"FROM rpa_admin.conversion_data A INNER JOIN rpa_admin.AGENT_ACCESS_MASTER E ON upper(A.AGENT_NAME) = upper(E.AGENT_NAME) "+
			"LEFT JOIN rpa_admin.xgen_temp b ON A.POLICY_NO   =B.POLICY_NO "+
			"WHERE  TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))=(SELECT max(TO_NUMBER(REPLACE(TO_CHAR(CONVERSION_dATE,'YYYY/MM'),'/',''))) FROM RPA_ADMIN.CONVERSION_DATA CD WHERE a.policy_no=CD.policy_no and  TO_NUMBER(REPLACE(TO_CHAR(CD.CONVERSION_dATE,'YYYY/MM'),'/',''))<=TO_NUMBER(REPLACE(TO_CHAR(B.ENTRY_DATE,'YYYY/MM'),'/',''))  )  and TO_CHAR(entry_date,'mm/yyyy')=E.MONTH_YEAR  and upper(A.PROCESS_TL) not in upper('vel') GROUP BY upper(A.PROCESS_TL), TO_CHAR(entry_date,'mm/yyyy') "+
			") C , "+
			"RPA_ADMIN.PL_SLAB_MASTER D, RPA_ADMIN.LEAD_TARGET_MASTER E WHERE UPPER(c.PL_NAME) =UPPER(E.name) AND TO_CHAR(MONTH_YEAR)=TO_CHAR(E.MONTH) || '/' ||TO_CHAR(E.year) AND lead_type='PL'  ";
		
		
		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					AgentResponse agentResponse = new AgentResponse();
					agentResponse.setName(rs.getString(1));
					agentResponse.setTotOdPremium(rs.getString(2));
					agentResponse.setTotalIncentive(rs.getString(3));
					agentResponse.setNoOfTwoWheelerPolicies(rs.getString(4));
					agentResponse.setTwoWheelerIncentive(rs.getString(5));
					agentResponse.setFinalIncentive(rs.getString(6));
					agentResponse.setMonthYear(rs.getString(7));
					agentResponse.setFlagType(rs.getString(8));
					agentIncentiveService.save(agentResponse);
					// agentResponseList.add(agentResponse);

					agentResponse = null;
				}
				if (rs != null)
					rs.close();
			}
		}
		
	
		
		
		} finally {
			if (conn != null)
				conn.close();
		}

		transactionInfo.setProcessStatus(RPAConstants.Success);
		exchange.setProperty(RPAConstants.TRANSACTION_INFO, transactionInfo);

		logger.info("BEGIN : AgentIncentiveCalculator Processor - doProcess Method Ended  ");
	}

}
