package com.example.cloudnotebook.room.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.cloudnotebook.room.dao.NoteDao;
import com.example.cloudnotebook.room.entity.Note;

/**
 * Room 数据库全局管理类
 * 作用：创建数据库实例、管理单例、提供 DAO 访问数据
 * 特点：全局唯一实例，避免重复创建连接
 */
@Database(
        entities = {Note.class},       // 数据库包含的实体类（表）
        version = 1,                   // 数据库版本（修改表结构时必须递增）
        exportSchema = false           // 关闭数据库架构导出（避免编译警告）
)
public abstract class AppDatabase extends RoomDatabase {

    // 全局唯一的数据库单例实例
    private static AppDatabase instance;

    /**
     * 抽象方法：Room 自动生成实现类
     * @return 提供操作 Note 表的 DAO
     */
    public abstract NoteDao noteDao();

    /**
     * 获取数据库单例（全局唯一）
     * synchronized 保证多线程安全
     */
    public static synchronized AppDatabase getInstance(Context context) {
        // 没有实例才创建，保证全局唯一
        if (instance == null) {
            // 构建数据库实例
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),  // 使用全局上下文，防止内存泄漏
                            AppDatabase.class,                // 当前数据库类
                            "cloud_notepad_db")                // 数据库文件名
                    .build();
        }
        // 返回唯一实例
        return instance;
    }
}