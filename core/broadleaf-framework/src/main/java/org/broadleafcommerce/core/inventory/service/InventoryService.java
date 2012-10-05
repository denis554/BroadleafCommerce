package org.broadleafcommerce.core.inventory.service;

import org.broadleafcommerce.core.catalog.domain.Sku;
import org.broadleafcommerce.core.inventory.domain.FulfillmentLocation;
import org.broadleafcommerce.core.inventory.domain.Inventory;
import org.broadleafcommerce.core.inventory.exception.ConcurrentInventoryModificationException;
import org.broadleafcommerce.core.inventory.exception.InventoryUnavailableException;

import java.util.List;
import java.util.Map;

/**
 * Copyright 2012 the original author or authors.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * This is a basic inventory service for Broadleaf Commerce.  This API defines a basic set of functions for checking 
 * inventory availability and for adjusting inventory.
 * 
 * NOTE: If you wrap this service inside another service or transactional component, it may be best to ensure that 
 * transactions are rolled back when encountering checked exceptions that are thrown from this service, such as 
 * {@link InventoryUnavailableException} and {@link ConcurrentInventoryModificationException}
 * 
 * @author Kelly Tisdell
 *
 */
public interface InventoryService {

    /**
     * Retrieves whether or not the quantity is available for a sku at all fulfillment locations.
     * @param sku the sku
     * @param quantity the amount for which to check; must be a positive integer
     * @return the boolean result of whether or not the quantity is available
     */
    public boolean isQuantityAvailable(Sku sku, Integer quantity);

    /**
     * Retrieves whether or not the quantity is available for a sku at a fulfillment location.
     * If fulfillmentLocation is not supplied, this will check all fulfillment locations for availability.
     * @param sku the sku
     * @param quantity the amount for which to check; must be a positive integer
     * @param fulfillmentLocation the fulfillment location
     * @return boolean result of whether or not the specified quantity is available
     */
    public boolean isQuantityAvailable(Sku sku, Integer quantity, FulfillmentLocation fulfillmentLocation);

    /**
     * Subtracts the quantity from available inventory in the default fulfillment location for each sku in the map. Specified quantity must be a positive integer.
     * @param skuInventory a map which contains the quantity of inventory to subtract from available inventory for each sku
     */
    public void decrementInventory(Map<Sku, Integer> skuInventory) throws ConcurrentInventoryModificationException, InventoryUnavailableException;

    /**
     * Subtracts the quantity from available inventory for each sku in the map for the given fulfillment location.
     * Quantity must be a positive integer.
     * @param skuInventory a map which contains the quantity of inventory to subtract from available inventory for each sku
     * @param fulfillmentLocation the fulfillment location
     */
    public void decrementInventory(Map<Sku, Integer> skuInventory, FulfillmentLocation fulfillmentLocation) throws ConcurrentInventoryModificationException, InventoryUnavailableException;

    /**
     * Add available inventory to sku. If fulfillment location is null, this method throws an {@link IllegalArgumentException}.
     * @param skuInventory
     * @param fulfillmentLocation
     * @throws ConcurrentInventoryModificationException
     */
    public void incrementInventory(Map<Sku, Integer> skuInventory, FulfillmentLocation fulfillmentLocation) throws ConcurrentInventoryModificationException;
    
    /**
     * Attempts to add inventory to an inventory record associated with a default fulfillment location.
     * 
     * @param skuInventory
     * @throws ConcurrentInventoryModificationException
     * @throws IllegalStateException if no default fulfillment location could be found for the Sku.
     */
    public void incrementInventory(Map<Sku, Integer> skuInventory) throws ConcurrentInventoryModificationException;

    /**
     * Retrieves the {@link Inventory} for the given {@link Sku} and {@link FulfillmentLocation}
     * @param sku {@link Sku}
     * @param fulfillmentLocation {@link FulfillmentLocation}
     * @return {@link Inventory}
     */
    public Inventory readInventory(Sku sku, FulfillmentLocation fulfillmentLocation);

    /**
     * Retrieves inventory for this sku at the default fulfillment location.
     *
     * @param sku
     * @return
     */
    public Inventory readInventory(Sku sku);

    /**
     * Retrieves all instances of Inventory for this fulfillmentLocation
     *
     * @param fulfillmentLocation
     * @return list of {@link Inventory}
     */
    public List<Inventory> readInventoryForFulfillmentLocation(FulfillmentLocation fulfillmentLocation);

    /**
     * Persists the inventory
     */
    public Inventory save(Inventory inventory) throws ConcurrentInventoryModificationException;

    /**
     * Retrieves skus that do not have inventory records at a particular fulfillment location
     * @param fulfillmentLocation
     * @return
     */
    public List<Sku> readSkusNotAtFulfillmentLocation(FulfillmentLocation fulfillmentLocation);

}
