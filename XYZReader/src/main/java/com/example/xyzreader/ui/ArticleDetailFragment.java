package com.example.xyzreader.ui;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.NetworkImageView;
import com.android.volley.toolbox.Volley;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;
    private static final String LOG_TAG = ArticleDetailFragment.class.getSimpleName();

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private NestedScrollView mScrollView;
    private FrameLayout mFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;
    private NetworkImageView mToolbarImage;
    private ImageLoader mImageLoader;
    private RequestQueue mRequestQueue;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mItemId = getArguments().getLong(ARG_ITEM_ID);
        }

        mIsCard = getResources().getBoolean(R.bool.detail_is_card);
        mStatusBarFullOpacityBottom = getResources().getDimensionPixelSize(
                R.dimen.detail_card_top_margin);
        setHasOptionsMenu(true);
    }

    public ArticleDetailActivity getActivityCast() {
        return (ArticleDetailActivity) getActivity();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // In support library r8, calling initLoader for a fragment in a FragmentPagerAdapter in
        // the fragment's onCreate may cause the same LoaderManager to be dealt to multiple
        // fragments because their mIndex is -1 (haven't been added to the activity yet). Thus,
        // we do this in onActivityCreated.
        getLoaderManager().initLoader(0, null, this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mRootView = inflater.inflate(R.layout.fragment_article_detail, container, false);
        mFrameLayout = (FrameLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);

        mScrollView = (NestedScrollView) mRootView.findViewById(R.id.scrollview);

        mStatusBarColorDrawable = new ColorDrawable(0);
        mToolbarImage = (NetworkImageView) getActivity().findViewById(R.id.toolbar_image);
        mRequestQueue = Volley.newRequestQueue(getActivity());

        bindViews();
        return mRootView;
    }

    // TODO: Hmmm maybe this should be kept in! It adapts the status bar depending on the image colors.
    // TODO: Not sure why we need a status bar other than the image in the first place though! We probably don't.
//    private void updateStatusBar() {
//        int color = 0;
//        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
//            float f = progress(mScrollY,
//                    mStatusBarFullOpacityBottom - mTopInset * 3,
//                    mStatusBarFullOpacityBottom - mTopInset);
//            color = Color.argb((int) (255 * f),
//                    (int) (Color.red(mMutedColor) * 0.9),
//                    (int) (Color.green(mMutedColor) * 0.9),
//                    (int) (Color.blue(mMutedColor) * 0.9));
//        }
//        mStatusBarColorDrawable.setColor(color);
//        mFrameLayout.setInsetBackground(mStatusBarColorDrawable);
//    }



    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        TextView titleView = (TextView) mRootView.findViewById(R.id.article_title);
        TextView bylineView = (TextView) mRootView.findViewById(R.id.article_byline);
        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            bylineView.setText(Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <font color='#ffffff'>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</font>"));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

//            mImageLoader = new ImageLoader();
            ImageRequest imageRequest = new ImageRequest(
                    mCursor.getString(ArticleLoader.Query.PHOTO_URL),
                    new Response.Listener<Bitmap>() {
                        @Override
                        public void onResponse(Bitmap bitmap) {
                            Log.d(LOG_TAG, "VOLLEY GOT US DAT BITMAP");
                            mToolbarImage.setImageBitmap(bitmap);
                        }
                    }
                    , 200, 200, null, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.d(LOG_TAG, "AAAAAAAAAAH VOLLEY ERROR VOLLEY ERROR");
                    Log.d(LOG_TAG, "Btw, url was:" + mCursor.getString(ArticleLoader.Query.PHOTO_URL));
                    Log.d(LOG_TAG, "Dat volley message gonna help us:" + volleyError.getMessage());
                    Log.d(LOG_TAG, Log.getStackTraceString(volleyError));
                }
            });
            mRequestQueue.add(imageRequest);

//            ImageLoaderHelper.getInstance(getActivity()).getImageLoader()
//                    .get(mCursor.getString(ArticleLoader.Query.PHOTO_URL), new ImageLoader.ImageListener() {
//                        @Override
//                        public void onResponse(ImageLoader.ImageContainer imageContainer, boolean b) {
//                            Bitmap bitmap = imageContainer.getBitmap();
//                            if (bitmap != null) {
//                                Palette p = Palette.generate(bitmap, 12);
//                                mMutedColor = p.getDarkMutedColor(0xFF333333);
////                                mPhotoView.setImageBitmap(imageContainer.getBitmap());
//                                mToolbarImage.setImageBitmap(imageContainer.getBitmap());
//                                mRootView.findViewById(R.id.meta_bar)
//                                        .setBackgroundColor(mMutedColor);
////                                updateStatusBar();
//                            }
//                            else {
//                                Log.d(LOG_TAG, "No bitmap found!!");
//                            }
//                        }
//
//                        @Override
//                        public void onErrorResponse(VolleyError volleyError) {
//
//                        }
//                    });
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A" );
            bodyView.setText("N/A");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        if (!isAdded()) {
            if (cursor != null) {
                cursor.close();
            }
            return;
        }

        mCursor = cursor;
        if (mCursor != null && !mCursor.moveToFirst()) {
            Log.e(TAG, "Error reading item detail cursor");
            mCursor.close();
            mCursor = null;
        }

        bindViews();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {
        mCursor = null;
        bindViews();
    }

    public int getUpButtonFloor() {
        if (mPhotoContainerView == null || mPhotoView.getHeight() == 0) {
            return Integer.MAX_VALUE;
        }

        // account for parallax
        return mIsCard
                ? (int) mPhotoContainerView.getTranslationY() + mPhotoView.getHeight() - mScrollY
                : mPhotoView.getHeight() - mScrollY;
    }
}
