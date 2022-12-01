/*
 * Robotic Process Automation
 * @originalAuthor Vijayananth.m
 * Copyright(c) 2017, www.tekplay.com
 */
package com.rpa.camel.processors.common;

import java.util.HashMap;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rpa.util.UtilityFile;


@Component
public class ChromeDriverHeadless {

	//private static WebDriver webDriver;

	private static final Logger logger=LoggerFactory.getLogger(ChromeDriverHeadless.class.getName());

	private ChromeDriverHeadless()
	{

	}
	static{// chrome driver
		//webDriver=newHeadlessDriver();	
	}

	/*@Bean
	public static WebDriver getDriverInstance() throws UnsupportedEncodingException, URISyntaxException {
		return webDriver;
	}*/

	public static WebDriver getNewChromeDriver(){
		return newHeadlessDriver();
	}

	private static WebDriver newHeadlessDriver()
	{
		try
		{
			System.setProperty("webdriver.chrome.driver", UtilityFile.getCodeBasePath() +"/rpa_drivers/chrome/chromedriver.exe");

			String downloadFilePath=UtilityFile.getCodeBasePath() + UtilityFile.getBatchProperty("FirstGen.File.DownLoad.Location");

			logger.info("before Replacement"+downloadFilePath);
			downloadFilePath=downloadFilePath.replace("/", "\\");

			logger.info("after Replacement"+downloadFilePath);
			if(downloadFilePath.startsWith("\\"))
			{
				downloadFilePath=downloadFilePath.substring(1);
				logger.info("after removing the slash"+downloadFilePath);
			}

			logger.info("newHeadlessDriver -  final download path for  chrome:"+downloadFilePath);
			logger.info(downloadFilePath);
			ChromeDriverService driverService=ChromeDriverService.createDefaultService(); 
			ChromeOptions options=new ChromeOptions();
			//options.addArguments("--headless");
			options.addArguments("--disable-gpu");
			//options.addArguments("--start-maximized");
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-dev-shm-usage");

			HashMap<String,Object> chromepref=new HashMap<String,Object>();
			chromepref.put("profile.default_content_settings.popups", 0);

			chromepref.put("download.default_directory", downloadFilePath);
			chromepref.put("protocol_handler.excluded_schemes.pulsesecure",false);
			options.setExperimentalOption("prefs", chromepref);
			
			

			ChromeDriver  driver= new ChromeDriver(driverService,options);


			//below codes to enable downloading in headless chrome
			Map<String,Object> commandParams=new HashMap<>();
			commandParams.put("cmd", "Page.setDownloadBehavior");
			Map<String,String> params=new  HashMap<>();
			params.put("behavior", "allow");
			params.put("downloadPath",downloadFilePath);
			commandParams.put("params", params);
			ObjectMapper objectMapper=new ObjectMapper();
			HttpClient httpClient=HttpClientBuilder.create().build();
			String command=objectMapper.writeValueAsString(commandParams);
			String u=driverService.getUrl().toString()+"/session/"+driver.getSessionId()+"/chromium/send_command";

			HttpPost request=new HttpPost(u);
			request.addHeader("content-type","application/json");
			request.setEntity(new StringEntity(command));
			httpClient.execute(request);

			return driver;
		}
		catch(Exception e)
		{
			logger.error("Error in Headless Chrome Driver"+e.getMessage(),e);

			return new ChromeDriver();
		}
	}
	
	
	public static WebDriver getNewMarutiChromeDriver(){
		return newMarutiDriver();
	}

	private static WebDriver newMarutiDriver()
	{
		try
		{
			System.setProperty("webdriver.chrome.driver", UtilityFile.getCodeBasePath() +"/rpa_drivers/chrome/chromedriver.exe");

			String downloadFilePath=UtilityFile.getCodeBasePath() + UtilityFile.getBatchProperty("FirstGen.File.DownLoad.Location");

			logger.info("before Replacement"+downloadFilePath);
			downloadFilePath=downloadFilePath.replace("/", "\\");

			logger.info("after Replacement"+downloadFilePath);
			if(downloadFilePath.startsWith("\\"))
			{
				downloadFilePath=downloadFilePath.substring(1);
				logger.info("after removing the slash"+downloadFilePath);
			}

			logger.info("newHeadlessDriver -  final download path for  chrome:"+downloadFilePath);
			logger.info(downloadFilePath);
			ChromeDriverService driverService=ChromeDriverService.createDefaultService(); 
			ChromeOptions options=new ChromeOptions();
			//options.addArguments("--headless");
			//options.addArguments("--disable-gpu");
			//options.addArguments("--start-minimized");

			HashMap<String,Object> chromepref=new HashMap<String,Object>();
			chromepref.put("profile.default_content_settings.popups", 0);

			chromepref.put("download.default_directory", downloadFilePath);
			chromepref.put("protocol_handler.excluded_schemes.pulsesecure",false);
			options.setExperimentalOption("prefs", chromepref);
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-dev-shm-usage");			

			ChromeDriver  driver= new ChromeDriver(driverService,options);


			//below codes to enable downloading in headless chrome
			Map<String,Object> commandParams=new HashMap<>();
			commandParams.put("cmd", "Page.setDownloadBehavior");
			Map<String,String> params=new  HashMap<>();
			params.put("behavior", "allow");
			params.put("downloadPath",downloadFilePath);
			commandParams.put("params", params);
			ObjectMapper objectMapper=new ObjectMapper();
			HttpClient httpClient=HttpClientBuilder.create().build();
			String command=objectMapper.writeValueAsString(commandParams);
			String u=driverService.getUrl().toString()+"/session/"+driver.getSessionId()+"/chromium/send_command";

			HttpPost request=new HttpPost(u);
			request.addHeader("content-type","application/json");
			request.setEntity(new StringEntity(command));
			httpClient.execute(request);
			
			//driver.manage().window().setPosition(new Point(0, -1000));

			return driver;
		}
		catch(Exception e)
		{
			logger.error("Error in Headless Chrome Driver"+e.getMessage(),e);

			return new ChromeDriver();
		}
	}

