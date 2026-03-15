package com.example.personal_agent.repository;

import com.example.personal_agent.model.SubTask;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface SubTaskMapper {
    void insert(SubTask subTask);
}
