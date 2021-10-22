package com.annazou.notebook.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.annazou.notebook.EditActivity;
import com.annazou.notebook.NoteDatabaseHelper;
import com.annazou.notebook.R;
import com.annazou.notebook.Utils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class NoteListFragment extends BasicListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, View.OnClickListener {

    ListView mList;
    NoteListAdapter mAdapter;
    NoteDatabaseHelper mDatabase;
    List<NoteItem> mNoteItems;
    List<NoteItem> mArrangeList;

    List<NoteItem> mDeleteList;
    int mTopCount;
    TextView mEmptyView;

    protected static NoteListFragment newInstance(int index) {
        NoteListFragment fragment = new NoteListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNoteItems = new ArrayList<>();
        mDatabase = new NoteDatabaseHelper(getActivity());
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_note, container, false);
        mList = root.findViewById(R.id.note_list);
        mAdapter = new NoteListAdapter();
        mList.setAdapter(mAdapter);
        mList.setOnItemLongClickListener(this);
        mList.setOnItemClickListener(this);
        mEmptyView = root.findViewById(R.id.note_list_empty_view);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isInArrangeMode()){
            refreshNoteItemList();
        }
        mAdapter.notifyDataSetChanged();
        mList.setVisibility(mAdapter.getCount() > 0 ? View.VISIBLE : View.GONE);
        mEmptyView.setVisibility(mAdapter.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (isInArrangeMode()){
            return;
        }
        Intent intent = new Intent(getActivity(), EditActivity.class);
        intent.putExtra(EditActivity.INTENT_NOTE, mNoteItems.get(position).name);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //delete, star，setTop
        if(!isInArrangeMode()){
            getArrangeHost().enterArrangeMode();
            return true;
        }
        return false;
    }

    @Override
    public void enterArrangeMode() {
        super.enterArrangeMode();
        mArrangeList = mNoteItems;
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void exitArrangeMode(boolean saveChange) {
        super.exitArrangeMode(saveChange);
        if(saveChange){
            for(NoteItem deleteItem : mDeleteList){
                mDatabase.deleteNote(deleteItem.name);
                Utils.deleteFile(Utils.getNoteDirPath(getActivity()) + "/" + deleteItem.name);
                mArrangeList.remove(deleteItem);
            }
            mTopCount = 0;
            for (int i = 0; i < mArrangeList.size(); i++){
                NoteItem item = mArrangeList.get(i);
                if(item.topOrder > 0) {
                    mTopCount++;
                    item.topOrder = i + 1;
                    mDatabase.setTopOrder(item.name,i + 1);
                } else {
                    mDatabase.setTopOrder(item.name, 0);
                }
                mDatabase.setStar(item.name, item.star);
            }
        }
        refreshNoteItemList();
        mAdapter.notifyDataSetChanged();
    }

    private void refreshNoteItemList(){
        File notes = new File(Utils.getNoteDirPath(getActivity()));
        if(!notes.exists()){
            notes.mkdirs();
        }
        String[] fileList = notes.list();
        mNoteItems = new ArrayList<>();
        List<NoteItem> tops = new ArrayList<>();
        for(String note : fileList){
            NoteItem item = new NoteItem();
            item.name = note;
            item.star = mDatabase.isStar(note);
            item.topOrder = mDatabase.getTopOrder(note);
            if(item.topOrder > 0) {
                // Save space for top items
                mNoteItems.add(0, new NoteItem());
                tops.add(item);
            } else {
                mNoteItems.add(item);
            }
        }

        mTopCount = tops.size();
        for(NoteItem item : tops){
            mNoteItems.remove(item.topOrder - 1);
            mNoteItems.add(item.topOrder - 1, item);
        }
        mDeleteList = new ArrayList<>();
        mArrangeList = new ArrayList<>();
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.arrange_star:
                int position = (int) v.getTag();
                mArrangeList.get(position).star = !mArrangeList.get(position).star;
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.arrange_delete:
                int position1 = (int) v.getTag();
                NoteItem item = mArrangeList.get(position1);
                if(mDeleteList.contains(item)){
                    mDeleteList.remove(item);
                } else {
                    mDeleteList.add(item);
                }
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.arrange_top:
                int position2 = (int) v.getTag();
                NoteItem item2 = mArrangeList.get(position2);
                if(item2.topOrder <= 0) {
                    mTopCount++;
                    mArrangeList.remove(position2);
                    mArrangeList.add(0, item2);
                    item2.topOrder = 1;
                } else {
                    mTopCount--;
                    item2.topOrder = 0;
                    mArrangeList.remove(position2);
                    mArrangeList.add(mTopCount, item2);
                }
                mAdapter.notifyDataSetChanged();
                break;
        }
    }

    class NoteListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return isInArrangeMode() ? mArrangeList.size() : mNoteItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if(convertView == null){
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_note, null);
                ViewHolder holder = new ViewHolder();
                holder.title = convertView.findViewById(R.id.note_title);
                holder.date = convertView.findViewById(R.id.note_date);
                holder.arrangeItems = convertView.findViewById(R.id.arrange_items);
                holder.background = convertView.findViewById(R.id.note_item_bg);
                holder.star = convertView.findViewById(R.id.star);
                holder.top = convertView.findViewById(R.id.note_top_indicator);
                convertView.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            NoteItem item = isInArrangeMode() ? mArrangeList.get(position) : mNoteItems.get(position);
            Log.e("mytest","item = " + item);
            holder.title.setText(Utils.getFileThumbTitle(Utils.getNoteDirPath(getActivity()) + "/" + item.name));
            holder.date.setText(Utils.getFileDate(new File(Utils.getNoteDirPath(getActivity()) + "/" + item.name)));
            holder.background.setBackgroundColor(getActivity().getColor(mDeleteList.contains(item) ?
                    R.color.arrange_mode_delete_bg : R.color.note_item_bg));
            holder.star.setVisibility(isInArrangeMode() ? View.GONE : item.star ? View.VISIBLE : View.GONE);
            holder.arrangeItems.setVisibility(isInArrangeMode() ? View.VISIBLE : View.GONE);
            holder.top.setVisibility(item.topOrder > 0 ? View.VISIBLE : View.GONE);
            if(isInArrangeMode()){
                ImageButton star = holder.arrangeItems.findViewById(R.id.arrange_star);
                star.setTag(position);
                star.setImageResource(item.star ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
                star.setOnClickListener(NoteListFragment.this);

                ImageButton delete = holder.arrangeItems.findViewById(R.id.arrange_delete);
                delete.setTag(position);
                delete.setOnClickListener(NoteListFragment.this);

                ImageButton top = holder.arrangeItems.findViewById(R.id.arrange_top);
                top.setTag(position);
                top.setOnClickListener(NoteListFragment.this);
            }

            return convertView;
        }
    }

    class ViewHolder{
        TextView title;
        TextView date;
        LinearLayout arrangeItems;
        View background;
        ImageView star;
        ImageView top;
    }

    class NoteItem{
        String name;
        boolean star;
        int topOrder;

        @Override
        public boolean equals(Object obj) {
            return obj instanceof NoteItem ? ((NoteItem) obj).name.equals(name) : false;
        }

        @Override
        public String toString() {
            return "name=" + name + " star=" + star + " topOrder=" + topOrder;
        }
    }
}
