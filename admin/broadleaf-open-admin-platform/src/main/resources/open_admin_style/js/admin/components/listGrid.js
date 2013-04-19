(function($, BLCAdmin) {
    
    // Add utility functions for list grids to the BLCAdmin object
    BLCAdmin.listGrid = {
        replaceRelatedListGrid : function(data) {
            var $headerWrapper = $($(data.trim())[0]);
            var $table = $headerWrapper.find('table');
            var tableId = $table.attr('id');
            var $oldTable = $('#' + tableId);
            
            var currentIndex = BLCAdmin.listGrid.paginate.getTopVisibleIndex($oldTable.find('tbody'));
            
            var $oldBodyWrapper = $oldTable.closest('.listgrid-body-wrapper');
            var $oldHeaderWrapper = $oldBodyWrapper.prev();
            
            $oldHeaderWrapper.remove();
            $oldBodyWrapper.before($headerWrapper);
            $oldBodyWrapper.remove();
            
            var $listGridContainer = $headerWrapper.closest('.listgrid-container');
            $listGridContainer.find('.listgrid-table-footer').text('');
            
            this.initialize($listGridContainer);
            
            BLCAdmin.listGrid.paginate.scrollToIndex($listGridContainer.find('tbody'), currentIndex);
            $listGridContainer.find('.listgrid-body-wrapper').mCustomScrollbar('update');
            
            this.showAlert($listGridContainer, 'Saved!', { alertType: 'save-alert', autoClose: 1000 });
        },
        
        getButtonLink : function($button) {
            var $container = $button.closest('.listgrid-container');
            var $selectedRows = $container.find('table tr.selected');
            var link = $selectedRows.attr('data-link');
            
            if ($button.attr('data-urlpostfix')) {
                link += $button.attr('data-urlpostfix');
            }
            
            return link;
        },
        
        getRowFields : function($tr) {
            var fields = {};
            
            $tr.find('td').each(function() {
                var fieldName = $(this).data('fieldname');
                var value = $(this).data('fieldvalue');
                fields[fieldName] = value;
            });
            
            return fields;
        },
        
        updateToolbarRowActionButtons : function($listGridContainer) {
            var hasSelected = $listGridContainer.find('tr.selected').length > 0;
            if (hasSelected) {
                $listGridContainer.find('button.row-action').removeAttr('disabled');
            } else {
                $listGridContainer.find('button.row-action').attr('disabled', 'disabled');
            }
        },
        
        showAlert : function($container, message, options) {
    	    var alertType = options.alertType || '';
    	    
    	    var $alert = $('<div>').addClass('alert-box list-grid-alert').addClass(alertType);
    	    var $closeLink = $('<a>').attr('href', '').addClass('close').html('&times;');
    	    
    	    $alert.append(message);
    	    $alert.append($closeLink);
    	    
    	    $container.children().first().after($alert);
    	    
    	    if (options.autoClose) {
    	        setTimeout(function() {
    	            $closeLink.click();
    	        }, options.autoClose);
    	    }
        },
        
        fixHelper : function(e, ui) {
            ui.closest('tbody').find('tr').children().each(function() {
                $(this).width($(this).width());
            });
            return ui;
        },
        
        initialize : function($container) {
            this.updateToolbarRowActionButtons($container);
            
            // Prepare the tabs in case a list grid inside of this container requires browser
            // rendering for something like table cell widths
            $('ul.tabs-content > li, ul.tabs-content > li.active').css('visibility', 'hidden').css('display', 'block');
            
            if (BLCAdmin.listGrid.paginate) {
                BLCAdmin.listGrid.paginate.initialize($container);
            }
            
            // Restore normal tab state
            $('ul.tabs-content > li').css('visibility', 'visible').css('display', 'none');
            $('ul.tabs-content > li.active').css('visibility', 'visible').css('display', 'block');
        }
    };
    
})(jQuery, BLCAdmin);

