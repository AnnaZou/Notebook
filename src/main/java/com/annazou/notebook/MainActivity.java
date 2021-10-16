package com.annazou.notebook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.Toolbar;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.annazou.notebook.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton mFab;
    boolean mIsArrangeMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        mFab = findViewById(R.id.fab);

        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setItems(new String[]{"new note", "new book"}, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(which == 0){
                            Intent intent = new Intent(MainActivity.this, EditActivity.class);
                            int notes = Utils.getNotesCount(MainActivity.this);
                            intent.putExtra(EditActivity.INTENT_NOTE,notes + 1);
                            startActivity(intent);
                        }else if (which == 1){
                            showAddBookDialog();
                        }
                    }
                }).show();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(item.getItemId() == R.id.arrange){
            if(!mIsArrangeMode){
                enterArrangeMode();
            }
            return true;
        }
        if(item.getItemId() == android.R.id.home){
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddBookDialog(){
        final View addView = LayoutInflater.from(this).inflate(R.layout.add_book_dialog, null);
        final EditText newNameView = (EditText) addView.findViewById(R.id.add_book_text);
        final TextView tip = (TextView) addView.findViewById(R.id.add_book_tip);

        newNameView.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                tip.setText("");
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Add new book")
                .setView(addView)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String newName = newNameView.getText().toString();
                if (newName.isEmpty()) {
                    tip.setText("Book name can't be empty.");
                    return;
                }
                if (Utils.checkBookNameExists(MainActivity.this, newName)) {
                    tip.setText("Already exist");
                    return;
                }

                Intent intent = new Intent(MainActivity.this, BookActivity.class);
                intent.putExtra(BookActivity.INTENT_BOOK, newName);
                dialog.dismiss();
                startActivity(intent);

            }
        }).show();
    }

    private void enterArrangeMode(){
        mIsArrangeMode = true;
        mFab.hide();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

    }

    private void exitArrangeMode(){
        mIsArrangeMode = false;
        mFab.show();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
    }

    @Override
    public void onBackPressed() {
        if(mIsArrangeMode){
            exitArrangeMode();
        } else {
            super.onBackPressed();
        }
    }
}