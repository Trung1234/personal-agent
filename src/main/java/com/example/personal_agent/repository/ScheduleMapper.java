package com.example.personal_agent.repository;

import com.example.personal_agent.model.Schedule;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ScheduleMapper {
    void insert(Schedule schedule);
}