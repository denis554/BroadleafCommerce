<%@ page trimDirectiveWhitespaces="true" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt"%>
<%@ taglib prefix="spring" uri="/spring"%>
<%@ taglib prefix="form" uri="/spring-form"%>
<%@ taglib prefix="security" uri="http://www.springframework.org/security/tags" %>
<jsp:include page="snippets/header.jsp"/>
		<form:form method="post" commandName="checkout">


	<h1>Order Summary</h1>

	<h2>Ordered Items</h2>
	<table border="1">
	<tr>
		<th>Name</th>
		<th>Description</th>
		<th>Price</th>
		<th>Quantity</th>
		<th>Total Line Price</th>
		<th>Actions</th>
	</tr>	
	<c:forEach var="item" items="${checkout.orderItems}" varStatus="myRow">
		<tr>
			<td><c:out value="${item.sellableItem.catalogItem.name}"/></td>
			<td><c:out value="${item.sellableItem.catalogItem.description}"/></td>
			<td><c:out value="${item.sellableItem.price}"/></td>
			<td><c:out value="${item.quantity}"/></td>
			<td><c:out value="${item.finalPrice}"/></td>
		</tr>
	</c:forEach>
		<tr>
			<td>Tax: </td>
			<td>$###.##</td>
		</tr>
		<tr>
			<td>Shipping/Handling:</td>
			<td>$###.##</td>
		</tr>
		<tr>
			<th>Order Total</th>
			<th>
			<c:out value="${checkout.order.orderTotal}"/>
			</th>
		</tr>		
	</table>
			
	<h2>Contact Information</h2>
		Primary Phone: <c:out value="${checkout.contactInfo.primaryPhone}"/><br/>
		Secondary Phone: <c:out value="${checkout.contactInfo.secondaryPhone}"/><br/>
		Email Address: <c:out value="${checkout.contactInfo.email}"/><br/>
		Fax: <c:out value="${checkout.contactInfo.fax}"/><br/>

	<h2>Shipping Information</h2>
		Address:<br/>
		<c:out value="${checkout.orderShipping.address.addressLine1}"/><br/>
		<c:out value="${checkout.orderShipping.address.addressLine2}"/><br/>
		<c:out value="${checkout.orderShipping.address.city}"/><br/>
		<c:out value="${checkout.orderShipping.address.zipCode}"/><br/>

	<h2>Billing Information</h2>
		CC Number: <c:out value="${checkout.orderPayment.referenceNumber}"/><br/>
		Total Amount: <c:out value="${checkout.orderPayment.amount}" />
		Address:<br/>
		<c:out value="${checkout.orderPayment.address.addressLine1}"/><br/>
		<c:out value="${checkout.orderPayment.address.addressLine2 }"/><br/>
		<c:out value="${checkout.orderPayment.address.city}"/><br/>
		<c:out value="${checkout.orderPayment.address.zipCode}"/><br/>
			
	
	
	
    		<div class="formButtonFooter personFormButtons">
          <input type="Reset">                                       
          <input type="submit" value="Cancel" name="_cancel">			
	      <input type="submit" value="Previous" name="_target2">
          <input type="submit" value="Submit Order" name="_finish">     
          </div>
			</form:form>
