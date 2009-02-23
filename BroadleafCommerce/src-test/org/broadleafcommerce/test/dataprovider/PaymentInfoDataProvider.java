package org.broadleafcommerce.test.dataprovider;

import java.math.BigDecimal;

import org.broadleafcommerce.order.domain.BroadleafPaymentInfo;
import org.broadleafcommerce.order.domain.PaymentInfo;
import org.testng.annotations.DataProvider;

public class PaymentInfoDataProvider {

    @DataProvider(name = "basicPaymentInfo")
    public static Object[][] provideBasicSalesPaymentInfo() {
        PaymentInfo sop = new BroadleafPaymentInfo();
        sop.setAmount(BigDecimal.valueOf(10.99));
        sop.setReferenceNumber("987654321");
        return new Object[][] { { sop } };
    }
}
