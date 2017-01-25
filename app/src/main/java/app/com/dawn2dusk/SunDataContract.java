package app.com.dawn2dusk;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by Akshay on 23-01-2017.
 */

public class SunDataContract {
    public static final String CONTENT_AUTHORITY = "app.com.dawn2dusk.content_provider";

    // A path that points to the version table
    public static final String PATH = "sundata";

    // Construct the Base Content Uri
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final class SunDataEntry implements BaseColumns {

        public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH).build();

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH;

        public static final String TABLE_NAME = "sundata";
        public static final String COLUMN_SUNDATA_ID = "sundata_id";
        public static final String COLUMN_SUNRISE = "sunrise";
        public static final String COLUMN_SUNSET = "sunset";
        public static final String COLUMN_DAY_LENGTH = "day_length";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_LAT = "lat";
        public static final String COLUMN_LNG = "lng";
        public static final String COLUMN_ADR = "adr";
        // Define projection for Version table
        public static final String[] PROJECTION = new String[]{
                /*0*/ SunDataEntry.COLUMN_SUNDATA_ID,
                /*1*/ SunDataEntry.COLUMN_SUNRISE,
                /*2*/ SunDataEntry.COLUMN_SUNSET,
                /*3*/ SunDataEntry.COLUMN_DAY_LENGTH,
                /*4*/ SunDataEntry.COLUMN_DATE,
                /*5*/SunDataEntry.COLUMN_LAT,
                /*6*/SunDataEntry.COLUMN_LNG,
                /*7*/SunDataEntry.COLUMN_ADR
        };

        public static Uri buildSundataUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
