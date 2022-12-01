package com.rpa.response;

import java.util.List;

public class ChartData {
	private List<String> labels;
	private List<ChartDataSet> datasets;

	
	public List<String> getLabels() {
		return labels;
	}
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	public List<ChartDataSet> getDatasets() {
		return datasets;
	}
	public void setDatasets(List<ChartDataSet> datasets) {
		this.datasets = datasets;
	}

}
