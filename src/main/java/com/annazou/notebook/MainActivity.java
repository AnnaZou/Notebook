package com.annazou.notebook;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.annazou.notebook.ui.main.SectionsPagerAdapter;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SectionsPagerAdapter sectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager());
        ViewPager viewPager = findViewById(R.id.view_pager);
        viewPager.setAdapter(sectionsPagerAdapter);
        TabLayout tabs = findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);
        FloatingActionButton fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
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

                        }
                    }
                }).show();
            }
        });
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
}