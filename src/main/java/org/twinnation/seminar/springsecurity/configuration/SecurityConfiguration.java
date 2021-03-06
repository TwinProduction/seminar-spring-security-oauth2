package org.twinnation.seminar.springsecurity.configuration;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.twinnation.seminar.springsecurity.bean.User;
import org.twinnation.seminar.springsecurity.service.UserService;
import org.twinnation.seminar.springsecurity.util.Utils;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@EnableOAuth2Client
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
	
	@Autowired
	private UserService userService;
	
	@Override
	protected void configure(HttpSecurity http) throws Exception {
		http.httpBasic()
			.and()
			.csrf().disable()
			.cors()
			.and()
			.headers()
				.frameOptions().sameOrigin()
			.and()
			.formLogin()
				.loginPage("/login")
				.loginProcessingUrl("/api/login")
				.failureHandler(authenticationFailureHandler())
				.successHandler(authenticationSuccessHandler())
			.and()
			.oauth2Login()
				.loginPage("/login")
				.userInfoEndpoint()
					.userService(userService)
			.and().and()
			.logout()
				.logoutSuccessHandler(logoutSuccessHandler())
				.logoutUrl("/api/logout").clearAuthentication(true).deleteCookies("JSESSIONID").invalidateHttpSession(true).permitAll()
			.and()
			.authorizeRequests()
				.anyRequest().permitAll();
	}
	
	
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
	
	
	@Bean
	public AuthenticationFailureHandler authenticationFailureHandler() {
		return new SimpleUrlAuthenticationFailureHandler() {
			@Override
			public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception) throws IOException {
				PrintWriter writer = response.getWriter();
				response.setContentType("application/json");
				response.setStatus(HttpStatus.UNAUTHORIZED.value());
				writer.write(Utils.jsonReply("ERROR", true, "MESSAGE", "Invalid username or password"));
			}
		};
	}
	
	
	@Bean
	public LogoutSuccessHandler logoutSuccessHandler() {
		return (request, response, auth) -> {
			PrintWriter writer = response.getWriter();
			response.setStatus(HttpStatus.ACCEPTED.value());
			response.setContentType("application/json");
			writer.write(Utils.jsonReply("ERROR", false, "MESSAGE", "You have been logged out"));
		};
	}
	
	
	@Bean
	public AuthenticationSuccessHandler authenticationSuccessHandler() {
		return new SimpleUrlAuthenticationSuccessHandler() {
			@Override
			public void  onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {
				PrintWriter writer = response.getWriter();
				response.setContentType("application/json");
				response.setStatus(HttpStatus.ACCEPTED.value());
				writer.write(Utils.jsonReply("ERROR", false, "USER", new User(authentication)));
			}
		};
	}

}
