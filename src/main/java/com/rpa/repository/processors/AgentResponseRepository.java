package com.rpa.repository.processors;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.AgentResponse;

public interface AgentResponseRepository extends JpaRepository<AgentResponse, Integer>  {

	@Query("select s from AgentResponse s where monthYear=?1 and flagType=?2")
	List<AgentResponse> getIncentiveReportDetails(String monthYear, String type);

	@Query(value="select distinct MONTH_YEAR from rpa_admin.agent_response where MONTH_YEAR like (%?1)", nativeQuery = true)
	List<String> getCalcalculationCompletionStatus(String year);
	


	
}
