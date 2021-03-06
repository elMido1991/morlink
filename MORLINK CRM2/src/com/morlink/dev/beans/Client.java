package com.morlink.dev.beans;

import java.io.Serializable;

public class Client implements Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = -8468002725746109760L;
	private String ncompte;
	private String intitule;
	private String contact;
	private String adresse;
	private String codePostal;
	private String ville;
	private String pays;
	private String telephone;
	private String telecopie;
	private String type;
	private float  montantEncours;
	private String email;
	private String numMP;
	
	
	public String getNumMP() {
		return numMP;
	}

	public void setNumMP(String numMP) {
		this.numMP = numMP;
	}

	public String getIntituleMP() {
		return intituleMP;
	}

	public void setIntituleMP(String intituleMP) {
		this.intituleMP = intituleMP;
	}

	private String intituleMP;

	public String getNcompte() {
		return this.ncompte;
	}

	public void setNcompte(String ncompte) {
		this.ncompte = ncompte;
	}

	public String getIntitule() {
		return this.intitule;
	}

	public void setIntitule(String intitule) {
		this.intitule = intitule;
	}

	public String getContact() {
		return this.contact;
	}

	public void setContact(String contact) {
		this.contact = contact;
	}

	public String getAdresse() {
		return this.adresse;
	}

	public void setAdresse(String adresse) {
		this.adresse = adresse;
	}

	public String getCodePostal() {
		return this.codePostal;
	}

	public void setCodePostal(String codePostal) {
		this.codePostal = codePostal;
	}

	public String getVille() {
		return this.ville;
	}

	public void setVille(String ville) {
		this.ville = ville;
	}

	public String getPays() {
		return this.pays;
	}

	public void setPays(String pays) {
		this.pays = pays;
	}

	public String getTelephone() {
		return this.telephone;
	}

	public void setTelephone(String telephone) {
		this.telephone = telephone;
	}

	public String getTelecopie() {
		return this.telecopie;
	}

	public void setTelecopie(String telecopie) {
		this.telecopie = telecopie;
	}

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}
	
	@Override
	public String toString()
	{

		return this.intitule;
	}

	public float getMontantEncours() {
		return montantEncours;
	}

	public void setMontantEncours(float montantEncours) {
		this.montantEncours = montantEncours;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
	
	
	
}