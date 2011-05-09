package org.broadleafcommerce.test;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.catalog.domain.Category;
import org.broadleafcommerce.catalog.domain.CategoryImpl;
import org.broadleafcommerce.catalog.domain.Product;
import org.broadleafcommerce.catalog.domain.ProductImpl;
import org.broadleafcommerce.catalog.domain.Sku;
import org.broadleafcommerce.catalog.domain.SkuImpl;
import org.broadleafcommerce.catalog.service.CatalogService;
import org.broadleafcommerce.order.dao.OrderDao;
import org.broadleafcommerce.order.domain.Order;
import org.broadleafcommerce.order.domain.OrderImpl;
import org.broadleafcommerce.order.service.OrderService;
import org.broadleafcommerce.order.service.type.OrderStatus;
import org.broadleafcommerce.pricing.domain.ShippingRate;
import org.broadleafcommerce.pricing.domain.ShippingRateImpl;
import org.broadleafcommerce.pricing.service.ShippingRateService;
import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.AddressImpl;
import org.broadleafcommerce.profile.domain.Country;
import org.broadleafcommerce.profile.domain.CountryImpl;
import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.profile.domain.CustomerAddress;
import org.broadleafcommerce.profile.domain.CustomerAddressImpl;
import org.broadleafcommerce.profile.domain.State;
import org.broadleafcommerce.profile.domain.StateImpl;
import org.broadleafcommerce.profile.service.CountryService;
import org.broadleafcommerce.profile.service.CustomerAddressService;
import org.broadleafcommerce.profile.service.CustomerService;
import org.broadleafcommerce.profile.service.StateService;
import org.broadleafcommerce.util.money.Money;

public abstract class CommonSetupBaseTest extends BaseTest {
    
	@Resource
    protected CountryService countryService;
    
    @Resource
    protected StateService stateService;
    
    @Resource
    protected CustomerService customerService;
    
    @Resource
    protected CustomerAddressService customerAddressService;
    
    @Resource
    protected CatalogService catalogService;
    
    @Resource
    protected OrderService orderService;
    
    @Resource
    protected ShippingRateService shippingRateService;

    @Resource
    private OrderDao orderDao;
    

    public void createCountry() {
        Country country = new CountryImpl();
        country.setAbbreviation("US");
        country.setName("United States");
        countryService.save(country);
    }
    
    public void createState() {
        State state = new StateImpl();
        state.setAbbreviation("KY");
        state.setName("Kentucky");
        state.setCountry(countryService.findCountryByAbbreviation("US"));
        stateService.save(state);
    }
    
    public Customer createCustomer() {
    	Customer customer = customerService.createCustomerFromId(null);
    	return customer;
    }
    
    /**
     * Creates a country, state, and customer with some CustomerAddresses
     * @return customer created
     */
    public Customer createCustomerWithAddresses() {
    	createCountry();
    	createState();
    	CustomerAddress ca1 = new CustomerAddressImpl();
        Address address1 = new AddressImpl();
        address1.setAddressLine1("1234 Merit Drive");
        address1.setCity("Bozeman");
        address1.setPostalCode("75251");
        ca1.setAddress(address1);
        ca1.setAddressName("address1");
        CustomerAddress caResult = createCustomerWithAddress(ca1);
        assert caResult != null;
        assert caResult.getCustomer() != null;
        Customer customer = caResult.getCustomer();

        CustomerAddress ca2 = new CustomerAddressImpl();
        Address address2 = new AddressImpl();
        address2.setAddressLine1("12 Testing Drive");
        address2.setCity("Portland");
        address2.setPostalCode("75251");
        ca2.setAddress(address2);
        ca2.setAddressName("address2");
        ca2.setCustomer(customer);
        CustomerAddress addResult = saveCustomerAddress(ca2);
        assert addResult != null;
        return customer;
    }
    
