package auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;

@Configuration
@EnableWebSecurity
@Order(1) // HIGHEST
//@EnableGlobalMethodSecurity(prePostEnabled = true)
public class OAuth2SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
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
	  
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		//TODO
		//http.requiresChannel().anyRequest().requiresSecure();
		http
			.formLogin()
				.loginPage("/login").permitAll()
				.usernameParameter("username")
				.passwordParameter("password")
		.and()
			.requestMatchers().antMatchers("/login", "/healthz", "/oauth/authorize", "/oauth/token", "/oauth/confirm_access")
		.and()
			.authorizeRequests()
				.antMatchers("/healthz").permitAll()
				.antMatchers("/login").permitAll()
				.antMatchers("/oauth/token").permitAll()
				.antMatchers(HttpMethod.OPTIONS).permitAll()
				.anyRequest().fullyAuthenticated()
		.and()
			.httpBasic()
		.and()
			.exceptionHandling()
				.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/login")).and().logout()
				.logoutSuccessUrl("/login").permitAll();
		
		http.csrf().disable();
	}
	

}
