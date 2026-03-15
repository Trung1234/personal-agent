package com.example.personal_agent.controller;


import com.example.personal_agent.model.User;
import com.example.personal_agent.repository.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDateTime;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserMapper userMapper;

    /**
     * Đây là trang đích sau khi Login Google thành công.
     * Spring Security sẽ tự redirect về đây (cần cấu hình trong SecurityConfig).
     */
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {  // ← KHÔNG dùng @AuthenticationPrincipal
//        if (authentication == null || !(authentication instanceof OAuth2AuthenticationToken)) {
//            return "redirect:/login";  // hoặc throw exception
//        }

        OAuth2AuthenticationToken token = (OAuth2AuthenticationToken) authentication;
        OidcUser user = (OidcUser) token.getPrincipal();
        // 1. Lấy thông tin User từ Google Token
        Map<String, Object> attributes = token.getPrincipal().getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");
        String googleId = authentication.getName(); // Đây chính là "sub" (Subject ID) của Google
        String avatarUrl = (String) attributes.get("picture");

        // 2. Kiểm tra xem User đã tồn tại trong DB chưa
        User existingUser = userMapper.findByEmail(email);

        if (existingUser == null) {
            // 3. Nếu chưa có -> Tạo mới và lưu vào SQL Server
            User newUser = new User();
            newUser.setEmail(email);
            newUser.setName(name);
            newUser.setGoogleId(googleId);
            newUser.setAvatarUrl(avatarUrl);
            newUser.setCreatedAt(LocalDateTime.now());

            userMapper.insert(newUser);
            // existingUser giờ đã có ID, nhưng ta không cần trả về gì ở đây
        }
        // Nếu đã tồn tại thì không cần làm gì, chỉ cần Security Context giữ phiên đăng nhập là được.

        return "dashboard"; // Trả về file dashboard.html
    }

    // Trang chủ mặc định sẽ redirect về dashboard nếu chưa login
    @GetMapping("/")
    public String home() {
        return "redirect:/dashboard";
    }
    @GetMapping("/login")
    public String loginPage() {
        // Trả về file login.html nằm trong thư mục templates
        return "login";
    }
}