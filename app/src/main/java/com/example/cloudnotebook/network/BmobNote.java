package com.example.cloudnotebook.network;

import cn.bmob.v3.BmobObject;

/**
 * Bmob云端表Note对应的实体类，字段和后台表完全一致
 */
public class BmobNote extends BmobObject {
    private String userId;
    private String title;
    private String content;
    private String category;
    private Long createTime;
    private Long updateTime;
    private Boolean isDeleted;

    // 构造函数：绑定云端数据表Note
    public BmobNote() {
        setTableName("Note");
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    public Boolean getDeleted() {
        return isDeleted;
    }

    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}