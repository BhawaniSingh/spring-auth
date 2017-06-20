package com.neladyn.service;

import com.neladyn.domain.Role;
import com.neladyn.domain.User;
import com.neladyn.domain.UserDetails;
import com.neladyn.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;


    public User getUser(String email) {
        return userRepository.findOne(email);
    }

    public User createUser(UserDetails userDetails) {
        return userRepository.save(new User(userDetails.getEmail(), Role.USER, true));
    }

    public void deleteUser(String email) {
        LOGGER.info("Delete userid={}", email);
        userRepository.delete(email);
    }
}
