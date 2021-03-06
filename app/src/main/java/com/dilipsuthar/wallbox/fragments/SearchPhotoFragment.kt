package com.dilipsuthar.wallbox.fragments


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityOptionsCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import butterknife.BindView
import butterknife.ButterKnife

import com.dilipsuthar.wallbox.R
import com.dilipsuthar.wallbox.WallBox
import com.dilipsuthar.wallbox.activity.PhotoDetailActivity
import com.dilipsuthar.wallbox.activity.ProfileActivity
import com.dilipsuthar.wallbox.adapters.PhotoAdapter
import com.dilipsuthar.wallbox.data_source.model.Photo
import com.dilipsuthar.wallbox.data_source.model.SearchPhotos
import com.dilipsuthar.wallbox.data_source.service.Services
import com.dilipsuthar.wallbox.helpers.setRefresh
import com.dilipsuthar.wallbox.preferences.Prefs
import com.dilipsuthar.wallbox.utils.PopupUtils
import com.dilipsuthar.wallbox.utils.ThemeUtils
import com.dilipsuthar.wallbox.utils.Tools
import com.dilipsuthar.wallbox.utils.itemDecorater.VerticalSpacingItemDecorator
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.mikhaellopez.circularimageview.CircularImageView
import retrofit2.Call
import retrofit2.Response
/**
 * Created by DILIP SUTHAR on 31/08/19
 */
class SearchPhotoFragment : Fragment() {
    private val TAG = "WallBox.SearchPhoto"

    companion object {
        fun newInstance(query: String?): SearchPhotoFragment {
            val fragment = SearchPhotoFragment()

            val args = Bundle()
            args.putString(Prefs.SEARCH_QUERY, query)
            fragment.arguments = args

            return fragment
        }
    }

    private var mQuery: String? = null
    private lateinit var mService: Services
    private lateinit var mOnSearchPhotosListener: Services.OnSearchPhotosListener
    private var mPage = 0
    private lateinit var mPhotoAdapter: PhotoAdapter
    private lateinit var mOnItemClickListener: PhotoAdapter.OnItemClickListener
    private var mPhotosList: ArrayList<Photo> = ArrayList()
    private var loadMore: Boolean = false
    private var snackBar: Snackbar? = null

    @BindView(R.id.recycler_view) lateinit var mRecyclerView: RecyclerView
    @BindView(R.id.swipe_refresh_layout) lateinit var mSwipeRefreshLayout: SwipeRefreshLayout
    @BindView(R.id.network_error_layout) lateinit var lytNetworkError: LinearLayout
    @BindView(R.id.http_error_layout) lateinit var lytHttpError: LinearLayout
    @BindView(R.id.no_items_layout) lateinit var lytNoItems: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mQuery = arguments!!.getString(Prefs.SEARCH_QUERY, "")

        /** SERVICES / API */
        mService = Services.getService()

        /** Listeners */
        // API request listener
        mOnSearchPhotosListener = object : Services.OnSearchPhotosListener {
            override fun onSearchPhotoSuccess(call: Call<SearchPhotos>, response: Response<SearchPhotos>) {

                Log.d(TAG, response.code().toString())
                mSwipeRefreshLayout setRefresh false
                if (!loadMore) PopupUtils.showToast(context, "Your photos :)", Toast.LENGTH_SHORT)
                if (response.code() == 200) {
                    if (response.body()!!.results.isNotEmpty()) {
                        mPage++
                        loadMore = false
                        mPhotosList.clear()
                        mPhotosList.addAll(ArrayList(response.body()!!.results))
                        updateAdapter(mPhotosList)
                        mRecyclerView.smoothScrollToPosition(mPhotoAdapter.itemCount.minus(mPhotosList.size))
                        Tools.visibleViews(mRecyclerView)
                        Tools.inVisibleViews(lytNetworkError, lytHttpError, lytNoItems, type = Tools.GONE)
                    } else if (response.body()!!.results.isEmpty() && mPhotosList.isEmpty()) {
                        Tools.visibleViews(lytNoItems)
                    }
                } else {
                    mSwipeRefreshLayout setRefresh false
                    loadMore = false
                    if (mPhotosList.isEmpty()) {
                        Tools.visibleViews(lytHttpError)
                        Tools.inVisibleViews(mRecyclerView, lytNetworkError, lytNoItems, type = Tools.GONE)
                    } else snackBar = PopupUtils.showHttpErrorSnackBar(mSwipeRefreshLayout) { load() }
                }

            }

            override fun onSearchPhotoFailed(call: Call<SearchPhotos>, t: Throwable) {

                Log.d(TAG, t.message!!)
                mSwipeRefreshLayout setRefresh false
                loadMore = false
                if (mPhotosList.isEmpty()) {
                    Tools.visibleViews(lytNetworkError)
                    Tools.inVisibleViews(mRecyclerView, lytHttpError, lytNoItems, type = Tools.GONE)
                } else snackBar = PopupUtils.showNetworkErrorSnackBar(mSwipeRefreshLayout) { load() }

            }
        }

