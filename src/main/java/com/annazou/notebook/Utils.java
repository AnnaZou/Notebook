package com.annazou.notebook;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Utils {
    public static final String TAG = "Notebook";
    public static final String DIR_NOTE = "notes";
    public static final String DIR_BOOK = "books";

    public static final int MAX_KEY_WORD_LENGTH = 20;

    private static final SimpleDateFormat SDF = new SimpleDateFormat("yy/MM/dd");

    public static String getNoteDirPath(Context context){
        return context.getFilesDir().getAbsolutePath() + "/" + DIR_NOTE;
    }

    public static String getBookDirPath(Context context){
        return context.getFilesDir().getAbsolutePath() + "/" + DIR_BOOK;
    }

    public static File getBookDir(Context context, String name){
        File dir = new File(getBookDirPath(context) + "/" + name);
        return dir;
    }

    public static String getNewFileName(){
        Date date = new Date();
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddhhmmss");
        return format.format(date);
    }

    public static String getChapterFilePath(Context context, String book, String chapter){
        return getBookDirPath(context) + "/" + book + "/" + chapter;
    }

    public static boolean checkBookNameExists(Context context, String name){
        File dir = new File(getBookDirPath(context));
        if(dir.exists()){
            String[] files = dir.list();
            for(String file : files){
                if(name.equals(file)) return true;
            }
        }
        return false;
    }

    public static boolean writeFile(String filePath, String content){
        try {
            FileOutputStream outputStream = new FileOutputStream(new File(filePath));
            outputStream.write(content.getBytes());
            outputStream.close();
            return true;
        } catch (java.io.FileNotFoundException e) {
            Log.d(TAG, "The File doesn't exist.");
        } catch (IOException e) {
            Log.d(TAG, e.getMessage());
        }
        return false;
    }

    public static String readFile(String path){
        StringBuilder sb = new StringBuilder("");
        try {
            FileInputStream inputStream = new FileInputStream(path);
            byte[] buffer = new byte[1024];
            int len = inputStream.read(buffer);
            while(len > 0){
                sb.append(new String(buffer,0,len));
                len = inputStream.read(buffer);
            }
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }

        String result = sb.toString();
        return result;
    }

    public static void deleteFile(String path){
        File file = new File(path);
        if (file.exists()) file.delete();
    }

    public static void renameFile(String dir,String oldName, String newName){
        File file = new File(dir + "/" + oldName);
        if (file.exists()){
            file.renameTo(new File(dir + "/" + newName));
        }
    }

    public static String getFileThumbTitle(String filePath){
        StringBuilder sb = new StringBuilder("");
        try {
            FileInputStream inputStream = new FileInputStream(filePath);
            byte[] buffer = new byte[64];
            int len = inputStream.read(buffer);
            sb.append(new String(buffer,0,len));
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
        String tmp = sb.toString();
        String[] title = tmp.split("\n");
        return title[0];
    }

    public static String getFileDate(File file){
        String date = "--/--/--";
        if(file.exists()){
            date = getDate(file.lastModified());
        }
        return date;
    }

    public static String getDate(long time){
        String date = "--/--/--";
        Date lastModify = new Date(time);
        date = SDF.format(lastModify);
        return date;
    }

    public List<File> searchBookContent(Context context, String key){
        if(key.length() > MAX_KEY_WORD_LENGTH) key = key.substring(0,MAX_KEY_WORD_LENGTH);
        File dir = new File(getBookDirPath(context));
        File[] books = dir.listFiles();
        List<File> matchedBook = new ArrayList<>();
        for(File book : books){
            boolean matched = false;
            for (File chapter : book.listFiles()){
                matched = checkContentContains(chapter.getAbsolutePath(), key);
                if(matched) break;
            }
            if (matched){
                matchedBook.add(book);
            }
        }
        return matchedBook;
    }

    public boolean checkNoteContains(String notePath, String key){
        if(key.length() > MAX_KEY_WORD_LENGTH) key = key.substring(0,MAX_KEY_WORD_LENGTH);
        return checkContentContains(notePath, key);
    }

    private boolean checkContentContains(String path, String key){
        boolean matched = false;
        int length = key.length() * 2;
        try {
            FileInputStream inputStream = new FileInputStream(path);
            byte[] buffer = new byte[100];
            String content = "";
            int len = inputStream.read(buffer);
            while(len > 0){
                content += new String(buffer,0,len);
                if(content.contains(key)){
                    matched = true;
                    break;
                }
                content = content.substring(len - length > 0 ? len - length : 0 ,len);
                len = inputStream.read(buffer);
            }
            inputStream.close();
        } catch (Exception e) {
            Log.e(TAG, "Error on write File:" + e);
        }
        return matched;
    }

}
