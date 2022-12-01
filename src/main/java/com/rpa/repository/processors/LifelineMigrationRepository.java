package com.rpa.repository.processors;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.LifeLineMigration;


public interface LifelineMigrationRepository  extends JpaRepository<LifeLineMigration, Integer>{

	List<LifeLineMigration> findAll();

	@Query("SELECT s from LifeLineMigration s WHERE s.countDate=?1 and appType=?2 ")
	List<LifeLineMigration> findByCountDateAndType(Date date, String appType);

	@Query("SELECT distinct s.countDate from LifeLineMigration s WHERE s.isCompared in ('N') and transactionInfoId=?1 ")
	List<Date> getUncomparedDates(Long id);

	@Query("SELECT s from LifeLineMigration s WHERE s.countDate=?1 order by s.appType ")
	List<LifeLineMigration> getUncomparedListByDate(Date date);

	List<LifeLineMigration> findByCountDate(Date date);
}
