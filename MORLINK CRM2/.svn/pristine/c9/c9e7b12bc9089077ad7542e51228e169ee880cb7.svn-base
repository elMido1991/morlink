package com.morlink.dev.selecteurs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.exceptions.PortalModuleException;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.providers.BaseViewProvider;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.core.providers.ICollectionModelViewProvider;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.CollectionViewModel;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelColumn;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.morlink.dev.beans.Article;
import com.morlink.dev.connexiondb.SingletonConnexionBDD;



/**
 * @author R.SABRI
 * CREATION DATE 20/10/2015
 * Update 21-004-2016 18:14 -Selector Filter
 * VDOC 14
 */


@SuppressWarnings("deprecation")
public class ArticleProvider extends BaseViewProvider implements ICollectionModelViewProvider {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger logger = Logger.getLogger(ArticleProvider.class);

	IContext ctx;
	/**
	 * @param context
	 * @param view
	 */
	public ArticleProvider(INavigateContext context, CtlAbstractView view) {
		super(context, view);
	}

	// La vu du selecteur
	@Override
	public void init() {
		super.init();
		// View model
		CollectionViewModel viewModel = (CollectionViewModel) getModel();
		// Création des colonnes dans le modèle de la vue
		ViewModelColumn modelColumn = new ViewModelColumn("Designation", "Désignation",ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
		modelColumn = new ViewModelColumn("IDArticle"  , "Code Article",ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
		
	}


	@Override
	public List<ViewModelItem> getModelItems() {

		IDirectoryModule iDirectoryModule = Modules.getDirectoryModule();
		ArrayList<ViewModelItem> cViewModelItem = new ArrayList<>();
		try {
			
			List<Article> articles = getArticles();
			for (Article article : articles) {

				ViewModelItem viewModelItem = new ViewModelItem();
				// "SetKey" le champs qui s'affiche après la séléction 
				viewModelItem.setKey(article);
				// Affectation des valeurs
				viewModelItem.setValue("Désignation", article.getDesignation());
				viewModelItem.setValue("IDArticle", article.getReference());

				cViewModelItem.add(viewModelItem);
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		} finally {

			Modules.releaseModule(iDirectoryModule);
		}
		return cViewModelItem;
	}
	
	
	

	public List<Article> getArticles() {

		List<Article> articles = new ArrayList<>();
		String query = " select AR_Ref, AR_Design , AR_PrixVen from F_ARTICLE where AR_Sommeil = 0 and AR_PrixVen != 0";
		
		
		try (
				Connection connection=SingletonConnexionBDD.getSqlSession().getConnectionVDoc(getWorkflowModule(),getPortalModule(),"MORLINK").getConnection();
				PreparedStatement st = connection.prepareStatement(query);
				ResultSet result = st.executeQuery();) {
			// Déclaration de connexion 
			// Réspecter le nom de la référence externe ici c'est "ref_ext_CIMAT"
			
			
			
			
			// Préparer la requette sql
			

			while (result.next()) { 
				Article article = new Article();
				article.setReference(result.getString(1));
				article.setDesignation(result.getString(2));
				article.setPrixVenteHT(result.getFloat(3));
				articles.add(article);

			}
			
		} catch (PortalModuleException | SQLException e) {
			logger.error(e.getMessage());
		} 
		return articles;
	}

	

}

