package com.miya.web.controller.admin;

import com.miya.common.result.Result;
import com.miya.common.utils.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 后台管理认证控制器
 */
@Slf4j
@RestController
@RequestMapping("/admin/auth")
@Tag(name = "后台认证管理")
public class AdminAuthController {

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping("/login")
    @Operation(summary = "管理员登录")
    public Result<Map<String, Object>> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        // TODO: 验证用户名密码
        if (!"admin".equals(username) || !"admin123".equals(password)) {
            return Result.error("用户名或密码错误");
        }

        // 生成Token
        String token = jwtUtil.generateToken(1L, username, "ADMIN");

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("userInfo", Map.of(
                "id", 1L,
                "username", username,
                "realName", "系统管理员",
                "avatar", "/api/admin/avatar/default.png"
        ));

        return Result.success(data);
    }

    @GetMapping("/info")
    @Operation(summary = "获取管理员信息")
    public Result<Map<String, Object>> getInfo(@RequestHeader("Authorization") String token) {
        // TODO: 从Token中获取用户信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1L);
        userInfo.put("username", "admin");
        userInfo.put("realName", "系统管理员");
        userInfo.put("avatar", "/api/admin/avatar/default.png");
        userInfo.put("roles", new String[]{"ADMIN"});
        userInfo.put("permissions", new String[]{"*:*:*"});

        return Result.success(userInfo);
    }

    @PostMapping("/logout")
    @Operation(summary = "管理员登出")
    public Result<Void> logout() {
        // TODO: 清除Token缓存
        return Result.success();
    }
}