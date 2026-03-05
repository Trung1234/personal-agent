-- 1. Tạo Database
CREATE DATABASE AIAgentDB;
GO

USE AIAgentDB;
GO

-- 2. Bảng Users (Lưu thông tin người dùng)
-- Không có khóa ngoại vì đây là bảng gốc
CREATE TABLE Users (
    id INT IDENTITY(1,1) PRIMARY KEY,
    email NVARCHAR(255) NOT NULL,
    name NVARCHAR(100) NOT NULL,
    google_id NVARCHAR(255) NOT NULL,
    avatar_url NVARCHAR(500),
    created_at DATETIME2 DEFAULT GETDATE()
);
GO

-- 3. Bảng Tasks (Lưu nhiệm vụ gốc)
-- user_id là cột bình thường, không phải khóa ngoại
CREATE TABLE Tasks (
    id INT IDENTITY(1,1) PRIMARY KEY,
    user_id INT NOT NULL,            -- Chỉ lưu ID user, không ràng buộc với bảng Users
    description NVARCHAR(MAX) NOT NULL,
    status NVARCHAR(50) DEFAULT 'NEW',
    created_at DATETIME2 DEFAULT GETDATE(),
    updated_at DATETIME2 DEFAULT GETDATE()
);
GO

-- 4. Bảng SubTasks (Lưu checklist)
-- task_id là cột bình thường
CREATE TABLE SubTasks (
    id INT IDENTITY(1,1) PRIMARY KEY,
    task_id INT NOT NULL,            -- Chỉ lưu ID task cha, không ràng buộc với bảng Tasks
    title NVARCHAR(255) NOT NULL,
    is_completed BIT DEFAULT 0,
    display_order INT DEFAULT 0
);
GO

-- 5. Bảng Schedules (Lưu lịch trình)
-- task_id và subtask_id là các cột bình thường
CREATE TABLE Schedules (
    id INT IDENTITY(1,1) PRIMARY KEY,
    task_id INT NOT NULL,            -- Chỉ lưu ID task, không ràng buộc
    subtask_id INT NULL,             -- Chỉ lưu ID subtask (nếu có), không ràng buộc
    title NVARCHAR(255) NOT NULL,
    start_time DATETIME2 NOT NULL,
    end_time DATETIME2 NOT NULL,
    google_event_id NVARCHAR(255),   -- ID của sự kiện trên Google Calendar
    location NVARCHAR(255)
);
GO

-- 6. Tạo Index để tìm kiếm nhanh (Giữ nguyên, không ảnh hưởng khóa ngoại)
CREATE INDEX IDX_Tasks_User ON Tasks(user_id);
CREATE INDEX IDX_Schedules_Time ON Schedules(start_time, end_time);
CREATE INDEX IDX_SubTasks_Task ON SubTasks(task_id);
GO