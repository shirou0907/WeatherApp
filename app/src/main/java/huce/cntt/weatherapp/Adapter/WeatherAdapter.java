package huce.cntt.weatherapp.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import huce.cntt.weatherapp.MainActivity;
import huce.cntt.weatherapp.Models.Weather;
import huce.cntt.weatherapp.R;

public class WeatherAdapter extends BaseAdapter {
    Context context;
    ArrayList<Weather> arrayList;
    TextView txtDate, txtStatus, txtC;
    ImageView imgMain;

    public WeatherAdapter(Context context, ArrayList<Weather> arrayList) {
        this.context = context;
        this.arrayList = arrayList;
    }

    @Override
    public int getCount() {
        return arrayList.size();
    }

    @Override
    public Object getItem(int i) {
        return arrayList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        view = inflater.inflate(R.layout.list_view_items, null);

        Weather weather = arrayList.get(i);

        txtDate = view.findViewById(R.id.txtDate);
        txtStatus = view.findViewById(R.id.txtStatus);
        txtC = view.findViewById(R.id.txtC);
        imgMain = view.findViewById(R.id.imgMain);

        txtDate.setText(weather.getDate());
        txtStatus.setText(weather.getStatus());
        txtC.setText(weather.getMax() +"°C/"+ weather.getMin()+"°C");
        int drawableResourceId = context.getResources().getIdentifier("img"+weather.getImg(), "drawable", context.getPackageName());
        imgMain.setBackgroundResource(drawableResourceId);

        return view;
    }
}
