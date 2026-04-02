package com.lfit.ms_security.Controllers;

import com.lfit.ms_security.Models.UserRole;
import com.lfit.ms_security.Services.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/user-role")
public class UserRoleController {

    @Autowired
    private UserRoleService theUserRoleService;

    @GetMapping("")
    public List<UserRole> find() {
        return this.theUserRoleService.find();
    }

    @GetMapping("{id}")
    public UserRole findById(@PathVariable String id) {
        return this.theUserRoleService.findById(id);
    }

    @PostMapping("user/{userId}/role/{roleId}")
    public ResponseEntity<Map<String, String>> addUserRole(
            @PathVariable String userId,
            @PathVariable String roleId) {

        boolean response = this.theUserRoleService.addUserRole(userId, roleId);
        if (response) {
            return ResponseEntity.ok(Map.of("message", "Success"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "User or Role not found"));
        }
    }

    @PutMapping("{id}")
    public UserRole update(@PathVariable String id, @RequestBody UserRole newUserRole) {
        return this.theUserRoleService.update(id, newUserRole);
    }

    @DeleteMapping("{userRoleId}")
    public ResponseEntity<Map<String, String>> removeUserRole(
            @PathVariable String userRoleId) {

        boolean response = this.theUserRoleService.removeUserRole(userRoleId);
        if (response) {
            return ResponseEntity.ok(Map.of("message", "Success"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "UserRole not found"));
        }
    }
}