        // Adapter listener
        mOnItemClickListener = object : PhotoAdapter.OnItemClickListener {
            override fun onItemClick(photo: Photo, view: View, pos: Int, imageView: ImageView) {
                //onPhotoClick(photo, view, pos, imageView)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, imageView, ViewCompat.getTransitionName(imageView)!!)
                val intent = Intent(activity, PhotoDetailActivity::class.java)
                intent.putExtra(Prefs.PHOTO, Gson().toJson(photo))
                startActivity(intent, options.toBundle())
            }

            override fun onItemLongClick(photo: Photo, view: View, pos: Int, imageView: ImageView) {
                //onPhotoLongClick(photo, view, pos, imageView)
                PopupUtils.showToast(context, "${pos.plus(1)}", Toast.LENGTH_SHORT)
                Log.d(WallBox.TAG, "mOnItemClickListener: onItemLongClick")
            }

            override fun onUserProfileClick(photo: Photo, pos: Int, imgPhotoBy: CircularImageView) {
                //onPhotoUserProfileClick(photo, pos, imgPhotoBy)
                val options = ActivityOptionsCompat.makeSceneTransitionAnimation(activity!!, imgPhotoBy, ViewCompat.getTransitionName(imgPhotoBy)!!)
                val intent = Intent(activity, ProfileActivity::class.java)
                intent.putExtra(Prefs.USER, Gson().toJson(photo.user))
                startActivity(intent, options.toBundle())
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        retainInstance = true
        val view =  inflater.inflate(R.layout.fragment_search_photo, container, false)
        ButterKnife.bind(this, view)

        /** Recycler View */
        mRecyclerView.layoutManager = LinearLayoutManager(context)
        mRecyclerView.setHasFixedSize(true)
        mRecyclerView.addItemDecoration(VerticalSpacingItemDecorator(22))
        mRecyclerView.setItemViewCacheSize(5)
        mPhotoAdapter = PhotoAdapter(ArrayList(), "list", context, activity, mOnItemClickListener)
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
                if (atTopReached) run {
                    // TODO: hide fabScrollUp here
                }

                val endHasBeenReached = lastVisible.plus(1) >= totalItem   // Load more photos on last item
                if (totalItem > 0 && endHasBeenReached && !loadMore) {
                    //loadMore = true
                    load()
                }

                verticalOffset += dy
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
        mSwipeRefreshLayout.setProgressBackgroundColorSchemeColor(ThemeUtils.getThemeAttrColor(context!!, R.attr.colorPrimary))
        mSwipeRefreshLayout.setColorSchemeColors(ContextCompat.getColor(context!!, R.color.white))
        mSwipeRefreshLayout.setOnRefreshListener {
            mPage = 1
            mPhotosList.clear()
            load()
            mPhotoAdapter = PhotoAdapter(ArrayList(), "list", context, activity, mOnItemClickListener)
            mRecyclerView.adapter = mPhotoAdapter
        }

        /** Event listener */
        lytNetworkError.setOnClickListener {
            load()
            it.visibility = View.GONE
        }
        lytHttpError.setOnClickListener {
            load()
            it.visibility = View.GONE
        }

        return view
    }

    /** Methods */
    private fun load() {
        if (mQuery != "") {
            Tools.inVisibleViews(lytNoItems, type = Tools.GONE)
            mSwipeRefreshLayout setRefresh true
            loadMore = true
            if (snackBar != null) snackBar?.dismiss()
            mService.searchPhotos(mQuery!!, mPage, WallBox.DEFAULT_PER_PAGE, null, mOnSearchPhotosListener)
        } else {
            mSwipeRefreshLayout setRefresh false
        }
    }

    private fun updateAdapter(photos: ArrayList<Photo>) {
        mPhotoAdapter.addAll(photos)
    }

}
