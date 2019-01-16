package com.morlink.dev.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.morlink.dev.beans.Article;
import com.morlink.dev.beans.Client;
import com.morlink.dev.beans.LigneDevis;
import com.morlink.dev.beans.LigneLivraison;
import com.morlink.dev.beans.Livraison;
import com.morlink.dev.servicesmetier.crm.MEtablissementDevis;

public class Livraison_DAO implements ILivraison_DAO {
	
	private Connection connection;
	private PreparedStatement st; 
	
	
	public Livraison_DAO()
	{
		// TODO Auto-generated constructor stub
		try
		{
			connection = MEtablissementDevis.getConnection();
		}
		catch (Exception e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	
	public void addLivraison(Livraison livraison) {
		// TODO Auto-generated method stub
		String request = "INSERT INTO Livraison(codeLivraison,dateLivraison,dateRetourPrevu,dateRetourReelle,type,codeClient,intituleClient)"
				+ "VALUES(?,?,?,?,?,?,?)";
		try
		{
			st =connection.prepareStatement(request);
			st.setString(1, livraison.getCodeLivraison());
			st.setDate(2,livraison.getDateLivraison()!=null ? new java.sql.Date( livraison.getDateLivraison().getTime()):null);
			st.setDate(3,livraison.getDateRetourPrevu()!=null ? new java.sql.Date( livraison.getDateRetourPrevu().getTime()):null);
			st.setDate(4,livraison.getDateRetourReel()!=null ? new java.sql.Date( livraison.getDateRetourReel().getTime()):null);
			st.setString(5, livraison.getType());
			st.setString(6, livraison.getClient().getNcompte());
			st.setString(7, livraison.getClient().getIntitule());
			st.execute();
			
			
			String request2 = "INSERT INTO LigneLivraison(codeArticle,designationArticle,quantiteLivrer,codeLivraison)"
					+ "VALUES(?,?,?,?)";
			st =connection.prepareStatement(request2);
			for(LigneLivraison ligneLivraison : livraison.getLigneslivraison()){
				st.setString(1, ligneLivraison.getArticle().getReference());
				st.setString(2, ligneLivraison.getArticle().getDesignation());
				st.setFloat(3, ligneLivraison.getQuantite());
				st.setString(4, ligneLivraison.getLivraison().getCodeLivraison());
				st.execute();
			}
			
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			try
			{
				connection.close();
			}
			catch (SQLException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}


	public Livraison getLivraison(String codeLivraison) {
		// TODO Auto-generated method stub
		String request = "SELECT codeLivraison,dateLivraison,dateRetourPrevu,dateRetourReelle,type,codeClient,intituleClient "
				+ "FROM LIVRAISON WHERE codeLivraison=?";
		Livraison livraison = new Livraison();
		try
		{
			st =connection.prepareStatement(request);
			st.setString(1, codeLivraison);
			ResultSet rs = st.executeQuery();
			while(rs.next()){
				livraison.setCodeLivraison(rs.getString(1));
				livraison.setDateLivraison(rs.getDate(2));
				livraison.setDateRetourPrevu(rs.getDate(3));
				livraison.setDateRetourReel(rs.getDate(4));
				livraison.setType(rs.getString(5));
				
				Client client = new Client();
				client.setNcompte(rs.getString(6));
				client.setIntitule(rs.getString(7));
				livraison.setClient(client);
				
			}
			
			String request2 = "select codeArticle,designationArticle,quantiteLivrer,codeLivraison from LigneLivraison "
					+ "WHERE codeLivraison=?";
			st =connection.prepareStatement(request2);
			st.setString(1, codeLivraison);
			rs = st.executeQuery();
			
			List<LigneLivraison> ligneslivraison = new ArrayList<>();
			while(rs.next()){
				LigneLivraison ligneLivraison = new LigneLivraison();
				
				Article article = new Article();
				article.setReference(rs.getString(1));
				article.setDesignation(rs.getString(2));
				ligneLivraison.setArticle(article);
				
				ligneLivraison.setQuantite(rs.getFloat(3));
				ligneLivraison.setLivraison(livraison);
				ligneslivraison.add(ligneLivraison);
			}
			
			livraison.setLigneslivraison(ligneslivraison);
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		return livraison;
	}

	public void deleteLivraison(String codeLivraison){
		
		try
		{
			String request = "delete from Livraison where codeLivraison = ?";
			String request2 = "delete from LigneLivraison where codeLivraison = ?";
			
			st =connection.prepareStatement(request2);
			st.setString(1, codeLivraison);
			st.execute();
			st =connection.prepareStatement(request);
			st.setString(1, codeLivraison);
			st.execute();
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}

	public void updateLivraison(Livraison livraison) {
		// TODO Auto-generated method stub
		String request = "update LIVRAISON set codeLivraison=?,dateLivraison=?,dateRetourPrevu=?,dateRetourReelle=?,type=?,codeClient=?,intituleClient=?"
				+ " WHERE codeLivraison=?";
		try
		{
			st =connection.prepareStatement(request);
			st =connection.prepareStatement(request);
			st.setString(1, livraison.getCodeLivraison());
			st.setDate(2,livraison.getDateLivraison()!=null ? new java.sql.Date( livraison.getDateLivraison().getTime()):null);
			st.setDate(3,livraison.getDateRetourPrevu()!=null ? new java.sql.Date( livraison.getDateRetourPrevu().getTime()):null);
			st.setDate(4,livraison.getDateRetourReel()!=null ? new java.sql.Date( livraison.getDateRetourReel().getTime()):null);
			st.setString(5, livraison.getType());
			st.setString(6, livraison.getClient().getNcompte());
			st.setString(7, livraison.getClient().getIntitule());
			st.setString(8, livraison.getCodeLivraison());
			st.execute();
			
			String request2 = "update LigneLivraison set codeArticle=?,designationArticle=?,quantiteLivrer=?,codeLivraison=?"
					+ " WHERE codeLivraison=? and codeArticle=? ";
			st =connection.prepareStatement(request2);
			for(LigneLivraison ligneLivraison : livraison.getLigneslivraison()){
				st.setString(1, ligneLivraison.getArticle().getReference());
				st.setString(2, ligneLivraison.getArticle().getDesignation());
				st.setFloat(3, ligneLivraison.getQuantite());
				st.setString(4, ligneLivraison.getLivraison().getCodeLivraison());
				st.setString(5, ligneLivraison.getLivraison().getCodeLivraison());
				st.setString(6, ligneLivraison.getArticle().getReference());
				st.execute();
			}
		}
		catch (SQLException e)
		{
			e.printStackTrace();
		}
		
	}

}
