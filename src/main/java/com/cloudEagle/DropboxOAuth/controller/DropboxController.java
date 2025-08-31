package com.cloudEagle.DropboxOAuth.controller;

import com.cloudEagle.DropboxOAuth.service.DropboxApiService;
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
  private HttpServletRequest request;

  @GetMapping("/team-info")
  public ResponseEntity<String> getTeamInfo() {
    String accessToken = (String) request.getSession().getAttribute("dropboxAccessToken");
    logger.info("/team-info called. accessToken: {}", accessToken);
    ResponseEntity<String> response = dropboxApiService.getTeamInfo(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/team-members")
  public ResponseEntity<String> getTeamMembers() {
    String accessToken = (String) request.getSession().getAttribute("dropboxAccessToken");
    logger.info("/team-members called. accessToken: {}", accessToken);
    ResponseEntity<String> response = dropboxApiService.getTeamMembers(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }

  @GetMapping("/sign-in-events")
  public ResponseEntity<String> getSignInEvents() {
    String accessToken = (String) request.getSession().getAttribute("dropboxAccessToken");
    logger.info("/sign-in-events called. accessToken: {}", accessToken);
    ResponseEntity<String> response = dropboxApiService.getSignInEvents(accessToken);
    logger.info("Dropbox API response: {}", response.getBody());
    return response;
  }
}
