package com.example.cloudnotebook.room.entity;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Room数据库笔记实体类
 * 对应本地数据表 note，存储笔记完整信息，兼顾本地持久化与云端同步
 */
@Entity(tableName = "note") // 声明为数据库实体，绑定表名 note
public class Note {

    /**
     * 本地主键ID
     * autoGenerate = true：插入数据时自动自增生成
     */
    @PrimaryKey(autoGenerate = true)
    private int localId;

    // 云端Bmob数据库对应的唯一ID，用于本地与云端数据匹配
    private String serverId;

    // 所属用户ID，实现多用户数据隔离
    private String userId;

    // 笔记标题
    private String title;

    // 笔记正文内容
    private String content;

    // 笔记分类（如工作、学习、生活）
    private String category;

    // 笔记创建时间戳
    private long createTime;

    // 笔记最后修改时间戳
    private long updateTime;

    // 同步标记：true=已同步到云端，false=未同步
    private boolean isSync;

    // 删除标记：true=已软删除，false=正常显示
    private boolean isDeleted;

    /**
     * 空参构造方法
     * Room要求必须提供无参构造，用于框架自动实例化实体对象
     */
    public Note() {}

    /**
     * 带参构造方法（创建新笔记专用）
     * @Ignore 注解：Room不会将此构造方法用于数据库映射
     */
    @Ignore
    public Note(String title,String content,String category,String userId){
        this.title = title;
        this.content = content;
        this.category = category;
        this.userId = userId;

        // 新建笔记时，创建时间、修改时间统一为当前系统时间
        this.createTime = System.currentTimeMillis();
        this.updateTime = this.createTime;
        // 新建笔记默认标记为未同步、未删除
        this.isSync = false;
        this.isDeleted = false;
    }

    /**
     * 全字段构造方法（数据转换/赋值专用）
     * @Ignore 注解：排除Room自动调用
     */
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

    // ====================== Getter & Setter 方法 ======================
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