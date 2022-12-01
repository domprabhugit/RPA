package com.rpa.service;

import com.rpa.response.ChartMultiData;

public interface DashboardService {
	
	public ChartMultiData carPolicyChartStatus(String year);

	public ChartMultiData xgenPolicyChartStatus(String year);

	public ChartMultiData getxgenGLStatusChartDetails(String year);
	
	public ChartMultiData gridAutomationChartStatus(String year);
	
	public ChartMultiData getPolicyMailRetriggerStatusChartDetails(String year);

	ChartMultiData virChartStatus(String year);

}
