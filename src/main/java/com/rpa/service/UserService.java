package com.rpa.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.rpa.model.User;

public interface UserService {
    void save(User user);

    User findById(Long id);

    User findByUsername(String userName);
    
    List<User> getUserListByUserName(String username);

	List<User> getAllUsers();
    
	User insertUser(User user);
	
	User updateUser(User user);
	
	boolean deleteUser(ArrayList<BigDecimal> Ids);
	
	boolean blockUser(ArrayList<BigDecimal> Ids);
	
	boolean activateUser(ArrayList<BigDecimal> Ids);
	
    List<User> findByIntermediaryNoAndUsername(String intermediaryNo,String username);
    
    User findOneByIntermediaryNoAndUsername(String intermediaryNo,String username);

	boolean resetPassword(ArrayList<BigDecimal> decArray);

	User getById(String userId);

	String getNewPassword(String userName, String regEmail);
}
