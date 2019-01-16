package com.morlink.dev.selecteurs;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;





import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.interfaces.runtime.INavigateContext;
import com.axemble.vdoc.sdk.modules.IDirectoryModule;
import com.axemble.vdoc.sdk.providers.BaseViewProvider;
import com.axemble.vdoc.sdk.utils.Logger;
import com.axemble.vdp.ui.core.providers.ICollectionModelViewProvider;
import com.axemble.vdp.ui.framework.composites.base.CtlAbstractView;
import com.axemble.vdp.ui.framework.composites.base.models.views.CollectionViewModel;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelColumn;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelItem;
import com.morlink.dev.beans.Client;
import com.morlink.dev.connexiondb.SingletonConnexionBDD;


/**
 * @author R.SABRI
 * CREATION DATE 20/10/2015
 * Update 21-004-2016 18:14 -Selector Filter
 * VDOC 14
 */


@SuppressWarnings("deprecation")
public class ClientProvider extends BaseViewProvider implements ICollectionModelViewProvider {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected static final Logger log = Logger.getLogger(ClientProvider.class);

	IContext ctx;
	Connection connection;
	PreparedStatement st;
	/**
	 * @param context
	 * @param view
	 */
	public ClientProvider(INavigateContext context, CtlAbstractView view) {
		super(context, view);
	}

	// La vu du selecteur
	@Override
	public void init() {
		super.init();
		// View model
		CollectionViewModel viewModel = (CollectionViewModel) getModel();
		// Création des colonnes dans le modèle de la vue
		ViewModelColumn modelColumn = new ViewModelColumn("Intitulé", "Intitulé" 	,ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
		modelColumn = new ViewModelColumn("IDClient", "Code Client"	,ViewModelColumn.TYPE_STRING);
		viewModel.addColumn(modelColumn);
		
	}


	@Override
	public List<ViewModelItem> getModelItems() {

		IDirectoryModule iDirectoryModule = Modules.getDirectoryModule();
		ArrayList<ViewModelItem> cViewModelItem = new ArrayList<ViewModelItem>();
		try {
			// We get the workflowInstance
			
			IWorkflowInstance workflowInstance = ApplicationDataProvider.workflowInstance;
			// we get articles list
			ArrayList<Client> clients = getClients(workflowInstance);
			for (Client client : clients) {

				ViewModelItem viewModelItem = new ViewModelItem();
				// "SetKey" le champs qui s'affiche après la séléction 
				Object object = new Object();
				object = client;
				viewModelItem.setKey(client.getNcompte());
				// Affectation des valeurs
				viewModelItem.setValue("Intitulé", client.getIntitule());
				//viewModelItem.setValue("IdCongeNormal", conge.getCongeIDNor());
				viewModelItem.setValue("IDClient", client.getNcompte());

				cViewModelItem.add(viewModelItem);
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {

			Modules.releaseModule(iDirectoryModule);
		}
		return cViewModelItem;
	}
	
	
	

	ArrayList<Client> getClients(IWorkflowInstance workflowInstance) {

		ArrayList<Client> clients = new ArrayList<Client>();
		try {
			// Déclaration de connexion 
			// Réspecter le nom de la référence externe ici c'est "ref_ext_CIMAT"
			connection=SingletonConnexionBDD.getSqlSession().getConnectionVDoc(getWorkflowModule(),getPortalModule(),"MORLINK").getConnection();
			
			String query = " select CT_Num,CT_Intitule from F_COMPTET where CT_Sommeil = 0 order by CT_Intitule ASC";
			st = connection.prepareStatement(query);
			ResultSet result = st.executeQuery();

			while (result.next()) { 
				Client client = new Client();
				client.setNcompte(result.getString(1));
				client.setIntitule(result.getString(2).trim());
				clients.add(client);
			}

		} catch (Exception e) {

			e.printStackTrace();
		} 

		return clients;
	}


}

