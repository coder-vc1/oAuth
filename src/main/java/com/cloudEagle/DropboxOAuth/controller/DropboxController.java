package com.cloudEagle.DropboxOAuth.controller;

import com.cloudEagle.DropboxOAuth.service.DropboxApiService;
import com.cloudEagle.DropboxOAuth.security.AuthUtil;
import lombok.RequiredArgsConstructor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@RestController
@RequestMapping("/dropbox")
@RequiredArgsConstructor
public class DropboxController {

  private static final Logger logger = LoggerFactory.getLogger(DropboxController.class);

  @Autowired
  private DropboxApiService dropboxApiService;

  @Autowired
  private AuthUtil authUtil;

  @GetMapping("/team-info")
  public ResponseEntity<String> getTeamInfo(HttpServletRequest request) {
    String accessToken = extractDropboxAccessToken(request);
    logger.info("/team-info called. accessToken: {}", accessToken);
    
    if (accessToken == null) {
      return ResponseEntity.badRequest().body("Dropbox access token not found in JWT token");
    }
    
    ResponseEntity<String> response = dropboxApiService.getTeamInfo(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/team-members")
  public ResponseEntity<String> getTeamMembers(HttpServletRequest request) {
    String accessToken = extractDropboxAccessToken(request);
    logger.info("/team-members called. accessToken: {}", accessToken);
    
    if (accessToken == null) {
      return ResponseEntity.badRequest().body("Dropbox access token not found in JWT token");
    }
    
    ResponseEntity<String> response = dropboxApiService.getTeamMembers(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/sign-in-events")
  public ResponseEntity<String> getSignInEvents(HttpServletRequest request) {
    String accessToken = extractDropboxAccessToken(request);
    logger.info("/sign-in-events called. accessToken: {}", accessToken);
    
    if (accessToken == null) {
      return ResponseEntity.badRequest().body("Dropbox access token not found in JWT token");
    }
    
    ResponseEntity<String> response = dropboxApiService.getSignInEvents(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  private String extractDropboxAccessToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      logger.error("Authorization header not found or invalid format");
      return null;
    }

    String jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
    try {
      return authUtil.getDropboxAccessTokenFromToken(jwtToken);
    } catch (Exception e) {
      logger.error("Error extracting Dropbox access token from JWT: {}", e.getMessage());
      return null;
    }
  }
}
