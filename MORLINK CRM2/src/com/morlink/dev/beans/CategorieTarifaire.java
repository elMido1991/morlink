package com.morlink.dev.beans;

import java.io.Serializable;

public class CategorieTarifaire implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2242622915710769359L;
	private String idCategorieTarifaire;
	private String libelleCategorieTarifaire;

	public String getLibelleCategorieTarifaire() {
		return this.libelleCategorieTarifaire;
	}

	public void setLibelleCategorieTarifaire(String libelleCategorieTarifaire) {
		this.libelleCategorieTarifaire = libelleCategorieTarifaire;
	}

	public String getIdCategorieTarifaire()
	{
		return idCategorieTarifaire;
	}

	public void setIdCategorieTarifaire(String idCategorieTarifaire)
	{
		this.idCategorieTarifaire = idCategorieTarifaire;
	}

}