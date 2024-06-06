package com.securevault.main.security;

import java.util.Collection;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JwtUserDetails implements UserDetails {
	private String id;
	private String email;
	private String username;
	private String password;
	private Collection<? extends GrantedAuthority> authorities;

	private JwtUserDetails(final String id, final String email, final String password,
			final Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.email = email;
		this.password = password;
		this.authorities = authorities;
	}

}
