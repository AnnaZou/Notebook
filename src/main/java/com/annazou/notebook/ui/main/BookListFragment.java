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
import com.annazou.notebook.R;
import com.annazou.notebook.Utils;
import com.annazou.notebook.VerifyUtils;

import java.io.File;

public class BookListFragment extends BasicListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, VerifyUtils.VerifyCallback {
    ListView mList;
    BookListAdapter mAdapter;
    String[] mBookList;

    protected static BookListFragment newInstance(int index) {
        BookListFragment fragment = new BookListFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        File books = new File(Utils.getBookDirPath(getActivity()));
        if(!books.exists()){
            books.mkdirs();
        }
        mBookList = new String[0];

    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_book, container, false);
        mList = root.findViewById(R.id.book_list);
        TextView emptyView = new TextView(getActivity());
        emptyView.setText("Empty");
        mList.setEmptyView(emptyView);
        mAdapter = new BookListAdapter();
        mList.setAdapter(mAdapter);
        mList.setOnItemLongClickListener(this);
        mList.setOnItemClickListener(this);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.e("mytest", "onResume Booklist");
        File books = new File(Utils.getBookDirPath(getActivity()));
        mBookList = books.list();
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        String book = mBookList[position];
        if(VerifyUtils.isBookLocked(getActivity(), book)) {
            VerifyUtils.showVerifyDialog(getActivity(), book, this);
        } else {
            Intent intent = new Intent(getActivity(), BookActivity.class);
            intent.putExtra(BookActivity.INTENT_BOOK, book);
            startActivity(intent);
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //delete, star
        return false;
    }

    @Override
    public void onVerifyResult(boolean successed, String book) {
        if(successed) {
            Intent intent = new Intent(getActivity(), BookActivity.class);
            intent.putExtra(BookActivity.INTENT_BOOK, book);
            startActivity(intent);
        }
    }

    class BookListAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mBookList.length;
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
                convertView = LayoutInflater.from(getActivity()).inflate(R.layout.item_book, null);
                ViewHolder holder = new ViewHolder();
                holder.title = convertView.findViewById(R.id.book_title);
                holder.date = convertView.findViewById(R.id.book_date);
                convertView.setTag(holder);
            }
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.title.setText(mBookList[position]);
            holder.date.setText(Utils.getFileDate(Utils.getBookDir(getActivity(), mBookList[position])));

            return convertView;
        }
    }

    class ViewHolder{
        TextView title;
        TextView date;
    }
}
