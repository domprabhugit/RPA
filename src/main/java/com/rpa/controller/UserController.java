/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.controller;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.rpa.constants.RPAConstants;
import com.rpa.model.BusinessProcess;
import com.rpa.model.Role;
import com.rpa.model.User;
import com.rpa.service.CommonService;
import com.rpa.service.EmailService;
import com.rpa.service.RoleService;
import com.rpa.service.SecurityService;
import com.rpa.service.UserService;
import com.rpa.service.process.ProcessService;
import com.rpa.util.UtilityFile;
import com.rpa.validator.UserValidator;

@Controller

public class UserController {

	private static final Logger logger = LoggerFactory.getLogger(UserController.class.getName());

	@Autowired
	private UserService userService;

	@Autowired
	private RoleService roleService;

	@Autowired
	private ProcessService processService;

	@Autowired
	private EmailService emailService;

	@Autowired
	private SecurityService securityService;

	@Autowired
	private CommonService commonService;

	@Autowired
	private UserValidator userValidator;

	@GetMapping("/login")
	public String login(Model model) {
		model.addAttribute("userForm", new User());
		return "login";
	}

	@PostMapping("/login")
	public String loginSubmit(@ModelAttribute User userForm, BindingResult bindingResult, ModelMap model) {
		userValidator.validate(userForm, bindingResult);

		if (bindingResult.hasErrors()) {
			return "login";
		}

		userService.save(userForm);

		securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());

		commonService.getDashBoardDetails(model);

