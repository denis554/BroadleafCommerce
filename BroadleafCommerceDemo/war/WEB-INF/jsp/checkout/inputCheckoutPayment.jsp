<%@ include file="/WEB-INF/jsp/include.jsp" %>
	<h4 class="formSectionHeader">Payment Information</h4>    		
	<table class="formTable">
		<tr>
			<td style="text-align:right"><label for="contactInfo.firstName"><b>First Name</b></label></td>
			<td colspan="2">
				<form:input path="billingAddress.firstName"/> 
				<form:errors path="billingAddress.firstName" cssClass="errorInputText"/>
			</td>
		</tr>
		<tr>
			<td style="text-align:right"><label for="contactInfo.lastName"><b>Last Name</b></label></td>
			<td colspan="2">
			 	<form:input path="billingAddress.lastName"/>
				<form:errors path="billingAddress.lastName" cssClass="errorInputText"/>
			</td>
   		</tr>
		<tr>
			<td style="text-align:right"><label for="contactInfo.primaryPhone"><b>Primary Phone Number</b></label></td>
			<td colspan="2">
			 	<form:input path="billingAddress.primaryPhone"/>
				<form:errors path="billingAddress.primaryPhone" cssClass="errorInputText"/>
			</td>
   		</tr>
		<tr>
			<td style="text-align:right"><label for="creditCardNumber"><b>CC Number:</b></label></td>
			<td colspan="2">
				<form:input maxlength="16" size="16" path="creditCardNumber" />
				<form:errors path="creditCardNumber" cssClass="errorInputText"/>
			</td>
		</tr>
		<tr>
			<td style="text-align:right"><label for="ccNumber"><b>CC Type:</b></label></td>
			<td><form:select path="selectedCreditCardType">
				<c:forEach var="ccType" items="${checkoutForm.approvedCreditCardTypes}">
					<form:option value="${ccType.type}"><c:out value="${ccType.type}" /></form:option>
				</c:forEach>
			</form:select></td>
   		</tr>
   		<tr>
			<td style="text-align:right"><label for="ccNumber"><b>Expiry Month/Year</b></label></td>
			<td>
				<form:input maxlength="2" size="2" path="creditCardExpMonth" />/<form:input maxlength="4" size="4" path="creditCardExpYear" />
			</td>
			<td colspan="1"> 
				<form:errors path="creditCardExpMonth" cssClass="errorInputText"/>
				<form:errors path="creditCardExpYear" cssClass="errorInputText"/>
	 		</td>
   		</tr>
   		<tr>
			<td style="text-align:right"><label for="cvv"><b>CVV Code</b></label></td>
			<td colspan="2">
				<form:input maxlength="4" size="4" path="creditCardCvvCode" />
				<form:errors path="creditCardCvvCode" cssClass="errorInputText"/>
			</td>
   		</tr>
		<tr>
			<td style="text-align:right"><label for="addressLine1">Address Line1:</b></label></td>
			<td colspan="2">
				<form:input path="billingAddress.addressLine1" />
				<form:errors path="billingAddress.addressLine1" cssClass="errorInputText"/>
			</td>
   		</tr>
		<tr>
			<td style="text-align:right"><label for="addressLine2">Address Line2:</b></label></td>
			<td colspan="2">
				<form:input path="billingAddress.addressLine2" />	
				<form:errors path="billingAddress.addressLine2" cssClass="errorInputText"/>
			</td>
   		</tr>
   		<tr>
			<td style="text-align:right"><label for="city">City:</b></label></td>
			<td colspan="2">
				<form:input path="billingAddress.city" />
				<form:errors path="billingAddress.city" cssClass="errorInputText"/>
			</td>
   		</tr>
   		<tr>
			<td style="text-align:right"><label for="state">State:</b></label></td>
			<td>
				<form:select path="billingAddress.state">
					<form:options items="${stateList}" itemValue="abbreviation" itemLabel="name" />
				</form:select>
			</td>
   		</tr>
   		<tr>
			<td style="text-align:right"><label for="postalCode">Postal Code:</b></label></td>
			<td colspan="2">
				<form:input path="billingAddress.postalCode" />
				<form:errors path="billingAddress.postalCode" cssClass="errorInputText"/>
			</td>
   		</tr>
   		<tr>
			<td style="text-align:right"><label for="country">Country:</b></label></td>
			<td>
				<form:select path="billingAddress.country">
					<form:options items="${countryList}" itemValue="abbreviation" itemLabel="name" />
				</form:select>
			</td>
   		</tr>
  	</table>