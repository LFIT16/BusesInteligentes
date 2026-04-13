package com.lfit.ms_security.Repositories;

import com.lfit.ms_security.Models.RolePermission;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RolePermissionRepository extends MongoRepository<RolePermission, String> {

    @Query("{'role.$id': ObjectId(?0)}")
    List<RolePermission> getPermissionsByRole(String roleId);

    // ✅ Cambiado a Optional
    @Query("{'role.$id': ObjectId(?0), 'permission.$id': ObjectId(?1)}")
    Optional<RolePermission> getRolePermission(String roleId, String permissionId);

    // ✅ Para verificar duplicados antes de guardar
    @Query("{'role.$id': ObjectId(?0), 'permission.$id': ObjectId(?1)}")
    List<RolePermission> getAllByRoleAndPermission(String roleId, String permissionId);
}