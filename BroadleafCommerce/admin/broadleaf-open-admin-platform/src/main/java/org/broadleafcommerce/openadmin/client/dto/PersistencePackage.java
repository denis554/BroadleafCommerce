/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.openadmin.client.dto;

import java.io.Serializable;

public class PersistencePackage implements Serializable {

	private static final long serialVersionUID = 1L;
	
	protected String ceilingEntityFullyQualifiedClassname;
	protected PersistencePerspective persistencePerspective;
	protected String[] customCriteria;
	protected Entity entity;
	
	public PersistencePackage(String ceilingEntityFullyQualifiedClassname, Entity entity, PersistencePerspective persistencePerspective, String[] customCriteria) {
		this.ceilingEntityFullyQualifiedClassname = ceilingEntityFullyQualifiedClassname;
		this.persistencePerspective = persistencePerspective;
		this.entity = entity;
		this.customCriteria = customCriteria;
	}
	
	public PersistencePackage() {
		//do nothing
	}
	
	public String getCeilingEntityFullyQualifiedClassname() {
		return ceilingEntityFullyQualifiedClassname;
	}
	
	public void setCeilingEntityFullyQualifiedClassname(
			String ceilingEntityFullyQualifiedClassname) {
		this.ceilingEntityFullyQualifiedClassname = ceilingEntityFullyQualifiedClassname;
	}
	
	public PersistencePerspective getPersistencePerspective() {
		return persistencePerspective;
	}
	
	public void setPersistencePerspective(
			PersistencePerspective persistencePerspective) {
		this.persistencePerspective = persistencePerspective;
	}
	
	public String[] getCustomCriteria() {
		return customCriteria;
	}
	
	public void setCustomCriteria(String[] customCriteria) {
		this.customCriteria = customCriteria;
	}
	
	public Entity getEntity() {
		return entity;
	}
	
	public void setEntity(Entity entity) {
		this.entity = entity;
	}
	
}
