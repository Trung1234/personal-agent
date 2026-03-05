🔄 Workflow System
<img width="1233" height="378" alt="image" src="https://github.com/user-attachments/assets/c1cb1df5-75c9-4ef5-983c-1c17ea0b61d7" />

🟠 1. User Input (Nhập mô tả)
Thành phần: TaskController + Thymeleaf Form.

Hành động: * Hiển thị create-task.html.

Nhận mô tả ngôn ngữ tự nhiên (VD: "Sắp xếp giúp mình 3 buổi học Java tuần này").

🟢 2. System Check (Kiểm tra lịch trống)
Thành phần: GoogleCalendarService (Google API Client).

Hành động: Sử dụng FreeBusy query để quét các khoảng thời gian trống từ Google Calendar của người dùng trong 7 ngày tới.

🟣 3. AI Processing (Phân tích bằng Gemini)
Thành phần: AIService (Spring AI + Vertex AI Gemini).

Hành động: * Kết hợp mô tả của User và danh sách lịch trống thành một Prompt.

Sử dụng Structured Output để yêu cầu Gemini trả về JSON format chuẩn cho kế hoạch.

🟡 4. User Decision (Xác nhận kế hoạch)
Thành phần: task-preview.html.

Hành động: * Hiển thị bản thảo lịch trình AI vừa lập.

Cung cấp lựa chọn: Lưu & Đồng bộ hoặc Thực hiện lại.

🟢 5. Save & Sync (Lưu trữ & Đồng bộ)
Thành phần: TaskService + MyBatis Mappers + GoogleCalendarService.

Hành động: * Database: Lưu thông tin Task vào SQL Server thông qua MyBatis.

Cloud: Gọi createEvent() để đẩy dữ liệu lên Google Calendar. Lưu lại google_event_id.

🟢 6. Auto Reminder (Thiết lập nhắc nhở)
Thành phần: Google Calendar Payload.

Hành động: Cấu hình thông báo mặc định (15 phút trước qua Popup và 1 ngày trước qua Email).

🟠 7. Notification (Thực hiện công việc)
Thành phần: Hệ thống thông báo của Google (Điện thoại/Desktop).

Hành động: Người dùng nhận thông báo, thực hiện công việc và cập nhật trạng thái Done trên giao diện App.

🚀 Công nghệ sử dụng
Backend: Java 17, Spring Boot 3.5.x (Snapshot).

AI: Spring AI (Gemini  AI).

Persistence: MyBatis, SQL Server (mssql-jdbc).

Integration: Google Calendar API (OAuth 2.0).

Frontend: Thymeleaf, Tailwind CSS.
