package com.rpa.repository.processors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.CommonPolicyTakenStatus;

public interface CommonPolicyTakenStatusRepository extends JpaRepository<CommonPolicyTakenStatus, Integer> {

	/*@Query("select to_char(TO_DATE(policyDate,'DD-MM-YY'),'MM/DD/YYYY') from MarutiPolicyTakenStatus where status='N' AND policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE+10 order by to_date(policyDate,'dd/mm/yyyy') ")*/
	@Query("select s from CommonPolicyTakenStatus s where s.hondaStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingPolicyDateListHonda(String extractionStartDate);

	@Query("select s from CommonPolicyTakenStatus s where s.hondaStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListHonda(String startDate, String endDate);
	
	@Query("select s from CommonPolicyTakenStatus s where s.fordStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingPolicyDateListFord(String extractionStartDate);

	@Query("select s from CommonPolicyTakenStatus s where s.fordStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListFord(String startDate, String endDate);
	
	@Query("select s from CommonPolicyTakenStatus s where s.tataPvStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingPolicyDateListTataPV(String extractionStartDate);

	@Query("select s from CommonPolicyTakenStatus s where s.tataPvStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListTataPV(String startDate, String endDate);
	
	@Query("select s from CommonPolicyTakenStatus s where s.tataCvStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingPolicyDateListTataCV(String extractionStartDate);

	@Query("select s from CommonPolicyTakenStatus s where s.tataCvStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListTataCV(String startDate, String endDate);
	
	@Query("select s from CommonPolicyTakenStatus s where s.abiblStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingPolicyDateListAbibl(String extractionStartDate);

	@Query("select s from CommonPolicyTakenStatus s where s.abiblStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListAbibl(String startDate, String endDate);
	
	@Query("select s from CommonPolicyTakenStatus s where s.miblStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingPolicyDateListMibl(String extractionStartDate);

	@Query("select s from CommonPolicyTakenStatus s where s.miblStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListMibl(String startDate, String endDate);
	
	@Query("select s from CommonPolicyTakenStatus s where s.volvoStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingPolicyDateListVolvo(String extractionStartDate);

	@Query("select s from CommonPolicyTakenStatus s where s.volvoStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListVolvo(String startDate, String endDate);
	
	@Query("select s from CommonPolicyTakenStatus s where s.tafeStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingPolicyDateListTafe(String extractionStartDate);

	@Query("select s from CommonPolicyTakenStatus s where s.tafeStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListTafe(String startDate, String endDate);
	
	@Query("select s from CommonPolicyTakenStatus s where s.piaggioStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingPolicyDateListPiaggio(String extractionStartDate);

	@Query("select s from CommonPolicyTakenStatus s where s.piaggioStatus='N' AND s.policyDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.policyDate,'dd/mm/yyyy') ")
	List<CommonPolicyTakenStatus> getPendingBacklogPolicyDateListPiaggio(String startDate, String endDate);
}
