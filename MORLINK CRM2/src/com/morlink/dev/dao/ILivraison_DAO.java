package com.morlink.dev.dao;

import com.morlink.dev.beans.Livraison;

public interface ILivraison_DAO {
	public void addLivraison(Livraison livraison);
	public Livraison getLivraison(String codeLivraison);
	public void deleteLivraison(String codeLivraison);
	public void updateLivraison(Livraison livraison);
}
