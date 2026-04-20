package com.gitquest.backend.security;

import org.springframework.security.core.context.SecurityContextHolder;

// JWT フィルターがセットした認証情報から、ログイン中のメールアドレスを取り出すユーティリティ
public class SecurityUtil {

    private SecurityUtil() {}

    public static String getCurrentUserEmail() {
        return SecurityContextHolder.getContext()
                .getAuthentication()
                .getName(); // UserDetailsServiceImpl で username にメールアドレスをセットしている
    }
}
