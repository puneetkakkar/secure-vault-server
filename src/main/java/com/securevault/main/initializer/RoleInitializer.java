package com.securevault.main.initializer;

import com.securevault.main.entity.Role;
import com.securevault.main.repository.RoleRepository;
import com.securevault.main.util.Constants;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class RoleInitializer implements ApplicationRunner {

    private final RoleRepository roleRepository;

    public RoleInitializer(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        createDefaultRoleIfNotExist(Constants.RoleEnum.ADMIN);
        createDefaultRoleIfNotExist(Constants.RoleEnum.USER);
    }

    private void createDefaultRoleIfNotExist(Constants.RoleEnum roleName) {
        Optional<Role> existingRole = roleRepository.findByName(roleName);
        if (existingRole.isEmpty()) {
            Role newRole = new Role(roleName);
            roleRepository.save(newRole);
        }
    }
}
