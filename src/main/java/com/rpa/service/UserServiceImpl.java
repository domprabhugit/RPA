/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.rpa.constants.RPAConstants;
import com.rpa.model.User;
import com.rpa.repository.RoleRepository;
import com.rpa.repository.UserRepository;
import com.rpa.util.UtilityFile;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
	private EmailService emailService;

    @Override
    public void save(User user) {
        user.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));
        user.setRoles(new HashSet<>(roleRepository.findAll()));
        userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username);
    }
    
    @Override
	public List<User> getAllUsers() {
		return userRepository.getAllUsers();
	}

	@Override
	public User insertUser(User user) {
		return userRepository.save(user);
	}

	@Override
	public User updateUser(User user) {
		return userRepository.save(user);
	}

	@Override
	public boolean deleteUser(ArrayList<BigDecimal> Ids){
		boolean flag = false;
		for (BigDecimal Id : Ids) {
			userRepository.delete(Id.longValue());		
			flag = true;
		}
		return flag;
	}

	@Override
	public boolean blockUser(ArrayList<BigDecimal> Ids) {
		boolean flag = false;
		User user = null;
		for (BigDecimal Id : Ids) {
			user = userRepository.findOne(Id.longValue());
			user.setActive(RPAConstants.N);
			userRepository.save(user);		
			flag = true;
		}
		return flag;
	}
	
	@Override
	public boolean activateUser(ArrayList<BigDecimal> Ids) {
		boolean flag = false;
		User user = null;
		for (BigDecimal Id : Ids) {
			user = userRepository.findOne(Id.longValue());
			user.setActive(RPAConstants.Y);
			userRepository.save(user);		
			flag = true;
		}
		return flag;
	}

	@Override
	public List<User> findByIntermediaryNoAndUsername(String intermediaryNo, String username) {
		//return userRepository.findByIntermediaryNoAndUsername(intermediaryNo, username);
		return null;
	}

	@Override
	public User findOneByIntermediaryNoAndUsername(String intermediaryNo, String username) {
		//return userRepository.findOneByIntermediaryNoAndUsername(intermediaryNo, username);
		return null;
	}

	@Override
	public List<User> getUserListByUserName(String username) {
		return userRepository.getUserListByUserName(username);
	}
	
	@Override
	public boolean resetPassword(ArrayList<BigDecimal> Ids) {
		boolean flag = false;
		User userDB = null;
		for (BigDecimal Id : Ids) {
			System.out.println("s-->" + Id);
			// decArray.add( new BigDecimal(s) ); User userDB = new User();
			userDB = userRepository.findOne(Id.longValue());
			String firstPassword = UtilityFile.generateInitialPassword(8);
			System.out.println("firstPassword-->" + firstPassword);
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String hashedPassword = passwordEncoder.encode(firstPassword);
			System.out.println("hashedPassword-->" + hashedPassword);
			userDB.setFirstTimePassword("Y");
			userDB.setPassword(hashedPassword);
			userRepository.save(userDB);
			User newUser = new User();
			BeanUtils.copyProperties(userDB, newUser);
			if (newUser != null) {
				newUser.setPassword(firstPassword);
				emailService.sendEmailForUserCreation(newUser);
			}
			flag = true;

		}

		return flag;
	}

	@Override
	public User getById(String id) {
		return userRepository.findOne(Long.valueOf(id));
	}

	@Override
	public String getNewPassword(String userName, String regEmail) {
		List<User> users = findByUsernameAndMail(userName, regEmail);
		String result = "";
		if (users.size() > 0) {
			User userDB = userRepository.findByUsername(userName);

			String firstPassword = UtilityFile.generateInitialPassword(8);
			BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
			String hashedPassword = passwordEncoder.encode(firstPassword);
			userDB.setPassword(hashedPassword);
			userDB.setFirstTimePassword(RPAConstants.Y);
			userRepository.save(userDB);

			User newUser = new User();
			BeanUtils.copyProperties(userDB, newUser);
			newUser.setPassword(firstPassword);
			emailService.sendEmailForUserCreation(newUser);

			result = "Temporary password sent to ur email";
		} else {
			result = "Invalid username or email ";
		}
		
		return result;
	}

	private List<User> findByUsernameAndMail(String userName, String regEmail) {
		return userRepository.findByUsernameAndMail(userName, regEmail);
	}

	
	
	
}
