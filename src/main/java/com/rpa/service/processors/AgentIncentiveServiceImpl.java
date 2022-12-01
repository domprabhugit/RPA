package com.rpa.service.processors;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.rpa.model.processors.AgentAccessMaster;
import com.rpa.model.processors.AgentConfig;
import com.rpa.model.processors.AgentIncentiveModel;
import com.rpa.repository.processors.AgentAccessMasterRepository;
import com.rpa.repository.processors.AgentConfigRepository;
import com.rpa.repository.processors.AgentIncentiveRepository;
import com.rpa.repository.processors.AgentResponseRepository;
import com.rpa.repository.processors.AgentSlabMasterRepository;
import com.rpa.repository.processors.InHouseLeadsRepository;
import com.rpa.repository.processors.LeadTargetMasterRepository;
import com.rpa.repository.processors.PlSlabMasterRepository;
import com.rpa.repository.processors.TlInhouseSlabMasterRepository;
import com.rpa.repository.processors.TlSlabMasterRepository;
import com.rpa.model.processors.AgentResponse;
import com.rpa.model.processors.AgentSlabMaster;
import com.rpa.model.processors.InHouseLeads;
import com.rpa.model.processors.LeadTargetMaster;
import com.rpa.model.processors.PlSlabMaster;
import com.rpa.model.processors.TlInHouseSlabMaster;
import com.rpa.model.processors.TlSlabMaster;

@Service
public class AgentIncentiveServiceImpl implements AgentIncentiveService{
	
	@Autowired
	AgentIncentiveRepository agentIncentiveRepository;
	
	@Autowired
	AgentResponseRepository agentResponseRepository;
	
	@Autowired
	AgentConfigRepository agentConfigRepository;
	
	@Autowired
	InHouseLeadsRepository inHouseLeadsRepository;
	
	@Autowired
	AgentSlabMasterRepository agentSlabMasterRepository;
	
	@Autowired
	TlSlabMasterRepository tlSlabMasterRepository;
	
	@Autowired
	PlSlabMasterRepository plSlabMasterRepository;
	
	@Autowired
	TlInhouseSlabMasterRepository tlInhouseSlabMasterRepository;
	
	@Autowired
	AgentAccessMasterRepository agentAccessMasterRepository;
	
	@Autowired
	LeadTargetMasterRepository leadTargetMasterRepository;

	@Override
	public AgentIncentiveModel save(AgentIncentiveModel agentIncentiveModel) {
		return agentIncentiveRepository.save(agentIncentiveModel);
		
	}
	
	@Override
	public AgentResponse save(AgentResponse agentResponse) {
		return agentResponseRepository.save(agentResponse);
		
	}

	@Override
	public List<AgentResponse> getIncentiveReportDetails(String year,String month,String type) {
		return agentResponseRepository.getIncentiveReportDetails(month+"/"+year,type);
	}

	@Override
	public List<String> getCalcalculationCompletionStatus(String year) {
		return agentResponseRepository.getCalcalculationCompletionStatus("/"+year);
	}

	@Override
	public AgentIncentiveModel getAgentIncentiveStatus(long id) {
		return agentIncentiveRepository.getAgentIncentiveStatus(id);
	}

	@Override
	public List<AgentConfig> getPerPolicyDetails() {
		return agentConfigRepository.findAll();
	}

	@Override
	public List<InHouseLeads> getInHouseLeads() {
		return inHouseLeadsRepository.findAll();
	}
	
	@Override
	public List<AgentSlabMaster> getAgentSlab() {
		return agentSlabMasterRepository.findAll();
	}

	@Override
	public List<TlSlabMaster> getTlSlab() {
		return tlSlabMasterRepository.findAll();
	}

	@Override
	public List<PlSlabMaster> getPlSlab() {
		return plSlabMasterRepository.findAll();
	}

	@Override
	public List<TlInHouseSlabMaster> getTlInHouseSlab() {
		return tlInhouseSlabMasterRepository.findAll();
	}

	@Override
	public List<AgentAccessMaster> getAgentAccessLate() {
		return agentAccessMasterRepository.findAll();
	}

	@Override
	public List<LeadTargetMaster> getLeadTarget() {
		return leadTargetMasterRepository.findAll();
	}

	@Override
	public AgentConfig findByKey(String key) {
		return agentConfigRepository.findByKey(key);
	}

	@Override
	public AgentConfig save(AgentConfig newAgentConfig) {
		return agentConfigRepository.save(newAgentConfig);
	}

	@Override
	public AgentSlabMaster findBySlab(String odMinSlab, String odMaxSlab) {
		return agentSlabMasterRepository.findBySlab(Integer.valueOf(odMinSlab) ,Integer.valueOf(odMaxSlab));
	}

	@Override
	public AgentSlabMaster save(AgentSlabMaster newAgentSlabMaster) {
		return agentSlabMasterRepository.save(newAgentSlabMaster);
	}

	@Override
	public TlSlabMaster save(TlSlabMaster tlSlabMaster) {
		return tlSlabMasterRepository.save(tlSlabMaster);
	}

	@Override
	public TlSlabMaster findById(String tlSlabId) {
		return tlSlabMasterRepository.findById(Long.valueOf(tlSlabId));
	}
	
	@Override
	public PlSlabMaster findByPlId(String tlSlabId) {
		return plSlabMasterRepository.findById(Long.valueOf(tlSlabId));
	}

	@Override
	public Object save(PlSlabMaster plSlabMaster) {
		return plSlabMasterRepository.save(plSlabMaster);
	}

	@Override
	public TlInHouseSlabMaster findByTlHouseSlab(String odMinSlab, String odMaxSlab) {
		return tlInhouseSlabMasterRepository.findByTlHouseSlab(Integer.valueOf(odMinSlab) ,Integer.valueOf(odMaxSlab));
	}

	@Override
	public TlInHouseSlabMaster save(TlInHouseSlabMaster newTlInHouseSlabMaster) {
		return tlInhouseSlabMasterRepository.save(newTlInHouseSlabMaster);
	}

	@Override
	public TlInHouseSlabMaster findByTlHouseSlabId(String inHouseTlSlabId) {
		return tlInhouseSlabMasterRepository.findById(Long.valueOf(inHouseTlSlabId));
	}
	
	
}
