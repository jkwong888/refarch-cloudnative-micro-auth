package auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Controller for Login Form
 *
 */
@Controller
public class LoginController {
    
    private static Logger logger =  LoggerFactory.getLogger(LoginController.class);
    
    @RequestMapping("/login")
    public String login() {
    	logger.debug("/login");
    	return "login";
    }
    
}