    /**
     * Creates a country, state, and customer with the supplied customerAddress
     * @param customerAddress
     * @return customer created
     */
    public CustomerAddress createCustomerWithAddress(CustomerAddress customerAddress) {
    	createCountry();
    	createState();
        Customer customer = createCustomer();
        customer.setUsername(String.valueOf(customer.getId()));
        customerAddress.setCustomer(customer);
        return saveCustomerAddress(customerAddress);
    }
    
    /**
     * Saves a customerAddress with state KY and country US.  Requires that createCountry() and createState() have been called
     * @param customerAddress
     * @return
     */
    public CustomerAddress saveCustomerAddress(CustomerAddress customerAddress) {
    	State state = stateService.findStateByAbbreviation("KY");
        customerAddress.getAddress().setState(state);
        Country country = countryService.findCountryByAbbreviation("US");
        customerAddress.getAddress().setCountry(country);
    	return customerAddressService.saveCustomerAddress(customerAddress);
    }
    
    /**
     * Create a state, country, and customer with a basic order and some addresses
     * @return
     */
    public Customer createCustomerWithBasicOrderAndAddresses() {
    	Customer customer = createCustomerWithAddresses();
        Order order = new OrderImpl();
        order.setStatus(OrderStatus.IN_PROCESS);
        order.setTotal(new Money(BigDecimal.valueOf(1000)));
        
    	assert order.getId() == null;
        order.setCustomer(customer);
        order = orderDao.save(order);
        assert order.getId() != null;
        
        return customer;
    }
    
    public Sku addTestSku(String skuName, String productName, String categoryName) {
    	return addTestSku(skuName, productName, categoryName, true);
    }
    
    public Sku addTestSku(String skuName, String productName, String categoryName, boolean active) {
    	Calendar activeStartCal = Calendar.getInstance();
    	activeStartCal.add(Calendar.DAY_OF_YEAR, -2);

    	Category category = new CategoryImpl();
        category.setName(categoryName);
        category.setActiveStartDate(activeStartCal.getTime());
        category = catalogService.saveCategory(category);
        Product newProduct = new ProductImpl();

        Calendar activeEndCal = Calendar.getInstance();
        activeEndCal.add(Calendar.DAY_OF_YEAR, -1);
        newProduct.setActiveStartDate(activeStartCal.getTime());
        
        newProduct.setDefaultCategory(category);
        newProduct.setName(productName);
        newProduct = catalogService.saveProduct(newProduct);

        List<Product> products = new ArrayList<Product>();
        products.add(newProduct);
        
        Sku newSku = new SkuImpl();
        newSku.setName(skuName);
        newSku.setRetailPrice(new Money(44.99));
        newSku.setActiveStartDate(activeStartCal.getTime());
        
        if (!active) {
        	newSku.setActiveEndDate(activeEndCal.getTime());
        }
        newSku.setDiscountable(true);
        newSku = catalogService.saveSku(newSku);
        newSku.setAllParentProducts(products);
        
        List<Sku> allSkus = new ArrayList<Sku>();
        allSkus.add(newSku);
        newProduct.setAllSkus(allSkus);
        newProduct = catalogService.saveProduct(newProduct);

        return newSku;
    }

    public void createShippingRates() {
        ShippingRate sr = new ShippingRateImpl();
        sr.setFeeType("SHIPPING");
        sr.setFeeSubType("ALL");
        sr.setFeeBand(1);
        sr.setBandUnitQuantity(BigDecimal.valueOf(29.99));
        sr.setBandResultQuantity(BigDecimal.valueOf(8.5));
        sr.setBandResultPercent(0);
        ShippingRate sr2 = new ShippingRateImpl();
        
        sr2.setFeeType("SHIPPING");
        sr2.setFeeSubType("ALL");
        sr2.setFeeBand(2);
        sr2.setBandUnitQuantity(BigDecimal.valueOf(999999.99));
        sr2.setBandResultQuantity(BigDecimal.valueOf(8.5));
        sr2.setBandResultPercent(0);
        
        shippingRateService.save(sr);
        shippingRateService.save(sr2);
    }

}
