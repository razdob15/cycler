package razdob.cycler.myUtils;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Raz on 31/05/2018, for project: PlacePicker2
 */
public class SectionsStatePagerAdapter extends FragmentStatePagerAdapter {

    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final HashMap<Fragment, Integer> mFragments = new HashMap<>();
    private final HashMap<String, Integer> mFragmentNumbers = new HashMap<>();
    private final HashMap<Integer, String> mFragmentNames = new HashMap<>();


    public SectionsStatePagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    public void addFragment(Fragment fragment, String fragmentName) {
        mFragmentList.add(fragment);
        final int pos = mFragmentList.size() - 1;
        mFragments.put(fragment, pos);
        mFragmentNumbers.put(fragmentName, pos);
        mFragmentNames.put(pos, fragmentName);
    }

    /**
     * Returns the fragment number with the name @param
     * @param fragmentName
     * @return Integer or null
     */
    public Integer getFragmentNumber(String fragmentName) {
        if (mFragmentNumbers.containsKey(fragmentName)) {
            return mFragmentNumbers.get(fragmentName);
        } else {
            return null;
        }
    }
    /**
     * Returns the fragment number with the name @param
     * @param fragment
     * @return Integer or null
     */
    public Integer getFragmentNumber(Fragment fragment) {
        if (mFragments.containsKey(fragment)) {
            return mFragments.get(fragment);
        } else {
            return null;
        }
    }
    /**
     * Returns the fragment number with the number @param
     * @param fragmentNumber
     * @return String or null
     */
    public String getFragmentName(Integer fragmentNumber) {
        if (mFragmentNames.containsKey(fragmentNumber)) {
            return mFragmentNames.get(fragmentNumber);
        } else {
            return null;
        }
    }
}
