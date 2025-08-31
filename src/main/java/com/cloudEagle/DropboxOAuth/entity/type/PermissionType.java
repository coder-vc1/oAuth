package com.cloudEagle.DropboxOAuth.entity.type;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionType {

  USER_MANAGE("user:manage");

  private final String permission;
}
