package org.broadleafcommerce.dependency.test;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.broadleafcommerce.test.integration.BaseTest;
import org.objectweb.asm.ClassReader;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class DependencyTest extends BaseTest {

    private List<String> testPackages;
    private List<String> targetPackages;
    private List<String> acceptablePackages;

    @Override
    @BeforeClass
    public void setup() {
        super.setup();
        testPackages = new ArrayList<String>();
        targetPackages = new ArrayList<String>();
        acceptablePackages = new ArrayList<String>();

        testPackages.add("javax.servlet");
        testPackages.add("org.springframework.web");

        targetPackages.add("org.broadleafcommerce.catalog");
        targetPackages.add("org.broadleafcommerce.checkout");
        targetPackages.add("org.broadleafcommerce.common");
        targetPackages.add("org.broadleafcommerce.email");
        targetPackages.add("org.broadleafcommerce.inventory");
        targetPackages.add("org.broadleafcommerce.marketing");
        targetPackages.add("org.broadleafcommerce.offer");
        targetPackages.add("org.broadleafcommerce.order");
        targetPackages.add("org.broadleafcommerce.payment");
        targetPackages.add("org.broadleafcommerce.pricing");
        //targetPackages.add("org.broadleafcommerce.promotion");
        targetPackages.add("org.broadleafcommerce.rules");
        targetPackages.add("org.broadleafcommerce.search");
        targetPackages.add("org.broadleafcommerce.util");
        targetPackages.add("org.broadleafcommerce.workflow");
        targetPackages.add("org.broadleafcommerce.profile");

        acceptablePackages.add("org.broadleafcommerce.catalog.web");
        acceptablePackages.add("org.broadleafcommerce.email.web");
        acceptablePackages.add("org.broadleafcommerce.order.web");
        acceptablePackages.add("org.broadleafcommerce.profile.web");
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testDependencies() throws Exception {
        for(String targetPackage : targetPackages) {
            DependencyVisitor v = new DependencyVisitor();

            List<Class> classes = getClasses(targetPackage);
            List<Class> finalClasses = new ArrayList<Class>();
            /*
             * remove acceptable packages
             */
            for (Class clazz : classes) {
                testPackage: {
                for (String acceptablePackage : acceptablePackages) {
                    if (clazz.getName().startsWith(acceptablePackage)) {
                        break testPackage;
                    }
                }
                finalClasses.add(clazz);
            }
            }

            for (Class clazz : finalClasses) {
                new ClassReader(clazz.getName()).accept(v, false);
            }

            Set<String> classPackages = v.getPackages();
            String[] classNames = classPackages.toArray(new String[classPackages.size()]);
            for (String className : classNames) {
                className = className.replace('/', '.');
                for (String testPackage : testPackages) {
                    if (className.startsWith(testPackage) || testPackage.startsWith(className)) {
                        throw new RuntimeException("Improper dependency (" + className + ") found in package (" + targetPackage + ")");
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private List<Class> getClasses(String pckgname) throws ClassNotFoundException {
        ArrayList<Class> classes=new ArrayList<Class>();
        try {
            URL url = DependencyVisitor.class.getResource('/'+pckgname.replace('.', '/'));
            if (url.getProtocol().equals("jar")) {
                String jarPath = url.getPath().substring(5, url.getPath().indexOf("!"));
                JarFile jar = new JarFile(jarPath);
                addClasses(jar, classes, pckgname);
            } else {
                File directory= new File(url.getFile());
                addClasses(directory, classes, pckgname);
            }
        } catch(Exception x) {
            throw new ClassNotFoundException(pckgname+" does not appear to be a valid package", x);
        }
        return classes;
    }

    @SuppressWarnings("unchecked")
    private void addClasses(JarFile jar, List<Class> classes, String pckgname) throws ClassNotFoundException {
        Enumeration<JarEntry> entries = jar.entries();
        while(entries.hasMoreElements()) {
            String name = entries.nextElement().getName().replace('/', '.');
            if (name.startsWith(pckgname) && name.endsWith(".class")) {
                classes.add(Class.forName(name.substring(0, name.length()-6)));
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void addClasses(File directory, List<Class> classes, String pckgname) throws ClassNotFoundException {
        File[] files=directory.listFiles();
        for(File file : files) {
            if(file.getName().endsWith(".class")) {
                String fileName = file.getName();
                classes.add(Class.forName(pckgname + "." + fileName.substring(0, fileName.length()-6)));
            } else if (file.isDirectory()) {
                addClasses(file, classes, pckgname + "." + file.getName());
            }
        }
    }

}
