package com.git.gdsbuilder.validator.collection;

import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.geometry.jts.JTSFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.git.gdsbuilder.type.dt.collection.MapSystemRule.MapSystemRuleType;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Polygon;

/**
 * @className CloseLayerOp.java
 * @description 인접 레이어 영역내의 객체를 계산하는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 2:32:24
 */
public class CloseLayerOp {

	protected static String geomColunm = "the_geom";

	String direction;
	DTLayer typeLayer;
	DTLayer closeLayer;
	Geometry closeBoundary;

	public String getDirection() {
		return direction;
	}

	public void setDirection(String direction) {
		this.direction = direction;
	}

	public DTLayer getTypeLayer() {
		return typeLayer;
	}

	public void setTypeLayer(DTLayer typeLayer) {
		this.typeLayer = typeLayer;
	}

	public DTLayer getCloseLayer() {
		return closeLayer;
	}

	public void setCloseLayer(DTLayer closeLayer) {
		this.closeLayer = closeLayer;
	}

	public Geometry getCloseBoundary() {
		return closeBoundary;
	}

	public void setCloseBoundary(Geometry closeBoundary) {
		this.closeBoundary = closeBoundary;
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:33:07
	 * @param type
	 * @param neatLineLayer
	 * @param typeLayer
	 * @param closeLayer
	 * @param tolerance
	 * @decription 인접 레이어 영역내의 객체를 계산
	 */
	public void closeLayerOp(MapSystemRuleType type, DTLayer neatLineLayer, DTLayer typeLayer, DTLayer closeLayer,
			OptionTolerance optionTolerance) {

		double tolerance = optionTolerance.getValue();

		SimpleFeatureCollection neatLineCollection = neatLineLayer.getSimpleFeatureCollection();

		Coordinate firstPoint = null;
		Coordinate secondPoint = null;
		Coordinate thirdPoint = null;
		Coordinate fourthPoint = null;

		int i = 0;
		SimpleFeatureIterator featureIterator = neatLineCollection.features();
		while (featureIterator.hasNext()) {
			if (i == 0) {
				SimpleFeature feature = featureIterator.next();
				Geometry geometry = (Geometry) feature.getDefaultGeometry();

				Coordinate[] coordinateArray = this.getSort5Coordinate(geometry.getCoordinates());

				firstPoint = coordinateArray[0];
				secondPoint = coordinateArray[1];
				thirdPoint = coordinateArray[2];
				fourthPoint = coordinateArray[3];
				i++;
			}
		}
		featureIterator.close();

		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

		Coordinate[] boundaryCoors = null;
		LineString boundary = null;

		String direction = type.getTypeName();

		if (direction.equals(MapSystemRuleType.TOP.getTypeName())) {
			boundaryCoors = new Coordinate[] { firstPoint, secondPoint };
			boundary = geometryFactory.createLineString(boundaryCoors);
		}
		if (direction.equals(MapSystemRuleType.BOTTOM.getTypeName())) {
			boundaryCoors = new Coordinate[] { thirdPoint, fourthPoint };
			boundary = geometryFactory.createLineString(boundaryCoors);
		}
		if (direction.equals(MapSystemRuleType.LEFT.getTypeName())) {
			boundaryCoors = new Coordinate[] { firstPoint, fourthPoint };
			boundary = geometryFactory.createLineString(boundaryCoors);
		}
		if (direction.equals(MapSystemRuleType.RIGHT.getTypeName())) {
			boundaryCoors = new Coordinate[] { secondPoint, thirdPoint };
			boundary = geometryFactory.createLineString(boundaryCoors);
		}

		Polygon bufferPolygon = (Polygon) boundary.buffer(tolerance);
		Filter filter = ff.intersects(ff.property(geomColunm), ff.literal(bufferPolygon));

		SimpleFeatureSource typeSource = DataUtilities.source(typeLayer.getSimpleFeatureCollection());
		SimpleFeatureSource closeSource = DataUtilities.source(closeLayer.getSimpleFeatureCollection());

		SimpleFeatureCollection typeFtCollection = null;
		SimpleFeatureCollection closeFtCollection = null;
		try {
			// type
			typeFtCollection = typeSource.getFeatures(filter);
			// close
			closeFtCollection = closeSource.getFeatures(filter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.direction = direction;
		this.typeLayer = new DTLayer(typeLayer.getLayerID(), typeLayer.getLayerType(), typeFtCollection, null, null);
		this.closeLayer = new DTLayer(closeLayer.getLayerID(), closeLayer.getLayerType(), closeFtCollection, null,
				null);
		this.closeBoundary = boundary;
	}

	public void closeLayerOpF2(MapSystemRuleType type, DTLayer neatLineLayer, DTLayer typeLayer, DTLayer closeLayer,
			OptionTolerance optionTolerance) {

		double tolerance = optionTolerance.getValue();

		// 임상도
		SimpleFeatureCollection neatLineCollection = neatLineLayer.getSimpleFeatureCollection();
		List<Geometry> geometries = new ArrayList<>();
		SimpleFeatureIterator featureIterator = neatLineCollection.features();
		while (featureIterator.hasNext()) {
			SimpleFeature feature = featureIterator.next();
			Geometry geometry = (Geometry) feature.getDefaultGeometry();
			geometries.add(geometry);
		}
		featureIterator.close();
		Geometry polygonBoundary = null;
		if (geometries.size() > 1) {
			GeometryCollection geometryCollection = (GeometryCollection) new GeometryFactory()
					.buildGeometry(geometries);
			Geometry union = geometryCollection.union();
			polygonBoundary = union.getBoundary();
		} else {
			polygonBoundary = geometries.get(0).getBoundary();
		}
		Coordinate[] coordinateArray = this.getSort5Coordinate(polygonBoundary.getCoordinates());
		Coordinate firstPoint = coordinateArray[0];
		Coordinate secondPoint = coordinateArray[1];
		Coordinate thirdPoint = coordinateArray[2];
		Coordinate fourthPoint = coordinateArray[3];

		GeometryFactory geometryFactory = JTSFactoryFinder.getGeometryFactory();
		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

		Coordinate[] boundaryCoors = null;
		LineString boundary = null;
		String direction = type.getTypeName();
		if (direction.equals(MapSystemRuleType.TOP.getTypeName())) {
			boundaryCoors = new Coordinate[] { firstPoint, secondPoint };
			boundary = geometryFactory.createLineString(boundaryCoors);
		}
		if (direction.equals(MapSystemRuleType.BOTTOM.getTypeName())) {
			boundaryCoors = new Coordinate[] { thirdPoint, fourthPoint };
			boundary = geometryFactory.createLineString(boundaryCoors);
		}
		if (direction.equals(MapSystemRuleType.LEFT.getTypeName())) {
			boundaryCoors = new Coordinate[] { firstPoint, fourthPoint };
			boundary = geometryFactory.createLineString(boundaryCoors);
		}
		if (direction.equals(MapSystemRuleType.RIGHT.getTypeName())) {
			boundaryCoors = new Coordinate[] { secondPoint, thirdPoint };
			boundary = geometryFactory.createLineString(boundaryCoors);
		}

		Polygon bufferPolygon = (Polygon) boundary.buffer(tolerance);

		// try {
		// DefaultFeatureCollection dfc = new DefaultFeatureCollection();
		// SimpleFeatureType sfType = DataUtilities.createType("test",
		// "the_geom:Polygon");
		// CoordinateReferenceSystem worldCRS = CRS.decode("EPSG:32652");
		// CoordinateReferenceSystem dataCRS = CRS.decode("EPSG:5186");
		// MathTransform transform = CRS.findMathTransform(worldCRS, dataCRS);
		// Geometry testGeom = JTS.transform(bufferPolygon, transform);
		// SimpleFeature newSimpleFeature = SimpleFeatureBuilder.build(sfType,
		// new Object[] { testGeom }, "test");
		// dfc.add(newSimpleFeature);
		// SHPFileWriter.writeSHP("EPSG:5186", dfc,
		// "C:\\Users\\GIT\\Desktop\\test\\boundary.shp");
		// } catch (Exception e) {
		// // TODO: handle exception
		// }

		Filter filter = ff.intersects(ff.property(geomColunm), ff.literal(bufferPolygon));

		SimpleFeatureSource typeSource = DataUtilities.source(typeLayer.getSimpleFeatureCollection());
		SimpleFeatureSource closeSource = DataUtilities.source(closeLayer.getSimpleFeatureCollection());

		SimpleFeatureCollection typeFtCollection = null;
		SimpleFeatureCollection closeFtCollection = null;
		try {
			// type
			typeFtCollection = typeSource.getFeatures(filter);
			// close
			closeFtCollection = closeSource.getFeatures(filter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.direction = direction;
		this.typeLayer = new DTLayer(typeLayer.getLayerID(), typeLayer.getLayerType(), typeFtCollection, null, null);
		this.closeLayer = new DTLayer(closeLayer.getLayerID(), closeLayer.getLayerType(), closeFtCollection, null,
				null);
		this.closeBoundary = boundary;

	}

	public void closeLayerOpF(DTLayer neatLineLayer, DTLayer typeLayer, DTLayer closeLayer,
			OptionTolerance optionTolerance) {

		double tolerance = optionTolerance.getValue();

		// 임상도
		SimpleFeatureCollection neatLineCollection = neatLineLayer.getSimpleFeatureCollection();
		List<Geometry> geometries = new ArrayList<>();
		SimpleFeatureIterator featureIterator = neatLineCollection.features();
		while (featureIterator.hasNext()) {
			SimpleFeature feature = featureIterator.next();
			Geometry geometry = (Geometry) feature.getDefaultGeometry();
			geometries.add(geometry);
		}
		featureIterator.close();
		Geometry boundary = null;
		if (geometries.size() > 1) {
			GeometryCollection geometryCollection = (GeometryCollection) new GeometryFactory()
					.buildGeometry(geometries);
			Geometry union = geometryCollection.union();
			boundary = union.getBoundary();
		} else {
			boundary = geometries.get(0).getBoundary();
		}

		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

		Geometry bufferPolygon = boundary.buffer(tolerance);

		Filter filter = ff.intersects(ff.property(geomColunm), ff.literal(bufferPolygon));

		SimpleFeatureSource typeSource = DataUtilities.source(typeLayer.getSimpleFeatureCollection());
		SimpleFeatureSource closeSource = DataUtilities.source(closeLayer.getSimpleFeatureCollection());

		SimpleFeatureCollection typeFtCollection = null;
		SimpleFeatureCollection closeFtCollection = null;
		try {
			// type
			typeFtCollection = typeSource.getFeatures(filter);
			// close
			closeFtCollection = closeSource.getFeatures(filter);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		this.typeLayer = new DTLayer(typeLayer.getLayerID(), typeLayer.getLayerType(), typeFtCollection, null, null);
		this.closeLayer = new DTLayer(closeLayer.getLayerID(), closeLayer.getLayerType(), closeFtCollection, null,
				null);
		this.closeBoundary = boundary;
	}

	private Coordinate[] getSort5Coordinate(Coordinate[] coordinates) {
		Coordinate[] returncoordinate = coordinates;
		if (coordinates.length == 5) {
			double fPointY = 0.0;
			double sPointY = 0.0;

			for (int a = 0; a < returncoordinate.length - 2; a++) {
				for (int j = 0; j < returncoordinate.length - 2; j++) {
					fPointY = returncoordinate[j].y;
					sPointY = returncoordinate[j + 1].y;

					Coordinate jCoordinate = returncoordinate[j];
					Coordinate kCoordinate = returncoordinate[j + 1];

					if (fPointY < sPointY) {
						returncoordinate[j + 1] = jCoordinate;
						returncoordinate[j] = kCoordinate;
					}
				}
			}
			Coordinate firstPoint = returncoordinate[0];
			Coordinate secondPoint = returncoordinate[1];
			Coordinate thirdPoint = returncoordinate[2];
			Coordinate fourthPoint = returncoordinate[3];

			if (firstPoint.x > secondPoint.x) {
				returncoordinate[0] = secondPoint;
				returncoordinate[1] = firstPoint;
			}

			if (thirdPoint.x < fourthPoint.x) {
				returncoordinate[2] = fourthPoint;
				returncoordinate[3] = thirdPoint;
			}

			returncoordinate[4] = returncoordinate[0];
		} else
			return null;

		return returncoordinate;
	}
}
