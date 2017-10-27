package ua.com.intelligent_games.igrichelovekov.SQL;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class CartSql extends SQLiteOpenHelper implements BaseColumns {
    public static final String DATABASE_CART = "cart_db";
    public static final int DATABASE_VERSION = 1;

    public static final String TABLE_CART = "cart";

    public CartSql(Context context) {
        super(context, DATABASE_CART, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE `"+TABLE_CART+"` (" +
                "`id` integer NOT NULL PRIMARY KEY AUTOINCREMENT,"+
                "`post_ID` int(11) NOT NULL,"+
                "`post_date` varchar(100) NOT NULL,"+
                "`post_content` text NOT NULL,"+
                "`post_custom_excerpt` text NOT NULL,"+
                "`post_title` varchar(255) NOT NULL,"+
                "`image_thumbnail` varchar(255) NOT NULL,"+
                "`post_name` varchar(255) NOT NULL,"+
                "`post_guid` varchar(255) NOT NULL,"+
                "`postmeta_sku` int(11) NOT NULL,"+
                "`postmeta_price` double NOT NULL,"+
                "`postmeta_sale_price` double NOT NULL,"+
                "`postmeta_regular_price` double NOT NULL,"+
                "`postmeta_weight` double NOT NULL,"+
                "`postmeta_length` double NOT NULL,"+
                "`postmeta_width` double NOT NULL,"+
                "`postmeta_height` double NOT NULL,"+
                "`cart_count` int(11) NOT NULL,"+
                "`cart_date` datetime NOT NULL,"+
                "`cart_date_stamp` float NOT NULL,"+
                "`cart_order_type` varchar(255) NOT NULL,"+
                "`cart_status` tinyint(1) NOT NULL DEFAULT '0'"+
                ");");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF IT EXISTS `"+TABLE_CART+"`");
        onCreate(db);
    }
}
