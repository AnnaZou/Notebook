package com.annazou.notebook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.annazou.notebook.ui.main.BookListFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class BookActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    public static final String INTENT_BOOK = "book";

    private String mBook;
    private ListView mList;
    private ChapterAdapter mAdapter;

    private File mBookDir;
    private TextView mEmptyView;

    private List<ChapterItem> mChapterItems;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setHomeButtonEnabled(true);

        mBook = getIntent().getStringExtra(INTENT_BOOK) + "";
        actionBar.setTitle(mBook);

        mBookDir = Utils.getBookDir(BookActivity.this, mBook);
        if(!mBookDir.exists()){
            mBookDir.mkdirs();
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

        mList = findViewById(R.id.chapter_list);
        mAdapter = new ChapterAdapter();
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        mList.setOnItemLongClickListener(this);
        mEmptyView = findViewById(R.id.book_empty_view);
        mChapterItems = new ArrayList<>();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.book_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean locked = VerifyUtils.isBookLocked(this, mBook);
        menu.findItem(R.id.lock).setTitle(locked? "Unlock" : "Lock");
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.lock){
            boolean locked = VerifyUtils.isBookLocked(this, mBook);
            if(locked){
                VerifyUtils.VerifyCallback callback = new VerifyUtils.VerifyCallback() {
                    @Override
                    public void onVerifyResult(boolean successed, String book) {
                        VerifyUtils.removeBookPassword(BookActivity.this, mBook);
                    }
                };
                VerifyUtils.showVerifyDialog(BookActivity.this, mBook, callback);
            } else{
                VerifyUtils.showSetPasswordDialog(BookActivity.this, mBook);
            }
            return true;
        } else if(item.getItemId() == R.id.rename){
            InputDialog renameDialog = new InputDialog(this, "Rename book");
            renameDialog.getInputTextView().setHint(mBook);
            InputDialog.Callbacks callbacks = new InputDialog.Callbacks() {
                @Override
                public void onInputChanged(InputDialog dialog, CharSequence s) {
                    dialog.setWarnText("");
                }

                @Override
                public void onPositiveClicked(InputDialog dialog) {
                    String newName = dialog.getInputText();
                    if (newName.isEmpty()) {
                        dialog.setWarnText("Book name can't be empty.");
                        return;
                    }
                    if (Utils.checkBookNameExists(BookActivity.this, newName)) {
                        dialog.setWarnText("Already exist");
                        return;
                    }
                    Utils.renameFile(Utils.getBookDirPath(BookActivity.this), mBook, newName);
                    mBook = newName;
                    mBookDir = Utils.getBookDir(BookActivity.this, mBook);
                    ActionBar actionBar = getSupportActionBar();
                    actionBar.setTitle(mBook);
                    dialog.getDialog().dismiss();
                }

                @Override
                public void onNegativeClicked(InputDialog dialog) {
                    dialog.getDialog().dismiss();
                }
            };
            renameDialog.setCallback(callbacks);
            renameDialog.getDialog().show();

        } else if(item.getItemId() == android.R.id.home){
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshChapterList();
        mAdapter.notifyDataSetChanged();
        mEmptyView.setVisibility(mList.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //delete, star
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.INTENT_BOOK, mBook);
        intent.putExtra(EditActivity.INTENT_CHAPTER, position + 1);
        startActivity(intent);
    }

    private void autoSort(){
        refreshChapterList();
        for(ChapterItem item : mChapterItems){
         //   item.thumb.
        }
    }

    private void refreshChapterList(){
        mChapterItems.clear();
        File book = Utils.getBookDir(this, mBook);
        File[] list = book.listFiles();

        for(File file : list){
            ChapterItem item = new ChapterItem();
            item.fileName = file.getName();
            item.date = file.lastModified();
            item.thumb = Utils.getFileThumbTitle(file.getAbsolutePath());
            mChapterItems.add(item);
        }
    }

    private class ChapterAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mChapterItems.size();
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
            ChapterItem item = mChapterItems.get(position);
            holder.title.setText(item.thumb);
            holder.time.setText(Utils.getDate(item.date));
            return convertView;
        }
    }

    class ViewHolder{
        TextView title;
        TextView time;
        ImageView star;
    }

    class ChapterItem{
        String fileName;
        String thumb;
        long date;
    }

}
