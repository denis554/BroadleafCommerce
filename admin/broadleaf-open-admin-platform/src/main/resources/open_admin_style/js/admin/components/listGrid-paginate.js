(function($, BLCAdmin) {
    
    var LISTGRID_AJAX_LOCK = 0;
    var fetchDebounce = 200;
    var updateUrlDebounce = 700;
    var lockDebounce = 100;
    var trHeight = null;
    
    // Add utility functions for list grids to the BLCAdmin object
    BLCAdmin.listGrid.paginate = {
            
        // ********************** *
        // LOCK RELATED FUNCTIONS *
        // ********************** *
            
        acquireLock : function() {
            if (LISTGRID_AJAX_LOCK == 0) {
                LISTGRID_AJAX_LOCK = 1;
                return true;
            }
            return false;
        },
        
        releaseLock : function() {
            LISTGRID_AJAX_LOCK = 0;
        },
        
        showLoadingSpinner : function($tbody, spinnerOffset) {
            var $spinner = $('<i>', { 'class' : 'icon-spin icon-spinner' });
            
            if (spinnerOffset) {
                $spinner.css('position', 'absolute').css('top', spinnerOffset + 'px');
            }
            
            var $footer = $tbody.closest('.listgrid-container').find('.listgrid-table-footer')
            $footer.html($spinner);
        },
        
        hideLoadingSpinner : function($tbody) {
            this.updateTableFooter($tbody);
        },
        
        // ****************************** *
        // RECORD RANGE RELATED FUNCTIONS *
        // ****************************** *
        
        getPageSize : function($tbody) {
            return $tbody.data('pagesize');
        },
        
        getTotalRecords : function($tbody) {
            return $tbody.data('totalrecords');
        },
        
        getRange : function(rangeDescription) {
            var range = rangeDescription.split('-');
            rangeObj = {lo : parseInt(range[0]), hi : parseInt(range[1])};
            return rangeObj;
        },
        
        getLoadedRecordRanges : function($tbody) {
            var rangeDescriptions = $tbody.data('recordranges').split(',');
            var ranges = [];
            
            for (var i = 0; i < rangeDescriptions.length; i++) {
                ranges[i] = this.getRange(rangeDescriptions[i]);
            }
            
            return ranges;
        },
        
        addLoadedRange : function($tbody, lo, hi) {
            var loadedRanges = this.getLoadedRecordRanges($tbody);
            
            // Add the new range
            loadedRanges.push({ lo : parseInt(lo), hi : parseInt(hi) });
            
            // Sort the ranges
            loadedRanges.sort(function(a, b) {
                return a.lo - b.lo;
            });
            
            // Merge any ranges that were "bridged" by the new range
            for (var i = loadedRanges.length - 1; i > 0; i--) {
                if (loadedRanges[i].lo - 1 == loadedRanges[i - 1].hi) {
                    loadedRanges[i - 1].hi = loadedRanges[i].hi;
                    loadedRanges.splice(i, 1);
                }
            }
            
            // Save the new record ranges
            var s = "";
            for (var i = 0; i < loadedRanges.length; i++) {
                s += loadedRanges[i].lo + '-' + loadedRanges[i].hi + ',';
            }
            s = s.substring(0, s.length - 1);
            $tbody.data('recordranges', s);
        },
        
        isIndexLoaded : function($tbody, index) {
            var loadedRanges = this.getLoadedRecordRanges($tbody);
            
            for (var i = 0; i < loadedRanges.length; i++) {
                if (loadedRanges[i].lo <= index && loadedRanges[i].hi >= index) {
                    return true;
                }
            }
            
            return false;
        },
        
        findFirstLoadedIndex : function($tbody, startIndex) {
            var loadedRanges = this.getLoadedRecordRanges($tbody);
            
            var firstIndex = null;
            for (var i = loadedRanges.length - 1; i >= 0; i--) {
                if (loadedRanges[i].lo > startIndex) {
                    firstIndex = loadedRanges[i].lo;
                }
            }
            
            return firstIndex;
        },
        
        findLastLoadedIndex : function($tbody, endIndex) {
            var loadedRanges = this.getLoadedRecordRanges($tbody);
            
            var lastIndex = null;
            for (var i = 0; i < loadedRanges.length; i++) {
                if (loadedRanges[i].hi < endIndex) {
                    lastIndex = loadedRanges[i].hi;
                }
            }
                
            return lastIndex;
        },
        
        // ************************** *
        // DOM MANIPULATION FUNCTIONS *
        // ************************** *
        
        injectRecords : function($tbody, $newTbody) {
            // Find the padding row that will be split (or potentially replaced)
            var _this = this;
            var newRange = this.getRange($newTbody.data('recordranges'));
            var $pad = null;
            var padRange = null;
            $tbody.find('tr.blank-padding').each(function(index, element) {
                var $e = $(element);
                var pr = _this.getRange($e.data('range'));
                
                if ((padRange == null || padRange.lo < pr.lo)  && pr.lo <= newRange.lo) {
                    $pad = $e;
                    padRange = pr;
                }
            });
            // Create the top split (potentially nothing)
            var blankRangeAbove = {
                lo : padRange.lo,
                hi : newRange.lo - 1
            };
            if (blankRangeAbove.lo != (blankRangeAbove.hi + 1)) {
                var $topPad = this.createPadding($tbody, blankRangeAbove.lo, blankRangeAbove.hi);
            }
            
            // Create the bottom split (potentially nothing)
            var blankRangeBelow = {
                lo : newRange.hi + 1,
                hi : padRange.hi
            };
            if (blankRangeBelow.lo != blankRangeBelow.hi + 1) {
                var $bottomPad = this.createPadding($tbody, blankRangeBelow.lo, blankRangeBelow.hi);
            }
            
            // Extract the new rows
            var $newTrs = $newTbody.find('tr');
            
            // Replace the old padding row with the top split, new rows, and bottom split
            if ($topPad != null) {
                $pad.before($topPad);
            }
            if ($bottomPad != null) {
                $pad.after($bottomPad);
            }
            $pad.replaceWith($newTrs);
            
            // Update the loaded record ranges to reflect this new chunk
            this.addLoadedRange($tbody, newRange.lo, newRange.hi);
        },
        
        // ********************** *
        // AJAX CALLING FUNCTIONS *
        // ********************** *
        
        loadRecords : function($tbody, baseUrl) {
            while (!this.acquireLock()) {
                var _this = this;
                //console.log("Couldn't acquire lock. Will try again in " + lockDebounce + "ms");
                $.doTimeout('acquirelock', lockDebounce, function() {
                    _this.loadRecords($tbody, baseUrl);
                });
                return false;
            }
            
            var topIndex = this.getTopVisibleIndex($tbody);
            var botIndex = this.getBottomVisibleIndex($tbody);
            var ranges = this.getLoadedRecordRanges($tbody);
            var pageSize = this.getPageSize($tbody);
            var totalRecords = this.getTotalRecords($tbody);
            
            var topIndexLoaded = this.isIndexLoaded($tbody, topIndex);
            var botIndexLoaded = this.isIndexLoaded($tbody, botIndex);
            
            var startIndex = null;
            var maxIndex = null;
            
            // This is responsible for determining which range of records to load, considering what the currently
            // visible viewport is, what ranges are already loaded near the viewport, and the maximum page size
            if (!topIndexLoaded && !botIndexLoaded) {
                var potentialStart = this.findLastLoadedIndex($tbody, topIndex);
                var potentialMax = this.findFirstLoadedIndex($tbody, botIndex);
                
                if (potentialStart == null) {
                    potentialStart = 0;
                } else {
                    potentialStart = potentialStart + 1;
                }
                
                if (potentialMax == null) {
                    potentialMax = totalRecords - 1;
                } else {
                    potentialMax = potentialMax - 1;
                }
                
                if (pageSize < potentialMax - topIndex) {
                    startIndex = topIndex;
                    maxIndex = topIndex + pageSize - 1;
                } else if (pageSize > potentialMax - potentialStart) {
                    startIndex = potentialStart;
                    maxIndex = potentialMax;
                } else if (pageSize > potentialMax - topIndex) {
                    startIndex = potentialMax - pageSize + 1;
                    maxIndex = potentialMax;
                } else {
                    startIndex = topIndex;
                    maxIndex = topIndex + pageSize - 1;
                }
            } else if (!topIndexLoaded && botIndexLoaded) {
                maxIndex = this.findFirstLoadedIndex($tbody, topIndex) - 1;
                
                var potentialStart = this.findLastLoadedIndex($tbody, topIndex);
                
                if (potentialStart == null) {
                    potentialStart = 0;
                } else {
                    potentialStart = potentialStart + 1;
                }
                
                if (pageSize > maxIndex - potentialStart) {
                    startIndex = potentialStart;
                } else {
                    startIndex = maxIndex - pageSize + 1;
                }
            } else if (topIndexLoaded && !botIndexLoaded) {
                startIndex = this.findLastLoadedIndex($tbody, botIndex) + 1;
                
                var potentialMax = this.findFirstLoadedIndex($tbody, startIndex);
                if (potentialMax == null) {
                    potentialMax = totalRecords - 1;
                } else {
                    potentialMax = potentialMax - 1;
                }
                
                if (pageSize > potentialMax - startIndex) {
                    maxIndex = potentialMax;
                } else {
                    maxIndex = startIndex + pageSize - 1;
                }
            } else {
                for (var i = topIndex + 1; i < botIndex; i++) {
                    if (!this.isIndexLoaded($tbody, i)) {
                        startIndex = i;
                        maxIndex = this.findFirstLoadedIndex($tbody, i) - 1;
                        break;
                    }
                }
            }
            
            if (startIndex != null && maxIndex != null) {
                
                var tbodyHeight = $tbody.closest('.listgrid-body-wrapper').height();
                var delta;
                
                if (startIndex <= topIndex && maxIndex <= botIndex) {
                    // Top range missing - show in the middle of the top and max
                    delta = (topIndex + maxIndex) / 2;
                } else if (startIndex > topIndex && maxIndex < botIndex) {
                    // Mid range missing - show in the middle of the start and max
                    delta = (startIndex + maxIndex) / 2;
                } else if (startIndex > topIndex && maxIndex >= botIndex) {
                    // Bottom range missing - show in the middle of the start and bot
                    delta = (startIndex + botIndex) / 2;
                } else {
                    // Full range missing - show in the middle of the top and bot
                    delta = (topIndex + botIndex) / 2;
                }
                delta = delta - topIndex;
                
                spinnerOffset = 95 + (this.getRowHeight($tbody) * delta);
                this.showLoadingSpinner($tbody, spinnerOffset);
                
                var url = BLCAdmin.history.getUrlWithParameter('startIndex', startIndex, null, baseUrl);
                url = BLCAdmin.history.getUrlWithParameter('maxIndex', maxIndex, null, url);
                
                console.log('Loading more records -- ' + url);
                
                BLC.ajax({ url: url, type: 'GET' }, function(data) {
                    var $newTbody = $(data.trim()).find('tbody');
                    BLCAdmin.listGrid.paginate.injectRecords($tbody, $newTbody);
                    BLCAdmin.listGrid.paginate.releaseLock();
                    BLCAdmin.listGrid.paginate.hideLoadingSpinner($tbody);
                });
            } else {
                BLCAdmin.listGrid.paginate.releaseLock();
            }
        },
        
        // ************************* *
        // CUSTOM SCROLLER FUNCTIONS *
        // ************************* *
        
        getRowHeight : function($tbody) {
            if (trHeight == null) {
                trHeight = $tbody.find('tr:not(.blank-padding):first').height();
            }
            return trHeight;
        },
        
        getTopVisibleIndex : function($tbody) {
            if (typeof mcs === 'undefined') {
                return 0;
            }
            
            var scrollOffset = mcs.top;
            var trHeight = this.getRowHeight($tbody);
            var topVisibleIndex = Math.floor(scrollOffset * -1 / trHeight);
            return topVisibleIndex;
        },
        
        getBottomVisibleIndex : function($tbody) {
            var scrollOffset = mcs.top;
            var trHeight = this.getRowHeight($tbody);
            var bottomVisibleIndex = Math.floor((scrollOffset * -1 + $tbody.closest('.listgrid-body-wrapper').height() - 4) / trHeight);
            return bottomVisibleIndex;
        },
        
        scrollToIndex : function($tbody, index) {
            var offset = index * this.getRowHeight($tbody);
            console.log('scrolling to ' + offset);
            debugger;
            $tbody.closest('.listgrid-body-wrapper').find('.mCSB_container').css('top', '-' + offset + 'px');
        },
        
        updateTableFooter : function($tbody) {
            var topIndex = this.getTopVisibleIndex($tbody) + 1;
            var botIndex = this.getBottomVisibleIndex($tbody) + 1;
            var totalRecords = this.getTotalRecords($tbody);
            var $footer = $tbody.closest('.listgrid-container').find('.listgrid-table-footer');
            
            $footer.text(topIndex + ' - ' + botIndex + ' of ' + totalRecords + ' records');
        },
        
        updateGridSize : function($tbody) {
            var $wrapper = $tbody.closest('.listgrid-body-wrapper');
            var $window = $(window);
            
            var wrapperHeight = $window.height() - $wrapper.offset().top - 50;
            
            $wrapper.css('max-height', wrapperHeight + 'px');
            //$wrapper.css('max-height', '250px');
            //$wrapper.css('max-height', '99999px');
        },
        
        updateUrlFromScroll : function($tbody) {
            var topIndex = this.getTopVisibleIndex($tbody);
            BLCAdmin.history.replaceUrlParameter('startIndex', topIndex);
        },
        
        createPadding : function($tbody, startRange, endRange) {
            var rowHeight = this.getRowHeight($tbody);
            var recordsCount = endRange - startRange + 1;
            
            var $pad = $('<tr>', { 
                'class' : 'blank-padding', 
                'css' : { 
                    'height' : recordsCount * rowHeight
                },
                'data-range' : startRange + '-' + endRange
            });
            
            return $pad;
        },
        
        // ********** *
        // INITIALIZE *
        // ********** *
        
        initialize : function($container) {
            var $table = $container.find('table.list-grid-table');
            var $tbody = $table.find('tbody');
            var thWidths = [];
            
            // First, we'll adjust the size of the table to be 15px less, since this is the margin we need
            // for our scrollbar. This will ensure the widths are correct once we draw the scrollbar
            $table.css('width', ($table.width() - 15) + 'px');
            
            // Figure out what the currently drawn widths are for each row
            // This is effectively the same for all rows for both the head and the body for now
            // Also, set the width we determined directly on the element
            $table.find('th').each(function(index, thElement) {
                var $th = $(thElement);
                var width = $th.width();
                $th.css('width', width);
                thWidths[index] = width;
            });
            
            $tbody.remove();
            var $clonedTable = $table.clone();
            $table.parent().after($clonedTable);
            $table.attr('id', $table.attr('id') + '-header');
            
            $clonedTable.wrap($('<div>', { 'class' : 'listgrid-body-wrapper' }));
            var $wrapper = $clonedTable.parent();
            
            $clonedTable.find('thead').find('tr').addClass('width-control-header').find('th').empty();
            $clonedTable.append($tbody);
            $tbody = $clonedTable.find('tbody');
            
            if ($tbody.closest('table').data('listgridtype') == 'main') {
                BLCAdmin.listGrid.paginate.updateGridSize($tbody);
            }
            
            // Set up the mCustomScrollbar on the table body. Also bind the necessary events to enable infinite scrolling
            $wrapper.mCustomScrollbar({
                theme: 'dark',
                scrollInertia: 70,
                callbacks: {
                    onScroll: function() {
                        var isMainGrid = $tbody.closest('table').data('listgridtype') == 'main';
            
                        // Update the currently visible range
                        BLCAdmin.listGrid.paginate.updateTableFooter($tbody);
                        
                        // Fetch records if necessary
                        $.doTimeout('fetch', fetchDebounce, function() {
                            var url = isMainGrid ? null : $tbody.closest('table').data('path');
                            BLCAdmin.listGrid.paginate.loadRecords($tbody, url);
                        });
                        
                        // Also update the URL if we're on a main list grid
                        if (isMainGrid) {
                            $.doTimeout('updateurl', updateUrlDebounce, function(){
                                BLCAdmin.listGrid.paginate.updateUrlFromScroll($tbody);
                            });
                        }
                    }
                }
            });
            
            // Figure out how large to make the top and bottom paddings
            var range = this.getLoadedRecordRanges($tbody)[0];
            var recordsAbove = range.lo;
            var recordsBelow = this.getTotalRecords($tbody) - 1 - range.hi;
            var rowHeight = this.getRowHeight($tbody);
            
            if (recordsAbove) {
                // Create the appropriate padding above
                var $pad = this.createPadding($tbody, 0, range.lo - 1);
                $tbody.find('tr:first').before($pad);
                
                // Update the height so that the user doesn't see a scroll action
                this.scrollToIndex($tbody, range.lo);
            }
            
            if (recordsBelow) {
                var $pad = this.createPadding($tbody, range.hi + 1, this.getTotalRecords($tbody) - 1);
                $tbody.find('tr:last').after($pad);
            }
            
            // Render the table
            $wrapper.mCustomScrollbar('update');
            $clonedTable.find('tbody').css('visibility', 'visible');
        }
    };
    
})(jQuery, BLCAdmin);

$(document).ready(function() {
    
    /**
     * Performs pagination on the given list grid. 
     */
    $('body').on('click', 'ul.pagination-links a', function(event) {
        var $toReplace = $(this).closest('.listgrid-container').find('tbody');
        var $this = $(this);
        
        BLC.ajax({
            url: $(this).attr('href'),
            type: 'GET'
        }, function(data) {
            $this.closest('ul').find('a.active').removeClass('active');
            $this.addClass('active')
            $toReplace.replaceWith($(data.trim()).find('tbody'));
        });
        
        return false;
    });
    
    $(window).resize(function() {
        var $mainTbody = $('tbody').filter(function() { 
            return $(this).closest('table').data('listgridtype') == 'main';
        });
        
        if ($mainTbody.length) {
            BLCAdmin.listGrid.paginate.updateGridSize($mainTbody);
        }
    });
    
});

