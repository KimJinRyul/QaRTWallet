package jrkim.rcash.ui.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.bitcoinj.core.Coin;

import java.util.Calendar;

import jrkim.rcash.R;
import jrkim.rcash.consts.RCashConsts;
import jrkim.rcash.data.History;
import jrkim.rcash.database.DataBaseConsts;
import jrkim.rcash.utils.Utils;

import static jrkim.rcash.data.History.TYPE_HISTORY;
import static jrkim.rcash.data.History.TYPE_MONTHAGO;
import static jrkim.rcash.data.History.TYPE_NOTHING;
import static jrkim.rcash.data.History.TYPE_RECENT;
import static jrkim.rcash.data.History.TYPE_STARTED;
import static jrkim.rcash.data.History.TYPE_WEEKAGO;
import static jrkim.rcash.data.History.TYPE_YEARAGO;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder> {

    private final String TAG = "RCash_HistoryAdapter";
    public interface HistoryListener {
        void onShowHistory(History history);
    }

    private HistoryListener listener = null;
    private Context context = null;

    public void setHistoryListener(HistoryListener listener) {
        this.listener = listener;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public HistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = null;
        switch (viewType) {
            case TYPE_HISTORY:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_transaction, parent, false);
                break;
            case TYPE_STARTED:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_started, parent, false);
                break;
            case TYPE_NOTHING:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_nothing, parent, false);
                break;
            case TYPE_RECENT:
            case TYPE_WEEKAGO:
            case TYPE_MONTHAGO:
            case TYPE_YEARAGO:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_mid, parent,  false);
                break;
            case TYPE_RECENT + 10:
            case TYPE_WEEKAGO + 10:
            case TYPE_MONTHAGO + 10:
            case TYPE_YEARAGO + 10:
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.history_top, parent, false);
                break;
        }

        if(itemView != null) {
            Utils.setGlobalFont(itemView, RCashConsts.FONT_NOTO_REGULAR);
        }

        return new HistoryViewHolder(itemView, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryViewHolder holder, int position) {
        if(History.arrHistory.size() > position) {
            final History history = History.arrHistory.get(position);
            holder.history = history;
            history.view = holder;

            switch (history.type) {
                case TYPE_HISTORY:
                    if(history.transactionType == DataBaseConsts.TRANSACTIONTYPE_SEND) {
                        holder.llBody.setBackgroundResource(R.drawable.shape_rect_send);
                    } else if(history.transactionType == DataBaseConsts.TRANSACTIONTYPE_RECEIVE) {
                        holder.llBody.setBackgroundResource(R.drawable.shape_rect_receive);
                    }
                    holder.tvTxId.setText(history.transactionId);
                    holder.tvAddress.setText(history.address);
                    holder.tvAmount.setText(Coin.valueOf(history.amount).toFriendlyString());
                    holder.tvTime.setText(Utils.getFormattedTime(history.timestamp, context, false));
                    holder.llBody.setOnClickListener((v) -> {
                        if(listener != null) {
                            listener.onShowHistory(history);
                        }
                    });
                    break;
                case TYPE_STARTED:
                    String format = DateUtils.formatDateTime(context, history.timestamp, DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_YEAR);
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(history.timestamp);
                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                    if(hour <= 5)
                        format += " " + context.getString(R.string.timeline_earlymorning);
                    else if(hour < 12)
                        format += " " + context.getString(R.string.timeline_morning);
                    else if(hour == 12)
                        format += " " + context.getString(R.string.timeline_noon);
                    else if(hour < 18)
                        format += " " + context.getString(R.string.timeline_afternoon);
                    else if(hour < 21)
                        format += " " + context.getString(R.string.timeline_evening);
                    else if(hour < 24)
                        format += " " + context.getString(R.string.timeline_night);
                    else
                        format += " " + context.getString(R.string.timeline_midnight);

                    holder.tvAgo.setText(format);
                    break;
                case TYPE_NOTHING:
                    break;
                case TYPE_RECENT:
                    holder.tvAgo.setText(R.string.timeline_recently);
                    break;
                case TYPE_WEEKAGO:
                    if (history.ago == 1)
                        holder.tvAgo.setText(holder.tvAgo.getContext().getString(R.string.time_week_ago));
                    else
                        holder.tvAgo.setText(
                                String.format(
                                        holder.tvAgo.getContext().getString(R.string.time_weeks_ago),
                                        history.ago));
                    break;
                case TYPE_MONTHAGO:
                    if (history.ago == 1)
                        holder.tvAgo.setText(holder.tvAgo.getContext().getString(R.string.time_month_ago));
                    else
                        holder.tvAgo.setText(
                                String.format(
                                        holder.tvAgo.getContext().getString(R.string.time_months_ago),
                                        history.ago));
                    break;
                case TYPE_YEARAGO:
                    if (history.ago == 1)
                        holder.tvAgo.setText(holder.tvAgo.getContext().getString(R.string.time_year_ago));
                    else
                        holder.tvAgo.setText(
                                String.format(
                                        holder.tvAgo.getContext().getString(R.string.time_years_ago),
                                        history.ago));
                    break;
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        if(History.arrHistory.size() > position) {
            if(History.arrHistory.get(position).type != History.TYPE_HISTORY && History.arrHistory.get(position).type != History.TYPE_STARTED && History.arrHistory.get(position).type != History.TYPE_NOTHING)
                return History.arrHistory.get(position).type + (position == 0 ? 10 : 0);
            else
                return History.arrHistory.get(position).type;
        } else {
            return TYPE_NOTHING;
        }
    }

    @Override
    public int getItemCount() {
        if(History.arrHistory != null)
            return History.arrHistory.size();
        return 0;
    }

    public class HistoryViewHolder extends RecyclerView.ViewHolder {
        public History history = null;

        public LinearLayout llBody = null;
        public TextView tvAgo = null;
        public TextView tvTime = null;
        public TextView tvTxId = null;
        public TextView tvAddress = null;
        public TextView tvAmount = null;

        public HistoryViewHolder(View v, int type) {
            super(v);
            switch (type ) {
                case TYPE_RECENT:
                case TYPE_WEEKAGO:
                case TYPE_MONTHAGO:
                case TYPE_YEARAGO:
                case TYPE_RECENT + 10:
                case TYPE_WEEKAGO + 10:
                case TYPE_MONTHAGO + 10:
                case TYPE_YEARAGO + 10:
                    tvAgo = v.findViewById(R.id.tvAgo);
                    break;
                case TYPE_HISTORY:
                    llBody = v.findViewById(R.id.llBody);
                    tvTime = v.findViewById(R.id.tvTime);
                    tvTxId = v.findViewById(R.id.tvTxId);
                    tvAddress = v.findViewById(R.id.tvAddress);
                    tvAmount = v.findViewById(R.id.tvAmount);
                    break;
                case TYPE_STARTED:
                    tvAgo = v.findViewById(R.id.tvDate);
                    break;
                case TYPE_NOTHING:
                    break;

            }
        }

    }
}
