<%@ include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="baseNoSide">
		<tiles:putAttribute name="headContentAdditional">
			<c:choose>
				<c:when test="${BLC_PAGE eq null}">
					<script language="javascript" src="/broadleafdemo/org.broadleafcommerce.admin.demoAdmin/admin/tinymce/tiny_mce_popup.js"></script>
					<script type="text/javascript" src="/broadleafdemo/org.broadleafcommerce.admin.demoAdmin/admin/tinymce/plugins/preview/jscripts/embed.js"></script>
					<script type="text/javascript">
					tinyMCEPopup.onInit.add(function(ed) {
						var dom = tinyMCEPopup.dom;
					
						// Place contents inside div container
						dom.setHTML('cmsContent', ed.getContent());
					});
					</script>
				</c:when>
				<c:otherwise>
					<c:if test="${BLC_PAGE.metaKeywords ne null}">
						<meta name="keywords" content="${BLC_PAGE.metaKeywords}"/>
					</c:if>
					<c:if test="${BLC_PAGE.metaDescription ne null}">
						<meta name="description" content="${BLC_PAGE.metaDescription}"/>
					</c:if>
				</c:otherwise>
			</c:choose>
		</tiles:putAttribute>
	<tiles:putAttribute name="mainContent" type="string">
	<div class="mainContentAreaFull" style="padding:8px;">
	<div id="cmsContent">
		<c:if test="${BLC_PAGE ne null}">
			${BLC_PAGE.pageFields.body.value}
		</c:if>
	</div>
	</div>
	</tiles:putAttribute>
</tiles:insertDefinition>

