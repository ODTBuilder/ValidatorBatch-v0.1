/*
 *    OpenGDS/Builder
 *    http://git.co.kr
 *
 *    (C) 2014-2017, GeoSpatial Information Technology(GIT)
 *    
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 3 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package com.git.gdsbuilder.validator.fileReader.ngi.header;

import java.util.ArrayList;
import java.util.List;

/**
 * @className NDAHeader.java
 * @description NDAHeader 정보를 담고있는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 2:20:25
 */
public class NDAHeader {

	private String version;
	private List<NDAField> aspatial_field_def;

	/**
	 * constructors
	 */
	public NDAHeader() {

	}

	/**
	 * constructors
	 * 
	 * @param version
	 * @param aspatial_field_def
	 */
	public NDAHeader(String version, List<NDAField> aspatial_field_def) {
		super();
		this.version = version;
		this.aspatial_field_def = aspatial_field_def;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public List<NDAField> getAspatial_field_def() {
		return aspatial_field_def;
	}

	public void setAspatial_field_def(List<NDAField> aspatial_field_def) {
		this.aspatial_field_def = aspatial_field_def;
	}

	public void addField(NDAField field) {
		aspatial_field_def.add(field);
	}

	public List<String> getFieldNames() {
		List<String> fieldNames = new ArrayList<String>();
		for (NDAField field : this.aspatial_field_def) {
			fieldNames.add(field.getFieldName());
		}
		return fieldNames;
	}
}
