package com.rpa.response;

import java.util.List;

public class ChartDataSet {
	private List<String> data;
	private List<String> backgroundColor;
	private List<String> hoverBackgroundColor;
	
	private String pointBorderWidth;
	private String pointHoverRadius;
	private String pointHoverBackgroundColor;
	private String pointHoverBorderColor;
	private String pointHoverBorderWidth;
	private String pointRadius;
	private String pointHitRadius;
	private String lineTension;

	public List<String> getData() {
		return data;
	}
	public void setData(List<String> data) {
		this.data = data;
	}
	public List<String> getBackgroundColor() {
		return backgroundColor;
	}
	public void setBackgroundColor(List<String> backgroundColor) {
		this.backgroundColor = backgroundColor;
	}
	public List<String> getHoverBackgroundColor() {
		return hoverBackgroundColor;
	}
	public void setHoverBackgroundColor(List<String> hoverBackgroundColor) {
		this.hoverBackgroundColor = hoverBackgroundColor;
	}
	
	// Line Chart
	public String getPointBorderWidth() {
		return pointBorderWidth;
	}
	public void setPointBorderWidth(String pointBorderWidth) {
		this.pointBorderWidth = pointBorderWidth;
	}
	public String getPointHoverRadius() {
		return pointHoverRadius;
	}
	public void setPointHoverRadius(String pointHoverRadius) {
		this.pointHoverRadius = pointHoverRadius;
	}
	public String getPointHoverBackgroundColor() {
		return pointHoverBackgroundColor;
	}
	public void setPointHoverBackgroundColor(String pointHoverBackgroundColor) {
		this.pointHoverBackgroundColor = pointHoverBackgroundColor;
	}
	public String getPointHoverBorderColor() {
		return pointHoverBorderColor;
	}
	public void setPointHoverBorderColor(String pointHoverBorderColor) {
		this.pointHoverBorderColor = pointHoverBorderColor;
	}
	public String getPointHoverBorderWidth() {
		return pointHoverBorderWidth;
	}
	public void setPointHoverBorderWidth(String pointHoverBorderWidth) {
		this.pointHoverBorderWidth = pointHoverBorderWidth;
	}
	public String getPointRadius() {
		return pointRadius;
	}
	public void setPointRadius(String pointRadius) {
		this.pointRadius = pointRadius;
	}
	public String getPointHitRadius() {
		return pointHitRadius;
	}
	public void setPointHitRadius(String pointHitRadius) {
		this.pointHitRadius = pointHitRadius;
	}
	
	public String getLineTension() {
		return lineTension;
	}
	public void setLineTension(String lineTension) {
		this.lineTension = lineTension;
	}
	
	
}
