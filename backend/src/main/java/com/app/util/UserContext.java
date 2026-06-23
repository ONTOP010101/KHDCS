package com.app.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class UserContext {

    public static Long getUserId() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object userId = request.getAttribute("userId");
            return userId != null ? (Long) userId : null;
        }
        return null;
    }

    public static String getUsername() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object username = request.getAttribute("username");
            return username != null ? (String) username : null;
        }
        return null;
    }

    public static String getRealName() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object realName = request.getAttribute("realName");
            return realName != null ? (String) realName : null;
        }
        return null;
    }

    public static Long getRoleId() {
        HttpServletRequest request = getRequest();
        if (request != null) {
            Object roleId = request.getAttribute("roleId");
            return roleId != null ? (Long) roleId : null;
        }
        return null;
    }

    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return attributes != null ? attributes.getRequest() : null;
    }
}
