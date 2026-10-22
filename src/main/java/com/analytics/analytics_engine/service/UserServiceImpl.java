package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.UserRequestDTO;
import com.analytics.analytics_engine.dto.UserResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.model.User;
import com.analytics.analytics_engine.repository.UserRepository;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService{
    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        if(userRepository.getUserByUsername(userRequestDTO.getUsername()).isPresent()){
            throw new APIException("Username already taken");
        }

        User savedUser = new User(userRequestDTO.getUsername());
        userRepository.save(savedUser);

        return UserResponseDTO.toResponse(savedUser);
    }
}
