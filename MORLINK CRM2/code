package com.morlink.dev.connexiondb;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.lang.reflect.Field;

import com.axemble.vdoc.sdk.Modules;
import com.axemble.vdoc.sdk.document.extensions.BaseDocumentExtension;
import com.axemble.vdoc.sdk.exceptions.PortalModuleException;
import com.axemble.vdoc.sdk.interfaces.IConnectionDefinition;
import com.axemble.vdoc.sdk.interfaces.IContext;
import com.axemble.vdoc.sdk.modules.IPortalModule;
import com.axemble.vdoc.sdk.modules.IWorkflowModule;
import com.axemble.vdoc.sdk.utils.Logger;


public class SingletonConnexionBDD extends BaseDocumentExtension{
    /**
     * 
     */
	private static final Logger LOGGER = Logger.getLogger(SingletonConnexionBDD.class);
    private static final long serialVersionUID = 1L;
    /*---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------
     *DECLARATION DES VARRIABLES 
     *-------------------------------------------------------------------------------------------------------------------------------------------------------------------
     */
    private  IContext processContext;
  
	// Constructeur
    private SingletonConnexionBDD(){
    }
    
    // Class SingletonHolder
    private static class SingletonHolder
    {		
    	private SingletonHolder() {
    		
    	}
        
		/** Instance unique non préinitialisée */
        @SuppressWarnings("unused")
		private static final SingletonConnexionBDD sqlinstance = new SingletonConnexionBDD(); //
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    // 
    // -----------------------------------------------------------------------------------------------------------------
    public static SingletonConnexionBDD getSqlSession(){
        SingletonHolder sh = new SingletonHolder();
        SingletonConnexionBDD sbdd = null; 
        Field field = null;
        try
        {
            field = SingletonHolder.class.getDeclaredField("sqlinstance");
            field.setAccessible(true);
            sbdd = (SingletonConnexionBDD) field.get(sh);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
        	LOGGER.error(e.getMessage());
        }
        return sbdd;
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    // CONNEXION AU BASE DE DONNEES  (RH_ATTIJARI / Ref_Attijari) ==> référence externe
    // -----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public IConnectionDefinition<java.sql.Connection> getConnectionVDoc (IWorkflowModule im,IPortalModule ip,String refExterne) throws PortalModuleException
    {
       
            processContext = im.getContextByLogin("sysadmin");
        
        return (IConnectionDefinition<Connection>) ip.getConnectionDefinition(processContext, refExterne);
    }
    // -----------------------------------------------------------------------------------------------------------------
    // CONNEXION AU BASE DE DONNEES => méthode de jasper Ireport
    // -----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    public IConnectionDefinition<Connection> getConnectionDefinition(IWorkflowModule im,String refExterne) throws PortalModuleException
    {
        processContext = im.getContextByLogin("sysadmin");
        return (IConnectionDefinition<Connection>) Modules.getPortalModule().getConnectionDefinition(processContext,refExterne);
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    // CLOSE SELECT STATEMENT
    // -----------------------------------------------------------------------------------------------------------------
    public static void close(Connection conn, PreparedStatement pstmt, ResultSet rs) {
        try {
            rs.close();
            pstmt.close();
            conn.close();
        } catch (Exception e) {
        	LOGGER.error(e.getMessage());
        }
    }
    
    // -----------------------------------------------------------------------------------------------------------------
    // CLOSE (INSERT, UPDATE, DELETE) STATEMENT
    // -----------------------------------------------------------------------------------------------------------------
    public static void close(Connection conn, PreparedStatement pstmt) {
        try {
            pstmt.close();
            conn.close();
        } catch (Exception e) {
        	LOGGER.error(e.getMessage());        }
    }
}
