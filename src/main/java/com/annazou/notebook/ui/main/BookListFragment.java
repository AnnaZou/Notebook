package com.annazou.notebook.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.annazou.notebook.BookActivity;
import com.annazou.notebook.R;
import com.annazou.notebook.Utils;
import com.annazou.notebook.VerifyUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BookListFragment extends BasicListFragment implements AdapterView.OnItemClickListener, AdapterView.OnItemLongClickListener, VerifyUtils.VerifyCallback {

    ListView mList;
    BookListAdapter mAdapter;
    TextView mEmptyView;

    List<BookItem> mBookItems;

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
        mBookItems = new ArrayList<>();
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
        mEmptyView = root.findViewById(R.id.book_list_empty_view);
        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshBookList();
        mAdapter.notifyDataSetChanged();

        mList.setVisibility(mAdapter.getCount() > 0 ? View.VISIBLE : View.GONE);
        mEmptyView.setVisibility(mAdapter.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    private void refreshBookList(){
        File books = new File(Utils.getBookDirPath(getActivity()));
        String[] bookList = books.list();

        mBookItems.clear();
        for(String book : bookList){
            BookItem item = new BookItem();
            item.name = book;
            item.locked = VerifyUtils.isBookLocked(getActivity(), book);
            mBookItems.add(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BookItem item = mBookItems.get(position);
        if(item.locked) {
            VerifyUtils.showVerifyDialog(getActivity(), item.name, this);
        } else {
            Intent intent = new Intent(getActivity(), BookActivity.class);
            intent.putExtra(BookActivity.INTENT_BOOK, item.name);
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
            return mBookItems.size();
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
                holder.lock = convertView.findViewById(R.id.book_lock);
                convertView.setTag(holder);
            }
            BookItem item = mBookItems.get(position);
            ViewHolder holder = (ViewHolder) convertView.getTag();
            holder.title.setText(item.name);
            holder.date.setText(Utils.getFileDate(Utils.getBookDir(getActivity(), item.name)));
            holder.lock.setVisibility(item.locked ? View.VISIBLE : View.GONE);
            return convertView;
        }
    }

    class ViewHolder{
        TextView title;
        TextView date;
        ImageView lock;
    }

    class BookItem{
        String name;
        boolean locked;
    }
}
