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
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.File;

public class EditActivity extends AppCompatActivity {

    public static final String INTENT_BOOK = "book";
    public static final String INTENT_CHAPTER = "chapter";
    public static final String INTENT_NOTE = "note";

    private static final int MSG_SAVE = 1;
    private static final int MSG_INIT = 2;
    private static final int MSG_SAVE_AND_EXIT = 3;

    private String mBook;
    private int mChapter;
    private String mNote;
    private boolean mChanged;

    private EditText mEditText;
    private String mFilePath;
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
        mChapter = getIntent().getIntExtra(INTENT_CHAPTER,0);
        mNote = getIntent().getStringExtra(INTENT_NOTE );

        if(mNote != null && !mNote.isEmpty()){
            Log.e("mytest","mNote = " + mNote);
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

        FloatingActionButton fab = findViewById(R.id.save);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mHandler.sendEmptyMessage(MSG_SAVE);
            }
        });

        mEditText = findViewById(R.id.edit);
        Log.e("mytest","filepath = " + mFilePath);
        File file = new File(mFilePath);
        if(!file.exists()){
            mEditText.addTextChangedListener(mTextWatch);
        } else {
            mHandler.sendEmptyMessage(MSG_INIT);
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
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
}
