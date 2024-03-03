package com.securevault.main.service;

import com.securevault.main.entity.User;
import com.securevault.main.model.AddUserRequest;
import com.securevault.main.model.UserResponse;
import com.securevault.main.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public UserResponse add(AddUserRequest request) {
        User user = this.userRepository.save(new User(request));
        return new UserResponse(user);
    }

}
