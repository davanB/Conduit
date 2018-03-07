package ca.uwaterloo.fydp.conduit.conduitview

import android.os.Parcelable
import android.support.v4.view.PagerAdapter
import android.support.v4.view.ViewPager
import android.view.View
import android.widget.RelativeLayout
import kotlin.properties.Delegates

class ViewPagerAdapter : PagerAdapter() {
    var conduitStatusView: ConduitStatusView by Delegates.notNull()
    var conduitListView: ConduitListView by Delegates.notNull()

    override fun instantiateItem(collection: View?, position: Int): Any {
        val view: RelativeLayout = when (position) {
            0 -> conduitStatusView
            else -> conduitListView
        }

        (collection as ViewPager).addView(view, 0)

        return view
    }

    override fun isViewFromObject(view: View?, other: Any?): Boolean {
        return view === other as View
    }

    override fun getCount(): Int {
        return 2
    }

    override fun destroyItem(arg0: View?, arg1: Int, arg2: Any?) {
        (arg0 as ViewPager).removeView(arg2 as View?)

    }

    override fun saveState(): Parcelable? {
        return null
    }

}