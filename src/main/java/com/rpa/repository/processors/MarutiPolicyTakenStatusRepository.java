package com.rpa.repository.processors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.MarutiPolicyTakenStatus;

public interface MarutiPolicyTakenStatusRepository extends JpaRepository<MarutiPolicyTakenStatus, Integer> {

	/*@Query("select to_char(TO_DATE(policyDate,'DD-MM-YY'),'MM/DD/YYYY') from MarutiPolicyTakenStatus where status='N' AND policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE+10 order by to_date(policyDate,'dd/mm/yyyy') ")*/
	@Query("select s from MarutiPolicyTakenStatus s where s.status='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-1 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<MarutiPolicyTakenStatus> getPendingPolicyDateList(String extractionStartDate);

	@Query("select s from MarutiPolicyTakenStatus s where s.status='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<MarutiPolicyTakenStatus> getPendingBacklogPolicyDateList(String startDate, String endDate);
	
	
}
