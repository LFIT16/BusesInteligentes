package com.lfit.ms_security.Services;

import com.lfit.ms_security.Models.Role;
import com.lfit.ms_security.Models.User;
import com.lfit.ms_security.Models.UserRole;
import com.lfit.ms_security.Repositories.RoleRepository;
import com.lfit.ms_security.Repositories.UserRepository;
import com.lfit.ms_security.Repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserRoleService {
    @Autowired
    private UserRepository theUserRepository;
    @Autowired
    private RoleRepository theRoleRepository;
    @Autowired
    private UserRoleRepository theUserRoleRepository;

    public List<UserRole> find() {

        return this.theUserRoleRepository.findAll();
    }

    public UserRole findById(String id) {
        UserRole theUserRole = this.theUserRoleRepository.findById(id).orElse(null);
        return theUserRole;
    }

    public boolean addUserRole(String userId, String roleId){
        User user = this.theUserRepository.findById(userId).orElse(null);
        Role role = this.theRoleRepository.findById(roleId).orElse(null);
        if(user != null && role != null){
            UserRole theUserRole = new UserRole(user, role);
            this.theUserRoleRepository.save(theUserRole);
            return true;
        }else {
            return false;
        }
    }

    public UserRole update(String id, UserRole newUserRole) {
        UserRole existing = this.theUserRoleRepository.findById(id).orElse(null);
        if (existing == null) return null;

        existing.setUser(newUserRole.getUser());
        existing.setRole(newUserRole.getRole());

        return this.theUserRoleRepository.save(existing);
    }

    public boolean removeUserRole(String userRoleId){
        UserRole userRole=this.theUserRoleRepository.findById(userRoleId).orElse(null);
        if(userRole!=null ){
            this.theUserRoleRepository.delete(userRole);
            return true;
        }else{
            return false;
        }
    }
}
