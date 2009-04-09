package org.broadleafcommerce.test.integration;

import java.math.BigDecimal;

import javax.annotation.Resource;

import org.broadleafcommerce.catalog.dao.SkuDao;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.test.dataprovider.SkuDaoDataProvider;
import org.broadleafcommerce.util.money.Money;
import org.springframework.test.annotation.Rollback;
import org.testng.annotations.Test;

public class SkuDaoTest extends BaseTest {

    private Long skuId;

    @Resource
    private SkuDao skuDao;

    @Test(groups = { "createSku" }, dataProvider = "basicSku", dataProviderClass = SkuDaoDataProvider.class, dependsOnGroups = { "readCustomer1", "createOrder", "createProduct" })
    @Rollback(false)
    public void createSku(Sku sku) {
        sku.setSalePrice(new Money(BigDecimal.valueOf(10.0)));
        assert sku.getId() == null;
        sku = skuDao.maintainSku(sku);
        assert sku.getId() != null;
        skuId = sku.getId();
    }

    @Test(groups = { "readFirstSku" }, dependsOnGroups = { "createSku" })
    public void readFirstSku() {
        Sku si = skuDao.readFirstSku();
        assert si != null;
        assert si.getId() != null;
    }

    @Test(groups = { "readSkuById" }, dependsOnGroups = { "createSku" })
    public void readSkuById() {
        Sku item = skuDao.readSkuById(skuId);
        assert item != null;
        assert item.getId() == skuId;
    }
}
