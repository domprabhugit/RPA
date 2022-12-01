package com.rpa.repository.processors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.AgentIncentiveModel;

public interface AgentIncentiveRepository extends JpaRepository<AgentIncentiveModel, Integer>  {

	@Query("SELECT s from AgentIncentiveModel s WHERE transactionRefNo=?1")
	AgentIncentiveModel getAgentIncentiveStatus(long id);
	


	
}
