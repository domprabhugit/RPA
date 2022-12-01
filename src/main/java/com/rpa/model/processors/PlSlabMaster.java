package com.rpa.model.processors;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name="PL_SLAB_MASTER")
public class PlSlabMaster implements  Serializable {

	@Id
	@SequenceGenerator(name = "pl_slab_master_seq", sequenceName = "pl_slab_master_seq", allocationSize=1)
	@GeneratedValue(strategy=GenerationType.SEQUENCE, generator="pl_slab_master_seq")
	long id;
	
	String odMinSlab;
	String odMaxSlab;
	String target;
	String percentage_70_To_85;
	String percentage_85_To_90;
	String percentage_90_To_95;
	String percentage_95_To_100;
	String percentage_100_Above;
	
	public long getId() {
		return id;
	}
	public void setId(long id) {
		this.id = id;
	}
	public String getOdMinSlab() {
		return odMinSlab;
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
	public String getPercentage_70_To_85() {
		return percentage_70_To_85;
	}
	public void setPercentage_70_To_85(String percentage_70_To_85) {
		this.percentage_70_To_85 = percentage_70_To_85;
	}
	public String getPercenatge_85_To_90() {
		return percentage_85_To_90;
	}
	public void setPercenatge_85_To_90(String percentage_85_To_90) {
		this.percentage_85_To_90 = percentage_85_To_90;
	}
	public String getPercenatge_90_To_95() {
		return percentage_90_To_95;
	}
	public void setPercenatge_90_To_95(String percentage_90_To_95) {
		this.percentage_90_To_95 = percentage_90_To_95;
	}
	public String getPercenatge_95_To_100() {
		return percentage_95_To_100;
	}
	public void setPercenatge_95_To_100(String percentage_95_To_100) {
		this.percentage_95_To_100 = percentage_95_To_100;
	}
	public String getPercenatge_100_Above() {
		return percentage_100_Above;
	}
	public void setPercenatge_100_Above(String percentage_100_Above) {
		this.percentage_100_Above = percentage_100_Above;
	}
	public void setOdMinSlab(String odMinSlab) {
		this.odMinSlab = odMinSlab;
	}

	
	
}