	public WebDriver getNewChromeDriverWithCustomDownloadPath(String downloadFilePath) {
		return newHeadlessDriverWithCustomDownloadPath(downloadFilePath);
	}

	private static WebDriver newHeadlessDriverWithCustomDownloadPath(String downloadFilePath)
	{
		try
		{
			System.setProperty("webdriver.chrome.driver", UtilityFile.getCodeBasePath() +"/rpa_drivers/chrome/chromedriver.exe");

			//String downloadFilePath=UtilityFile.getCodeBasePath() + UtilityFile.getBatchProperty("FirstGen.File.DownLoad.Location");

			logger.info("before Replacement"+downloadFilePath);
			downloadFilePath=downloadFilePath.replace("/", "\\");

			logger.info("after Replacement"+downloadFilePath);
			if(downloadFilePath.startsWith("\\"))
			{
				downloadFilePath=downloadFilePath.substring(1);
				logger.info("getNewChromeDriverWithCustomDownloadPath - after removing the slash"+downloadFilePath);
			}

			logger.info(" final download path for  chrome:"+downloadFilePath);
			logger.info(downloadFilePath);
			ChromeDriverService driverService=ChromeDriverService.createDefaultService(); 
			ChromeOptions options=new ChromeOptions();
			//options.addArguments("--headless");
			//options.addArguments("--disable-gpu");
			//options.addArguments("--start-maximized");

			HashMap<String,Object> chromepref=new HashMap<String,Object>();
			chromepref.put("profile.default_content_settings.popups", 0);
			chromepref.put("plugins.always_open_pdf_externally", true);

			chromepref.put("download.default_directory", downloadFilePath);
			chromepref.put("protocol_handler.excluded_schemes.pulsesecure",false);
			chromepref.put("plugins.plugins_disabled", new String[] { "Chrome PDF Viewer", "Adobe Flash Player"});
			options.setExperimentalOption("prefs", chromepref);
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-dev-shm-usage");
			ChromeDriver  driver= new ChromeDriver(driverService,options);


			//below codes to enable downloading in headless chrome
			Map<String,Object> commandParams=new HashMap<>();
			commandParams.put("cmd", "Page.setDownloadBehavior");
			Map<String,String> params=new  HashMap<>();
			params.put("behavior", "allow");
			params.put("downloadPath",downloadFilePath);
			commandParams.put("params", params);
			ObjectMapper objectMapper=new ObjectMapper();
			HttpClient httpClient=HttpClientBuilder.create().build();
			String command=objectMapper.writeValueAsString(commandParams);
			String u=driverService.getUrl().toString()+"/session/"+driver.getSessionId()+"/chromium/send_command";

			HttpPost request=new HttpPost(u);
			request.addHeader("content-type","application/json");
			 //request.addHeader("content-type", "application/pdf");
			request.setEntity(new StringEntity(command));
			httpClient.execute(request);

			return driver;
		}
		catch(Exception e)
		{
			logger.error("Error in Headless Chrome Driver"+e.getMessage(),e);

			return new ChromeDriver();
		}
	}
	
	
	public WebDriver getNewHeadlessDriverWithPdfExternalOpen(String downloadFilePath) {
		return newHeadlessDriverWithPdfExternalOpen(downloadFilePath);
	}
	
	
	public static WebDriver newHeadlessDriverWithPdfExternalOpen(String downloadFilePath)
	{
		try
		{
			System.setProperty("webdriver.chrome.driver", UtilityFile.getCodeBasePath() +"/rpa_drivers/chrome/chromedriver.exe");

			logger.info("before Replacement"+downloadFilePath);
			downloadFilePath=downloadFilePath.replace("/", "\\");

			logger.info("after Replacement"+downloadFilePath);
			if(downloadFilePath.startsWith("\\"))
			{
				downloadFilePath=downloadFilePath.substring(1);
				logger.info("after removing the slash"+downloadFilePath);
			}

			logger.info("newHeadlessDriverWithPdfExternalOpen - final download path for  chrome:"+downloadFilePath);
			logger.info(downloadFilePath);
			ChromeDriverService driverService=ChromeDriverService.createDefaultService(); 
			ChromeOptions options=new ChromeOptions();
			//options.addArguments("--headless");
			options.addArguments("--disable-gpu");

			HashMap<String,Object> chromepref=new HashMap<String,Object>();
			chromepref.put("profile.default_content_settings.popups", 0);
			chromepref.put("plugins.always_open_pdf_externally", true);
			chromepref.put("plugins.plugins_disabled", new String[] { "Chrome PDF Viewer", "Adobe Flash Player"});

			chromepref.put("download.default_directory", downloadFilePath);
			chromepref.put("protocol_handler.excluded_schemes.pulsesecure",false);
			options.setExperimentalOption("prefs", chromepref);
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-dev-shm-usage");
			ChromeDriver  driver= new ChromeDriver(driverService,options);


			//below codes to enable downloading in headless chrome
			Map<String,Object> commandParams=new HashMap<>();
			commandParams.put("cmd", "Page.setDownloadBehavior");
			Map<String,String> params=new  HashMap<>();
			params.put("behavior", "allow");
			params.put("downloadPath",downloadFilePath);
			commandParams.put("params", params);
			ObjectMapper objectMapper=new ObjectMapper();
			HttpClient httpClient=HttpClientBuilder.create().build();
			String command=objectMapper.writeValueAsString(commandParams);
			String u=driverService.getUrl().toString()+"/session/"+driver.getSessionId()+"/chromium/send_command";

			HttpPost request=new HttpPost(u);
			request.addHeader("content-type","application/json");
			 //request.addHeader("content-type", "application/pdf");
			request.setEntity(new StringEntity(command));
			httpClient.execute(request);
			
			

			return driver;
		}
		catch(Exception e)
		{
			logger.error("Error in Headless Chrome Driver"+e.getMessage(),e);

			return new ChromeDriver();
		}
	}
	
	
	
