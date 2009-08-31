package org.broadleafcommerce.search.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "BLC_SEARCH_SYNONYM")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class SearchSynonymImpl implements SearchSynonym {
    @Id
    @GeneratedValue(generator = "SearchSynonymId", strategy = GenerationType.TABLE)
    @TableGenerator(name = "SearchSynonymId", table = "SEQUENCE_GENERATOR", pkColumnName = "ID_NAME", valueColumnName = "ID_VAL", pkColumnValue = "SearchSynonymImpl", allocationSize = 50)
    @Column(name = "SEARCH_SYNONYM_ID")
    private Long id;
    @Column(name = "TERM")
    private String term;
    @Column(name = "SYNONYMS")
    private String synonyms;
    
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }
    public String getTerm() {
        return term;
    }
    public void setTerm(String term) {
        this.term = term;
    }
    public String[] getSynonyms() {
        return synonyms.split("|");
    }
    public void setSynonyms(String[] synonyms) {
        this.synonyms = StringUtils.join(synonyms, '|');
    }
    
}