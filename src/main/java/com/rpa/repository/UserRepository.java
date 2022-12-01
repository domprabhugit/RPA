/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.User;

public interface UserRepository extends JpaRepository<User, Long> {
    User findById(Long id);
    
    User findByUsername(String username);
    
	@Query("SELECT u from User u WHERE u.username = ?1")
    List<User> getUserListByUserName(String username);
	
	@Query("SELECT s from User s WHERE s.active NOT IN ('S')")
	List<User> getAllUsers();

	@Query("SELECT s from User s WHERE s.username=?1 and s.emailId=?2 ")
	List<User> findByUsernameAndMail(String userName, String regEmail);
}
