package auth;

import java.util.Base64;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.netflix.discovery.DiscoveryClient;
import com.netflix.discovery.shared.Application;

/**
 * REST Controller to manage Customer database
 *
 */
@RestController
public class AuthController {
    
    private static Logger logger =  LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private CustomerServiceClient customerService;
   
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
    	final List<Customer> custList = customerService.getCustomerByUsername(split[0]);
    	
    	if (custList.isEmpty()) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	
    	final Customer cust = custList.get(0);
    	// TODO: hash password
    	if (!cust.getPassword().equals(split[1])) {
    		return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    	}
    	
    	return ResponseEntity.ok().build();
    }
    
}
