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
 * @className NGIHeader.java
 * @description NGIHeader 정보를 담고있는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 2:20:59
 */
public class NGIHeader {

	private String version;
	private String geometric_metadata;
	private String dim;
	private String bound;
	private List<String> point_represent;
	private List<String> line_represent;
	private List<String> region_represent;
	private List<String> text_represent;

	/**
	 * constructors
	 */
	public NGIHeader() {
		this.version = "";
		this.geometric_metadata = "";
		this.dim = "";
		this.bound = "";
		this.point_represent = new ArrayList<String>();
		this.line_represent = new ArrayList<String>();
		this.region_represent = new ArrayList<String>();
		this.text_represent = new ArrayList<String>();
	}

	/**
	 * constructors
	 * 
	 * @param version
	 * @param geometric_metadata
	 * @param dim
	 * @param bound
	 * @param point_represent
	 * @param line_represent
	 * @param region_represent
	 * @param text_represent
	 */
	public NGIHeader(String version, String geometric_metadata, String dim, String bound, List<String> point_represent,
			List<String> line_represent, List<String> region_represent, List<String> text_represent) {
		super();
		this.version = version;
		this.geometric_metadata = geometric_metadata;
		this.dim = dim;
		this.bound = bound;
		this.point_represent = point_represent;
		this.line_represent = line_represent;
		this.region_represent = region_represent;
		this.text_represent = text_represent;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getGeometric_metadata() {
		return geometric_metadata;
	}

	public void setGeometric_metadata(String geometric_metadata) {
		this.geometric_metadata = geometric_metadata;
	}

	public String getDim() {
		return dim;
	}

	public void setDim(String dim) {
		this.dim = dim;
	}

	public String getBound() {
		return bound;
	}

	public void setBound(String bound) {
		this.bound = bound;
	}

	public List<String> getPoint_represent() {
		return point_represent;
	}

	public void setPoint_represent(List<String> point_represent) {
		this.point_represent = point_represent;
	}

	public List<String> getLine_represent() {
		return line_represent;
	}

	public void setLine_represent(List<String> line_represent) {
		this.line_represent = line_represent;
	}

	public List<String> getRegion_represent() {
		return region_represent;
	}

	public void setRegion_represent(List<String> region_represent) {
		this.region_represent = region_represent;
	}

	public List<String> getText_represent() {
		return text_represent;
	}

	public void setText_represent(List<String> text_represent) {
		this.text_represent = text_represent;
	}

	public void addRegion_represent(String region_represent) {
		this.region_represent.add(region_represent);
	}

}
