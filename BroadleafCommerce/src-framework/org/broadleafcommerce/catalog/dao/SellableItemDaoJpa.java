package org.broadleafcommerce.catalog.dao;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

import org.broadleafcommerce.catalog.domain.SellableItem;
import org.springframework.stereotype.Repository;

@Repository("sellableItemDao")
public class SellableItemDaoJpa implements SellableItemDao {

    @PersistenceContext
    private EntityManager em;

    @Override
    public SellableItem maintainSellableItem(SellableItem sellableItem) {
        if(sellableItem.getId() == null){
            em.persist(sellableItem);
        }else{
            sellableItem = em.merge(sellableItem);
        }
        return sellableItem;
    }

    @Override
    public SellableItem readSellableItemById(Long sellableItemId) {
        return em.find(SellableItem.class, sellableItemId);
    }

    @Override
    public SellableItem readFirstSellableItem(){
        Query query = em.createQuery("SELECT sellableItem FROM org.broadleafcommerce.catalog.domain.SellableItem sellableItem");
        return (SellableItem) query.getSingleResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SellableItem> readAllSellableItems() {
        Query query = em.createQuery("SELECT sellableItem FROM org.broadleafcommerce.catalog.domain.SellableItem sellableItem");
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SellableItem> readSellableItemsByCategoryItemId(Long catalogItemId) {
        Query query = em.createQuery("SELECT sellableItem FROM org.broadleafcommerce.catalog.domain.SellableItem sellableItem WHERE sellableItem.catalogItem.id = :catalogItemId");
        query.setParameter("catalogItemId", catalogItemId);
        return query.getResultList();
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<SellableItem> readSellableItemById(List<Long> ids) {
        Query query = em.createQuery("SELECT sellableItem FROM org.broadleafcommerce.catalog.domain.SellableItem sellableItem WHERE sellableItem.id IN (:sellableItemIds)");
        query.setParameter("sellableItemIds", ids);
        return query.getResultList();
    }

}
