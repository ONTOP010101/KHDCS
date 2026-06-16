package com.app.service;

import com.app.common.BusinessException;
import com.app.entity.*;
import com.app.mapper.*;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FriendService {

    @Autowired
    private FriendshipMapper friendshipMapper;

    @Autowired
    private FriendRequestMapper friendRequestMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private RoleMapper roleMapper;

    public List<Map<String, Object>> listFriends() {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<Friendship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friendship::getUserId, userId);
        List<Friendship> friendships = friendshipMapper.selectList(wrapper);

        List<Map<String, Object>> list = new ArrayList<>();
        for (Friendship f : friendships) {
            User friend = userMapper.selectById(f.getFriendId());
            if (friend == null) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("id", friend.getId());
            map.put("username", friend.getUsername());
            map.put("realName", friend.getRealName());
            map.put("avatar", friend.getAvatar());
            map.put("department", friend.getDepartment());
            map.put("remark", f.getRemark());
            map.put("status", f.getStatus());
            map.put("createTime", f.getCreateTime());

            LambdaQueryWrapper<UserRole> urWrapper = new LambdaQueryWrapper<>();
            urWrapper.eq(UserRole::getUserId, friend.getId());
            UserRole userRole = userRoleMapper.selectOne(urWrapper);
            if (userRole != null) {
                Role role = roleMapper.selectById(userRole.getRoleId());
                map.put("role", role != null ? role.getName() : null);
            }

            list.add(map);
        }
        return list;
    }

    @Transactional
    public void sendRequest(Long toUserId, String message) {
        Long fromUserId = UserContext.getUserId();
        if (fromUserId.equals(toUserId)) {
            throw new BusinessException(400, "不能添加自己为好友");
        }

        LambdaQueryWrapper<Friendship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friendship::getUserId, fromUserId)
                .eq(Friendship::getFriendId, toUserId);
        if (friendshipMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(400, "已经是好友了");
        }

        LambdaQueryWrapper<FriendRequest> reqWrapper = new LambdaQueryWrapper<>();
        reqWrapper.eq(FriendRequest::getFromUserId, fromUserId)
                .eq(FriendRequest::getToUserId, toUserId)
                .eq(FriendRequest::getStatus, 0);
        if (friendRequestMapper.selectCount(reqWrapper) > 0) {
            throw new BusinessException(400, "已发送过好友申请，请等待对方处理");
        }

        FriendRequest request = new FriendRequest();
        request.setFromUserId(fromUserId);
        request.setToUserId(toUserId);
        request.setMessage(message);
        request.setStatus(0);
        friendRequestMapper.insert(request);
    }

    @Transactional
    public void handleRequest(Long requestId, int status) {
        FriendRequest request = friendRequestMapper.selectById(requestId);
        if (request == null || request.getStatus() != 0) {
            throw new BusinessException(400, "申请不存在或已处理");
        }
        if (!request.getToUserId().equals(UserContext.getUserId())) {
            throw new BusinessException(400, "无权处理该申请");
        }

        request.setStatus(status);
        friendRequestMapper.updateById(request);

        if (status == 1) {
            Friendship f1 = new Friendship();
            f1.setUserId(request.getFromUserId());
            f1.setFriendId(request.getToUserId());
            friendshipMapper.insert(f1);

            Friendship f2 = new Friendship();
            f2.setUserId(request.getToUserId());
            f2.setFriendId(request.getFromUserId());
            friendshipMapper.insert(f2);
        }
    }

    public List<Map<String, Object>> listRequests() {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<FriendRequest> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FriendRequest::getToUserId, userId)
                .eq(FriendRequest::getStatus, 0)
                .orderByDesc(FriendRequest::getCreateTime);
        List<FriendRequest> requests = friendRequestMapper.selectList(wrapper);

        return requests.stream().map(r -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", r.getId());
            map.put("fromUserId", r.getFromUserId());
            User fromUser = userMapper.selectById(r.getFromUserId());
            map.put("fromUsername", fromUser != null ? fromUser.getUsername() : null);
            map.put("fromRealName", fromUser != null ? fromUser.getRealName() : null);
            map.put("message", r.getMessage());
            map.put("createTime", r.getCreateTime());
            return map;
        }).collect(Collectors.toList());
    }

    @Transactional
    public void deleteFriend(Long friendId) {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<Friendship> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Friendship::getUserId, userId)
                .eq(Friendship::getFriendId, friendId);
        friendshipMapper.delete(wrapper);

        LambdaQueryWrapper<Friendship> wrapper2 = new LambdaQueryWrapper<>();
        wrapper2.eq(Friendship::getUserId, friendId)
                .eq(Friendship::getFriendId, userId);
        friendshipMapper.delete(wrapper2);
    }

    public List<Map<String, Object>> searchUsers(String keyword) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or()
                    .like(User::getRealName, keyword);
        }
        wrapper.eq(User::getStatus, 1);
        wrapper.last("LIMIT 20");

        return userMapper.selectList(wrapper).stream().map(u -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", u.getId());
            map.put("username", u.getUsername());
            map.put("realName", u.getRealName());
            map.put("department", u.getDepartment());
            return map;
        }).collect(Collectors.toList());
    }
}
