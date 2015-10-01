--
--  SQL File that creates the permissions used for Enterprise Search functionality
--

INSERT INTO BLC_ADMIN_MODULE (ADMIN_MODULE_ID, NAME, MODULE_KEY, ICON, DISPLAY_ORDER) VALUES (-10,'Search','BLCSearch', 'icon-time', 125);

--
--  Search Redirects (permissions are defined in framework / load_admin_permissions
--

-- Mapping permissions to friendly permissions
INSERT INTO BLC_ADMIN_PERMISSION (ADMIN_PERMISSION_ID, DESCRIPTION, NAME, PERMISSION_TYPE, IS_FRIENDLY) VALUES (-23500,'View Search Redirects','PERMISSION_SEARCH_REDIRECT', 'READ', TRUE);
INSERT INTO BLC_ADMIN_PERMISSION_XREF (ADMIN_PERMISSION_ID, CHILD_PERMISSION_ID) VALUES (-23500, -30);

INSERT INTO BLC_ADMIN_PERMISSION (ADMIN_PERMISSION_ID, DESCRIPTION, NAME, PERMISSION_TYPE, IS_FRIENDLY) VALUES (-23501,'Maintain Search Redirects','PERMISSION_SEARCH_REDIRECT', 'ALL', TRUE);
INSERT INTO BLC_ADMIN_PERMISSION_XREF (ADMIN_PERMISSION_ID, CHILD_PERMISSION_ID) VALUES (-23501, -31);


--
--  Search Facets
--

-- Mapping permissions to friendly permissions
INSERT INTO BLC_ADMIN_PERMISSION (ADMIN_PERMISSION_ID, DESCRIPTION, NAME, PERMISSION_TYPE, IS_FRIENDLY) VALUES (-23502,'View Search Facets','PERMISSION_SEARCH_FACET', 'READ', TRUE);
INSERT INTO BLC_ADMIN_PERMISSION_XREF (ADMIN_PERMISSION_ID, CHILD_PERMISSION_ID) VALUES (-23502, -32);

INSERT INTO BLC_ADMIN_PERMISSION (ADMIN_PERMISSION_ID, DESCRIPTION, NAME, PERMISSION_TYPE, IS_FRIENDLY) VALUES (-23503,'Maintain Search Facets','PERMISSION_SEARCH_FACET', 'ALL', TRUE);
INSERT INTO BLC_ADMIN_PERMISSION_XREF (ADMIN_PERMISSION_ID, CHILD_PERMISSION_ID) VALUES (-23503, -33);


-- Mapping permissions to friendly permissions
INSERT INTO BLC_ADMIN_PERMISSION (ADMIN_PERMISSION_ID, DESCRIPTION, NAME, PERMISSION_TYPE, IS_FRIENDLY) VALUES (-23504,'View Search Facets','PERMISSION_SEARCH_FACET', 'READ', TRUE);
INSERT INTO BLC_ADMIN_PERMISSION_XREF (ADMIN_PERMISSION_ID, CHILD_PERMISSION_ID) VALUES (-23504, -34);

INSERT INTO BLC_ADMIN_PERMISSION (ADMIN_PERMISSION_ID, DESCRIPTION, NAME, PERMISSION_TYPE, IS_FRIENDLY) VALUES (-23505,'Maintain Search Facets','PERMISSION_SEARCH_FACET', 'ALL', TRUE);
INSERT INTO BLC_ADMIN_PERMISSION_XREF (ADMIN_PERMISSION_ID, CHILD_PERMISSION_ID) VALUES (-23505, -35);


--
-- Mapping from Roles to permissions
-- Site admins are allowed to do everything with pricelists
-- Merchandisers are allowed to view pricelists
--
INSERT INTO BLC_ADMIN_ROLE_PERMISSION_XREF (ADMIN_ROLE_ID, ADMIN_PERMISSION_ID) VALUES (-1,-23501);
INSERT INTO BLC_ADMIN_ROLE_PERMISSION_XREF (ADMIN_ROLE_ID, ADMIN_PERMISSION_ID) VALUES (-1,-23503);
INSERT INTO BLC_ADMIN_ROLE_PERMISSION_XREF (ADMIN_ROLE_ID, ADMIN_PERMISSION_ID) VALUES (-1,-23505);
INSERT INTO BLC_ADMIN_ROLE_PERMISSION_XREF (ADMIN_ROLE_ID, ADMIN_PERMISSION_ID) VALUES (-2,-23504);
INSERT INTO BLC_ADMIN_ROLE_PERMISSION_XREF (ADMIN_ROLE_ID, ADMIN_PERMISSION_ID) VALUES (-2,-23502);
INSERT INTO BLC_ADMIN_ROLE_PERMISSION_XREF (ADMIN_ROLE_ID, ADMIN_PERMISSION_ID) VALUES (-2,-23500);



--
-- Mapping sections and permissions
--
INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, CEILING_ENTITY, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, USE_DEFAULT_HANDLER) VALUES (-23500, 13000, 'org.broadleafcommerce.core.search.redirect.domain.SearchRedirect', -10, 'Redirects', 'SearchRedirect', '/search-redirect', TRUE);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-23500,-23500);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-23500,-23501);

INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, CEILING_ENTITY, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, USE_DEFAULT_HANDLER) VALUES (-23501, 11000, 'org.broadleafcommerce.core.search.domain.SearchFacet', -10, 'Facets', 'SearchFacet', '/search-facet', TRUE);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-23501,-23502);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-23501,-23503);

INSERT INTO BLC_ADMIN_SECTION (ADMIN_SECTION_ID, DISPLAY_ORDER, CEILING_ENTITY, ADMIN_MODULE_ID, NAME, SECTION_KEY, URL, USE_DEFAULT_HANDLER) VALUES (-23502, 12000, 'org.broadleafcommerce.core.search.domain.SearchField', -10, 'Fields', 'SearchField', '/search-field', TRUE);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-23502,-23504);
INSERT INTO BLC_ADMIN_SEC_PERM_XREF (ADMIN_SECTION_ID, ADMIN_PERMISSION_ID) VALUES (-23502,-23505);