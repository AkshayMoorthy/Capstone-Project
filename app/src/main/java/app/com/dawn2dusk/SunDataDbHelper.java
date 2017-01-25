package app.com.dawn2dusk;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.net.PortUnreachableException;

/**
 * Created by Akshay on 23-01-2017.
 */
public class SunDataDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION=1;
    private static final String DATABASE_NAME="sundata.db";
    public SunDataDbHelper(Context context) {
        super(context, DATABASE_NAME,null,DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        final String SQL_CREATE_SUNDATA_TABLE="CREATE TABLE "+ SunDataContract.SunDataEntry.TABLE_NAME+"("+
                SunDataContract.SunDataEntry.COLUMN_SUNDATA_ID+" INTEGER, "+
                SunDataContract.SunDataEntry.COLUMN_SUNRISE+" TEXT, "+
                SunDataContract.SunDataEntry.COLUMN_SUNSET+" TEXT, "+
                SunDataContract.SunDataEntry.COLUMN_DAY_LENGTH+" TEXT, "+
                SunDataContract.SunDataEntry.COLUMN_DATE+" TEXT, "+
                SunDataContract.SunDataEntry.COLUMN_LAT+" TEXT, "+
                SunDataContract.SunDataEntry.COLUMN_LNG+" TEXT, "+
                SunDataContract.SunDataEntry.COLUMN_ADR+" TEXT"+");";
                sqLiteDatabase.execSQL(SQL_CREATE_SUNDATA_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS "+ SunDataContract.SunDataEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
