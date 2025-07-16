package com.nibm.tutionmanagement;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class CoursePagerAdapter extends FragmentStateAdapter {

    public CoursePagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new StartCourseFragment();
            case 1:
                //return new UpdateCourseFragment(); // You need to create this fragment
            default:
                //return new StartCourseFragment();
        }
        return null;
    }

    @Override
    public int getItemCount() {
        return 2; // Two tabs: Start Course and Update Course
    }
}
