package com.lfit.ms_security.Controllers;

import com.lfit.ms_security.Services.UserRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/user-role")
@CrossOrigin
public class UserRoleController {

    @Autowired
    private UserRoleService theUserRoleService;

    @GetMapping("/user")
    public ResponseEntity<List<HashMap<String, Object>>> findGroupedByUser() {
        return ResponseEntity.ok(this.theUserRoleService.findGroupedByUser());
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<HashMap<String, Object>> findGroupedUserById(@PathVariable("id") String userId) {
        HashMap<String, Object> result = this.theUserRoleService.findGroupedUserById(userId);

        if (result == null) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(result);
    }

    @PostMapping("/user/{id}")
    public ResponseEntity<HashMap<String, Object>> createGrouped(
            @PathVariable("id") String userId,
            @RequestBody HashMap<String, Object> body
    ) {
        List<String> roleIds = (List<String>) body.get("roleIds");

        HashMap<String, Object> result = this.theUserRoleService.createGrouped(userId, roleIds);

        if (result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    @PutMapping("/user/{id}")
    public ResponseEntity<HashMap<String, Object>> updateGrouped(
            @PathVariable("id") String userId,
            @RequestBody HashMap<String, Object> body
    ) {
        List<String> roleIds = (List<String>) body.get("roleIds");

        HashMap<String, Object> result = this.theUserRoleService.updateGrouped(userId, roleIds);

        if (result == null) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteGrouped(@PathVariable("id") String userId) {
        boolean deleted = this.theUserRoleService.deleteGrouped(userId);

        if (!deleted) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok().build();
    }
}