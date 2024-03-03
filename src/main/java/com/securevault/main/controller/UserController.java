package com.securevault.main.controller;

import com.securevault.main.model.AddUserRequest;
import com.securevault.main.model.UserResponse;
import com.securevault.main.service.UserService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }
    @PostMapping(path = "/add")
    public UserResponse addUser(@RequestBody AddUserRequest request){
        return this.userService.add(request);
    }
}
