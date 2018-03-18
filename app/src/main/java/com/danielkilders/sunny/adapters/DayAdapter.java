package com.danielkilders.sunny.adapters;

import android.content.Context;
import android.media.Image;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.danielkilders.sunny.R;
import com.danielkilders.sunny.weather.Day;

import org.w3c.dom.Text;

public class DayAdapter extends BaseAdapter {

    private Context mContext;
    private Day[] mDays;

    //TODO: add celsius support

    public DayAdapter(Context context, Day[] days ) {
        mContext = context;
        mDays = days;
    }

    @Override
    public int getCount() {
        return mDays.length;
    }

    @Override
    public Object getItem(int i) {
        return mDays[i];
    }

    @Override
    public long getItemId(int i) {
        return 0; // not going to use this. Used for tagging items for easy reference
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {
        ViewHolder holder;

        if (view == null) {
            view = LayoutInflater.from(mContext).inflate(R.layout.daily_list_item, null);
            holder = new ViewHolder();
            holder.circleImageView = view.findViewById(R.id.circleImageView);
            holder.iconImageView = view.findViewById(R.id.iconImageView);
            holder.temperatureLabel = view.findViewById(R.id.temperatureLabel);
            holder.dayLabel = view.findViewById(R.id.dayNameLabel);

            view.setTag(holder);
        } else {
            holder = (ViewHolder) view.getTag();
        }
        Day day = mDays[position];

        holder.circleImageView.setImageResource(R.drawable.bg_temperature);
        holder.iconImageView.setImageResource(day.getIconId());
        holder.temperatureLabel.setText(day.getTemperatureMax() + "");
        holder.dayLabel.setText(day.getDayOfTheWeek());

        return view;
    }

    private static class ViewHolder {
        ImageView circleImageView;
        ImageView iconImageView;
        TextView temperatureLabel;
        TextView dayLabel;
    }
}
