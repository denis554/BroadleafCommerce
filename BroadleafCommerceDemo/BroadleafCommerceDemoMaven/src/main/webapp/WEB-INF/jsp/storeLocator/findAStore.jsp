<%@ include file="/WEB-INF/jsp/include.jsp" %>
<tiles:insertDefinition name="baseNoSide">
	<tiles:putAttribute name="mainContent" type="string">
	<h3 class="pageTitle" ><b>Store Locator</b></h3>

	<div class="span-24">
		<form:form method="post" action="findStores.htm" commandName="findAStoreForm" >
			<div class="orderBorder column span-6" style="margin-top:0px;" >
				<div class="orderTitle" > <b>Address Information</b></div>
				<div class="column span-3" style="height:150px; line-height:25px;">
					<b>Postal Code:</b> <br/><br/>
					<b>Radius (miles):</b> <br/> <br/>
					<input type="submit" name="Find" value="Find"/>
				</div>
				<div class="column span-2" >
					<form:input path="postalCode" size="5"/> <br/><br/>
					<form:input path="distance" size="3"/> 
				</div>
			</div>
		</form:form>
	</div>
	
	<div class="span-24" style="margin-top:15px;" >
		<c:choose>
			<c:when test="${errorMessage != null}">
				<h4 style="margin-top:10px;" ><b>Search Results </b></h4>
				<span> ${errorMessage} </span>
			</c:when>
			<c:when test="${!(empty findAStoreForm.storeDistanceMap)}">
				<h4 style="margin-top:10px;" ><b>Search Results </b></h4>
				<table class="cartTable">
					<thead>
						<tr>
							<th class="alignCenter"> Name </th>
							<th class="alignCenter"> Address </th>
							<th class="alignCenter"> Driving Distance </th>
						</tr>
					</thead> 
					<c:forEach var="entry" items="${findAStoreForm.storeDistanceMap}" varStatus="status">
						<tr>
							<td class="alignCenter">${entry.key.name } </td>
							<td class="alignCenter">
								${entry.key.address1}
								<c:if test="${(entry.key.address2 != null) || !(empty entry.key.address2)}" >
									${entry.key.address2 }<br/>
								</c:if>
								${entry.key.city}, ${entry.key.state}, ${entry.key.zip}
							</td>
							<td class="alignCenter"><fmt:formatNumber value="${entry.value}" maxFractionDigits="2" /> miles</td>
						</tr>
					</c:forEach> 
				</table>
			</c:when>
		</c:choose>
	</div>

	</tiles:putAttribute>
</tiles:insertDefinition>