package com.morlink.dev.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.morlink.dev.beans.Client;
import com.morlink.dev.servicesmetier.crm.MEtablissementDevis;

public class Client_DAO {
	
	public Client getClient(String numCompte){
		Connection connectionSage = MEtablissementDevis.getConnectionSage();
		Client client = new Client();
		try{
			String request = "select CT_Num,CT_Intitule,CT_Contact,CT_Adresse,CT_CodePostal,CT_Ville,CT_Pays,"
					+ "CT_Telephone,CT_Telecopie,CT_EMail,CT_Encours "
					+ "from [MORLINK_20160627].[dbo].[F_COMPTET] "
					+ "where CT_Num = ? ";
			PreparedStatement stx =connectionSage.prepareStatement(request);
			stx.setString(1, numCompte);
			ResultSet rs = stx.executeQuery();
			while(rs.next()){
				client.setNcompte(rs.getString(1));
				client.setIntitule(rs.getString(2));
				client.setContact(rs.getString(3));
				client.setAdresse(rs.getString(4));
				client.setCodePostal(rs.getString(5));
				client.setVille(rs.getString(6));
				client.setPays(rs.getString(7));
				client.setTelephone(rs.getString(8));
				client.setTelecopie(rs.getString(9));
				client.setEmail(rs.getString(10));
				client.setMontantEncours(rs.getFloat(11));
			}
			
			
			request = "select m.MR_No,m.MR_Intitule "
					+ "from [MORLINK_20160627].[dbo].[F_COMPTET] c, [MORLINK_20160627].[dbo].[F_MODELER] m "
					+ "where c.CT_Num = ?  and m.MR_No = c.cbMR_No";
			stx =connectionSage.prepareStatement(request);
			stx.setString(1, numCompte);
			rs = stx.executeQuery();
			while(rs.next()){
				client.setNumMP(rs.getString(1));
				client.setIntituleMP(rs.getString(2));
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return client;
	}
	
}
