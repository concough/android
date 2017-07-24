package com.concough.android.concough;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.concough.android.singletons.FontCacheSingleton;

import java.io.Serializable;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ArchiveDetailActivity extends AppCompatActivity {

    private ArrayList<MyStruct> listDetail;
    private RecyclerView recyclerView;

    private TextView title1;
    private TextView title2;
    private TextView title3;
    private ImageView logoImage;

    private String groupName = "ریاضی و فنی (ریاضی و فنی)";
    private String codeNumber = "1000";
    private String publishedCount = "1";

    private class MyStruct implements Serializable {
        String name="دولتی";
        String date = "1394";
        String additional = "دین: اسلام - زبان: انگلیسی";
        String sellCount = "23";
        String fullDate = "9 دی 1395";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_archive_detail);


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


        title1 = (TextView) findViewById(R.id.archiveDetailA_title1);
        title2 = (TextView) findViewById(R.id.archiveDetailA_title2);
        title3 = (TextView) findViewById(R.id.archiveDetailA_title3);
        logoImage = (CircleImageView) findViewById(R.id.signupInfo1A_imageViewNeutral);

        title1.setText(groupName);


        ActionBar mActionBar = getSupportActionBar();
        mActionBar.setDisplayShowTitleEnabled(false);
        mActionBar.setElevation(2);
        LayoutInflater mInflater = LayoutInflater.from(this);
        View mCustomView = mInflater.inflate(R.layout.cc_archivedetail_actionbar, null);

        TextView abtext = (TextView) mCustomView.findViewById(R.id.archiveDetailActionBarA_title);
        abtext.setTypeface(FontCacheSingleton.getInstance(getApplicationContext()).getBold());

        mActionBar.setCustomView(mCustomView);
        mActionBar.setDisplayShowCustomEnabled(true);


        recyclerView = (RecyclerView) findViewById(R.id.archiveA_recycleDetail);
        RecyclerView.LayoutManager layoutManagerDetails = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(layoutManagerDetails);

//        DetailsAdapter adapterDeails = new DetailsAdapter(this, listDetail);
//        recyclerView.setAdapter(adapterDeails);


    }


//    private class DetailActivity extends RecyclerView.Adapter<DetailActivity.DetailViewHolder> {
//
//        private Context context;
//        private ArrayList<View> listView;
//        private ArrayList<ArchiveDetailActivity.MyStruct> stringList;
//
//        public DetailActivity(Context context, ArrayList allList, ArrayList<MyStruct> stringList) {
//            this.context = context;
//            this.listView = new ArrayList<View>();
//            this.stringList = new ArrayList<ArchiveDetailActivity.MyStruct>();
//        }
//
//        public class DetailViewHolder extends RecyclerView.ViewHolder {
//
//            private TextView text1;
//            private TextView text2;
//            private TextView text3;
//            private TextView text4;
//            private TextView text5;
//            private TextView text6;
//
//            public DetailViewHolder(View itemView) {
//                super(itemView);
//
//            }
//        }
//
//        @Override
//        public DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cc_archivedetail_listitem_details, parent, false);
//            this.listView.add(view);
//            return new DetailViewHolder(view);
//        }
//
//        @Override
//        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//
//        }
//
//        @Override
//        public int getItemCount() {
//            return 0;
//        }
//    }
}
