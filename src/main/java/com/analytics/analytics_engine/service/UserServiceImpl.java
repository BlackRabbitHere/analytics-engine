package com.analytics.analytics_engine.service;


import com.analytics.analytics_engine.dto.UserRequestDTO;
import com.analytics.analytics_engine.dto.UserResponseDTO;
import com.analytics.analytics_engine.exception.APIException;
import com.analytics.analytics_engine.exception.ErrorCode;
import com.analytics.analytics_engine.model.User;
import com.analytics.analytics_engine.repository.UserRepository;
import jakarta.transaction.Transactional;
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
    @Transactional
    public UserResponseDTO createUser(UserRequestDTO userRequestDTO) {
        String username=userRequestDTO.getUsername();
        if(userRepository.getUserByUsername(username).isPresent()){
            throw new APIException(ErrorCode.USERNAME_ALREADY_EXISTS);
        }

        User savedUser;

        try {
            savedUser= userRepository.saveAndFlush(new User(username));
        }catch (DataIntegrityViolationException ex){
            if(isUsernameConflict(ex)){
                throw new APIException(ErrorCode.USERNAME_ALREADY_EXISTS,ex);
            }

            throw ex;
        }

        return UserResponseDTO.toResponse(savedUser);
    }

    private boolean isUsernameConflict(Throwable exception) {
        Throwable current = exception;

        while (current != null) {
            if (current instanceof ConstraintViolationException violation) {
                String constraintName = violation.getConstraintName();

                if (constraintName != null) {
                    String normalizedName = constraintName
                            .replace("`", "")
                            .replace("\"", "");

                    // MySQL may report "users.uc_users_username".
                    String simpleName = normalizedName.substring(
                            normalizedName.lastIndexOf('.') + 1
                    );

                    if ("uc_users_username".equalsIgnoreCase(simpleName)) {
                        return true;
                    }
                }
            }

            current = current.getCause();
        }

        return false;
    }
}
