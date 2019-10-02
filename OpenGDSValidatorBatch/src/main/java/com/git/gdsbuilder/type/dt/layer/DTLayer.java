package com.git.gdsbuilder.type.dt.layer;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.opengis.feature.simple.SimpleFeature;

import com.git.gdsbuilder.type.dt.collection.MapSystemRule;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;


/**
 * @className DTLayer.java
 * @description DTLayer 정보를 저장하는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 2:03:42
 */


public class DTLayer {

	String layerID;
	String layerType;
	SimpleFeatureCollection simpleFeatureCollection;
	OptionFilter filter;
	MapSystemRule mapRule; // 인접도엽 정보
	
	public DTLayer(){}
	public DTLayer(String layerID, String layerType, SimpleFeatureCollection typeFtCollection, OptionFilter filter,
			MapSystemRule mapRule) {
		this.layerID = layerID;
		this.layerType = layerType;
		this.simpleFeatureCollection = typeFtCollection;
		this.filter = filter;
		this.mapRule = mapRule;
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:03:58
	 * @param feature
	 * @decription simpleFeatureCollection에 feature를 더함
	 */
	public void addFeature(SimpleFeature feature) {
		((DefaultFeatureCollection) this.simpleFeatureCollection).add(feature);
	}

	public String getLayerID() {
		return layerID;
	}

	public void setLayerID(String layerID) {
		this.layerID = layerID;
	}

	public String getLayerType() {
		return layerType;
	}

	public void setLayerType(String layerType) {
		this.layerType = layerType;
	}

	public SimpleFeatureCollection getSimpleFeatureCollection() {
		return simpleFeatureCollection;
	}

	public void setSimpleFeatureCollection(SimpleFeatureCollection simpleFeatureCollection) {
		this.simpleFeatureCollection = simpleFeatureCollection;
	}

	public OptionFilter getFilter() {
		return filter;
	}

	public void setFilter(OptionFilter filter) {
		this.filter = filter;
	}

	public MapSystemRule getMapRule() {
		return mapRule;
	}

	public void setMapRule(MapSystemRule mapRule) {
		this.mapRule = mapRule;
	}
	
}
