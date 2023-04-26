package com.example.authserver.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.authserver.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, String>  {
 
    AppUser findByEmail(String email);
    AppUser findByPhone(String phone);
}
