package de.gutekunst.florian.hgv.activities;

import android.os.*;
import android.support.annotation.*;
import android.support.design.widget.*;
import android.support.v4.app.*;
import android.support.v4.view.*;
import android.view.*;

import de.gutekunst.florian.hgv.*;

public class TermineFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private int selected = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_termine, container, false);

        //TabBar erstellen
        
        FragmentStatePagerAdapter adapter = new FragmentStatePagerAdapter(getActivity().getSupportFragmentManager()) {
            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new TermineSchulaufgabenFragment();
                    case 1:
                        return new TermineAllgemeinFragment();
                    default:
                        return new TermineSchulaufgabenFragment();
                }
            }

            @Override
            public int getCount() {
                return 2;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                switch (position) {
                    case 0:
                        return "Schulaufgaben";
                    case 1:
                        return "Allgemein";
                    default:
                        return "Schulaufgaben";
                }
            }
        };

        viewPager = (ViewPager) view.findViewById(R.id.termine_vp);

        viewPager.setAdapter(adapter);

        tabLayout = (TabLayout) view.findViewById(R.id.termine_tl);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                tabLayout.getTabAt(tab.getPosition()).select();
                selected = tab.getPosition();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        tabLayout.setupWithViewPager(viewPager);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (savedInstanceState == null) {
            return;
        }

        int pos = savedInstanceState.getInt("selected", 0);
        viewPager.setCurrentItem(pos);
        tabLayout.getTabAt(pos).select();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt("selected", selected);

        super.onSaveInstanceState(outState);
    }
}
