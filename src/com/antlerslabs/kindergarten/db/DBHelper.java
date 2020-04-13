package com.antlerslabs.kindergarten.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.Date;

import com.antlerslabs.kindergarten.annotation.Column;
import com.antlerslabs.kindergarten.annotation.Table;
import com.antlerslabs.kindergarten.pojo.*;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;

class DBHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "kindergarden";
	private static final int DATABASE_VERSION = 1;

	public DBHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(extractTable(Kindergarden.class, false));
		db.execSQL(extractTable(LatestNews.class, false));
		db.execSQL(extractTable(Message.class, false));
		db.execSQL(extractTable(CalendarDetail.class, false));
		db.execSQL(extractTable(CalendarDocument.class, false));
		db.execSQL(extractTable(Admin.class, false));
		db.execSQL(extractTable(Teacher.class, false));
		db.execSQL(extractTable(Code.class, false));
		db.execSQL(extractTable(CalendarSync.class, false));
		db.execSQL(extractTable(AboutDetails.class, false));
		db.execSQL(extractTable(AboutDocument.class, false));
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL(extractTable(Kindergarden.class, true));
		db.execSQL(extractTable(LatestNews.class, true));
		db.execSQL(extractTable(Message.class, true));
		db.execSQL(extractTable(CalendarDetail.class, true));
		db.execSQL(extractTable(CalendarDocument.class, true));
		db.execSQL(extractTable(Admin.class, true));
		db.execSQL(extractTable(Teacher.class, true));
		db.execSQL(extractTable(Code.class, true));
		db.execSQL(extractTable(CalendarSync.class, true));
		db.execSQL(extractTable(AboutDetails.class, true));
		db.execSQL(extractTable(AboutDocument.class, true));
	}
	
	private String extractTable(Class<? extends BaseObject> cls, boolean withDrop) {
		Annotation aTable = cls.getAnnotation(Table.class);
		StringBuffer values = new StringBuffer();
		
		if(aTable != null && aTable instanceof Table) {
			Table table = (Table) aTable;
			
			if(withDrop)
				values.append("DROP TABLE IF EXISTS " + table.name() + ";");
			
			values.append("CREATE TABLE " + table.name() + "(");
			
			Field[] fields = cls.getDeclaredFields();
			StringBuffer columns = new StringBuffer();
			
			for(Field field: fields) {
				Annotation aColumn = field.getAnnotation(Column.class);
				
				if(aColumn != null && aColumn instanceof Column) {
					Column column = (Column) aColumn;
					
					if(field.getType().isAssignableFrom(int.class)) {
						columns.append(column.name() + " INTEGER");
						if(column.primaryKey())
							columns.append(" PRIMARY KEY");
						if(column.autoIncrement())
							columns.append(" AUTOINCREMENT");
						columns.append(",");
					} else if(field.getType().isAssignableFrom(long.class)) {
						columns.append(column.name() + " INTEGER");
						if(column.primaryKey())
							columns.append(" PRIMARY KEY");
						if(column.autoIncrement())
							columns.append(" AUTOINCREMENT");
						columns.append(","); 
					} else if(field.getType().isAssignableFrom(Date.class)) {
						columns.append(column.name() + " INTEGER");
						if(column.primaryKey())
							columns.append(" PRIMARY KEY");
						if(column.autoIncrement())
							columns.append(" AUTOINCREMENT");
						columns.append(",");
					} else if(field.getType().isAssignableFrom(double.class)) {
						columns.append(column.name() + " REAL");
						if(column.primaryKey())
							columns.append(" PRIMARY KEY");
						if(column.autoIncrement())
							columns.append(" AUTOINCREMENT");
						columns.append(",");
					} else if(field.getType().isAssignableFrom(boolean.class)) {
						columns.append(column.name() + " INTEGER");
						if(column.primaryKey())
							columns.append(" PRIMARY KEY");
						if(column.autoIncrement())
							columns.append(" AUTOINCREMENT");
						columns.append(",");
					} else if(field.getType().isAssignableFrom(Bitmap.class)) {
						columns.append(column.name() + " BLOB");
						if(column.primaryKey())
							columns.append(" PRIMARY KEY");
						if(column.autoIncrement())
							columns.append(" AUTOINCREMENT");
						columns.append(",");
					} else {
						columns.append(column.name() + " TEXT");
						if(column.primaryKey())
							columns.append(" PRIMARY KEY");
						if(column.autoIncrement())
							columns.append(" AUTOINCREMENT");
						columns.append(",");
					}
				}
			}
			values.append(columns.substring(0, columns.length() - 1));
			values.append(");");
		}
		return values.toString();
	}
}
