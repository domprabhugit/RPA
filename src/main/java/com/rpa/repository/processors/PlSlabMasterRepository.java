package com.rpa.repository.processors;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.rpa.model.processors.PlSlabMaster;

public interface PlSlabMasterRepository extends JpaRepository<PlSlabMaster, Integer>  {

	@Query("select s from PlSlabMaster s where id=?1 ")
	PlSlabMaster findById(Long valueOf);

	
}
