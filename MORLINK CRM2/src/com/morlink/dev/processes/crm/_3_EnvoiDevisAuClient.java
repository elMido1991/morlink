package com.morlink.dev.processes.crm;
import com.morlink.dev.servicesmetier.crm.MEtablissementDevis;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IAction;
import com.axemble.vdoc.sdk.interfaces.IProperty;

public class _3_EnvoiDevisAuClient extends BaseDocumentExtension
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
		med.getArticlesWithDifferentPricesForTabArt3(getWorkflowInstance());
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property)
	{
		// TODO Auto-generated method stub
		//med.onPropertyChangedOnDevis(property, getWorkflowInstance());
		super.onPropertyChanged(property);
	}
	
	@Override
	public boolean onBeforeSubmit(IAction action)
	{
		// TODO Auto-generated method stub
		return med.onBeforeTestFileIsEmpty(getWorkflowInstance(),getResourceController());
	}
	
	@Override
	public boolean onAfterSubmit(IAction action)
	{
		// TODO Auto-generated method stub
		
		//add devis et ligne devis
		//med.addDevisToDDB(action, getWorkflowInstance());
		return super.onAfterSubmit(action);
	}
}
