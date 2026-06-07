package com.example.cloudnotebook.room.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "note")
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int localId;
    private String serverId;
    private String userId;
    private String title;
    private String content;
    private String category;
    private long createTime;
    private long updateTime;
    private boolean isSync;
    private boolean isDeleted;

    public Note() {}

    @Ignore
    public Note(String title,String content,String category,String userId){
        this.title= title;
        this.content = content;
        this.category = category;
        this.userId = userId;

        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
        this.isSync = false;
        this.isDeleted = false;
    }

    @Ignore
    public Note(int localId, String serverId, String userId, String title, String content,
                String category, long createTime, long updateTime, boolean isSync, boolean isDeleted) {
        this.localId = localId;
        this.serverId = serverId;
        this.userId = userId;
        this.title = title;
        this.content = content;
        this.category = category;
        this.createTime = createTime;
        this.updateTime = updateTime;
        this.isSync = isSync;
        this.isDeleted = isDeleted;
    }

    // getter/setter 全部不变
    public int getLocalId() { return localId; }
    public void setLocalId(int localId) { this.localId = localId; }
    public String getServerId() { return serverId; }
    public void setServerId(String serverId) { this.serverId = serverId; }
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public long getCreateTime() { return createTime; }
    public void setCreateTime(long createTime) { this.createTime = createTime; }
    public long getUpdateTime() { return updateTime; }
    public void setUpdateTime(long updateTime) { this.updateTime = updateTime; }
    public boolean isSync() { return isSync; }
    public void setSync(boolean sync) { isSync = sync; }
    public boolean isDeleted() { return isDeleted; }
    public void setDeleted(boolean deleted) { isDeleted = deleted; }
}