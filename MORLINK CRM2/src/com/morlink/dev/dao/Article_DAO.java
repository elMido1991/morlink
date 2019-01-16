package com.morlink.dev.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.axemble.vdoc.sdk.utils.Logger;
import com.morlink.dev.beans.Article;
import com.morlink.dev.beans.Catalogue;
import com.morlink.dev.servicesmetier.crm.MEtablissementDevis;

public class Article_DAO {
	private static final Logger LOGGER = Logger.getLogger(Article_DAO.class);

	public Article getArticle(String codeArticle) {
		Connection connectionSage = MEtablissementDevis.getConnectionSage();
		Connection connectionProcess = MEtablissementDevis.getConnection();
		Article article = new Article();
		String request = "select AR_Ref, AR_Design ,AR_PrixVen,AR_PrixAch from " + "F_ARTICLE where AR_Ref = ? ";
		try {

			PreparedStatement stx = connectionSage.prepareStatement(request);
			stx.setString(1, codeArticle);
			ResultSet rs = stx.executeQuery();
			while (rs.next()) {
				article.setReference(rs.getString(1));
				article.setDesignation(rs.getString(2));
				article.setPrixVenteHT(rs.getFloat(3));
				article.setPrixVenteTTC(rs.getFloat(3) + (rs.getFloat(3) * 0.2f));
				article.setPrixAchat(rs.getFloat(4));
			}

			request = "select catalogueArticle from CatalogueArticle where codeArticle = ? ";
			stx = connectionProcess.prepareStatement(request);
			stx.setString(1, codeArticle);
			rs = stx.executeQuery();
			Catalogue catalogue = new Catalogue();
			while (rs.next()) {
				catalogue.setCatalogueArticle(rs.getString(1));
				catalogue.setArticle(article);
				article.setCatalogue(catalogue);
			}
		} catch (Exception e) {
			LOGGER.error(e.getMessage());
		} finally {
			try {
				connectionSage.close();
				connectionProcess.close();
			} catch (SQLException e) {
				LOGGER.error(e.getMessage());
			}

		}
		return article;
	}

	public Article updateArticle(Article updatedArticle) {
		Connection connectionProcess = MEtablissementDevis.getConnection();
		PreparedStatement stx = null;
		Article article = new Article();
		try {

			String request = "update CatalogueArticle set catalogueArticle=?  where codeArticle = ? ";
			stx = connectionProcess.prepareStatement(request);
			stx.setString(1, updatedArticle.getCatalogue().getCatalogueArticle());
			stx.setString(2, updatedArticle.getReference());
			int resultat = stx.executeUpdate();
			if (resultat == 0) {
				request = "insert into CatalogueArticle(codeArticle,catalogueArticle) values(?,?) ";
				stx = connectionProcess.prepareStatement(request);
				stx.setString(1, updatedArticle.getReference());
				stx.setString(2, updatedArticle.getCatalogue().getCatalogueArticle());
				stx.execute();
			}
			stx.close();

		} catch (SQLException e) {
			LOGGER.error(e.getMessage());
		}
		
		return article;
	}

}
