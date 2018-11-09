--
-- This file contains the out of box menus items (modules and sections) for the admin and their corresponding 
-- permission mappings.
--
-- Client systems can choose new modules and associated permission mappings by deleting rows they don't want and adding
-- others that make sense for their admin.
--
-- Broadleaf Commerce add-on modules will add rows to these tables as needed.
--

--
-- Create BLC MODULES (These modules are required for the admin left navigation)
--
INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-1,'Catalog','BLCMerchandising', 'blc-icon-catalog', 100);

-- New 3.2 - Pricing
INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-8,'Pricing','BLCPricing', 'fa fa-usd', 150);
INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-2,'Content','BLCContentManagement', 'blc-icon-content', 200);

-- New 3.2 - Inventory
INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-9,'Inventory','BLCInventory', 'blc-icon-inventory', 250);

INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-6,'Design','BLCDesign', 'blc-icon-design', 400);
INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-7,'Site Updates','BLCWorkflow', 'blc-icon-site-updates', 500);

-- Moved 3.2 - Customers 
INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-3,'Customer Care','BLCCustomerCare', 'blc-icon-customer-care', 550);
INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-4,'Security','BLCOpenAdmin', 'blc-icon-security', 600);
INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-5,'Settings','BLCModuleConfiguration', 'blc-icon-settings', 700);



--
-- Create BLC SECTIONS (These modules are required for the admin left navigation)
--
INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-1, 2000, -1, 'Category', 'Category', '/category', 'org.broadleafcommerce.core.catalog.domain.Category');
INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-2, 3000, -1, 'All Products', 'All Products', '/product', 'org.broadleafcommerce.core.catalog.domain.Product');
INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-3, 5000, -1, 'Product Options', 'ProductOptions', '/product-options', 'org.broadleafcommerce.core.catalog.domain.ProductOption');

INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-99, 3000, -1, 'Base Product', 'Base Product', '/product:product', 'org.broadleafcommerce.core.catalog.domain.Product');

-- Change 3.2 - Move offer to pricing
INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-4, 1000, -8, 'Offer', 'Offer', '/offer', 'org.broadleafcommerce.core.offer.domain.Offer');

INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-5, 2000, -2, 'Pages', 'Pages', '/pages', 'org.broadleafcommerce.cms.page.domain.Page');
INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-6, 4000, -2, 'Assets', 'Assets', '/assets', 'org.broadleafcommerce.cms.file.domain.StaticAsset');
INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-8, 7000, -2, 'Redirect URL', 'RedirectURL', '/redirect-url', 'org.broadleafcommerce.cms.url.domain.URLHandler');

INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-10, 1000, -3,'Customer', 'Customer', '/customer', 'org.broadleafcommerce.profile.core.domain.Customer');

INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-11, 2000, -5, 'User Management', 'UserManagement', '/user-management', 'org.broadleafcommerce.openadmin.server.security.domain.AdminUser');
INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-12, 3000, -5, 'Role Management', 'RoleManagement', '/role-management', 'org.broadleafcommerce.openadmin.server.security.domain.AdminRole');

INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-13, 10000, -5, 'Configuration Management', 'ConfigurationManagement', '/configuration-management', 'org.broadleafcommerce.common.config.domain.ModuleConfiguration');

-- INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-14, 2000, -5, 'Enumerations', 'Enumerations', '/enumerations', 'org.broadleafcommerce.common.enumeration.domain.DataDrivenEnumeration');

-- Permission Management
INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-17, 11000, -5, 'Permission Management', 'PermissionManagement', '/permission-management', 'org.broadleafcommerce.openadmin.server.security.domain.AdminPermission');

INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, CEILING_ENTITY) VALUES (-20, 1000, -29000,'Locale', 'Locale', '/locale', 'org.broadleafcommerce.common.locale.domain.Locale');

--
--
-- Mapping from Sections to Permissions
--
-- Category
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-1,-100);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-1,-101);

-- All Products
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-2,-102);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-2,-103);

-- Base Product
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-99,-100);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-99,-101);


-- Product Options
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-3,-104);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-3,-105);

-- Offer
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-4,-106);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-4,-107);

-- Pages
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-5,-108);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-5,-109);

-- Assets
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-6,-110);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-6,-111);

-- Redirect URL
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-8,-114);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-8,-115);

-- Customer
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-10,-118);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-10,-119);

-- User Management
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-11,-120);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-11,-121);

-- Role Management
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-12,-140);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-12,-141);

-- Permission Management added later, see below ... 

-- Configuration Managament
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-13,-126);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-13,-127);

-- Data Driven Enumerations
-- INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-14,-128);
-- INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-14,-129);

-- Permission Management
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-17,-150);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-17,-151);