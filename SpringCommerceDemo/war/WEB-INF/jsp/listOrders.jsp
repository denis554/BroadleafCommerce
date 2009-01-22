<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="/spring"%>
<%@ taglib prefix="form" uri="/spring-form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<jsp:include page="snippets/header.jsp"/>
				<h1>Manage Orders </h1>

	<table border="1">
	<tr>
		<th>ID</th>
		<th>Status</th>
		<th>Total</th>
	</tr>
	<c:forEach var="item" items="${orderList}" varStatus="status">
		<tr>
			<td><c:out value="${item.id}"/></td>
			<td><c:out value="${item.orderStatus}"/></td>
			<td><c:out value="${item.orderTotal}"></c:out>
		</tr>
	</c:forEach>

	</table>

	<a href="<c:url value="/createOrder.htm"/>">Create New Order</a>
	<a href="<c:url value="/logout"/>">Logout</a>
</div>