package com.rpa.repository.processors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.TlSlabMaster;

public interface TlSlabMasterRepository extends JpaRepository<TlSlabMaster, Integer>  {

	@Query("select s from TlSlabMaster s where id=?1 ")
	TlSlabMaster findById(Long valueOf);

	
}
