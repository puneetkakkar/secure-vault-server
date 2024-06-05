package com.securevault.main.service;

import org.springframework.stereotype.Service;

import com.securevault.main.entity.Role;
import com.securevault.main.exception.NotFoundException;
import com.securevault.main.repository.RoleRepository;
import com.securevault.main.util.Constants;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class RoleService {
	private final RoleRepository roleRepository;
	private final MessageSourceService messageSourceService;

	/**
	 * Find by role name.
	 * 
	 * @param name
	 * @return
	 */
	public Role findByName(final Constants.RoleEnum name) {
		return roleRepository.findByName(name)
				.orElseThrow(() -> new NotFoundException("Role Not Found"));
	}

	/**
	 * Create role.
	 * 
	 * @param role
	 * @return
	 */
	public Role create(final Role role) {
		return roleRepository.save(role);
	}

}
