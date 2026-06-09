package com.firstclub.membership.controller;

import com.firstclub.membership.dto.DtoMapper;
import com.firstclub.membership.dto.request.CreateUserRequest;
import com.firstclub.membership.dto.response.UserResponse;
import com.firstclub.membership.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        var user = userService.createUser(request.name(), request.email(), request.cohort());
        return ResponseEntity.status(HttpStatus.CREATED).body(DtoMapper.toUserResponse(user));
    }

    @GetMapping
    public List<UserResponse> list() {
        return userService.listUsers().stream().map(DtoMapper::toUserResponse).toList();
    }

    @GetMapping("/{id}")
    public UserResponse get(@PathVariable Long id) {
        return DtoMapper.toUserResponse(userService.getUser(id));
    }
}
