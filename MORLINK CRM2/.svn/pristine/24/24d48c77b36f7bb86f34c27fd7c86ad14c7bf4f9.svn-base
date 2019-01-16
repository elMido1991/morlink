package com.morlink.dev.processes.crm;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.morlink.dev.servicesmetier.crm.MEtablissementDevis;

public class _5_1ConfirmationDevis extends BaseDocumentExtension {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private MEtablissementDevis med;

	@Override
	public boolean onAfterLoad()
	{
		// TODO Auto-generated method stub
		med = new MEtablissementDevis();
		med.getArticlesDisponibleDansStockSageForConfirmationDevis(getWorkflowInstance(), getResourceController());
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property)
	{
		// TODO Auto-generated method stub
		
		super.onPropertyChanged(property);
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action)
	{
		// TODO Auto-generated method stub
		boolean checkQuantite = true;
		if(action.getName().equals("Valider8")) {
			checkQuantite = med.checkQuantite(getWorkflowInstance());
			if(!checkQuantite) {
				getResourceController().alert("La commande va être bloquée due au non disponibilité du stock");
			}
		}
			
		return checkQuantite;
	}
	
	@Override
	public boolean onAfterSubmit(IAction action)
	{
		// TODO Auto-generated method stub
		
		return super.onAfterSubmit(action);
	}
}
