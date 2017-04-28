package auth;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.TokenStore;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.JwtTokenStore;

@Configuration
@EnableAuthorizationServer
public class AuthorizationServerConfig extends AuthorizationServerConfigurerAdapter {

	    @Value("${resource.id:spring-boot-application}")
		private String resourceId;
		
		@Value("${access_token.validity_period:3600}")
		int accessTokenValiditySeconds = 3600;
		
        @Autowired
        @Qualifier("authenticationManagerBean")
        private AuthenticationManager authenticationManager;
        
        @Autowired
        private JwtConfig securityConfig;
        
        protected static class CustomTokenEnhancer implements TokenEnhancer {
        	@Override
        	public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
        		final Map<String, Object> additionalInfo = new HashMap<>();
        		// TODO: add additional claims to the token
        		//additionalInfo.put("additionClaims", value);
        		
        		((DefaultOAuth2AccessToken)accessToken).setAdditionalInformation(additionalInfo);
        		return accessToken;
        	}
        }

        @Bean
        public JwtAccessTokenConverter jwtTokenEnhancer() {
            final JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
           
            /* for HS256, set the signing key */
            converter.setSigningKey(securityConfig.getSharedSecret());
            
            /* for RS256, use a KeyPair
            final KeyStoreKeyFactory keyStoreKeyFactory = new KeyStoreKeyFactory(
                    new ClassPathResource("keystore.jks"), "foobar".toCharArray());
            converter.setKeyPair(keyStoreKeyFactory.getKeyPair("test"));
            */
            return converter;
        }
        
        @Bean
        @Qualifier("tokenStore")
        public TokenStore tokenStore() {
            return new JwtTokenStore(jwtTokenEnhancer());
        }
    
        @Override
        public void configure(ClientDetailsServiceConfigurer clients) throws Exception {
            clients.inMemory()
				// web bff -- password grant type
				.withClient("bluecomputeweb") 
                    .secret("bluecomputewebs3cret")
                    .authorizedGrantTypes(
                            "refresh_token",
                            "password")
                    .scopes("blue").and()
				// mobile bff -- implicit grant type
				.withClient("bluecomputemobile")
                    .secret("bluecomputemobiles3cret")
                    .authorizedGrantTypes(
                    		"implicit",
                            "refresh_token")
                    .scopes("blue");
        }

        @Override
        public void configure(AuthorizationServerEndpointsConfigurer endpoints)
                throws Exception {
        	final TokenEnhancerChain tokenEnhancerChain = new TokenEnhancerChain();
        	tokenEnhancerChain.setTokenEnhancers(Arrays.asList(new CustomTokenEnhancer(), jwtTokenEnhancer()));
        	
            endpoints.tokenStore(tokenStore())
                     .tokenEnhancer(tokenEnhancerChain)
                     .authenticationManager(authenticationManager);
        }

        @Override
        public void configure(AuthorizationServerSecurityConfigurer oauthServer)
                throws Exception {
            oauthServer.tokenKeyAccess("permitAll()")
                       .checkTokenAccess("isAuthenticated()");
        }

}
