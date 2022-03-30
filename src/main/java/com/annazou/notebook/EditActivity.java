package com.annazou.notebook;

import android.content.DialogInterface;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.Toast;

import java.io.File;

public class EditActivity extends AppCompatActivity implements EditManager.EditRecordCallback {

    public static final String INTENT_BOOK = "book";
    public static final String INTENT_CHAPTER = "chapter";
    public static final String INTENT_NOTE = "note";

    private static final int MSG_SAVE = 1;
    private static final int MSG_INIT = 2;
    private static final int MSG_SAVE_AND_EXIT = 3;

    private String mBook;
    private String mChapter;
    private String mNote;
    private boolean mChanged;

    private EditText mEditText;
    private String mFilePath;
    private EditManager mEditManager;
    FloatingActionButton mFab;
    boolean mAdjustFabLocation = false;

    private MenuItem mUndoButton;
    private MenuItem mRedoButton;
    TextWatcher mTextWatch = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mChanged = true;
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what){
                case MSG_SAVE:
                    File file = new File(mFilePath);
                    if(!file.exists()) {
                        if(mNote != null && !mNote.isEmpty()) {
                            NoteDatabaseHelper database = new NoteDatabaseHelper(EditActivity.this);
                            database.addNote(mNote);
                        }
                        if(mBook != null && !mBook.isEmpty()){
                            BookDatabaseHelper databaseHelper = new BookDatabaseHelper(EditActivity.this);
                            databaseHelper.addChapter(mChapter, mBook);
                        }
                    }
                    Utils.writeFile(mFilePath, mEditText.getText().toString());
                    mChanged = false;
                    Toast.makeText(EditActivity.this, "Saved", Toast.LENGTH_SHORT);
                    if(msg.obj != null && (boolean)msg.obj == true){
                        finish();
                    }
                    break;
                case MSG_INIT:
                    String content = Utils.readFile(mFilePath);
                    mEditText.setText(content);
                    mEditText.addTextChangedListener(mTextWatch);
                    mEditManager = new EditManager(mEditText);
                    mEditManager.setEditRecordCallback(EditActivity.this);
                    break;
                case MSG_SAVE_AND_EXIT:
                    Message sae = mHandler.obtainMessage(MSG_SAVE, true);
                    mHandler.sendMessage(sae);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        mBook = getIntent().getStringExtra(INTENT_BOOK);
        mChapter = getIntent().getStringExtra(INTENT_CHAPTER);
        mNote = getIntent().getStringExtra(INTENT_NOTE );

        if(mNote != null && !mNote.isEmpty()){
            mFilePath = Utils.getNoteDirPath(this) + "/" + mNote;
            actionBar.setTitle("Note");
        }

        if(mBook != null && !mBook.isEmpty()){
            mFilePath = Utils.getBookDirPath(this) + "/" + mBook + "/" + mChapter;
            actionBar.setTitle(mBook);
        }

        if (mFilePath == null){
            finish();
        }

        mFab = findViewById(R.id.save);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(MSG_SAVE);
            }
        });
        MoveableView.addMoveControl(mFab);

        mEditText = findViewById(R.id.edit);
        refreshFontSize(SettingUtils.getFontSize(this));
        refreshColorMode(SettingUtils.isDarkMode(this));

        File file = new File(mFilePath);
        if(!file.exists()){
            mEditText.addTextChangedListener(mTextWatch);
        } else {
            mHandler.sendEmptyMessage(MSG_INIT);
        }

        ScrollView scrollView = findViewById(R.id.edit_scroll);
        scrollView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getActionMasked() == MotionEvent.ACTION_DOWN){
                    mEditText.requestFocus();
                    mEditText.setSelection(mEditText.getText().length());
                    InputMethodManager im = getSystemService(InputMethodManager.class);
                    im.showSoftInput(mEditText, 0);
                    return true;
                }
                return false;
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.edit_menu, menu);
        MenuItem colorItem = menu.findItem(R.id.color_mode);
        colorItem.getSubMenu().clearHeader();
        MenuItem fontItem = menu.findItem(R.id.font_size);
        fontItem.getSubMenu().clearHeader();

        mUndoButton = menu.findItem(R.id.edit_undo);
        mRedoButton = menu.findItem(R.id.edit_redo);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isDark = SettingUtils.isDarkMode(this);
        if(isDark){
            menu.findItem(R.id.dark_mode).setChecked(true);
        } else {
            menu.findItem(R.id.day_mode).setChecked(true);
        }

        int fontSize = SettingUtils.getFontSize(this);
        if(fontSize == 1) {
            menu.findItem(R.id.font_size_small).setChecked(true);
        } else if(fontSize == 2) {
            menu.findItem(R.id.font_size_large).setChecked(true);
        } else {
            menu.findItem(R.id.font_size_normal).setChecked(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.clear:
                mEditText.setText("");
                break;
            case R.id.font_size_normal:
                setFontSize(0);
                break;
            case R.id.font_size_small:
                setFontSize(1);
                break;
            case R.id.font_size_large:
                setFontSize(2);
                break;
            case R.id.day_mode:
                setColorMode(false);
                break;
            case R.id.dark_mode:
                setColorMode(true);
                break;
            case R.id.edit_undo:
                mEditManager.undo();
                break;
            case R.id.edit_redo:
                mEditManager.redo();
                break;
            case R.id.add_image:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFontSize(int size){
        int fontSize = SettingUtils.getFontSize(this);
        if(fontSize != size){
            SettingUtils.setFontSize(this, size);
            refreshFontSize(size);
        }
    }

    private void refreshFontSize(int size){
        mEditText.setTextSize(TypedValue.COMPLEX_UNIT_PX, size == 1 ? getResources().getDimensionPixelSize(R.dimen.font_size_small)
                : size == 2 ? getResources().getDimension(R.dimen.font_size_large)
                : getResources().getDimension(R.dimen.font_size_normal));
    }

    private void setColorMode(boolean isDark){
        boolean dark = SettingUtils.isDarkMode(this);
        if(isDark != dark){
            SettingUtils.setColorMode(this, isDark);
            refreshColorMode(isDark);
        }
    }

    private void refreshColorMode(boolean isDark){
        mEditText.setTextColor(getColor(isDark ? R.color.dark_mode_text_color : R.color.day_mode_text_color));
        View cover = findViewById(R.id.edit_bg_dark_cover);
        cover.setVisibility(isDark ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onBackPressed() {
        if(mChanged) {
            showSaveDialog();
        } else {
            super.onBackPressed();
        }
    }

    private void showSaveDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exiting").setPositiveButton("save and exit", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mHandler.sendEmptyMessage(MSG_SAVE_AND_EXIT);
                dialog.dismiss();
            }
        }).setNegativeButton("exit without save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish();
            }
        }).setNeutralButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        }).show();
    }

    @Override
    public void onRedoEnabled(boolean enable) {
        if(mRedoButton != null && mRedoButton.isEnabled() != enable) {
            mRedoButton.setEnabled(enable);
        }
    }

    @Override
    public void onUndoEnabled(boolean enable) {
        if(mUndoButton != null && mUndoButton.isEnabled() != enable) {
            mUndoButton.setEnabled(enable);
        }
    }
}
