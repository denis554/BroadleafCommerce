<%@ include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="baseNoSide">
	<tiles:putAttribute name="mainContent" type="string">
	
<%@ include file="/WEB-INF/jsp/include.jsp" %>
		<h3 style="margin:8px 0;font-weight:bold;">Your Shopping Cart</h3>
		<c:choose>
		    <c:when test="${!empty currentCartOrder.orderItems}">
			    <p style="margin-bottom:8px;">Checking out is quick, easy and secure. &nbsp; If you have any questions, please contact support.</p>
			 	<table class="cartTable">
			 	  <thead>
			          <tr valign="bottom">
			          	<th width="80">&nbsp;</th>
			            <th>Item Description</th>
			            <th style="text-align:center;">Quantity</th>
			            <th style="text-align:right;padding-right:12px" width="150">Price</th>
			            <th style="text-align:right;padding-right:14px">Total</th>
			          </tr>
		          </thead>
		          <tbody>
				  	<form:form modelAttribute="cartSummary" action="viewCart.htm" method="POST">
					<form:errors path="*" cssClass="errorText"/>
					<c:forEach items="${currentCartOrder.orderItems}" var="orderItem" varStatus="status">		
				    	<c:set var="item" value="${orderItem.sku}"/>
						<c:set var="product" value="${item.allParentProducts[0]}"/>
						<c:url var="itemUrl" value="/${product.defaultCategory.generatedUrl}">
							<c:param name="productId" value="${product.id}"/>
						</c:url>
		          		<tr valign="top">
		          			<td class="thumbnail"><a href="${itemUrl}">
									<img border="0" title="${product.name}" width="80" alt="${product.name}" src="/broadleafdemo${product.productImages.small}" />
								</a></td>
					  		<td class="item">
								<p class="description">
									<a href="${itemUrl}">${item.name}</a>
								</p>
                      		</td>
			  		  		<td style="text-align:center;">
			              		<form:hidden path="rows[${status.index}].orderItem.id"/>
			              		<form:input cssClass="quantityInput" cssStyle="width:30px" maxlength="3" path="rows[${status.index}].quantity" autocomplete="off"/>
			              		<br/>
			              		<input type="submit" name="updateItemQuantity" id="updateQuantity" value="Update" class="cartButton" />
					  			<br />
					  			<c:url var="removeItemUrl" value="/basket/viewCart.htm">
					  				<c:param name="orderItemId" value="${orderItem.id}"/>
					  				<c:param name="removeItemFromCart" value="true"/>
					  			</c:url>
				  	  			<a class="cartLinkBtn" href="${removeItemUrl}">Remove</a>
					  		</td>
			  		  		<td style="text-align:right;">
				    			<span class="price">
	           						<c:choose>
										<c:when test="${true}">
											<span class="salePrice"><fmt:formatNumber type="currency" value="${item.salePrice.amount}" /></span>
											<br/><span class="originalPrice">reg&nbsp;<fmt:formatNumber type="currency" value="${item.listPrice.amount}" /></span>
										</c:when>
										<c:otherwise>
											<fmt:formatNumber type="currency" value="${item.listPrice.amount}" />
										</c:otherwise>
									</c:choose>
					    		</span>
						    </td>
					  		<td style="text-align:right;">
					  		  <span class="price"><fmt:formatNumber type="currency" value="${orderItem.price.amount * orderItem.quantity}" /></span>
					  		</td>
			     		</tr>
					</c:forEach>
					<tr class="totals highlight">
						<td colspan="3">&nbsp;</td>
						<td style="text-align:right">Subtotal:</td>
						<td style="text-align:right"><span class="price">${currentCartOrder.subTotal}</span></td>
					</tr>
					<tr class="totals">
						<td colspan="3">&nbsp;</td>
						<td style="text-align:right">Estimated Tax:</td>
						<td style="text-align:right"><span class="price">${currentCartOrder.totalTax}</span></td>
					</tr>
					<tr class="totals">
						<td colspan="3" style="text-align:right;">Shipping Method:
							<form:select cssClass="shipMethod" path="fulfillmentGroup.method">
								<form:options items="${fulfillmentGroups}" itemValue="method" itemLabel="method" />
							</form:select>
						</td>
						<td style="text-align:right">Estimated Shipping:</td>
						<td style="text-align:right"><span class="price">${currentCartOrder.totalShipping}</span></td>
					</tr>
					<tr class="totals">
						<td colspan="3">&nbsp;</td>
						<td style="text-align:right">Total:</td>
						<td style="text-align:right"><span class="price">${currentCartOrder.total}</span></td>
					</tr>
					<tr class="totals">
						<td colspan="5" style="text-align:right"><a href="<c:url value="/store" />">Continue Shopping</a>  <button type="submit" name="checkout" id="checkout">Proceed to Checkout &raquo;</button>
						</td>
					</tr>
			</form:form>
				</tbody>
				</table>
				</c:when>
				<c:otherwise>
					<c:url var="storeUrl" value="/store" />
					<div class="alert" style="line-height: 20px; margin-top: 16px;">
	                	<b>Your shopping cart is currently empty</b><br>
						&bull; &nbsp;  <a href="<c:out value="${storeUrl}" />" class="link">Click here</a>
	                    to shop from our thousands of space- and time-saving solutions.
	                    <c:if test="${customer.firstName eq null}">
		                    <br>&bull; &nbsp; If you are a registered user,
		                    <a href="/profile/login.htm" class="link">sign in</a>
		                    to retrieve any saved items.
	                    </c:if>
	                </div>
				</c:otherwise>
			</c:choose>
	</tiles:putAttribute>
</tiles:insertDefinition>