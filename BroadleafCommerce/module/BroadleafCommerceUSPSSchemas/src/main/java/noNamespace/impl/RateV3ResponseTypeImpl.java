/*
 * XML Type:  RateV3ResponseType
 * Namespace: 
 * Java type: noNamespace.RateV3ResponseType
 *
 * Automatically generated - do not modify.
 */
package noNamespace.impl;
/**
 * An XML RateV3ResponseType(@).
 *
 * This is a complex type.
 */
public class RateV3ResponseTypeImpl extends org.apache.xmlbeans.impl.values.XmlComplexContentImpl implements noNamespace.RateV3ResponseType
{
    private static final long serialVersionUID = 1L;
    
    public RateV3ResponseTypeImpl(org.apache.xmlbeans.SchemaType sType)
    {
        super(sType);
    }
    
    private static final javax.xml.namespace.QName PACKAGE$0 = 
        new javax.xml.namespace.QName("", "Package");
    
    
    /**
     * Gets array of all "Package" elements
     */
    public noNamespace.ResponsePackageV3Type[] getPackageArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            java.util.List targetList = new java.util.ArrayList();
            get_store().find_all_element_users(PACKAGE$0, targetList);
            noNamespace.ResponsePackageV3Type[] result = new noNamespace.ResponsePackageV3Type[targetList.size()];
            targetList.toArray(result);
            return result;
        }
    }
    
    /**
     * Gets ith "Package" element
     */
    public noNamespace.ResponsePackageV3Type getPackageArray(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            noNamespace.ResponsePackageV3Type target = null;
            target = (noNamespace.ResponsePackageV3Type)get_store().find_element_user(PACKAGE$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            return target;
        }
    }
    
    /**
     * Returns number of "Package" element
     */
    public int sizeOfPackageArray()
    {
        synchronized (monitor())
        {
            check_orphaned();
            return get_store().count_elements(PACKAGE$0);
        }
    }
    
    /**
     * Sets array of all "Package" element
     */
    public void setPackageArray(noNamespace.ResponsePackageV3Type[] xpackageArray)
    {
        synchronized (monitor())
        {
            check_orphaned();
            arraySetterHelper(xpackageArray, PACKAGE$0);
        }
    }
    
    /**
     * Sets ith "Package" element
     */
    public void setPackageArray(int i, noNamespace.ResponsePackageV3Type xpackage)
    {
        synchronized (monitor())
        {
            check_orphaned();
            noNamespace.ResponsePackageV3Type target = null;
            target = (noNamespace.ResponsePackageV3Type)get_store().find_element_user(PACKAGE$0, i);
            if (target == null)
            {
                throw new IndexOutOfBoundsException();
            }
            target.set(xpackage);
        }
    }
    
    /**
     * Inserts and returns a new empty value (as xml) as the ith "Package" element
     */
    public noNamespace.ResponsePackageV3Type insertNewPackage(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            noNamespace.ResponsePackageV3Type target = null;
            target = (noNamespace.ResponsePackageV3Type)get_store().insert_element_user(PACKAGE$0, i);
            return target;
        }
    }
    
    /**
     * Appends and returns a new empty value (as xml) as the last "Package" element
     */
    public noNamespace.ResponsePackageV3Type addNewPackage()
    {
        synchronized (monitor())
        {
            check_orphaned();
            noNamespace.ResponsePackageV3Type target = null;
            target = (noNamespace.ResponsePackageV3Type)get_store().add_element_user(PACKAGE$0);
            return target;
        }
    }
    
    /**
     * Removes the ith "Package" element
     */
    public void removePackage(int i)
    {
        synchronized (monitor())
        {
            check_orphaned();
            get_store().remove_element(PACKAGE$0, i);
        }
    }
}
