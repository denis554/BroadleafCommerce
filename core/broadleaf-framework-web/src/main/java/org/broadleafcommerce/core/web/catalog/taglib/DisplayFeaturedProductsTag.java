/*
 * Copyright 2008-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.broadleafcommerce.core.web.catalog.taglib;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.broadleafcommerce.core.catalog.domain.Product;

public class DisplayFeaturedProductsTag extends TagSupport {

    private static final long serialVersionUID = 1L;

    private List<Product> products;
	private String maxFeatures;
	private String var;
	 
	@Override
	public int doStartTag() throws JspException {
		List<Product> featuredProducts = getFeaturedProducts(products);
        if (maxFeatures != null && !"".equals(maxFeatures)) {
        	featuredProducts = featuredProducts.subList(0, (getMaxFeatureCount(maxFeatures, featuredProducts.size())));
        }
		super.pageContext.setAttribute(var, featuredProducts);
        
		return EVAL_BODY_INCLUDE;
    }
    
    private List<Product> getFeaturedProducts(List<Product> products) {
    	List<Product> featuredProducts = new ArrayList<Product>();
    	if (products != null) {
	    	Iterator<Product> i = products.iterator();
	    	
	    	while (i.hasNext()) {
	    		Product p = i.next();
	    		if (p.isFeaturedProduct()) {
	    			featuredProducts.add(p);
	    		}
	    	}
    	}    	
    	return featuredProducts;
    }
    
    private int getMaxFeatureCount(String count, int max) {
    	if ((count != null) || (!"".equals(count))) {
	    	try {
	    		if (Integer.parseInt(count) < max) {
	    			return Integer.parseInt(count);
	    		}
	    	}
	    	catch(Exception e) {}
    	}
    	
    	return max;
    }
    
	public String getMaxFeatures() {
		return maxFeatures;
	}

	public void setMaxFeatures(String maxFeatures) {
		this.maxFeatures = maxFeatures;
	}

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }
	public String getVar() {
		return var;
	}

	public void setVar(String var) {
		this.var = var;
	}    
}
