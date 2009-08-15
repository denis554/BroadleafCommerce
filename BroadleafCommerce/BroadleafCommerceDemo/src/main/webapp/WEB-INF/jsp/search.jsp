<%@ include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="baseNoSide">
	<tiles:putAttribute name="mainContent" type="string">
<br/>
<script>
	function updateSearchFilterResults() {
		$('#searchResults').prepend("<div class='grayedOut'><img style='margin-top:25px' src='/broadleafdemo/images/ajaxLoading.gif'/></div>");
		var postData = $('#refineSearch').serializeArray();
		postData.push({name:'ajax',value:'true'});
		$('#searchResults').load($('#refineSearch').attr('action'), postData);
	}
</script>
<div id="searchFilter">
	<form:form method="post" id="refineSearch" commandName="doSearch">
		<blc:searchFilter products="${products}" queryString="${queryString}">
			<blc:searchFilterItem property="defaultCategory.id" propertyDisplay="defaultCategory.name" displayTitle="Categories"/>
			<blc:searchFilterItem property="manufacturer" displayTitle="Manufacturers"/>
			<blc:searchFilterItem property="skus[0].salePrice" displayTitle="Prices" displayType="sliderRange"/>
		</blc:searchFilter>
		<input type="submit" value="Search"/>
	</form:form>
</div>
<div id="searchResults">
	<jsp:include page="searchAjax.jsp"/>
</div>
<div clear="both"> </div>

	</tiles:putAttribute>
</tiles:insertDefinition>
