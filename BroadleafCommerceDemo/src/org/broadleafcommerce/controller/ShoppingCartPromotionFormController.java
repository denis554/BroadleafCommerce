package org.broadleafcommerce.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.rules.domain.ShoppingCartPromotion;
import org.broadleafcommerce.rules.service.RuleService;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ShoppingCartPromotionFormController extends SimpleFormController {

	protected final Log logger = LogFactory.getLog(getClass());

	private RuleService ruleService;

	public void setRuleService(RuleService ruleService) {
		this.ruleService = ruleService;
	}

	protected Object formBackingObject(HttpServletRequest request)
			throws ServletException {
		ShoppingCartPromotion shoppingCartPromotion = new ShoppingCartPromotion();

		if (request.getParameter("promotionRuleId") != null) {
			shoppingCartPromotion = ruleService.readShoppingCartPromotionById(Long
					.valueOf(request.getParameter("promotionRuleId")));
		}

		return shoppingCartPromotion;
	}

	@Override
	protected ModelAndView onSubmit(HttpServletRequest request,
			HttpServletResponse response, Object command, BindException errors)
			throws Exception {

		ShoppingCartPromotion shoppingCartPromotion = (ShoppingCartPromotion) command;

		ruleService.saveShoppingCartPromotion(shoppingCartPromotion);
		ruleService.writeRuleFile(shoppingCartPromotion);


		if (errors.hasErrors()) {
			logger.debug("Error returning back to the form");

			return showForm(request, response, errors);
		}

		ModelAndView mav = new ModelAndView(getSuccessView(), errors.getModel());
		mav.addObject("saved", true);

		return mav;
	}
}
