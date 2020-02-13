package com.git.gdsbuilder.type.dt.layer;

import java.io.IOException;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;

import com.git.gdsbuilder.type.dt.collection.MapSystemRule;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.validator.quad.Quadtree;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;

/**
 * @className DTLayer.java
 * @description DTLayer 정보를 저장하는 클래스
 * @author DY.Oh
 * @Since 2018. 1. 30. 오후 2:03:42
 */

public class OpenDTLayer {

	String typeName;
	String layerID;
	String layerType;
	SimpleFeatureCollection simpleFeatureCollection;
	OptionFilter filter;
	OptionFigure figure;
	MapSystemRule mapRule; // 인접도엽 정보
	Quadtree quadTree = null;

	public OpenDTLayer() {
		super();
	}

	public OpenDTLayer(String layerID, String layerType, SimpleFeatureCollection simpleFeatureCollection,
			OptionFilter filter, MapSystemRule mapRule) {
		this.layerID = layerID;
		this.layerType = layerType;
		this.simpleFeatureCollection = simpleFeatureCollection;
		this.filter = filter;
		this.mapRule = mapRule;
	}

	public OpenDTLayer(String typeName, String layerID, String layerType,
			SimpleFeatureCollection simpleFeatureCollection, OptionFilter filter, OptionFigure figure,
			MapSystemRule mapRule) {
		super();
		this.typeName = typeName;
		this.layerID = layerID;
		this.layerType = layerType;
		this.simpleFeatureCollection = simpleFeatureCollection;
		this.filter = filter;
		this.figure = figure;
		this.mapRule = mapRule;
	}

	/**
	 * @author DY.Oh
	 * @Since 2018. 1. 30. 오후 2:03:58
	 * @param feature
	 * @decription simpleFeatureCollection에 feature를 더함
	 */
	public void addFeature(SimpleFeature feature) {
		((DefaultFeatureCollection) this.simpleFeatureCollection).add(feature);
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

	public OptionFigure getFigure() {
		return figure;
	}

	public void setFigure(OptionFigure figure) {
		this.figure = figure;
	}

	public MapSystemRule getMapRule() {
		return mapRule;
	}

	public void setMapRule(MapSystemRule mapRule) {
		this.mapRule = mapRule;
	}

	public Quadtree getQuadTree() {
		return quadTree;
	}

	public void setQuadTree(Quadtree quadTree) {
		this.quadTree = quadTree;
	}

	public void buildQuad() {
		if (simpleFeatureCollection != null) {
			this.quadTree = getQuadTree(simpleFeatureCollection);
		}
	}

	public Quadtree getQuadTree(SimpleFeatureCollection sfc) {

		Quadtree quad = new Quadtree();
		try {
			sfc.accepts(new FeatureVisitor() {
				@Override
				public void visit(Feature feature) {
					SimpleFeature simpleFeature = (SimpleFeature) feature;
					Geometry geom = (Geometry) simpleFeature.getDefaultGeometry();
					// Just in case: check for null or empty geometry
					if (geom != null) {
						Envelope env = geom.getEnvelopeInternal();
						if (!env.isNull()) {
							quad.insert(env, simpleFeature);
						}
					}
				}
			}, new NullProgressListener());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return quad;
	}

}
