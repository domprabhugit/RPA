/*
 * Robotic Process Automation
 * @originalAuthor Mohamed Ismaiel.S
 * Copyright(c) 2017, www.tekplay.com
 */

package com.rpa.service.processors;

import java.util.Date;
import java.util.List;

import com.rpa.model.processors.LifeLineMigration;

public interface LifelineMigrationService {
    LifeLineMigration save(LifeLineMigration lifeLineMigration);

    List<LifeLineMigration> findAll();

	List<LifeLineMigration> findByCountDateAndType(Date date, String appType);

	List<Date> getUncomparedDates(Long id);

	List<LifeLineMigration> getUncomparedListByDate(Date date);

	List<LifeLineMigration> findByCountDate(Date date);
}
