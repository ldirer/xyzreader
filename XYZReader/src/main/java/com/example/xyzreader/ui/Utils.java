package com.example.xyzreader.ui;

import android.content.Context;
import android.database.Cursor;
import android.text.Html;
import android.text.format.DateUtils;

import com.example.xyzreader.R;
import com.example.xyzreader.data.ArticleLoader;

public class Utils {
    static CharSequence getDateAuthorLineText(Context context, Cursor cursor) {
        return Html.fromHtml(context.getString(
                R.string.date_and_author_line, DateUtils.getRelativeTimeSpanString(
                        cursor.getLong(ArticleLoader.Query.PUBLISHED_DATE),
                        System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS,
                        DateUtils.FORMAT_ABBREV_ALL).toString(),
                cursor.getString(ArticleLoader.Query.AUTHOR)));

    }

    public static String getTextPreview(String text) {
        String htmlString = Html.fromHtml(text).toString();
        return (htmlString.substring(0, Math.min(htmlString.length(), 100)).concat("..."));

    }
}
