package com.concough.android.concough;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.concough.android.singletons.FontCacheSingleton;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnCancelListener;
import com.orhanobut.dialogplus.OnItemClickListener;

import java.io.Serializable;
import java.util.ArrayList;


public class ArchiveActivity extends AppCompatActivity {
    private final String TAG = "ArchiveActivity";

    private ArrayAdapter<CharSequence> adapter;
    private ArrayAdapter<CharSequence> adapterTabTop;

    private DialogPlus dialog;
    private Button texButton;
    private RecyclerView recycleView;
    private RecyclerView recycleViewDetails;

    private int currentPositionDropDown;

    private ArrayList<String> list;
    private ArrayList<String> listTab;
    private ArrayList<MyStruct> listDetail;



    private class MyStruct implements Serializable {
        String name="ریاضی";
        String cout = "0 کنکور";
        String itemCode = "کد: 12";

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getCout() {
            return cout;
        }

        public void setCout(String cout) {
            this.cout = cout;
        }

        public String getItemCode() {
            return itemCode;
        }

        public void setItemCode(String itemCode) {
            this.itemCode = itemCode;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive);


        list = new ArrayList<String>();
        list.add("دولتی");
        list.add("آزاد");
        list.add("پیام نور");
        list.add("علمی کاربردی");

        listTab = new ArrayList<String>();
        listTab.add("ریاضی و فنی");
        listTab.add("علوم تجربی");
        listTab.add("علوم انسانی");
        listTab.add("هنر");
        listTab.add("معماری");
        listTab.add("پزشکی");
        listTab.add("عمران");

        listDetail = new ArrayList<MyStruct>();
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());
        listDetail.add(new MyStruct());





        recycleView = (RecyclerView) findViewById(R.id.archiveA_recycle);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setReverseLayout(true);
        layoutManager.setStackFromEnd(false);
        recycleView.setLayoutManager(layoutManager);

        TabTopAdapter adapterTabTop = new TabTopAdapter(this, listTab);
        recycleView.setAdapter(adapterTabTop);


