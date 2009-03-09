package org.broadleafcommerce.layout.tags;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.BodyTagSupport;

import org.broadleafcommerce.catalog.service.CatalogService;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ProductLookupTag extends BodyTagSupport {
    // private Logger log = Logger.getLogger(this.getClass());
    private static final long serialVersionUID = 1L;
    private String var;
    private long productId;

    public long getProductId() {
		return productId;
	}

	public void setProductId(long productId) {
		this.productId = productId;
	}

	@Override
    public int doStartTag() throws JspException {
        WebApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(pageContext.getServletContext());
        CatalogService catalogService = (CatalogService) applicationContext.getBean("catalogService");
        pageContext.setAttribute(var, catalogService.findProductById(productId));
        return EVAL_PAGE;
    }

    public String getVar() {
        return var;
    }

    public void setVar(String var) {
        this.var = var;
    }
}
