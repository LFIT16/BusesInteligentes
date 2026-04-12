package com.lfit.ms_security.Services;

import com.lfit.ms_security.Models.Role;
import com.lfit.ms_security.Models.User;
import com.lfit.ms_security.Models.UserRole;
import com.lfit.ms_security.Repositories.RoleRepository;
import com.lfit.ms_security.Repositories.UserRepository;
import com.lfit.ms_security.Repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserRoleService {

    @Autowired
    private UserRepository theUserRepository;

    @Autowired
    private RoleRepository theRoleRepository;

    @Autowired
    private UserRoleRepository theUserRoleRepository;

    @Autowired
    private EmailService theEmailService;

    private List<Role> extractRoles(List<UserRole> relations) {
        List<Role> roles = new ArrayList<>();

        for (UserRole relation : relations) {
            if (relation.getRole() != null) {
                boolean exists = roles.stream()
                        .anyMatch(r -> r.getId().equals(relation.getRole().getId()));
                if (!exists) {
                    roles.add(relation.getRole());
                }
            }
        }
        return roles;
    }

    private List<String> normalizeRoleIds(List<String> roleIds) {
        return roleIds == null ? new ArrayList<>() : roleIds;
    }

    private boolean hasRolesChanged(List<Role> oldRoles, List<Role> newRoles) {
        return oldRoles.size() != newRoles.size() ||
                !oldRoles.stream().map(Role::getId).collect(Collectors.toSet())
                        .equals(newRoles.stream().map(Role::getId).collect(Collectors.toSet()));
    }

    private void syncUserRoles(User user, List<String> roleIds) {
        List<UserRole> currentRelations = this.theUserRoleRepository.getRolesByUser(user.getId());
        Set<String> newRoleIds = new HashSet<>(roleIds);
        Set<String> currentRoleIds = currentRelations.stream()
                .filter(relation -> relation.getRole() != null)
                .map(relation -> relation.getRole().getId())
                .collect(Collectors.toSet());

        for (UserRole relation : currentRelations) {
            if (relation.getRole() != null && !newRoleIds.contains(relation.getRole().getId())) {
                this.theUserRoleRepository.delete(relation);
            }
        }

        for (String roleId : roleIds) {
            if (!currentRoleIds.contains(roleId)) {
                Role role = this.theRoleRepository.findById(roleId).orElse(null);
                if (role != null) {
                    this.theUserRoleRepository.save(new UserRole(user, role));
                }
            }
        }
    }

    private HashMap<String, Object> buildResponse(String message, boolean emailSent, String userId) {
        HashMap<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        response.put("emailSent", emailSent);
        response.put("emailMessage", emailSent
                ? "Se notificó al usuario del cambio de roles."
                : "No hubo cambios en los roles.");
        response.put("data", this.findGroupedUserById(userId));
        return response;
    }

    public List<HashMap<String, Object>> findGroupedByUser() {
        List<UserRole> relations = this.theUserRoleRepository.findAll();
        LinkedHashMap<String, HashMap<String, Object>> grouped = new LinkedHashMap<>();

        for (UserRole relation : relations) {
            User user = relation.getUser();
            Role role = relation.getRole();

            if (user == null) continue;

            HashMap<String, Object> row = grouped.get(user.getId());

            if (row == null) {
                row = new HashMap<>();
                row.put("user", user);
                row.put("roles", new ArrayList<Role>());
                grouped.put(user.getId(), row);
            }

            if (role != null) {
                List<Role> roles = (List<Role>) row.get("roles");
                boolean exists = roles.stream().anyMatch(r -> r.getId().equals(role.getId()));
                if (!exists) {
                    roles.add(role);
                }
            }
        }

        return new ArrayList<>(grouped.values());
    }

    public HashMap<String, Object> findGroupedUserById(String userId) {
        User user = this.theUserRepository.findById(userId).orElse(null);
        if (user == null) return null;

        List<Role> roles = extractRoles(this.theUserRoleRepository.getRolesByUser(userId));

        HashMap<String, Object> result = new HashMap<>();
        result.put("user", user);
        result.put("roles", roles);

        return result;
    }

    public HashMap<String, Object> createGrouped(String userId, List<String> roleIds) {
        User user = this.theUserRepository.findById(userId).orElse(null);
        if (user == null) return null;

        roleIds = normalizeRoleIds(roleIds);

        List<Role> oldRoles = extractRoles(this.theUserRoleRepository.getRolesByUser(userId));

        Set<String> currentRoleIds = oldRoles.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        for (String roleId : roleIds) {
            if (!currentRoleIds.contains(roleId)) {
                Role role = this.theRoleRepository.findById(roleId).orElse(null);
                if (role != null) {
                    this.theUserRoleRepository.save(new UserRole(user, role));
                }
            }
        }

        List<Role> newRoles = extractRoles(this.theUserRoleRepository.getRolesByUser(userId));

        boolean emailSent = hasRolesChanged(oldRoles, newRoles);
        if (emailSent) {
            this.theEmailService.sendRolesChangedEmail(user, oldRoles, newRoles);
        }

        return buildResponse("Roles creados correctamente.", emailSent, userId);
    }

    public HashMap<String, Object> updateGrouped(String userId, List<String> roleIds) {
        User user = this.theUserRepository.findById(userId).orElse(null);
        if (user == null) return null;

        roleIds = normalizeRoleIds(roleIds);

        List<Role> oldRoles = extractRoles(this.theUserRoleRepository.getRolesByUser(userId));
        syncUserRoles(user, roleIds);
        List<Role> newRoles = extractRoles(this.theUserRoleRepository.getRolesByUser(userId));

        boolean emailSent = hasRolesChanged(oldRoles, newRoles);
        if (emailSent) {
            this.theEmailService.sendRolesChangedEmail(user, oldRoles, newRoles);
        }

        return buildResponse("Roles actualizados correctamente.", emailSent, userId);
    }

    public boolean deleteGrouped(String userId) {
        User user = this.theUserRepository.findById(userId).orElse(null);
        if (user == null) return false;

        List<UserRole> relations = this.theUserRoleRepository.getRolesByUser(userId);
        if (relations == null || relations.isEmpty()) return false;

        List<Role> oldRoles = extractRoles(relations);
        this.theUserRoleRepository.deleteAll(relations);
        this.theEmailService.sendRolesChangedEmail(user, oldRoles, new ArrayList<>());

        return true;
    }
}