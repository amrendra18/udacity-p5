package com.example.xyzreader.ui;

import android.app.Activity;
import android.app.Fragment;
import android.app.LoaderManager;
import android.content.Intent;
import android.content.Loader;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ShareCompat;
import android.support.v7.graphics.Palette;
import android.text.Html;
import android.text.Spanned;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

/**
 * A fragment representing a single Article detail screen. This fragment is
 * either contained in a {@link ArticleListActivity} in two-pane mode (on
 * tablets) or a {@link ArticleDetailActivity} on handsets.
 */
public class ArticleDetailFragment extends Fragment implements
        LoaderManager.LoaderCallbacks<Cursor>, AppBarLayout.OnOffsetChangedListener {
    private static final String TAG = "ArticleDetailFragment";

    public static final String ARG_ITEM_ID = "item_id";
    private static final float PARALLAX_FACTOR = 1.25f;

    private Cursor mCursor;
    private long mItemId;
    private View mRootView;
    private int mMutedColor = 0xFF333333;
    private DrawInsetsFrameLayout mDrawInsetsFrameLayout;
    private ColorDrawable mStatusBarColorDrawable;

    private int mTopInset;
    private View mPhotoContainerView;
    private ImageView mPhotoView;
    private int mScrollY;
    private boolean mIsCard = false;
    private int mStatusBarFullOpacityBottom;

    LinearLayout metaBarLinearLayout;
    CollapsingToolbarLayout mCollapsingToolbar;
    LinearLayout mTitleLine;
    AppBarLayout appBarLayout;

    TextView titleView;
    TextView bylineView;
    TextView toolbarTitleView;
    TextView toolbarBylineView;

    FloatingActionButton shareButton;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ArticleDetailFragment() {
    }

    public static ArticleDetailFragment newInstance(long itemId) {
        Bundle arguments = new Bundle();
        arguments.putLong(ARG_ITEM_ID, itemId);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(arguments);
        return fragment;
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
        appBarLayout = (AppBarLayout) mRootView.findViewById(R.id.app_bar_layout);

        metaBarLinearLayout = (LinearLayout) mRootView.findViewById(R.id.meta_bar);
        mTitleLine = (LinearLayout) mRootView.findViewById(R.id.toolbar_header_view);
        mCollapsingToolbar = (CollapsingToolbarLayout) mRootView.findViewById(R.id
                .collapsing_toolbar);

        if (mTitleLine != null) {
            appBarLayout.addOnOffsetChangedListener(this);
        }
        shareButton = (FloatingActionButton) mRootView.findViewById(R.id.share_fab);
        mDrawInsetsFrameLayout = (DrawInsetsFrameLayout)
                mRootView.findViewById(R.id.draw_insets_frame_layout);
        mDrawInsetsFrameLayout.setOnInsetsCallback(new DrawInsetsFrameLayout.OnInsetsCallback() {
            @Override
            public void onInsetsChanged(Rect insets) {
                mTopInset = insets.top;
            }
        });

        mPhotoView = (ImageView) mRootView.findViewById(R.id.photo);
        mPhotoContainerView = mRootView.findViewById(R.id.collapsing_toolbar);

        mStatusBarColorDrawable = new ColorDrawable(0);

        mRootView.findViewById(R.id.share_fab).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StringBuilder sb = new StringBuilder();
                sb.append("Checkout this article\n");
                sb.append(titleView.getText() + "\n");
                sb.append("#" + getString(R.string.app_name));
                startActivity(Intent.createChooser(ShareCompat.IntentBuilder.from(getActivity())
                        .setType("text/plain")
                        .setText(sb.toString())
                        .getIntent(), getString(R.string.action_share)));
            }
        });

        bindViews();
        updateStatusBar();
        return mRootView;
    }

    private void updateStatusBar() {
        int color = 0;
        if (mPhotoView != null && mTopInset != 0 && mScrollY > 0) {
            float f = progress(mScrollY,
                    mStatusBarFullOpacityBottom - mTopInset * 3,
                    mStatusBarFullOpacityBottom - mTopInset);
            color = Color.argb((int) (255 * f),
                    (int) (Color.red(mMutedColor) * 0.9),
                    (int) (Color.green(mMutedColor) * 0.9),
                    (int) (Color.blue(mMutedColor) * 0.9));
        }
        mStatusBarColorDrawable.setColor(color);
        mDrawInsetsFrameLayout.setInsetBackground(mStatusBarColorDrawable);
        setCollapsingToolbarColor(mMutedColor);
    }

    private void setCollapsingToolbarColor(int color) {
        if (mCollapsingToolbar != null) {
            mCollapsingToolbar.setBackgroundColor(color);
            mCollapsingToolbar.setStatusBarScrimColor(color);
            mCollapsingToolbar.setContentScrimColor(color);
        }
    }

    private void setToolbarTitlesColor(int color) {
        if (toolbarBylineView != null) {
            toolbarBylineView.setTextColor(color);
            toolbarTitleView.setTextColor(color);
        }
    }

    private void setMetaTitlesColor(int color) {
        bylineView.setTextColor(color);
        titleView.setTextColor(color);
    }

    private void setMetaBarLinearLayoutColor(int color) {
        metaBarLinearLayout.setBackgroundColor(color);
    }

    static float progress(float v, float min, float max) {
        return constrain((v - min) / (max - min), 0, 1);
    }

    static float constrain(float val, float min, float max) {
        if (val < min) {
            return min;
        } else if (val > max) {
            return max;
        } else {
            return val;
        }
    }

    private void bindViews() {
        if (mRootView == null) {
            return;
        }

        titleView = (TextView) mRootView.findViewById(R.id.article_title);
        bylineView = (TextView) mRootView.findViewById(R.id.article_byline);

        toolbarTitleView = (TextView) mRootView.findViewById(R.id.header_view_title);
        toolbarBylineView = (TextView) mRootView.findViewById(R.id.header_view_sub_title);

        bylineView.setMovementMethod(new LinkMovementMethod());
        TextView bodyView = (TextView) mRootView.findViewById(R.id.article_body);

        if (mCursor != null) {
            mRootView.setAlpha(0);
            mRootView.setVisibility(View.VISIBLE);
            mRootView.animate().alpha(1);
            String title = mCursor.getString(ArticleLoader.Query.TITLE);
            Spanned byline = Html.fromHtml(
                    DateUtils.getRelativeTimeSpanString(
                            mCursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                            System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                            DateUtils.FORMAT_ABBREV_ALL).toString()
                            + " by <i><b>"
                            + mCursor.getString(ArticleLoader.Query.AUTHOR)
                            + "</b></i>");
            titleView.setText(title);
            bylineView.setText(byline);
            if (toolbarTitleView != null) {
                toolbarTitleView.setText(title);
                toolbarBylineView.setText(byline);
            }
            bodyView.setText(Html.fromHtml(mCursor.getString(ArticleLoader.Query.BODY)));

            Glide.with(getActivity())
                    .load(mCursor.getString(ArticleLoader.Query.PHOTO_URL)).asBitmap()
                    .listener(new RequestListener<String, Bitmap>() {
                        @Override
                        public boolean onException(Exception e, String model, Target<Bitmap> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(Bitmap resource, String model, Target<Bitmap> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            if (resource != null) {
                                Palette.from(resource).generate(
                                        new Palette.PaletteAsyncListener() {
                                            @Override
                                            public void onGenerated(Palette palette) {
                                                Palette.Swatch darkVibrantSwatch = palette.getDarkVibrantSwatch();
                                                Palette.Swatch darkMutedSwatch = palette.getDarkMutedSwatch();
                                                Palette.Swatch lightVibrantSwatch = palette.getLightVibrantSwatch();
                                                Palette.Swatch lightMutedSwatch = palette.getLightMutedSwatch();

                                                Palette.Swatch backgroundAndContentColors = darkVibrantSwatch;
                                                if (backgroundAndContentColors == null) {
                                                    backgroundAndContentColors = darkMutedSwatch;
                                                }
                                                Palette.Swatch titleAndFabColors = lightVibrantSwatch;

                                                if (titleAndFabColors == null) {
                                                    titleAndFabColors = lightMutedSwatch;
                                                }

                                                if (backgroundAndContentColors != null) {
                                                    mMutedColor = backgroundAndContentColors
                                                            .getRgb();

                                                } else {
                                                    mMutedColor = palette.getDarkMutedColor
                                                            (0xFF333333);
                                                }
                                                updateStatusBar();
                                                setDarkColorWork(backgroundAndContentColors);
                                                setLightColorWork(titleAndFabColors);
                                            }
                                        });
                            }
                            return false;
                        }
                    })
                    .into(mPhotoView);
        } else {
            mRootView.setVisibility(View.GONE);
            titleView.setText("N/A");
            bylineView.setText("N/A");
            bodyView.setText("N/A");
        }
    }

    private void setDarkColorWork(Palette.Swatch swatch) {
        if (swatch != null) {
            int color = swatch.getRgb();
            shareButton.setBackgroundTintList(ColorStateList.valueOf(color));
            setCollapsingToolbarColor(color);
            setMetaBarLinearLayoutColor(color);
        }
    }

    private void setLightColorWork(Palette.Swatch swatch) {
        if (swatch != null) {
            int color = swatch.getRgb();
            shareButton.setRippleColor(color);
            setToolbarTitlesColor(color);
            setMetaTitlesColor(color);
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

    private boolean isToolbarShown = false;

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        int maxScroll = appBarLayout.getTotalScrollRange();
        float percentage = (float) Math.abs(verticalOffset) / (float) maxScroll;
        if (percentage >= 1f && isToolbarShown) {
            hideToolbar();
        } else if (percentage < 1f && !isToolbarShown) {
            showToolbar();
        }
    }

    private void showToolbar() {
        if (!isToolbarShown) {
            mTitleLine.setVisibility(View.INVISIBLE);
            metaBarLinearLayout.setVisibility(View.VISIBLE);
            Activity activity = getActivity();
            if (activity instanceof ArticleDetailActivity) {
                ((ArticleDetailActivity) getActivity()).setUpButtonVisibility(View.VISIBLE);
            }
            isToolbarShown = true;
        }
    }

    private void hideToolbar() {
        if (isToolbarShown) {
            mTitleLine.setVisibility(View.VISIBLE);
            metaBarLinearLayout.setVisibility(View.INVISIBLE);
            Activity activity = getActivity();
            if (activity instanceof ArticleDetailActivity) {
                ((ArticleDetailActivity) getActivity()).setUpButtonVisibility(View.GONE);
            }
            isToolbarShown = false;
        }
    }
}
