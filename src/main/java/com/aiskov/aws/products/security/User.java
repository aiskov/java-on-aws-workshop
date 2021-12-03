package com.aiskov.aws.products.security;

import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Id;

@Data
@Entity
public class User {
    @Id
    private String username;
    private String password;
}
