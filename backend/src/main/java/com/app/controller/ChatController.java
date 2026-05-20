package com.app.controller;

import com.app.common.PageResult;
import com.app.common.Result;
import com.app.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping("/send")
    public Result<Map<String, Object>> sendMessage(@RequestBody Map<String, Object> body) {
        Long receiverId = body.get("receiverId") != null ? Long.valueOf(body.get("receiverId").toString()) : null;
        Long groupId = body.get("groupId") != null ? Long.valueOf(body.get("groupId").toString()) : null;
        String content = (String) body.get("content");
        int type = body.get("type") != null ? ((Number) body.get("type")).intValue() : 1;
        return Result.success("发送成功", chatService.sendMessage(receiverId, groupId, content, type));
    }

    @GetMapping("/messages")
    public Result<List<Map<String, Object>>> getMessages(
            @RequestParam(required = false) Long friendId,
            @RequestParam(required = false) Long groupId,
            @RequestParam(defaultValue = "50") int limit) {
        return Result.success(chatService.getMessages(friendId, groupId, limit));
    }

    @PutMapping("/messages/read")
    public Result<Void> markRead(@RequestParam Long friendId) {
        chatService.markRead(friendId);
        return Result.success();
    }

    @PostMapping("/group/create")
    public Result<Map<String, Object>> createGroup(@RequestBody Map<String, Object> body) {
        String name = (String) body.get("name");
        @SuppressWarnings("unchecked")
        List<Long> memberIds = (List<Long>) body.get("memberIds");
        return Result.success("群聊创建成功", chatService.createGroup(name, memberIds));
    }

    @GetMapping("/groups")
    public Result<List<Map<String, Object>>> listGroups() {
        return Result.success(chatService.listGroups());
    }

    @PostMapping("/group/{groupId}/members")
    public Result<Void> addMembers(@PathVariable Long groupId, @RequestBody List<Long> memberIds) {
        chatService.addMembers(groupId, memberIds);
        return Result.ok("成员添加成功");
    }

    @DeleteMapping("/group/{groupId}/members/{userId}")
    public Result<Void> removeMember(@PathVariable Long groupId, @PathVariable Long userId) {
        chatService.removeMember(groupId, userId);
        return Result.ok("成员移除成功");
    }

    @PutMapping("/group/{groupId}")
    public Result<Void> updateGroup(@PathVariable Long groupId, @RequestBody Map<String, Object> body) {
        chatService.updateGroup(groupId, (String) body.get("name"), (String) body.get("description"));
        return Result.ok("群聊更新成功");
    }
}
