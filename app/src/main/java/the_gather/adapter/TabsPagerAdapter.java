package the_gather.adapter;

import com.keenan.gather.ContactsFragment;
import com.keenan.gather.HomeFragment;
import com.keenan.gather.ProfileFragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class TabsPagerAdapter extends FragmentPagerAdapter {

	public TabsPagerAdapter(FragmentManager fm) {
		super(fm);
	}

	@Override
	public Fragment getItem(int index) {

		switch (index) {
		case 0:

			return new HomeFragment();
		case 1:

			return new ContactsFragment();
		case 2:

			return new ProfileFragment();
		}

		return null;
	}

	@Override
	public int getCount() {
		// get item count - equal to number of tabs
		return 3;
	}

}
