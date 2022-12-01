package com.rpa.model.processors;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="AGENT_SLAB_MASTER")
public class AgentSlabMaster implements  Serializable {

	@Id
	String odMinSlab;
	String odMaxSlab;
	String pgqPercentage;
	String wpiPercentage;
	String wsiPercentage;
	String inbPercentage;
	
	
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
	public String getPgqPercentage() {
		return pgqPercentage;
	}
	public void setPgqPercentage(String pgqPercentage) {
		this.pgqPercentage = pgqPercentage;
	}
	public String getWpiPercentage() {
		return wpiPercentage;
	}
	public void setWpiPercentage(String wpiPercentage) {
		this.wpiPercentage = wpiPercentage;
	}
	public String getInbPercentage() {
		return inbPercentage;
	}
	public void setInbPercentage(String inbPercentage) {
		this.inbPercentage = inbPercentage;
	}
	public String getWsiPercentage() {
		return wsiPercentage;
	}
	public void setWsiPercentage(String wsiPercentage) {
		this.wsiPercentage = wsiPercentage;
	}
	
	

	
}
