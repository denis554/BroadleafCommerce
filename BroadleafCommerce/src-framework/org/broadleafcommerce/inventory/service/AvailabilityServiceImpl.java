/*
 * Copyright 2008-2009 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.broadleafcommerce.inventory.service;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.broadleafcommerce.inventory.dao.AvailabilityDao;
import org.broadleafcommerce.inventory.domain.SkuAvailability;
import org.springframework.stereotype.Service;

@Service("availabilityService")
public class AvailabilityServiceImpl implements AvailabilityService {

    @Resource
    private AvailabilityDao availabilityDao;

	/**
	 * Returns the availability status for this passed in skuId.   Implementations may choose
	 * to cache the status based upon the passed in realTime indicator.
	 *
	 * @param skuId
	 * @param realTime
	 * @return String indicating the availabilityStatus (statuses are implementation specific)
	 */
	public SkuAvailability lookupSKUAvailability(Long skuId, boolean realTime) {
		List<Long> skuIds = new ArrayList<Long>();
		skuIds.add(skuId);
		List<SkuAvailability> skuAvailbilityList =  availabilityDao.readSKUAvailability(skuIds, realTime);
		if (skuAvailbilityList != null && skuAvailbilityList.size() >=1) {
			return skuAvailbilityList.get(0);
		}
		return null;
	}

	/**
	 * Returns the availability status for a specific skuId and location.   Implementations may choose
	 * to cache the status based upon the passed in realTime indicator.
	 *
	 * @param skuId
	 * @param locationId
	 * @param realTime
	 * @return String indicating the availabilityStatus (statuses are implementation specific)
	 */
	public SkuAvailability lookupSKUAvailabilityForLocation(Long skuId, Long locationId, boolean realTime) {
		List<Long> skuIds = new ArrayList<Long>();
		skuIds.add(skuId);
		List<SkuAvailability> skuAvailbilityList =  availabilityDao.readSKUAvailabilityForLocation(skuIds, locationId, realTime);
		if (skuAvailbilityList != null && skuAvailbilityList.size() >=1) {
			return skuAvailbilityList.get(0);
		}
		return null;
	}

	/**
	 * Returns the availability status for this passed in skuId.   Implementations may choose
	 * to cache the status based upon the passed in realTime indicator.
	 *
	 * @param skuId
	 * @param realTime
	 * @return String indicating the availabilityStatus (statuses are implementation specific)
	 */
	public List<SkuAvailability> lookupSKUAvailability(List<Long> skuIds, boolean realTime) {
		return availabilityDao.readSKUAvailability(skuIds, realTime);
	}

	/**
	 * Returns the availability status for a specific skuId and location.   Implementations may choose
	 * to cache the status based upon the passed in realTime indicator.
	 *
	 * @param skuId
	 * @param locationId
	 * @param realTime
	 * @return String indicating the availabilityStatus (statuses are implementation specific)
	 */
	public List<SkuAvailability> lookupSKUAvailabilityForLocation(List<Long> skuIds, Long locationId, boolean realTime) {
		return availabilityDao.readSKUAvailabilityForLocation(skuIds, locationId, realTime);
	}
}
