package org.vt.hokiehelper;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;

public class DatabaseVersion {
	private DatastoreService datastore_ = DatastoreServiceFactory.getDatastoreService();
	private String databaseName_;
	private Double databaseVersion_ = 0.0;
	
	public DatabaseVersion(String name){
		this.databaseName_ = name;
		this.updateDatabaseVersion();
	}
	
	private void updateDatabaseVersion(){
		Query q = new Query("Version");
		q.addFilter("name", Query.FilterOperator.EQUAL, this.databaseName_);
		PreparedQuery pq = datastore_.prepare(q);
		for (Entity result : pq.asIterable()) {
		  	String foundName = (String) result.getProperty("name");
			if(foundName.equals(this.databaseName_)){
				databaseVersion_ = (Double) result.getProperty("version");
				break;
			}
		}
	}
	
	public Double toDouble(){
		return databaseVersion_;
	}
	public String toString(){
		return databaseVersion_.toString();
	}
}
