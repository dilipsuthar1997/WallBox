package com.dilipsuthar.wallbox.fragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife

import com.dilipsuthar.wallbox.R
import com.dilipsuthar.wallbox.WallBox
import com.dilipsuthar.wallbox.activity.PhotoDetailActivity
import com.dilipsuthar.wallbox.adapters.PhotoAdapter
import com.dilipsuthar.wallbox.data.model.Photo
import com.dilipsuthar.wallbox.data.service.Services
import com.dilipsuthar.wallbox.helpers.setRefresh
import com.dilipsuthar.wallbox.preferences.Preferences
import com.dilipsuthar.wallbox.utils.*
import com.dilipsuthar.wallbox.utils.itemDecorater.VerticalSpacingItemDecorator
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Response

/**
 * Created by DILIP SUTHAR on 28/07/19
 */

class CuratedWallFragment : Fragment() {

    companion object {
        const val TAG = "Fragment.CuratedWall"
        fun newInstance(sort: String): CuratedWallFragment {
            val fragment = CuratedWallFragment()

            val args = Bundle()
            args.putString(Preferences.SORT, sort)
            fragment.arguments = args

            return fragment
        }
    }

    // VIEWS
    private var mService: Services? = null
    private var mOnRequestPhotosListener: Services.OnRequestPhotosListener? = null
    private var mPage: Int = 0
    private var mSort: String? = null
    private var mPhotosList: ArrayList<Photo> = ArrayList()
    private var mPhotoAdapter: PhotoAdapter? = null
    private var mOnItemClickListener: PhotoAdapter.OnItemClickListener? = null
    private var loadMore: Boolean = false
    private var snackBar: Snackbar? = null

    // VIEWS
    @BindView(R.id.curated_wallpaper_list) lateinit var mRecyclerView: RecyclerView
    @BindView(R.id.curated_swipe_refresh_layout) lateinit var mSwipeRefreshView: SwipeRefreshLayout
    @BindView(R.id.network_error_layout) lateinit var netWorkErrorLyt: View
    @BindView(R.id.http_error_layout) lateinit var httpErrorLyt: View

    /** MAIN METHOD */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mSort = arguments?.getString(Preferences.SORT, WallBox.DEFAULT_SORT_PHOTOS)

        /** SERVICES / API */
        mService = Services.getService()

        /** Listeners */
        // API request listener
        mOnRequestPhotosListener = object : Services.OnRequestPhotosListener {
            override fun onRequestPhotosSuccess(call: Call<List<Photo>>, response: Response<List<Photo>>) {
                Log.d(TAG, response.code().toString())
                mSwipeRefreshView setRefresh false
                if (!loadMore)  PopupUtils.showToast(context, "Your photos :)", Toast.LENGTH_SHORT)
                if (response.isSuccessful) {
                    mPage++
                    loadMore = false
                    mPhotosList.clear()
                    mPhotosList.addAll(ArrayList(response.body()!!))
                    updateAdapter(mPhotosList)
                    Tools.visibleViews(mRecyclerView)
                    Tools.inVisibleViews(netWorkErrorLyt, httpErrorLyt, type = Tools.GONE)
                } else {
                    mSwipeRefreshView setRefresh false
                    loadMore = false
                    if (mPhotosList.isEmpty()) {
                        Tools.visibleViews(httpErrorLyt)
                        Tools.inVisibleViews(mRecyclerView, netWorkErrorLyt, type = Tools.GONE)
                    } else PopupUtils.showHttpErrorSnackBar(mSwipeRefreshView) { load() }
                }
            }

            override fun onRequestPhotosFailed(call: Call<List<Photo>>, t: Throwable) {
                Log.d(WallBox.TAG, t.message!!)
                mSwipeRefreshView setRefresh false
                loadMore = false
                if (mPhotosList.isEmpty()) {
                    Tools.visibleViews(netWorkErrorLyt)
                    Tools.inVisibleViews(mRecyclerView, httpErrorLyt, type = Tools.GONE)
                } else PopupUtils.showNetworkErrorSnackBar(mSwipeRefreshView) { load() }
            }
        }

