package com.cloudEagle.DropboxOAuth.security;
import static com.cloudEagle.DropboxOAuth.entity.type.PermissionType.USER_MANAGE;
import static com.cloudEagle.DropboxOAuth.entity.type.RoleType.USER;

import com.cloudEagle.DropboxOAuth.entity.type.PermissionType;
import com.cloudEagle.DropboxOAuth.entity.type.RoleType;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class RolePermissionMapping {

  private static final Map<RoleType, Set<PermissionType>> map = Map.of(USER, Set.of(USER_MANAGE));

  public static Set<SimpleGrantedAuthority> getAuthoritiesForRole(RoleType role) {
    return map.get(role).stream()
        .map(permission -> new SimpleGrantedAuthority(permission.getPermission()))
        .collect(Collectors.toSet());
  }
}
