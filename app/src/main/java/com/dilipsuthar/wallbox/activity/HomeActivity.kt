package com.dilipsuthar.wallbox.activity

import android.content.Intent
import android.content.SharedPreferences
import android.graphics.PorterDuff
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentTransaction
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import androidx.viewpager.widget.ViewPager
import butterknife.BindView
import butterknife.ButterKnife
import com.afollestad.materialdialogs.MaterialDialog
import com.dilipsuthar.wallbox.R
import com.dilipsuthar.wallbox.adapters.SectionPagerAdapter
import com.dilipsuthar.wallbox.data_source.managers.AuthManager
import com.dilipsuthar.wallbox.fragments.*
import com.dilipsuthar.wallbox.helpers.LocaleHelper
import com.dilipsuthar.wallbox.helpers.loadUrl
import com.dilipsuthar.wallbox.preferences.Prefs
import com.dilipsuthar.wallbox.preferences.SharedPref
import com.dilipsuthar.wallbox.utils.PopupUtils
import com.dilipsuthar.wallbox.utils.ThemeUtils
import com.dilipsuthar.wallbox.utils.Tools
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.tabs.TabLayout
import com.mikhaellopez.circularimageview.CircularImageView
import java.util.*
/**
 * Created by DILIP SUTHAR 05/06/19
 */
