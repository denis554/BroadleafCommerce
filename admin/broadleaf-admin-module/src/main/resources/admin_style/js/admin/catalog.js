/**
 * Custom handlers for frontend catalog functions
 */
$(document).ready(function() {
    
    $('body').on('click', 'button.generate-skus', function() {
        $.get($(this).data('actionurl'), function(data) {
            if (data.skusGenerated == 0) {
                alert("No Skus were generated. It is likely that each product option value permutation already has" +
                		" a Sku associated with it");
            } else if (data.skusGenerate == -1) {
                alert("This product has no Product Options configured to generate Skus from");
            } else {
                alert(data.skusGenerated + " Skus have been generated from the configured product options");
                var additionalSkusTable = $('body').find('#additionalSkus table');
                var url = additionalSkusTable.data('currenturl');
                url += "/additionalSkus";
                BLC.ajax({
                    url: url,
                    type: "GET",
                }, function(data) {
                    additionalSkusTable.find('tbody').replaceWith($(data.trim()).find('tbody'));
                });
            }
        });
        return false;
    });

});