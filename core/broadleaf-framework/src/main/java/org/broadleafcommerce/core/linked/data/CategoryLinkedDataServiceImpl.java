package org.broadleafcommerce.core.linked.data;

import org.broadleafcommerce.core.catalog.domain.Product;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Created by jacobmitash on 6/28/17.
 */
@Service("blCategoryLinkedDataService")
public class CategoryLinkedDataServiceImpl extends AbstractLinkedDataService implements CategoryLinkedDataService {

    @Override
    public String getLinkedData(List<Product> products, String url) throws JSONException {
        JSONArray schemaObjects = new JSONArray();

        JSONObject categoryData = new JSONObject();
        categoryData.put("@context", "http://schema.org");
        categoryData.put("@type", "ItemList");
        JSONArray itemList = new JSONArray();
        for(int i = 0; i < products.size(); i++) {
            JSONObject item = new JSONObject();
            item.put("@type", "ListItem");
            item.put("position", i + 1);
            item.put("url", products.get(i).getUrl());
            itemList.put(item);
        }
        categoryData.put("itemListElement", itemList);

        schemaObjects.put(categoryData);
        schemaObjects.put(getDefaultBreadcrumbList());
        schemaObjects.put(getDefaultOrganization(url));
        schemaObjects.put(getDefaultWebSite(url));

        return schemaObjects.toString();
    }
}
