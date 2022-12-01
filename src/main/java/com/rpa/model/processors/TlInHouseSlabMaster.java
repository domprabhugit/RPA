package com.rpa.model.processors;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="TL_INHOUSE_SLAB_MASTER")
public class TlInHouseSlabMaster implements  Serializable {

	@Id
	@SequenceGenerator(name = "tl_inhouse_slab_master_seq", sequenceName = "tl_inhouse_slab_master_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="tl_inhouse_slab_master_seq")
	long id;
	
	String odMinSlab;
	String odMaxSlab;
	String target;
	String incentivePercentage;
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getOdMinSlab() {
		return odMinSlab;
	}
	public void setOdMinSlab(String odMinSlab) {
		this.odMinSlab = odMinSlab;
	}
	public String getOdMaxSlab() {
		return odMaxSlab;
	}
	public void setOdMaxSlab(String odMaxSlab) {
		this.odMaxSlab = odMaxSlab;
	}
	public String getTarget() {
		return target;
	}
	public void setTarget(String target) {
		this.target = target;
	}
	public String getIncentivePercentage() {
		return incentivePercentage;
	}
	public void setIncentivePercentage(String incentivePercentage) {
		this.incentivePercentage = incentivePercentage;
	}
	
	
	
	
}
