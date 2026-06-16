package com.app.service;

import com.app.common.BusinessException;
import com.app.entity.*;
import com.app.mapper.*;
import com.app.util.UserContext;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageMapper chatMessageMapper;

    @Autowired
    private ChatGroupMapper chatGroupMapper;

    @Autowired
    private GroupMemberMapper groupMemberMapper;

    @Autowired
    private UserMapper userMapper;

    public Map<String, Object> sendMessage(Long receiverId, Long groupId, String content, int type) {
        Long senderId = UserContext.getUserId();

        ChatMessage message = new ChatMessage();
        message.setSenderId(senderId);
        message.setReceiverId(receiverId);
        message.setGroupId(groupId);

        if (receiverId != null) {
            message.setConversationId(generateConversationId(senderId, receiverId));
        } else if (groupId != null) {
            message.setConversationId("group_" + groupId);
        }

        message.setType(type);
        message.setContent(content);
        message.setIsRead(0);
        chatMessageMapper.insert(message);

        Map<String, Object> result = new HashMap<>();
        result.put("id", message.getId());
        result.put("senderId", senderId);
        result.put("content", content);
        result.put("type", type);
        result.put("createTime", message.getCreateTime());

        User sender = userMapper.selectById(senderId);
        if (sender != null) {
            result.put("senderName", sender.getRealName());
            result.put("senderUsername", sender.getUsername());
        }

        return result;
    }

    public List<Map<String, Object>> getMessages(Long friendId, Long groupId, int limit) {
        Long userId = UserContext.getUserId();
        String conversationId;

        if (friendId != null) {
            conversationId = generateConversationId(userId, friendId);
        } else if (groupId != null) {
            conversationId = "group_" + groupId;
        } else {
            return new ArrayList<>();
        }

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
                .orderByDesc(ChatMessage::getCreateTime)
                .last("LIMIT " + limit);

        List<ChatMessage> messages = chatMessageMapper.selectList(wrapper);
        Collections.reverse(messages);

        return messages.stream().map(m -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", m.getId());
            map.put("senderId", m.getSenderId());
            map.put("receiverId", m.getReceiverId());
            map.put("groupId", m.getGroupId());
            map.put("type", m.getType());
            map.put("content", m.getContent());
            map.put("filePath", m.getFilePath());
            map.put("fileName", m.getFileName());
            map.put("fileSize", m.getFileSize());
            map.put("isRead", m.getIsRead());
            map.put("createTime", m.getCreateTime());

            User sender = userMapper.selectById(m.getSenderId());
            if (sender != null) {
                map.put("senderName", sender.getRealName());
                map.put("senderUsername", sender.getUsername());
            }
            return map;
        }).collect(Collectors.toList());
    }

    public void markRead(Long friendId) {
        Long userId = UserContext.getUserId();
        String conversationId = generateConversationId(friendId, userId);

        LambdaQueryWrapper<ChatMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ChatMessage::getConversationId, conversationId)
                .eq(ChatMessage::getIsRead, 0)
                .eq(ChatMessage::getSenderId, friendId);

        ChatMessage update = new ChatMessage();
        update.setIsRead(1);
        chatMessageMapper.update(update, wrapper);
    }

    @Transactional
    public Map<String, Object> createGroup(String name, List<Long> memberIds) {
        Long ownerId = UserContext.getUserId();

        ChatGroup group = new ChatGroup();
        group.setName(name);
        group.setOwnerId(ownerId);
        group.setMaxMembers(200);
        group.setStatus(1);
        chatGroupMapper.insert(group);

        GroupMember ownerMember = new GroupMember();
        ownerMember.setGroupId(group.getId());
        ownerMember.setUserId(ownerId);
        ownerMember.setRole(2);
        groupMemberMapper.insert(ownerMember);

        if (memberIds != null) {
            for (Long memberId : memberIds) {
                if (!memberId.equals(ownerId)) {
                    GroupMember member = new GroupMember();
                    member.setGroupId(group.getId());
                    member.setUserId(memberId);
                    member.setRole(0);
                    groupMemberMapper.insert(member);
                }
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", group.getId());
        result.put("name", group.getName());
        result.put("ownerId", ownerId);
        return result;
    }

    public List<Map<String, Object>> listGroups() {
        Long userId = UserContext.getUserId();

        LambdaQueryWrapper<GroupMember> memberWrapper = new LambdaQueryWrapper<>();
        memberWrapper.eq(GroupMember::getUserId, userId);
        List<GroupMember> members = groupMemberMapper.selectList(memberWrapper);

        List<Map<String, Object>> groups = new ArrayList<>();
        for (GroupMember member : members) {
            ChatGroup group = chatGroupMapper.selectById(member.getGroupId());
            if (group == null || group.getStatus() == 0) continue;

            Map<String, Object> map = new HashMap<>();
            map.put("id", group.getId());
            map.put("name", group.getName());
            map.put("ownerId", group.getOwnerId());
            map.put("description", group.getDescription());
            map.put("myRole", member.getRole());

            LambdaQueryWrapper<GroupMember> countWrapper = new LambdaQueryWrapper<>();
            countWrapper.eq(GroupMember::getGroupId, group.getId());
            map.put("memberCount", groupMemberMapper.selectCount(countWrapper));

            groups.add(map);
        }
        return groups;
    }

    @Transactional
    public void addMembers(Long groupId, List<Long> memberIds) {
        for (Long userId : memberIds) {
            LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(GroupMember::getGroupId, groupId)
                    .eq(GroupMember::getUserId, userId);
            if (groupMemberMapper.selectCount(wrapper) == 0) {
                GroupMember member = new GroupMember();
                member.setGroupId(groupId);
                member.setUserId(userId);
                member.setRole(0);
                groupMemberMapper.insert(member);
            }
        }
    }

    public void removeMember(Long groupId, Long userId) {
        LambdaQueryWrapper<GroupMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GroupMember::getGroupId, groupId)
                .eq(GroupMember::getUserId, userId);
        groupMemberMapper.delete(wrapper);
    }

    public void updateGroup(Long groupId, String name, String description) {
        ChatGroup group = chatGroupMapper.selectById(groupId);
        if (group == null) {
            throw new BusinessException(404, "群聊不存在");
        }
        if (name != null) group.setName(name);
        if (description != null) group.setDescription(description);
        chatGroupMapper.updateById(group);
    }

    private String generateConversationId(Long user1, Long user2) {
        return user1 < user2 ? user1 + "_" + user2 : user2 + "_" + user1;
    }
}
