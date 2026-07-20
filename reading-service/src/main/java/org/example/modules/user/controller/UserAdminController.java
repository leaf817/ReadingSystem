package org.example.modules.user.controller;

import org.example.common.response.ResponseResult;
import org.example.modules.user.dto.UpdateUserRequest;
import org.example.modules.user.dto.UserView;
import org.example.modules.user.service.UserAdminService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class UserAdminController {

    private final UserAdminService userAdminService;

    public UserAdminController(UserAdminService userAdminService) {
        this.userAdminService = userAdminService;
    }

    @GetMapping
    public ResponseResult<List<UserView>> listUsers() {
        return ResponseResult.success(userAdminService.listUsers());
    }

    @PutMapping("/{id}")
    public ResponseResult<UserView> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest request) {
        return ResponseResult.success(userAdminService.updateUser(id, request));
    }
}
