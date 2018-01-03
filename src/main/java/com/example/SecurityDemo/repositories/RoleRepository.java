package com.example.SecurityDemo.repositories;

import com.example.SecurityDemo.domain.Role;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<Role, Long>{
    Role findByType(String type);
}
