package com.rpa.repository.processors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.TlInHouseSlabMaster;

public interface TlInhouseSlabMasterRepository extends JpaRepository<TlInHouseSlabMaster, Integer>  {

	@Query(value="select * from tl_inhouse_slab_master where (to_number(?1) between od_min_slab and OD_MAX_SLAB) or (to_number(?2) between od_min_slab and OD_MAX_SLAB)  ", nativeQuery = true)
	TlInHouseSlabMaster findByTlHouseSlab(Integer valueOf, Integer valueOf2);

	@Query("select s from TlInHouseSlabMaster s where id=?1 ")
	TlInHouseSlabMaster findById(Long valueOf);

	
}
