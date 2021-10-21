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

import android.os.Handler;
import android.os.Message;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener {
    public static final String INTENT_BOOK = "book";
    private static final int MSG_UPDATE_LIST = 1;
    private static final int MSG_SORT = 2;

    private String mBook;
    private ListView mList;
    private ChapterAdapter mAdapter;

    private File mBookDir;
    private TextView mEmptyView;

    private List<ChapterItem> mChapterItems;

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MSG_SORT:
                    refreshChapterList();
                    mAdapter.notifyDataSetChanged();
                    break;
            }
            super.handleMessage(msg);
        }
    };

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
        mChapterItems = new ArrayList<>();

        mList = findViewById(R.id.chapter_list);
        mAdapter = new ChapterAdapter();
        mList.setAdapter(mAdapter);
        mList.setOnItemClickListener(this);
        mList.setOnItemLongClickListener(this);
        mEmptyView = findViewById(R.id.book_empty_view);
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
        } else if(item.getItemId() == R.id.sort_auto){
            SettingUtils.setBookSortMethod(this,SettingUtils.SORT_AUTO);
            mHandler.sendEmptyMessage(MSG_SORT);
        } else if(item.getItemId() == R.id.sort_modify){
            SettingUtils.setBookSortMethod(this, SettingUtils.SORT_MODIFY);
            mHandler.sendEmptyMessage(MSG_SORT);
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
        initAutoSortIndex();
        for(int i = 0; i < mChapterItems.size(); i++){
            ChapterItem tmp;
            for (int j = i + 1; j < mChapterItems.size(); j++){
                if(mChapterItems.get(i) .autoIndex > mChapterItems.get(j).autoIndex){
                    tmp = mChapterItems.get(i);
                    mChapterItems.set(i, mChapterItems.get(j));
                    mChapterItems.set(j,tmp);
                }
            }
        }
    }

    private void sortByModify(){
        for(int i = 0; i < mChapterItems.size(); i++){
            ChapterItem tmp;
            for (int j = i + 1; j < mChapterItems.size(); j++){
                if(mChapterItems.get(i) .date < mChapterItems.get(j).date){
                    tmp = mChapterItems.get(i);
                    mChapterItems.set(i, mChapterItems.get(j));
                    mChapterItems.set(j,tmp);
                }
            }
        }
    }

    private void initAutoSortIndex(){
        Pattern patternDigit = Pattern.compile("\\d+");
        Pattern patternChineseNumber = Pattern.compile("[零一二三四五六七八九十百]+");
        for(ChapterItem item : mChapterItems){
            String thumb = item.thumb;
            item.autoIndex = Integer.MAX_VALUE;
            String index = "";
            Matcher matcher = patternDigit.matcher(thumb);
            if (matcher.find()) {
                index = matcher.group(0);
                item.autoIndex = Integer.valueOf(index);
                continue;
            }
            if(index.isEmpty()) {
                matcher = patternChineseNumber.matcher(thumb);
                String chineseNumber = "";
                if (matcher.find()){
                    chineseNumber = matcher.group(0);
                    item.autoIndex = getNumberFromChinese(chineseNumber);
                    continue;
                }

            }
        }
    }

    private int getNumberFromChinese(String match){
        List<String> splitList = new ArrayList<>();
        String splitListItem = "";
        String lastUnit = "";
        List<String> units = new ArrayList<>();
        units.add("十");
        units.add("百");

        for(int n = 0; n < match.length(); n++){
            String curNumber = match.substring(n, n + 1);
            if(units.indexOf(curNumber) < 0) {
                if (!splitListItem.isEmpty()) {
                    if (lastUnit.equals("百")) {
                        splitListItem += "十";
                    }
                    splitList.add(splitListItem);
                    break;
                }
                splitListItem += curNumber;
                if(curNumber.equals("零")) {
                    if (lastUnit.equals("百")) {
                        splitListItem += "十";
                        lastUnit = "十";
                    }
                    lastUnit = (units.indexOf(lastUnit) - 1) >= 0 ? units.get(units.indexOf(lastUnit) - 1) : "";
                    splitListItem += lastUnit;
                    splitList.add(splitListItem);
                    splitListItem = "";
                    continue;
                }
            } else {
                if(!lastUnit.isEmpty() && units.indexOf(lastUnit) <= units.indexOf(curNumber)){
                    if(splitListItem.length() == 1) {
                        splitListItem += (units.indexOf(lastUnit) - 1) >= 0 ? units.get(units.indexOf(lastUnit) - 1) : "";
                    }
                    splitList.add(splitListItem);
                    break;
                }
                if (splitListItem.isEmpty()){
                    splitListItem += "一";
                }
                lastUnit = curNumber;
                splitListItem += curNumber;
                splitList.add(splitListItem);
                splitListItem = "";
            }
            if(n == match.length() - 1 && !splitListItem.isEmpty()){
                if(splitListItem.length() == 1) {
                    splitListItem += (units.indexOf(lastUnit) - 1) >= 0 ? units.get(units.indexOf(lastUnit) - 1) : "";
                }
                splitList.add(splitListItem);
            }
        }

        int result = 0;
        for(int m = 0; m < splitList.size(); m++){
            result += getUnit(splitList.get(m));
        }
        return result;
    }

    private int getUnit(String unit){
        if(unit.contains("百")){
            return getCount(unit.replace("百","")) * 100;
        } else if(unit.contains("十")){
            return getCount(unit.replace("十","")) * 10;
        }
        return getCount(unit);
    }

    private int getCount(String chinese){
        switch (chinese){
            case "一":
                return 1;
            case "二":
                return 2;
            case "三":
                return 3;
            case "四":
                return 4;
            case "五":
                return 5;
            case "六":
                return 6;
            case "七":
                return 7;
            case "八":
                return 8;
            case "九":
                return 9;
            case "零":
                return 0;
        }
        return -1;
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

        String sort = SettingUtils.getBookSortMethod(this);
        if(sort.equals(SettingUtils.SORT_MODIFY)){
            sortByModify();
        } else {
            autoSort();
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
        int autoIndex;
    }

}
