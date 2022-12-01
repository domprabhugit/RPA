package com.rpa.repository.processors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.CommonReportTakenStatus;

public interface CommonReportTakenStatusRepository extends JpaRepository<CommonReportTakenStatus, Integer> {

	/*@Query("select to_char(TO_DATE(policyDate,'DD-MM-YY'),'MM/DD/YYYY') from MarutiPolicyTakenStatus where status='N' AND policyDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE+10 order by to_date(policyDate,'dd/mm/yyyy') ")*/
	@Query("select s from CommonReportTakenStatus s where s.autoinspektStatus='N' AND s.inspectionDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.inspectionDate,'dd/mm/yyyy') ")
	List<CommonReportTakenStatus> getPendingPolicyDateListAutoInspekt(String extractionStartDate);

	@Query("select s from CommonReportTakenStatus s where s.autoinspektStatus='N' AND s.inspectionDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.inspectionDate,'dd/mm/yyyy') ")
	List<CommonReportTakenStatus> getPendingBacklogPolicyDateListAutoInspekt(String startDate, String endDate);
	
	@Query("select s from CommonReportTakenStatus s where s.adroitStatus='N' AND s.inspectionDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.inspectionDate,'dd/mm/yyyy') ")
	List<CommonReportTakenStatus> getPendingPolicyDateListAdroit(String extractionStartDate);

	@Query("select s from CommonReportTakenStatus s where s.adroitStatus='N' AND s.inspectionDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.inspectionDate,'dd/mm/yyyy') ")
	List<CommonReportTakenStatus> getPendingBacklogPolicyDateListAdroit(String startDate, String endDate);
	
	@Query("select s from CommonReportTakenStatus s where s.virappStatus='N' AND s.inspectionDate BETWEEN to_date(?1,'dd/mm/yy') AND SYSDATE-2 order by to_date(s.inspectionDate,'dd/mm/yyyy') ")
	List<CommonReportTakenStatus> getPendingPolicyDateListVirApp(String extractionStartDate);

	@Query("select s from CommonReportTakenStatus s where s.virappStatus='N' AND s.inspectionDate BETWEEN to_date(?1,'dd/mm/yy') AND to_date(?2,'dd/mm/yy') order by to_date(s.inspectionDate,'dd/mm/yyyy') ")
	List<CommonReportTakenStatus> getPendingBacklogPolicyDateListVirApp(String startDate, String endDate);
}