		return "redirect:/dashboard";
	}

	@GetMapping("/registration")
	public String registration(Model model) {
		model.addAttribute("userForm", new User());
		return "registration";
	}

	@PostMapping("/registration")
	public String registration(@ModelAttribute("userForm") User userForm, BindingResult bindingResult, ModelMap model) {
		userValidator.validate(userForm, bindingResult);

		if (bindingResult.hasErrors()) {
			return "login";
		}

		userService.save(userForm);

		securityService.autologin(userForm.getUsername(), userForm.getPasswordConfirm());

		return "redirect:/dashboard";
	}

	@GetMapping("/users")
	public String users(ModelMap model) {
		User form = new User();
		model.addAttribute("userForm", form);	
		model.addAttribute("processlist", commonService.getProcessDetails());
		return "users";
	}

	@PostMapping("/getAllUsers")
	public @ResponseBody List<User> getAllUsers() {
		List<User> list = userService.getAllUsers();
		for(User user:list){
			Set<Role> roles = user.getRoles();
			for (Role role: roles) {
				user.setRoleName(role.getName());
			}
			if(RPAConstants.Y.equals(user.getActive())){
				user.setActive(RPAConstants.Active);
			} else {
				user.setActive(RPAConstants.Inactive);
			}
		}
		return list;
	}

	@PostMapping("/insertUser")
	public @ResponseBody String insertUser(HttpServletRequest req, HttpServletResponse resp, ModelMap model) throws ParseException{

		logger.info("BEGIN - insertUser()");

		String username = req.getParameter("username") == null ? "": req.getParameter("username");
		String emailId = req.getParameter("emailId") == null ? "" : req.getParameter("emailId");
		String phoneNo = req.getParameter("phoneNo") == null ? "": req.getParameter("phoneNo");
		String roles = req.getParameter("roleName") == null ? "": req.getParameter("roleName");
		String processes = req.getParameter("processName") == null ? "": req.getParameter("processName");

		List<User> duplicateUser = userService.getUserListByUserName(username);

		if(duplicateUser.size()>0){
			logger.info("END - insertUser() - Duplicate User::username-"+username);
			return RPAConstants.USER_ALREADY_REGISTERED;
		}

		User userDB = new User();
		userDB.setUsername(username);
		userDB.setEmailId(emailId);
		userDB.setPhoneNo(phoneNo);
		userDB.setRoles(new HashSet<>(roleService.findByName(roles)));
		userDB.setActive(RPAConstants.Y);

		if(processes.length()>0){
			String[] processList = processes.split(",");
			Set<BusinessProcess> processSet = new HashSet<BusinessProcess>();
			for(String processName : processList){
				BusinessProcess businessProcess = processService.findByProcessName(processName);
				if(businessProcess!=null){
					processSet.add(businessProcess);
				}
			}
			userDB.setBusinessProcesses(processSet);
		}

		String firstPassword = UtilityFile.generateInitialPassword(8);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String hashedPassword = passwordEncoder.encode(firstPassword);
		userDB.setPassword(hashedPassword);
		userDB.setFirstTimePassword(RPAConstants.Y);

		User user = userService.insertUser(userDB);

		if(user!=null){
			//Send Email to USER
			user.setPassword(firstPassword);
			emailService.sendEmailForUserCreation(user);

			logger.info("END - insertUser() - User Registered::username-"+username);
			return RPAConstants.USER_REGISTERED;
		}
		logger.info("END - insertUser() - Internal Error::username-"+username);

		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/updateUser")
	public @ResponseBody String updateUser(HttpServletRequest req, HttpServletResponse resp) throws ParseException{

		logger.info("BEGIN - updateUser()");

		String username = req.getParameter("username") == null ? "": req.getParameter("username");
		String emailId = req.getParameter("emailId") == null ? "" : req.getParameter("emailId");
		String phoneNo = req.getParameter("phoneNo") == null ? "": req.getParameter("phoneNo");
		String processes = req.getParameter("processName") == null ? "": req.getParameter("processName");

		List<User> duplicateUser = userService.getUserListByUserName(username);

		if(duplicateUser.size()==0){
			logger.info("END - updateUser() - User Not Exists::::username-"+username);
			return RPAConstants.USER_DOES_NOT_EXISTS;
		}

		User userDB = userService.findByUsername(username);

		if(!StringUtils.isEmpty(emailId)){
			userDB.setEmailId(emailId);
		}
		if(!StringUtils.isEmpty(phoneNo)){
			userDB.setPhoneNo(phoneNo);
		}

		String[] processList = processes.split(",");
		Set<BusinessProcess> processSet = new HashSet<>();
		for(String processName : processList){
			BusinessProcess businessProcess = processService.findByProcessName(processName);
			processSet.add(businessProcess);
		}
		userDB.setBusinessProcesses(processSet);

		User user = userService.updateUser(userDB);

		if(user!=null){
			logger.info("END - updateUser() - User Updated::::username-"+username);
			return RPAConstants.USER_UPDATED;
		}
		logger.info("END - updateUser() - Internal Error::::username-"+username);

		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/deleteUser")
	public @ResponseBody String deleteUser( HttpServletRequest req, HttpServletResponse resp) throws ParseException{

		logger.info("BEGIN - deleteUser()");

		String checked = req.getParameter("checked") == null ? "": req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for( String s : checked.split(",") ){
			decArray.add( new BigDecimal(s) );
			logger.info("deleteUser() - User Ids to be Deleted::"+s);
		}

		boolean flag = userService.deleteUser(decArray);

		if(flag){
			logger.info("END - deleteUser() - User Deleted");
			return RPAConstants.USER_DELETED;
		}
		logger.info("END - deleteUser() - Internal Error");

		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/blockUser")
	public @ResponseBody String blockUser( HttpServletRequest req, HttpServletResponse resp ) throws ParseException{

		logger.info("BEGIN - blockUser()");

		String checked = req.getParameter("checked") == null ? "": req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for( String s : checked.split(",") ){
			decArray.add( new BigDecimal(s) );
			logger.info("blockUser() - User Ids to be Blocked::"+s);
		}

		boolean flag = userService.blockUser(decArray);

		if(flag){
			logger.info("END - blockUser() - User Blocked");
			return RPAConstants.USER_BLOCKED;
		}
		logger.info("END - blockUser() - Internal Error");

		return RPAConstants.INTERNAL_ERROR;
	}

	@PostMapping("/activateUser")
	public @ResponseBody String activateUser( HttpServletRequest req, HttpServletResponse resp ) throws ParseException{

		logger.info("BEGIN - activateUser()");

		String checked = req.getParameter("checked") == null ? "": req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for( String s : checked.split(",") ){
			decArray.add( new BigDecimal(s) );
			logger.info("activateUser() - User Ids to be Activated::"+s);
		}

		boolean flag = userService.activateUser(decArray);

		if(flag){
			logger.info("END - activateUser() - User Activated");
			return RPAConstants.USER_ACTIVATED;
		}
		logger.info("END - activateUser() - Internal Error");

		return RPAConstants.INTERNAL_ERROR;
	}


	@GetMapping("/welcome")
	public String index(ModelMap model) {
		return "welcome";
	}

	@GetMapping("/covernote")
	public String covernote(ModelMap model) {
		return "welcome";
	}

	@GetMapping("/covernotes")
	public String covernotes(ModelMap model) {
		model.addAttribute("template", "covernotes");     
		return "index";
	}

	@GetMapping("/403")
	public String error403() {
		return "error/403";
	}

	@PostMapping("/viewUser")
	public @ResponseBody User viewUser(HttpServletRequest req, HttpServletResponse resp) {
		String id = req.getParameter("id") == null ? "": req.getParameter("id");
		User user = userService.findById(Long.parseLong(id));	
		Set<Role> roles = user.getRoles();
		String roleName = "";
		for (Role role: roles) {
			roleName = roleName + role.getName();
		}
		user.setRoleName(roleName);		
		Set<BusinessProcess> processes = user.getBusinessProcesses();
		String processName = "",comma=",";
		for (BusinessProcess process: processes) {
			processName = processName +comma+ process.getProcessName();
		}
		user.setProcessName(processName);		

		return user;
	}

	/*@PostMapping("/filterCoverNotes")
	public @ResponseBody List<CoverNote> filterCoverNotes(HttpServletRequest req, HttpServletResponse resp) throws ParseException{

		String intermediaryNo = req.getParameter("intermediaryNo") == null ? "": req.getParameter("intermediaryNo");
		String rsMode = req.getParameter("rsMode") == null ? "" : req.getParameter("rsMode");
		String action = req.getParameter("action") == null ? "": req.getParameter("action");
		String fromDate = req.getParameter("fromDate") == null ? "": req.getParameter("fromDate");
		String toDate = req.getParameter("toDate") == null ? "": req.getParameter("toDate");

		List<CoverNote> list = coverNoteService.filterCoverNotes(intermediaryNo,rsMode,action,fromDate,toDate);

		return list;
	}*/
	
	
	@PostMapping("/resetPassword")
	public @ResponseBody String resetPassword(HttpServletRequest req, HttpServletResponse resp) throws ParseException {

		logger.info("BEGIN - resetPassword()");

		String checked = req.getParameter("checked") == null ? "" : req.getParameter("checked");

		ArrayList<BigDecimal> decArray = new ArrayList<BigDecimal>();

		for (String s : checked.split(",")) {
			decArray.add(new BigDecimal(s));
			logger.info("resetPassword() - User Ids to be Resetted::" + s);
		}

		boolean flag = userService.resetPassword(decArray);

		if (flag) {
			logger.info("END - resetPassword() - User Resetted");
			return RPAConstants.USER_PASSWORD_RESET;
		}
		logger.info("END - resetPassword() - Internal Error");

		return RPAConstants.INTERNAL_ERROR;

	}
	
	@PostMapping("/changePassword")
	public @ResponseBody String changePassword(HttpServletRequest req, HttpServletResponse resp) throws ParseException {
		logger.info("BEGIN - changePassword()");

		String userId = req.getParameter("userId") == null ? "" : req.getParameter("userId");
		String password = req.getParameter("password") == null ? "" : req.getParameter("password");

		User userDB = new User();
		userDB = userService.getById(userId);
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String hashedPassword = passwordEncoder.encode(password);
		userDB.setPassword(hashedPassword);
		userDB.setFirstTimePassword(RPAConstants.N);
		User user = userService.updateUser(userDB);

		if (user != null) {
			logger.info("Password changed for " + user.getUsername());
			return RPAConstants.USER_PASSWORD_CHANGE;
		} else {
			logger.error("Error in changePassword() ");
			return RPAConstants.INTERNAL_ERROR;
		}
	}
	
	@GetMapping("/forgetPassword")
	public String forgetPassword() {
		return "forgetPassword";
	}
	
	@PostMapping("/getNewPassword")
	public @ResponseBody String getNewPassword(HttpServletRequest req, HttpServletResponse resp) {
		logger.info("BEGIN - getNewPassword()");
		String userName = req.getParameter("username") == null ? "" : req.getParameter("username");
		String regEmail = req.getParameter("email") == null ? "" : req.getParameter("email");

		String result = userService.getNewPassword(userName, regEmail);

		logger.info("END - getNewPassword()");
		return result;
	}
}
