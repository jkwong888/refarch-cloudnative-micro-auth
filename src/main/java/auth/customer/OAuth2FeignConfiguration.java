package auth.customer;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Base64;
import java.util.Calendar;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import auth.JwtConfig;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

/**
 * Interceptor that generates a signed admin JWT to call 
 * the search API on the customer API
 * @author jkwong
 *
 */
@Configuration
public class OAuth2FeignConfiguration {
	private static final String AUTHORIZATION_HEADER = "Authorization";
	private static final String BEARER_TOKEN_TYPE = "Bearer";
	private static final Logger logger = LoggerFactory.getLogger(OAuth2FeignRequestInterceptor.class);

	private static String JWT_ISS = "auth-service";
	private static String JWT_ADMIN_SCOPE_NAME = "admin";
	private static String JWT_BLUE_SCOPE_NAME = "blue";

	@Bean
	public RequestInterceptor oauth2FeignRequestInterceptor() {
		return new OAuth2FeignRequestInterceptor();
	}

	private static class OAuth2FeignRequestInterceptor implements RequestInterceptor {
		@Autowired
		private JwtConfig jwtConfig;

		/**
		 * Generate an Admin JWT Token to query the Customer Microservice
		 * 
		 * @return
		 */
		private String generateJwtToken() {
			// expire in 1 hour
			final Calendar now = Calendar.getInstance();
			final Calendar exp = Calendar.getInstance();
			exp.add(Calendar.HOUR, 1);

			try {
				return Jwts.builder().setExpiration(exp.getTime()).setIssuedAt(now.getTime()).setIssuer(JWT_ISS)
						.claim("user_name", "admin")
						.claim("scope", Arrays.asList(JWT_BLUE_SCOPE_NAME, JWT_ADMIN_SCOPE_NAME))
						.signWith(SignatureAlgorithm.HS256,
								Base64.getEncoder().encodeToString(jwtConfig.getSharedSecret().getBytes("UTF-8")))
						.compact();
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage());
				return null;
			}
		}

		@Override
		public void apply(RequestTemplate template) {
			if (template.headers().containsKey(AUTHORIZATION_HEADER)) {
				logger.warn("The Authorization token has been already set");
			} else {
				logger.debug("Constructing Header {} for Token {}", AUTHORIZATION_HEADER, BEARER_TOKEN_TYPE);
				template.header(AUTHORIZATION_HEADER, String.format("%s %s", BEARER_TOKEN_TYPE, generateJwtToken()));
			}
		}
	}
}