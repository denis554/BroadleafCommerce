package org.broadleafcommerce.common.presentation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Adorned target collections are a variant of the basic collection type (@see AdminPresentationCollection).
 * This type of collection concept comes into play when you want to represent a "ToMany" association, but
 * you also want to capture some additional data around the association. CrossSaleProductImpl is an example of
 * this concept. CrossSaleProductImpl not only contains a product reference, but sequence and
 * promotional message fields as well. We want the admin user to choose the desired product for the association
 * and also specify the order and promotional message information to complete the interaction.
 * The Adorned target concept embodied in this annotation makes this possible.
 *
 * @author Jeff Fischer
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
public @interface AdminPresentationMap {

    /**
     * Optional - field name will be used if not specified
     *
     * The friendly name to present to a user for this field in a GUI. If supporting i18N,
     * the friendly name may be a key to retrieve a localized friendly name using
     * the GWT support for i18N.
     *
     * @return the friendly name
     */
    String friendlyName() default "";

    /**
     * Optional - only required if you wish to apply security to this field
     *
     * If a security level is specified, it is registered with the SecurityManager.
     * The SecurityManager checks the permission of the current user to
     * determine if this field should be disabled based on the specified level.
     *
     * @return the security level
     */
    String securityLevel() default "";

    /**
     * Optional - fields are not excluded by default
     *
     * Specify if this field should be excluded from inclusion in the
     * admin presentation layer
     *
     * @return whether or not the field should be excluded
     */
    boolean excluded() default false;

    /**
     * Optional - only required if you want to specify ordering for this field
     *
     * The order in which this field will appear in a GUI relative to other fields from the same class
     *
     * @return the display order
     */
    int order() default 99999;

    /**
     * Optional - only required if you want the resulting collection grid element to
     * appear somewhere other than below the main detail form
     *
     * Specify a UI element Id to which the collection grid should be added. This is useful
     * if, for example, you want the resulting collection grid to appear in another tab, or
     * some other location in the admin tool UI.
     *
     * @return UI element Id to which the collection grid should be added
     */
    String targetUIElementId() default "";

    /**
     * Optional - unique name for the backing datasource. If unspecified, the datasource
     * name will be the JPA entity field name with "AdvancedCollectionDS" appended to the end.
     *
     * The datasource can be retrieved programatically in admin code via
     * PresenterSequenceSetupManager.getDataSource(..)
     *
     * @return unique name for the backing datasource
     */
    String dataSourceName() default "";






    /**
     * Optional - only required if the type for the key of this map
     * is other than java.lang.String, or if the map is not a generic
     * type from which the key type can be derived
     *
     * The type for the key of this map
     *
     * @return The type for the key of this map
     */
    String keyClassName() default "";

    /**
     * Optional - only required if the key field title for this
     * map should be translated to another lang, or should be
     * something other than the constant "Key"
     *
     * The friendly name to present to a user for this key field title in a GUI. If supporting i18N,
     * the friendly name may be a key to retrieve a localized friendly name using
     * the GWT support for i18N.
     *
     * @return the friendly name
     */
    String keyPropertyFriendlyName() default "Key";

    /**
     * Optional - only required if the type for the value of this map
     * is other than java.lang.String, or if the map is not a generic
     * type from which the value type can be derived, or if there is
     * not a @ManyToMany annotation used from which a targetEntity
     * can be inferred.
     *
     * The type for the value of this map
     *
     * @return The type for the value of this map
     */
    String valueClassName() default "";

    /**
     * Optional - only required if the value class is a
     * JPA managed type and the persisted entity should
     * be deleted upon removal from this map
     *
     * Whether or not a complex (JPA managed) value should
     * be deleted upon removal from this map
     *
     * @return Whether or not a complex value is deleted upon map removal
     */
    boolean deleteEntityUponRemove() default false;

    /**
     * Optional - only required if the value property for this map
     * is simple (Not JPA managed - e.g. java.lang.String) and if the
     * value field title for this map should be translated to another lang, or
     * should be something other than the constant "Value"
     *
     * The friendly name to present to a user for this value field title in a GUI. If supporting i18N,
     * the friendly name may be a key to retrieve a localized friendly name using
     * the GWT support for i18N.
     *
     * @return the friendly name
     */
    String valuePropertyFriendlyName() default "Value";

    /**
     * Optional - only required if the value type cannot be derived from the map
     * declaration in the JPA managed entity and the value type is complex (JPA managed entity)
     *
     * Whether or not the value type for the map is complex (JPA managed entity), rather than an simple
     * type (e.g. java.lang.String). This can usually be inferred from the parameterized type of the map
     * (if available), or from the targetEntity property of a @ManyToMany annotation for the map (if available).
     *
     * @return Whether or not the value type for the map is complex
     */
    boolean isSimpleValue() default true;

    /**
     * Optional - only required if the value type for the map is complex (JPA managed) and one of the fields
     * of the complex value provides a URL value that points to a resolvable image url.
     *
     * The field name of complex value that provides an image url
     *
     * @return The field name of complex value that provides an image url
     */
    String mediaField() default "";

    /**
     * Optional - only required when the user should select from a list of pre-defined
     * keys when adding/editing this map
     *
     * Specify the keys available for the user to select from
     *
     * @return the array of keys from which the user can select
     */
    AdminPresentationMapKey[] keys() default {};
}
