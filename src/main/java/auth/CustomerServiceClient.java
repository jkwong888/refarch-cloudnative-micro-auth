package auth;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient("customer-microservice")
public interface CustomerServiceClient {
	@RequestMapping(method=RequestMethod.GET, value="/customer/search")
	List<Customer> getCustomerByUsername(@RequestParam(required=true) String username);
}
