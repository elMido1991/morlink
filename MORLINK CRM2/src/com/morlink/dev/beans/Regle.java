package com.morlink.dev.beans;

import java.io.Serializable;

public class Regle implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7871134321036719551L;
	private String idRegle;
	private float prixRegle;
	private float qteMin;
	private float qteMax;
	private CategorieTarifaire categorieTarifaire;
	private Article article;
	private Client client;

	public float getQteMax() {
		return this.qteMax;
	}

	public void setQteMax(float qteMax) {
		this.qteMax = qteMax;
	}

	public float getQteMin() {
		return this.qteMin;
	}

	public void setQteMin(float qteMin) {
		this.qteMin = qteMin;
	}

	public String getIdRegle()
	{
		return idRegle;
	}

	public void setIdRegle(String idRegle)
	{
		this.idRegle = idRegle;
	}

	public float getPrixRegle()
	{
		return prixRegle;
	}

	public void setPrixRegle(float prixRegle)
	{
		this.prixRegle = prixRegle;
	}

	public CategorieTarifaire getCategorieTarifaire() {
		return categorieTarifaire;
	}

	public void setCategorieTarifaire(CategorieTarifaire categorieTarifaire) {
		this.categorieTarifaire = categorieTarifaire;
	}

	public Article getArticle() {
		return article;
	}

	public void setArticle(Article article) {
		this.article = article;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}
}