package com.morlink.dev.beans;

import java.io.Serializable;

public class LigneDevis implements Serializable{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -4733125020396162836L;
	private String nLigneDevis;
	private String codeArticle;
	private String designationArticle;
	private float qte;
	private float qtedispo;
	private float qtelivre;
	private float qtealivre;
	private float qtereste;
	private float qteAffecte;
	private float prixtarifCommercial;
	private float prixtarifADV;
	private float prixpardefaut;
	private boolean avecCatalogue;
	private boolean statut;
	
	private Devis devis;

	public String getCodeArticle() {
		return this.codeArticle;
	}

	public void setCodeArticle(String codeArticle) {
		this.codeArticle = codeArticle;
	}

	public float getQte() {
		return this.qte;
	}

	public void setQte(float qte) {
		this.qte = qte;
	}

	

	public float getPrixpardefaut() {
		return this.prixpardefaut;
	}

	public void setPrixpardefaut(float prixpardefaut) {
		this.prixpardefaut = prixpardefaut;
	}

	public String getnLigneDevis()
	{
		return nLigneDevis;
	}

	public void setnLigneDevis(String nLigneDevis)
	{
		this.nLigneDevis = nLigneDevis;
	}

	public Devis getDevis()
	{
		return devis;
	}

	public void setDevis(Devis devis)
	{
		this.devis = devis;
	}

	public String getDesignationArticle()
	{
		return designationArticle;
	}

	public void setDesignationArticle(String designationArticle)
	{
		this.designationArticle = designationArticle;
	}

	public boolean isAvecCatalogue()
	{
		return avecCatalogue;
	}

	public void setAvecCatalogue(boolean avecCatalogue)
	{
		this.avecCatalogue = avecCatalogue;
	}

	public float getPrixtarifADV()
	{
		return prixtarifADV;
	}

	public void setPrixtarifADV(float prixtarifADV)
	{
		this.prixtarifADV = prixtarifADV;
	}

	public float getPrixtarifCommercial()
	{
		return prixtarifCommercial;
	}

	public void setPrixtarifCommercial(float prixtarifCommercial)
	{
		this.prixtarifCommercial = prixtarifCommercial;
	}

	public float getQtedispo() {
		return qtedispo;
	}

	public void setQtedispo(float qtedispo) {
		this.qtedispo = qtedispo;
	}

	public float getQtelivre() {
		return qtelivre;
	}

	public void setQtelivre(float qtelivre) {
		this.qtelivre = qtelivre;
	}

	public float getQtereste() {
		return qtereste;
	}

	public void setQtereste(float qtereste) {
		this.qtereste = qtereste;
	}

	public float getQtealivre() {
		return qtealivre;
	}

	public void setQtealivre(float qtealivre) {
		this.qtealivre = qtealivre;
	}

	public float getQteAffecte() {
		return qteAffecte;
	}

	public void setQteAffecte(float qteAffecte) {
		this.qteAffecte = qteAffecte;
	}

	public boolean isStatut() {
		return statut;
	}

	public void setStatut(boolean statut) {
		this.statut = statut;
	}
}