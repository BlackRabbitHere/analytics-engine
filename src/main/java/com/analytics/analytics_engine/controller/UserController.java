package com.analytics.analytics_engine.controller;


import com.analytics.analytics_engine.dto.UserRequestDTO;
import com.analytics.analytics_engine.dto.UserResponseDTO;
import com.analytics.analytics_engine.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {


    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }



    @PostMapping("/create-user")
    public ResponseEntity<UserResponseDTO> createUser(@Valid @RequestBody UserRequestDTO userRequestDTO){


        return new ResponseEntity<>(userService.createUser(userRequestDTO), HttpStatus.CREATED);

    }


}
