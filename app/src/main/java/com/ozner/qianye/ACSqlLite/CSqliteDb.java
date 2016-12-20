package com.ozner.qianye.ACSqlLite;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by C-sir@hotmail.com  on 2016/1/11.
 */
public class CSqliteDb extends SQLiteOpenHelper {
    public final static String DATABASE_NAME = "CSIROZNER.db";
    private final static int DATABASE_VERSION = 2;
    public CSqliteDb(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);

        // TODO Auto-generated constructor stub
    }
    /**
     * 执行一个SQL查询操作
     *
     * @param SQL查询语句
     * @return list的数组列表
     */
    public static List<String[]> ExecSQL(Context context,String sql, String[] param) {
        if(context!=null) {
            CSqliteDb cSqliteDb=new CSqliteDb(context);
            List<String[]> rets = new ArrayList<String[]>();
            SQLiteDatabase db = cSqliteDb.getReadableDatabase();
            try {
                Cursor cursor = db.rawQuery(sql, param);
                while (cursor.moveToNext()) {
                    int count = cursor.getColumnCount();
                    String[] val = new String[count];
                    for (int i = 0; i < count; i++) {
                        val[i] = cursor.getString(i);
                    }
                    rets.add(val);
                }
                if (!cursor.isClosed()) {
                    cursor.close();
                }
            } catch (Exception e) {
                // TODO: handle exception
            } finally {
                if (db.isOpen()) {
                    db.close();
                }
            }
            return rets;
        }return null;
    }
    /**
     * 创建数据完成以后，可以在里面对数据库进行操作 onCreate只是被 调用一次，以后版本发生改变就调用onUpgrade
     * 正常情况下是不会被直接调用，因为反复调用会有问题
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        // // 创建表（多数情况下只是做创建,正常情况下其他添加,删除都可以做,但是逻辑会表混乱,所以会单独封装出去）
        // // 创建表的sql语句
        // // 类型写错没写会有默认值的:text
        // // 可以执行创建多张表
        // // 一天的数据累加表
        // String dayDateTable = "create table DayDateTable(" + "sn varchar,"
        // + "time datetime," + "vol integer," + "updateflag integer)"
        // + ";";
        //
        // // 一小时的数据量累加
        // String hourDateTable = "create table HourDateTable(" + "sn varchar,"
        // + "time datetime," + "vol integer)" + ";";
        // // 杯子的设置信息
        // String cupSettinTable = "create table CupSettinTable(" +
        // "sn varchar,"
        // + "json varchar)" + ";";
        // // 执行SQL语句
        // db.execSQL(dayDateTable);
        // db.execSQL(hourDateTable);
        // db.execSQL(cupSettinTable);
    }

    // 当你的数据版本号发生更改的时候，这个方法会被调用，里面可以实现自己的逻辑
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (newVersion > oldVersion) {
            // 更新操作(加张表啊，删除表，修改字段...)
        }
    }
    /**
     * 执行sql语句函数 包括创建表，插入表，更新表，删除表
     *
     * @param SQL
     */
    public static void execSQLNonQuery(Context context,String sql, Object[] param) {
        if(context!=null) {
            CSqliteDb cSqliteDb=new CSqliteDb(context);
            SQLiteDatabase db =cSqliteDb .getWritableDatabase();
            db.execSQL(sql, param);
            if (db.isOpen()) {
                db.close();
            }
        }
    }
}