package com.morlink.dev.servicesmetier.crm;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.turbine.services.servlet.TurbineServlet;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.exceptions.WorkflowModuleException;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IAttachment;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdoc.sdk.interfaces.IResourceController;
import com.axemble.vdoc.sdk.interfaces.IWorkflowInstance;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.morlink.dev.beans.Article;
import com.morlink.dev.beans.Catalogue;
import com.morlink.dev.beans.Client;
import com.morlink.dev.beans.Devis;
import com.morlink.dev.beans.LigneDevis;
import com.morlink.dev.beans.LigneLivraison;
import com.morlink.dev.beans.Livraison;
import com.morlink.dev.connexiondb.SingletonConnexionBDD;
import com.morlink.dev.dao.Article_DAO;
import com.morlink.dev.dao.Client_DAO;
import com.morlink.dev.dao.Devis_DAO;
import com.morlink.dev.dao.Livraison_DAO;
import com.morlink.dev.services.FileManager;
import com.morlink.dev.services.GenererPDF;


public class MEtablissementDevis {
	private Article article;

	// ************************************ METHODES DE SERVICES
	// ******************************************************//
	public static float getNombreFromString(String nombreString) {
		float nombre = 0;
		try {
			nombre = Float.parseFloat(nombreString.replace(",", ".").replace("\u00a0", ""));
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nombre;
	}

	public static String getStringFromNombre(Float nombre) {
		String nombreString = "";
		try {
			DecimalFormat df = new DecimalFormat("#,##0.00",new DecimalFormatSymbols(new Locale("fr", "FR")));
			nombreString = df.format(nombre.floatValue()) + "";
		} catch (Exception e) {
			e.printStackTrace();
		}

		return nombreString;
	}

	public static Connection getConnection() {
		Connection con = null;
		try {
			con = SingletonConnexionBDD.getSqlSession().getConnectionVDoc(Modules.getWorkflowModule(),Modules.getPortalModule(), "MORLINK_CRM").getConnection();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return con;

	}

	public static Connection getConnectionSage() {
		Connection con = null;
		try {
			con = SingletonConnexionBDD.getSqlSession().getConnectionVDoc(Modules.getWorkflowModule(),Modules.getPortalModule(), "MORLINK").getConnection();

		} catch (Exception e) {
			e.printStackTrace();
		}

		return con;

	}

	// ***************************************************************************************************//

	// ************************************ ETAPE 1
	// ******************************************************//
	@SuppressWarnings("unchecked")
	public  void addDevisToDDB(IAction action, IWorkflowInstance instance) {
		if (action.getName().equals("Valider")) {
			// add devis
			String ndevis = (String) instance.getValue("sys_Reference");
			Date dateCreation = new Date();
			Object oClient = (Object) instance.getValue("MP_GES_CRM_CLIENT");
			String nClient = "";
			String intituleClient = "";
			Client client = null;
			if (oClient instanceof Client) {
				client = (Client) oClient;
				nClient = client.getNcompte();
				intituleClient = client.getIntitule();
			} else if (oClient instanceof String) {
				intituleClient = (String) instance.getValue("MP_GES_CRM_CLIENTINTITULE");
				nClient = (String) instance.getValue("MP_GES_CRM_CODECLIENT");
			}
			String ice = (String) instance.getValue("MP_GES_CRM_Ice");
			int nbrRelanceClient = 0;
			String pj = null;
			List<LigneDevis> lignesdevis = new ArrayList<LigneDevis>();

			// construire devis
			Devis devis = new Devis();
			devis.setNdevis(ndevis);
			devis.setNClient(nClient);
			devis.setIntituleClient(intituleClient);
			devis.setDateCreation(dateCreation);
			devis.setNbrRelanceClient(nbrRelanceClient);
			devis.setIce(ice);
			devis.setPj(pj);

			// ajouter ligne devis au devis
			List<ILinkedResource> lignesdevisvdoc = (List<ILinkedResource>) instance.getLinkedResources("MP_GES_CRM_TABARTICLES");
			for (ILinkedResource lignedevisvdoc : lignesdevisvdoc) {
				LigneDevis lignedevis = new LigneDevis();
				lignedevis.setCodeArticle((String) lignedevisvdoc.getValue("MP_GES_CRM_TABARTICLES_CODEARTICLE"));
				
				Object oArticle = (Object) lignedevisvdoc.getValue("MP_GES_CRM_TABARTICLES_ARTICLE");
				if(oArticle==null) {
					oArticle = (Object) lignedevisvdoc.getValue("MP_GES_CRM_TABARTICLES_ARTICLEBIS");
				}
				String DesignationArticle = "";
				Article article = null;
				if (oArticle instanceof Article) {
					article = (Article) oArticle;
					DesignationArticle = article.getDesignation();
				} else if (oClient instanceof String) {
					DesignationArticle = (String) oArticle;
				}

				lignedevis.setDesignationArticle(DesignationArticle);
				lignedevis.setPrixpardefaut(getNombreFromString((String) lignedevisvdoc.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT")));

				if (lignedevisvdoc.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT") != null)
					lignedevis.setPrixtarifCommercial(getNombreFromString((String) lignedevisvdoc.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT")));
				else
					lignedevis.setPrixtarifCommercial(getNombreFromString((String) lignedevisvdoc.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT")));

				lignedevis.setQte((float) lignedevisvdoc.getValue("MP_GES_CRM_TABARTICLES_QUANTITE"));
				boolean isAvecCatalogue = ((String) lignedevisvdoc.getValue("MP_GES_CRM_TABARTICLES_AVECCATALOGUE")).equals("Oui") ? true : false;
				lignedevis.setAvecCatalogue(isAvecCatalogue);
				lignedevis.setPrixtarifADV(lignedevis.getPrixtarifCommercial());
				lignedevis.setQtereste(lignedevis.getQtereste());
				lignedevis.setQtelivre(0);
				lignedevis.setQtealivre(0);
				lignedevis.setQteAffecte(0);
				lignedevis.setStatut(true);
				lignedevis.setDevis(devis);
				lignesdevis.add(lignedevis);

			}
			devis.setLigneDevises(lignesdevis);
			// pesister devis dans la BDD
			
			Devis_DAO devisdao = new Devis_DAO();
			Devis devRecherche = devisdao.getDevis(ndevis);
			if(devRecherche.getNdevis()==null) {
				devisdao.addDevis(devis);
				devisdao.addLigneDevis(devis);
				devisdao.addLigneDevisArchive(devis);
			}else {
				devisdao.addLigneDevis(devis);
				devisdao.addLigneDevisArchive(devis);
			}
			
		}
	}

	@SuppressWarnings("unchecked")
	public void onPropertyChangedOnDevis(IProperty property,IWorkflowInstance instance,IWorkflowModule im) {

		if (property.getName().equals("MP_GES_CRM_TABARTICLES")){
			try{
				List<ILinkedResource> lignesdevis = (List<ILinkedResource>) instance.getLinkedResources("MP_GES_CRM_TABARTICLES");
				Article_DAO article_DAO = new Article_DAO();
				instance.setValue("MP_GES_CRM_CATALOGUESARTICLES", null);
				for (ILinkedResource lignedevis : lignesdevis) {
					String codeArticle 			  = (String) lignedevis.getValue("MP_GES_CRM_TABARTICLES_CODEARTICLE");
					String decisionAvecCatalogues = (String) lignedevis.getValue("MP_GES_CRM_TABARTICLES_AVECCATALOGUE");
					Article article = article_DAO.getArticle(codeArticle);
					File articleFile = article.getCatalogue()!=null ? getFile(TurbineServlet.getRealPath("CATALOGUE") + "\\"+codeArticle+"\\"+ article.getCatalogue().getCatalogueArticle()):null;
					if(articleFile!=null && decisionAvecCatalogues.equals("Oui")){
						im.addAttachment(instance, "MP_GES_CRM_CATALOGUESARTICLES", articleFile.getName(),TurbineServlet.getRealPath("CATALOGUE") + "\\"+codeArticle+"\\"+ article.getCatalogue().getCatalogueArticle());
					}
					
					
				}
			}catch(Exception e){
				e.printStackTrace();
			}
		}
		
		
		if (property.getName().equals("MP_GES_CRM_CLIENT")) {

			String clientNCompte = (String) instance.getValue("MP_GES_CRM_CLIENT");
			
			if (clientNCompte != null) {
				Client_DAO client_dao = new Client_DAO();
				Client client = client_dao.getClient(clientNCompte);
				instance.setValue("MP_GES_CRM_CODECLIENT", client.getNcompte());
				instance.setValue("MP_GES_CRM_MONTANTENCOURS",getStringFromNombre(client.getMontantEncours()));
				instance.setValue("MP_GES_CRM_CLIENTINTITULE",client.getIntitule());
				instance.setValue("MP_GES_CRM_EMAILCLIENT",client.getEmail());
				instance.setValue("MP_GES_CRM_MODPAIS",client.getIntituleMP());
			}

		}

		if (property.getName().equals("MP_GES_CRM_TVAN")|| property.getName().equals("MP_GES_CRM_TABARTICLES")) {
			// TODO Auto-generated method stub
			List<ILinkedResource> listArticles = (List<ILinkedResource>) instance.getLinkedResources("MP_GES_CRM_TABARTICLES");
			float flag = 0f;
			float totalHT = 0f;

			for (ILinkedResource article : listArticles) {

				if (article.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT") != null) {

					float nouveauPrixHT = getNombreFromString((String) article.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT"));
					float prixParDefaut = getNombreFromString((String) article.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT"));

					if (nouveauPrixHT != prixParDefaut) {
						float quantite = article.getValue("MP_GES_CRM_TABARTICLES_QUANTITE") == null ? 0: (float) article.getValue("MP_GES_CRM_TABARTICLES_QUANTITE");
						float prixTotalHT = nouveauPrixHT * quantite;
						article.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT",getStringFromNombre(prixTotalHT));
						totalHT += prixTotalHT;
						flag++;
					} else {
						float prixTotalHT = getNombreFromString((String) article.getValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT"));
						totalHT += prixTotalHT;
					}
				} else if (article.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT") != null) {

					float prixTotalHT = getNombreFromString((String) article.getValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT"));
					totalHT += prixTotalHT;
				}

			}

			instance.setValue("MP_GES_CRM_MONTANTHT",getStringFromNombre(totalHT));
			double tvad = (double) instance.getValue("MP_GES_CRM_TVA");
			float tva = Float.parseFloat(tvad + "");
			instance.setValue("MP_GES_CRM_MONTANTTTC",getStringFromNombre(totalHT * tva + totalHT));
			instance.setValue("MP_GES_CRM_FRAISTIMBRE",totalHT*0.025f);
			instance.setValue("MP_GES_CRM_TVASTR",getStringFromNombre(totalHT * tva));
			instance.setValue("MP_GES_CRM_FRAISTIMBRESTR",getStringFromNombre(totalHT*0.025f));
			instance.setValue("MP_GES_CRM_FLAGPRIX", flag);
			
			instance.setValue("MP_GES_CRM_MONTANTHTEDBIS",getStringFromNombre(totalHT));
			instance.setValue("MP_GES_CRM_MONTANTTTCEDBIS",getStringFromNombre(totalHT * tva + totalHT));
			instance.setValue("MP_GES_CRM_TVASTRBIS",getStringFromNombre(totalHT * tva));
			instance.setValue("MP_GES_CRM_FRAISTIMBRESTRBIS",getStringFromNombre(totalHT*0.025f));
		}
	}

	public void getPJCatalogues(IWorkflowInstance instance,IWorkflowModule im) {
		// TODO Auto-generated method stub
		
		
	}
	
	public File getFile(String filename){
		return new File(filename);
	}
	// ***************************************************************************************************//

	// *********************************** TABLEAU ARTICLES 1
	// ********************************************//
	public void onPropertyChangedOnTabArticle1(IProperty property,
			IWorkflowInstance instance) {
		// TODO Auto-generated method stub
		if(property.getName().equals("MP_GES_CRM_TABARTICLES_TYPERECHERCHEARTICLE")) {
			String valeRecherche = (String) instance.getValue("MP_GES_CRM_TABARTICLES_TYPERECHERCHEARTICLE");
			if(valeRecherche==null) {
				instance.setValue("MP_GES_CRM_TABARTICLES_ARTICLE", null);
				instance.setValue("MP_GES_CRM_TABARTICLES_ARTICLEBIS", null);
			}
			else if(valeRecherche.equals("Code")) {
				instance.setValue("MP_GES_CRM_TABARTICLES_ARTICLE", null);
			}
			else if(valeRecherche.equals("D�signation")) {
				instance.setValue("MP_GES_CRM_TABARTICLES_ARTICLEBIS", null);
			}
		}
		if (property.getName().equals("MP_GES_CRM_TABARTICLES_ARTICLE")||property.getName().equals("MP_GES_CRM_TABARTICLES_ARTICLEBIS")) {
			String nsArticle = property.getName();
			article = (Article) instance.getValue(nsArticle);
			
			instance.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE","");
			instance.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT",getStringFromNombre(0f));
			instance.setValue("MP_GES_CRM_TABARTICLES_DESIGNATION","");
			instance.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT",getStringFromNombre(0f));
			if (article != null) {
				instance.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE",article.getReference());
				instance.setValue("MP_GES_CRM_TABARTICLES_DESIGNATION",article.getDesignation());
				float prixVenteHT = article.getPrixVenteHT();
				float quantite = instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE") == null ? 0f: (float) instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE");
				float prixTotalHT = prixVenteHT * quantite;
				instance.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT",getStringFromNombre(prixVenteHT));
				instance.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT",getStringFromNombre(prixTotalHT));
			}

		}

		else if (property.getName().equals("MP_GES_CRM_TABARTICLES_QUANTITE")) {
			float prixVenteHT = 0f;
			float quantite = instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE") == null ? 0: (float) instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE");
			String reponseNewPrix = (String) instance.getValue("MP_GES_CRM_TABARTICLES_NEWPRIX");
			if (reponseNewPrix.equals("Oui")) {
				prixVenteHT = instance.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT") == null ? 0: getNombreFromString((String) instance.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT"));
			} else {
				prixVenteHT = instance.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT") == null ? 0: getNombreFromString((String) instance.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT"));
			}

			float prixTotalHT = prixVenteHT * quantite;
			instance.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT",getStringFromNombre(prixTotalHT));
		}

		else if (property.getName().equals("MP_GES_CRM_TABARTICLES_NEWPRIX")) {
			String reponseNewPrix = (String) instance.getValue("MP_GES_CRM_TABARTICLES_NEWPRIX");
			if (reponseNewPrix.equals("Non")) {
				instance.setValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT", null);

				float prixVenteHT = getNombreFromString((String) instance.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT"));
				float quantite = instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE") == null ? 0: (float) instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE");
				float prixTotalHT = prixVenteHT * quantite;
				instance.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT",getStringFromNombre(prixVenteHT));
				instance.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT",getStringFromNombre(prixTotalHT));

			}
		}

		else if (property.getName().equals("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT")) {
			if (instance.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT") != null) {
				float prixVenteHT = getNombreFromString((String) instance.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT"));
				float quantite = instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE") == null ? 0: (float) instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE");
				float prixTotalHT = prixVenteHT * quantite;
				instance.setValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT",getStringFromNombre(prixVenteHT));
				instance.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT",getStringFromNombre(prixTotalHT));
			}

		}
	}

	@SuppressWarnings("unchecked")
	public void onBeforeSave(IWorkflowInstance instance) {
		List<ILinkedResource> listArticles = (List<ILinkedResource>) instance.getParentInstance().getLinkedResources("MP_GES_CRM_TABARTICLES");
		float flag = 0f;
		float totalHT = 0f;

		for (ILinkedResource article : listArticles) {

			if (article.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT") != null) {

				float nouveauPrixHT = getNombreFromString((String) article.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHT"));
				float prixParDefaut = getNombreFromString((String) article.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT"));

				if (nouveauPrixHT != prixParDefaut) {
					float quantite = instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE") == null ? 0: (float) instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE");
					float prixTotalHT = nouveauPrixHT * quantite;
					instance.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT",getStringFromNombre(prixTotalHT));
					totalHT += prixTotalHT;
					flag++;
				} else {
					float prixTotalHT = getNombreFromString((String) article.getValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT"));
					totalHT += prixTotalHT;
				}
			}

		}

		instance.getParentInstance().setValue("MP_GES_CRM_MONTANTHT",getStringFromNombre(totalHT));
		double tvad = (double) instance.getParentInstance().getValue("MP_GES_CRM_TVA");
		float tva = Float.parseFloat(tvad + "");
		instance.getParentInstance().setValue("MP_GES_CRM_MONTANTTTC",getStringFromNombre(totalHT * tva + totalHT));
		instance.getParentInstance().setValue("MP_GES_CRM_FLAGPRIX", flag);
	}

	// ***************************************************************************************************//

	// ************************************ ETAPE 2
	// ******************************************************//

	@SuppressWarnings("unchecked")
	public void getArticlesWithDifferentPricesForTabArt2(IWorkflowInstance instance) {
		String devisNumber = (String) instance.getValue("sys_Reference");
		Devis_DAO devisdao = new Devis_DAO();
		Devis devis = devisdao.getDevis(devisNumber);
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES2");
		instance.setValue("MP_GES_CRM_TABARTICLES2", null);;
		listLigneDevis = null;
		if (listLigneDevis == null || listLigneDevis.isEmpty())
			for (LigneDevis lignedevis : devis.getLigneDevises()) {
				float prixParDefaut = lignedevis.getPrixpardefaut();
				float prixNvCommercial = lignedevis.getPrixtarifCommercial();
				if (prixParDefaut != prixNvCommercial) {
					ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES2");
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE2",lignedevis.getDesignationArticle());
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE2",lignedevis.getCodeArticle());

					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT2",getStringFromNombre(lignedevis.getPrixpardefaut()));
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHTCOM",getStringFromNombre(lignedevis.getPrixtarifCommercial()));

					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITE2",lignedevis.getQte());
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT2",getStringFromNombre(lignedevis.getQte()* lignedevis.getPrixtarifCommercial()));
					instance.addLinkedResource(iLigneDevis);
				}

			}
	}

	@SuppressWarnings("unchecked")
	public void onAfterSubmitEtapeValidationADV(IAction action,IWorkflowInstance instance) {
		try {
			if (action.getName().equals("Valider3")) {
				List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES2");
				Devis devis = new Devis();
				devis.setNdevis((String) instance.getValue("sys_Reference"));

				for (ILinkedResource ilignedevis : listLigneDevis) {
					LigneDevis lignedevis = new LigneDevis();
					lignedevis.setCodeArticle((String) ilignedevis.getValue("MP_GES_CRM_TABARTICLES_CODEARTICLE2"));
					lignedevis.setPrixtarifADV(getNombreFromString((String) ilignedevis.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHTADV")));
					lignedevis.setDevis(devis);
					Devis_DAO devisdao = new Devis_DAO();
					devisdao.updateLigneDevis(lignedevis);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ***************************************************************************************************//

	// *********************************** TABLEAU ARTICLES 1
	// ********************************************//
	public void onPropertyChangedOnTabArticle2(IProperty property,IWorkflowInstance instance) {
		// TODO Auto-generated method stub

		if (property.getName().equals("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHTADV")) {
			if (instance.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHTADV") != null) {
				float prixVenteHTADV = getNombreFromString((String) instance.getValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHTADV"));
				float quantite = (float) instance.getValue("MP_GES_CRM_TABARTICLES_QUANTITE2");
				float prixTotalHTADV = prixVenteHTADV * quantite;
				instance.setValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHTADV",getStringFromNombre(prixVenteHTADV));
				instance.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT2",getStringFromNombre(prixTotalHTADV));
			}

		}
	}

	// ***************************************************************************************************//

	// *********************************** ETAPE ENVOI DEVIS AU CLIENT
	// ***********************************//

	@SuppressWarnings("unchecked")
	public void getArticlesWithDifferentPricesForTabArt3(IWorkflowInstance instance) {
		String devisNumber = (String) instance.getValue("sys_Reference");
		Devis_DAO devisdao = new Devis_DAO();
		Devis devis = devisdao.getDevis(devisNumber);
		
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES3");
		instance.setValue("MP_GES_CRM_TABARTICLES3",null);
		listLigneDevis = null;
		if (listLigneDevis == null|| listLigneDevis.isEmpty()) {
			
			float prixtotalHT = 0;
			float prixtotalTTC = 0;
			float tva = Float.parseFloat((double) instance.getValue("MP_GES_CRM_TVA") + "");

			for (LigneDevis lignedevis : devis.getLigneDevises()) {
				ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES3");
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE3",lignedevis.getDesignationArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE3",lignedevis.getCodeArticle());
				float prixCommercial = lignedevis.getPrixtarifCommercial();
				float prixADV = lignedevis.getPrixtarifADV();
				float prixfinal = (prixCommercial == prixADV || prixADV == 0f) ? prixCommercial: prixADV;
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT3",getStringFromNombre(prixfinal));

				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITE3",lignedevis.getQte());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALVENTEHT3",getStringFromNombre(lignedevis.getQte() * prixfinal));
				prixtotalHT += lignedevis.getQte() * prixfinal;
				instance.addLinkedResource(iLigneDevis);
				
			}

			prixtotalTTC = prixtotalHT * tva + prixtotalHT;
			instance.setValue("MP_GES_CRM_MONTANTHTED",getStringFromNombre(prixtotalHT));
			instance.setValue("MP_GES_CRM_MONTANTTTCED",getStringFromNombre(prixtotalTTC));
			instance.setValue("MP_GES_CRM_FRAISTIMBRESTR",getStringFromNombre(prixtotalHT*0.025f));

		}

	}

	public void generateDevis(IWorkflowInstance iw, IWorkflowModule im,IResourceController ir, ILinkedResource ilr) {
		try {
			String nombreDevis = (String) iw.getValue("sys_Reference");
			
			
			
			
			String montantHT = (String) iw.getValue("MP_GES_CRM_MONTANTHTED");

			String montantTTC = (String) iw.getValue("MP_GES_CRM_MONTANTTTCED");
			String modalitePaiement = (String) iw.getValue("MP_GES_CRM_MODPAIS");
			String tva = (String) iw.getValue("MP_GES_CRM_TVASTR");
			String filename = nombreDevis;
			String nomFichierPDF = filename + ".pdf";

			File newFile = new File(TurbineServlet.getRealPath("DEVIS")+ "\\OUT\\" + nomFichierPDF);
			newFile.createNewFile();

			Map<String, Object> Parametr = new HashMap<String, Object>();
			Parametr.put("NumeroDevis", nombreDevis);
			Parametr.put("TVA", tva + " Dhs");
			Parametr.put("MontantGlobalHT", montantHT + " Dhs");
			Parametr.put("MontantGlobalTTC", montantTTC + " Dhs");
			Parametr.put("modalitePaiement", modalitePaiement);

			FileManager path = new FileManager();
			Connection connection = getConnection();

			GenererPDF.generer("devisMorlink", path.getOutDir(), nomFichierPDF,Parametr, connection);
			iw.setValue("MP_GES_CRM_DEVISPJ", null);
			im.addAttachment(iw, "MP_GES_CRM_DEVISPJ", nomFichierPDF,TurbineServlet.getRealPath("DEVIS") + "\\OUT\\"+ nomFichierPDF);
			ir.alert("Document g�n�r� avec succ�s !!");
		} catch (Exception e) {
			e.printStackTrace();
			ir.alert(e.getMessage());
		}
	}
	
	public void generateArchivedArticles(IWorkflowInstance iw, IWorkflowModule im,IResourceController ir, ILinkedResource ilr) {
		try {
			String nombreDevis = (String) iw.getValue("sys_Reference");
			String filename = nombreDevis;
			String nomFichierPDF = filename + "_ARCHIVED.pdf";

			File newFile = new File(TurbineServlet.getRealPath("DEVIS")+ "\\OUT\\" + nomFichierPDF);
			if(newFile.exists())
				newFile.delete();
			newFile.createNewFile();

			Map<String, Object> Parametr = new HashMap<String, Object>();
			Parametr.put("NDEVIS", nombreDevis);

			FileManager path = new FileManager();
			Connection connection = getConnection();

			GenererPDF.generer("ArichedArticles", path.getOutDir(), nomFichierPDF,Parametr, connection);
			iw.setValue("MP_GES_CRM_ARCHIVEDARTICLES", null);
			im.addAttachment(iw, "MP_GES_CRM_ARCHIVEDARTICLES", nomFichierPDF,TurbineServlet.getRealPath("DEVIS") + "\\OUT\\"+ nomFichierPDF);
			ir.alert("Document g�n�r� avec succ�s !!");
		} catch (Exception e) {
			e.printStackTrace();
			ir.alert(e.getMessage());
		}
	}
	
	@SuppressWarnings("unchecked")
	public boolean onBeforeTestFileIsEmpty(IWorkflowInstance workflowInstance,IResourceController resourceController) {
		// TODO Auto-generated method stub
		List<IAttachment> pjs = (ArrayList<IAttachment>) workflowInstance.getValue("MP_GES_CRM_DEVISPJ");
		if(pjs == null || pjs.isEmpty()) {
			resourceController.alert("La g�n�ration du devis est obligatoire !!!");
			return false;
		}
		else {
			return true;
		}
	}
	
	
	// ***************************************************************************************************//

	// *********************************** ETAPE RELANCE CLIENT
	// ******************************************//
	public void getNombreRelanceClient(IWorkflowInstance instance) {
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("sys_Reference");
		float nombreRelance = devisdao.getRelanceDevis(numDevis);
		instance.setValue("MP_GES_CRM_FLAGRELANCE", nombreRelance);
	}

	public void setRelanceClient(IWorkflowInstance instance) {
		float nombreRelance = (float) instance.getValue("MP_GES_CRM_FLAGRELANCE");
		String numDevis = (String) instance.getValue("sys_Reference");
		Devis_DAO devisdao = new Devis_DAO();
		devisdao.updateRelanceDevis(numDevis, nombreRelance + 1f);
	}

	// ***************************************************************************************************//

	// ******************************* ETAPE VALIDATION STATUT CLIENT
	// ************************************//
	@SuppressWarnings("unchecked")
	public float getSizeOfTable(String sysname, IWorkflowInstance iw) {
		List<ILinkedResource> liste = (List<ILinkedResource>) iw.getLinkedResources(sysname);
		return liste == null ? 0f : liste.size();
	}

	public void checkVente(IWorkflowInstance instance, IResourceController irc) {
		float nombreQuantiteEgalZero = (float) instance.getValue("MP_GES_CRM_FLAGQUANTITEDISPOINIT");
		float nombreQuantiteDispoSupAuCmde = (float) instance.getValue("MP_GES_CRM_FLAGQTEDISPOSUPQTECMD");
		if (nombreQuantiteEgalZero == 0) {
			irc.showBodyBlock("FragChekingMontantEnCours", true);
			irc.showBodyBlock("FragDecision", true);
			irc.showBodyBlock("FragDecisionQteZero", false);
			if (nombreQuantiteDispoSupAuCmde == getSizeOfTable("MP_GES_CRM_TABARTICLES10", instance)) {
				irc.showBodyBlock("FragChekingMontantEnCours", false);
				irc.showBodyBlock("FragDevisInterne", false);
			} else {
				irc.showBodyBlock("FragChekingMontantEnCours", true);
				String decision = (String) instance.getValue("MP_GES_CRM_DECISONFORCERDEVIS");
				if (decision.equals("Oui")) {
					irc.showBodyBlock("FragDevisInterne", true);
				} else {
					irc.showBodyBlock("FragDevisInterne", false);
				}
				instance.setValue("MP_GES_CRM_VentePartielleTotal", "Totale");
			}

			checkMontantDeLencours(instance, irc);
			irc.getButtonContainer(2).get(0).setHidden(true);
			irc.getButtonContainer(2).get(1).setHidden(false);
		} else {
			irc.showBodyBlock("FragChekingMontantEnCours", false);
			irc.showBodyBlock("FragDevisInterne", false);
			irc.showBodyBlock("FragDecision", false);
			irc.showBodyBlock("FragDecisionQteZero", true);
			irc.showBodyBlock("FragDevisInterne", false);
			irc.getButtonContainer(2).get(0).setHidden(false);
			irc.getButtonContainer(2).get(1).setHidden(true);
		}
	}

	public void checkMontantDeLencours(IWorkflowInstance instance,IResourceController irc) {
		String montantEncoursText = (String) instance.getValue("MP_GES_CRM_MONTANTENCOURS");
		String montantDevisText = (String) instance.getValue("MP_GES_CRM_MONTANTTTCED");

		float montantEncours = getNombreFromString(montantEncoursText);
		float montantDevis = getNombreFromString(montantDevisText);
		if (montantDevis > montantEncours) {
			irc.showBodyBlock("FragDecision", true);
			String decision = (String) instance.getValue("MP_GES_CRM_DECISONFORCERDEVIS");
			if (decision.equals("Oui")) {
				irc.showBodyBlock("FragChekingMontantEnCours", true);
				// irc.showBodyBlock("FragVenteTotal", true);
				String typeVente = (String) instance.getValue("MP_GES_CRM_VentePartielleTotal");
				if (typeVente.equals("Totale")) {
					irc.showBodyBlock("FragDevisInterne", true);
				} else {
					irc.showBodyBlock("FragDevisInterne", false);
				}
				irc.getButtonContainer(2).get(0).setHidden(true);
				irc.getButtonContainer(2).get(1).setHidden(false);
			} else {
				irc.showBodyBlock("FragChekingMontantEnCours", false);
				irc.showBodyBlock("FragDevisInterne", false);
				irc.getButtonContainer(2).get(0).setHidden(false);
				irc.getButtonContainer(2).get(1).setHidden(true);
			}
		} else {
			irc.showBodyBlock("FragDecision", false);
		}
	}

	@SuppressWarnings("unchecked")
	public void getArticlesDisponibleDansStockSageForStatutClient(IWorkflowInstance instance, IResourceController irc) {
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("sys_Reference");
		Devis devis = devisdao.getDevisWithStock(numDevis);
		float flagQteDispo = 0;
		float flagQtetoutDispo = 0;
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES10");

		if (listLigneDevis != null) {
			instance.deleteLinkedResources(listLigneDevis);
		}

		// if(listLigneDevis == null || (listLigneDevis != null &&
		// listLigneDevis.isEmpty()))
		for (LigneDevis lignedevis : devis.getLigneDevises()) {
			if (lignedevis.getQtereste() > 0) {
				ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES10");
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE10",lignedevis.getDesignationArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE10",lignedevis.getCodeArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITE10",lignedevis.getQtereste());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEDISPO10",lignedevis.getQtedispo());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_MONTANTART10",lignedevis.getPrixtarifADV());
				instance.addLinkedResource(iLigneDevis);
			}

			if (lignedevis.getQtedispo() == 0) {
				flagQteDispo++;
			}
			if (lignedevis.getQtedispo() >= lignedevis.getQte()) {
				flagQtetoutDispo++;
			}
		}

		instance.setValue("MP_GES_CRM_FLAGQUANTITEDISPOINIT", flagQteDispo);
		instance.setValue("MP_GES_CRM_FLAGQTEDISPOSUPQTECMD", flagQtetoutDispo);
		checkVente(instance, irc);

	}

	@SuppressWarnings("unchecked")
	public List<Float> calculateMontantGlobalHTetTTC(IWorkflowInstance instance) {
		List<Float> montants = new ArrayList<>();
		List<ILinkedResource> lignesdevis = (List<ILinkedResource>) instance.getLinkedResources("MP_GES_CRM_TABARTICLES10");
		float montantGlobalHT = 0;
		float montantGlobalTTC = 0;
		for (ILinkedResource lignedevis : lignesdevis) {
			float quantiteCommandee = (float) lignedevis.getValue("MP_GES_CRM_TABARTICLES_QUANTITE10");
			float quantiteDispo = (float) lignedevis.getValue("MP_GES_CRM_TABARTICLES_QUANTITEDISPO10");
			float montantArticle = (float) lignedevis.getValue("MP_GES_CRM_TABARTICLES_MONTANTART10");
			float montanHTFils = 0;
			if (quantiteCommandee <= quantiteDispo) {
				montanHTFils = quantiteCommandee * montantArticle;
			} else {
				montanHTFils = quantiteDispo * montantArticle;
			}
			montantGlobalHT += montanHTFils;
		}
		montantGlobalTTC = montantGlobalHT * 0.2f + montantGlobalHT;
		montants.add(montantGlobalHT);
		montants.add(montantGlobalTTC);
		return montants;
	}

	public void generateDevisInterne(IWorkflowInstance iw, IWorkflowModule im,IResourceController ir, ILinkedResource ilr) {
		try {
			String nombreDevis = (String) iw.getValue("sys_Reference");
			String montantHT = getStringFromNombre(calculateMontantGlobalHTetTTC(iw).get(0));

			String montantTTC = getStringFromNombre(calculateMontantGlobalHTetTTC(iw).get(1));
			String modalitePaiement = (String) iw.getValue("MP_GES_CRM_MODPAIS");
			String tva = getStringFromNombre(getNombreFromString(montantTTC)- getNombreFromString(montantHT));

			String filename = "INT" + nombreDevis;
			String nomFichierPDF = filename + ".pdf";

			File newFile = new File(TurbineServlet.getRealPath("DEVIS")+ "\\OUT\\" + nomFichierPDF);
			newFile.createNewFile();

			Map<String, Object> Parametr = new HashMap<String, Object>();
			Parametr.put("NumeroDevis", nombreDevis);
			Parametr.put("TVA", tva + " Dhs");
			Parametr.put("MontantGlobalHT", montantHT + " Dhs");
			Parametr.put("MontantGlobalTTC", montantTTC + " Dhs");
			Parametr.put("modalitePaiement", modalitePaiement);

			FileManager path = new FileManager();
			Connection connection = getConnection();

			GenererPDF.generer("devisMorlinkInterne", path.getOutDir(),nomFichierPDF, Parametr, connection);
			iw.setValue("MP_GES_CRM_DEVISINTERNE", null);
			im.addAttachment(iw, "MP_GES_CRM_DEVISINTERNE", nomFichierPDF,TurbineServlet.getRealPath("DEVIS") + "\\OUT\\"+ nomFichierPDF);
			ir.alert("Document g�n�r� avec succ�s !!");
		} catch (Exception e) {
			e.printStackTrace();
			ir.alert(e.getMessage());
		}
	}

	public void onPropertyChangeStatutClient(IProperty property,IWorkflowInstance instance, IResourceController irc) {
		
		if (property.getName().equals("MP_GES_CRM_DECISONFORCERDEVIS")) {
			String decision = (String) instance.getValue("MP_GES_CRM_DECISONFORCERDEVIS");
			float nombreQuantiteDispoSupAuCmde = (float) instance.getValue("MP_GES_CRM_FLAGQTEDISPOSUPQTECMD");
			
			int widgetPlaceValider = 1;
			int widgetPlaceRejeter = 0;
			if (decision.equals("Oui")) {

				// irc.showBodyBlock("FragVenteTotal", true);
				irc.getButtonContainer(2).get(widgetPlaceValider).setHidden(true);
				irc.getButtonContainer(2).get(widgetPlaceRejeter).setHidden(false);

				if (nombreQuantiteDispoSupAuCmde != getSizeOfTable("MP_GES_CRM_TABARTICLES10", instance)) {
					irc.showBodyBlock("FragChekingMontantEnCours", true);
					String decisionx = (String) instance.getValue("MP_GES_CRM_VentePartielleTotal");
					if (decisionx.equals("Totale")) {
						// irc.showBodyBlock("FragVenteTotal", false);
						irc.showBodyBlock("FragDevisInterne", true);
					} else {
						// irc.showBodyBlock("FragVenteTotal", true);
						irc.showBodyBlock("FragDevisInterne", false);
					}
				} else {
					instance.setValue("MP_GES_CRM_VentePartielleTotal","Totale");
				}

			} else {
				irc.showBodyBlock("FragChekingMontantEnCours", false);
				// irc.showBodyBlock("FragVenteTotal", false);
				irc.showBodyBlock("FragDevisInterne", false);
				irc.getButtonContainer(2).get(widgetPlaceValider).setHidden(false);
				irc.getButtonContainer(2).get(widgetPlaceRejeter).setHidden(true);
			}
		}

		if (property.getName().equals("MP_GES_CRM_VentePartielleTotal")) {
			String decision = (String) instance.getValue("MP_GES_CRM_VentePartielleTotal");
			String decisionfocerDevis = (String) instance.getValue("MP_GES_CRM_DECISONFORCERDEVIS");
			float nombreQuantiteDispoSupAuCmde = (float) instance.getValue("MP_GES_CRM_FLAGQTEDISPOSUPQTECMD");
			if (decisionfocerDevis.equals("Oui")&& nombreQuantiteDispoSupAuCmde != getSizeOfTable("MP_GES_CRM_TABARTICLES10", instance)) {
				if (decision.equals("Totale")) {
					// irc.showBodyBlock("FragVenteTotal", false);
					irc.showBodyBlock("FragDevisInterne", true);
				} else {
					// irc.showBodyBlock("FragVenteTotal", true);
					irc.showBodyBlock("FragDevisInterne", false);
				}
			}

		}

	}

	// ******************************** SOUS PROCESSUS FACTURATION
	// ***************************************//
	// ******************************** ETABLISSEMENT BON DE SORTIE
	// **************************************//
	@SuppressWarnings("unchecked")
	public void getArticlesDisponibleDansStockSage(IWorkflowInstance instance) {
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("MP_GES_CRM_NDEVIS");// "MP_GES_CRM_NDEVIS"
		Devis devis = devisdao.getDevisWithStock(numDevis);
		float flagQteDispo = 0;
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES5");

		if (listLigneDevis != null) {
			instance.deleteLinkedResources(listLigneDevis);
			List<ILinkedResource> listLigneDevisQteNnDispo = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES8");
			if (listLigneDevisQteNnDispo != null)
				instance.deleteLinkedResources(listLigneDevisQteNnDispo);
		}

		// if(listLigneDevis == null || (listLigneDevis != null &&
		// listLigneDevis.isEmpty()))
		for (LigneDevis lignedevis : devis.getLigneDevises()) {
			if (lignedevis.getQtereste() > 0) {
				if (lignedevis.getQtedispo() > 0) {
					ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES5");
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE5",lignedevis.getDesignationArticle());
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE5",lignedevis.getCodeArticle());
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITE5",lignedevis.getQtereste());
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEDISPO",lignedevis.getQtedispo());
					instance.addLinkedResource(iLigneDevis);
				} else {
					ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES8");
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE8",lignedevis.getDesignationArticle());
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE8",lignedevis.getCodeArticle());
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITE8",lignedevis.getQtereste());
					iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEDISPO8",lignedevis.getQtedispo());
					instance.addLinkedResource(iLigneDevis);
				}
			}

			if (lignedevis.getQtedispo() > 0 && lignedevis.getQtedispo() - lignedevis.getQtereste() < 0) {
				flagQteDispo++;
			}
		}

		instance.setValue("MP_GES_CRM_FLAGQUANTITEDISPO", flagQteDispo);

	}

	public String afficheDate(Date date) {
		String stringDate = "";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			if(date!=null)
			stringDate = sdf.format(date);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return stringDate;
	}

	@SuppressWarnings("unchecked")
	public void setQteALivretoArticles(IWorkflowInstance instance) {
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES5");
		String devisNum = (String) instance.getValue("MP_GES_CRM_NDEVIS");
		Devis_DAO devisdao = new Devis_DAO();
		for (ILinkedResource lignedevis : listLigneDevis) {
			String codeArticle = (String) lignedevis.getValue("MP_GES_CRM_TABARTICLES_CODEARTICLE5");
			float qteDemandee = (float) lignedevis.getValue("MP_GES_CRM_TABARTICLES_QUANTITE5");
			float qteDisponible = (float) lignedevis.getValue("MP_GES_CRM_TABARTICLES_QUANTITEDISPO");
			float qteAlivrer = qteDemandee <= qteDisponible ? qteDemandee: qteDisponible;

			LigneDevis updatedLigneDevis = devisdao.getLigneDevis(codeArticle,devisNum);
			updatedLigneDevis.setQtealivre(qteAlivrer);
			devisdao.updateLigneDevisBis(updatedLigneDevis);
		}
	}

	public void generateBonSortie(IWorkflowInstance iw, IWorkflowModule im,IResourceController ir, ILinkedResource ilr) {
		try {
			String nombreDevis = (String) iw.getValue("MP_GES_CRM_NDEVIS");
			String reference = (String) iw.getValue("sys_Reference");
			
			String nBonSortie = reference.contains("FAC")?reference.replace("FAC", "BS"):reference.replace("DV", "BS");
			String adresse = (String) iw.getValue("MP_GES_CRM_Adresse");
			String reponsableMorlink = (String) iw.getValue("MP_GES_CRM_RespMORLINK");
			String modalitePaiement = (String) iw.getValue("MP_GES_CRM_MODPAIS");

			String transport = (String) iw.getValue("MP_GES_CRM_TRANSPORT");
			float nombrecolis = (float) iw.getValue("MP_GES_CRM_NBRECOLIS");
			String dateLivraison = afficheDate((Date) iw.getValue("MP_GES_CRM_DATELIVRAISON"));
			String numeroBL = (String) iw.getValue("MP_GES_CRM_NUMEROBL");

			String filename = nBonSortie;
			String nomFichierPDF = filename + ".pdf";

			File newFile = new File(TurbineServlet.getRealPath("DEVIS")+ "\\OUT\\" + nomFichierPDF);
			newFile.createNewFile();

			Map<String, Object> Parametr = new HashMap<String, Object>();
			Parametr.put("NumeroDevis", nombreDevis);
			Parametr.put("nbonsortie", nBonSortie);
			Parametr.put("adresse", adresse);
			Parametr.put("responsableMorlink", reponsableMorlink);
			Parametr.put("modalitePaiement", modalitePaiement);

			Parametr.put("transport", transport);
			Parametr.put("nombrecolis", nombrecolis);
			Parametr.put("datelivraison", dateLivraison);
			Parametr.put("nbonlivraison", numeroBL);

			FileManager path = new FileManager();
			Connection connection = getConnection();

			GenererPDF.generer("bonsortiemorlink", path.getOutDir(),nomFichierPDF, Parametr, connection);
			iw.setValue("MP_GES_CRM_BS", null);
			im.addAttachment(iw, "MP_GES_CRM_BS", nomFichierPDF,TurbineServlet.getRealPath("DEVIS") + "\\OUT\\"+ nomFichierPDF);
			ir.alert("Document g�n�r� avec succ�s !!");
		} catch (Exception e) {
			e.printStackTrace();
			ir.alert(e.getMessage());
		}
	}

	// ***************************************************************************************************//
	// ************************************ Etablissement du BL
	// ******************************************//

	@SuppressWarnings("unchecked")
	public void getArticlesDisponibleDansStockSagePourBL(
			IWorkflowInstance instance) {
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("MP_GES_CRM_NDEVIS");

		String reference = (String) instance.getValue("sys_Reference");
		String nboncommande = reference.replace("FAC", "BC");
		instance.setValue("MP_GES_CRM_NBONCOMMANDE", nboncommande);

		Devis devis = devisdao.getDevisWithStock(numDevis);
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES7");

		if (listLigneDevis == null|| (listLigneDevis != null && listLigneDevis.isEmpty()))
			for (LigneDevis lignedevis : devis.getLigneDevises()) {
				if (lignedevis.getQtereste() > 0)
					if (lignedevis.getQtedispo() > 0) {
						ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES7");
						iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE7",lignedevis.getDesignationArticle());
						iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE7",lignedevis.getCodeArticle());
						iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITELIVRE7",lignedevis.getQtealivre());
						instance.addLinkedResource(iLigneDevis);
					}

			}
	}

	public void generateBonLivraison(IWorkflowInstance iw, IWorkflowModule im,IResourceController ir, ILinkedResource ilr) {
		try {
			String nombreDevis = (String) iw.getValue("MP_GES_CRM_NDEVIS");
			String reference = (String) iw.getValue("sys_Reference");
			String nbonlivraison = reference.contains("FAC")?reference.replace("FAC", "BL"):reference.replace("DV", "BL");
			String adresse = (String) iw.getValue("MP_GES_CRM_Adresse");
			String bcNumero = (String) iw.getValue("MP_GES_CRM_NBONCOMMANDE");

			String filename = nbonlivraison;
			String nomFichierPDF = filename + ".pdf";

			File newFile = new File(TurbineServlet.getRealPath("DEVIS")+ "\\OUT\\" + nomFichierPDF);
			newFile.createNewFile();

			Map<String, Object> Parametr = new HashMap<String, Object>();
			Parametr.put("NumeroDevis", nombreDevis);
			Parametr.put("nbonlivraison", nbonlivraison);
			Parametr.put("adresse", adresse);
			Parametr.put("numBC", bcNumero);

			FileManager path = new FileManager();
			Connection connection = getConnection();

			GenererPDF.generer("bonlivraisonmorlink", path.getOutDir(),nomFichierPDF, Parametr, connection);
			iw.setValue("MP_GES_CRM_BL", null);
			im.addAttachment(iw, "MP_GES_CRM_BL", nomFichierPDF,TurbineServlet.getRealPath("DEVIS") + "\\OUT\\"+ nomFichierPDF);
			ir.alert("Document g�n�r� avec succ�s !!");
		} catch (Exception e) {
			e.printStackTrace();
			ir.alert(e.getMessage());
		}
	}

	// ***************************************************************************************************//
	// ************************************ Etablissement du Facture
	// *************************************//

	@SuppressWarnings("unchecked")
	public void caculateSommeFacture(IWorkflowInstance instance) {
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("MP_GES_CRM_NDEVIS");
		Devis devis = devisdao.getDevisWithStock(numDevis);
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES9");
		Object tvaob = (Object) instance.getValue("MP_GES_CRM_SP_NTVA");
		float tva = 0f;
		if (tvaob instanceof Float) {
			tva = (float) tvaob;
		} else if (tvaob instanceof Double) {
			double tvaDouble = (double) tvaob;
			tva = Float.parseFloat(tvaDouble + "");
		}
		float prixGlobalHT = 0f;
		if (listLigneDevis == null|| (listLigneDevis != null && listLigneDevis.isEmpty()))
			for (LigneDevis lignedevis : devis.getLigneDevises()) {
				if (lignedevis.getQtereste() > 0)
					if (lignedevis.getQtedispo() > 0) {
						ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES9");
						iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE9",lignedevis.getDesignationArticle());
						iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE9",lignedevis.getCodeArticle());
						iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QTE9",lignedevis.getQtealivre());
						iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXHT9",getStringFromNombre(lignedevis.getPrixtarifADV()));
						iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXTOTALHT9",getStringFromNombre(lignedevis.getQtealivre()* lignedevis.getPrixtarifADV()));
						instance.addLinkedResource(iLigneDevis);

					}

			}

		for (LigneDevis lignedevis : devis.getLigneDevises()) {
			if (lignedevis.getQtereste() > 0)
				if (lignedevis.getQtedispo() > 0) {
					prixGlobalHT += lignedevis.getQtealivre()* lignedevis.getPrixtarifADV();
				}
		}

		instance.setValue("MP_GES_CRM_SP_MONTANTGLOBALHT",getStringFromNombre(prixGlobalHT));
		instance.setValue("MP_GES_CRM_SP_MONTANTGLOBALTTC",getStringFromNombre(prixGlobalHT * tva + prixGlobalHT));
	}

	@SuppressWarnings("unchecked")
	public void setQteALivretoQteLivree(IWorkflowInstance instance) {
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES9");
		String devisNum = (String) instance.getValue("MP_GES_CRM_NDEVIS");
		Devis_DAO devisdao = new Devis_DAO();
		for (ILinkedResource lignedevis : listLigneDevis) {
			String codeArticle = (String) lignedevis.getValue("MP_GES_CRM_TABARTICLES_CODEARTICLE9");
			float qteAlivree = (float) lignedevis.getValue("MP_GES_CRM_TABARTICLES_QTE9");

			LigneDevis updatedLigneDevis = devisdao.getLigneDevis(codeArticle,devisNum);
			updatedLigneDevis.setQtelivre(updatedLigneDevis.getQtelivre()+ qteAlivree);
			updatedLigneDevis.setQtereste(updatedLigneDevis.getQtereste()- qteAlivree);
			updatedLigneDevis.setQtealivre(0f);
			devisdao.updateLigneDevisBis(updatedLigneDevis);
		}
	}

	public void generateFacture(IWorkflowInstance iw, IWorkflowModule im,
			IResourceController ir, ILinkedResource ilr) {
		try {
			String nombreDevis = (String) iw.getValue("MP_GES_CRM_NDEVIS");
			String reference = (String) iw.getValue("sys_Reference");
			String nfacture = reference.contains("FAC")?reference.replace("FAC", "FAC"):reference.replace("DV", "FAC");;
			String adresse = (String) iw.getValue("MP_GES_CRM_Adresse");
			String bcNumero = (String) iw.getValue("MP_GES_CRM_NBONCOMMANDE");
			String modalitepaiement = (String) iw.getValue("MP_GES_CRM_MODPAIS");
			String montantHT = (String) iw.getValue("MP_GES_CRM_SP_MONTANTGLOBALHT");
			String montantTTC = (String) iw.getValue("MP_GES_CRM_SP_MONTANTGLOBALTTC");
			String tva = getStringFromNombre(getNombreFromString(montantTTC)- getNombreFromString(montantHT));
			float nombrecolis = (float) iw.getValue("MP_GES_CRM_NBRECOLIS");

			String filename = nfacture;
			String nomFichierPDF = filename + ".pdf";

			File newFile = new File(TurbineServlet.getRealPath("DEVIS")+ "\\OUT\\" + nomFichierPDF);
			newFile.createNewFile();

			Map<String, Object> Parametr = new HashMap<String, Object>();
			Parametr.put("NumeroDevis", nombreDevis);
			Parametr.put("nfacture", nfacture);
			Parametr.put("adresse", adresse);
			Parametr.put("numBC", bcNumero);
			Parametr.put("MontantGlobalHT", montantHT + " Dhs");
			Parametr.put("TVA", tva + " Dhs");
			Parametr.put("MontantGlobalTTC", montantTTC + " Dhs");
			Parametr.put("modalitePaiement", modalitepaiement);
			Parametr.put("nombrecolis", nombrecolis);

			FileManager path = new FileManager();
			Connection connection = getConnection();

			GenererPDF.generer("facturemorlink", path.getOutDir(),nomFichierPDF, Parametr, connection);
			iw.setValue("MP_GES_CRM_FACTURE", null);
			im.addAttachment(iw, "MP_GES_CRM_FACTURE", nomFichierPDF,TurbineServlet.getRealPath("DEVIS") + "\\OUT\\"+ nomFichierPDF);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ***************************************************************************************************//
	// ************************************ Modifier Prix ADV
	// ********************************************//
	@SuppressWarnings("unchecked")
	public void getArticlesAModifierLePrix(IWorkflowInstance instance) {
		// TODO Auto-generated method stub
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("MP_GES_CRM_NDEVIS");
		Devis devis = devisdao.getDevisWithStock(numDevis);
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES6");
		if (listLigneDevis != null) {
			instance.deleteLinkedResources(listLigneDevis);
		}
		// if(listLigneDevis == null || (listLigneDevis != null &&
		// listLigneDevis.isEmpty())){
		for (LigneDevis lignedevis : devis.getLigneDevises()) {
			if (lignedevis.getQtedispo() <= lignedevis.getQtealivre()&& lignedevis.getQtealivre() > 0) {
				ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES6");
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE6",lignedevis.getDesignationArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE6",lignedevis.getCodeArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEDEM",lignedevis.getQte());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEDISP",lignedevis.getQtedispo());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHTANC",getStringFromNombre(lignedevis.getPrixtarifADV()));
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHTNEW",getStringFromNombre(lignedevis.getPrixtarifADV()));
				instance.addLinkedResource(iLigneDevis);
			}
		}
		// }

	}

	public void onPropertyChangedOnTabArticle6(IProperty property,IWorkflowInstance instance) {
		// TODO Auto-generated method stub

		if (property.getName().equals("MP_GES_CRM_TABARTICLES_PRIXVENTEHTNEW")) {
			if (instance.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHTNEW") != null) {
				float nouveauxPrixAdv = getNombreFromString((String) instance.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHTNEW"));
				instance.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHTNEW",getStringFromNombre(nouveauxPrixAdv));
			}

		}
	}

	@SuppressWarnings("unchecked")
	public void setNouveauxPrixToArticlesAModifier(IWorkflowInstance instance) {
		// TODO Auto-generated method stub
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES6");
		String devisNum = (String) instance.getValue("MP_GES_CRM_NDEVIS");
		Devis_DAO devisdao = new Devis_DAO();
		for (ILinkedResource lignedevis : listLigneDevis) {
			String codeArticle = (String) lignedevis.getValue("MP_GES_CRM_TABARTICLES_CODEARTICLE6");
			float nouveauxPrixADV = getNombreFromString((String) lignedevis.getValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHTNEW"));
			LigneDevis updatedLigneDevis = devisdao.getLigneDevis(codeArticle,devisNum);
			updatedLigneDevis.setPrixtarifADV(nouveauxPrixADV);
			devisdao.updateLigneDevisBis(updatedLigneDevis);
		}
	}

	// controle affichage des �tapes

	public void controleAffichageEnLecture(IWorkflowInstance workflowInstance,IResourceController resourceController, IWorkflowModule iwm) {
		

			String montantEncoursText = (String) workflowInstance.getValue("MP_GES_CRM_MONTANTENCOURS");
			String montantDevisText = (String) workflowInstance.getValue("MP_GES_CRM_MONTANTTTCED");
			float montantEncours = getNombreFromString(montantEncoursText==null?"0":montantEncoursText);
			float montantDevis = getNombreFromString(montantDevisText==null?"1":montantDevisText);

			String decision = (String) workflowInstance.getValue("MP_GES_CRM_DECISONFORCERDEVIS");
			// V�rifier si la decision est Oui
			if ((montantDevis > montantEncours && decision.equals("Oui"))|| montantDevis <= montantEncours) {

				// v�rifier si le montant de l'encours est inf�rieur strictement au montant du devis
				if (montantDevis > montantEncours) {
					// si oui alors afficher decision du forcage du devis
					resourceController.showBodyBlock("FragDecision", true);
				} else {
					resourceController.showBodyBlock("FragDecision", false);
				}

				// get nombre d'articles dont le stock est 0
				float nombreQuantiteEgalZero = (float) workflowInstance.getValue("MP_GES_CRM_FLAGQUANTITEDISPOINIT");
				// r�cup�rer le nombre d'articles dont le stock qt� dispo est sup�rieur ou �gal � la qt� command�e
				float nombreQuantiteDispoSupAuCmde = (float) workflowInstance.getValue("MP_GES_CRM_FLAGQTEDISPOSUPQTECMD");

				if (nombreQuantiteEgalZero == 0) {

					resourceController.showBodyBlock("FragChekingMontantEnCours", true);
					resourceController.showBodyBlock("FragDecision", true);
					resourceController.showBodyBlock("FragDecisionQteZero",false);

					if (nombreQuantiteDispoSupAuCmde == getSizeOfTable("MP_GES_CRM_TABARTICLES10", workflowInstance)) {
						resourceController.showBodyBlock("FragChekingMontantEnCours", false);
						resourceController.showBodyBlock("FragDevisInterne",false);
					} else {
						resourceController.showBodyBlock("FragChekingMontantEnCours", true);
						String typeVente = (String) workflowInstance.getValue("MP_GES_CRM_VentePartielleTotal");
						if (typeVente.equals("Totale")) {
							resourceController.showBodyBlock("FragDevisInterne", true);
						} else {
							resourceController.showBodyBlock("FragDevisInterne", false);
						}
					}

					// checkMontantDeLencours(instance, irc);

				} else {
					resourceController.showBodyBlock("FragChekingMontantEnCours", false);
					resourceController.showBodyBlock("FragDevisInterne", false);
					resourceController.showBodyBlock("FragDecision", false);
					resourceController.showBodyBlock("FragDecisionQteZero",true);
					resourceController.showBodyBlock("FragDevisInterne", false);
				}

				

			//}

		}

	}

	// ----------------------------------------------------------------------------------------------------------------------
	// --------------------------- GESTION DES CATALOGUES

	@SuppressWarnings("unchecked")
	public  String copyPJToVDocWar(IWorkflowInstance iWorkflowInstance, String sysnamePJ, String codeArticle) throws IOException {
		try {

			List<IAttachment> pjs = (List<IAttachment>) iWorkflowInstance.getValue(sysnamePJ);

			String filename = null;
			for (IAttachment pj : pjs) {
				filename = pj.getShortName().contains(codeArticle)?pj.getShortName():codeArticle+pj.getShortName();
				File newFile = null;
				try{
					newFile = new File(TurbineServlet.getRealPath("CATALOGUE")+ "//" + codeArticle + "//" + filename);
					newFile.createNewFile();
				}catch (Exception e) {
					File newdir = new File(TurbineServlet.getRealPath("CATALOGUE")+ "//" + codeArticle+"//");
					newdir.mkdir();
					newFile = new File(TurbineServlet.getRealPath("CATALOGUE")+ "//" + codeArticle + "//" + filename);
					newFile.createNewFile();
					//e.printStackTrace();;
				}
				
				InputStream is = pj.getInputStream();
				OutputStream os = new FileOutputStream(newFile);
				byte[] buffer = new byte[is.available()];
				int length;
				while ((length = is.read(buffer)) > 0) {
					os.write(buffer, 0, length);
				}
				is.close();
				os.close();
			}

			return filename;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}

	}

	public void onPropertyChangedCatalogue(IWorkflowInstance instance,IWorkflowModule module, IProperty property) {
		if (property.getName().equals("MP_GES_CATALOGUES_ARTICLE")) {
			article = (Article) instance.getValue("MP_GES_CATALOGUES_ARTICLE");
			if (article != null) {
				Article_DAO daoArticle = new Article_DAO();
				article = daoArticle.getArticle(article.getReference());
				instance.setValue("MP_GES_CATALOGUES_CODEARTICLE", article.getReference());
				instance.setValue("MP_GES_CATALOGUES_CATALOGUE", null);
				try {
					if(article.getCatalogue()!=null)
					module.addAttachment(instance, "MP_GES_CATALOGUES_CATALOGUE", article.getCatalogue().getCatalogueArticle(),TurbineServlet.getRealPath("CATALOGUE") + "\\"+ article.getReference()+ "\\"+ article.getCatalogue().getCatalogueArticle());
				} catch (WorkflowModuleException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else{
				
				instance.setValue("MP_GES_CATALOGUES_CATALOGUE", null);
			}
		}
	}

	public void onAfterValidateCatalogue(IWorkflowInstance instance) {
				Article_DAO daoArticle = new Article_DAO();
				String codeArticle = (String) instance.getValue("MP_GES_CATALOGUES_CODEARTICLE");
				Article updatedArticle = daoArticle.getArticle(codeArticle);
				
			    
				try {
					String filecatalogue = copyPJToVDocWar(instance, "MP_GES_CATALOGUES_CATALOGUE", updatedArticle.getReference());
					Catalogue updatedcatalogue = updatedArticle.getCatalogue();
					if(updatedcatalogue==null){
						updatedcatalogue = new Catalogue();
						updatedcatalogue.setArticle(updatedArticle);
						updatedcatalogue.setCatalogueArticle(filecatalogue);
						updatedArticle.setCatalogue(updatedcatalogue);
					}
					else{
						updatedcatalogue.setCatalogueArticle(filecatalogue);
						updatedArticle.setCatalogue(updatedcatalogue);
					}
					
					
					daoArticle.updateArticle(updatedArticle);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
	}

	
	// ----------------------------------------------------------------------------------------------------------------------
	// --------------------------- GESTION DES LIVRAISONS HORS VENTE
	
	public void onPropertyChangedClientLiv(IWorkflowInstance instance, IProperty property){
		if (property.getName().equals("MP_SP_NEWLIVRHORVENTE_CLIENT")) {

			String clientNCompte = (String) instance.getValue("MP_SP_NEWLIVRHORVENTE_CLIENT");
			if (clientNCompte != null) {
				Client_DAO client_dao = new Client_DAO();
				Client client = client_dao.getClient(clientNCompte);
				instance.setValue("MP_SP_NEWLIVRHORVENTE_CODECLIENT", client.getNcompte());
				instance.setValue("MP_SP_NEWLIVRHORVENTE_INTITULECLIENT",client.getIntitule());
				instance.setValue("MP_GES_CRM_EMAILCLIENT",client.getEmail());
			}
			else{
				instance.setValue("MP_SP_NEWLIVRHORVENTE_CODECLIENT", null);
				instance.setValue("MP_SP_NEWLIVRHORVENTE_INTITULECLIENT",null);
				instance.setValue("MP_GES_CRM_EMAILCLIENT",null);
			}

		}
	}
	
	//************************* on propertyChangedTableau
	public void onPropertyChangedClientLivTab(IWorkflowInstance instance, IProperty property){
		if (property.getName().equals("MP_SP_NEWLIVRHORVENTE_TABARTICLES_ARTICLE")) {
 
			Article article = (Article) instance.getValue("MP_SP_NEWLIVRHORVENTE_TABARTICLES_ARTICLE");
			if (article != null) {
				//Article_DAO article_dao = new Article_DAO();
				//Article article = article_dao.getArticle(refArticle);
				instance.setValue("MP_SP_NEWLIVRHORVENTE_TABARTICLES_CODEARTICLE", article.getReference());
				instance.setValue("MP_SP_NEWLIVRHORVENTE_TABARTICLES_DESIARTICLE", article.getDesignation());
			}
			else{
				instance.setValue("MP_SP_NEWLIVRHORVENTE_TABARTICLES_CODEARTICLE", null);
				instance.setValue("MP_SP_NEWLIVRHORVENTE_TABARTICLES_DESIARTICLE",null);
			}

		}
	}
	
	@SuppressWarnings("unchecked")
	public void onAfterSubmitLivraison(IWorkflowInstance instance){
		Client_DAO client_dao = new Client_DAO();
		Article_DAO article_dao = new Article_DAO();
		Livraison_DAO livraison_dao = new Livraison_DAO();
		
		String codeLivraison = (String) instance.getValue("sys_Reference");
		
		Livraison livraisonUpdate = livraison_dao.getLivraison(codeLivraison);
		if(livraisonUpdate!=null){
			livraison_dao.deleteLivraison(codeLivraison);
		}
		
		String refClient = (String) instance.getValue("MP_SP_NEWLIVRHORVENTE_CODECLIENT");
		Client client = client_dao.getClient(refClient);
		Date dateRetourPrevu = (Date) instance.getValue("MP_SP_NEWLIVRHORVENTE_DATERETOUR");
		String typeLivraison = (String) instance.getValue("MP_SP_NEWLIVRHORVENTE_TYPELIVRAISON");
			
		Livraison livraison = new Livraison();
		livraison.setClient(client);
		livraison.setDateLivraison(null);
		livraison.setDateRetourPrevu(dateRetourPrevu);
		livraison.setDateRetourReel(null);
		livraison.setType(typeLivraison);
		livraison.setCodeLivraison(codeLivraison);
		List<LigneLivraison> lignesLivraison = new ArrayList<LigneLivraison>();
			
		List<ILinkedResource> articles = (List<ILinkedResource>) instance.getLinkedResources("MP_SP_NEWLIVRHORVENTE_TABARTICLES");
		for(ILinkedResource lignearticle : articles){
			LigneLivraison ligneLivraison = new LigneLivraison();
			String codeArticle = (String) lignearticle.getValue("MP_SP_NEWLIVRHORVENTE_TABARTICLES_CODEARTICLE");
			Article article =  article_dao.getArticle(codeArticle);
			float quantiteLivrer = (float) lignearticle.getValue("MP_SP_NEWLIVRHORVENTE_TABARTICLES_QUANTITE");
			ligneLivraison.setArticle(article);
			ligneLivraison.setQuantite(quantiteLivrer);
			ligneLivraison.setLivraison(livraison);
			lignesLivraison.add(ligneLivraison);
				
		}
			
		livraison.setLigneslivraison(lignesLivraison);
		livraison_dao.addLivraison(livraison);	
	
	}
	
	public void generateBonSortieLivraison(IWorkflowInstance iw, IWorkflowModule im,IResourceController ir, ILinkedResource ilr) {
		try {
			String nombreDevis 			= iw.getValue("MP_GES_CRM_NDEVIS") != null ? (String) iw.getValue("MP_GES_CRM_NDEVIS")+ "" : "";
			String reference 			= iw.getValue("sys_Reference") != null ? (String) iw.getValue("sys_Reference")+ "" : "";
			String nBonSortie 			= reference.replace("LIV", "BSLIV");
			String adresse 				= iw.getValue("MP_GES_CRM_Adresse") != null ? (String) iw.getValue("MP_GES_CRM_Adresse"): "";
			String reponsableMorlink 	= iw.getValue("MP_GES_CRM_RespMORLINK") != null ?(String) iw.getValue("MP_GES_CRM_RespMORLINK"): "";
			String modalitePaiement 	= iw.getValue("MP_GES_CRM_MODPAIS") != null ?(String) iw.getValue("MP_GES_CRM_MODPAIS"): "";

			String transport 			= iw.getValue("MP_GES_CRM_TRANSPORT") != null ?(String) iw.getValue("MP_GES_CRM_TRANSPORT"): "";
			String nombrecolis 			= iw.getValue("MP_GES_CRM_NBRECOLIS")!=null ? (float) iw.getValue("MP_GES_CRM_NBRECOLIS")+ "" : "";
			String dateLivraison 		= afficheDate((Date) iw.getValue("MP_GES_CRM_DATELIVRAISON"));
			String numeroBL 			= iw.getValue("MP_GES_CRM_NUMEROBL") != null ?(String) iw.getValue("MP_GES_CRM_NUMEROBL"): "";

			String filename = nBonSortie;
			String nomFichierPDF = filename + ".pdf";

			File newFile = new File(TurbineServlet.getRealPath("DEVIS")+ "\\OUT\\" + nomFichierPDF);
			newFile.createNewFile();

			Map<String, Object> Parametr = new HashMap<String, Object>();
			Parametr.put("NumeroBS", reference);
			Parametr.put("NumeroDevis", nombreDevis);
			Parametr.put("nbonsortie", nBonSortie);
			Parametr.put("adresse", adresse);
			Parametr.put("responsableMorlink", reponsableMorlink);
			Parametr.put("modalitePaiement", modalitePaiement);

			Parametr.put("transport", transport);
			Parametr.put("nombrecolis", nombrecolis);
			Parametr.put("datelivraison", dateLivraison);
			Parametr.put("nbonlivraison", numeroBL);

			FileManager path = new FileManager();
			Connection connection = getConnection();

			String typeLivraison = (String) iw.getValue("MP_SP_NEWLIVRHORVENTE_TYPELIVRAISON");
			if(typeLivraison.equals("D�monstration"))
				GenererPDF.generer("bonsortiemorlinkdemonstration", path.getOutDir(),nomFichierPDF, Parametr, connection);
			else if(typeLivraison.equals("Showroom"))
				GenererPDF.generer("bonsortiemorlinkshowroom", path.getOutDir(),nomFichierPDF, Parametr, connection);
			else 
				GenererPDF.generer("bonsortiemorlinkpret", path.getOutDir(),nomFichierPDF, Parametr, connection);
			
			iw.setValue("MP_GES_CRM_BS", null);
			im.addAttachment(iw, "MP_GES_CRM_BS", nomFichierPDF,TurbineServlet.getRealPath("DEVIS") + "\\OUT\\"+ nomFichierPDF);
			ir.alert("Document g�n�r� avec succ�s !!");
		} catch (Exception e) {
			e.printStackTrace();
			ir.alert(e.getMessage());
		}
	}

	public void onAfterSubmitReceptionLivraison(IWorkflowInstance instance) {
		// TODO Auto-generated method stub
		String codeLivraison =  (String) instance.getValue("sys_Reference");
		Date dateRetourReel 	 =  (Date) instance.getValue("MP_SP_NEWLIVRHORVENTE_DATERECEPTION");
		Livraison_DAO livraison_DAO = new Livraison_DAO();
		Livraison livraison = livraison_DAO.getLivraison(codeLivraison);
		livraison.setDateRetourReel(dateRetourReel);
		livraison_DAO.updateLivraison(livraison);
	}

	public void onAfterSubmitGenerationBS(IWorkflowInstance instance) {
		// TODO Auto-generated method stub
		String codeLivraison =  (String) instance.getValue("sys_Reference");
		Date dateLivraison 	 =  (Date) instance.getValue("MP_GES_CRM_DATELIVRAISON");
		Livraison_DAO livraison_DAO = new Livraison_DAO();
		Livraison livraison = livraison_DAO.getLivraison(codeLivraison);
		livraison.setDateLivraison(dateLivraison);
		livraison_DAO.updateLivraison(livraison);
	}

	
	public boolean checkQuantite(IWorkflowInstance instance) {
		// TODO Auto-generated method stub
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("MP_GES_CRM_NDEVIS");
		Devis devis = devisdao.getDevisWithStock(numDevis);
		boolean checkDisponibility = true;
		for (LigneDevis lignedevis : devis.getLigneDevises()) {
			if (lignedevis.getQtedispo() < lignedevis.getQte()) {
				checkDisponibility =  false;
				//instance.setValue("MP_GES_CRM_ALERTNONDISPOSTOCK", "La commande va �tre bloqu�e due au non disponibilit� du stock");
			}
		}
		
		return checkDisponibility;
	}
	
	@SuppressWarnings("unchecked")
	public void getArticlesDisponibleDansStockSageForConfirmationDevis(IWorkflowInstance instance, IResourceController irc) {
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("sys_Reference");
		Devis devis = devisdao.getDevisWithStock(numDevis);
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES15");

		if (listLigneDevis != null) {
			instance.deleteLinkedResources(listLigneDevis);
		}

		for (LigneDevis lignedevis : devis.getLigneDevises()) {
				ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES15");
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE15",lignedevis.getDesignationArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE15",lignedevis.getCodeArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITE15",lignedevis.getQtereste());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEDISPO15",lignedevis.getQtedispo());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT15",lignedevis.getPrixpardefaut());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHTCOM15",lignedevis.getPrixtarifCommercial());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHTADV15",lignedevis.getPrixtarifADV());
				instance.addLinkedResource(iLigneDevis);
		}

	}
	
	@SuppressWarnings("unchecked")
	public void getArticlesDisponibleDansStockSageForAffectationStock(IWorkflowInstance instance, IResourceController irc) {
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("sys_Reference");
		Devis devis = devisdao.getDevisWithStock(numDevis);
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES16");

		if (listLigneDevis != null) {
			instance.deleteLinkedResources(listLigneDevis);
		}

		for (LigneDevis lignedevis : devis.getLigneDevises()) {
				ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES16");
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE16",lignedevis.getDesignationArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE16",lignedevis.getCodeArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITE16",getStringFromNombre(lignedevis.getQtereste()));
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEDISPO16",getStringFromNombre(lignedevis.getQtedispo()));
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEAFFECTE16",lignedevis.getQtedispo());
				instance.addLinkedResource(iLigneDevis);
		}

	}
	
	
	public void onAfterSaveTableArticle16OfCheckDispoOfStockPhysique(IWorkflowInstance instance) {
		byte flag = 0 ;
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) (instance.getValue("MP_GES_CRM_TABARTICLES16"));
		if(listLigneDevis != null)
		for (ILinkedResource lignedevis : listLigneDevis) {
			float quantiteDemande = getNombreFromString((String) lignedevis.getValue("MP_GES_CRM_TABARTICLES_QUANTITE16"));
			//float quantiteDispo = getNombreFromString((String) lignedevis.getValue("MP_GES_CRM_TABARTICLES_QUANTITEDISPO16"));
			float quantitePhysique = (float) lignedevis.getValue("MP_GES_CRM_TABARTICLES_QUANTITEAFFECTE16");
			if(quantiteDemande>quantitePhysique) {
				flag = 1;
				break;
			}
		}
		
		
		instance.setValue("MP_GES_CRM_FLAGQTEPHYSIQUE", flag);
	}

	@SuppressWarnings("unchecked")
	public void setStatutQteOfMagasinier(IWorkflowInstance instance) {
		// TODO Auto-generated method stub
		String numdevis = (String) instance.getValue("sys_Reference");
		Devis_DAO devisdao = new Devis_DAO();
		
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) (instance.getValue("MP_GES_CRM_TABARTICLES16"));
		if(listLigneDevis!=null)
		for (ILinkedResource ilignedevis : listLigneDevis) {
			String codeArticle = (String) ilignedevis.getValue("MP_GES_CRM_TABARTICLES_CODEARTICLE16");
			LigneDevis ligneDevis = devisdao.getLigneDevis(codeArticle, numdevis);
			float quantitePhysique = (float) ilignedevis.getValue("MP_GES_CRM_TABARTICLES_QUANTITEAFFECTE16");
			ligneDevis.setQteAffecte(quantitePhysique);
			ligneDevis.setStatut(true);
			devisdao.updateLigneDevisBis(ligneDevis);
		}
	}

	public void getArticlesToMakeDecision(IWorkflowInstance instance) {
		// TODO Auto-generated method stub
		Devis_DAO devisdao = new Devis_DAO();
		String numDevis = (String) instance.getValue("sys_Reference");
		Devis devis = devisdao.getDevis(numDevis);
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) instance.getValue("MP_GES_CRM_TABARTICLES17");

		if (listLigneDevis != null) {
			instance.deleteLinkedResources(listLigneDevis);
		}

		for (LigneDevis lignedevis : devis.getLigneDevises()) {
				ILinkedResource iLigneDevis = instance.createLinkedResource("MP_GES_CRM_TABARTICLES17");
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_ARTICLE17",lignedevis.getDesignationArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_CODEARTICLE17",lignedevis.getCodeArticle());
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITE17",getStringFromNombre(lignedevis.getQtereste()));
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEDISPO17",getStringFromNombre(lignedevis.getQtedispo()));
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_QUANTITEAFFECTE17",getStringFromNombre(lignedevis.getQteAffecte()));
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHT17",getStringFromNombre(lignedevis.getPrixpardefaut()));
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_PRIXVENTEHTCOM17",getStringFromNombre(lignedevis.getPrixtarifCommercial()));
				iLigneDevis.setValue("MP_GES_CRM_TABARTICLES_NVPRIXVENTEHTADV17",getStringFromNombre(lignedevis.getPrixtarifADV()));
				instance.addLinkedResource(iLigneDevis);
		}
	}
	
	@SuppressWarnings("unchecked")
	public void setStatutQteOfADV(IWorkflowInstance instance) {
		// TODO Auto-generated method stub
		String numdevis = (String) instance.getValue("sys_Reference");
		Devis_DAO devisdao = new Devis_DAO();
		
		List<ILinkedResource> listLigneDevis = (List<ILinkedResource>) (instance.getValue("MP_GES_CRM_TABARTICLES17"));
		for (ILinkedResource ilignedevis : listLigneDevis) {
			String codeArticle = (String) ilignedevis.getValue("MP_GES_CRM_TABARTICLES_CODEARTICLE17");
			LigneDevis ligneDevis = devisdao.getLigneDevis(codeArticle, numdevis);
			boolean quantitePhysique = ((String) ilignedevis.getValue("MP_GES_CRM_TABARTICLES_STATUTARTICLE17")== "Oui" ? true : false);
			ligneDevis.setStatut(quantitePhysique);
			devisdao.updateLigneDevisBis(ligneDevis);
		}
	}

	

	
	
	
}
