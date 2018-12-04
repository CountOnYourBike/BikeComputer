package pl.edu.pg.eti.bikecounter;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class CounterDatabase {

    private static final String DATABASE_NAME = "COUNTER_DATABASE.db";
    private static final String WHEEL_TABLE = "WHEEL_TABLE";
    private static final int DATABASE_VERSION = 1 ;
    private final Context mContext;

    public static  String TAG = CounterDatabase.class.getSimpleName();

    private DatabaseHelper mDbHelper;
    SQLiteDatabase mDb;

    public static final String KEY_ROWID = "_id";
    public static final String ETRTO = "ETRTO";
    public static final String INCH = "Inch";
    public static final String CIRCUT = "circut";

    public static final String[] WHEEL_FIELDS = new String[]{
            KEY_ROWID,
            ETRTO,
            INCH,
            CIRCUT
    };

    private static final String CREATE_TABLE_WCHEEL =
            "create table "+ WHEEL_TABLE+"("
                +KEY_ROWID  +   " INTEGER PRIMARY KEY AUTOINCREMENT,"
                +ETRTO      +   " text,"
                +INCH       +   " text,"
                +CIRCUT     +   " INTEGER"
                +");"
            ;



    private static class DatabaseHelper extends SQLiteOpenHelper{
        DatabaseHelper (Context context){
        super(context,DATABASE_NAME,null,DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE_WCHEEL);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Database update from version "+oldVersion +" to version "+newVersion);
            //TODO usunąć strae Table, nie tylko WHEEL_TABLE
            db.execSQL("DROP TABLE IF EXISTS " + WHEEL_TABLE);
            onCreate(db);
        }
    }

    public CounterDatabase(Context context) {
        mContext = context;
    }
    public CounterDatabase open() throws SQLException{
        mDbHelper =new DatabaseHelper(mContext);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close(){
        if(mDbHelper!=null){
            mDbHelper.close();
        }
    }

    public void upgrade() throws SQLException{
        mDbHelper = new DatabaseHelper(mContext);
        mDb =mDbHelper.getWritableDatabase();
        mDbHelper.onUpgrade(mDb,0,1);
    }

    public long insertWheel(ContentValues initialVales){
        return mDb.insertWithOnConflict(WHEEL_TABLE,null,initialVales,SQLiteDatabase.CONFLICT_IGNORE);
    }

    public boolean updateWheel (int id, ContentValues newValues){
        String [] selectionArgs = {String.valueOf(id)};
        return mDb.update(WHEEL_TABLE,newValues,KEY_ROWID+"=?",selectionArgs)>0;
    }
    public boolean deleteWheel (int id){
        String [] selectionArgs = {String.valueOf(id)};
        return mDb.delete(WHEEL_TABLE,KEY_ROWID+"=?",selectionArgs)>0;
    }
    public Cursor getWheele(){
        return mDb.query(WHEEL_TABLE,WHEEL_FIELDS,null,null,null,null,null);
    }

    public static Wheel getWheelFromCursor(Cursor cursor){
        Wheel wheel= new Wheel();
        wheel.setId(cursor.getInt(cursor.getColumnIndex(KEY_ROWID)));
        wheel.setETRTO(cursor.getString(cursor.getColumnIndex(ETRTO)));
        wheel.setInch(cursor.getString(cursor.getColumnIndex(INCH)));
        wheel.setCircuit((double)cursor.getInt(cursor.getColumnIndex(CIRCUT)));
        return wheel;
    }


}
