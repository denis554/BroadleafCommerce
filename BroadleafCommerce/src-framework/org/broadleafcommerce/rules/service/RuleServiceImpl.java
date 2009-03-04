package org.broadleafcommerce.rules.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.math.BigDecimal;

import javax.annotation.Resource;

import org.broadleafcommerce.rules.dao.RuleDao;
import org.broadleafcommerce.rules.domain.ShoppingCartPromotion;
import org.drools.compiler.DroolsParserException;
import org.drools.compiler.PackageBuilder;
import org.drools.rule.Package;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service("ruleServiceImpl")
public class RuleServiceImpl implements RuleService {

    @Resource
    private RuleDao ruleDao;

    @Resource
    private RuleBaseService ruleBaseService;

    public ShoppingCartPromotion saveShoppingCartPromotion(ShoppingCartPromotion shoppingCartPromotion) {
        return ruleDao.maintainShoppingCartPromotion(shoppingCartPromotion);
    }

    public Package addRuleToNewPackage(File drlFile) {

        PackageBuilder pkgBuilder = new PackageBuilder();
        Reader drlFileReader = null;
        ;

        try {
            drlFileReader = new FileReader(drlFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        try {
            pkgBuilder.addPackageFromDrl(drlFileReader);
        } catch (DroolsParserException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Package pkg = pkgBuilder.getPackage();

        return pkg;

    }

    public void removeRuleFromRuleBase(String pkg, Long id) {

        // Pass in the package name and rule name as strings to remove the rule
        ruleBaseService.getRuleBase().removeRule(pkg, id.toString());

    }

    public void mergePackageWithRuleBase(Package pkg) {

        try {
            ruleBaseService.getRuleBase().addPackage(pkg);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public void writeRuleFile(ShoppingCartPromotion shoppingCartPromotion, String logicalOperator) {

        try {

            File drlFile = new File("war/WEB-INF/drools/" + shoppingCartPromotion.getName() + ".drl");

            Writer output = new BufferedWriter(new FileWriter(drlFile));

            if (drlFile == null) {
                throw new IllegalArgumentException("File should not be null.");
            }
            if (!drlFile.exists()) {
                throw new FileNotFoundException("File does not exist: " + drlFile);
            }
            if (!drlFile.isFile()) {
                throw new IllegalArgumentException("Should not be a directory: " + drlFile);
            }
            if (!drlFile.canWrite()) {
                throw new IllegalArgumentException("File cannot be written: " + drlFile);
            }

            String newLine = "\n";
            String tab = "\t";

            output.write("package org.broadleafcommerce.rules;" + newLine);
            output.write("import org.broadleafcommerce.rules.domain.CouponCode;" + newLine);
            output.write("import org.broadleafcommerce.order.domain.Order;" + newLine);

            output.write("rule \"" + shoppingCartPromotion.getName() + "\"" + newLine);

            output.write("when" + newLine + tab);

            if (!shoppingCartPromotion.getCouponCode().isEmpty()) {
                output.write("CouponCode(code == \"" + shoppingCartPromotion.getCouponCode() + "\")" + newLine + tab);
            }

            if (shoppingCartPromotion.getOrderTotal().compareTo(BigDecimal.ZERO) > 0) {
                output.write("BroadleafOrder(orderTotal " + logicalOperator + " " + shoppingCartPromotion.getOrderTotal() + ")" + newLine);
            }

            output.write("then" + newLine + tab);

            output.write("System.out.println(\"SUCCESS\");" + newLine);

            output.write("end");

            output.close();

            Package pkg = addRuleToNewPackage(drlFile);

            mergePackageWithRuleBase(pkg);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error writing drools file");
        } finally {

        }
    }
}
