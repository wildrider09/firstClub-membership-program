package com.firstclub.membership.service;

import com.firstclub.membership.domain.User;
import com.firstclub.membership.exception.BusinessRuleException;
import com.firstclub.membership.exception.ResourceNotFoundException;
import com.firstclub.membership.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(String name, String email, String cohort) {
        userRepository.findByEmail(email).ifPresent(u -> {
            throw new BusinessRuleException("A user already exists with email " + email);
        });
        User user = User.builder()
                .name(name)
                .email(email)
                .cohort(cohort)
                .build();
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
