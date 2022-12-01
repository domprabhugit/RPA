package com.rpa.repository.processors;

import org.springframework.data.jpa.repository.JpaRepository;

import com.rpa.model.processors.AgentAccessMaster;
import com.rpa.model.processors.LeadTargetMaster;

public interface LeadTargetMasterRepository extends JpaRepository< LeadTargetMaster, Integer>  {

	
}
