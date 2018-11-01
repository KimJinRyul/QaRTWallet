package jrkim.rcash.data;

import android.content.Context;

import java.util.ArrayList;

import jrkim.rcash.database.DataBaseConsts;

public class History {
    public static ArrayList<History> arrHistory = new ArrayList<>();

    public long id = 0; // database id, you can update DB by this id
    public int transactionType = DataBaseConsts.TRANSACTIONTYPE_SEND;
    public String transactionId = null;
    public String address = null; // to when sent, from when received
    public long amount = 0; // satoshis
    public long timestamp = 0; // timestamp


    // RecyclerView에 보여질때 보여지는 타입
    // 보여줄 layout의 종류에 따라서 아래 추가하고 HistoryAdapter에서 처리
    public final static int TYPE_RECENT = 0;        // 최근
    public final static int TYPE_HISTORY = 1;       // 트랜잭션
    public final static int TYPE_STARTED = 2;       // 시작일
    public final static int TYPE_WEEKAGO = 3;       // 1주전
    public final static int TYPE_MONTHAGO = 4;      // 1달전
    public final static int TYPE_YEARAGO = 5;       // 1년전
    public final static int TYPE_NOTHING = 6;

    public int type = TYPE_NOTHING;
    public int ago = 0;
    public Context context = null;
    public Object view = null;
}
