package com.annazou.notebook;


import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.annazou.notebook.ui.main.NoteListFragment;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BookActivity extends AppCompatActivity implements AdapterView.OnItemLongClickListener, AdapterView.OnItemClickListener, View.OnClickListener {
    public static final String INTENT_BOOK = "book";
    private static final int MSG_UPDATE_LIST = 1;
    private static final int MSG_SORT = 2;

    boolean mIsArrangeMode;

    private String mBook;
    private ListView mList;
    private ChapterAdapter mAdapter;

    private File mBookDir;
    private TextView mEmptyView;
    FloatingActionButton mFab;

    private List<ChapterItem> mChapterItems;
    private List<ChapterItem> mArrangeList;
    private List<ChapterItem> mDeleteList;
    int mTopCount;

    BookDatabaseHelper mDatabase;

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

        mDatabase = new BookDatabaseHelper(this);

        mFab = findViewById(R.id.add_chapter);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BookActivity.this, EditActivity.class);
                intent.putExtra(EditActivity.INTENT_BOOK, mBook);
                intent.putExtra(EditActivity.INTENT_CHAPTER, Utils.getNewFileName());
                startActivity(intent);
            }
        });
        mChapterItems = new ArrayList<>();
        mArrangeList = new ArrayList<>();
        mDeleteList = new ArrayList<>();

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
        menu.setGroupVisible(R.id.book_moreMenu,mIsArrangeMode ? false : true);
        MenuItem store = menu.findItem(R.id.book_store);
        store.setVisible(mIsArrangeMode ? true : false);
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
        } else if (item.getItemId() == R.id.book_store){
            exitArrangeMode(true);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mIsArrangeMode){
            refreshChapterList();
        }
        mAdapter.notifyDataSetChanged();
        mEmptyView.setVisibility(mList.getCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(mIsArrangeMode){
            showSaveChangeDialog();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
        //delete, star
        if(!mIsArrangeMode){
            enterArrangeMode();
            return true;
        }
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if(mIsArrangeMode) return;
        ChapterItem item = mChapterItems.get(position);
        Intent intent = new Intent(this, EditActivity.class);
        intent.putExtra(EditActivity.INTENT_BOOK, mBook);
        intent.putExtra(EditActivity.INTENT_CHAPTER, item.fileName);
        startActivity(intent);
    }

    public void enterArrangeMode(){
        mIsArrangeMode = true;
        mFab.hide();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Arrange mode");
        invalidateOptionsMenu();
        mArrangeList = mChapterItems;
        mAdapter.notifyDataSetChanged();
    }

    public void exitArrangeMode(boolean saveChange){
        if(!mIsArrangeMode) return;
        mIsArrangeMode = false;
        mFab.show();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(mBook);
        invalidateOptionsMenu();

        if(saveChange){
            for(ChapterItem deleteItem : mDeleteList){
                mDatabase.deleteChapter(deleteItem.fileName, mBook);
                Utils.deleteFile(Utils.getBookDirPath(this) + "/" + mBook + "/" + deleteItem.fileName);
                mArrangeList.remove(deleteItem);
            }
            mTopCount = 0;
            for (int i = 0; i < mArrangeList.size(); i++){
                ChapterItem item = mArrangeList.get(i);
                if(item.topOrder > 0) {
                    mTopCount++;
                    item.topOrder = i + 1;
                    mDatabase.setTopOrder(item.fileName,i + 1);
                } else {
                    mDatabase.setTopOrder(item.fileName, 0);
                }
                mDatabase.setStar(item.fileName, item.star);
            }
        }
        refreshChapterList();
        mAdapter.notifyDataSetChanged();
    }

    private void showSaveChangeDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Save change?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exitArrangeMode(true);
            }
        }).setNeutralButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        }).setNegativeButton("Don't save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                exitArrangeMode(false);
            }
        }).show();
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
            String expectedUnit = (units.indexOf(lastUnit) - 1) >= 0 ? units.get(units.indexOf(lastUnit) - 1) : "";
            if(units.indexOf(curNumber) < 0) {
                if (!splitListItem.isEmpty()) {
                    splitListItem += expectedUnit;
                    splitList.add(splitListItem);
                    break;
                }
                splitListItem += curNumber;
                if(curNumber.equals("零")) {
                    lastUnit = expectedUnit;
                    splitListItem += expectedUnit;
                    splitList.add(splitListItem);
                    splitListItem = "";
                    continue;
                }
            } else {
                if(!lastUnit.isEmpty() && units.indexOf(lastUnit) <= units.indexOf(curNumber)){
                    if(splitListItem.length() == 1) {
                        splitListItem += expectedUnit;
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
                    splitListItem += expectedUnit;
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

    List<String> chineseNumbers = new ArrayList<String>(){{
        add("零"); add("一");add("二");add("三");add("四");add("五");add("六");add("七");add("八");add("九");}};
    private int getCount(String chinese){
        return chineseNumbers.indexOf(chinese);
    }

    private void refreshChapterList(){
        mChapterItems.clear();
        File book = Utils.getBookDir(this, mBook);
        File[] list = book.listFiles();

        mTopCount = 0;

        for(File file : list){
            ChapterItem item = new ChapterItem();
            item.fileName = file.getName();
            item.date = file.lastModified();
            item.thumb = Utils.getFileThumbTitle(file.getAbsolutePath());
            item.star = mDatabase.isStar(item.fileName);
            item.topOrder = mDatabase.getTopOrder(item.fileName);
            if(item.topOrder > 0) mTopCount++;
            mChapterItems.add(item);
        }

        String sort = SettingUtils.getBookSortMethod(this);
        if(sort.equals(SettingUtils.SORT_MODIFY)){
            sortByModify();
        } else {
            autoSort();
        }

        for(int i = 0; i < mChapterItems.size(); i++){
            if(i < mTopCount) {
                mChapterItems.add(0, null);
            } else {
                ChapterItem item = mChapterItems.get(i);
                if(item.topOrder > 0) {
                    mChapterItems.set(item.topOrder - 1, item);
                    mChapterItems.remove(i);
                    i--;
                }
            }
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
                ChapterItem item = mArrangeList.get(position1);
                if(mDeleteList.contains(item)){
                    mDeleteList.remove(item);
                } else {
                    mDeleteList.add(item);
                }
                mAdapter.notifyDataSetChanged();
                break;
            case R.id.arrange_top:
                int position2 = (int) v.getTag();
                ChapterItem item2 = mArrangeList.get(position2);
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

    private class ChapterAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mIsArrangeMode ? mArrangeList.size() : mChapterItems.size();
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

                holder.arrangeItems = convertView.findViewById(R.id.chapter_arrange_items);
                holder.background = convertView.findViewById(R.id.chapter_item_bg);
                holder.top = convertView.findViewById(R.id.chapter_top_indicator);
                convertView.setTag(holder);
            }

            ViewHolder holder = (ViewHolder) convertView.getTag();
            ChapterItem item = mIsArrangeMode ? mArrangeList.get(position) : mChapterItems.get(position);
            holder.title.setText(item.thumb);
            holder.time.setText(Utils.getDate(item.date));
            holder.top.setVisibility(item.topOrder > 0 ? View.VISIBLE : View.GONE);
            holder.star.setVisibility(mIsArrangeMode ? View.GONE : item.star ? View.VISIBLE : View.GONE);
            holder.arrangeItems.setVisibility(mIsArrangeMode ? View.VISIBLE : View.GONE);
            holder.background.setBackgroundColor(getColor(mDeleteList.contains(item) ?
                    R.color.arrange_mode_delete_bg : R.color.chapter_item_bg));
            if(mIsArrangeMode){
                ImageButton star = holder.arrangeItems.findViewById(R.id.arrange_star);
                star.setTag(position);
                star.setImageResource(item.star ? android.R.drawable.btn_star_big_on : android.R.drawable.btn_star_big_off);
                star.setOnClickListener(BookActivity.this);

                ImageButton delete = holder.arrangeItems.findViewById(R.id.arrange_delete);
                delete.setTag(position);
                delete.setOnClickListener(BookActivity.this);

                ImageButton top = holder.arrangeItems.findViewById(R.id.arrange_top);
                top.setTag(position);
                top.setOnClickListener(BookActivity.this);
            }
            return convertView;
        }
    }

    class ViewHolder{
        TextView title;
        TextView time;
        ImageView star;
        LinearLayout arrangeItems;
        View background;
        ImageView top;
    }

    class ChapterItem{
        String fileName;
        String thumb;
        long date;
        int autoIndex;
        boolean star;
        int topOrder;
    }

}
