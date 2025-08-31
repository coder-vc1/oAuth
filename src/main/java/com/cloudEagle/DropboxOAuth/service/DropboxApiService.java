package com.cloudEagle.DropboxOAuth.service;

import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class DropboxApiService {
    private final RestTemplate restTemplate = new RestTemplate();

    public ResponseEntity<String> getTeamInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<>(null, headers);

      return restTemplate.exchange(
            "https://api.dropboxapi.com/2/team/get_info",
            HttpMethod.POST,
            entity,
            String.class
        );
    }

    public ResponseEntity<String> getTeamMembers(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
      HttpEntity<String> entity = new HttpEntity<>(null, headers);

      return restTemplate.exchange(
            "https://api.dropboxapi.com/2/team/members/list",
            HttpMethod.POST,
            entity,
            String.class
        );
    }

    public ResponseEntity<String> getSignInEvents(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = "{\"limit\":10,\"event_types\":[\"login\"]}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        return restTemplate.exchange(
            "https://api.dropboxapi.com/2/team_log/get_events",
            HttpMethod.POST,
            entity,
            String.class
        );
    }
}

