package com.example.techbharsolutiongndechub;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class ViewPagerAdapter extends FragmentStateAdapter {

    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {

        switch (position) {

            case 0:
                return new homefragment();

            case 1:
                return new livefragment();

            case 2:
                return new whatsappfragment();

            case 3:
                return new notificationfragment();

            default:
                return new homefragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4; // total fragments
    }
}