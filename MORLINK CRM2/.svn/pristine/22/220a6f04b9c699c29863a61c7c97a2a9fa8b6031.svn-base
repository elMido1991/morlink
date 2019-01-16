package com.morlink.dev.processes.crm.spfacturation;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.morlink.dev.servicesmetier.crm.MEtablissementDevis;

public class _3_EtablissementBonLivraison extends BaseDocumentExtension {
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
		med.getArticlesDisponibleDansStockSagePourBL(getWorkflowInstance());
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
		return super.onBeforeSubmit(action);
	}
	
	@Override
	public boolean onAfterSubmit(IAction action)
	{
		// TODO Auto-generated method stub
		
		return super.onAfterSubmit(action);
	}
}
