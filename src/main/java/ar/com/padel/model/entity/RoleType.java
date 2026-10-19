package ar.com.padel.model.entity;

import java.time.LocalDateTime;

public class RoleType extends AuditInformation {
	
	private String LongDescription;
	private String ShortDescription;
	private Long Surrogate;	
	
	public RoleType(String aLongDescription, String aShortDescription, Long aSurrogate, LocalDateTime aCreatedDate, LocalDateTime anUpdatedDate, Employee aCreatedBy, Employee anUpdatedBy) {
		super(aCreatedDate, anUpdatedDate, aCreatedBy, anUpdatedBy);
		setLongDescription(aLongDescription);
		setShortDescription(aShortDescription);
		setSurrogate(aSurrogate);
	}
	
	public RoleType() {
		super();
	}
	
	//Getters
	public String getLongDescription() {
		return this.LongDescription;
	}
	
	public String getShortDescription() {
		return this.ShortDescription;
	}
	
	public Long getSurrogate() {
		return this.Surrogate;
	}
	
	@Override
	public String toString() {
	    return getLongDescription();
	}
	
	//Setters
	public void setLongDescription(String aDescription) {
		this.LongDescription = aDescription;
	}
	
	public void setShortDescription(String aDescription) {
		this.ShortDescription = aDescription;
	}
	
	public void setSurrogate(Long aSurrogate) {
		this.Surrogate = aSurrogate;
	}

}
