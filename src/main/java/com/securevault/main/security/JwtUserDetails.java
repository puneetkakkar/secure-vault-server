package com.securevault.main.security;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.securevault.main.entity.User;

import lombok.Data;

@Data
public class JwtUserDetails implements UserDetails {
	private String id;

	private String email;

	private String username;

	private String password;

	private Collection<? extends GrantedAuthority> authorities;

	/**
	 * JwtUserDetails constructor.
	 *
	 * @param id          String
	 * @param email       String
	 * @param password    String
	 * @param authorities Collection<? extends GrantedAuthority>
	 */
	private JwtUserDetails(final String id, final String email, final String password,
			final Collection<? extends GrantedAuthority> authorities) {
		this.id = id;
		this.email = email;
		this.username = email;
		this.password = password;
		this.authorities = authorities;
	}

	/**
	 * Create JwtUserDetails from user
	 * 
	 * @param user
	 * @return
	 */
	public static JwtUserDetails create(final User user) {
		List<GrantedAuthority> authorities = user.getRoles().stream()
				.map(role -> new SimpleGrantedAuthority(role.getName().name()))
				.collect(Collectors.toList());

		return new JwtUserDetails(user.getId().toString(), user.getEmail(), user.getMasterPasswordHash(), authorities);
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return true;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

}