class HomeActivity : BaseActivity(), SharedPreferences.OnSharedPreferenceChangeListener,
    AuthManager.OnAuthStateChangeListener {
    private val TAG = HomeActivity::class.java.simpleName

    @BindView(R.id.toolbar) lateinit var mToolbar: Toolbar
    @BindView(R.id.tab_layout) lateinit var mTabLayout: TabLayout
    @BindView(R.id.view_pager) lateinit var mViewPager: ViewPager
    @BindView(R.id.nav_view) lateinit var mNavigationView: NavigationView
    @BindView(R.id.drawer_layout) lateinit var mDrawerLayout: DrawerLayout
    @BindView(R.id.root_coordinator_layout) lateinit var mRootView: View
    /*@BindView(R.id.fab_scroll_to_top) lateinit var mFabScrollUp: FloatingActionButton*/

    private var mViewPagerAdapter: SectionPagerAdapter? = null
    private lateinit var currentLanguage: Locale
    private lateinit var currentTheme: String

    // Wallpaper sort menu
    private var mSortRecentLatest: MenuItem? = null
    private var mSortRecentOldest: MenuItem? = null
    private var mSortRecentPopular: MenuItem? = null
    private var mSortCuratedLatest: MenuItem? = null
    private var mSortCuratedOldest: MenuItem? = null
    private var mSortCuratedPopular: MenuItem? = null
    private var mSortCollectionAll: MenuItem? = null
    private var mSortCollectionFeatured: MenuItem? = null
    private var mSortCollectionCurated: MenuItem? = null

    // vars
    private var actionSortMenu: MenuItem? = null
    private var _isAccountMode = false
    private lateinit var _menuNavigation: Menu

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)
        ButterKnife.bind(this)
        // fabScrollUp = mFabScrollUp

        currentLanguage = LocaleHelper.getLocale(this)
        currentTheme = ThemeUtils.getTheme(this)

        initToolbar()
        initTabLayout()
        initNavigationDrawer()

        /** Fab scrollToUp listener */
        /*mFabScrollUp.setOnClickListener {
            val fragment: Fragment?
            when (mViewPager.currentItem) {
                0 -> {
                    fragment = mViewPagerAdapter?.getItem(0)
                    if (fragment is RecentWallFragment)
                        (fragment as RecentWallFragment).scrollToTop()
                }
            }
        }*/

        /** Bottom sheet */
        /*bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetSearch)
        bottomSheetBehavior.setBottomSheetCallback(object: BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {}

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                bottomSheet.setOnTouchListener { _, motionEvent ->

                    when (MotionEventCompat.getActionMasked(motionEvent)) {
                        MotionEvent.ACTION_DOWN -> false
                        else -> true
                    }
                }
            }
        })
        btnCloseSheet.setOnClickListener {
            if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        }*/

    }

    override fun onStart() {
        super.onStart()
        SharedPref.getInstance(this).getSharedPreferences().registerOnSharedPreferenceChangeListener(this)
        AuthManager.getInstance().addOnAuthStateChangeListener(this)
        if (AuthManager.getInstance().isAuthorized() && TextUtils.isEmpty(AuthManager.getInstance().getUsername())) {
            AuthManager.getInstance().requestUserProfileData()
        }
    }

    override fun onResume() {
        super.onResume()
        setDrawerHeader(mNavigationView.getHeaderView(0))
    }

    override fun onBackPressed() {
        super.onBackPressed()
        /*if (bottomSheetBehavior.state == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.state = BottomSheetBehavior.STATE_COLLAPSED
        else super.onBackPressed()*/
    }

    /*override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.loadLocal(this)
        recreate()
    }*/

    override fun onDestroy() {
        super.onDestroy()
        SharedPref.getInstance(this).getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this)
        AuthManager.getInstance().removeOnAuthStateChangeListener()
        AuthManager.getInstance().cancelRequests()
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        if (key == Prefs.THEME || key == Prefs.LANGUAGE) {
            mDrawerLayout.closeDrawers()
            recreate()
        }
    }

    /** @method init toolbar settings */
    private fun initToolbar() {
        setSupportActionBar(mToolbar)
        val actionBar = supportActionBar
        actionBar?.title = resources.getString(R.string.app_name)
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        Tools.changeNavigationIconColor(mToolbar, ThemeUtils.getThemeAttrColor(this, R.attr.tabUnselectedColor))
    }

    /** @method init tab layout settings */
    private fun initTabLayout() {
        // Main tab layout
        /** Set mViewPager and link it with TabLayout */
        mViewPagerAdapter = SectionPagerAdapter(supportFragmentManager)
        with(mViewPagerAdapter!!) {
            addFragment(RecentWallFragment.newInstance("latest"), resources.getString(R.string.tab_title_recent_wall_fragment))
            //addFragment(CuratedWallFragment.newInstance("latest"), resources.getString(R.string.tab_title_curated_wall_fragment))
            addFragment(CollectionsFragment.newInstance("featured"), resources.getString(R.string.tab_title_collections_fragment))
            mViewPager.adapter = this
        }
        mViewPager.offscreenPageLimit = 2
        mTabLayout.setupWithViewPager(mViewPager)

        /** Add icons to TabLayout */
        mTabLayout.getTabAt(0)?.setIcon(R.drawable.ic_tab_recent)
        //mTabLayout.getTabAt(1)?.setIcon(R.drawable.ic_tab_curated)
        mTabLayout.getTabAt(1)?.setIcon(R.drawable.ic_tab_collection)

        /** Set color to TabLayout icons */
        mTabLayout.getTabAt(0)?.icon?.setColorFilter(
            ThemeUtils.getThemeAttrColor(this, R.attr.tabSelectedColor),
            PorterDuff.Mode.SRC_IN)
        /*mTabLayout.getTabAt(1)?.icon?.setColorFilter(
            ThemeUtils.getThemeAttrColor(this, R.attr.tabUnselectedColor),
            PorterDuff.Mode.SRC_IN)*/
        mTabLayout.getTabAt(1)?.icon?.setColorFilter(
            ThemeUtils.getThemeAttrColor(this, R.attr.tabUnselectedColor),
            PorterDuff.Mode.SRC_IN)

        mTabLayout.setOnTabSelectedListener(object : TabLayout.OnTabSelectedListener{
            override fun onTabReselected(p0: TabLayout.Tab?) {
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.icon?.setColorFilter(
                    ThemeUtils.getThemeAttrColor(this@HomeActivity, R.attr.tabUnselectedColor),
                    PorterDuff.Mode.SRC_IN)
            }

            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.icon?.setColorFilter(
                    ThemeUtils.getThemeAttrColor(this@HomeActivity, R.attr.tabSelectedColor),
                    PorterDuff.Mode.SRC_IN)
            }

        })
    }

    /** @method init navigation drawer settings */
    private fun initNavigationDrawer() {

        /*// Link toggle with mDrawerLayout
        val drawerToggle: ActionBarDrawerToggle = object : ActionBarDrawerToggle(
            this,
            mDrawerLayout,
            mToolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)
        {
            override fun onDrawerOpened(drawerView: View) {
                super.onDrawerOpened(drawerView)
            }
        }

        // Configure the mDrawerLayout layout to add listener and show icon on mToolbar
        drawerToggle.isDrawerIndicatorEnabled = false
        mDrawerLayout.setDrawerListener(drawerToggle)
        drawerToggle.syncState()*/

        mNavigationView.setNavigationItemSelectedListener {
            val url = "https://play.google.com/store/apps/details?id=${packageName}"

            if (_isAccountMode) {
                if (AuthManager.getInstance().isAuthorized()) {
                    when (it.itemId) {
                        1002 -> startActivity(Intent(this, ProfileActivity::class.java))
                        1004 -> showLogoutAlertDialog()
                    }
                } else {
                    if (it.itemId == 1001)
                        startActivity(Intent(this, MeProfileActivity::class.java))
                }
            } else {
                when (it.itemId) {
                    //R.id.nav_Favorites -> startActivity(Intent(applicationContext, FavoritesActivity::class.java))
                    R.id.nav_settings -> startActivity(Intent(applicationContext, SettingsActivity::class.java))
                    R.id.nav_about -> startActivity(Intent(applicationContext, AboutActivity::class.java))
                    R.id.nav_support_us -> startActivity(Intent(applicationContext, SupportUsActivity::class.java))
                    R.id.nav_share_app -> {
                        val i = Intent(Intent.ACTION_SEND)
                        i.type = "text/plain"
                        i.putExtra(Intent.EXTRA_TEXT, "You should give a try to WallBox for Android, it's freaking cool!\n\n$url")
                        startActivity(Intent.createChooser(i, "Share WallBox using"))
                    }
                    R.id.nav_rate_review -> {
                        val i = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        startActivity(i)
                    }
                }
            }

            mDrawerLayout.closeDrawers()
            true
        }

        _menuNavigation = mNavigationView.menu

        // navigation header
        val drawerHeader = mNavigationView.getHeaderView(0)
        setDrawerHeader(drawerHeader)

        (drawerHeader.findViewById(R.id.btn_account) as LinearLayout).setOnClickListener {
            val _isHide = Tools.toggleArrow(drawerHeader.findViewById(R.id.ic_arrow_drop) as ImageView)
            _isAccountMode = _isHide
            _menuNavigation.clear()
            if (_isHide) {
                if (AuthManager.getInstance().isAuthorized()) {
                    _menuNavigation.add(1, 1002, 100, AuthManager.getInstance().getUsername()).setIcon(R.drawable.ic_account_circle)
                    _menuNavigation.add(1, 1003, 100, "Manage Account").setIcon(R.drawable.ic_nav_settings)
                    _menuNavigation.add(1, 1004, 100, "Logout").setIcon(R.drawable.ic_logout)
                } else {
                    _menuNavigation.add(1,1001,100, "Add account").setIcon(R.drawable.ic_add)
                }
            } else {
                mNavigationView.inflateMenu(R.menu.menu_navigation_drawer_main)
            }
        }
    }

    /** @method show snack bar */
    private fun showSnackBar(message: String, duration: Int) {
        Snackbar.make(mDrawerLayout, message, duration).show()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar_main, menu)
        Tools.changeMenuIconColor(menu!!, ThemeUtils.getThemeAttrColor(this, R.attr.tabUnselectedColor))
        mSortRecentLatest = menu.findItem(R.id.menu_sort_recent_latest)
        mSortRecentOldest = menu.findItem(R.id.menu_sort_recent_oldest)
        mSortRecentPopular = menu.findItem(R.id.menu_sort_recent_popular)
        /*mSortCuratedLatest = menu.findItem(R.id.menu_sort_curated_latest)
        mSortCuratedOldest = menu.findItem(R.id.menu_sort_curated_oldest)
        mSortCuratedPopular = menu.findItem(R.id.menu_sort_curated_popular)*/
        mSortCollectionAll = menu.findItem(R.id.menu_sort_collection_all)
        mSortCollectionFeatured = menu.findItem(R.id.menu_sort_collection_featured)

        actionSortMenu = menu.findItem(R.id.action_sort)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        val transaction = supportFragmentManager.beginTransaction()

        when (item?.itemId) {
            R.id.action_search -> {
                startActivity(Intent(this, SearchActivity::class.java))
            }
            R.id.action_sort -> {
                when (mViewPager.currentItem) {
                    0 -> handleSortMenuItems(true,true,true,false, false)
                    1 -> handleSortMenuItems(false,false,false,true,true)
                    /*2 -> handleSortMenuItems(false,false,false,false,false,false,true,true,true)*/
                }
            }
            R.id.menu_sort_recent_latest -> {
                transaction.replace(R.id.recent_container, RecentWallFragment.newInstance("latest")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
                showSnackBar("Wallpaper sorted by Latest", Snackbar.LENGTH_SHORT)
            }
            R.id.menu_sort_recent_oldest -> {
                transaction.replace(R.id.recent_container, RecentWallFragment.newInstance("oldest")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
                showSnackBar("Wallpaper sorted by Oldest", Snackbar.LENGTH_SHORT)
            }
            R.id.menu_sort_recent_popular -> {
                transaction.replace(R.id.recent_container, RecentWallFragment.newInstance("popular")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
                showSnackBar("Wallpaper sorted by Popular", Snackbar.LENGTH_SHORT)
            }
            /*R.id.menu_sort_curated_latest -> {
                transaction.replace(R.id.curated_container, CuratedWallFragment.newInstance("latest")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
                showSnackBar("Wallpaper sorted by Latest", Snackbar.LENGTH_SHORT)
            }
            R.id.menu_sort_curated_oldest -> {
                transaction.replace(R.id.curated_container, CuratedWallFragment.newInstance("oldest")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
                showSnackBar("Wallpaper sorted by Oldest", Snackbar.LENGTH_SHORT)
            }
            R.id.menu_sort_curated_popular -> {
                transaction.replace(R.id.curated_container, CuratedWallFragment.newInstance("popular")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
                showSnackBar("Wallpaper sorted by Popular", Snackbar.LENGTH_SHORT)
            }*/
            R.id.menu_sort_collection_all -> {
                transaction.replace(R.id.collections_container, CollectionsFragment.newInstance("all")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
                showSnackBar("Collections sorted by All", Snackbar.LENGTH_SHORT)
            }
            R.id.menu_sort_collection_featured -> {
                transaction.replace(R.id.collections_container, CollectionsFragment.newInstance("featured")).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE).commit()
                showSnackBar("Collections sorted by Featured", Snackbar.LENGTH_SHORT)
            }
            else -> mDrawerLayout.openDrawer(GravityCompat.START)
        }

        return super.onOptionsItemSelected(item!!)
    }

    /** @method handle sort menu items on different fragment */
    private fun handleSortMenuItems(vararg value: Boolean) {
        for ((i, v) in value.withIndex()) {
            when (i) {
                0 -> mSortRecentLatest?.isVisible = v
                1 -> mSortRecentOldest?.isVisible = v
                2 -> mSortRecentPopular?.isVisible = v
                /*3 -> mSortCuratedLatest?.isVisible = v
                4 -> mSortCuratedOldest?.isVisible = v
                5 -> mSortCuratedPopular?.isVisible = v*/
                3 -> mSortCollectionAll?.isVisible = v
                4 -> mSortCollectionFeatured?.isVisible = v
            }
        }
    }

    /*private fun runTutorial() {
        TapTargetSequence(this)
            .targets(
                TapTarget.forView(actionSortMenu as View, "Sort Photos", "Tap here to sort photos")
                    .outerCircleColor(R.color.colorAccent)
                    .outerCircleAlpha(0.80f)
                    .targetCircleColor(R.color.white)
                    .titleTextColor(R.color.primaryTextColor)
            )
            .listener(object : TapTargetSequence.Listener {
                override fun onSequenceFinish() {
                    SharedPref.getInstance(this@HomeActivity).saveData(Prefs.ACT_HOME_INTRO, false)
                }

                override fun onSequenceStep(lastTarget: TapTarget?, targetClicked: Boolean) {

                }

                override fun onSequenceCanceled(lastTarget: TapTarget?) {

                }
            })
            .start()
    }*/

    private fun setDrawerHeader(drawerHeader: View) {
        if (AuthManager.getInstance().isAuthorized() && !TextUtils.isEmpty(AuthManager.getInstance().getProfileUrl())) {
            val fullName = AuthManager.getInstance().getFirstName() + " " + AuthManager.getInstance().getLastName()
            (drawerHeader.findViewById(R.id.tv_full_name) as TextView).text = fullName
            (drawerHeader.findViewById(R.id.tv_email) as TextView).text = AuthManager.getInstance().getEmail()

            val circularProgressDrawable = CircularProgressDrawable(this)
            circularProgressDrawable.strokeWidth = 5f
            circularProgressDrawable.centerRadius = 20f
            circularProgressDrawable.setColorSchemeColors(ThemeUtils.getThemeAttrColor(this, R.attr.colorAccent))
            circularProgressDrawable.start()

            (drawerHeader.findViewById(R.id.img_profile) as CircularImageView)
                .loadUrl(AuthManager.getInstance().getProfileUrl(), circularProgressDrawable, getDrawable(R.drawable.placeholder_profile))

        } else {
            (drawerHeader.findViewById(R.id.tv_full_name) as TextView).text = getString(R.string.app_name)
            (drawerHeader.findViewById(R.id.tv_email) as TextView).text = "Free HD Wallpapers"
        }
    }

    private fun showLogoutAlertDialog() {
        MaterialDialog(this).show {
            cornerRadius(16f)
            title(text = "Alert")
            message(text = "Are you sure you want to logout?")
            positiveButton(R.string.yes) {
                AuthManager.getInstance().logout()
                it.dismiss()
            }
            negativeButton(R.string.no) {
                it.dismiss()
            }
        }
    }

    override fun onSaveAccessToken() {

    }

    override fun onLogin() {
        PopupUtils.showSnackbar(mDrawerLayout, "Login success :)")
    }

    override fun onSaveProfileUrl() {
        setDrawerHeader(mNavigationView.getHeaderView(0))

        _menuNavigation.clear()
        _menuNavigation.add(1, 1002, 100, AuthManager.getInstance().getUsername()).setIcon(R.drawable.ic_account_circle)
        _menuNavigation.add(1, 1003, 100, "Manage Account").setIcon(R.drawable.ic_nav_settings)
        _menuNavigation.add(1, 1004, 100, "Logout").setIcon(R.drawable.ic_logout)
    }

    override fun onLogout() {
        PopupUtils.showSnackbar(mDrawerLayout, "Logout success!")
        setDrawerHeader(mNavigationView.getHeaderView(0))

        _menuNavigation.clear()
        _menuNavigation.add(1,1001,100, "Add account").setIcon(R.drawable.ic_add)
    }

}

