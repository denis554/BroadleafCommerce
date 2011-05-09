package org.broadleafcommerce.gwt.client.service;

import org.broadleafcommerce.gwt.client.datasource.relations.PersistencePerspective;
import org.broadleafcommerce.gwt.client.datasource.results.DynamicResultSet;
import org.broadleafcommerce.gwt.client.datasource.results.Entity;
import org.broadleafcommerce.gwt.client.datasource.results.FieldMetadata;

import com.anasoft.os.daofusion.cto.client.CriteriaTransferObject;
import com.google.gwt.user.client.rpc.RemoteService;

public interface DynamicEntityService extends RemoteService {
    
	//@Secured("PERMISSION_DEFAULT")
	public DynamicResultSet inspect(String ceilingEntityFullyQualifiedClassname, PersistencePerspective persistencePerspective, String[] customCriteria, String[] metadataOverrideKeys, FieldMetadata[] metadataOverrideValues) throws ServiceException;
	
	//@Secured("PERMISSION_DEFAULT")
	public DynamicResultSet fetch(String ceilingEntityFullyQualifiedClassname, CriteriaTransferObject cto, PersistencePerspective persistencePerspective, String[] customCriteria) throws ServiceException;
    
	//@Secured("PERMISSION_DEFAULT")
	public Entity add(String ceilingEntityFullyQualifiedClassname, Entity entity, PersistencePerspective persistencePerspective, String[] customCriteria) throws ServiceException;
    
	//@Secured("PERMISSION_DEFAULT")
    public Entity update(Entity entity, PersistencePerspective persistencePerspective, String[] customCriteria) throws ServiceException;
    
	//@Secured("PERMISSION_DEFAULT")
    public void remove(Entity entity, PersistencePerspective persistencePerspective, String[] customCriteria) throws ServiceException;
    
}
