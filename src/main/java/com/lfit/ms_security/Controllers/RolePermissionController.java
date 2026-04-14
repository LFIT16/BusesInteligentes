package com.lfit.ms_security.Controllers;

import com.lfit.ms_security.Models.RolePermission;
import com.lfit.ms_security.Services.RolePermissionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("/api/role-permission")
public class RolePermissionController {

    @Autowired
    private RolePermissionService theRolePermissionService;
    @GetMapping("/role/{roleId}")
    public ResponseEntity<List<RolePermission>> getByRole(@PathVariable String roleId) {
        List<RolePermission> response = this.theRolePermissionService.getByRole(roleId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("role/{roleId}/permission/{permissionId}")
    public ResponseEntity<Map<String, String>> addRolePermission(
            @PathVariable String roleId,
            @PathVariable String permissionId) {

        boolean response = this.theRolePermissionService.addRolePermission(roleId, permissionId);
        if (response) {
            return ResponseEntity.ok(Map.of("message", "Success"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "Role or Permission not found"));
        }
    }

    @DeleteMapping("{rolePermissionId}")
    public ResponseEntity<Map<String, String>> removeRolePermission(
            @PathVariable String rolePermissionId) {

        boolean response = this.theRolePermissionService.removeRolePermission(rolePermissionId);
        if (response) {
            return ResponseEntity.ok(Map.of("message", "Success"));
        } else {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("message", "RolePermission not found"));
        }
    }
}
