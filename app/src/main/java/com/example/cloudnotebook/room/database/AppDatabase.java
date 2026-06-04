package com.example.cloudnotebook.room.database;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.example.cloudnotebook.room.dao.NoteDao;
import com.example.cloudnotebook.room.entity.Note;

/**
 * 数据库全局管理类
 * 作用：创建数据库、获取数据库单例、提供 DAO 访问数据
 */
@Database(
        entities = {Note.class},   // 数据库包含的表：只有 Note 表
        version = 1,               // 数据库版本号，修改表结构时需要 +1
        exportSchema = false       // 不导出数据库架构文件（默认 false）
)
public abstract class AppDatabase extends RoomDatabase {

    // 单例：全局唯一的数据库实例
    private static AppDatabase instance;

    /**
     * 抽象方法：Room 自动实现
     * @return 返回 Note 表的 DAO，用于操作笔记数据
     */
    public abstract NoteDao noteDao();

    /**
     * 获取数据库【单例实例】
     * 全局只创建一次数据库，避免多次连接浪费资源
     * synchronized：保证线程安全
     */
    public static synchronized AppDatabase getInstance(Context context){
        // 如果还没有创建实例，就创建
        if (instance == null){
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),  // 上下文
                            AppDatabase.class,               // 数据库类
                            "cloud_notepad_db")             // 数据库文件名
                    .build();
        }
        // 返回唯一实例
        return instance;
    }
}