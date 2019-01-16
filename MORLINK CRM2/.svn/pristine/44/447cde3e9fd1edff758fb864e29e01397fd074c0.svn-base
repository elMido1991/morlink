package com.morlink.dev.processes.crm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.morlink.dev.beans.Client;
import com.morlink.dev.beans.Devis;
import com.morlink.dev.beans.LigneDevis;
import com.morlink.dev.connexiondb.*;
import com.morlink.dev.dao.Devis_DAO;
import com.morlink.dev.servicesmetier.crm.MEtablissementDevis;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.ILinkedResource;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.axemble.vdp.ui.framework.composites.base.models.views.ViewModelColumn;

public class _1_EtablissementDevis extends BaseDocumentExtension
{
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
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property)
	{
		// TODO Auto-generated method stub
		med.onPropertyChangedOnDevis(property, getWorkflowInstance(),getWorkflowModule());
		super.onPropertyChanged(property);
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action)
	{
		// TODO Auto-generated method stub
		if(action.getName().equals("Valider"))
			med.getPJCatalogues(getWorkflowInstance(),getWorkflowModule());
		return super.onBeforeSubmit(action);
	}
	
	@Override
	public boolean onAfterSubmit(IAction action)
	{
		// TODO Auto-generated method stub
		
		//add devis et ligne devis
		if(action.getName().equals("Valider"))
			med.addDevisToDDB(action, getWorkflowInstance());
		return super.onAfterSubmit(action);
	}
}
