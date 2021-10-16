package com.annazou.notebook.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.annazou.notebook.BookActivity;
import com.annazou.notebook.EditActivity;
import com.annazou.notebook.R;
import com.annazou.notebook.Utils;

import java.io.File;

public class NoteListFragment extends BasicListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener {

    ListView mList;
    NoteListAdapter mAdapter;
    String[] mNoteList;

    protected static NoteListFragment newInstance(int index) {
        NoteListFragment fragment = new NoteListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File notes = new File(Utils.getNoteDirPath(getActivity()));
        if(!notes.exists()){
            notes.mkdirs();
        }
        mNoteList = notes.list();
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
        return root;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(getActivity(), EditActivity.class);
        Log.e("mytest","onItemClick = " + mNoteList[position]);

        intent.putExtra(EditActivity.INTENT_NOTE, mNoteList[position]);
        startActivity(intent);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //delete, star，setTop
        return false;
    }

    class NoteListAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mNoteList.length;
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
                convertView.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.title.setText(Utils.getFileThumbTitle(Utils.getNoteDirPath(getActivity()) + "/" + mNoteList[position]));
            holder.date.setText(Utils.getFileDate(new File(Utils.getNoteDirPath(getActivity())) ));

            return convertView;
        }
    }

    class ViewHolder{
        TextView title;
        TextView date;
    }
}
