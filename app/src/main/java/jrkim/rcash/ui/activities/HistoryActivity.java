package jrkim.rcash.ui.activities;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.widget.TextView;

import org.joda.time.DateTime;
import org.joda.time.Period;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jp.wasabeef.recyclerview.animators.ScaleInAnimator;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.data.History;
import jrkim.rcash.database.DataBaseConsts;
import jrkim.rcash.database.DataBaseMgr;
import jrkim.rcash.ui.adapters.HistoryAdapter;
import jrkim.rcash.utils.Utils;

public class HistoryActivity extends BaseActivity implements HistoryAdapter.HistoryListener {

    private final static String TAG = "RCash_History";

    private final static int MESSAGE_LOAD_COMPLETED = 1000;

    @BindView(R.id.rvHistory) RecyclerView rvHistory;

    public class Section {
        public final static int SECTION_RECENTLY = 0;
        public final static int SECTION_WEEKS = 1;
        public final static int SECTION_MONTHS = 2;
        public final static int SECTION_YEARS = 3;

        public Section() {
            type = SECTION_RECENTLY;
            number = 0;
        }

        public int type;
        public int number;
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onToolbarNavigator() {
        onBackPressed();
    }

    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_LOAD_COMPLETED:
                initHistory();
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_history);
        ButterKnife.bind(this);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        ((TextView)findViewById(R.id.tvToolbarTitle)).setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_NOTO_BOLD));
        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);

        new Thread(()-> {
            loadHistoryFromDB();
            makeHistoryForDisplay();
            ((Handler)handler).sendEmptyMessage(MESSAGE_LOAD_COMPLETED);
        }).start();
    }

    private Section getSectionInfo(long time) {
        Section section = new Section();
        if(time > 0) {
            Period period = new Period(new DateTime(time), new DateTime());
            int years = period.getYears();
            int months = period.getMonths();
            int weeks = period.getWeeks();

            if(years > 0) {
                section.type = Section.SECTION_YEARS;
                section.number = years;
            } else if(months > 0) {
                section.type = Section.SECTION_MONTHS;
                section.number = months;
            } else if(weeks > 0) {
                section.type = Section.SECTION_WEEKS;
                section.number = weeks;
            } else {
                section.type = Section.SECTION_RECENTLY;
                section.number = 1;
            }
        }
        return section;
    }

    private void makeHistoryForDisplay() {
        History curHistory = null;
        Section sectionInfo = null;
        Section sectionTemp = null;

        if(History.arrHistory.size() == 0) {
            curHistory = new History();
            curHistory.type = History.TYPE_NOTHING;
            curHistory.timestamp = System.currentTimeMillis();
            History.arrHistory.add(curHistory);
        }

        for(int i = 0; i < History.arrHistory.size(); i++) {
            curHistory = History.arrHistory.get(i);
            sectionTemp = getSectionInfo(curHistory.timestamp);
            if(sectionInfo == null ||
                    sectionInfo.type != sectionTemp.type ||
                    sectionInfo.number != sectionTemp.number) {
                sectionInfo = sectionTemp;
                History history = new History();
                switch (sectionInfo.type) {
                    case Section.SECTION_RECENTLY:
                        history.type = History.TYPE_RECENT;
                        break;
                    case Section.SECTION_WEEKS:
                        history.type = History.TYPE_WEEKAGO;
                        break;
                    case Section.SECTION_MONTHS:
                        history.type = History.TYPE_MONTHAGO;
                        break;
                    case Section.SECTION_YEARS:
                        history.type = History.TYPE_YEARAGO;
                        break;
                }
                history.ago = sectionInfo.number;
                history.timestamp = curHistory.timestamp + 1;
                History.arrHistory.add(i++, history);
            }
        }

        curHistory = new History();
        curHistory.type = History.TYPE_STARTED;
        curHistory.timestamp = Long.MIN_VALUE;
        long firstInstalledTime = Long.MAX_VALUE;
        try {
            firstInstalledTime = getPackageManager().getPackageInfo(getPackageName(), 0).firstInstallTime * 1000;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        long oldestTimeline = curHistory.arrHistory.get(curHistory.arrHistory.size() - 1).timestamp;
        curHistory.timestamp = firstInstalledTime < oldestTimeline ? firstInstalledTime : oldestTimeline;
        History.arrHistory.add(curHistory);
    }

    private void initHistory() {
        rvHistory.setItemAnimator(new ScaleInAnimator());
        rvHistory.setHasFixedSize(true);

        LinearLayoutManager llm =new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        rvHistory.setLayoutManager(llm);

        HistoryAdapter historyAdapter = new HistoryAdapter();
        historyAdapter.setHistoryListener(this);
        historyAdapter.setContext(getApplicationContext());
        rvHistory.setAdapter(historyAdapter);
    }

    private void loadHistoryFromDB() {
        History.arrHistory.clear();
        DataBaseMgr dbMgr = DataBaseMgr.getInstance(this);
        if(dbMgr != null) {
            Cursor cursor = dbMgr.getAllHistories();
            if(cursor != null) {
                while(cursor.moveToNext()) {
                    addHistroyFromCursor(cursor);
                }
                cursor.close();
            }
        }
    }



    private void addHistroyFromCursor(Cursor cursor) {

        History history = new History();
        history.id = cursor.getLong(cursor.getColumnIndex(DataBaseConsts._ID));
        history.transactionType = cursor.getInt(cursor.getColumnIndex(DataBaseConsts._transactionType));
        history.transactionId = cursor.getString(cursor.getColumnIndex(DataBaseConsts._transactionId));
        history.address = cursor.getString(cursor.getColumnIndex(DataBaseConsts._address));
        history.timestamp = cursor.getLong(cursor.getColumnIndex(DataBaseConsts._timestamp));
        history.amount = cursor.getLong(cursor.getColumnIndex(DataBaseConsts._amount));
        history.type = History.TYPE_HISTORY;

//        Log.i(TAG, "addHistory:" + history.id + ", txType:" + history.transactionType + ", txId:" + history.transactionId + ", address:" + history.address + ", timestamp:" + history.timestamp + ", amount:" + history.amount);
        History.arrHistory.add(history);
    }

    @Override
    public void onShowHistory(History history) {
        String url = "https://explorer.bitcoin.com/bch/tx/" + history.transactionId;
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.transaction_ttl));
        intent.putExtra(Intent.EXTRA_TEXT, url);
        startActivity(intent);
    }
}
