package org.broadleafcommerce.common.presentation;

import org.broadleafcommerce.common.presentation.client.AddMethodType;
import org.broadleafcommerce.common.presentation.client.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to describe a simple persistent collection
 * for use by the admin tool.
 *
 * @author Jeff Fischer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AdminPresentationCollection {

    /**
     * <p>Optional - only required when targeting a metadata override
     * via application context xml.</p>
     *
     * <p>When a configuration key is present, the system will look for configuration
     * override specified in application context xml for this collection.</p>
     *
     * @return the key tied to the override configuration
     */
    String configurationKey() default "";

    /**
     * <p>Optional - field name will be used if not specified</p>
     *
     * <p>The friendly name to present to a user for this field in a GUI. If supporting i18N,
     * the friendly name may be a key to retrieve a localized friendly name using
     * the GWT support for i18N.</p>
     *
     * @return the friendly name
     */
    String friendlyName() default "";

    /**
     * <p>Optional - only required if you wish to apply security to this field</p>
     *
     * <p>If a security level is specified, it is registered with the SecurityManager.
     * The SecurityManager checks the permission of the current user to
     * determine if this field should be disabled based on the specified level.</p>
     *
     * @return the security level
     */
    String securityLevel() default "";

    /**
     * <p>Optional - fields are not excluded by default</p>
     *
     * <p>Specify if this field should be excluded from inclusion in the
     * admin presentation layer</p>
     *
     * @return whether or not the field should be excluded
     */
    boolean excluded() default false;

    /**
     * <p>Optional - only required if the collection grid UI
     * should be in read only mode</p>
     *
     * <p>Whether or not the collection can be edited</p>
     *
     * @return Whether or not the collection can be edited
     */
    boolean mutable() default true;

    /**
     * <p>Define whether or not added items for this
     * collection are acquired via search or construction.</p>
     *
     * @return the item is acquired via lookup or construction
     */
    AddMethodType addType();

    /**
     * <p>Optional - only required in the absence of a "mappedBy" property
     * on the JPA annotation</p>
     *
     * <p>For the target entity of this collection, specify the field
     * name that refers back to the parent entity.</p>
     *
     * <p>For collection definitions that use the "mappedBy" property
     * of the @OneToMany and @ManyToMany annotations, this value
     * can be safely ignored as the system will be able to infer
     * the proper value from this.</p>
     *
     * @return the parent entity referring field name
     */
    String manyToField() default "";

    /**
     * <p>Optional - only required if you want to specify ordering for this field</p>
     *
     * <p>The order in which this field will appear in a GUI relative to other collections from the same class</p>
     *
     * @return the display order
     */
    int order() default 99999;

    /**
     * <p>Optional - only required if you want the resulting collection grid element to
     * appear somewhere other than below the main detail form</p>
     *
     * <p>Specify a UI element Id to which the collection grid should be added. This is useful
     * if, for example, you want the resulting collection grid to appear in another tab, or
     * some other location in the admin tool UI.</p>
     *
     * @return UI element Id to which the collection grid should be added
     */
    String targetUIElementId() default "";

    /**
     * <p>Optional - unique name for the backing datasource. If unspecified, the datasource
     * name will be the JPA entity field name with "AdvancedCollectionDS" appended to the end.</p>
     *
     * <p>The datasource can be retrieved programatically in admin code via
     * PresenterSequenceSetupManager.getDataSource(..)</p>
     *
     * @return unique name for the backing datasource
     */
    String dataSourceName() default "";

    /**
     * <p>Optional - only required if you need to specially handle crud operations for this
     * specific collection on the server</p>
     *
     * <p>Custom string values that will be passed to the server during CRUB operations on this
     * collection. These criteria values can be detected in a custom persistence handler
     * (@CustomPersistenceHandler) in order to engage special handling through custom server
     * side code for this collection.</p>
     *
     * @return the custom string array to pass to the server during CRUD operations
     */
    String[] customCriteria() default {};

    /**
     * <p>Optional - only required if a special operation type is required for a CRUD operation. This
     * setting is not normally changed and is an advanced setting</p>
     *
     * <p>The operation type for a CRUD operation</p>
     *
     * @return the operation type
     */
    AdminPresentationOperationTypes operationTypes() default @AdminPresentationOperationTypes(addType = OperationType.BASIC, fetchType = OperationType.BASIC, inspectType = OperationType.BASIC, removeType = OperationType.BASIC, updateType = OperationType.BASIC);

}
