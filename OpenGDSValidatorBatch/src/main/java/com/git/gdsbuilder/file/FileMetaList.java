/**
 * 
 */
package com.git.gdsbuilder.file;

import java.util.ArrayList;

/**
 * @className FileMetaList.java
 * @description
 * @author DY.Oh
 * @since 2018. 2. 21. 오후 2:13:21
 */
@SuppressWarnings("serial")
public class FileMetaList extends ArrayList<FileMeta> {

	String name;
	String path;

	public FileMetaList(){
		
		
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

}
