package org.broadleafcommerce.util;

public class Mod43CheckDigitUtil {

    private final static String CHARSET = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ-. $/+%";

    public static boolean isValidCheckedValue(String value) {
        boolean valid = false;
        if (value != null && !value.isEmpty()) {
            String code = value.substring(0, value.length() - 1);
            char checkDigit = value.substring(value.length() - 1).charAt(0);
            try {
                if (generateCheckDigit(code) == checkDigit) {
                    valid = true;
                }
            } catch (Exception e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return valid;
    }

    public static char generateCheckDigit(String data) {
        // MOD 43 check digit - take the acsii value of each digit, sum them up, divide by 43. the remainder is the check digit (in ascii)
        int sum = 0;
        for (int i = 0; i < data.length(); ++i) {
            sum += CHARSET.indexOf(data.charAt(i));
        }
        int remainder = sum % 43;
        return CHARSET.charAt(remainder);
    }

    public static void main(String[] args) {
        try {
            System.out.println(generateCheckDigit("TEACH000012345"));
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        System.out.println(isValidCheckedValue("TEACH000012345B"));
    }
}
