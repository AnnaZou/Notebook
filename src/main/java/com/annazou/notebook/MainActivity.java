package com.annazou.notebook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.annazou.notebook.ui.main.BasicListFragment;
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

public class MainActivity extends AppCompatActivity implements BasicListFragment.ArrangeHost {

    SectionsPagerAdapter mPagerAdapter;
    FloatingActionButton mFab;
    boolean mIsArrangeMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        mPagerAdapter.setArrangeHost(this);
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(mPagerAdapter);
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
                            intent.putExtra(EditActivity.INTENT_NOTE,Utils.getNewNoteName());
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
        MenuItem store = menu.findItem(R.id.store);
        MenuItem arrangeMode = menu.findItem(R.id.arrange);
        store.setVisible(mIsArrangeMode ? true :false);
        arrangeMode.setVisible(mIsArrangeMode ? false : true);
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
        if (item.getItemId() == R.id.store){
            exitArrangeMode(true);
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAddBookDialog(){
        final InputDialog addDialog = new InputDialog(this, "Add new book");
        InputDialog.Callbacks callback = new InputDialog.Callbacks() {
            @Override
            public void onInputChanged(InputDialog dialog, CharSequence s) {
                addDialog.setWarnText("");
            }

            @Override
            public void onPositiveClicked(InputDialog dialog) {
                String newName = addDialog.getInputText();
                if (newName.isEmpty()) {
                    addDialog.setWarnText("Book name can't be empty.");
                    return;
                }
                if (Utils.checkBookNameExists(MainActivity.this, newName)) {
                    addDialog.setWarnText("Already exist");
                    return;
                }

                Intent intent = new Intent(MainActivity.this, BookActivity.class);
                intent.putExtra(BookActivity.INTENT_BOOK, newName);
                dialog.getDialog().dismiss();
                startActivity(intent);
            }

            @Override
            public void onNegativeClicked(InputDialog dialog) {
                dialog.getDialog().dismiss();
            }
        };
        addDialog.setCallback(callback);
        addDialog.getDialog().show();
    }

    public void enterArrangeMode(){
        mIsArrangeMode = true;
        mFab.hide();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle("Arrange mode");
        mPagerAdapter.enterArrangeMode();
        invalidateOptionsMenu();

    }

    public void exitArrangeMode(boolean saveChange){
        if(!mIsArrangeMode) return;
        mIsArrangeMode = false;
        mFab.show();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(false);
        actionBar.setTitle(getString(R.string.app_name));
        mPagerAdapter.exitArrangeMode(saveChange);
        invalidateOptionsMenu();
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

    @Override
    public void onBackPressed() {
        if(mIsArrangeMode){
            showSaveChangeDialog();
        } else {
            super.onBackPressed();
        }
    }
}