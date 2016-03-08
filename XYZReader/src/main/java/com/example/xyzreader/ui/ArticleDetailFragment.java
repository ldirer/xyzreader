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
import android.support.v7.app.AppCompatActivity;
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

    private boolean mIsCard = false;
    private ImageView mToolbarImage;
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

        mToolbarImage = (ImageView) getActivity().findViewById(R.id.toolbar_image);
        mRequestQueue = Volley.newRequestQueue(getActivity());

        return mRootView;
    }

    private void bindViews() {
        Log.d(LOG_TAG, "in bindViews");
        if (mRootView == null) {
            return;
        }

        TextView titleView = (TextView) getActivity().findViewById(R.id.article_title);
        TextView bylineView = (TextView) getActivity().findViewById(R.id.article_byline);
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);
        bodyView.setTypeface(Typeface.createFromAsset(getResources().getAssets(), "Rosario-Regular.ttf"));

//        TODO: hide subtitle in the collapsed toolbar. Potential source (most voted ans):
//        http://stackoverflow.com/questions/31662416/show-collapsingtoolbarlayout-title-only-when-collapsed
        if (mCursor != null) {
            bodyView.setText(mCursor.getString(ArticleLoader.Query.BODY));
//            titleView.setText(mCursor.getString(ArticleLoader.Query.TITLE));
            //noinspection ConstantConditions
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(mCursor.getString(ArticleLoader.Query.TITLE));
            bylineView.setText(Utils.getDateAuthorLineText(getActivity(), mCursor));
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            //TODO: fix image loading: the right image is loaded *at first*, then another one takes its place!
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
            Log.d(LOG_TAG, "Cursor was null in bind views, cannot set title, body and 'byline'!");
            titleView.setText("");
            bylineView.setText("");
            bodyView.setText("");
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return ArticleLoader.newInstanceForItemId(getActivity(), mItemId);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        Log.d(LOG_TAG, "in onLoadFinished");
        if (!isAdded()) {
            Log.d(LOG_TAG, "Fragment not added yet!??! And loader created...");
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
    }

}
