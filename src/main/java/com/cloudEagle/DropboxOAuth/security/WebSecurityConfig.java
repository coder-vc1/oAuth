package com.cloudEagle.DropboxOAuth.security;

import static com.cloudEagle.DropboxOAuth.entity.type.PermissionType.USER_MANAGE;
import static com.cloudEagle.DropboxOAuth.entity.type.RoleType.USER;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@RequiredArgsConstructor
@Slf4j
@EnableMethodSecurity
public class WebSecurityConfig {

  private final JwtAuthFilter jwtAuthFilter;
  private final OAuth2SuccessHandler oAuth2SuccessHandler;
  private final HandlerExceptionResolver handlerExceptionResolver;

  @Bean
  public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity,
      OAuth2UserService<OAuth2UserRequest, OAuth2User> dropboxOAuth2UserService) throws Exception {
    httpSecurity
        .csrf(csrfConfig -> csrfConfig.disable())
//        .sessionManagement(sessionConfig ->
//            sessionConfig.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .authorizeHttpRequests(auth -> auth
            .requestMatchers( "/auth/**").permitAll()
            .requestMatchers(HttpMethod.DELETE, "/dropbox/**")
            .hasAnyAuthority(USER_MANAGE.name())
            .requestMatchers("/dropbox/**").hasRole(USER.name())

            .anyRequest().authenticated()
        )
        .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
        .oauth2Login(oAuth2 -> oAuth2
            .userInfoEndpoint(userInfo ->
                userInfo.userService(dropboxOAuth2UserService))
            .failureHandler((request, response, exception) -> {
              log.error("OAuth2 error: {}", exception.getMessage());
              handlerExceptionResolver.resolveException(request, response, null, exception);
            })
            .successHandler(oAuth2SuccessHandler)
        )
        .exceptionHandling(exceptionHandlingConfigurer ->
            exceptionHandlingConfigurer.accessDeniedHandler(
                (request, response, accessDeniedException) -> {
                  handlerExceptionResolver.resolveException(request, response, null,
                      accessDeniedException);
                }));

//                .formLogin();
    return httpSecurity.build();
  }

}
