package com.rpa.service.processors;

import java.util.List;

import com.rpa.model.processors.AgentAccessMaster;
import com.rpa.model.processors.AgentConfig;
import com.rpa.model.processors.AgentIncentiveModel;
import com.rpa.model.processors.AgentResponse;
import com.rpa.model.processors.AgentSlabMaster;
import com.rpa.model.processors.InHouseLeads;
import com.rpa.model.processors.LeadTargetMaster;
import com.rpa.model.processors.PlSlabMaster;
import com.rpa.model.processors.TlInHouseSlabMaster;
import com.rpa.model.processors.TlSlabMaster;

public interface AgentIncentiveService {

	AgentIncentiveModel save(AgentIncentiveModel agentIncentiveModel);

	AgentResponse save(AgentResponse agentResponse);

	List<AgentResponse> getIncentiveReportDetails(String year, String month, String type);

	List<String> getCalcalculationCompletionStatus(String year);

	AgentIncentiveModel getAgentIncentiveStatus(long parseLong);

	List<AgentConfig> getPerPolicyDetails();

	List<InHouseLeads> getInHouseLeads();

	List<AgentSlabMaster> getAgentSlab();

	List<TlSlabMaster> getTlSlab();

	List<PlSlabMaster> getPlSlab();

	List<TlInHouseSlabMaster> getTlInHouseSlab();

	List<AgentAccessMaster> getAgentAccessLate();

	List<LeadTargetMaster> getLeadTarget();

	AgentConfig findByKey(String key);

	AgentConfig save(AgentConfig newAgentConfig);

	AgentSlabMaster findBySlab(String odMinSlab, String odMaxSlab);

	AgentSlabMaster save(AgentSlabMaster newAgentSlabMaster);

	TlSlabMaster save(TlSlabMaster tlSlabMaster);

	TlSlabMaster findById(String tlSlabId);
	
	PlSlabMaster findByPlId(String plSlabId);

	Object save(PlSlabMaster plSlabMaster);

	TlInHouseSlabMaster findByTlHouseSlab(String odMinSlab, String odMaxSlab);

	TlInHouseSlabMaster save(TlInHouseSlabMaster newTlInHouseSlabMaster);

	TlInHouseSlabMaster findByTlHouseSlabId(String inHouseTlSlabId);


}
