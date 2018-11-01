package jrkim.rcash.database;

public class DataBaseConsts {
    public static final int DB_VERSION = 1;
    public static final String DB_NAME = "rcash.cb";

    public static final int TRANSACTIONTYPE_SEND = 1;
    public static final int TRANSACTIONTYPE_RECEIVE = 2;

    public static final String _ID = "_id";
    public static final String _transactionType = "_transactionType";   // 타입 send, receive... etc....
    public static final String _transactionId = "_transaxtionId";       // tx id
    public static final String _address = "_address";   // 전송한 주소
    public static final String _amount = "_amount";     // 양
    public static final String _timestamp = "_timestamp"; // 시간
    public static final String _reserved1 = "_reserve1";
    public static final String _reserved2 = "_reserve2";
    public static final String _reserved3 = "_reserve3";
    public static final String _reserved4 = "_reserve4";

    public static final String _alias = "_alias";

    public static final String _longtitude = "_longtitude";
    public static final String _latitude = "_latitude";


    // 거래 내역
    public static final String _TABLE_HISTORY = "_table_history";
    public static final String _CREATE_HISTORY =
            "create table if not exists " + _TABLE_HISTORY + "(" +
                    _ID + " integer primary key autoincrement, " +
                    _transactionType + " integer not null, " +
                    _transactionId + " text not null, " +
                    _address + " text not null, " +
                    _amount + " integer not null, " +
                    _reserved1 + " text not null, " +
                    _reserved2 + " text not null, " +
                    _reserved3 + " text not null, " +
                    _reserved4 + " text not null, " +
                    _timestamp + " integer not null);";

    // 주소 - 별명 매칭
    public static final String _TABLE_ALIAS = "_table_alias";
    public static final String _CREATE_ALIAS =
            "create table if not exists " + _TABLE_ALIAS + "(" +
                    _ID + " integer primary key autoincrement, " +
                    _address + " text not null, " +
                    _alias + " text not null);";

    // for GeoLocation
    public final static String _TABLE_GEOLOCATION = "_table_geolocation";
    public final static String _CREATE_GEOLOCATION = "create table if not exists " + _TABLE_GEOLOCATION + "(" +
            _ID + " integer primary key autoincrement, " +
            _timestamp + " integer not null, " +
            _longtitude + " real not null, " +
            _latitude + " real not null);";
}
