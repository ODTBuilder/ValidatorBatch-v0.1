/**
 * 
 */
package com.git.gdsbuilder.type.dt.feature;

import java.util.List;

import org.opengis.feature.simple.SimpleFeature;

import com.git.gdsbuilder.type.validate.option.specific.AttributeFilter;

/**
 * @className DTFeature.java
 * @description
 * @author DY.Oh
 * @date 2018. 3. 15. 오후 6:25:07
 */

public class DTFeature {

	String typeName;
	String layerID;
	SimpleFeature simefeature;
	List<AttributeFilter> filter;

	public DTFeature() {
	}

	public DTFeature(String layerID, SimpleFeature simplefeature, List<AttributeFilter> filter) {
		this.layerID = layerID;
		this.simefeature = simplefeature;
		this.filter = filter;
	}

	public DTFeature(String typeName, String layerID, SimpleFeature simplefeature, List<AttributeFilter> filter) {
		this.typeName = typeName;
		this.layerID = layerID;
		this.simefeature = simplefeature;
		this.filter = filter;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getLayerID() {
		return layerID;
	}

	public void setLayerID(String layerID) {
		this.layerID = layerID;
	}

	public SimpleFeature getSimefeature() {
		return simefeature;
	}

	public void setSimefeature(SimpleFeature simefeature) {
		this.simefeature = simefeature;
	}

	public List<AttributeFilter> getFilter() {
		return filter;
	}

	public void setFilter(List<AttributeFilter> filter) {
		this.filter = filter;
	}

}
