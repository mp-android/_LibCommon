package net.madroom.common;

import java.util.HashSet;
import java.util.Set;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.TextUtils;


public class CommonDbUtil {
    public static Set<Long> getIdSet(Cursor c) {
        Set<Long> ids = new HashSet<Long>();
        if(c.moveToFirst()) {
            do {
                ids.add(c.getLong(c.getColumnIndex(BaseColumns._ID)));
            } while(c.moveToNext());
        }
        return ids;
    }

    public static int delete(Context context, Uri uri, String where) {
        return context.getContentResolver().delete(uri, where, null);
    }

    public static int deleteById(Context context, Uri uri, long id) {
        return context.getContentResolver().delete(ContentUris.withAppendedId(uri, id), null, null);
    }

    public static String concatenateWhereAND(String a, String b) {
        if (TextUtils.isEmpty(a)) {
            return b;
        }
        if (TextUtils.isEmpty(b)) {
            return a;
        }

        return "(" + a + ") AND (" + b + ")";
    }

    public static String concatenateWhereOR(String a, String b) {
        if (TextUtils.isEmpty(a)) {
            return b;
        }
        if (TextUtils.isEmpty(b)) {
            return a;
        }

        return "(" + a + ") OR (" + b + ")";
    }
}
