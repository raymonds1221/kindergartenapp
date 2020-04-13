package com.antlerslabs.kindergarten.db;

import java.util.List;

import com.antlerslabs.kindergarten.pojo.BaseObject;

public interface DataSource {
	public DataSource where(String source, String query);
	public DataSource where(String source, int query);
	public DataSource andWhere(String source, String query);
	public DataSource orWhere(String source, String query);
	public DataSource like(String source, String query);
	public DataSource likeWhere(String source, String query);
	public DataSource likeOrWhere(String source, String query);
	public DataSource likeInWhere(String query, String... sources);
	public DataSource orderBy(String source);
	public DataSource orderBy(String source, String orderby);
	public DataSource groupBy(String column);
	public DataSource limit(int limit);
	public DataSource limit(int start, int end);
	public DataSource select(String... columns);
	public DataSource rawQuery(String rawQuery);
	public long insert(BaseObject baseObject);
	public void insertAll(List<? extends BaseObject> baseObjects);
	public void update(BaseObject baseObject);
	public <T> void delete(Class<T> cls);
	public <T> T getForObject(Class<T> cls);
	public <T> List<T> getForObjects(Class<T> cls);
	public int getRecordCount();
}
