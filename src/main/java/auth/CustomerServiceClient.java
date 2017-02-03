package auth;

import java.util.List;

import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;


@FeignClient("customer-microservice")
public interface CustomerServiceClient {
	@RequestMapping(method=RequestMethod.GET, value="/micro/customer/search", produces={MediaType.APPLICATION_JSON_VALUE})
	ResponseEntity<List<Customer>> getCustomerByUsername(@RequestParam(value="username", required=true) String username);
}
