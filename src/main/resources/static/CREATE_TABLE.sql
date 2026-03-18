-- 1. Tạo Database
CREATE DATABASE AIAgentDB;


-- 2. Bảng Users
CREATE TABLE Users (
    id SERIAL PRIMARY KEY,            -- Thay cho IDENTITY
    email VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    google_id VARCHAR(255) NOT NULL UNIQUE,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP -- Thay cho GETDATE()
);

-- 3. Bảng Tasks
CREATE TABLE Tasks (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL,
    description TEXT NOT NULL,        -- Dùng TEXT thay vì NVARCHAR(MAX)
    status VARCHAR(50) DEFAULT 'NEW',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. Bảng SubTasks
CREATE TABLE SubTasks (
    id SERIAL PRIMARY KEY,
    task_id INT NOT NULL,
    title VARCHAR(255) NOT NULL,
    is_completed BOOLEAN DEFAULT FALSE, -- Thay cho BIT
    display_order INT DEFAULT 0
);

-- 5. Bảng Schedules
CREATE TABLE Schedules (
    id SERIAL PRIMARY KEY,
    task_id INT NOT NULL,
    subtask_id INT,
    title VARCHAR(255) NOT NULL,
    start_time TIMESTAMP NOT NULL,    -- Thay cho DATETIME2
    end_time TIMESTAMP NOT NULL,
    google_event_id VARCHAR(255),
    location VARCHAR(255)
);

-- 6. Tạo Index
CREATE INDEX IDX_Tasks_User ON Tasks(user_id);
CREATE INDEX IDX_Schedules_Time ON Schedules(start_time, end_time);
CREATE INDEX IDX_SubTasks_Task ON SubTasks(task_id);