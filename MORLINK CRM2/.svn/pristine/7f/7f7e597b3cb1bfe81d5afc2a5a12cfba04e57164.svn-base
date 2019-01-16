package com.morlink.dev.processes.crm;


import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.morlink.dev.servicesmetier.crm.MEtablissementDevis;

public class _1_TAB_EtablissementDevis extends BaseDocumentExtension
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
		med.onPropertyChangedOnTabArticle1(property, getWorkflowInstance());
		super.onPropertyChanged(property);
	}
	
	@Override
	public boolean onBeforeSave()
	{
		med.onBeforeSave(getWorkflowInstance());
		return super.onBeforeSave();
	}
	
}
