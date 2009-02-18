package org.broadleafcommerce.controller;

import java.net.BindException;
import java.util.Iterator;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.broadleafcommerce.controller.validator.CheckoutValidator;
import org.broadleafcommerce.order.domain.BroadleafOrder;
import org.broadleafcommerce.order.domain.OrderPayment;
import org.broadleafcommerce.order.domain.OrderShipping;
import org.broadleafcommerce.order.service.OrderService;
import org.broadleafcommerce.profile.domain.Address;
import org.broadleafcommerce.profile.domain.BroadleafContactInfo;
import org.broadleafcommerce.profile.domain.BroadleafCustomerAddress;
import org.broadleafcommerce.profile.domain.ContactInfo;
import org.broadleafcommerce.profile.domain.Customer;
import org.broadleafcommerce.profile.service.AddressService;
import org.broadleafcommerce.profile.service.AddressStandardizationService;
import org.broadleafcommerce.profile.service.ContactInfoService;
import org.broadleafcommerce.profile.service.CustomerService;
import org.broadleafcommerce.profile.service.addressValidation.AddressStandarizationResponse;
import org.broadleafcommerce.util.Checkout;
import org.springframework.security.Authentication;
import org.springframework.security.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractWizardFormController;

public class CheckoutController extends AbstractWizardFormController {

    CustomerService customerService;
    OrderService orderService;
    ContactInfoService contactInfoService;
    AddressService addressService;
    AddressStandardizationService addressStandardizationService;

    private String successView;

    public CheckoutController() {
        setCommandClass(Checkout.class);
    }

    @Override
    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        Checkout checkout = new Checkout();

        BroadleafOrder order;

        OrderShipping orderShipping = new OrderShipping();
        orderShipping.setAddress(new BroadleafCustomerAddress());

        OrderPayment orderPayment = new OrderPayment();
        orderPayment.setAddress(new BroadleafCustomerAddress());

        ContactInfo contactInfo = new BroadleafContactInfo();

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Customer customer = customerService.readCustomerByUsername(auth.getName());
        List<ContactInfo> contactInfos = contactInfoService.readContactInfoByUserId(customer.getId());
        List<Address> addressList = addressService.readAddressByUserId(customer.getId());

        order = orderService.getCurrentBasketForCustomer(customer);

        checkout.setUserContactInfo(contactInfos);
        checkout.setAddressList(addressList);
        checkout.setOrder(orderService.getCurrentBasketForUserId(customer.getId()));
        checkout.setOrderItems(orderService.getItemsForOrder(order.getId()));
        checkout.setContactInfo(contactInfo);
        checkout.setOrderShipping(orderShipping);
        checkout.setOrderPayment(orderPayment);
        return checkout;
    }

    protected ModelAndView processFinish(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        Checkout checkout = (Checkout) command;
        return new ModelAndView("printCommand", "command", checkout);
    }

    protected ModelAndView processCancel(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        return new ModelAndView("printCommand", "command", "survey form submission cancelled");

    }

    @Override
    protected void validatePage(Object command, Errors errors, int page, boolean finish) {
        Checkout checkout = (Checkout) command;
        CheckoutValidator validator = (CheckoutValidator) getValidator();

        switch (page) {
        case 0:
            String contactIndex = checkout.getSelectedContactInfoId();
            if (contactIndex != null && !contactIndex.equals("")) {
                checkout.setContactInfo(checkout.getUserContactInfo().get(Integer.parseInt(contactIndex) - 1));
            } else {
                validator.validatePageContactInformation(command, errors);
            }
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            Customer customer = customerService.readCustomerByUsername(auth.getName());
            checkout.getContactInfo().setCustomer(customer);
            orderService.addContactInfoToOrder(checkout.getOrder(), checkout.getContactInfo());
            break;
        case 1:
            String shippingAddressId = checkout.getSelectedShippingAddressId();
            if (!StringUtils.isEmpty(shippingAddressId)) {
                Iterator<Address> itr = checkout.getAddressList().iterator();
                while (itr.hasNext()) {
                    Address address = itr.next();
                    if (address.getId().equals(Long.parseLong(shippingAddressId))) {
                        checkout.getOrderShipping().setAddress(address);
                        break;
                    }
                }
            } else {
                validator.validateShippingAddressInformation(command, errors);
                Address address = checkout.getOrderShipping().getAddress();
                if (!errors.hasErrors()) {
                    AddressStandarizationResponse standardizedResponse = addressStandardizationService.standardizeAddress(address);
                    if (standardizedResponse.isErrorDetected()) {
                        address.setStandardized(false);
                        errors.rejectValue("orderShipping.address.addressLine1", "addressVerification.failed", null, null);
                    }
                }
            }
            orderService.addShippingToOrder(checkout.getOrder(), checkout.getOrderShipping());
            break;
        case 2:
            String billingAddressId = checkout.getSelectedBillingAddressId();
            if (!StringUtils.isEmpty(billingAddressId)) {
                Iterator<Address> itr = checkout.getAddressList().iterator();
                while (itr.hasNext()) {
                    Address address = itr.next();
                    if (address.getId().equals(Long.parseLong(billingAddressId))) {
                        checkout.getOrderPayment().setAddress(address);
                        break;
                    }
                }
            } else {
                validator.validateBillingAddressInformation(command, errors);
                Address billingAddress = checkout.getOrderPayment().getAddress();
                if (!errors.hasErrors()) {
                    AddressStandarizationResponse standardizedResponse = addressStandardizationService.standardizeAddress(billingAddress);
                    if (standardizedResponse.isErrorDetected()) {
                        logger.debug("Address verification Failed. Please check the address and try again");
                        billingAddress.setStandardized(false);
                        errors.rejectValue("orderPayment.address.addressLine1", "addressVerification.failed", null, null);
                    }
                }
            }
            orderService.addPaymentToOrder(checkout.getOrder(), checkout.getOrderPayment());
            break;
        case 3:

            finish = true;
            break;
        default:
        }
        if (finish) {
            orderService.confirmOrder(checkout.getOrder());
        }
    }

    @Override
    protected ModelAndView processFinish(HttpServletRequest arg0, HttpServletResponse arg1, Object arg2, org.springframework.validation.BindException arg3) throws Exception {
        // TODO Auto-generated method stub
        ModelAndView mav = new ModelAndView(getSuccessView());
        return mav;
    }

    public OrderService getOrderService() {
        return orderService;
    }

    public void setOrderService(OrderService orderService) {
        this.orderService = orderService;
    }

    public CustomerService getUserService() {
        return customerService;
    }

    public void setCustomerService(CustomerService customerService) {
        this.customerService = customerService;
    }

    public ContactInfoService getContactInfoService() {
        return contactInfoService;
    }

    public void setContactInfoService(ContactInfoService contactInfoService) {
        this.contactInfoService = contactInfoService;
    }

    public void setAddressStandardizationService(AddressStandardizationService addressStandardizationService) {
        this.addressStandardizationService = addressStandardizationService;
    }

    public void setAddressService(AddressService addressService) {
        this.addressService = addressService;
    }

    public String getSuccessView() {
        return successView;
    }

    public void setSuccessView(String successView) {
        this.successView = successView;
    }

}
