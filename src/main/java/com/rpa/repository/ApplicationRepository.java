/*
 * Robotic Process Automation
 * @originalAuthor Dominic D Prabhu
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rpa.model.ApplicationMaster;

public interface ApplicationRepository extends JpaRepository<ApplicationMaster, Long>{

}