$(document).ready(function() {
    
    /**
     * Bind a handler to trigger anytime a table row is clicked on any list grid. 
     * 
     * After assembling information, this will delegate to the specialized rowSelected
     * handler for this particular kind of list grid.
     */
    $('body').on('click', '.list-grid-table tbody tr', function() {
        var $tr = $(this);
        var $table = $tr.closest('table');
        var link = $tr.data('link');
        var listGridType = $table.data('listgridtype');
        var currentUrl = $table.data('currenturl');
        var fields = BLCAdmin.listGrid.getRowFields($tr);
        
        if ($tr.find('td.list-grid-no-results').length == 0 && !$table.hasClass('reordering')) {
            $('body').trigger('listGrid-' + listGridType + '-rowSelected', [link, fields, currentUrl]);
        }
    });
    
    /**
     * The rowSelected handler for the main list grid doesn't do anything by default
     */
    $('body').on('listGrid-main-rowSelected', function(event, link, fields, currentUrl) {
    });

    /**
     * The rowSelected handler for the inline list grid ...
     */
    $('body').on('listGrid-inline-rowSelected', function(event, link, fields, currentUrl) {
        var $tr = $('tr[data-link="' + link + '"]');
        var currentlySelected = $tr.hasClass('selected');
        var $listGridContainer = $tr.closest('.listgrid-container');
        
        $tr.closest('tbody').find('tr').removeClass('selected');
        
        if (!currentlySelected) {
            $tr.addClass("selected");
        }
        
        BLCAdmin.listGrid.updateToolbarRowActionButtons($listGridContainer);
    });
    
    /**
     * The rowSelected handler for a toOne list grid needs to trigger the specific valueSelected handler 
     * for the field that we are performing the to-one lookup on.
     */
    $('body').on('listGrid-to_one-rowSelected', function(event, link, fields, currentUrl) {
        $('div.additional-foreign-key-container').trigger('valueSelected', fields);
    });
    
    /**
     * The rowSelected handler for a simpleCollection list grid ...
     */
    $('body').on('listGrid-basic-rowSelected', function(event, link, fields, currentUrl) {
        var postData = {};
        
        for (var key in fields){
            if (fields.hasOwnProperty(key)){
                postData["fields['" + key + "'].value"] = fields[key];
            }
        }   
        
        BLC.ajax({
            url : currentUrl,
            type : "POST",
            data : postData
        }, function(data) {
            BLCAdmin.listGrid.replaceRelatedListGrid(data);
            BLCAdmin.hideCurrentModal();
        })
    });
    
    /**
     * The rowSelected handler for an adornedTarget list grid. This is specific to adorned target
     * lists that do not have any additional maintained fields. In this case, we can simply
     * submit the form directly.
     */
    $('body').on('listGrid-adorned-rowSelected', function(event, link, fields, currentUrl) {
        $(this).find('input#adornedTargetIdProperty').val(fields['id']);
        var $modal = BLCAdmin.currentModal();
        $modal.find('form.modal-form').submit();
    });
    
    /**
     * The rowSelected handler for an adornedTargetWithForm list grid. Once the user selects an entity,
     * show the form with the additional maintained fields.
     */
    $('body').on('listGrid-adorned_with_form-rowSelected', function(event, link, fields, currentUrl) {
        $(this).find('input#adornedTargetIdProperty').val(fields['id']);
        $('a#adornedModalTab2Link').click();
    });
    
    /**
     * This handler will fire for additional foreign key fields when the find button is clicked.
     * 
     * It is responsible for binding a valueSelected handler for this field as well as launching
     * a list grid modal that will be used to select the to-one entity.
     * 
     * Note that we MUST unbind this handler when the modal is hidden as there could be many different
     * to-one fields on an entity form.
     */
    $('body').on('click', '.to-one-lookup', function(event) {
        var $container = $(this).closest('div.additional-foreign-key-container');
        
        $container.on('valueSelected', function(event, fields) {
            var $this = $(this);
            var displayValueProp = $this.find('input.display-value-property').val();
            
            var $valueField = $this.find('input.value');
            $valueField.val(fields['id']);
            $this.find('span.display-value').html(fields[displayValueProp]);
            //ensure that the clear button shows up after selecting a value
            $this.find('button.clear-foreign-key').show();
            
            // To-one fields potentially trigger a dynamicform. We test to see if this field should
            // trigger a form, and bind the necessary event if it should.
            var onChangeTrigger = $valueField.data('onchangetrigger');
            if (onChangeTrigger) {
                var trigger = onChangeTrigger.split("-");
                if (trigger[0] == 'dynamicForm') {
                    var $fieldSet = $("fieldset[data-dynamicpropertyname='" + trigger[1] + "']");
                    var url = $fieldSet.data('currenturl') + '?propertyTypeId=' + fields['id'];
                    
                    BLC.ajax({
                        url : url,
                        type : "GET"
                    }, function(data) {
                        var dynamicPropertyName = $(data.trim()).find('fieldset').data('dynamicpropertyname');
                        var $oldFieldset = $('fieldset[data-dynamicpropertyname="' + dynamicPropertyName + '"]');
                        var $newFieldset = $(data.trim()).find('fieldset');
                        
                        BLCAdmin.initializeFields($newFieldset);
                        
                        $oldFieldset.replaceWith($newFieldset);
                    });
                }
            }
            
            BLCAdmin.hideCurrentModal();
        });
        
        BLCAdmin.showLinkAsModal($(this).data('select-url'), function() {
            $('div.additional-foreign-key-container').unbind('valueSelected');
        });
        
        return false;
    });
    
    $('body').on('click', 'button.sub-list-grid-add', function() {
        BLCAdmin.showLinkAsModal($(this).attr('data-actionurl'));
        return false;
    });
    
    $('body').on('click', 'button.sub-list-grid-reorder', function() {
        var $container = $(this).closest('.listgrid-container');
        var $table = $container.find('table');
        var $tbody = $table.find('tbody');
        var $trs = $tbody.find('tr');
        var doneReordering = $table.hasClass('reordering');
        
        $table.toggleClass('reordering');
        
        if (doneReordering) {
            $container.find('.listgrid-toolbar button').removeAttr('disabled');
            $(this).html('Reorder');
            
            BLCAdmin.listGrid.updateToolbarRowActionButtons($container);
            
            $trs.removeClass('draggable').addClass('clickable');
            $tbody.sortable("destroy");
        } else {
            $container.find('.listgrid-toolbar button').attr('disabled', 'disabled');
            $(this).removeAttr('disabled').html('Done');
            
            $trs.removeClass('clickable').addClass('draggable');
            
            $tbody.sortable({
                helper : BLCAdmin.listGrid.fixHelper,
                update : function(event, ui) {
                    BLC.ajax({
                        url : ui.item.data('link') + '/sequence',
                        type : "POST",
                        data : {
                            newSequence : ui.item.index()
                        }
                    }, function(data) {
                        var $container = $('div.listgrid-container#' + data.field);
                        BLCAdmin.listGrid.showAlert($container, 'Saved!', { alertType: 'save-alert', autoClose: 400 });
                        console.log(data);
                    });
                }
            }).disableSelection();
        }
        
        return false;
    });
    
    $('body').on('click', 'button.sub-list-grid-remove', function() {
        var link = BLCAdmin.listGrid.getButtonLink($(this));
        
        var $container = $(this).closest('.listgrid-container');
        var $selectedRows = $container.find('table tr.selected');
        var rowFields = BLCAdmin.listGrid.getRowFields($selectedRows);
        
        BLC.ajax({
            url: link,
            data: rowFields,
            type: "POST"
        }, function(data) {
            BLCAdmin.listGrid.replaceRelatedListGrid(data);
        });
        
        return false;
    });
    
    $('body').on('click', 'button.sub-list-grid-update', function() {
        var link = BLCAdmin.listGrid.getButtonLink($(this));
        
        BLCAdmin.showLinkAsModal(link);
        
        return false;
    });
    
    $('body').on('submit', 'form.modal-form', function(event) {
        BLCAdmin.runSubmitHandlers($(this));
        
        BLC.ajax({
            url: this.action,
            type: "POST",
            data: $(this).serialize()
        }, function(data) {
            BLCAdmin.listGrid.replaceRelatedListGrid(data);
            BLCAdmin.hideCurrentModal();
        });
        return false;
    });
    
    /**
     * Handler that fires whenever a sorting link is clicked, sort ascending or descending. This will also modify the
     * sort value input for the closet sort input for this list grid header
     */
    $('body').on('click', 'a.sort', function() {
        //reset any of the currently active sorts on all the fields in the grid
        $(this).closest('thead').find('i.sort-icon').removeClass('listgrid-icon-down').removeClass('listgrid-icon-up');
        $(this).closest('thead').find('input.sort-direction').removeClass('active').val('');
        $(this).closest('thead').find('input.sort-property').removeClass('active');
        
        //apply the sort to the current field
        var ascending = $(this).hasClass('down');
        var sortValue = (ascending) ? 'ASCENDING' : 'DESCENDING';
        var $sortType = $(this).parents('ul').find('input.sort-direction');
        $sortType.val(sortValue);
        
        //update the header icon for this field
        var icon = $(this).parents('.listgrid-headerBtn').find('div i.sort-icon');
        icon.toggleClass('listgrid-icon-up', ascending);
        icon.toggleClass('listgrid-icon-down', !ascending);
        
        //also mark these particular sorts as active so they will be serialized
        $sortType.toggleClass('active', true);
        $(this).parents('ul').find('input.sort-property').toggleClass('active', true);

        //submit the form just for this particular field since this is the only sort that changed
        $(this).closest('ul').find('div.filter-fields .listgrid-filter').click();
        return false;
    });

    /**
     * Intercepts the enter keypress from the listgrid criteria input (since it is not apart of a form) and clicks the
     * closest filter button
     */
    $('body').on('keypress', 'input.listgrid-criteria-input', function(event) {
        if (event.which == 13) {
            $(this).closest('.filter-fields').find('a.listgrid-filter').click();
            return false;
        }
    });
    
    /**
     * Intercepts click events on the 'filter' button for the list grid headers. This will execute an AJAX call after
     * serializing all of the inputs for all of the list grid header fields so that criteria on multiple fields can
     * be sent to the server
     */
    $('body').on('click', 'div.filter-fields a.listgrid-filter', function(event) {
        //Serialize all of the filter-forms in this particular list grid since it's possible that criteria could be set
        //for multiple fields at the same time
        var toReplace = $(this).closest('.list-grid-table').find('tbody');
        $(this).closest('ul').removeClass('show-dropdown');
        
        var $inputs = $(this).closest('thead').find('div.filter-fields :input');
        var nonBlankInputs = [];
        $inputs.each(function(index, input) {
            //since these filter inputs do not have 'real' input names in the DOM, give it here to make serialization easier
            $(input).attr('name', $(input).data('name'));
            
            //convert the datepicker inputs to server-valid ones
            if ($(input).hasClass('datepicker')) {
                input = $('<input>', {
                    type: 'hidden',
                    name: $(input).attr('name'),
                    value: BLCAdmin.dates.getServerDate($(input).val())
                })[0];
            }
            
            //toggle the filter icon for this field as active or not
            var filterIcon = $(input).parents('.listgrid-headerBtn').find('div i.filter-icon');
            filterIcon.toggleClass('icon-filter', !!$(input).val());
            
            //only submit fields that have a value set and are not a sort field. Sort fields will be added separately
            if ($(input).val() && !$(input).hasClass('sort-direction') && !$(input).hasClass('sort-property')) {
                nonBlankInputs.push(input);
            }
        });
        
        //also grab the sorts and ensure those inputs are also serialized
        var sorts = $(this).closest('thead').find('input.sort-direction.active, input.sort-property.active');
        nonBlankInputs = nonBlankInputs.concat($.makeArray(sorts));
        
        BLC.ajax({
            url: $(this).closest('.filter-fields').data('action'),
            type: "GET",
            data: $(nonBlankInputs).serialize()
        }, function(data) {
            toReplace.replaceWith($(data.trim()).find('tbody'));
        });
        
        $inputs.each(function(index, input) {
            $(input).removeAttr('name');
        });
        
        return false;
    });
    
    /**
     * Intercepts the form submission for a top-level entity search. This search is only available on a main entity page
     * (like Products)
     */
    $('body').on('submit', 'form.custom-entity-search', function(event) {
        $('body').find('.custom-entity-search a').click();
        return false;
    });
    
    /**
     * Intercepts the button click for the main entity search. This will look at the first field in the main list grid (of
     * which there is only 1 on the page) and replace the criteria value for that field with whatever was typed into the
     * search box.
     */
    $('body').on('click', '.custom-entity-search a', function(event) {
        //this takes place on the main list grid screen so there should be a single list grid
        var search = $('body').find('input').val();
        var $firstHeader = $('body').find('.list-grid-table th.th1');
        $firstHeader.find('input.listgrid-criteria-input').val(search);
        BLC.ajax({
            url: $(this).closest('form').action,
            type: "GET",
            data: $firstHeader.find('div.filter-fields :input').serialize()
        }, function(data) {
            $('body').find('.list-grid-table tbody').replaceWith($(data.trim()).find('tbody'));
        });
        return false;
    });
    
    /**
     * Clears out a previously-selected foreign key on both a form and listgrid criteria
     */
    $('body').on('click', 'button.clear-foreign-key', function(event) {        
        //remove the current display value
        $(this).prev().html($(this).prev().prev().html());
        
        //remove the criteria input val
        $(this).closest('.additional-foreign-key-container').find('.value').val('');
        $(this).toggle();
        
        //don't follow the link; prevents page jumping
        return false;
    });
    
    $('body').on('mouseover', 'td.row-action-selector', function(event) {
        $(this).find('ul.row-actions').show();
    });
    
    $('body').on('mouseout', 'td.row-action-selector', function(event) {
        $(this).find('ul.row-actions').hide();
    });
    
    $('.listgrid-container').each(function(index, element) {
        BLCAdmin.listGrid.initialize($(element));
    });
    
});

