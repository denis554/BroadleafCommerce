package org.broadleafcommerce.cms.field.domain;

import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;

import javax.persistence.*;
import java.util.List;

/**
 * Created by jfischer
 */
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@Table(name = "BLC_FIELD_ENUM")
@Cache(usage= CacheConcurrencyStrategy.NONSTRICT_READ_WRITE, region="blCMSElements")
public class FieldEnumerationImpl implements FieldEnumeration {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "FieldEnumerationId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "FieldEnumerationId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "FieldEnumerationImpl", allocationSize = 10)
    @Column(name = "FIELD_ENUM_ID")
    protected Long id;

    @Column (name = "NAME")
    protected String name;

    @OneToMany(mappedBy = "fieldEnumeration", targetEntity = FieldEnumerationItemImpl.class, cascade = {CascadeType.ALL})
    @Cascade(value={org.hibernate.annotations.CascadeType.ALL, org.hibernate.annotations.CascadeType.DELETE_ORPHAN})
    @Cache(usage = CacheConcurrencyStrategy.READ_WRITE, region="blCMSElements")
    @OrderColumn(name = "FIELD_ORDER")
    @BatchSize(size = 20)
    protected List<FieldEnumerationItem> enumerationItems;

    @Override
    public List<FieldEnumerationItem> getEnumerationItems() {
        return enumerationItems;
    }

    @Override
    public void setEnumerationItems(List<FieldEnumerationItem> enumerationItems) {
        this.enumerationItems = enumerationItems;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }
}
