package com.example.cloudnotebook.network;

import cn.bmob.v3.BmobObject;

/**
 * Bmob 云端笔记实体类
 * 作用：与 Bmob 后台的 Note 表一一对应
 * 继承 BmobObject：自动拥有 objectId、createdAt、updatedAt 等云端字段
 */
public class BmobNote extends BmobObject {

    // 关联用户ID（区分哪个用户的笔记）
    private String userId;

    // 笔记标题
    private String title;

    // 笔记内容
    private String content;

    // 笔记分类
    private String category;

    // 创建时间（时间戳）
    private Long createTime;

    // 修改时间（时间戳）
    private Long updateTime;

    // 逻辑删除标记（true=已删除，false=未删除）
    private Boolean isDeleted;

    /**
     * 构造方法
     * 绑定 Bmob 后台的数据表名：Note
     */
    public BmobNote() {
        setTableName("Note");
    }

    // ===================== Getter & Setter =====================
    // 获取用户ID
    public String getUserId() {
        return userId;
    }

    // 设置用户ID
    public void setUserId(String userId) {
        this.userId = userId;
    }

    // 获取标题
    public String getTitle() {
        return title;
    }

    // 设置标题
    public void setTitle(String title) {
        this.title = title;
    }

    // 获取内容
    public String getContent() {
        return content;
    }

    // 设置内容
    public void setContent(String content) {
        this.content = content;
    }

    // 获取分类
    public String getCategory() {
        return category;
    }

    // 设置分类
    public void setCategory(String category) {
        this.category = category;
    }

    // 获取创建时间
    public Long getCreateTime() {
        return createTime;
    }

    // 设置创建时间
    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    // 获取更新时间
    public Long getUpdateTime() {
        return updateTime;
    }

    // 设置更新时间
    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }

    // 获取删除状态（isDeleted）
    public Boolean getDeleted() {
        return isDeleted;
    }

    // 设置删除状态
    public void setDeleted(Boolean deleted) {
        isDeleted = deleted;
    }
}