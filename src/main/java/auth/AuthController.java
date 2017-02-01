package auth;

import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
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
     * @return customer by username
     */
    @RequestMapping(value = "/authenticate", method = RequestMethod.GET)
    @ResponseBody ResponseEntity<?> authenticate(@RequestHeader(value="Authorization") String authHeader) {
    	final String creds = new String(Base64.getDecoder().decode(authHeader));
    	final String[] split = creds.split(":");
    	logger.debug("Authenticating: user=" + split[0] + ", password=" + split[1]);
       
    	// TODO: set signed JWT before calling the customer service
    	// TODO: call customer service
    	
    	
    	return ResponseEntity.ok().build();
    }
    
}
