package com.annazou.notebook.ui.main;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.annazou.notebook.R;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{R.string.tab_text_1, R.string.tab_text_2};
    private final Context mContext;

    BasicListFragment[] mFragments;
    BasicListFragment.ArrangeHost mHost;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm);
        mContext = context;
        mFragments = new BasicListFragment[getCount()];
    }

    @Override
    public Fragment getItem(int position) {
        // getItem is called to instantiate the fragment for the given page.
        // Return a PlaceholderFragment (defined as a static inner class below).
        if(position == 0) {
            mFragments[0] = NoteListFragment.newInstance(position + 1);
        }
        if(position == 1) {
            mFragments[1] = BookListFragment.newInstance(position + 1);
        }
        mFragments[position].setArrangeHost(mHost);
        return mFragments[position];
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 2 total pages.
        return 2;
    }

    public void setArrangeHost(BasicListFragment.ArrangeHost host){
        mHost = host;
    }

    public void enterArrangeMode() {
        for(BasicListFragment fragment : mFragments){
            fragment.enterArrangeMode();
        }
    }

    public void exitArrangeMode(boolean saveChange) {
        for(BasicListFragment fragment : mFragments){
            fragment.exitArrangeMode(saveChange);
        }
    }
}