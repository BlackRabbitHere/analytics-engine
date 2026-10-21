package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.UserRequestDTO;
import com.analytics.analytics_engine.dto.UserResponseDTO;
import com.analytics.analytics_engine.exception.UsernameAlreadyTakenException;
import com.analytics.analytics_engine.model.User;
import com.analytics.analytics_engine.repository.UserRepository;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
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
            throw new UsernameAlreadyTakenException();
        }

        User savedUser;
        try {
            // Flush here so a concurrent duplicate insert is translated before returning.
            savedUser = userRepository.saveAndFlush(new User(userRequestDTO.getUsername()));
        } catch (DataIntegrityViolationException exception) {
            if (isUsernameConstraintViolation(exception)) {
                throw new UsernameAlreadyTakenException(exception);
            }
            throw exception;
        }
        return UserResponseDTO.toResponse(savedUser);
    }

    private boolean isUsernameConstraintViolation(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                String constraint = violation.getConstraintName();
                if (constraint != null && (constraint.equalsIgnoreCase("uc_users_username")
                        || constraint.toLowerCase(java.util.Locale.ROOT).endsWith(".uc_users_username"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
