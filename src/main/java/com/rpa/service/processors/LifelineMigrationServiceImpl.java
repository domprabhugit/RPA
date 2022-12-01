/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.processors.LifeLineMigration;
import com.rpa.repository.processors.LifelineMigrationRepository;

@Service
public class LifelineMigrationServiceImpl implements LifelineMigrationService {
    
  @Autowired
   private LifelineMigrationRepository lifelineMigrationRepository;

@Override
public LifeLineMigration save(LifeLineMigration lifeLineMigration) {
	return lifelineMigrationRepository.save(lifeLineMigration);
}

@Override
public List<LifeLineMigration> findAll() {
	return lifelineMigrationRepository.findAll();
}

@Override
public List<LifeLineMigration> findByCountDateAndType(Date date,String appType) {
	return lifelineMigrationRepository.findByCountDateAndType(date,appType);
}

@Override
public List<Date> getUncomparedDates(Long id) {
	return lifelineMigrationRepository.getUncomparedDates(id);
}

@Override
public List<LifeLineMigration> getUncomparedListByDate(Date date) {
	return lifelineMigrationRepository.getUncomparedListByDate(date);
}

@Override
public List<LifeLineMigration> findByCountDate(Date date) {
	return lifelineMigrationRepository.findByCountDate(date);
}
	
}
