package com.example.personal_agent.repository;


import com.example.personal_agent.model.Task;
import org.apache.ibatis.annotations.Mapper;


@Mapper
public interface TaskMapper {
    void insert(Task task);
    Task findById(Integer id);
}