package org.broadleafcommerce.promotion.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "PROMOTION")
public class Promotion implements Serializable {

	
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue
	@Column(name = "PROMOTION_ID")
	private Long id;
	
	@Column(name = "PROMOTION_TYPE")
	private String type;
	
	@Column(name = "PROMOTION_REFERENCE")
	private String reference;
	
	@Column(name = "PROMOTION_DISCOUNT")
	private double discount;
	
	@Column(name = "PROMOTION_USES")
	private int uses;
	
	@Column(name = "PROMOTIONS_APPLIED")
	private int applied;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getReference() {
		return reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public int getUses() {
		return uses;
	}

	public void setUses(int uses) {
		this.uses = uses;
	}

	public int getApplied() {
		return applied;
	}

	public void setApplied(int applied) {
		this.applied = applied;
	}
	
	
}
