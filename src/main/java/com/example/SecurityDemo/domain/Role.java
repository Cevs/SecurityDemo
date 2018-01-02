package com.example.SecurityDemo.domain;

import javax.persistence.*;

@Entity
@Table(name="role", schema = "public")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name="id")
    private long roleId;

    @Column(name="type")
    private String role;

    public Role(){}

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
