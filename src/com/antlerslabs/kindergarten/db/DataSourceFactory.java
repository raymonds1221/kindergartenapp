package com.antlerslabs.kindergarten.db;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.io.ByteArrayOutputStream;
import java.util.Date;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.antlerslabs.kindergarten.annotation.Column;
import com.antlerslabs.kindergarten.annotation.Table;
import com.antlerslabs.kindergarten.pojo.BaseObject;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class DataSourceFactory implements DataSource {
	private DBHelper mVikingDBHelper;
	private SQLiteDatabase mDatabase;
	private String[] mColumns;
	private String mOrderBy;
	private String mGroupBy;
	private String mLimit;
	private StringBuffer whereClause = new StringBuffer();
	private int mRecordCount;
	
	private DataSourceFactory(Context context) {
		whereClause = new StringBuffer();
		mVikingDBHelper = new DBHelper(context);
	}
	
	public static final DataSource getDataSourceFactory(Context context) {
		return new DataSourceFactory(context);
	}
	
	@Override
	public DataSource where(String source, String query) {
		whereClause = new StringBuffer();
		whereClause.append(String.format(" %s='%s'", source, query));
		return this;
	}
	
	@Override
	public DataSource where(String source, int query) {
		whereClause = new StringBuffer();
		whereClause.append(String.format(" %s=%s", source, String.valueOf(query)));
		return this;
	}

	@Override
	public DataSource andWhere(String source, String query) {
		whereClause.append(String.format(" and %s='%s'", source, query));
		return this;
	}

	@Override
	public DataSource orWhere(String source, String query) {
		whereClause.append(String.format(" or %s='%s'", source, query));
		return this;
	}
	
	@Override
	public DataSource like(String source, String query) {
		whereClause.append(" " + source + " like '%" + query + "%'");
		return this;
	}
	
	
	@Override
	public DataSource likeWhere(String source, String query) {
		whereClause.append(" and " + source + " like '%" + query + "%'");
		return this;
	}
	
	@Override
	public DataSource likeOrWhere(String source, String query) {
		whereClause.append(" or " + source + " like '%" + query + "%'");
		return this;
	}
	
	@Override
	public DataSource likeInWhere(String query, String... sources) {
		String whereStmt = " and (";
		
		for(int i=0;i<sources.length;i++) {
			if(i == 0) {
				whereStmt += sources[i] + " like '%" + query + "%'";
			} else {
				whereStmt += " or " + sources[i] + " like '%" + query + "%'";
			}
		}
		
		whereStmt += ")";
		whereClause.append(whereStmt);
		return this;
	}
	
	@Override
	public DataSource rawQuery(String rawQuery) {
		whereClause.append(rawQuery);
		return this;
	}
	
	@Override
	public DataSource orderBy(String source) {
		mOrderBy = source;
		return this;
	}

	@Override
	public DataSource orderBy(String source, String orderby) {
		mOrderBy = source + " " + orderby;
		return this;
	}
	
	@Override
	public DataSource groupBy(String column) {
		mGroupBy = column;
		return this;
	}

	@Override
	public DataSource limit(int limit) {
		mLimit = String.valueOf(limit);
		return this;
	}

	@Override
	public DataSource limit(int start, int end) {
		mLimit = start + "," + end;
		return this;
	}

	@Override
	public DataSource select(String... columns) {
		this.mColumns = columns;
		return this;
	}

	@Override
	public synchronized long insert(BaseObject baseObject) {
		mDatabase = mVikingDBHelper.getWritableDatabase();
		Annotation aTable = baseObject.getClass().getAnnotation(Table.class);
		
		try {
			if(aTable != null && aTable instanceof Table) {
				Table table = (Table) aTable;
				Field[] fields = baseObject.getClass().getDeclaredFields();
				ContentValues values = new ContentValues();
				
				for(Field field: fields) {
					field.setAccessible(true);
					Annotation aColumn = field.getAnnotation(Column.class);
					
					if(aColumn != null && aColumn instanceof Column) {
						Column column = (Column) aColumn;
						
						if(column.allowChange()) {
							if(field.getType().isPrimitive()) {
								if(field.getType().isAssignableFrom(int.class)) {
									values.put(column.name(), field.getInt(baseObject));
								} else if(field.getType().isAssignableFrom(double.class)) {
									values.put(column.name(), field.getDouble(baseObject));
								} else if(field.getType().isAssignableFrom(boolean.class)) {
									values.put(column.name(), field.getBoolean(baseObject)?1:0);
								} else if(field.getType().isAssignableFrom(long.class)) {
									values.put(column.name(), field.getLong(baseObject));
								}
							} else {
								if(field.getType().isAssignableFrom(Date.class)) {
									Method method = field.get(baseObject).getClass().getMethod("getTime", new Class[0]);
									long milliseconds = Long.parseLong(method.invoke(field.get(baseObject), new Object[0]).toString());
									values.put(column.name(), milliseconds);
								} else if(field.getType().isAssignableFrom(Bitmap.class)) {
									Bitmap bitmap = (Bitmap) field.get(baseObject);
									if(bitmap != null) {
										ByteArrayOutputStream stream = new ByteArrayOutputStream();
										bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
										values.put(column.name(), stream.toByteArray());
									}
								} else {
									if(field.get(baseObject) == null) {
										values.put(column.name(), "");
									} else {
										values.put(column.name(), (field.get(baseObject) == null)?"":field.get(baseObject).toString());
									}
								}
							}
						}
					}
				}
				
				return mDatabase.insertOrThrow(table.name(), null, values);
			}
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		} catch (SecurityException e) {
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} finally {
			mDatabase.close();
			clear();
		}
		
		return 0;
	}
	
	@Override
	public synchronized void insertAll(List<? extends BaseObject> baseObjects) {
		mDatabase = mVikingDBHelper.getWritableDatabase();
		
		mDatabase.beginTransaction();
		
		try {
			for(BaseObject baseObject: baseObjects) {
				Annotation aTable = baseObject.getClass().getAnnotation(Table.class);
				ContentValues values = new ContentValues();
				
				try {
					if(aTable != null && aTable instanceof Table) {
						Table table = (Table) aTable;
						Field[] fields = baseObject.getClass().getDeclaredFields();
						
						for(Field field: fields) {
							field.setAccessible(true);
							Annotation aColumn = field.getAnnotation(Column.class);
							
							if(aColumn != null && aColumn instanceof Column) {
								Column column = (Column) aColumn;
								
								if(column.allowChange()) {
									if(field.getType().isPrimitive()) {
										if(field.getType().isAssignableFrom(int.class)) {
											values.put(column.name(), field.getInt(baseObject));
										} else if(field.getType().isAssignableFrom(double.class)) {
											values.put(column.name(), field.getDouble(baseObject));
										} else if(field.getType().isAssignableFrom(boolean.class)) {
											values.put(column.name(), field.getBoolean(baseObject)?1:0);
										} else if(field.getType().isAssignableFrom(long.class)) {
											values.put(column.name(), field.getLong(baseObject));
										}
									} else {
										if(field.getType().isAssignableFrom(Date.class)) {
											if(field.get(baseObject) != null) {
												Method method = field.get(baseObject).getClass().getMethod("getTime", new Class[0]);
												long milliseconds = Long.parseLong(method.invoke(field.get(baseObject), new Object[0]).toString());
												values.put(column.name(), milliseconds);
											}
										} else if(field.getType().isAssignableFrom(Bitmap.class)) {
											Bitmap bitmap = (Bitmap) field.get(baseObject);
											if(bitmap != null) {
												ByteArrayOutputStream stream = new ByteArrayOutputStream();
												bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
												values.put(column.name(), stream.toByteArray());
											}
										} else {
											values.put(column.name(), (field.get(baseObject) == null)?"":field.get(baseObject).toString());
										}
									}
								}
							}
						}
						
						mDatabase.insert(table.name(), null, values);
					}
				} catch(IllegalAccessException e) {
					e.printStackTrace();
				} catch (NumberFormatException e) {
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					e.printStackTrace();
				} catch (SecurityException e) {
					e.printStackTrace();
				} catch (NoSuchMethodException e) {
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					e.printStackTrace();
				}
			}
			mDatabase.setTransactionSuccessful();
		} finally {
			mDatabase.endTransaction();
			mDatabase.close();
			clear();
		}
	}

	@Override
	public synchronized void update(BaseObject baseObject) {
		mDatabase = mVikingDBHelper.getWritableDatabase();
		Annotation aTable = baseObject.getClass().getAnnotation(Table.class);
		
		try {
			if(aTable != null && aTable instanceof Table) {
				Table table = (Table) aTable;
				Field[] fields = baseObject.getClass().getDeclaredFields();
				ContentValues values = new ContentValues();
				
				for(Field field: fields) {
					field.setAccessible(true);
					Annotation aColumn = field.getAnnotation(Column.class);
					
					if(aColumn != null && aColumn instanceof Column) {
						Column column = (Column) aColumn;
						
						if(column.allowChange()) {
							if(field.getType().isPrimitive()) {
								if(field.getType().isAssignableFrom(int.class)) {
									values.put(column.name(), field.getInt(baseObject));
								} else if(field.getType().isAssignableFrom(double.class)) {
									values.put(column.name(), field.getDouble(baseObject));
								} else if(field.getType().isAssignableFrom(boolean.class)) {
									values.put(column.name(), field.getBoolean(baseObject)?1:0);
								} else if(field.getType().isAssignableFrom(long.class)) {
									values.put(column.name(), field.getLong(baseObject));
								}
							} else {
								if(field.getType().isAssignableFrom(Date.class)) {
									Date date = (Date) field.get(baseObject);
									values.put(column.name(), date.getTime());
								} else if(field.getType().isAssignableFrom(Bitmap.class)) {
									Bitmap bitmap = (Bitmap) field.get(baseObject);
									if(bitmap != null) {
										ByteArrayOutputStream stream = new ByteArrayOutputStream();
										bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
										values.put(column.name(), stream.toByteArray());
									}
								}else {
									values.put(column.name(), field.get(baseObject).toString());
								}
							}
						}
					}
				}
				
				mDatabase.update(table.name(), values, whereClause.toString(), null);
			}
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			mDatabase.close();
			clear();
		}
	}

	@Override
	public synchronized <T> void delete(Class<T> cls) {
		mDatabase = mVikingDBHelper.getWritableDatabase();
		Annotation aTable = cls.getAnnotation(Table.class);
		
		try {
			if(aTable != null && aTable instanceof Table) {
				Table table = (Table) aTable;
				mDatabase.delete(table.name(), whereClause.toString(), null);
			}
		} finally {
			mDatabase.close();
			clear();
		}
	}

	@Override
	public synchronized <T> T getForObject(Class<T> cls) {
		mDatabase = mVikingDBHelper.getReadableDatabase();
		Annotation aTable = cls.getAnnotation(Table.class);
		T t = null;
		
		try {
			t = cls.newInstance();
			if(aTable != null && aTable instanceof Table) {
				Table table = (Table) aTable;
				Cursor cursor = mDatabase.query(table.name(), 
									mColumns, whereClause.toString(), null, mGroupBy, null, mOrderBy, mLimit);
				mRecordCount = cursor.getCount();
				
				if(cursor.moveToNext()) {
					Field[] fields = cls.getDeclaredFields();
					
					for(Field field: fields) {
						Annotation aColumn = field.getAnnotation(Column.class);
						
						if(aColumn != null && aColumn instanceof Column) {
							Column column = (Column) aColumn;
							field.setAccessible(true);
							
							if(mColumns != null) {
								if(Arrays.asList(mColumns).contains(column.name())) {
									if(field.getType().isPrimitive()) {
										if(field.getType().isAssignableFrom(int.class)) {
											field.setInt(t, cursor.getInt(cursor.getColumnIndex(column.name())));
										} else if(field.getType().isAssignableFrom(double.class)) {
											field.setDouble(t, cursor.getDouble(cursor.getColumnIndex(column.name())));
										} else if(field.getType().isAssignableFrom(boolean.class)) {
											field.setBoolean(t, cursor.getInt(cursor.getColumnIndex(column.name())) == 1);
										} else if(field.getType().isAssignableFrom(long.class)) {
											field.setLong(t, cursor.getLong(cursor.getColumnIndex(column.name())));
										}
									} else {
										if(field.getType().isAssignableFrom(Date.class)) {
											long milliseconds = cursor.getLong(cursor.getColumnIndex(column.name()));
											field.set(t, new Date(milliseconds));
										} else {
											field.set(t, cursor.getString(cursor.getColumnIndex(column.name())));
										}
									}
								}
							} else {
								if(field.getType().isPrimitive()) {
									if(field.getType().isAssignableFrom(int.class)) {
										field.set(t, cursor.getInt(cursor.getColumnIndex(column.name())));
									} else if(field.getType().isAssignableFrom(double.class)) {
										field.set(t, cursor.getDouble(cursor.getColumnIndex(column.name())));
									} else if(field.getType().isAssignableFrom(boolean.class)) {
										field.setBoolean(t, cursor.getInt(cursor.getColumnIndex(column.name())) == 1);
									} else if(field.getType().isAssignableFrom(long.class)) {
										field.setLong(t, cursor.getLong(cursor.getColumnIndex(column.name())));
									}
								} else {
									if(field.getType().isAssignableFrom(Date.class)) {
										long milliseconds = cursor.getLong(cursor.getColumnIndex(column.name()));
										field.set(t, new Date(milliseconds));
									} else if(field.getType().isAssignableFrom(Bitmap.class)) {
										byte[] data = cursor.getBlob(cursor.getColumnIndex(column.name()));
										if(data != null) {
											Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
											field.set(t, bitmap);
										}
									} else {
										field.set(t, cursor.getString(cursor.getColumnIndex(column.name())));
									}
								}
							}
						}
					}
				}
				cursor.close();
			}
		} catch(InstantiationException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			mDatabase.close();
			clear();
		}
		
		return t;
	}

	@Override
	public synchronized <T> List<T> getForObjects(Class<T> cls) {
		List<T> values = new ArrayList<T>();
		mDatabase = mVikingDBHelper.getReadableDatabase();
		Annotation aTable = cls.getAnnotation(Table.class);
		
		try {
			if(aTable != null && aTable instanceof Table) {
				Table table = (Table) aTable;
				Cursor cursor = mDatabase.query(table.name(), 
									mColumns, whereClause.toString(), null, mGroupBy, null, mOrderBy, mLimit);
				mRecordCount = cursor.getCount();
				while(cursor.moveToNext()) {
					T t = cls.newInstance();
					Field[] fields = cls.getDeclaredFields();
					
					for(Field field: fields) {
						Annotation aColumn = field.getAnnotation(Column.class);
						
						if(aColumn != null && aColumn instanceof Column) {
							Column column = (Column) aColumn;
							field.setAccessible(true);
							
							if(mColumns != null) {
								if(Arrays.asList(mColumns).contains(column.name())) {
									if(field.getType().isPrimitive()) {
										if(field.getType().isAssignableFrom(int.class)) {
											field.setInt(t, cursor.getInt(cursor.getColumnIndex(column.name())));
										} else if(field.getType().isAssignableFrom(double.class)) {
											field.setDouble(t, cursor.getDouble(cursor.getColumnIndex(column.name())));
										} else if(field.getType().isAssignableFrom(boolean.class)) {
											field.setBoolean(t, cursor.getInt(cursor.getColumnIndex(column.name())) == 1);
										} else if(field.getType().isAssignableFrom(long.class)) {
											field.setLong(t, cursor.getLong(cursor.getColumnIndex(column.name())));
										}
									} else {
										if(field.getType().isAssignableFrom(Date.class)) {
											long milliseconds = cursor.getLong(cursor.getColumnIndex(column.name()));
											field.set(t, new Date(milliseconds));
										} else {
											field.set(t, cursor.getString(cursor.getColumnIndex(column.name())));
										}
									}
								}
							} else {
								if(field.getType().isPrimitive()) {
									if(field.getType().isAssignableFrom(int.class)) {
										field.set(t, cursor.getInt(cursor.getColumnIndex(column.name())));
									} else if(field.getType().isAssignableFrom(double.class)) {
										field.set(t, cursor.getDouble(cursor.getColumnIndex(column.name())));
									} else if(field.getType().isAssignableFrom(boolean.class)) {
										field.setBoolean(t, cursor.getInt(cursor.getColumnIndex(column.name())) == 1);
									} else if(field.getType().isAssignableFrom(long.class)) {
										field.setLong(t, cursor.getLong(cursor.getColumnIndex(column.name())));
									}
								} else {
									if(field.getType().isAssignableFrom(Date.class)) {
										long milliseconds = cursor.getLong(cursor.getColumnIndex(column.name()));
										field.set(t, new Date(milliseconds));
									} else if(field.getType().isAssignableFrom(Bitmap.class)) {
										byte[] data = cursor.getBlob(cursor.getColumnIndex(column.name()));
										
										if(data != null) {
											Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);
											field.set(t, bitmap);
										}
									} else {
										field.set(t, cursor.getString(cursor.getColumnIndex(column.name())));
									}
								}
							}
						}
					}
					
					values.add(t);
				}
				cursor.close();
			}
		} catch(InstantiationException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		} finally {
			mDatabase.close();
			clear();
		}
		
		return values;
	}
	
	private void clear() {
		mColumns = null;
		whereClause = new StringBuffer();
		mGroupBy = "";
		mOrderBy = "";
		mLimit = "";
	}
	
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}

	@Override
	public int getRecordCount() {
		return mRecordCount;
	}
}
