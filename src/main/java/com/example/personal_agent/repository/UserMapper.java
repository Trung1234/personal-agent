package com.example.personal_agent.repository;


import com.example.personal_agent.model.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper {
    User findByEmail(String email);
    void insert(User user);
}