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
    private String type;

    public long getRoleId() {
        return roleId;
    }

    public void setRoleId(int roleId) {
        this.roleId = roleId;
    }

    public String getRole() {
        return type;
    }

    public void setRole(String type) {
        this.type = type;
    }
}
