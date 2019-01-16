package com.morlink.dev.processes.crm;

import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.interfaces.IProperty;
import com.morlink.dev.servicesmetier.crm.MEtablissementDevis;

public class _5_ValidationStatutClient extends BaseDocumentExtension {

	private static final long serialVersionUID = 1L;
	private MEtablissementDevis med;
	
	@Override
	public boolean onAfterLoad() {
		// TODO Auto-generated method stub
		med = new MEtablissementDevis();
		
		med.getArticlesDisponibleDansStockSageForStatutClient(getWorkflowInstance(), getResourceController());
		return super.onAfterLoad();
	}
	
	@Override
	public void onPropertyChanged(IProperty property) {
		// TODO Auto-generated method stub
		med.onPropertyChangeStatutClient(property, getWorkflowInstance(), getResourceController());
		super.onPropertyChanged(property);
	}
}
