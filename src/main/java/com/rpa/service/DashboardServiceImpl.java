package com.rpa.service;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import com.rpa.constants.RPAConstants;
import com.rpa.response.CarPolicyStatus;
import com.rpa.response.ChartMultiData;
import com.rpa.response.ChartMultiDataSet;
import com.rpa.response.GridAutomationStatus;
import com.rpa.response.MonthWiseCountStatus;
import com.rpa.util.UtilityFile;

@Service
public class DashboardServiceImpl implements DashboardService {
	
	@Autowired
	private Environment environment;

	private static final Logger logger = LoggerFactory.getLogger(DashboardServiceImpl.class.getName());

	@Override
	public ChartMultiData carPolicyChartStatus(String year) {
	try{
		ChartMultiData chartData = new ChartMultiData();
		List<ChartMultiDataSet> chartDataSets = new ArrayList<ChartMultiDataSet>();
		List<String> labels = new ArrayList<String>();
		List<String> data = new ArrayList<String>();
		List<String> data1 = new ArrayList<String>();
		List<String> data2 = new ArrayList<String>();
		List<String> data3 = new ArrayList<String>();
		List<CarPolicyStatus> list = new ArrayList<CarPolicyStatus>();

		list = carPolicyChartStatusDetails(year);

		List<String> carType = new ArrayList<String>();
		List<String> policyDownloaded = new ArrayList<String>();
		List<String> proposalDownloaded = new ArrayList<String>();
		List<String> policyUploaded = new ArrayList<String>();
		List<String> proposalUploaded = new ArrayList<String>();

		for (CarPolicyStatus carPolicyStatus : list) {
			carType.add(carPolicyStatus.getCarType());
			policyDownloaded.add(carPolicyStatus.getPolicyDownloaded());
			proposalDownloaded.add(carPolicyStatus.getProposalDownloaded());
			policyUploaded.add(carPolicyStatus.getPolicyUploaded());
			proposalUploaded.add(carPolicyStatus.getProposalUploaded());
		}
		int index = 0;
		for (int i = 0; i < carType.size(); i++) {
			data1.add(policyDownloaded.get(i));
			labels.add(carType.get(i));
		}
		//List<String> hoverBackgroundColor1 = new ArrayList<String>();
		//hoverBackgroundColor1.add(color1);
		
		ChartMultiDataSet chartDataSet1 = new ChartMultiDataSet();
		String color1 = getBGColorCode(index);
		chartDataSet1.setBackgroundColor(color1);
		/*chartDataSet1.setLabel("Policy Downloaded");*/
		chartDataSet1.setLabel("Policies Downloaded");
		chartDataSet1.setData(data1);
		chartDataSets.add(chartDataSet1);

		ChartMultiDataSet chartDataSet = new ChartMultiDataSet();
		for (int i = 0; i < proposalDownloaded.size(); i++) {
			data.add(proposalDownloaded.get(i));
		}
		String color = getBGColorCode(index+1);
		chartDataSet.setBackgroundColor(color);
		chartDataSet.setData(data);
		/*chartDataSet.setLabel("Proposal Downloaded");*/
		chartDataSet.setLabel("Proposals Downloaded");
		chartDataSets.add(chartDataSet);

		ChartMultiDataSet chartDataSet2 = new ChartMultiDataSet();
		for (int i = 0; i < policyUploaded.size(); i++) {
			data2.add(policyUploaded.get(i));
		}
		String color2 = getBGColorCode(index+2);
		chartDataSet2.setBackgroundColor(color2);
		chartDataSet2.setData(data2);
		/*chartDataSet2.setLabel("Policy Uploaded");*/
		chartDataSet2.setLabel("Policies Uploaded");
		chartDataSets.add(chartDataSet2);
		
		ChartMultiDataSet chartDataSet3 = new ChartMultiDataSet();
		for (int i = 0; i < proposalUploaded.size(); i++) {
			data3.add(proposalUploaded.get(i));
		}
		String color3 = getBGColorCode(index+3);
		chartDataSet3.setBackgroundColor(color3);
		chartDataSet3.setData(data3);
		/*chartDataSet3.setLabel("Proposal Uploaded");*/
		chartDataSet3.setLabel("Proposals Uploaded");
		chartDataSets.add(chartDataSet3);

		chartData.setDatasets(chartDataSets);
		chartData.setLabels(labels);
		return chartData;
	} catch (Exception e) {
		logger.error("Error in getPOStatusChart of SupplierWiseMisServiceImpl is called ::" + e.getMessage(), e);
		return null;
	}
	}
	
