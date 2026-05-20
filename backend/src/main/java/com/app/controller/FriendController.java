package com.app.controller;

import com.app.common.Result;
import com.app.service.FriendService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/friends")
public class FriendController {

    @Autowired
    private FriendService friendService;

    @GetMapping
    public Result<List<Map<String, Object>>> list() {
        return Result.success(friendService.listFriends());
    }

    @GetMapping("/requests")
    public Result<List<Map<String, Object>>> listRequests() {
        return Result.success(friendService.listRequests());
    }

    @PostMapping("/request")
    public Result<Void> sendRequest(@RequestBody Map<String, Object> body) {
        Long toUserId = Long.valueOf(body.get("toUserId").toString());
        String message = (String) body.getOrDefault("message", "");
        friendService.sendRequest(toUserId, message);
        return Result.ok("好友申请已发送");
    }

    @PutMapping("/request/{id}")
    public Result<Void> handleRequest(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        int status = ((Number) body.get("status")).intValue();
        friendService.handleRequest(id, status);
        return Result.ok(status == 1 ? "已同意好友申请" : "已拒绝好友申请");
    }

    @DeleteMapping("/{friendId}")
    public Result<Void> deleteFriend(@PathVariable Long friendId) {
        friendService.deleteFriend(friendId);
        return Result.ok("已删除好友");
    }

    @GetMapping("/search")
    public Result<List<Map<String, Object>>> searchUsers(@RequestParam String keyword) {
        return Result.success(friendService.searchUsers(keyword));
    }
}
