 $(document).ready(function(){
	 verifyCheckbox();
	 
	 function verifyCheckbox() {
	   if ($("#sameShippingInfo").attr("checked") == false) {
		   $("#checkoutContactInfo").removeClass("displayNone");
	   }
	   else {
		   $("#checkoutContactInfo").addClass("displayNone");
	   }
	 }
	 
   $("#sameShippingInfo").click(function () {   
	   verifyCheckbox();
   });
 });