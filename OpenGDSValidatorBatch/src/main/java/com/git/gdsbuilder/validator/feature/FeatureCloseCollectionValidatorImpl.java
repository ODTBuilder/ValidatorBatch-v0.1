package com.git.gdsbuilder.validator.feature;

import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;

import com.git.gdsbuilder.type.dt.feature.DTFeature;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.validate.error.ErrorFeature;
import com.git.gdsbuilder.type.validate.option.en.LangType;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFigure;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.git.gdsbuilder.type.validate.option.type.DMQAOptions;
import com.git.gdsbuilder.validator.feature.filter.FeatureFilter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public class FeatureCloseCollectionValidatorImpl implements FeatureCloseCollectionValidator {

	LangType langType;

	public FeatureCloseCollectionValidatorImpl(LangType langType) {
		this.langType = langType;
	}

	@Override
	public ErrorFeature validateFRefEntityNone(DTFeature feature, DTLayer closeLayer, Geometry closeBoundary,
			OptionTolerance tolerance) {

		SimpleFeature sf = feature.getSimefeature();
		String featureID = sf.getID();
		boolean isTrue = false;
		List<AttributeFilter> attrFilters = feature.getFilter();
		if (attrFilters != null) {
			isTrue = FeatureFilter.filter(sf, attrFilters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		Double value = tolerance.getValue();
		SimpleFeature maxSf = null;

		if (isTrue) {
			Geometry geomBuffer = geometry.buffer(value * 2);
			SimpleFeatureCollection closeSfc = closeLayer.getSimpleFeatureCollection();
			OptionFilter closeFilter = closeLayer.getFilter();
			List<AttributeFilter> closeAttrFilters = null;
			if (closeFilter != null) {
				closeAttrFilters = closeFilter.getFilter();
			}
			double maxValue = 0.0;
			SimpleFeatureIterator closeIter = closeSfc.features();
			if (closeIter == null) {
				return null;
			}
			while (closeIter.hasNext()) {
				SimpleFeature closeSf = closeIter.next();
				boolean isCloseTrue = false;
				if (closeAttrFilters != null) {
					isCloseTrue = FeatureFilter.filter(closeSf, closeAttrFilters);
				} else {
					isCloseTrue = true;
				}
				if (isCloseTrue) {
					Geometry closeGeom = (Geometry) closeSf.getDefaultGeometry();
					Geometry closeBuffer = closeGeom.buffer(value);
					Geometry intersection = closeBuffer.intersection(geomBuffer);
					if (!intersection.isEmpty()) {
						String interType = intersection.getGeometryType();
						if (interType.equals("Polygon") || interType.equals("MultiPolygon")) {
							double area = intersection.getArea();
							if (maxValue < area) {
								maxSf = closeSf;
								maxValue = area;
							}
						}
						if (interType.equals("LineString") || interType.equals("MultiLineString")) {
							double length = intersection.getLength();
							if (maxValue < length) {
								maxSf = closeSf;
								maxValue = length;
							}
						}
					}
				}
			}
			closeIter.close();
			if (maxSf == null) {
				isError = true;
			}
			// else {
			// if (maxValue < value) {
			// isError = true;
			// }
			// }
		}
		if (isError) {
			Coordinate[] coors = geometry.getCoordinates();
			Point minDistPt = null;
			GeometryFactory geometryFactory = new GeometryFactory();
			for (int i = 0; i < coors.length; i++) {
				Point tmpPt = geometryFactory.createPoint(coors[i]);
				double tarDist = closeBoundary.distance(tmpPt);
				if (tarDist >= 0 && tarDist <= value) {
					minDistPt = tmpPt;
					break;
				}
			}
			if (minDistPt == null) {
				return null;
			}
			ErrorFeature errFeature = new ErrorFeature(featureID, DMQAOptions.Type.REFENTITYNONE.getErrCode(),
					DMQAOptions.Type.REFENTITYNONE.getErrTypeE(), DMQAOptions.Type.REFENTITYNONE.getErrName(langType),
					"", minDistPt);
			return errFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateDRefEntityNone(DTFeature feature, DTLayer closeLayer, Geometry closeBoundary,
			OptionTolerance tolerance) {

		SimpleFeature sf = feature.getSimefeature();
		String featureID = sf.getID();
		boolean isTrue = false;
		List<AttributeFilter> attrFilters = feature.getFilter();
		if (attrFilters != null) {
			isTrue = FeatureFilter.filter(sf, attrFilters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		Double value = tolerance.getValue();
		SimpleFeature maxSf = null;

		if (isTrue) {
			Geometry geomBuffer = geometry.buffer(value * 2);
			SimpleFeatureCollection closeSfc = closeLayer.getSimpleFeatureCollection();
			OptionFilter closeFilter = closeLayer.getFilter();
			List<AttributeFilter> closeAttrFilters = null;
			if (closeFilter != null) {
				closeAttrFilters = closeFilter.getFilter();
			}
			double maxValue = 0.0;
			SimpleFeatureIterator closeIter = closeSfc.features();
			if (closeIter == null) {
				return null;
			}
			while (closeIter.hasNext()) {
				SimpleFeature closeSf = closeIter.next();
				boolean isCloseTrue = false;
				if (closeAttrFilters != null) {
					isCloseTrue = FeatureFilter.filter(closeSf, closeAttrFilters);
				} else {
					isCloseTrue = true;
				}
				if (isCloseTrue) {
					Geometry closeGeom = (Geometry) closeSf.getDefaultGeometry();
					Geometry closeBuffer = closeGeom.buffer(value);
					Geometry intersection = closeBuffer.intersection(geomBuffer);
					if (!intersection.isEmpty()) {
						String interType = intersection.getGeometryType();
						if (interType.equals("Polygon") || interType.equals("MultiPolygon")) {
							double area = intersection.getArea();
							if (maxValue < area) {
								maxSf = closeSf;
								maxValue = area;
							}
						}
						if (interType.equals("LineString") || interType.equals("MultiLineString")) {
							double length = intersection.getLength();
							if (maxValue < length) {
								maxSf = closeSf;
								maxValue = length;
							}
						}
					}
				}
			}
			closeIter.close();
			if (maxSf == null) {
				isError = true;
			}
			// else {
			// if (maxValue < value) {
			// isError = true;
			// }
			// }
		}
		if (isError) {
			Coordinate[] coors = geometry.getCoordinates();
			Point minDistPt = null;
			GeometryFactory geometryFactory = new GeometryFactory();
			for (int i = 0; i < coors.length; i++) {
				Point tmpPt = geometryFactory.createPoint(coors[i]);
				double tarDist = closeBoundary.distance(tmpPt);
				if (tarDist >= 0 && tarDist <= value) {
					minDistPt = tmpPt;
					break;
				}
			}
			if (minDistPt == null) {
				return null;
			}
			ErrorFeature errFeature = new ErrorFeature(featureID, DMQAOptions.Type.REFENTITYNONE.getErrCode(),
					DMQAOptions.Type.REFENTITYNONE.getErrTypeE(), DMQAOptions.Type.REFENTITYNONE.getErrName(langType),
					"", minDistPt);
			return errFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateRefZValueMiss(DTFeature feature, DTLayer closeLayer, Geometry closeBoundary,
			OptionTolerance tolerance, OptionFigure figure) {

		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> attrFilters = feature.getFilter();
		if (attrFilters != null) {
			isTrue = FeatureFilter.filter(sf, attrFilters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		SimpleFeature maxSf = null;
		if (isTrue) {
			double value = tolerance.getValue();
			Geometry geomBuffer = geometry.buffer(value * 2);

			SimpleFeatureCollection closeSfc = closeLayer.getSimpleFeatureCollection();
			OptionFilter closeFilter = closeLayer.getFilter();
			List<AttributeFilter> closeAttrFilters = null;
			if (closeFilter != null) {
				closeAttrFilters = closeFilter.getFilter();
			}
			double maxArea = 0.0;
			SimpleFeatureIterator closeIter = closeSfc.features();
			if (closeIter == null) {
				return null;
			}
			while (closeIter.hasNext()) {
				SimpleFeature closeSf = closeIter.next();
				boolean isCloseTrue = false;
				if (closeAttrFilters != null) {
					isCloseTrue = FeatureFilter.filter(closeSf, closeAttrFilters);
				} else {
					isCloseTrue = true;
				}
				if (isCloseTrue) {
					Geometry closeGeom = (Geometry) closeSf.getDefaultGeometry();
					Geometry closeBuffer = closeGeom.buffer(value);
					Geometry intersection = closeBuffer.intersection(geomBuffer);
					if (!intersection.isEmpty()) {
						double area = intersection.getArea();
						if (maxArea < area) {
							maxSf = closeSf;
							maxArea = area;
						}
					}
				}
			}
			closeIter.close();
			if (maxSf != null) {
				List<AttributeFigure> attrFigures = figure.getFigure();
				for (AttributeFigure attrFigure : attrFigures) {
					String attriKey = attrFigure.getKey();
					Property relationProperty = maxSf.getProperty(attriKey);
					Property targetProperty = sf.getProperty(attriKey);
					if (relationProperty != null && targetProperty != null) {
						Object rePropertyValue = relationProperty.getValue();
						Object tarPropertyValue = targetProperty.getValue();
						if (rePropertyValue != null && tarPropertyValue != null) {
							String reValueStr = rePropertyValue.toString();
							String tarValueStr = tarPropertyValue.toString();
							if (!reValueStr.equals(tarValueStr)) {
								isError = true;
								break;
							}
						} else {
							isError = true;
							break;
						}
					} else {
						isError = true;
						break;
					}
				}
			}
		}
		if (isError && maxSf != null) {
			Double value = tolerance.getValue();
			String featureID = sf.getID();
			String reFeatureID = maxSf.getID();
			Coordinate[] coors = geometry.getCoordinates();
			Point minDistPt = null;
			GeometryFactory geometryFactory = new GeometryFactory();
			for (int i = 0; i < coors.length; i++) {
				Point tmpPt = geometryFactory.createPoint(coors[i]);
				double tarDist = closeBoundary.distance(tmpPt);
				if (tarDist >= 0 && tarDist <= value) {
					minDistPt = tmpPt;
					break;
				}
			}
			if (minDistPt == null) {
				return null;
			}

			String reLayerId = closeLayer.getLayerID();

			ErrorFeature errFeature = new ErrorFeature(featureID, reLayerId, reFeatureID,
					DMQAOptions.Type.REFZVALUEMISS.getErrCode(), DMQAOptions.Type.REFZVALUEMISS.getErrTypeE(),
					DMQAOptions.Type.REFZVALUEMISS.getErrName(langType), "", minDistPt);
			return errFeature;
		} else {
			return null;
		}
	}

	/**
	 * @since 2018. 3. 22.
	 * @author DY.Oh
	 * @param simpleFeature
	 * @param closeSfc
	 * @param closeBoundary
	 * @param tolerance
	 * @param figure
	 * @return ErrorFeature
	 */
	@Override
	public ErrorFeature validateRefAttributeMiss(DTFeature feature, DTLayer closeLayer, Geometry closeBoundary,
			OptionTolerance tolerance, OptionFigure figure) {

		SimpleFeature sf = feature.getSimefeature();

		boolean isTrue = false;
		List<AttributeFilter> attrFilters = feature.getFilter();
		if (attrFilters != null) {
			isTrue = FeatureFilter.filter(sf, attrFilters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		SimpleFeature maxSf = null;
		Geometry maxGeom = null;
		if (isTrue) {
			double value = tolerance.getValue();
			Geometry geomBuffer = geometry.buffer(value);
			SimpleFeatureCollection closeSfc = closeLayer.getSimpleFeatureCollection();
			OptionFilter closeFilter = closeLayer.getFilter();
			List<AttributeFilter> closeAttrFilters = null;
			if (closeFilter != null) {
				closeAttrFilters = closeFilter.getFilter();
			}
			double maxValue = 0.0;
			SimpleFeatureIterator closeIter = closeSfc.features();
			if (closeIter == null) {
				return null;
			}
			while (closeIter.hasNext()) {
				SimpleFeature closeSf = closeIter.next();
				boolean isCloseTrue = false;
				if (closeAttrFilters != null) {
					isCloseTrue = FeatureFilter.filter(closeSf, closeAttrFilters);
				} else {
					isCloseTrue = true;
				}
				if (isCloseTrue) {
					Geometry closeGeom = (Geometry) closeSf.getDefaultGeometry();
					Geometry closeBuffer = closeGeom.buffer(value);
					Geometry intersection = closeBuffer.intersection(geomBuffer);
					if (!intersection.isEmpty()) {
						String interType = intersection.getGeometryType();
						if (interType.equals("Polygon") || interType.equals("MultiPolygon")) {
							double area = intersection.getArea();
							if (maxValue < area) {
								maxSf = closeSf;
								maxValue = area;
								maxGeom = closeGeom;
							}
						}
						if (interType.equals("LineString") || interType.equals("MultiLineString")) {
							double length = intersection.getLength();
							if (maxValue < length) {
								maxSf = closeSf;
								maxValue = length;
								maxGeom = closeGeom;
							}
						}
					}

				}
			}
			closeIter.close();
			if (maxSf != null && maxValue > value) {
				List<AttributeFigure> attrFigures = figure.getFigure();
				for (AttributeFigure attrFigure : attrFigures) {
					String attriKey = attrFigure.getKey();
					Property relationProperty = maxSf.getProperty(attriKey);
					Property targetProperty = sf.getProperty(attriKey);
					if (relationProperty != null && targetProperty != null) {
						Object rePropertyValue = relationProperty.getValue();
						Object tarPropertyValue = targetProperty.getValue();
						if (rePropertyValue != null && tarPropertyValue != null) {
							String reValueStr = rePropertyValue.toString();
							String tarValueStr = tarPropertyValue.toString();
							if (!reValueStr.equals(tarValueStr)) {
								isError = true;
								break;
							}
						} else {
							isError = true;
							break;
						}
					} else {
						isError = true;
						break;
					}
				}
			}
		}
		if (isError && maxSf != null && maxGeom != null) {
			Double value = tolerance.getValue();
			String featureID = sf.getID();
			String reFeatureID = maxSf.getID();
			Coordinate[] coors = geometry.getCoordinates();
			Point minDistPt = null;
			GeometryFactory geometryFactory = new GeometryFactory();
			for (int i = 0; i < coors.length; i++) {
				Point tmpPt = geometryFactory.createPoint(coors[i]);
				double tarDist = maxGeom.distance(tmpPt);
				if (tarDist >= 0 && tarDist <= value) {
					minDistPt = tmpPt;
					break;
				}
			}
			if (minDistPt == null) {
				return null;
			}
			String reLayerId = closeLayer.getLayerID();
			ErrorFeature errFeature = new ErrorFeature(featureID, reLayerId, reFeatureID,
					DMQAOptions.Type.REFATTRIBUTEMISS.getErrCode(), DMQAOptions.Type.REFATTRIBUTEMISS.getErrTypeE(),
					DMQAOptions.Type.REFATTRIBUTEMISS.getErrName(langType), "", minDistPt);
			return errFeature;
		} else {
			return null;
		}
	}
}
