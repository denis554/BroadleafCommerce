package org.springcommerce.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springcommerce.catalog.domain.Category;
import org.springcommerce.catalog.service.CatalogService;
import org.springcommerce.util.CreateCategory;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class CategoryFormController extends SimpleFormController {
    protected final Log logger = LogFactory.getLog(getClass());
    private CatalogService catalogService;

    public void setCatalogService(CatalogService catalogService) {
        this.catalogService = catalogService;
    }

    protected Object formBackingObject(HttpServletRequest request)
                                throws ServletException {
        return new CreateCategory();
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors)
                             throws Exception {
    	CreateCategory createCategory = (CreateCategory) command;
    	Category category = new Category();
    	category.setName(createCategory.getName());

    	if (createCategory.getParentId()!= null){
    		Category parentCategory = catalogService.readCategoryById(new Long(createCategory.getParentId()));
    		if (parentCategory != null){
    			category.setParentCategory(parentCategory);
    		}
    	}

        ModelAndView mav = new ModelAndView(getSuccessView(), errors.getModel());

        if (errors.hasErrors()) {
            logger.debug("Error returning back to the form");

            return showForm(request, response, errors);
        }

        catalogService.saveCategory(category);
        mav.addObject("saved", true);

        return mav;
    }
}
