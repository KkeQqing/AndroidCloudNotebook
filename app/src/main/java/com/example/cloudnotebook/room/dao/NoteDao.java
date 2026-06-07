package com.example.cloudnotebook.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cloudnotebook.room.entity.Note;

import java.util.List;

/**
 * 笔记 DAO 层（Data Access Object）
 * 作用：提供对本地 Room 数据库 note 表的所有操作方法
 * 包括：增、删、改、查、搜索、分类、同步状态管理
 */
@Dao
public interface NoteDao {

    /**
     * 插入单条笔记
     * @param note 要保存的笔记对象
     */
    @Insert
    void insert(Note note);

    /**
     * 更新单条笔记
     * 根据主键（localId）自动匹配更新
     */
    @Update
    void update(Note note);

    /**
     * 查询当前用户的所有未删除笔记
     * 按更新时间 最新 → 最旧 排序
     * 返回 LiveData，数据变化自动刷新页面
     */
    @Query("SELECT * FROM note " +
            "WHERE userId = :userId " +
            "AND isDeleted = 0 " +
            "ORDER BY updateTime DESC")
    LiveData<List<Note>> getAllNotes(String userId);

    /**
     * 根据分类查询笔记（工作、学习、生活等）
     */
    @Query("SELECT * FROM note " +
            "WHERE userId = :userId " +
            "AND isDeleted = 0 " +
            "AND category = :category " +
            "ORDER BY updateTime DESC")
    LiveData<List<Note>> getNotesByCategory(String userId, String category);

    /**
     * 搜索笔记（模糊匹配标题 + 内容）
     * LIKE %关键词% 实现包含查询
     */
    @Query("SELECT * FROM note " +
            "WHERE userId = :userId " +
            "AND isDeleted = 0 " +
            "AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') " +
            "ORDER BY updateTime DESC")
    LiveData<List<Note>> searchNotes(String userId, String query);

    /**
     * 获取未同步到云端的笔记
     * 用于离线后一键云同步
     */
    @Query("SELECT * FROM note " +
            "WHERE isDeleted = 0 " +
            "AND isSync = 0 " +
            "AND userId = :userId")
    List<Note> getUnsyncedNotes(String userId);

    /**
     * 更新笔记的同步状态
     * 上传云端成功后，将 isSync 设为 true
     */
    @Query("UPDATE note SET isSync = :isSync " +
            "WHERE localId = :localId")
    void updateSyncStatus(int localId, boolean isSync);

    /**
     * 批量软删除笔记
     * 不真正删除数据，只标记 isDeleted = 1
     * 支持同时删除多条
     */
    @Query("UPDATE note SET isDeleted = 1, updateTime = :updateTime " +
            "WHERE localId IN (:ids)")
    void softDeleteNotes(long updateTime, int... ids);

    /**
     * 根据云端 ID（serverId）查询本地笔记
     * 用于云端同步时判断数据是否已存在
     */
    @Query("SELECT * FROM note " +
            "WHERE serverId = :serverId LIMIT 1")
    Note getNoteByServerId(String serverId);

    /**
     * 根据本地主键 ID 查询单条笔记
     * 用于编辑、查看详情
     */
    @Query("SELECT * FROM note " +
            "WHERE localId = :localId")
    Note getNoteByLocalId(int localId);

    /**
     * 插入笔记并返回自动生成的主键 ID
     * 用于插入后立刻拿到 ID 进行后续操作
     */
    @Insert
    long insertAndReturnId(Note note);
}