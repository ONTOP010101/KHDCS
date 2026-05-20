package com.app.entity;

import com.baomidou.mybatisplus.annotation.TableName;

@TableName("chat_groups")
public class ChatGroup extends BaseEntity {

    private String name;
    private String avatar;
    private Long ownerId;
    private String description;
    private Integer maxMembers;
    private Integer status;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public Long getOwnerId() { return ownerId; }
    public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Integer getMaxMembers() { return maxMembers; }
    public void setMaxMembers(Integer maxMembers) { this.maxMembers = maxMembers; }
    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }
}
