package org.broadleafcommerce.controller;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.broadleafcommerce.profile.domain.User;
import org.broadleafcommerce.profile.service.EmailService;
import org.broadleafcommerce.profile.service.UserService;
import org.broadleafcommerce.profile.util.PasswordChange;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.SimpleFormController;

public class ForgotPasswordChangeFormController extends SimpleFormController {

    /** Logger for this class and subclasses */
    protected final Log logger = LogFactory.getLog(getClass());
    private static final String TEMPLATE = "forgotPassword.vm";
    private static final String EMAIL_FROM = "BroadleafCommerce@credera.com";
    private static final String EMAIL_SUBJECT = "Email From Broadleaf Commerce Group";
    private UserService userService;
    private EmailService emailService;

    public void setEmailService(EmailService emailService) {
        this.emailService = emailService;
    }

    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    protected Object formBackingObject(HttpServletRequest request) throws ServletException {
        return new PasswordChange();
    }

    @Override
    protected ModelAndView onSubmit(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        PasswordChange pwChange = (PasswordChange) command;
        User user = userService.readUserByEmail(request.getParameter("email"));
        user.setUnencodedPassword(pwChange.getNewPassword());
        userService.saveUser(user);
        ModelAndView mav = new ModelAndView(getSuccessView(), errors.getModel());
        emailService.sendEmail(user, TEMPLATE, EMAIL_FROM, EMAIL_SUBJECT);
        mav.addObject("saved", true);
        return mav;
    }
}
