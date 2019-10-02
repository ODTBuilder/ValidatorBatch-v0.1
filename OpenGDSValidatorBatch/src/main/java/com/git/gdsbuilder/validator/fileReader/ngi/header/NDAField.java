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

/**
 * @className NDAField.java
 * @description NGIField 정보를 담고있는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 2:18:15
 */
public class NDAField {

	String originFieldName;
	String fieldName;
	String type;
	String size;
	String decimal;
	boolean isUnique;
	boolean isNotNull;

	/**
	 * constructors
	 */
	public NDAField() {
		super();
	}

	/**
	 * constructors
	 * 
	 * @param originFieldName
	 * @param fieldName
	 * @param type
	 * @param size
	 * @param decimal
	 * @param isUnique
	 * @param isNotNull
	 */
	public NDAField(String originFieldName, String fieldName, String type, String size, String decimal,
			boolean isUnique, boolean isNotNull) {
		super();
		this.originFieldName = originFieldName;
		this.fieldName = fieldName;
		this.type = type;
		this.size = size;
		this.decimal = decimal;
		this.isUnique = isUnique;
		this.isNotNull = isNotNull;
	}

	/**
	 * constructors
	 * 
	 * @param fieldName
	 * @param type
	 * @param size
	 * @param decimal
	 * @param isUnique
	 * @param isNotNull
	 */
	public NDAField(String fieldName, String type, String size, String decimal, boolean isUnique, boolean isNotNull) {
		super();
		this.fieldName = fieldName;
		this.type = type;
		this.size = size;
		this.decimal = decimal;
		this.isUnique = isUnique;
		this.isNotNull = isNotNull;
	}

	/**
	 * constructors
	 * 
	 * @param fieldName
	 * @param type
	 * @param size
	 * @param decimal
	 * @param isUnique
	 */
	public NDAField(String fieldName, String type, String size, String decimal, boolean isUnique) {
		super();
		this.fieldName = fieldName;
		this.type = type;
		this.size = size;
		this.decimal = decimal;
		this.isUnique = isUnique;
	}

	public String getOriginFieldName() {
		return originFieldName;
	}

	public void setOriginFieldName(String originFieldName) {
		this.originFieldName = originFieldName;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	/**
	 * decimal getter @author DY.Oh @since 2017. 3. 11. 오후 2:40:08 @return
	 * String @throws
	 */
	public String getDecimal() {
		return decimal;
	}

	public void setDecimal(String decimal) {
		this.decimal = decimal;
	}

	public boolean isUnique() {
		return isUnique;
	}

	public void setUnique(boolean isUnique) {
		this.isUnique = isUnique;
	}

}