	public WebDriver getNewHeadlessDriverWithWindowMaximized(String downloadFilePath) {
		return newHeadlessDriverWithWindowsMaximized(downloadFilePath);
	}
	
	
	public static WebDriver newHeadlessDriverWithWindowsMaximized(String downloadFilePath)
	{
		try
		{
			System.setProperty("webdriver.chrome.driver", UtilityFile.getCodeBasePath() +"/rpa_drivers/chrome/chromedriver.exe");

			logger.info("before Replacement"+downloadFilePath);
			downloadFilePath=downloadFilePath.replace("/", "\\");

			logger.info("after Replacement"+downloadFilePath);
			if(downloadFilePath.startsWith("\\"))
			{
				downloadFilePath=downloadFilePath.substring(1);
				logger.info("after removing the slash"+downloadFilePath);
			}

			logger.info("newHeadlessDriverWithPdfExternalOpen - final download path for  chrome:"+downloadFilePath);
			logger.info(downloadFilePath);
			ChromeDriverService driverService=ChromeDriverService.createDefaultService(); 
			ChromeOptions options=new ChromeOptions();
			//options.addArguments("--headless");
			options.addArguments("--disable-gpu");
			options.addArguments("--start-maximized");

			HashMap<String,Object> chromepref=new HashMap<String,Object>();
			chromepref.put("profile.default_content_settings.popups", 0);
			chromepref.put("plugins.always_open_pdf_externally", true);
			chromepref.put("plugins.plugins_disabled", new String[] { "Chrome PDF Viewer", "Adobe Flash Player"});

			chromepref.put("download.default_directory", downloadFilePath);
			chromepref.put("protocol_handler.excluded_schemes.pulsesecure",false);
			options.setExperimentalOption("prefs", chromepref);
			options.addArguments("--no-sandbox");
			options.addArguments("--disable-dev-shm-usage");
			ChromeDriver  driver= new ChromeDriver(driverService,options);


			//below codes to enable downloading in headless chrome
			Map<String,Object> commandParams=new HashMap<>();
			commandParams.put("cmd", "Page.setDownloadBehavior");
			Map<String,String> params=new  HashMap<>();
			params.put("behavior", "allow");
			params.put("downloadPath",downloadFilePath);
			commandParams.put("params", params);
			ObjectMapper objectMapper=new ObjectMapper();
			HttpClient httpClient=HttpClientBuilder.create().build();
			String command=objectMapper.writeValueAsString(commandParams);
			String u=driverService.getUrl().toString()+"/session/"+driver.getSessionId()+"/chromium/send_command";

			HttpPost request=new HttpPost(u);
			request.addHeader("content-type","application/json");
			 //request.addHeader("content-type", "application/pdf");
			request.setEntity(new StringEntity(command));
			httpClient.execute(request);
			
			

			return driver;
		}
		catch(Exception e)
		{
			logger.error("Error in Headless Chrome Driver"+e.getMessage(),e);

			return new ChromeDriver();
		}
	}
}