package jrkim.rcash.ui.activities;

import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import org.bitcoinj.crypto.MnemonicCode;
import org.joda.time.DateTime;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.utils.Utils;


public class RestoreActivity extends BaseActivity {

    private final static String TAG = "RCash_Restore";

    public final static String EXTRA_SEED = "extra_seed";
    public final static String EXTRA_CREATION_TIME = "extra_creation_time";

    @BindView(R.id.actvSearch) AutoCompleteTextView actvSearch;
    @BindView(R.id.tvPhrases) TextView tvPhrases;
    @BindView(R.id.btnInput) Button btnInput;
    @BindView(R.id.spYears) Spinner spYears;
    @BindView(R.id.spMonths) Spinner spMonths;

    private String phrases = "";
    private long creationTime = -1;
    private int insertedCount = 0;
    private ArrayAdapter<String> adapter = null;
    private static final int MESSAGE_START = 1000;
    @Override
    protected void handleMessage(Message msg) {
        switch (msg.what) {
            case MESSAGE_START:
                findViewById(R.id.rlProgress).setVisibility(View.GONE);
                actvSearch.setAdapter(adapter);
                break;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restore);
        ButterKnife.bind(this);

        Utils.setGlobalFont(getWindow().getDecorView(), RCashConsts.FONT_NOTO_LIGHT);
        findViewById(R.id.rlToolbar).setPadding(0, statusBarHeight, 0, 0);
        findViewById(R.id.rlBottom).setPadding(0, 0, 0, softKeyHeight);

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.years_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spYears.setAdapter(adapter);

        adapter = ArrayAdapter.createFromResource(this,
                R.array.months_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spMonths.setAdapter(adapter);

        tvPhrases.setTypeface(Typeface.createFromAsset(getAssets(), RCashConsts.FONT_SANSATION_BOLD));

        actvSearch.setThreshold(2);
        actvSearch.setDropDownHeight(getResources().getDimensionPixelSize(R.dimen.default_height) * 3);

        new Thread(()->{
            this.adapter = getMnemonicCodes();
            sendEmptyMessageDelayed(handler, MESSAGE_START, 500);
        }).start();

        actvSearch.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            Object item = parent.getItemAtPosition(position);
            if(item instanceof  String) {
                String temp = (String)item;
                if(insertedCount < 12) {
                    insertedCount++;

                    phrases += " " + temp;
                    phrases = phrases.trim();
                    String t = phrases + System.lineSeparator() + insertedCount;
                    tvPhrases.setText(t);
                    actvSearch.setText("");
                }

                if(insertedCount == 12) {
                    findViewById(R.id.actvSearch).setEnabled(false);
                    btnInput.setEnabled(true);
                    btnInput.setAlpha(1.f);
                }
            }
        });

        btnInput.setEnabled(false);
        btnInput.setAlpha(0.4f);
    }

    private ArrayAdapter<String> getMnemonicCodes() {
        List<String> wordList = MnemonicCode.INSTANCE.getWordList();
        String [] arrWords = new String [wordList.size()];
        wordList.toArray(arrWords);
        return new ArrayAdapter<>(this, android.R.layout.select_dialog_item, arrWords);
    }

    @OnClick(R.id.ivToolbarNavigator)
    public void onToolbarNavigator() {
        finish();
    }

    @Override
    public void onBackPressed() {
        onToolbarNavigator();
    }

    @OnClick(R.id.btnInput)
    public void onInsert() {

        DateTime dateTime  = new DateTime();

        int selectedYear = spYears.getSelectedItemPosition();
        int selectedMonth = spMonths.getSelectedItemPosition();

        if(selectedYear != 0 || selectedMonth != 0) {
            if(selectedYear != 0)
                dateTime = dateTime.minusYears(selectedYear);
            if(selectedMonth != 0)
                dateTime = dateTime.minusMonths(selectedMonth + 1);

            creationTime = dateTime.getMillis() / 1000;
        } else {
            creationTime = -1;
        }

        Intent intent = new Intent();
        intent.putExtra(EXTRA_SEED, phrases);
        intent.putExtra(EXTRA_CREATION_TIME, creationTime);
        setResult(RESULT_OK, intent);
        finish();
    }

    @OnClick(R.id.btnDelete)
    public void onDelete() {
        if(insertedCount > 0) {
            insertedCount--;

            if(phrases.lastIndexOf(" ") > 0) {
                phrases = phrases.substring(0, phrases.lastIndexOf(" "));
                phrases = phrases.trim();
            } else {
                phrases = "";
            }

            if(insertedCount > 0) {
                String t = phrases + System.lineSeparator() + insertedCount;
                tvPhrases.setText(t);
            } else {
                tvPhrases.setText("");
            }
        }

        if(insertedCount == 11) {
            findViewById(R.id.actvSearch).setEnabled(true);
            btnInput.setEnabled(false);
            btnInput.setAlpha(0.4f);
        }
    }
}
