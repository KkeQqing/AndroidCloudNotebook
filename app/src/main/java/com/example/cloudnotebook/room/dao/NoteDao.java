package com.example.cloudnotebook.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.cloudnotebook.room.entity.Note;

import java.util.List;

/**
 * 笔记表的 DAO (Data Access Object)
 * 作用：定义所有对 笔记(note) 表的 增、删、改、查 操作
 */
@Dao
public interface NoteDao {

    /**
     * 插入一条新笔记
     * @param note 要添加的笔记对象
     */
    @Insert
    void insert(Note note);

    /**
     * 更新一条已有的笔记
     * @param note 要修改的笔记对象
     */
    @Update
    void update(Note note);

    /**
     * 查询【当前用户】的【所有未删除】笔记
     * 按【更新时间 最新→最旧】排序
     * LiveData：数据变化时自动通知界面刷新
     */
    @Query("SELECT * FROM note " +
            "WHERE userId = :userId " +
            "AND isDeleted = 0 " +
            "ORDER BY updateTime DESC")
    LiveData<List<Note>> getAllNotes(String userId);

    /**
     * 根据【分类】查询笔记
     * 查询指定用户、指定分类、未删除的笔记
     */
    @Query("SELECT * FROM note " +
            "WHERE userId = :userId " +
            "AND isDeleted = 0 " +
            "AND category = :category " +
            "ORDER BY updateTime DESC")
    LiveData<List<Note>> getNotesByCategory(String userId, String category);

    /**
     * 搜索笔记（模糊查询）
     * 匹配：标题 或 内容 包含搜索关键词
     */
    @Query("SELECT * FROM note " +
            "WHERE userId = :userId " +
            "AND isDeleted = 0 " +
            "AND (title LIKE '%' || :query || '%' OR content LIKE '%' || :query || '%') " +
            "ORDER BY updateTime DESC")
    LiveData<List<Note>> searchNotes(String userId, String query);

    /**
     * 获取【未同步到服务器】的笔记
     * 用于离线同步功能
     */
    @Query("SELECT * FROM note " +
            "WHERE isDeleted = 0 " +
            "AND isSync = 0 " +
            "AND userId = :userId")
    List<Note> getUnsyncedNotes(String userId);

    /**
     * 更新笔记的【同步状态】
     * 上传服务器成功后，标记为已同步
     */
    @Query("UPDATE note SET isSync = :isSync " +
            "WHERE localId = :localId")
    void updateSyncStatus(int localId, boolean isSync);

    /**
     * 【批量软删除】笔记
     * 不是真删除，只是标记 isDeleted = 1
     * 同时更新修改时间
     */
    @Query("UPDATE note SET isDeleted = 1, updateTime = :updateTime " +
            "WHERE localId IN (:ids)")
    void softDeleteNotes(long updateTime, int... ids);

    /**
     * 根据【服务器ID】查询单条笔记
     * 用于云端同步时匹配数据
     */
    @Query("SELECT * FROM note " +
            "WHERE serverId = :serverId LIMIT 1")
    Note getNoteByServerId(String serverId);

    /**
     * 根据【本地ID】查询单条笔记
     * 用于编辑、查看单条笔记
     */
    @Query("SELECT * FROM note " +
            "WHERE localId = :localId")
    Note getNoteByLocalId(int localId);

    @Insert
    long insertAndReturnId(Note note);
}