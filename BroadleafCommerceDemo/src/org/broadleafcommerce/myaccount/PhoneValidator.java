package org.broadleafcommerce.myaccount;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.profile.domain.Phone;
import org.springframework.validation.Errors;
import org.springframework.validation.ValidationUtils;
import org.springframework.validation.Validator;

public class PhoneValidator implements Validator {

/** Logger for this class and subclasses */
protected final Log logger = LogFactory.getLog(getClass());

@SuppressWarnings("unchecked")
@Override
public boolean supports(Class clazz) {
return clazz.equals(Phone.class);
}

@Override
public void validate(Object obj, Errors errors) {
//use regular phone
Phone phone = (Phone) obj;
ValidationUtils.rejectIfEmptyOrWhitespace(errors, "phoneNumber", "field.required", new String[] { "phoneNumber" });
if (!errors.hasErrors()) {
String phoneNumber = phone.getPhoneNumber();
String newString = phoneNumber.replaceAll("\\D", "");
if (newString.length() != 10) {
errors.rejectValue("phoneNumber", "phone.ten_digits_required", null);
}

// Check for common false data.
if (newString.equals("1234567890")
|| newString.equals("0123456789")
|| newString.matches("0{10}")
|| newString.matches("1{10}")
|| newString.matches("2{10}")
|| newString.matches("3{10}")
|| newString.matches("4{10}")
|| newString.matches("5{10}")
|| newString.matches("6{10}")
|| newString.matches("7{10}")
|| newString.matches("8{10}")
|| newString.matches("9{10}")) {
errors.rejectValue("phoneNumber", "phone.invalid", null);
}
}
}
}