        recycleViewDetails = (RecyclerView) findViewById(R.id.archiveA_recycleDetail);
        RecyclerView.LayoutManager layoutManagerDetails = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL,false);
        recycleViewDetails.setLayoutManager(layoutManagerDetails);

        DetailsAdapter adapterDeails = new DetailsAdapter(this,listDetail);
        recycleViewDetails.setAdapter(adapterDeails);



        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setElevation(2);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.cc_archive_actionbar, null);

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);


        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, R.layout.cc_archive_listitem_archive, list);
        dialog = DialogPlus.newDialog(this)
                .setAdapter(adapter)
                .setOnItemClickListener(new OnItemClickListener() {
                    @Override
                    public void onItemClick(DialogPlus dialog, Object item, View view, int position) {
                        currentPositionDropDown = position;
                        texButton.setText(buttonTextMaker(list.get(position).toString(), false));
                        dialog.dismiss();
                    }
                })
                .setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogPlus dialog) {
                        texButton.setText(buttonTextMaker(list.get(currentPositionDropDown).toString(), false));
                    }
                })
                .setGravity(Gravity.TOP)
                .create();


        texButton = (Button) mCustomView
                .findViewById(R.id.actionBarL_dropDown);
        texButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                if (dialog.isShowing()) {
                    texButton.setText(buttonTextMaker(list.get(currentPositionDropDown).toString(), false));
                    dialog.dismiss();
                } else {
                    texButton.setText(buttonTextMaker(list.get(currentPositionDropDown).toString(), true));
                    dialog.show();
                }

            }

        });


        texButton.setText(buttonTextMaker(list.get(0).toString(), false));
        texButton.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

    }


    private String buttonTextMaker(String txt, boolean doOpen) {
        int up = 0x25B2;
        int down = 0x25BC;
//
//        int up = 0xf077;
//        int down = 0xf077;

        if (doOpen) {
            return new String(Character.toChars(up)) + "   " + txt;
        } else {
            return new String(Character.toChars(down)) + "   " + txt;
        }
    }


    private class TabTopAdapter extends RecyclerView.Adapter<TabTopAdapter.ExampleViewHolder> {

        public class ExampleViewHolder extends RecyclerView.ViewHolder {

            private TextView text1;

            ExampleViewHolder(final View itemView) {
                super(itemView);
                text1 = (TextView) itemView.findViewById(R.id.ccListitemTabbarL_text);
                text1.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(ArchiveActivity.this, "On Click:" + v.getId(), Toast.LENGTH_SHORT).show();
                        for (View item : TabTopAdapter.this.allView) {
                            item.setBackground(null);
                        }
                        v.setBackgroundColor(ContextCompat.getColor(ArchiveActivity.this, R.color.colorConcoughGray));
                        v.setBackground(ContextCompat.getDrawable(ArchiveActivity.this, R.drawable.concough_textview_bottom_border));
                    }
                });
            }
        }


        private Context context;
        private ArrayList<String> stringArrayList = new ArrayList<String>();

        private ArrayList<View> allView;
        private ArrayList<String> mCustomObjects;

        public TabTopAdapter(Context context, ArrayList<String> stringArrayList) {
            this.context = context;
            this.stringArrayList = stringArrayList;
            this.allView = new ArrayList<>();
        }


        @Override
        public ExampleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cc_archive_listitem_tabbar, parent, false);
            this.allView.add(view);
            return new ExampleViewHolder(view);
        }


        @Override
        public void onBindViewHolder(ExampleViewHolder holder, int position) {
            String current = listTab.get(position);
            holder.text1.setText(current);
        }

        @Override
        public int getItemCount() {
            return listTab.size();
        }


    }


    private class DetailsAdapter extends RecyclerView.Adapter<DetailsAdapter.DetailsViewHolder> {

        public class DetailsViewHolder extends RecyclerView.ViewHolder {

            private TextView text1;
            private TextView text2;
            private TextView text3;

            DetailsViewHolder(final View itemView) {
                super(itemView);
                text1 = (TextView) itemView.findViewById(R.id.ccListitemTabbarL_text1);
                text2 = (TextView) itemView.findViewById(R.id.ccListitemTabbarL_text2);
                text3 = (TextView) itemView.findViewById(R.id.ccListitemTabbarL_text3);
                text1.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getRegular());
                text2.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                text3.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getLight());
                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        Toast.makeText(ArchiveActivity.this, "On Click:" + v.getId(), Toast.LENGTH_SHORT).show();
//                        for (View item : DetailsAdapter.this.allView) {
//                            item.setBackground(null);
//                        }
//                        v.setBackgroundColor(ContextCompat.getColor(ArchiveActivity.this, R.color.colorConcoughGray));
//                        v.setBackground(ContextCompat.getDrawable(ArchiveActivity.this, R.drawable.concough_textview_bottom_border));
                    }
                });
            }
        }


        private Context context;
        private ArrayList<MyStruct> stringArrayList = new ArrayList<MyStruct>();

        private ArrayList<View> allView;
        private ArrayList<String> mCustomObjects;

        public DetailsAdapter(Context context, ArrayList<MyStruct> stringArrayList) {
            this.context = context;
            this.stringArrayList = stringArrayList;
            this.allView = new ArrayList<>();
        }


        @Override
        public DetailsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cc_archive_listitem_details, parent, false);
            this.allView.add(view);
            return new DetailsViewHolder(view);
        }


        @Override
        public void onBindViewHolder(DetailsViewHolder holder, int position) {
            String name = ArchiveActivity.this.listDetail.get(position).getName();
            String code = ArchiveActivity.this.listDetail.get(position).getItemCode();
            String count = ArchiveActivity.this.listDetail.get(position).getCout();
            holder.text1.setText(name);
            holder.text2.setText(code);
            holder.text3.setText(count);
        }

        @Override
        public int getItemCount() {
            return listDetail.size();
        }


    }


}

