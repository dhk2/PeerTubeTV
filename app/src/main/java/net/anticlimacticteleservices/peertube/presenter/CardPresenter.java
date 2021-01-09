package net.anticlimacticteleservices.peertube.presenter;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.leanback.widget.ImageCardView;
import androidx.leanback.widget.Presenter;

import com.bumptech.glide.Glide;

import net.anticlimacticteleservices.peertube.R;
import net.anticlimacticteleservices.peertube.model.Video;

/*
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an Image CardView
 */
public class CardPresenter extends Presenter {
    private static final String TAG = "CardPresenter";

    private static final int CARD_WIDTH = 313;
    private static final int CARD_HEIGHT = 176;
    private static int sSelectedBackgroundColor;
    private static int sDefaultBackgroundColor;
    private Drawable mDefaultCardImage;

    private static void updateCardBackgroundColor(ImageCardView view, boolean selected) {
        int color = selected ? sSelectedBackgroundColor : sDefaultBackgroundColor;
        // Both background colors should be set because the view"s background is temporarily visible
        // during animations.
        view.setBackgroundColor(color);
        view.findViewById(R.id.info_field).setBackgroundColor(color);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {
        Log.d(TAG, "onCreateViewHolder");

        sDefaultBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.design_default_color_primary_dark);
        sSelectedBackgroundColor =
                ContextCompat.getColor(parent.getContext(), R.color.primaryColorDeepPurple);
        /*
         * This template uses a default image in res/drawable, but the general case for Android TV
         * will require your resources in xhdpi. For more information, see
         * https://developer.android.com/training/tv/start/layouts.html#density-resources
         */
        mDefaultCardImage = ContextCompat.getDrawable(parent.getContext(), R.drawable.ic_logo);

        ImageCardView cardView =
                new ImageCardView(parent.getContext()) {
                    @Override
                    public void setSelected(boolean selected) {
                        updateCardBackgroundColor(this, selected);
                        super.setSelected(selected);
                    }
                };
        cardView.setOnFocusChangeListener((view, isFocused) -> {
            if (isFocused) {
                ((TextView) cardView.findViewById(R.id.title_text)).setMaxLines(5);
                ((TextView) cardView.findViewById(R.id.title_text)).setSingleLine(false);
            }
            else {
                ((TextView) cardView.findViewById(R.id.title_text)).setMaxLines(1);
                ((TextView) cardView.findViewById(R.id.title_text)).setSingleLine(true);
                ((TextView) cardView.findViewById(R.id.title_text)).setMarqueeRepeatLimit(5000);
                ((TextView) cardView.findViewById(R.id.title_text)).setEllipsize(TextUtils.TruncateAt.MARQUEE);
                ((TextView) cardView.findViewById(R.id.title_text)).setFocusable(true);
                ((TextView) cardView.findViewById(R.id.title_text)).setFocusableInTouchMode(true);
                ((TextView) cardView.findViewById(R.id.title_text)).setSelected(true);
            }
        });

        ((TextView) cardView.findViewById(R.id.title_text)).setMarqueeRepeatLimit(5000);
        ((TextView) cardView.findViewById(R.id.title_text)).setEllipsize(TextUtils.TruncateAt.MARQUEE);
        ((TextView) cardView.findViewById(R.id.title_text)).setFocusable(true);
        ((TextView) cardView.findViewById(R.id.title_text)).setFocusableInTouchMode(true);
        ((TextView) cardView.findViewById(R.id.title_text)).setSelected(true);
        ((TextView) cardView.findViewById(R.id.title_text)).setSingleLine(true);
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);
        updateCardBackgroundColor(cardView, false);
        return new ViewHolder(cardView);
    }

    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {
        Video movie = (Video) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        Log.d(TAG, "onBindViewHolder");
        if (movie.getName() != null) {
            cardView.setTitleText(movie.getName());
            cardView.setContentText(movie.getAccount().getDisplayName());
            cardView.setMainImageDimensions(CARD_WIDTH, CARD_HEIGHT);
            Log.i(TAG,"thumbnailpath "+movie.getAccount().getHost()+movie.getThumbnailPath());
            //TODO fix this properly
            Glide.with(viewHolder.view.getContext())
                    .load("https://"+movie.getAccount().getHost()+movie.getThumbnailPath())
                    .centerCrop()
                    .error(mDefaultCardImage)
                    .into(cardView.getMainImageView());
        }
    }

    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {
        Log.d(TAG, "onUnbindViewHolder");
        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}