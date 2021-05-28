package ru.vsu.csf.pchernyshov.photogallery;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import ru.vsu.csf.pchernyshov.photogallery.service.SaveSharedPreference;

public class OnboardingActivity extends AppCompatActivity {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private static final int[] descriptions = {R.string.onboarding_desc_1, R.string.onboarding_desc_2, R.string.onboarding_desc_3, R.string.onboarding_desc_4};
    private static final int[] indicators = {R.id.intro_indicator_0, R.id.intro_indicator_1, R.id.intro_indicator_2, R.id.intro_indicator_3};
    private static final int[] photos = {R.drawable.onboarding_1, R.drawable.onboarding_2, R.drawable.onboarding_3, R.drawable.onboarding_4};

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        ImageButton mNextBtn = findViewById(R.id.intro_btn_next);
        Button mFinishBtn = findViewById(R.id.intro_btn_finish);
        Button mSkipBtn = findViewById(R.id.intro_btn_skip);

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                ImageView ind = findViewById(indicators[position]);
                ind.setImageResource(R.drawable.indicator_selected);
                for (int i = 0; i < indicators.length; i++) {
                    if (i != position) {
                        ind = findViewById(indicators[i]);
                        ind.setImageResource(R.drawable.indicator_unselected);
                    }
                }
                mNextBtn.setVisibility(position == indicators.length - 1 ? View.GONE : View.VISIBLE);
                mFinishBtn.setVisibility(position == indicators.length - 1 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mNextBtn.setOnClickListener(view -> mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1));
        mFinishBtn.setOnClickListener(view -> finishOnboarding());
        mSkipBtn.setOnClickListener(view -> finishOnboarding());
    }

    private void finishOnboarding() {
        SaveSharedPreference.onboardingDone(getApplicationContext());
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_onboarding, container, false);
            TextView textView = rootView.findViewById(R.id.onboarding_text);
            textView.setText(getString(descriptions[getArguments().getInt(ARG_SECTION_NUMBER)]));
            ImageView imageView = rootView.findViewById(R.id.onboarding_photo);
            imageView.setImageResource(photos[getArguments().getInt(ARG_SECTION_NUMBER)]);
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            return PlaceholderFragment.newInstance(position);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return indicators.length;
        }
    }
}
