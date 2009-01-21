<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="/spring"%>
<%@ taglib prefix="form" uri="/spring-form"%>
<%@ taglib prefix="sc" uri="/springcommerce"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<script src="http://ajax.googleapis.com/ajax/libs/jquery/1.3.0/jquery.min.js">
</script>
<c:out value="${paginationObject.dataSource}"/>
<div id="banner">
	<span id="greeting">Logged in as <b><security:authentication property="principal.username" /></b></span>
	<hr/>
				<h1>Manage Catalog Items</h1>
</div>
<style>
	.listItem { 
		float:left;
		background-color:#DDDDDD;
		margin: 5px;
		padding: 5px;
		width: 400px;
		}
</style>
<div id="catalogListContainer">
<jsp:include page="listCatalogItemAjax.jsp"/>
</div>

<div>
<a href="<c:url value="/createCatalogItem.htm"/>">Create CatalogItem</a>
<br/>

<a href="<c:url value="/logout"/>">Logout</a>
</div>