package com.rpa.repository.processors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.AgentSlabMaster;

public interface AgentSlabMasterRepository extends JpaRepository<AgentSlabMaster, Integer>  {

	@Query(value="select * from agent_slab_master where (to_number(?1) between od_min_slab and OD_MAX_SLAB) or (to_number(?2) between od_min_slab and OD_MAX_SLAB)  ", nativeQuery = true)
	AgentSlabMaster findBySlab(Integer odMinSlab, Integer odMaxSlab);

	
}