        // Adapter listener
        mOnItemClickListener = object : PhotoAdapter.OnItemClickListener {
            override fun onItemClick(photo: Photo, view: View, pos: Int, imageView: ImageView) {
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, imageView, ViewCompat.getTransitionName(imageView)!!)
                val intent = Intent(activity, PhotoDetailActivity::class.java)
                intent.putExtra(Preferences.PHOTO, Gson().toJson(photo))
                startActivity(intent, options.toBundle())
            }

            override fun onItemLongClick(photo: Photo, view: View, pos: Int, imageView: ImageView) {
                PopupUtils.showToast(context, "$pos", Toast.LENGTH_SHORT)
                Log.d(WallBox.TAG, "mOnItemClickListener: onItemLongClick")
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Log.d(TAG, "onCreateView: called >>>>>")

        retainInstance = true
        val view = inflater.inflate(R.layout.fragment_curated_wall, container, false)
        ButterKnife.bind(this, view)

        /** Recycler View */
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.addItemDecoration(VerticalSpacingItemDecorator(22))
        mRecyclerView.setItemViewCacheSize(5)
        mPhotoAdapter = PhotoAdapter(ArrayList(), "list", context, mOnItemClickListener)
        mRecyclerView.adapter = mPhotoAdapter

        mPage = 1
        load()

        /** Views listeners */
        // RecyclerView listener
        mRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {

            var verticalOffset: Int = 0
            var scrollingUp: Boolean = false

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

                /** check for first & last item position */
                val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                val totalItem = layoutManager.itemCount
                val lastVisible = layoutManager.findLastCompletelyVisibleItemPosition()
                val firstVisible = layoutManager.findFirstCompletelyVisibleItemPosition()

                val atTopReached = firstVisible.minus(1) <= 0    // Hide fab on first item
                if (atTopReached) {
                    // TODO: hide fabScrollUp here
                }

                val endHasBeenReached = lastVisible.plus(1) >= totalItem   // Load more photos on last item
                if (totalItem > 0 && endHasBeenReached && !loadMore) {
                    //loadMore = true
                    load()
                }

                verticalOffset.plus(dy)
                scrollingUp = dy > 0

            }

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    if (scrollingUp) {
                        // TODO: hide fabScrollUp here
                    } else {
                        // TODO: show fabScrollUp here
                    }
                }
            }

        })


        // Swipe refresh listener
        mSwipeRefreshView.setOnRefreshListener {
            mPage = 1
            mPhotosList.clear()
            load()
            mPhotoAdapter = PhotoAdapter(ArrayList(), "list", context, mOnItemClickListener)
            mRecyclerView.adapter = mPhotoAdapter
        }

        /** Event listener */
        netWorkErrorLyt.setOnClickListener {
            load()
            it.visibility = View.GONE
        }
        httpErrorLyt.setOnClickListener {
            load()
            it.visibility = View.GONE
        }

        return view
    }

    /** Methods */
    private fun load() {
        Log.d(TAG, "load: called >>>>>>>>>>")
        mSwipeRefreshView setRefresh true
        loadMore = true
        if (snackBar != null) snackBar?.dismiss()
        mService?.requestCuratedPhotos(mPage, WallBox.DEFAULT_PER_PAGE, mSort!!, mOnRequestPhotosListener)
    }

    private fun loadMore() {
        Log.d(TAG, "loadMore: called >>>>>>>>>>")
        mService?.requestCuratedPhotos(mPage, WallBox.DEFAULT_PER_PAGE, mSort!!, mOnRequestPhotosListener)
    }

    private fun updateAdapter(photos: ArrayList<Photo>) {
        Log.d(TAG, "updateAdapter: called >>>>>>>>>>")
        mPhotoAdapter?.addAll(photos)
    }

    /*fun scrollToTop() {
        val layoutManager = mRecyclerView.layoutManager as LinearLayoutManager
        if (mRecyclerView != null) {
            if (layoutManager != null && layoutManager.findFirstVisibleItemPosition() > 5) {
                mRecyclerView.scrollToPosition(5)
            }
            mRecyclerView.smoothScrollToPosition(0)
        }

    }*/

}
