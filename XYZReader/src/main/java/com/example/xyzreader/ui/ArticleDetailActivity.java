package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v13.app.FragmentStatePagerAdapter;
import android.support.v4.app.ShareCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.Volley;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;
import com.example.xyzreader.data.ItemsContract;

/**
 * An activity representing a single Article detail screen, letting you swipe between articles.
 */
public class ArticleDetailActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = ArticleDetailActivity.class.getSimpleName();
    private Cursor mCursor;
    // mStartId is the id of the article that was opened from the article list.
    private long mStartId;


    private ViewPager mPager;
    private MyPagerAdapter mPagerAdapter;
    private ActionBar mActionBar;
    private CollapsingToolbarLayout mCollapsingToolbarLayout;

    private RequestQueue mRequestQueue;
    private ImageView mToolbarImage;
    private TextView mByLineView;
    private ViewPager.OnPageChangeListener onPageChangeListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: Maybe make the activity Immersive (hide status bar). Seems unnecessary but if it was supposed to be there...
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            getWindow().getDecorView().setSystemUiVisibility(
//                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
//                            View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
//        }

        setContentView(R.layout.activity_article_detail);

        getLoaderManager().initLoader(0, null, this);


        // Pratik Butani's answer for issues with setTitle not updating the title:
        // http://stackoverflow.com/questions/26486730/in-android-app-toolbar-settitle-method-has-no-effect-application-name-is-shown
        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mCollapsingToolbarLayout = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar_layout);


//        Hide subtitle in the collapsed toolbar. Source (most voted ans):
//        http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
        final AppBarLayout mAppBarLayout = (AppBarLayout) findViewById(R.id.toolbar_container);
        mAppBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean subtitleHidden = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = mAppBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    mByLineView.setVisibility(View.GONE);
                    subtitleHidden = true;
                } else if(subtitleHidden) {
                    mByLineView.setVisibility(View.VISIBLE);
                    subtitleHidden = false;
                }
            }
        });
        setSupportActionBar(mToolbar);

        mCollapsingToolbarLayout.setTitleEnabled(true);
        // By default the title will show the name of the app: we don't want that.
        mCollapsingToolbarLayout.setTitle("");

        // We add the "left-arrow-back-button".
        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
        }

        mToolbarImage = (ImageView) findViewById(R.id.toolbar_image);

        mByLineView = (TextView) findViewById(R.id.article_byline);
        mRequestQueue = Volley.newRequestQueue(this);


        mPagerAdapter = new MyPagerAdapter(getFragmentManager());
        mPager = (ViewPager) findViewById(R.id.pager);
        mPager.setAdapter(mPagerAdapter);
        mPager.setPageMargin((int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics()));
        mPager.setPageMarginDrawable(new ColorDrawable(0x22000000));

        onPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            /** In here we set the image in the toolbar. It would have been nice to do it in the Fragment.
            However since the toolbar belongs to the activity we can't do this (fragments are instantiated independently of the display!)
            */
            @Override
            public void onPageSelected(int position) {
                Log.d(LOG_TAG, "in onPageSelected");
                Log.d(LOG_TAG, "in onPageSelected with position " + String.valueOf(position));


                //TODO: fix image loading: Images in detail vs list view do not match
                // TODO position is somehow offset by 1...

                mCursor.moveToPosition(position);
                final String imageUrl = mCursor.getString(ArticleLoader.Query.PHOTO_URL);
                ImageRequest imageRequest = new ImageRequest(imageUrl,
                        new Response.Listener<Bitmap>() {

                            @Override
                            public void onResponse(Bitmap bitmap) {
                                Log.d(LOG_TAG, "VOLLEY GOT US DAT BITMAP");
                                Log.d(LOG_TAG, "Btw, url was:" + imageUrl);
                                mToolbarImage.setImageBitmap(bitmap);
                            }
                        }, 200, 200, null, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.d(LOG_TAG, "AAAAAAAAAAH VOLLEY ERROR VOLLEY ERROR");
                        Log.d(LOG_TAG, "Btw, url was:" + mCursor.getString(ArticleLoader.Query.PHOTO_URL));
                        Log.d(LOG_TAG, Log.getStackTraceString(volleyError));
                    }
                });
                mRequestQueue.add(imageRequest);
                mCollapsingToolbarLayout.setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
                mByLineView.setText(Utils.getDateAuthorLineText(ArticleDetailActivity.this, mCursor));
        }


            @Override
            public void onPageScrollStateChanged(int state) {

            }
        };

        mPager.addOnPageChangeListener(onPageChangeListener);


        // TODO: That's some sharing functionality.
        findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(ArticleDetailActivity.this)
                        .setType("text/plain")
                        .setText("Some sample text")
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        if (savedInstanceState == null) {
            if (getIntent() != null && getIntent().getData() != null) {
                mStartId = ItemsContract.Items.getItemId(getIntent().getData());
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mPager.clearOnPageChangeListeners();

    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newAllArticlesInstance(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        // Note if we open the app and browse through items this is called only once.
        Log.d(LOG_TAG, "in onLoadFinished");
        mCursor = cursor;
        mPagerAdapter.notifyDataSetChanged();

        // Select the start ID
        if (mStartId > 0) {
            mCursor.moveToFirst();
            // TODO: optimize. For now this is just a loop looking for the right item (matching on ID)
            while (!mCursor.isAfterLast()) {
                if (mCursor.getLong(ArticleLoader.Query._ID) == mStartId) {
                    final int position = mCursor.getPosition();
                    Log.d(LOG_TAG, String.format("in onLoadFinished, setting position: %d", position));
                    mPager.setCurrentItem(position, false);
                    break;
                }
                mCursor.moveToNext();
            }
            mStartId = 0;
        }


        // For some reason onPageSelected is not triggered for the first page of the list.
        // Workaround from http://stackoverflow.com/questions/11794269/onpageselected-isnt-triggered-when-calling-setcurrentitem0
        mPager.post(new Runnable() {
            @Override
            public void run() {
                onPageChangeListener.onPageSelected(mPager.getCurrentItem());
            }
        });
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        mPagerAdapter.notifyDataSetChanged();
    }


    private class MyPagerAdapter extends FragmentStatePagerAdapter {
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public void setPrimaryItem(ViewGroup container, int position, Object object) {
            super.setPrimaryItem(container, position, object);
        }

        @Override
        public Fragment getItem(int position) {
            Log.d(LOG_TAG, String.format("in getItem with position: %d", position));

            mCursor.moveToPosition(position);
            Bundle arguments = new Bundle();

            Log.d(LOG_TAG, String.format("in getItem with position: %d - id: %d", position,
                    mCursor.getLong(ArticleLoader.Query._ID)));
            arguments.putLong(ArticleDetailFragment.ARG_ITEM_ID,
                    mCursor.getLong(ArticleLoader.Query._ID));
            ArticleDetailFragment fragment = new ArticleDetailFragment();
            fragment.setArguments(arguments);
            return fragment;
        }

        @Override
        public int getCount() {
            return (mCursor != null) ? mCursor.getCount() : 0;
        }
    }
}
