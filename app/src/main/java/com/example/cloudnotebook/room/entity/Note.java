package com.example.cloudnotebook.room.entity;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "note")     // Room数据库注解：将该类映射为名为note的数据表
public class Note {
    @PrimaryKey(autoGenerate = true)
    private int localId;    // 本地数据库主键ID，自增长，仅本地有效
    private String serverId;    // 云端Bmob服务器返回的笔记唯一ID
    private String userId;  // 所属用户ID，用于区分不同用户的笔记
    private String title;   // 笔记标题
    private String content; // 笔记内容
    private String category;    // 笔记分类（如：工作、生活、学习等）
    private long createTime;    // 笔记创建时间（毫秒时间戳）
    private long updateTime;    // 笔记最后修改时间（毫秒时间戳）
    private boolean isSync; // 同步状态：true=已同步到云端，false=未同步
    private boolean isDeleted;  // 逻辑删除标记：true=已删除，false=未删除

    public Note() {}

    // 新增笔记时使用的构造方法（自动填充时间、同步、删除状态）
    public Note(String title,String content,String category,String userId){
        this.title= title;
        this.content = content;
        this.category = category;
        this.userId = userId;

        this.createTime = System.currentTimeMillis(); // 获取当前系统时间作为创建时间
        this.updateTime = this.createTime;            // 刚创建时，修改时间 = 创建时间
        this.isSync = false;                          // 新笔记默认未同步到云端
        this.isDeleted = false;                       // 新笔记默认未删除
    }

    // 全参构造（用于查询、数据转换等完整赋值场景）
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

    // getter/setter
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