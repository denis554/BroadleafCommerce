/*
 * #%L
 * BroadleafCommerce Export Module
 * %%
 * Copyright (C) 2009 - 2017 Broadleaf Commerce
 * %%
 * Licensed under the Broadleaf Fair Use License Agreement, Version 1.0
 * (the "Fair Use License" located  at http://license.broadleafcommerce.org/fair_use_license-1.0.txt)
 * unless the restrictions on use therein are violated and require payment to Broadleaf in which case
 * the Broadleaf End User License Agreement (EULA), Version 1.1
 * (the "Commercial License" located at http://license.broadleafcommerce.org/commercial_license-1.1.txt)
 * shall apply.
 * 
 * Alternatively, the Commercial License may be replaced with a mutually agreed upon license (the "Custom License")
 * between you and Broadleaf Commerce. You may not use this file except in compliance with the applicable license.
 * #L%
 */
package com.broadleafcommerce.export.admin.server.security.service;

import org.apache.commons.collections.CollectionUtils;
import org.broadleafcommerce.openadmin.server.security.domain.AdminUser;
import org.broadleafcommerce.openadmin.server.security.service.AbstractRowLevelSecurityProvider;
import org.broadleafcommerce.openadmin.server.security.service.AdminSecurityService;
import org.broadleafcommerce.openadmin.server.security.service.type.PermissionType;
import org.springframework.stereotype.Component;

import com.broadleafcommerce.export.domain.ExportInfo;
import com.broadleafcommerce.export.domain.type.ExportEntityType;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Expression;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

@Component("blExportRowLevelSecurityProvider")
public class ExportRowLevelSecurityProvider extends AbstractRowLevelSecurityProvider {

    @Resource(name = "blAdminSecurityService")
    protected AdminSecurityService adminSecurityService;
    
    @Override
    public void addFetchRestrictions(AdminUser currentUser, String ceilingEntity, List<Predicate> restrictions, List<Order> sorts, Root entityRoot, CriteriaQuery criteria, CriteriaBuilder criteriaBuilder) {
        if (ExportInfo.class.getName().equals(ceilingEntity)) {
            sorts.add(criteriaBuilder.desc(entityRoot.get("dateCreated")));
            Expression<Boolean> isShared = entityRoot.get("shared");
            Expression<Long> rowAdminUser = entityRoot.get("adminUserId");
            Predicate viewable = criteriaBuilder.and(criteriaBuilder.or(criteriaBuilder.equal(isShared, true), criteriaBuilder.equal(rowAdminUser, currentUser.getId())));
            restrictions.add(viewable);
            List<String> accessableTypes = new ArrayList<>();
            for (String type : ExportEntityType.keySet()) {
                if (adminSecurityService.isUserQualifiedForOperationOnCeilingEntity(currentUser, PermissionType.READ, ExportEntityType.getInstance(type).getCeilingEntity())) {
                    accessableTypes.add(type);
                }
            }
            if (CollectionUtils.isNotEmpty(accessableTypes)) {
                Expression entityType = entityRoot.get("entityType");
                Predicate userCanView = criteriaBuilder.and(entityType.in(accessableTypes)); 
                restrictions.add(userCanView);
            }
        }
    }

}
