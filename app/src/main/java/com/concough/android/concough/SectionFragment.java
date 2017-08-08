package com.concough.android.concough;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by Owner on 7/27/2017.
 */

public class SectionFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */

        protected void PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
//        public PlaceholderFragment newInstance(int sectionNumber) {
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
//            return fragment;
//        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            RecyclerView.LayoutManager mLayoutManager;
            RecyclerView recyclerView;


            View rootView = inflater.inflate(R.layout.activity_archive_fragment, container, false);
            recyclerView =(RecyclerView) rootView.findViewById(R.id.archiveFragment_recycle);
            recyclerView.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
            recyclerView.setAdapter(new SetsAdapter(getContext(), new ArrayList<String>()));

            return rootView;



        }

    public class SetsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private Context context;
        private ArrayList<String> arrayList = new ArrayList<>();

        private ImageView logoImage;
        private TextView text1;
        private TextView  text2;
        private TextView  text3;

        public class ViewHolder extends RecyclerView.ViewHolder {
            // each data item is just a string in this case
            public TextView mTextView;
            public ViewHolder(TextView v) {
                super(v);
                mTextView = v;
            }
        }

        public SetsAdapter(Context context, ArrayList<String> arrayList) {
            this.context = context;
            this.arrayList = arrayList;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
//            text1 = (TextView) holder.itemView.findViewById(R.id.ccListitemTabbarL_text1);
//            text2 = (TextView) holder.itemView.findViewById(R.id.ccListitemTabbarL_text2);
//            text3 = (TextView) holder.itemView.findViewById(R.id.ccListitemTabbarL_text3);
        }

        @Override
        public int getItemCount() {
            return 0;
        }
    }





}

