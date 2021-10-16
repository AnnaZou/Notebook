package com.annazou.notebook;

import androidx.appcompat.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class VerifyUtils {
    private static final String SP_NAME = "notebook";

    public static boolean isBookLocked(Context context, String book){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String correct = sp.getString(book, "");
        return correct.isEmpty() ? false : true;
    }

    public static void setBookPassword(Context context, String book, String password){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putString(book, password);
        editor.commit();
    }

    public static void removeBookPassword(Context context, String book){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.remove(book);
        editor.commit();
    }

    public static boolean verifyBookPassword(Context context, String book, String password){
        SharedPreferences sp = context.getSharedPreferences(SP_NAME, Context.MODE_PRIVATE);
        String correct = sp.getString(book, "");
        if(correct.equals(password)){
            return true;
        }
        return false;
    }

    public static void showSetPasswordDialog(final Context context, final String book){
        final View addView = LayoutInflater.from(context).inflate(R.layout.dialog_set_password, null);
        final EditText password1 = (EditText) addView.findViewById(R.id.password_1);
        final EditText password2 = (EditText) addView.findViewById(R.id.password_2);
        final TextView tip = (TextView) addView.findViewById(R.id.set_password_tip);

        password1.addTextChangedListener(new TextWatcher() {
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
        password2.addTextChangedListener(new TextWatcher() {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set password of " + book)
                .setView(addView)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pwd1 = password1.getText().toString();
                String pwd2 = password2.getText().toString();
                if (pwd1.isEmpty()) {
                    tip.setText("Password can't be empty.");
                    return;
                }
                if (!pwd1.equals(pwd2)) {
                    tip.setText("Different password");
                    return;
                }

                setBookPassword(context, book, pwd1);
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }

    public interface VerifyCallback {
        void onVerifyResult(boolean successed, String book);
    }

    public static void showVerifyDialog(final Context context, final String book, final VerifyCallback callback){
        final View addView = LayoutInflater.from(context).inflate(R.layout.dialog_verify, null);
        final EditText password = (EditText) addView.findViewById(R.id.password);
        final TextView tip = (TextView) addView.findViewById(R.id.verify_tip);

        password.addTextChangedListener(new TextWatcher() {
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

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Password of " + book)
                .setView(addView)
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        return;
                    }
                }).setPositiveButton("ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String pwd = password.getText().toString();
                if (pwd.isEmpty()) {
                    tip.setText("Password can't be empty.");
                    return;
                }
                if (!verifyBookPassword(context, book, pwd)) {
                    tip.setText("Wrong password");
                    return;
                }
                callback.onVerifyResult(true, book);
                dialog.dismiss();
            }
        }).show();
    }
}
