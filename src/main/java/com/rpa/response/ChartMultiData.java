package com.rpa.response;

import java.util.List;

public class ChartMultiData {
	private List<String> labels;
	private List<ChartMultiDataSet> datasets;

	public List<String> getLabels() {
		return labels;
	}
	public void setLabels(List<String> labels) {
		this.labels = labels;
	}
	public List<ChartMultiDataSet> getDatasets() {
		return datasets;
	}
	public void setDatasets(List<ChartMultiDataSet> datasets) {
		this.datasets = datasets;
	}

}