	public List<CarPolicyStatus> carPolicyChartStatusDetails(String year) throws SQLException{
		List<CarPolicyStatus> statusList = new ArrayList<>();
		
		String sql ="select 'MARUTI' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded),sum(proposal_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM maruti_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM maruti_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_downloaded='Y' "+
		"union all " +
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_uploaded,0 AS proposal_uploaded FROM maruti_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_uploaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,COUNT(*) AS proposal_uploaded FROM maruti_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_uploaded='Y' "+
		") union "+
		
		"select 'HONDA' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded),sum(proposal_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM honda_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM honda_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_downloaded='Y' "+
		"union all " +
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_uploaded,0 AS proposal_uploaded FROM honda_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_uploaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,COUNT(*) AS proposal_uploaded FROM honda_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_uploaded='Y' "+
		") union "+
		
		"select 'FORD' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded),sum(proposal_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM ford_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM ford_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_downloaded='Y' "+
		"union all " +
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_uploaded,0 AS proposal_uploaded FROM ford_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_uploaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,COUNT(*) AS proposal_uploaded FROM ford_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_uploaded='Y' "+
		") union "+
		
		" select 'TATA' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded),sum(proposal_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM tata_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM tata_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_downloaded='Y' "+
		"union all " +
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_uploaded,0 AS proposal_uploaded FROM tata_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_uploaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,COUNT(*) AS proposal_uploaded FROM tata_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_uploaded='Y' "+
		")union "+
		
		"select 'ABIBL' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded),sum(proposal_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM abibl_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM abibl_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_downloaded='Y' "+
		"union all " +
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_uploaded,0 AS proposal_uploaded FROM abibl_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_uploaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,COUNT(*) AS proposal_uploaded FROM abibl_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_uploaded='Y' "+
		")union "+
		
		"select 'MIBL' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded),sum(proposal_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM mibl_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM mibl_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_downloaded='Y' "+
		"union all " +
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_uploaded,0 AS proposal_uploaded FROM mibl_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_uploaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,COUNT(*) AS proposal_uploaded FROM mibl_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_uploaded='Y' "+
		")union "+
		
		"select 'VOLVO' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded),sum(proposal_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM volvo_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM volvo_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_downloaded='Y' "+
		"union all " +
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_uploaded,0 AS proposal_uploaded FROM volvo_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_uploaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,COUNT(*) AS proposal_uploaded FROM volvo_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_uploaded='Y' "+
		")union "+
		
		"select 'TAFE' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded),sum(proposal_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM tafe_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM tafe_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_downloaded='Y' "+
		"union all " +
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_uploaded,0 AS proposal_uploaded FROM tafe_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_uploaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,COUNT(*) AS proposal_uploaded FROM tafe_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_uploaded='Y' "+
		")union "+
		
		"select 'PIAGGIO' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded),sum(proposal_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM piaggio_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded,0 AS proposal_uploaded FROM piaggio_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_downloaded='Y' "+
		"union all " +
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_uploaded,0 AS proposal_uploaded FROM piaggio_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_policy_uploaded='Y' "+
		"union all "+
		"SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded,COUNT(*) AS proposal_uploaded FROM piaggio_policy WHERE TO_CHAR(to_date(policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_proposal_uploaded='Y' "+
		") ";
		
		
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					CarPolicyStatus carPolicyStatus = new CarPolicyStatus();
					carPolicyStatus.setCarType(rs.getString(1));
					carPolicyStatus.setPolicyDownloaded(rs.getString(2));
					carPolicyStatus.setProposalDownloaded(rs.getString(3));
					carPolicyStatus.setPolicyUploaded(rs.getString(4));
					carPolicyStatus.setProposalUploaded(rs.getString(5));
					statusList.add(carPolicyStatus);
					carPolicyStatus = null;
				}
				if (rs != null)
					rs.close();
			
