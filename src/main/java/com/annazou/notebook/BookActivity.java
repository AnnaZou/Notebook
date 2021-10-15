package com.annazou.notebook;

import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;

public class BookActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    public static final String INTENT_BOOK = "book";

    private String mBook;
    private ListView mList;
    private ChapterAdapter mAdapter;

    private File mBookDir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBook = getIntent().getStringExtra(INTENT_BOOK) + "";

        mBookDir = Utils.getBookDir(BookActivity.this, mBook);
        if(!mBookDir.exists()){
            mBookDir.mkdir();
        }

        FloatingActionButton fab = findViewById(R.id.add_chapter);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int chapter = mBookDir.list().length + 1;
                Intent intent = new Intent(BookActivity.this, EditActivity.class);
                intent.putExtra(EditActivity.INTENT_BOOK, mBook);
                intent.putExtra(EditActivity.INTENT_CHAPTER, chapter);
                startActivity(intent);
            }
        });

        mAdapter = new ChapterAdapter();
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        mList.setOnItemLongClickListener(this);

    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.INTENT_BOOK, mBook);
        intent.putExtra(EditActivity.INTENT_CHAPTER, position + 1);
        startActivity(intent);
    }

    private class ChapterAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mBookDir.list().length;
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
                convertView = LayoutInflater.from(BookActivity.this).inflate(R.layout.chapter_item, null);
                ViewHolder holder = new ViewHolder();
                holder.title = convertView.findViewById(R.id.chapter_title);
                holder.time = convertView.findViewById(R.id.chapter_time);
                holder.star = convertView.findViewById(R.id.chapter_star);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            int chapter = position + 1;
            holder.title.setText(Utils.getChapterThumbTitle(BookActivity.this, mBook, chapter));
            holder.time.setText(Utils.getFileDate(new File(Utils.getChapterFilePath(BookActivity.this, mBook, chapter))));
            return convertView;
        }
    }

    class ViewHolder{
        TextView title;
        TextView time;
        ImageView star;
    }

}
