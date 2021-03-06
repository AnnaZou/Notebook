package com.annazou.notebook.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.annazou.notebook.R;

/**
 * A placeholder fragment containing a simple view.
 */
public class BasicListFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";

    protected PageViewModel pageViewModel;
    protected boolean mIsArranging;

    protected static BasicListFragment newInstance(int index) {
        BasicListFragment fragment = new BasicListFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        pageViewModel = ViewModelProviders.of(this).get(PageViewModel.class);
        int index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
        pageViewModel.setIndex(index);
    }

    private ArrangeHost mArrangeHost;

    public interface ArrangeHost{
        void enterArrangeMode();
        void exitArrangeMode(boolean saveChange);
    }

    public void setArrangeHost(ArrangeHost host){
        mArrangeHost = host;
    }

    public ArrangeHost getArrangeHost(){
        return mArrangeHost;
    }

    public void enterArrangeMode(){
        mIsArranging = true;
    }

    public void exitArrangeMode(boolean saveChange){
        mIsArranging = false;
    }

    public boolean isInArrangeMode(){
        return mIsArranging;
    }


}