		logger.info("carPolicyChartStatusDetails - connection object created :: " + conn);
		}catch(Exception e){
			logger.error("Error in carPolicyChartStatusDetails ::" + e.getMessage(), e);
		}
		finally{
			if(conn != null)
			conn.close();
			if(ps!=null)
				ps.close();
		}
		return statusList;
	}
	
	
	
	
	public String getBGColorCode(int index) {
		String color = "";
		String[] colors =  "#cc0066,#00b050,#00b0f0,#ffc000,#cc00cc,#006600,#003399,#f5d607,#aa5eff,#ff0404,#2bf8ff,#b6ff67".split(",");
		if(index<colors.length){
			 color = colors[index];	
		}else{
			Random r = new Random();
			final char[] hex = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
					'a', 'b', 'c', 'd', 'e', 'f' };
			char[] s = new char[7];
			int n = r.nextInt(0x1000000);

			s[0] = '#';
			for (int i = 1; i < 7; i++) {
				s[i] = hex[n & 0xf];
				n >>= 4;
			}
			color = new String(s);
		}
		
		return color;

	}
	
	
	@Override
	public ChartMultiData xgenPolicyChartStatus(String year) {
	try{
		ChartMultiData chartData = new ChartMultiData();
		List<ChartMultiDataSet> chartDataSets = new ArrayList<ChartMultiDataSet>();
		List<String> labels = new ArrayList<String>();
		List<String> data = new ArrayList<String>();
		List<String> data1 = new ArrayList<String>();
		List<String> data2 = new ArrayList<String>();
		List<CarPolicyStatus> list = new ArrayList<CarPolicyStatus>();
		
		//labels.add("X-Gen");

		list = xgenPolicyChartStatusDetails(year);

		List<String> carType = new ArrayList<String>();
		List<String> policyDownloaded = new ArrayList<String>();
		List<String> proposalDownloaded = new ArrayList<String>();
		List<String> policyUploaded = new ArrayList<String>();

		for (CarPolicyStatus carPolicyStatus : list) {
			carType.add(carPolicyStatus.getCarType());
			policyDownloaded.add(carPolicyStatus.getProposalDownloaded());
			proposalDownloaded.add(carPolicyStatus.getProposalDownloaded());
			policyUploaded.add(carPolicyStatus.getPolicyUploaded());
		}
		int index = 0;
		for (int i = 0; i < carType.size(); i++) {
			data1.add(policyDownloaded.get(i));
			labels.add(carType.get(i));
		}
		//List<String> hoverBackgroundColor1 = new ArrayList<String>();
		//hoverBackgroundColor1.add(color1);
		
		ChartMultiDataSet chartDataSet1 = new ChartMultiDataSet();
		String color1 = getBGColorCode(index);
		chartDataSet1.setBackgroundColor(color1);
		/*chartDataSet1.setLabel("Policy Downloaded");*/
		chartDataSet1.setLabel("Policies Downloaded");
		chartDataSet1.setData(data1);
		chartDataSets.add(chartDataSet1);

		ChartMultiDataSet chartDataSet = new ChartMultiDataSet();
		for (int i = 0; i < proposalDownloaded.size(); i++) {
			data.add(proposalDownloaded.get(i));
		}
		String color = getBGColorCode(index+1);
		chartDataSet.setBackgroundColor(color);
		chartDataSet.setData(data);
		/*chartDataSet.setLabel("Proposal Downloaded");*/
		chartDataSet.setLabel("Invoice Downloaded");
		chartDataSets.add(chartDataSet);

		ChartMultiDataSet chartDataSet2 = new ChartMultiDataSet();
		for (int i = 0; i < policyUploaded.size(); i++) {
			data2.add(policyUploaded.get(i));
		}
		String color2 = getBGColorCode(index+2);
		chartDataSet2.setBackgroundColor(color2);
		chartDataSet2.setData(data2);
		/*chartDataSet2.setLabel("Policy Uploaded");*/
		chartDataSet2.setLabel("Policies Uploaded");
		chartDataSets.add(chartDataSet2);
		
		chartData.setDatasets(chartDataSets);
		chartData.setLabels(labels);
		return chartData;
	} catch (Exception e) {
		logger.error("Error in getPOStatusChart of SupplierWiseMisServiceImpl is called ::" + e.getMessage(), e);
		return null;
	}
	}

	private List<CarPolicyStatus> xgenPolicyChartStatusDetails(String year) throws SQLException {
List<CarPolicyStatus> statusList = new ArrayList<>();
		
		String sql ="select 'XGEN' car_type,sum(policy_downloaded),sum(proposal_downloaded),sum(policy_uploaded) from ("+
		"SELECT COUNT(*) AS policy_downloaded,0 AS proposal_downloaded,0 AS policy_uploaded FROM firstgen_policy marutipoli0_ "
		+ "WHERE TO_CHAR(to_date(marutipoli0_.policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND  marutipoli0_.is_policy_downloaded='Y'"
		+ " union all SELECT 0 AS policy_downloaded,COUNT(*) AS proposal_downloaded,0 AS policy_uploaded FROM firstgen_policy marutipoli0_"
		+ " WHERE TO_CHAR(to_date(marutipoli0_.policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND marutipoli0_.is_invoice_downloaded='Y'"
		+ " union all SELECT 0 AS policy_downloaded,0 AS proposal_downloaded,COUNT(*) AS policy_invoice_uploaded FROM firstgen_policy marutipoli0_"
		+ " WHERE TO_CHAR(to_date(marutipoli0_.policy_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND marutipoli0_.is_policy_uploaded='Y')";
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					CarPolicyStatus carPolicyStatus = new CarPolicyStatus();
					carPolicyStatus.setCarType(rs.getString(1));
					carPolicyStatus.setPolicyDownloaded(rs.getString(2));
					carPolicyStatus.setProposalDownloaded(rs.getString(3));
					carPolicyStatus.setPolicyUploaded(rs.getString(4));
					statusList.add(carPolicyStatus);
					carPolicyStatus = null;
				}
				if (rs != null)
					rs.close();
			
		logger.info("carPolicyChartStatusDetails - connection object created :: " + conn);
		}catch(Exception e){
			logger.error("Error in carPolicyChartStatusDetails ::" + e.getMessage(), e);
		}
		finally{
			if(conn != null)
			conn.close();
			if(ps!=null)
				ps.close();
		}
		return statusList;
	}
	
	
	
	private List<MonthWiseCountStatus> xgenGLChartStatusDetails(String year) throws SQLException {
		List<MonthWiseCountStatus> statusList = new ArrayList<>();
				
				String sql ="select  month_year , SUM(count) from (select TO_CHAR(B.DATE_FROM,'MM/YYYY') month_year, count(*) count from GL_BATCH_PROCESS B where TO_CHAR(B.DATE_FROM,'YYYY')='"+year+"' group by TO_CHAR(B.DATE_FROM,'MM/YYYY') union  Select (lpad(Rownum, 2, '0')||'/"+year+"') month_year ,  0 count  From dual Connect By Rownum <= 12 ) group by month_year order by  month_year";
				
				Connection conn = null;
				PreparedStatement ps = null;
				try{
				DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
				String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_URL);
				String username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_USERNAME);
				String password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_PASSWORD);
				conn = DriverManager.getConnection(connInstance, username, password);
				ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
						while (rs.next()) {
							MonthWiseCountStatus monthWiseCountStatus = new MonthWiseCountStatus();
							monthWiseCountStatus.setMonthYear(rs.getString(1));
							monthWiseCountStatus.setCount(rs.getString(2));
							statusList.add(monthWiseCountStatus);
							monthWiseCountStatus = null;
						}
						if (rs != null)
							rs.close();
					
				logger.info("xgenGLChartStatusDetails - connection object created :: " + conn);
				}catch(Exception e){
					logger.error("Error in xgenGLChartStatusDetails ::" + e.getMessage(), e);
				}
				finally{
					if(conn != null)
					conn.close();
					if(ps!=null)
						ps.close();
				}
				return statusList;
			}

	@Override
	public ChartMultiData getxgenGLStatusChartDetails(String year) {
		try{
			ChartMultiData chartData = new ChartMultiData();
			List<ChartMultiDataSet> chartDataSets = new ArrayList<ChartMultiDataSet>();
			List<String> labels = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");
			List<String> data1 = new ArrayList<String>();
			List<MonthWiseCountStatus> list = new ArrayList<MonthWiseCountStatus>();
			
			//labels.add("X-Gen");

			list = xgenGLChartStatusDetails(year);

			List<String> GLExtractioncount = new ArrayList<String>();

			for (MonthWiseCountStatus monthWiseCountStatus : list) {
				GLExtractioncount.add(monthWiseCountStatus.getCount());
			}
			int index = 0;
			for (int i = 0; i < GLExtractioncount.size(); i++) {
				data1.add(GLExtractioncount.get(i));
			}
			//List<String> hoverBackgroundColor1 = new ArrayList<String>();
			//hoverBackgroundColor1.add(color1);
			
			ChartMultiDataSet chartDataSet1 = new ChartMultiDataSet();
			String color1 = getBGColorCode(index);
			chartDataSet1.setBackgroundColor(color1);
			/*chartDataSet1.setLabel("Policy Downloaded");*/
			chartDataSet1.setLabel("XGEN");
			chartDataSet1.setData(data1);
			chartDataSets.add(chartDataSet1);
			
			chartData.setDatasets(chartDataSets);
			chartData.setLabels(labels);
			return chartData;
		} catch (Exception e) {
			logger.error("Error in getPOStatusChart of SupplierWiseMisServiceImpl is called ::" + e.getMessage(), e);
			return null;
		}
	}
	
	
	@Override
	public ChartMultiData gridAutomationChartStatus(String year) {
	try{
		ChartMultiData chartData = new ChartMultiData();
		List<ChartMultiDataSet> chartDataSets = new ArrayList<ChartMultiDataSet>();
		List<String> labels = new ArrayList<String>();
		List<String> data = new ArrayList<String>();
		List<String> data1 = new ArrayList<String>();
		List<String> data2 = new ArrayList<String>();
		/*List<String> data3 = new ArrayList<String>();*/
		List<GridAutomationStatus> list = new ArrayList<GridAutomationStatus>();

		list = GridAutomationChartStatusDetails(year);

		List<String> carType = new ArrayList<String>();
		List<String> totalFiles = new ArrayList<String>();
		List<String> successFiles = new ArrayList<String>();
		List<String> validaionFailedFiles = new ArrayList<String>();
		/*List<String> errorCount = new ArrayList<String>();*/

		for (GridAutomationStatus carPolicyStatus : list) {
			carType.add(carPolicyStatus.getFileType());
			totalFiles.add(carPolicyStatus.getTotalFiles());
			successFiles.add(carPolicyStatus.getSuccessFiles());
			validaionFailedFiles.add(carPolicyStatus.getValidationFailed());
			/*errorCount.add(carPolicyStatus.getErrorFiles());*/
		}
		int index = 0;
		for (int i = 0; i < carType.size(); i++) {
			data1.add(totalFiles.get(i));
			labels.add(carType.get(i));
		}
		//List<String> hoverBackgroundColor1 = new ArrayList<String>();
		//hoverBackgroundColor1.add(color1);
		
		ChartMultiDataSet chartDataSet1 = new ChartMultiDataSet();
		String color1 = getBGColorCode(index);
		chartDataSet1.setBackgroundColor(color1);
		/*chartDataSet1.setLabel("Policy Downloaded");*/
		chartDataSet1.setLabel("Total Files");
		chartDataSet1.setData(data1);
		chartDataSets.add(chartDataSet1);

		ChartMultiDataSet chartDataSet = new ChartMultiDataSet();
		for (int i = 0; i < successFiles.size(); i++) {
			data.add(successFiles.get(i));
		}
		String color = getBGColorCode(index+1);
		chartDataSet.setBackgroundColor(color);
		chartDataSet.setData(data);
		/*chartDataSet.setLabel("Proposal Downloaded");*/
		chartDataSet.setLabel("Success Files");
		chartDataSets.add(chartDataSet);

		ChartMultiDataSet chartDataSet2 = new ChartMultiDataSet();
		for (int i = 0; i < validaionFailedFiles.size(); i++) {
			data2.add(validaionFailedFiles.get(i));
		}
		String color2 = getBGColorCode(index+2);
		chartDataSet2.setBackgroundColor(color2);
		chartDataSet2.setData(data2);
		/*chartDataSet2.setLabel("Policy Uploaded");*/
		chartDataSet2.setLabel("Validation Failed");
		chartDataSets.add(chartDataSet2);
		
		/*ChartMultiDataSet chartDataSet3 = new ChartMultiDataSet();
		for (int i = 0; i < errorCount.size(); i++) {
			data3.add(errorCount.get(i));
		}
		String color3 = getBGColorCode(index+3);
		chartDataSet3.setBackgroundColor(color3);
		chartDataSet3.setData(data3);
		chartDataSet3.setLabel("Proposal Uploaded");
		chartDataSet3.setLabel("Error Files");
		chartDataSets.add(chartDataSet3);*/

		chartData.setDatasets(chartDataSets);
		chartData.setLabels(labels);
		return chartData;
	} catch (Exception e) {
		logger.error("Error in getPOStatusChart of SupplierWiseMisServiceImpl is called ::" + e.getMessage(), e);
		return null;
	}
	}
	
	public List<GridAutomationStatus> GridAutomationChartStatusDetails(String year) throws SQLException{
		List<GridAutomationStatus> statusList = new ArrayList<>();
		
		String sql ="SELECT 'Grid_Automation' AS File_type,SUM(total_count),SUM(success_count),SUM(validation_failed),SUM(error_count)FROM "
				+ "(SELECT COUNT(*) AS total_count,0 AS success_count,0 AS validation_failed,0 AS error_count FROM transaction_info a WHERE  a.process_name ='GridMasterUploadProcess' AND a. transaction_status IN ( 'Completed','Failed' ) and TO_CHAR(CAST(TRANSACTION_START_DATE AS DATE), 'yyyy')='"+year+"' "
				+ "UNION ALL "
				+ "SELECT 0 AS total_count,COUNT(*) AS success_count,0  AS validation_failed,0  AS error_count FROM transaction_info a WHERE  a.process_name  ='GridMasterUploadProcess' AND a. transaction_status = 'Completed' and TO_CHAR(CAST(TRANSACTION_START_DATE AS DATE), 'yyyy')='"+year+"' "
				+ " UNION ALL"
				+ " SELECT 0 AS total_count, 0  AS success_count, COUNT(*) AS validation_failed, 0  AS error_count FROM transaction_info a WHERE  a.process_name  ='GridMasterUploadProcess' AND a. transaction_status = 'Failed' and TO_CHAR(CAST(TRANSACTION_START_DATE AS DATE), 'yyyy')='"+year+"' "
				+ "UNION ALL"
				+ " SELECT 0 AS total_count,0  AS success_count,0  AS validation_failed,COUNT(*) AS error_count FROM transaction_info a WHERE  a.process_name ='GridMasterUploadProcess' AND a. transaction_status IN ( 'Error','VALIDATION' ) and TO_CHAR(CAST(TRANSACTION_START_DATE AS DATE), 'yyyy')='"+year+"' )"
				+ " UNION"
				+ " SELECT 'Grid with model Automation' AS File_type, SUM(total_count), SUM(success_count), SUM(validation_failed), SUM(error_count) FROM"
				+ " (SELECT COUNT(*) AS total_count,0  AS success_count,0  AS validation_failed,0  AS error_count FROM transaction_info a  WHERE  a.process_name ='GridWithModelSheetMasterUploadProcess' AND a. transaction_status IN ( 'Completed','Failed' ) and TO_CHAR(CAST(TRANSACTION_START_DATE AS DATE), 'yyyy')='"+year+"' "
				+ " UNION ALL"
				+ " SELECT 0 AS total_count, COUNT(*) AS success_count, 0  AS validation_failed, 0  AS error_count FROM transaction_info a WHERE  a.process_name  ='GridWithModelSheetMasterUploadProcess' AND a. transaction_status = 'Completed' and TO_CHAR(CAST(TRANSACTION_START_DATE AS DATE), 'yyyy')='"+year+"' "
				+ " UNION ALL"
				+ " SELECT 0 AS total_count, 0  AS success_count, COUNT(*) AS validation_failed, 0  AS error_count FROM transaction_info a WHERE  a.process_name  ='GridWithModelSheetMasterUploadProcess' AND a. transaction_status = 'Failed' and TO_CHAR(CAST(TRANSACTION_START_DATE AS DATE), 'yyyy')='"+year+"' "
				+ " UNION ALL SELECT 0 AS total_count, 0  AS success_count, 0  AS validation_failed, COUNT(*) AS error_count FROM transaction_info a WHERE  a.process_name ='GridWithModelSheetMasterUploadProcess' AND a. transaction_status IN ( 'Error','VALIDATION' ) and TO_CHAR(CAST(TRANSACTION_START_DATE AS DATE), 'yyyy')='"+year+"' ) A"; 
		
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = "",username="",password="";
		if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
			 connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_UAT_URL);
			 username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_UAT_USERNAME);
			 password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_UAT_PASSWORD); 
		}else{
			 connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_URL);
			 username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_USERNAME);
			 password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_PASSWORD);
		}
		
		conn = DriverManager.getConnection(connInstance, username, password);
		ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					GridAutomationStatus gridAutomationStatus = new GridAutomationStatus();
					gridAutomationStatus.setFileType(rs.getString(1));
					gridAutomationStatus.setTotalFiles(rs.getString(2));
					gridAutomationStatus.setSuccessFiles(rs.getString(3));
					gridAutomationStatus.setValidationFailed(rs.getString(4));
					gridAutomationStatus.setErrorFiles(rs.getString(5));
					statusList.add(gridAutomationStatus);
					gridAutomationStatus = null;
				}
				if (rs != null)
					rs.close();
			
		logger.info("GridAutomationChartStatusDetails - connection object created :: " + conn);
		}catch(Exception e){
			logger.error("Error in GridAutomationChartStatusDetails ::" + e.getMessage(), e);
		}
		finally{
			if(conn != null)
			conn.close();
			if(ps!=null)
				ps.close();
		}
		return statusList;
	}
	
	
	@Override
	public ChartMultiData getPolicyMailRetriggerStatusChartDetails(String year) {
		try{
			ChartMultiData chartData = new ChartMultiData();
			List<ChartMultiDataSet> chartDataSets = new ArrayList<ChartMultiDataSet>();
			/*List<String> labels = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");*/
			
			List<String> labels = Arrays.asList("0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23");
			List<String> data1 = new ArrayList<String>();
			List<MonthWiseCountStatus> list = new ArrayList<MonthWiseCountStatus>();
			
			//labels.add("X-Gen");

			list = getPolicyMailRetriggerStatusDetails(year);

			List<String> GLExtractioncount = new ArrayList<String>();

			for (MonthWiseCountStatus monthWiseCountStatus : list) {
				GLExtractioncount.add(monthWiseCountStatus.getCount());
			}
			int index = 0;
			for (int i = 0; i < GLExtractioncount.size(); i++) {
				data1.add(GLExtractioncount.get(i));
			}
			//List<String> hoverBackgroundColor1 = new ArrayList<String>();
			//hoverBackgroundColor1.add(color1);
			
			ChartMultiDataSet chartDataSet1 = new ChartMultiDataSet();
			String color1 = getBGColorCode(index);
			chartDataSet1.setBackgroundColor(color1);
			/*chartDataSet1.setLabel("Policy Downloaded");*/
			chartDataSet1.setLabel("Quote");
			chartDataSet1.setData(data1);
			chartDataSets.add(chartDataSet1);
			
			chartData.setDatasets(chartDataSets);
			chartData.setLabels(labels);
			return chartData;
		} catch (Exception e) {
			logger.error("Error in getPOStatusChart of SupplierWiseMisServiceImpl is called ::" + e.getMessage(), e);
			return null;
		}
	}
	
	private List<MonthWiseCountStatus> getPolicyMailRetriggerStatusDetails(String date) throws SQLException {
		List<MonthWiseCountStatus> statusList = new ArrayList<>();
				
				//String sql ="select  month_year , SUM(count) from (select to_char(created_time, 'MM/YYYY') month_year , SUM(quote_count) count from POLICY_PDF_MAIL_RETRIGGER where is_excel_uploaded='Y' group by to_char(created_time, 'MM/YYYY') UNION SELECT (lpad(Rownum, 2, '0') ||'/"+year+"') month_year , 0 COUNT FROM dual CONNECT BY Rownum <= 12 ) group by month_year order by month_year";
		String sql ="select  month_year , sum(count) from (select CAST (EXTRACT(HOUR FROM created_time) AS CHAR(2)) month_year, sum(to_number(quote_count)) count from POLICY_PDF_MAIL_RETRIGGER where is_excel_uploaded='Y' AND to_char(created_time, 'DD/MM/YYYY') ='"+date+"' group by EXTRACT(HOUR FROM created_time) UNION SELECT CAST(Rownum-1 AS CHAR(2)) month_year , to_number(0) COUNT FROM dual CONNECT BY Rownum <= 24 ) group by month_year order by to_number(month_year)";
				
				Connection conn = null;
				PreparedStatement ps = null;
				try{
				DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
				/*String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_URL);
				String username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_USERNAME);
				String password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_PASSWORD);*/
				
				String connInstance = "",username="",password="";
				if (Arrays.stream(environment.getActiveProfiles()).anyMatch(env -> (env.equalsIgnoreCase("prod")))) {
					 connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_UAT_URL);
					 username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_UAT_USERNAME);
					 password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_UAT_PASSWORD); 
				}else{
					 connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_URL);
					 username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_USERNAME);
					 password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_PASSWORD);
				}
				
				conn = DriverManager.getConnection(connInstance, username, password);
				ps = conn.prepareStatement(sql);
					ResultSet rs = ps.executeQuery();
						while (rs.next()) {
							MonthWiseCountStatus monthWiseCountStatus = new MonthWiseCountStatus();
							monthWiseCountStatus.setMonthYear(rs.getString(1));
							monthWiseCountStatus.setCount(rs.getString(2));
							statusList.add(monthWiseCountStatus);
							monthWiseCountStatus = null;
						}
						if (rs != null)
							rs.close();
					
				logger.info("getPolicyMailRetriggerStatusDetails - connection object created :: " + conn);
				}catch(Exception e){
					logger.error("Error in getPolicyMailRetriggerStatusDetails ::" + e.getMessage(), e);
				}
				finally{
					if(conn != null)
					conn.close();
					if(ps!=null)
						ps.close();
				}
				return statusList;
			}
	
	@Override
	public ChartMultiData virChartStatus(String year) {
	try{
		ChartMultiData chartData = new ChartMultiData();
		List<ChartMultiDataSet> chartDataSets = new ArrayList<ChartMultiDataSet>();
		List<String> labels = new ArrayList<String>();
		List<String> data = new ArrayList<String>();
		List<String> data1 = new ArrayList<String>();
		List<String> data2 = new ArrayList<String>();
		List<String> data3 = new ArrayList<String>();
		List<CarPolicyStatus> list = new ArrayList<CarPolicyStatus>();

		list = virChartStatusDetails(year);

		List<String> carType = new ArrayList<String>();
		List<String> policyDownloaded = new ArrayList<String>();
		List<String> policyUploaded = new ArrayList<String>();

		for (CarPolicyStatus carPolicyStatus : list) {
			carType.add(carPolicyStatus.getCarType());
			policyDownloaded.add(carPolicyStatus.getPolicyDownloaded());
			policyUploaded.add(carPolicyStatus.getPolicyUploaded());
		}
		int index = 0;
		for (int i = 0; i < carType.size(); i++) {
			data1.add(policyDownloaded.get(i));
			labels.add(carType.get(i));
		}
		//List<String> hoverBackgroundColor1 = new ArrayList<String>();
		//hoverBackgroundColor1.add(color1);
		
		ChartMultiDataSet chartDataSet1 = new ChartMultiDataSet();
		String color1 = getBGColorCode(index);
		chartDataSet1.setBackgroundColor(color1);
		/*chartDataSet1.setLabel("Policy Downloaded");*/
		chartDataSet1.setLabel("VIR Report Downloaded");
		chartDataSet1.setData(data1);
		chartDataSets.add(chartDataSet1);


		ChartMultiDataSet chartDataSet2 = new ChartMultiDataSet();
		for (int i = 0; i < policyUploaded.size(); i++) {
			data2.add(policyUploaded.get(i));
		}
		String color2 = getBGColorCode(index+2);
		chartDataSet2.setBackgroundColor(color2);
		chartDataSet2.setData(data2);
		/*chartDataSet2.setLabel("Policy Uploaded");*/
		chartDataSet2.setLabel("VIR Report Uploaded");
		chartDataSets.add(chartDataSet2);
		

		chartData.setDatasets(chartDataSets);
		chartData.setLabels(labels);
		return chartData;
	} catch (Exception e) {
		logger.error("Error in virChartStatus  ::" + e.getMessage(), e);
		return null;
	}
	}
	
	
	public List<CarPolicyStatus> virChartStatusDetails(String year) throws SQLException{
		List<CarPolicyStatus> statusList = new ArrayList<>();
		
		String sql ="select 'APP_VIR' car_type,sum(is_report_downloaded),sum(is_report_uploaded) from ("+
		"SELECT COUNT(*) AS is_report_downloaded,0 AS is_report_uploaded FROM APP_VIR WHERE TO_CHAR(to_date(inspection_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_report_downloaded='Y' "+
		"union all "+
		"SELECT 0 AS is_report_downloaded, COUNT(*) AS is_report_uploaded  FROM APP_VIR WHERE TO_CHAR(to_date(inspection_date, 'mm/dd/yyyy'), 'yyyy')='"+year+"' AND is_report_uploaded='Y' "+
		") ";
		
		Connection conn = null;
		PreparedStatement ps = null;
		try{
		DriverManager.registerDriver((Driver) (Class.forName("oracle.jdbc.driver.OracleDriver").newInstance()));
		String connInstance = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_URL);
		String username = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_USERNAME);
		String password = UtilityFile.getDatabaseProperty(RPAConstants.DATASOURCE_PASSWORD);
		conn = DriverManager.getConnection(connInstance, username, password);
		ps = conn.prepareStatement(sql);
			ResultSet rs = ps.executeQuery();
				while (rs.next()) {
					CarPolicyStatus carPolicyStatus = new CarPolicyStatus();
					carPolicyStatus.setCarType(rs.getString(1));
					carPolicyStatus.setPolicyDownloaded(rs.getString(2));
					carPolicyStatus.setPolicyUploaded(rs.getString(3));
					statusList.add(carPolicyStatus);
					carPolicyStatus = null;
				}
				if (rs != null)
					rs.close();
			
		logger.info("carPolicyChartStatusDetails - connection object created :: " + conn);
		}catch(Exception e){
			logger.error("Error in carPolicyChartStatusDetails ::" + e.getMessage(), e);
		}
		finally{
			if(conn != null)
			conn.close();
			if(ps!=null)
				ps.close();
		}
		return statusList;
	}
}
