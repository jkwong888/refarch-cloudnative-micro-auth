package auth;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller to manage Customer database
 *
 */
@RestController
public class AuthController {
    
    private static Logger logger =  LoggerFactory.getLogger(AuthController.class);
    
    /**
     * check
     */
    @RequestMapping("/check")
    @ResponseBody String check() {
        return "it works!";
    }

    /**
     * Handle auth header
     * @return HTTP 200 if success
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.GET)
    @ResponseBody ResponseEntity<?> getAuthenticate() {
    	logger.debug("GET /authenticate");
    	
    	return ResponseEntity.ok().build();
    }
    
	/**
     * Handle auth header
     * @return HTTP 200 if success
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.POST)
    @ResponseBody ResponseEntity<?> postAuthenticate() {
    	logger.debug("POST /authenticate");
    	
    	return ResponseEntity.ok().build();
    }

}
