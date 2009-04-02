package org.broadleafcommerce.inventory.test;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.inventory.dao.AvailabilityDao;
import org.broadleafcommerce.inventory.domain.SkuAvailability;
import org.broadleafcommerce.inventory.service.AvailabilityService;
import org.broadleafcommerce.inventory.test.dataprovider.SkuAvailabilityDataProvider;
import org.broadleafcommerce.test.integration.BaseTest;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

public class SkuAvailabilityTest extends BaseTest {
    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());
	protected final Long[] skuIDs = {1L, 2L, 3L, 4L, 5L};
	protected final List<Long> skuIdList = Arrays.asList(skuIDs);

    @Resource
    private AvailabilityService availabilityService;

    @Resource
    private AvailabilityDao availabilityDao;

    @Test(groups = { "createSkuAvailability" }, dataProvider = "setupSkuAvailability", dataProviderClass = SkuAvailabilityDataProvider.class)
    @Rollback(false)
    public void createSkuAvailability(SkuAvailability skuAvailability) {
    	availabilityDao.saveSKUAvailability(skuAvailability);
    }

    @Test(dependsOnGroups = { "createSkuAvailability" })
    public void readSKUAvailabilityEntries() {
        List<SkuAvailability> skuAvailabilityList = availabilityService.lookupSKUAvailability(skuIdList, false);
        assert(skuAvailabilityList.size() == 5);

        int backorderCount=0;
        int availableCount=0;
        for (SkuAvailability skuAvailability : skuAvailabilityList) {
			if (skuAvailability.getAvailabilityStatus() != null && skuAvailability.getAvailabilityStatus().equals("backordered")) {
				backorderCount++;
			}
			if (skuAvailability.getAvailabilityStatus() != null && skuAvailability.getAvailabilityStatus().equals("available")) {
				availableCount++;
			}
		}
        assert(backorderCount == 1);
        assert(availableCount == 1);
    }

    @Test(dependsOnGroups = { "createSkuAvailability" })
    public void readAvailableSkusForUnknownLocation() {
        List<SkuAvailability> skuAvailabilityList = availabilityService.lookupSKUAvailabilityForLocation(skuIdList, 100L, false);
        assert(skuAvailabilityList.size() == 0);
    }

    @Test(dependsOnGroups = { "createSkuAvailability" })
    public void readAvailableSkusForLocation() {
        List<SkuAvailability> skuAvailabilityList = availabilityService.lookupSKUAvailabilityForLocation(skuIdList, 1L, false);
        assert(skuAvailabilityList.size() == 5);
    }

    @Test(dependsOnGroups = { "createSkuAvailability" })
    public void checkAvailableQuantityWithReserveAndQOH() {
        SkuAvailability skuAvailability = availabilityService.lookupSKUAvailabilityForLocation(2L, 1L, false);
        assert(skuAvailability.getReserveQuantity() == 1 && skuAvailability.getQuantityOnHand() == 5);
        assert(skuAvailability.getAvailableQuantity() == 4);
    }

    @Test(dependsOnGroups = { "createSkuAvailability" })
    public void checkAvailableQuantityWithNullReserveQty() {
        SkuAvailability skuAvailability = availabilityService.lookupSKUAvailabilityForLocation(5L, 1L, false);
        assert(skuAvailability.getReserveQuantity() == null && skuAvailability.getQuantityOnHand() == 5);
        assert(skuAvailability.getAvailableQuantity() == 5);
    }

    @Test(dependsOnGroups = { "createSkuAvailability" })
    public void checkAvailableQuantityWithNullQuantityOnHand() {
        SkuAvailability skuAvailability = availabilityService.lookupSKUAvailabilityForLocation(1L, 1L, false);
        assert(skuAvailability.getReserveQuantity() == 1 && skuAvailability.getQuantityOnHand() == null);
        assert(skuAvailability.getAvailableQuantity() == null);
    }
}
