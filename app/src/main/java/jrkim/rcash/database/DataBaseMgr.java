package jrkim.rcash.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.provider.ContactsContract;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import jrkim.rcash.utils.SharedPreferenceMgr;

import static jrkim.rcash.consts.RCashConsts.SHAREDPREF_INT_DBVERSION;

public class DataBaseMgr {

    private static DataBaseMgr instance = null;
    private static SQLiteDatabase sqlite = null;
    private Context context = null;
    private static final String TAG = "RCash_Database";
    private static final int LOG_MAX = 10000;

    public static DataBaseMgr getInstance(Context context) {
        if(instance == null && context != null) {
            synchronized (DataBaseMgr.class) {
                if(instance == null) {
                    instance = new DataBaseMgr(context);
                    instance.open();
                }
            }
        }

        if(instance != null && sqlite == null) {
            instance.open();
        }

        return instance;
    }

    private DataBaseMgr(Context context) {
        this.context = context;
    }

    private void open() {
        synchronized (DataBaseMgr.class) {
            if(sqlite == null) {
                try {
                    File db = context.getDatabasePath(DataBaseConsts.DB_NAME);
                    SharedPreferenceMgr sharedPreferenceMgr = new SharedPreferenceMgr(context);
                    int oldVersion = sharedPreferenceMgr.get(SHAREDPREF_INT_DBVERSION, -1);
                    if(!db.exists()) {
                        sharedPreferenceMgr.put(SHAREDPREF_INT_DBVERSION, DataBaseConsts.DB_VERSION);
                        File parent = db.getParentFile();
                        parent.mkdirs();
                        db.createNewFile();
                    } else if (oldVersion != DataBaseConsts.DB_VERSION) {
                        /**
                         * TODO
                         * 현재 DB버전이 다르면 삭제후 재생성 -> 이후 DB를 새로운 버전으로 업데이트 하도록 코드 추가필요
                         */
                        sharedPreferenceMgr.put(SHAREDPREF_INT_DBVERSION, DataBaseConsts.DB_VERSION);
                        db.delete();
                        db.createNewFile();
                    }

                    sqlite = SQLiteDatabase.openDatabase(db.getAbsolutePath(), null, SQLiteDatabase.OPEN_READWRITE);

                    try {
                        sqlite.execSQL(DataBaseConsts._CREATE_HISTORY);
                        sqlite.execSQL(DataBaseConsts._CREATE_ALIAS);
                        sqlite.execSQL(DataBaseConsts._CREATE_GEOLOCATION);
                    } catch (SQLiteException e) {
                        e.printStackTrace();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void delete() {
        synchronized (DataBaseMgr.class) {
            if(sqlite != null) {
                sqlite.close();
                sqlite = null;
                instance = null;
            }
            context.deleteDatabase(DataBaseConsts.DB_NAME);
        }
    }

    public Cursor getAllHistories() {
        synchronized (DataBaseMgr.class) {
            if(sqlite != null) {
                Cursor cursor;
                try {
                    cursor = sqlite.query(DataBaseConsts._TABLE_HISTORY, null, null, null, null, null, DataBaseConsts._timestamp + " DESC");
                } catch (SQLiteException e) {
                    e.printStackTrace();
                    cursor = null;
                }
                return cursor;
            }
            return null;
        }
    }

    public Cursor getHistoryById(int id) {
        synchronized (DataBaseMgr.class) {
            if(sqlite != null) {
                String [] args = new String [] {String.valueOf(id)};
                return sqlite.rawQuery("SELECT * FROM " + DataBaseConsts._TABLE_HISTORY + " WHERE " + DataBaseConsts._ID + " = ?", args);
            }
            return null;
        }
    }

    public Cursor getHistoryByTimestamp(long timestamp) {
        synchronized (DataBaseMgr.class) {
            if(sqlite != null) {
                String [] args = new String [] {String.valueOf(timestamp)};
                return sqlite.rawQuery("SELECT * FROM " + DataBaseConsts._TABLE_HISTORY + " WHERE " + DataBaseConsts._timestamp + " = ?", args);
            }
            return null;
        }
    }

    public Cursor getHistoryByAlias(String alias) {
        synchronized (DataBaseMgr.class) {
            if(sqlite != null) {
                String [] args = new String [] { alias };
                return sqlite.rawQuery("SELECT * FROM " + DataBaseConsts._TABLE_HISTORY + " WHERE " + DataBaseConsts._alias + " = ?", args);
            }
            return null;
        }
    }

    public Cursor getHistoryByAddress(String address) {
        synchronized (DataBaseMgr.class) {
            if(sqlite != null) {
                String [] args = new String [] { address };
                return sqlite.rawQuery("SELECT * FROM " + DataBaseConsts._TABLE_HISTORY + " WHERE " + DataBaseConsts._address + " = ?", args);
            }
            return null;
        }
    }

    public boolean deleteHistoryById(int id) {
        synchronized (DataBaseMgr.class) {
            Cursor cursor = getHistoryById(id);
            if(sqlite != null && cursor != null && cursor.getCount() > 0) {
                cursor.close();
                sqlite.delete(DataBaseConsts._TABLE_HISTORY, DataBaseConsts._ID + "=\'" + id + "\'", null);
                return true;
            } else {
                if(cursor != null)
                    cursor.close();
                return false;
            }
        }
    }

    public int removeAllHistories() {
        synchronized (DataBaseMgr.class) {
            if(sqlite != null) {
                return sqlite.delete(DataBaseConsts._TABLE_HISTORY, "1", null);
            }
            return 0;
        }
    }

    public boolean deleteHistoryByTimestamp(long timestamp) {
        synchronized (DataBaseMgr.class) {
            Cursor cursor = getHistoryByTimestamp(timestamp);
            if(sqlite != null && cursor != null && cursor.getCount() > 0) {
                cursor.close();
                sqlite.delete(DataBaseConsts._TABLE_HISTORY, DataBaseConsts._timestamp + "=\'" + timestamp + "\'", null);
                return true;
            } else {
                if(cursor != null)
                    cursor.close();
                return false;
            }
        }
    }

    public boolean updateHistoryByTimestamp(long timestamp, String key, String value) {
        synchronized (DataBaseMgr.class) {
            Cursor cursor = getHistoryByTimestamp(timestamp);
            if(sqlite != null && cursor != null && cursor.getCount() > 0) {
                cursor.close();

                String query = "UPDATE " + DataBaseConsts._TABLE_HISTORY + " SET " + key + "=\'" + value + "\' WHERE " + DataBaseConsts._timestamp + "=\'" + timestamp + "\'";
                sqlite.execSQL(query);
                return true;
            } else {
                if(cursor != null)
                    cursor.close();
                return false;
            }
        }
    }

    public boolean updateHistorybyId(int id, String key, String value) {
        synchronized (DataBaseMgr.class) {
            Cursor cursor = getHistoryById(id);
            if(sqlite != null && cursor != null && cursor.getCount() > 0) {
                cursor.close();

                String query = "UPDATE " + DataBaseConsts._TABLE_HISTORY + " SET " + key + "=\'" + value + "\' WHERE " + DataBaseConsts._ID + "=\'" + id + "\'";
                sqlite.execSQL(query);
                return true;
            } else {
                if(cursor != null) {
                    cursor.close();
                }
                return false;
            }
        }
    }

    public boolean insertHistory(int transactionType, String transactionId, String address, long amount, long timestamp) {

        Log.i(TAG, "inserHistory:" + transactionType + ", " + transactionId + ", " + address + ", " + amount + ", " + timestamp);
        synchronized (DataBaseMgr.class) {

            if(sqlite != null) {
                Cursor cursor = getAllHistories();
                if(cursor != null) {
                    if(cursor.getCount() > LOG_MAX) {
                        String query = DataBaseConsts._ID + " in (select " + DataBaseConsts._ID + " from " + DataBaseConsts._TABLE_HISTORY + " order by " + DataBaseConsts._ID + " asc limit " + (cursor.getCount() - LOG_MAX) + ")";
                        sqlite.delete(DataBaseConsts._TABLE_HISTORY, query, null);
                    }
                    cursor.close();
                }

                ContentValues values = new ContentValues();
                values.put(DataBaseConsts._transactionType, transactionType);
                values.put(DataBaseConsts._transactionId, transactionId != null ? transactionId : "");
                values.put(DataBaseConsts._address, address != null ? address : "");
                values.put(DataBaseConsts._amount, amount);
                values.put(DataBaseConsts._timestamp, timestamp);
                values.put(DataBaseConsts._reserved1, "");
                values.put(DataBaseConsts._reserved2, "");
                values.put(DataBaseConsts._reserved3, "");
                values.put(DataBaseConsts._reserved4, "");


                sqlite.insert(DataBaseConsts._TABLE_HISTORY, null, values);

                return true;
            } else {
                return false;
            }
        }
    }
}
