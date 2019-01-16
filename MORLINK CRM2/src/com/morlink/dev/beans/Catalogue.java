package com.morlink.dev.beans;

import java.io.Serializable;

public class Catalogue implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String catalogueArticle;
	private Article article;
	public String getCatalogueArticle() {
		return catalogueArticle;
	}
	public void setCatalogueArticle(String catalogueArticle) {
		this.catalogueArticle = catalogueArticle;
	}
	public Article getArticle() {
		return article;
	}
	public void setArticle(Article article) {
		this.article = article;
	}

	
}