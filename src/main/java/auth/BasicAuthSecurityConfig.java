package auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class BasicAuthSecurityConfig extends WebSecurityConfigurerAdapter {
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//TODO
		//http.requiresChannel().anyRequest().requiresSecure();
		
		http.requestMatchers().antMatchers("/authenticate")
			.and()
			.httpBasic()
			.and()
			.authorizeRequests().antMatchers("/authenticate").authenticated();
		
		http.csrf().disable();
	}
	
	@Override
	@Bean
	public AuthenticationManager authenticationManagerBean() throws Exception {
		return super.authenticationManagerBean();
	}
	
	@Autowired
	private CustomerAuthenticationProvider customerAuthenticationProvider;
 
	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
    	// use the CustomerService as a user directory backend
		auth.authenticationProvider(customerAuthenticationProvider);
	}
	
}
