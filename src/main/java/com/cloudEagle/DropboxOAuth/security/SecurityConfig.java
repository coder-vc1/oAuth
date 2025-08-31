package com.cloudEagle.DropboxOAuth.security;

import java.util.Collections;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.client.RestTemplate;

@Configuration
public class SecurityConfig {

  @Bean
  public OAuth2UserService<OAuth2UserRequest, OAuth2User> dropboxOAuth2UserService() {
    return userRequest -> {
      String accessToken = userRequest.getAccessToken().getTokenValue();

      RestTemplate restTemplate = new RestTemplate();

      HttpHeaders headers = new HttpHeaders();
      headers.setBearerAuth(accessToken);
      headers.setContentType(MediaType.APPLICATION_JSON);

      HttpEntity<String> entity = new HttpEntity<>("null", headers);

      ResponseEntity<Map> response = restTemplate.exchange(
          "https://api.dropboxapi.com/2/team/get_info",
          HttpMethod.POST,
          entity,
          Map.class
      );

      Map<String, Object> userAttributes = response.getBody();
      return new DefaultOAuth2User(
          Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")),
          userAttributes,
          "team_id"
      );
    };
  }


}
