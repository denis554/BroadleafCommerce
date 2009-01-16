<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<c:if test="${paginationObject.showPrevious}">
<c:url var="prevLink" value="/listCatalogItem.htm"><c:param name="pageNumber" value="${paginationObject.pageNumber-1}"/></c:url>
<a href='<c:out value="${prevLink}"/>'
   onClick='$("#catalogList").load("<c:out value="${prevLink}"/>&ajaxRequest=true"); return false;'>&lt;- Previous</a>
</c:if>

<c:if test="${paginationObject.showNext}">
<c:url var="nextLink" value="/listCatalogItem.htm"><c:param name="pageNumber" value="${paginationObject.pageNumber+1}"/></c:url>
<a href='<c:out value="${nextLink}"/>'
   onClick='$("#catalogList").load("<c:out value="${nextLink}"/>&ajaxRequest=true"); return false;'>Next -&gt;</a> 
</c:if>