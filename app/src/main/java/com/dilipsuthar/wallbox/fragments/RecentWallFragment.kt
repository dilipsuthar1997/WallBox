package com.dilipsuthar.wallbox.fragments

import android.os.Bundle
import android.view.*

import com.dilipsuthar.wallbox.R
import com.dilipsuthar.wallbox.WallBox
import com.dilipsuthar.wallbox.helpers.setRefresh
import com.dilipsuthar.wallbox.preferences.Prefs
/**
 * Modified by DILIP SUTHAR on 30/08/19
 */
class RecentWallFragment : BasePhotosFragment() {
    private val TAG = RecentWallFragment::class.java.simpleName

    companion object {
        fun newInstance(sort: String): RecentWallFragment {
            val fragment = RecentWallFragment()

            val args = Bundle()
            args.putString(Prefs.SORT, sort)
            fragment.arguments = args

            return fragment
        }
    }

    override fun getView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.fragment_recent_wall, container, false)
    }

    override fun loadPhotos(totalItem: Int) {
        mSwipeRefreshLayout setRefresh true
        loadMore = true
        if (snackBar != null) snackBar?.dismiss()
        mService?.requestPhotos(mPage, WallBox.DEFAULT_PER_PAGE, mSort!!, this)
    }
}
