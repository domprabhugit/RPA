package com.rpa.repository.processors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.AgentConfig;

public interface AgentConfigRepository extends JpaRepository<AgentConfig, Integer>  {

	@Query("SELECT s from AgentConfig s WHERE key=?1")
	AgentConfig findByKey(String key);

	/*@Query("SELECT s from AgentIncentiveModel s WHERE transactionRefNo=?1")
	AgentIncentiveModel getAgentIncentiveStatus(long id);*/
	


	
}
