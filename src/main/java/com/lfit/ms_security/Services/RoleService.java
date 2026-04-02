package com.lfit.ms_security.Services;

import com.lfit.ms_security.Models.Role;
import com.lfit.ms_security.Models.UserRole;
import com.lfit.ms_security.Repositories.RoleRepository;
import com.lfit.ms_security.Repositories.UserRoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoleService {

    @Autowired
    private RoleRepository theRoleRepository;

    @Autowired
    private UserRoleRepository theUserRoleRepository;

    public List<Role> find(){
        return this.theRoleRepository.findAll();
    }

    public Role findById(String id){
        return this.theRoleRepository.findById(id).orElse(null);
    }

    public Role create(Role newRole){
        return this.theRoleRepository.save(newRole);
    }

    public Role update(String id, Role newRole){
        Role actualRole = this.theRoleRepository.findById(id).orElse(null);

        if(actualRole != null){
            actualRole.setName(newRole.getName());
            actualRole.setDescription(newRole.getDescription());
            this.theRoleRepository.save(actualRole);
            return actualRole;
        } else {
            return null;
        }
    }

    public boolean delete(String id) {
        Role theRole = this.theRoleRepository.findById(id).orElse(null);
        if (theRole == null) return false;

        List<UserRole> userRoles = this.theUserRoleRepository.findByRole(id);
        if (!userRoles.isEmpty()) {
            return false;
        }

        this.theRoleRepository.delete(theRole);
        return true;
    }
}

