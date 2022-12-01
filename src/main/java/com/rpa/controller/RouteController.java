/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.camel.CamelContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rpa.messages.RPAMessages;
import com.rpa.model.ApplicationConfiguration;
import com.rpa.model.BusinessProcess;
import com.rpa.model.EmailConfiguration;
import com.rpa.service.CommonService;
import com.rpa.service.EmailService;
import com.rpa.service.process.ProcessService;
import com.rpa.util.UtilityFile;

@Controller
public class RouteController {

	private static final Logger logger = LoggerFactory.getLogger(RouteController.class.getName());

	@Autowired
	private CamelContext camelContext;

	@Autowired
	private CommonService commonService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private ProcessService processService;

	@GetMapping("/controlroute")
	public @ResponseBody String controlroute(HttpServletRequest req, HttpServletResponse resp, ModelMap model) {
		
		UtilityFile.printHeapDetails("controlling routes");

		String processStatus = RPAMessages.UNABLE_TO_PROCESS;

		String processState = req.getParameter("processState") == null ? "" : req.getParameter("processState");

		String processName = req.getParameter("processName") == null ? "" : req.getParameter("processName");

		logger.info("BEGIN controlroute()-Process State::"+processState+"::Process Name::"+processName);

		if("On".equals(processState)){			
			BusinessProcess businessProcess = processService.findByProcessName(processName);
			List<EmailConfiguration> emailConfiguration = emailService.findByProcessId(businessProcess.getId());
			List<ApplicationConfiguration> applicationConfiguration = processService.findByProcessId(businessProcess.getId());

			if(emailConfiguration.size()==0){
				processStatus = RPAMessages.EMAIL_NOT_CONFIGURED;
			} else if(applicationConfiguration.size()==0){
				processStatus = RPAMessages.APP_NOT_CONFIGURED;
			} else {
				processStatus = onStartProcess(processName);
			}
		} else if("Off".equals(processState)){
			processStatus = onStopProcess(processName);
		}

		commonService.getDashBoardDetails(model);
		model.addAttribute("processlist", commonService.getProcessDetails());

		logger.info("END controlroute()::"+processStatus);

		return processStatus;
	}

	public String onStartProcess(String processName) {

		String processStatus = RPAMessages.UNABLE_TO_PROCESS;

		if(!camelContext.getRouteStatus(processName).isStarted()){
			try {
				camelContext.startRoute(processName);
				processStatus = "Process started successfully.";
			} catch (Exception e) {
				logger.info("onStartProcess Exception Caught::", e);
			}
		} else {
			processStatus = "Process is already in running mode.";
		}
		return processStatus;
	}

	public String onStopProcess(String processName) {

		String processStatus = RPAMessages.UNABLE_TO_PROCESS;

		if(!camelContext.getRouteStatus(processName).isStopped()){
			try {
				camelContext.stopRoute(processName);
				processStatus = "Process stopped successfully.";
			} catch (Exception e) {
				logger.info("onStopProcess Exception Caught::", e);
			}
		} else {
			processStatus = "Process is already in stop mode.";
		}
		return processStatus;
	}
}