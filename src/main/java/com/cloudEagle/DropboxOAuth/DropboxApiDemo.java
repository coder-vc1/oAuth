package com.cloudEagle.DropboxOAuth;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.stereotype.Component;

/**
 * Simple Java program to demonstrate Dropbox API authentication and data fetching
 * This program shows how to authenticate with Dropbox OAuth2 and fetch team information
 */
@Component
public class DropboxApiDemo {

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Main method to demonstrate the API usage
     */
    public static void main(String[] args) {
        DropboxApiDemo demo = new DropboxApiDemo();
        
        // Note: In a real application, you would get this token from OAuth2 flow
        String accessToken = "YOUR_DROPBOX_ACCESS_TOKEN_HERE";
        
        if ("YOUR_DROPBOX_ACCESS_TOKEN_HERE".equals(accessToken)) {
            System.out.println("Please replace 'YOUR_DROPBOX_ACCESS_TOKEN_HERE' with your actual Dropbox access token");
            System.out.println("You can get this token by completing the OAuth2 flow in the Spring Boot application");
            return;
        }
        
        try {
            // Fetch team information
            String teamInfo = demo.getTeamInfo(accessToken);
            System.out.println("=== Team Information ===");
            System.out.println(teamInfo);
            
            // Fetch account information
            String accountInfo = demo.getAccountInfo(accessToken);
            System.out.println("\n=== Account Information ===");
            System.out.println(accountInfo);
            
        } catch (Exception e) {
            System.err.println("Error fetching data from Dropbox API: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Fetch team information from Dropbox API
     */
    public String getTeamInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "https://api.dropboxapi.com/2/team/get_info",
            HttpMethod.POST,
            entity,
            String.class
        );
        
        return response.getBody();
    }

    /**
     * Fetch current account information from Dropbox API
     */
    public String getAccountInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        HttpEntity<String> entity = new HttpEntity<>(null, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "https://api.dropboxapi.com/2/users/get_current_account",
            HttpMethod.POST,
            entity,
            String.class
        );
        
        return response.getBody();
    }

    /**
     * Fetch team members from Dropbox API
     */
    public String getTeamMembers(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String body = "{\"limit\":100,\"include_removed\":false}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "https://api.dropboxapi.com/2/team/members/list",
            HttpMethod.POST,
            entity,
            String.class
        );
        
        return response.getBody();
    }

    /**
     * Fetch sign-in events from Dropbox API
     */
    public String getSignInEvents(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        String body = "{\"limit\":10,\"event_type\":[\"login\"]}";
        HttpEntity<String> entity = new HttpEntity<>(body, headers);
        
        ResponseEntity<String> response = restTemplate.exchange(
            "https://api.dropboxapi.com/2/team_log/get_events",
            HttpMethod.POST,
            entity,
            String.class
        );
        
        return response.getBody();
    }
}
