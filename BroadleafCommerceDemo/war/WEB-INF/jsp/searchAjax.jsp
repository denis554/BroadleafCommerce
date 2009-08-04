<%@ include file="/WEB-INF/jsp/include.jsp" %>
<h1>
	Search Results
</h1>
<c:choose>
<c:when test="${fn:length(categories) == 1}">
	<c:forEach var="product" items="${products}" varStatus="status">
		<div class="searchProduct span-3">
			<a href="/broadleafdemo/${category.generatedUrl}?productId=${product.id}">
				<img border="0" title="${product.name}" alt="${product.name}" src="/broadleafdemo${product.productImages.small}" width="75"/>
				<br/>
			<c:out value="${product.name}"/></a>
		</div>
		<c:if test="${status.index % 4 == 3}">
			<div style="clear:both"> </div>
		</c:if>
	</c:forEach>
</c:when>
<c:otherwise>
	<c:forEach var="category" items="${categories}" varStatus="status">
		<div class="searchCategory span-13">
			<h2><c:out value="${category.name}"/></h2>
			<c:forEach var="product" items="${categoryGroups[category.id]}" varStatus="status" end="3">
				<div class="searchProduct span-3">
					<a href="/broadleafdemo/${category.generatedUrl}?productId=${product.id}">
						<img border="0" title="${product.name}" alt="${product.name}" src="/broadleafdemo${product.productImages.small}" width="75"/>
						<br/>
					<c:out value="${product.name}"/></a>
				</div>
			</c:forEach>
			<div style="clear:both"> </div>
		</div>
	</c:forEach>
</c:otherwise>
</c:choose>
