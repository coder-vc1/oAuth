package com.cloudEagle.DropboxOAuth.controller;

import com.cloudEagle.DropboxOAuth.service.DropboxApiService;
import com.cloudEagle.DropboxOAuth.security.AuthUtil;
import com.cloudEagle.DropboxOAuth.dto.*;
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
  public ResponseEntity<TeamInfoResponseDto> getTeamInfo(HttpServletRequest request) {
    String accessToken = extractDropboxAccessToken(request);
    logger.info("/team-info called. accessToken: {}", accessToken);
    
    if (accessToken == null) {
      return ResponseEntity.badRequest().body(new TeamInfoResponseDto("Error: Dropbox access token not found", ""));
    }
    
    ResponseEntity<TeamInfoResponseDto> response = dropboxApiService.getTeamInfo(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/account-info")
  public ResponseEntity<AccountInfoResponseDto> getAccountInfo(HttpServletRequest request) {
    String accessToken = extractDropboxAccessToken(request);
    logger.info("/account-info called. accessToken: {}", accessToken);
    
    if (accessToken == null) {
      return ResponseEntity.badRequest().body(new AccountInfoResponseDto());
    }
    
    ResponseEntity<AccountInfoResponseDto> response = dropboxApiService.getAccountInfo(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/account-plan-license")
  public ResponseEntity<AccountPlanLicenseDto> getAccountPlanLicense(HttpServletRequest request) {
    String accessToken = extractDropboxAccessToken(request);
    logger.info("/account-plan-license called. accessToken: {}", accessToken);
    
    if (accessToken == null) {
      return ResponseEntity.badRequest().body(new AccountPlanLicenseDto());
    }
    
    ResponseEntity<AccountPlanLicenseDto> response = dropboxApiService.getAccountPlanLicenseInfo(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/plan-license")
  public ResponseEntity<PlanLicenseDto> getPlanLicense(HttpServletRequest request) {
    String accessToken = extractDropboxAccessToken(request);
    logger.info("/plan-license called. accessToken: {}", accessToken);
    
    if (accessToken == null) {
      return ResponseEntity.badRequest().body(new PlanLicenseDto());
    }
    
    ResponseEntity<PlanLicenseDto> response = dropboxApiService.getPlanLicenseInfo(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/team-members")
  public ResponseEntity<TeamMembersListResponseDto> getTeamMembers(
      HttpServletRequest request,
      Integer limit,
      String cursor) {
    String accessToken = extractDropboxAccessToken(request);
    logger.info("/team-members called. accessToken: {}, limit: {}, cursor: {}", accessToken, limit, cursor);
    
    if (accessToken == null) {
      return ResponseEntity.badRequest().body(new TeamMembersListResponseDto());
    }
    
    // Set default limit if not provided
    if (limit == null || limit <= 0) {
      limit = 100;
    }
    
    ResponseEntity<TeamMembersListResponseDto> response = dropboxApiService.getTeamMembers(accessToken, limit, cursor);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/sign-in-events")
  public ResponseEntity<SignInEventsListResponseDto> getSignInEvents(
      HttpServletRequest request,
      Integer limit,
      String cursor) {
    String accessToken = extractDropboxAccessToken(request);
    logger.info("/sign-in-events called. accessToken: {}, limit: {}, cursor: {}", accessToken, limit, cursor);
    
    if (accessToken == null) {
      return ResponseEntity.badRequest().body(new SignInEventsListResponseDto());
    }
    
    // Set default limit if not provided
    if (limit == null || limit <= 0) {
      limit = 10;
    }
    
    ResponseEntity<SignInEventsListResponseDto> response = dropboxApiService.getSignInEvents(accessToken, limit, cursor);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/refresh-dropbox-token")
  public ResponseEntity<String> refreshDropboxToken(HttpServletRequest request) {
    String refreshToken = extractDropboxRefreshToken(request);
    logger.info("/refresh-dropbox-token called. refreshToken: {}", refreshToken);
    
    if (refreshToken == null) {
      return ResponseEntity.badRequest().body("Dropbox refresh token not found in JWT token");
    }
    
    ResponseEntity<String> response = dropboxApiService.refreshAccessToken(refreshToken);
    logger.info("Dropbox token refresh response: {}", response.getBody());
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

  private String extractDropboxRefreshToken(HttpServletRequest request) {
    String authHeader = request.getHeader("Authorization");
    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      logger.error("Authorization header not found or invalid format");
      return null;
    }

    String jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
    try {
      return authUtil.getDropboxRefreshTokenFromToken(jwtToken);
    } catch (Exception e) {
      logger.error("Error extracting Dropbox refresh token from JWT: {}", e.getMessage());
      return null;
    }
  }
}
