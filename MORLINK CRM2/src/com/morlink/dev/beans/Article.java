package com.morlink.dev.beans;

import java.io.Serializable;

public class Article implements Serializable{
	
	private static final long serialVersionUID = -3726277384067452376L;
	
	private String reference;
	private String designation;
	private float prixAchat;
	private float prixVenteTTC;
	private float prixVenteHT;
	private String statut;
	private Catalogue catalogue;

	public String getReference() {
		return this.reference;
	}

	public void setReference(String reference) {
		this.reference = reference;
	}

	public String getDesignation() {
		return this.designation;
	}

	public void setDesignation(String designation) {
		this.designation = designation;
	}

	public float getPrixAchat() {
		return this.prixAchat;
	}

	public void setPrixAchat(float prixAchat) {
		this.prixAchat = prixAchat;
	}

	public float getPrixVenteTTC() {
		return this.prixVenteTTC;
	}

	public void setPrixVenteTTC(float prixVenteTTC) {
		this.prixVenteTTC = prixVenteTTC;
	}

	public float getPrixVenteHT() {
		return this.prixVenteHT;
	}

	public void setPrixVenteHT(float prixVenteHT) {
		this.prixVenteHT = prixVenteHT;
	}


	public String getStatut()
	{
		return statut;
	}

	public void setStatut(String statut)
	{
		this.statut = statut;
	}
	
	@Override
	public String toString()
	{
		return this.designation;
	}

	public Catalogue getCatalogue() {
		return catalogue;
	}

	public void setCatalogue(Catalogue catalogue) {
		this.catalogue = catalogue;
	}
}