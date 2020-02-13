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

/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 *
 *    (C) 2002-2012, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */

package com.git.gdsbuilder.validator.feature;

import java.awt.geom.AffineTransform;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.geotools.referencing.operation.transform.AffineTransform2D;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.git.gdsbuilder.type.dt.feature.DTFeature;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.DTLayerList;
import com.git.gdsbuilder.type.validate.error.ErrorFeature;
import com.git.gdsbuilder.type.validate.option.en.LangType;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.git.gdsbuilder.type.validate.option.type.LayerFieldOptions;
import com.git.gdsbuilder.type.validate.option.type.DMQAOptions;
import com.git.gdsbuilder.type.validate.option.type.FTMQAOptions;
import com.git.gdsbuilder.type.validate.option.type.UFMQAOptions;
import com.git.gdsbuilder.validator.feature.filter.FeatureFilter;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

public class FeatureGraphicValidatorImpl implements FeatureGraphicValidator {

	LangType langType;

	public FeatureGraphicValidatorImpl(LangType langType) {
		this.langType = langType;
	}

	@Override
	public List<ErrorFeature> validateConBreak(DTFeature dtFeature, DTLayer neatLine, OptionTolerance optionTolerance) {

		List<ErrorFeature> errFeatures = new ArrayList<>();

		SimpleFeature sf = dtFeature.getSimefeature();
		List<AttributeFilter> filters = dtFeature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (isTrue) {

			OptionFilter relationFilter = neatLine.getFilter();
			List<AttributeFilter> relationConditions = null;

			if (relationFilter != null) {

				relationConditions = relationFilter.getFilter();
			}

			Double value = optionTolerance.getValue();
			String conditon = optionTolerance.getCondition();

			Geometry geometry = (Geometry) sf.getDefaultGeometry();
			Coordinate[] coordinates = geometry.getCoordinates();
			Coordinate start = coordinates[0];
			Coordinate end = coordinates[coordinates.length - 1];
			GeometryFactory geometryFactory = new GeometryFactory();

			if (!start.equals2D(end)) {

				SimpleFeatureCollection neatLineSfc = neatLine.getSimpleFeatureCollection();
				SimpleFeatureIterator iterator = neatLineSfc.features();

				while (iterator.hasNext()) {

					SimpleFeature aopSimpleFeature = iterator.next();

					if (FeatureFilter.filter(aopSimpleFeature, relationConditions)) {

						Geometry aopGeom = (Geometry) aopSimpleFeature.getDefaultGeometry();

						if (!geometry.intersects(aopGeom)) {
							Coordinate[] temp = new Coordinate[] { start, end };
							int tempSize = temp.length;

							boolean isError = false;

							for (int i = 0; i < tempSize; i++) {

								Geometry returnGeom = geometryFactory.createPoint(temp[i]);

								if (conditon.equals("over")) {
									if (Math.abs(returnGeom.distance(aopGeom)) < value || returnGeom.crosses(aopGeom)) {
										isError = true;
									}
								} else if (conditon.equals("under")) {
									if (Math.abs(returnGeom.distance(aopGeom)) > value || returnGeom.crosses(aopGeom)) {
										isError = true;
									}
								} else if (conditon.equals("equal")) {
									if (Math.abs(returnGeom.distance(aopGeom)) != value
											|| returnGeom.crosses(aopGeom)) {
										isError = true;
									}
								}
							}

							if (isError) {
								String featureID = sf.getID();
								ErrorFeature errFeatureSt = new ErrorFeature(featureID,
										DMQAOptions.Type.CONBREAK.getErrCode(), DMQAOptions.Type.CONBREAK.getErrTypeE(),
										DMQAOptions.Type.CONBREAK.getErrName(langType), dtFeature.getLayerID(),
										geometryFactory.createPoint(start));
								errFeatures.add(errFeatureSt);
								ErrorFeature errFeatureEd = new ErrorFeature(featureID,
										DMQAOptions.Type.CONBREAK.getErrCode(), DMQAOptions.Type.CONBREAK.getErrTypeE(),
										DMQAOptions.Type.CONBREAK.getErrName(langType), dtFeature.getLayerID(),
										geometryFactory.createPoint(end));
								errFeatures.add(errFeatureEd);
							}
						}
					}
				}
				iterator.close();
			}
		}

		if (errFeatures.size() != 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateConIntersected(DTFeature dtFeature) {

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		SimpleFeature sf = dtFeature.getSimefeature();
		List<AttributeFilter> filters = dtFeature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		if (isTrue) {

			GeometryFactory geometryFactory = new GeometryFactory();
			Geometry geometry = (Geometry) sf.getDefaultGeometry();
			if (!geometry.isSimple()) {
				Coordinate[] coordinates = geometry.getCoordinates();
				for (int i = 0; i < coordinates.length - 1; i++) {
					Coordinate[] coordI = new Coordinate[] { new Coordinate(coordinates[i]),
							new Coordinate(coordinates[i + 1]) };
					LineString lineI = geometryFactory.createLineString(coordI);
					for (int j = 0; j < coordinates.length - 1; j++) {
						Coordinate[] coordJ = new Coordinate[] { new Coordinate(coordinates[j]),
								new Coordinate(coordinates[j + 1]) };
						LineString lineJ = geometryFactory.createLineString(coordJ);
						if (lineI.intersects(lineJ)) {
							Geometry intersectGeom = lineI.intersection(lineJ);
							Coordinate[] intersectCoors = intersectGeom.getCoordinates();
							for (int k = 0; k < intersectCoors.length; k++) {
								Coordinate interCoor = intersectCoors[k];
								Geometry errPoint = geometryFactory.createPoint(interCoor);
								Boolean flag = false;
								for (int l = 0; l < coordI.length; l++) {
									Coordinate coordPoint = coordI[l];
									if (interCoor.equals2D(coordPoint)) {
										flag = true;
										break;
									}
								}
								if (flag == false) {
									String featureID = sf.getID();
									ErrorFeature errFeature = new ErrorFeature(featureID,
											DMQAOptions.Type.CONINTERSECTED.getErrCode(),
											DMQAOptions.Type.CONINTERSECTED.getErrTypeE(),
											DMQAOptions.Type.CONINTERSECTED.getErrName(langType), "", errPoint);
									errFeatures.add(errFeature);
								}
							}
						}
					}
				}
			}
		}

		if (errFeatures.size() != 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateConIntersected(DTFeature dtFeature, DTFeature reFeature) {

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		SimpleFeature sfi = dtFeature.getSimefeature();
		SimpleFeature sfj = reFeature.getSimefeature();
		List<AttributeFilter> filters = dtFeature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			if (FeatureFilter.filter(sfi, filters)) {

				filters = reFeature.getFilter();

				if (filters != null) {
					isTrue = FeatureFilter.filter(sfj, filters);
				} else {
					isTrue = true;
				}
			} else {
				isTrue = false;
			}
		} else {
			isTrue = true;
		}

		if (isTrue) {

			GeometryFactory geometryFactory = new GeometryFactory();
			Geometry geometryI = (Geometry) sfi.getDefaultGeometry();
			Geometry geometryJ = (Geometry) sfj.getDefaultGeometry();

			if (geometryI.intersects(geometryJ)) {
				Geometry returnGeom = geometryI.intersection(geometryJ);
				Coordinate[] coordinates = returnGeom.getCoordinates();
				String reFeatureId = sfj.getID();

				for (int i = 0; i < coordinates.length; i++) {
					Coordinate coordinate = coordinates[i];
					Geometry intersectPoint = geometryFactory.createPoint(coordinate);
					String featureID = sfi.getID();
					ErrorFeature errFeature = new ErrorFeature(featureID, reFeatureId,
							DMQAOptions.Type.CONINTERSECTED.getErrCode(), DMQAOptions.Type.CONINTERSECTED.getErrTypeE(),
							DMQAOptions.Type.CONINTERSECTED.getErrName(langType), "", intersectPoint);

					errFeatures.add(errFeature);
				}
				return errFeatures;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateConOverDegree(DTFeature dtFeature, OptionTolerance optionTolerance) {

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		SimpleFeature sf = dtFeature.getSimefeature();
		List<AttributeFilter> filters = dtFeature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		if (isTrue) {

			Double value = optionTolerance.getValue();
			String conditon = optionTolerance.getCondition();

			Geometry geometry = (Geometry) sf.getDefaultGeometry();
			Coordinate[] coordinates = geometry.getCoordinates();
			int coorSize = coordinates.length;
			for (int i = 0; i < coorSize - 2; i++) {

				boolean isError = false;

				Coordinate a = coordinates[i];
				Coordinate b = coordinates[i + 1];
				Coordinate c = coordinates[i + 2];
				if (!a.equals2D(b) && !b.equals2D(c) && !c.equals2D(a)) {

					double angle = Angle.toDegrees(Angle.angleBetween(a, b, c));

					if (conditon.equals("over")) {
						if (angle < value) {
							isError = true;
						}
					} else if (conditon.equals("under")) {
						if (angle > value) {
							isError = true;
						}
					} else if (conditon.equals("equal")) {
						if (angle != value) {
							isError = true;
						}
					}

					if (isError) {
						GeometryFactory geometryFactory = new GeometryFactory();
						Point errPoint = geometryFactory.createPoint(b);
						String featureID = sf.getID();
						ErrorFeature errFeature = new ErrorFeature(featureID,
								DMQAOptions.Type.CONOVERDEGREE.getErrCode(),
								DMQAOptions.Type.CONOVERDEGREE.getErrTypeE(),
								DMQAOptions.Type.CONOVERDEGREE.getErrName(langType), "", errPoint);
						errFeatures.add(errFeature);
					}
				}
			}
		}

		if (errFeatures.size() != 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateUselessPoint(DTFeature dtFeature) {

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		SimpleFeature sf = dtFeature.getSimefeature();
		List<AttributeFilter> filters = dtFeature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		if (isTrue) {
			String featureID = sf.getID();
			Geometry geometry = (Geometry) sf.getDefaultGeometry();
			Coordinate[] coors = geometry.getCoordinates();
			CoordinateReferenceSystem crs;
			try {
				crs = CRS.decode("EPSG:32652");
				int coorsSize = coors.length;
				for (int i = 0; i < coorsSize - 1; i++) {
					Coordinate a = coors[i];
					Coordinate b = coors[i + 1];
					if (a.equals2D(b)) {
						continue;
					}
					boolean isAngError = false;
					if (i < coorsSize - 2) {
						// 각도 조건
						Coordinate c = coors[i + 2];
						if (!a.equals2D(b) && !b.equals2D(c) && !c.equals2D(a)) {
							double angle = Angle.toDegrees(Angle.angleBetween(a, b, c));
							if (180 - angle < 6) {
								isAngError = true;
							}
						}
					}
					boolean isDistError = false;
					if (isAngError) {
						// 길이 조건
						// double tmpLength = a.distance(b);
						double distance = JTS.orthodromicDistance(a, b, crs);
						if (distance < 0.01) {
							isDistError = true;
						}
					}
					if (isDistError && isAngError) {
						GeometryFactory gFactory = new GeometryFactory();
						Geometry returnGeom = gFactory.createPoint(b);
						ErrorFeature errFeature = new ErrorFeature(featureID,
								DMQAOptions.Type.USELESSPOINT.getErrCode(), DMQAOptions.Type.USELESSPOINT.getErrTypeE(),
								DMQAOptions.Type.USELESSPOINT.getErrName(langType), "", returnGeom);
						errFeatures.add(errFeature);
					}
				}
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (errFeatures.size() != 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateSmallArea(DTFeature dtFeature, OptionTolerance optionTolerance) {

		SimpleFeature sf = dtFeature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		boolean isError = false;
		String featureID = sf.getID();

		// String osmId = sf.getAttribute("osm_id").toString();
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		Double value = optionTolerance.getValue();
		String conditon = optionTolerance.getCondition();

		CoordinateReferenceSystem dataCRS = sf.getFeatureType().getCoordinateReferenceSystem();
		CoordinateReferenceSystem worldCRS;
		MathTransform transform;
		try {
			if (isTrue) {
				if (geometry.getGeometryType().equals("MultiPolygon") || geometry.getGeometryType().equals("Polygon")) {
					if (dataCRS != null) {
						// crs transform
						worldCRS = CRS.decode("EPSG:32652");
						transform = CRS.findMathTransform(dataCRS, worldCRS, true);
						for (int i = 0; i < geometry.getNumGeometries(); i++) {
							Geometry g = JTS.transform(geometry.getGeometryN(i), transform);
							double geomArea = g.getArea();
							if (conditon.equals("over")) {
								if (geomArea < value) {
									isError = true;
								}
							} else if (conditon.equals("under")) {
								if (geomArea > value) {
									isError = true;
								}
							} else if (conditon.equals("equal")) {
								if (geomArea != value) {
									isError = true;
								}
							}
						}
					} else {
						for (int i = 0; i < geometry.getNumGeometries(); i++) {
							double geomArea = geometry.getGeometryN(i).getArea();
							if (conditon.equals("over")) {
								if (geomArea < value) {
									isError = true;
								}
							} else if (conditon.equals("under")) {
								if (geomArea > value) {
									isError = true;
								}
							} else if (conditon.equals("equal")) {
								if (geomArea != value) {
									isError = true;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isError) {
			ErrorFeature errFeature = new ErrorFeature(featureID, DMQAOptions.Type.SMALLAREA.getErrCode(),
					DMQAOptions.Type.SMALLAREA.getErrName(langType), DMQAOptions.Type.SMALLAREA.getErrNameE(), "",
					geometry.getInteriorPoint());
			return errFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateSmallLength(DTFeature dtFeature, OptionTolerance optionTolerance) {

		SimpleFeature sf = dtFeature.getSimefeature();
		List<AttributeFilter> filters = dtFeature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		String featureID = sf.getID();

		// String osmId = sf.getAttribute("osm_id").toString();
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		Double value = optionTolerance.getValue();
		String conditon = optionTolerance.getCondition();

		CoordinateReferenceSystem dataCRS = sf.getFeatureType().getCoordinateReferenceSystem();
		CoordinateReferenceSystem worldCRS;
		MathTransform transform;
		try {
			if (isTrue) {
				if (geometry.getGeometryType().equals("MultiLineString")
						|| geometry.getGeometryType().equals("LineString")) {
					if (dataCRS != null) {
						// crs transform
						worldCRS = CRS.decode("EPSG:32652");
						transform = CRS.findMathTransform(dataCRS, worldCRS, true);
						for (int i = 0; i < geometry.getNumGeometries(); i++) {
							Geometry g = JTS.transform(geometry.getGeometryN(i), transform);
							double geomArea = g.getLength();
							if (conditon.equals("over")) {
								if (geomArea < value) {
									isError = true;
								}
							} else if (conditon.equals("under")) {
								if (geomArea > value) {
									isError = true;
								}
							} else if (conditon.equals("equal")) {
								if (geomArea != value) {
									isError = true;
								}
							}
						}
					} else {
						for (int i = 0; i < geometry.getNumGeometries(); i++) {
							double geomArea = geometry.getGeometryN(i).getLength();
							if (conditon.equals("over")) {
								if (geomArea < value) {
									isError = true;
								}
							} else if (conditon.equals("under")) {
								if (geomArea > value) {
									isError = true;
								}
							} else if (conditon.equals("equal")) {
								if (geomArea != value) {
									isError = true;
								}
							}
						}
					}
				}
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (isError) {
			ErrorFeature errFeature = new ErrorFeature(featureID, DMQAOptions.Type.SMALLLENGTH.getErrCode(),
					DMQAOptions.Type.SMALLLENGTH.getErrName(langType), DMQAOptions.Type.SMALLLENGTH.getErrNameE(), "",
					geometry.getInteriorPoint());
			return errFeature;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateOverShoot(DTFeature dtFeature, DTLayer reFeature,
			OptionTolerance optionTolerance) {

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		String reLayerId = reFeature.getLayerID();

		SimpleFeature sf = dtFeature.getSimefeature();
		List<AttributeFilter> filters = dtFeature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		if (isTrue) {

			OptionFilter relationFilter = reFeature.getFilter();
			List<AttributeFilter> relationConditions = null;

			if (relationFilter != null) {

				relationConditions = relationFilter.getFilter();
			}

			Double value = optionTolerance.getValue();
			Geometry geom = (Geometry) sf.getDefaultGeometry();

			SimpleFeatureCollection neatLineSfc = reFeature.getSimpleFeatureCollection();
			SimpleFeatureIterator iterator = neatLineSfc.features();

			while (iterator.hasNext()) {

				SimpleFeature aopSimpleFeature = iterator.next();
				String reFeatureId = aopSimpleFeature.getID();
				if (FeatureFilter.filter(aopSimpleFeature, relationConditions)) {
					Geometry aopGeom = (Geometry) aopSimpleFeature.getDefaultGeometry();
					Coordinate[] aopCoors = aopGeom.getCoordinates();
					LinearRing ring = new GeometryFactory().createLinearRing(aopCoors);
					Geometry aopPolygon = new GeometryFactory().createPolygon(ring, null);
					Coordinate[] geomCoors = geom.getCoordinates();

					for (int i = 0; i < geomCoors.length; i++) {

						Coordinate geomCoor = geomCoors[i];
						Geometry geomPt = new GeometryFactory().createPoint(geomCoor);

						if (!geomPt.within(aopPolygon)) {

							Geometry toleGeom = aopPolygon.buffer(value);

							if (!geomPt.within(toleGeom)) {

								String featureID = sf.getID();
								ErrorFeature errFeature = new ErrorFeature(featureID, reLayerId, reFeatureId,
										DMQAOptions.Type.OVERSHOOT.getErrCode(),
										DMQAOptions.Type.OVERSHOOT.getErrTypeE(),
										DMQAOptions.Type.OVERSHOOT.getErrName(langType), "", geomPt);
								errFeatures.add(errFeature);
							}
						}
					}
				}
			}
		}
		if (errFeatures.size() != 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateSelfEntity(DTFeature dtFeature, DTFeature reFeature, OptionTolerance tolerance) {

		boolean isTrue = false;
		SimpleFeature sf = dtFeature.getSimefeature();
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		// relation filter
		SimpleFeature reSf = reFeature.getSimefeature();
		List<AttributeFilter> refilters = reFeature.getFilter();
		if (refilters != null) {
			isTrue = FeatureFilter.filter(reSf, refilters);
		} else {
			isTrue = true;
		}

		Geometry geometryI = (Geometry) sf.getDefaultGeometry();
		Geometry geometryJ = (Geometry) reSf.getDefaultGeometry();
		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
		GeometryFactory geometryFactory = new GeometryFactory();

		if (isTrue) {
			String geomIType = geometryI.getGeometryType();
			Geometry returnGeom = null;
			Double value = null;
			String condition = null;
			if (tolerance == null) {
				value = 0.0;
				condition = "equal";
			} else {
				value = tolerance.getValue();
				condition = tolerance.getCondition();
			}
			if (geomIType.equals("Point") || geomIType.equals("MultiPoint")) {
				returnGeom = selfEntityPoint(geometryI, geometryJ);
			}
			if (geomIType.equals("LineString") || geomIType.equals("MultiLineString")) {
				returnGeom = selfEntityLineString(geometryI, geometryJ, value, condition);
			}
			if (geomIType.equals("Polygon") || geomIType.equals("MultiPolygon")) {
				returnGeom = selfEntityPolygon(geometryI, geometryJ, value, condition);
			}
			if (returnGeom != null && !returnGeom.isEmpty()) {
				String featureID = sf.getID();
				String reFeatureID = reSf.getID();
				// String osmId = sf.getAttribute("osm_id").toString();
				// String reOmsId = reSf.getAttribute("osm_id").toString();

				String reLayerId = reFeature.getLayerID();
				String returnGeomType = returnGeom.getGeometryType().toUpperCase();
				if (returnGeomType.equals("LINESTRING")) {
					if (returnGeom.getLength() == 0.0 || returnGeom.getLength() == 0) {
						Coordinate[] coordinates = returnGeom.getCoordinates();
						Point startPoint = geometryFactory.createPoint(coordinates[0]);
						ErrorFeature errFeature = new ErrorFeature(featureID, reLayerId, reFeatureID,
								DMQAOptions.Type.SELFENTITY.getErrCode(), DMQAOptions.Type.SELFENTITY.getErrTypeE(),
								DMQAOptions.Type.SELFENTITY.getErrName(langType), "", startPoint);
						errFeatures.add(errFeature);
					} else {
						ErrorFeature errFeature = new ErrorFeature(featureID, reLayerId, reFeatureID,
								DMQAOptions.Type.SELFENTITY.getErrCode(), DMQAOptions.Type.SELFENTITY.getErrTypeE(),
								DMQAOptions.Type.SELFENTITY.getErrName(langType), "", returnGeom.getInteriorPoint());
						errFeatures.add(errFeature);
					}
				} else {
					for (int i = 0; i < returnGeom.getNumGeometries(); i++) {
						ErrorFeature errFeature = new ErrorFeature(featureID, reLayerId, reFeatureID,
								DMQAOptions.Type.SELFENTITY.getErrCode(), DMQAOptions.Type.SELFENTITY.getErrTypeE(),
								DMQAOptions.Type.SELFENTITY.getErrName(langType), "",
								returnGeom.getGeometryN(i).getInteriorPoint());
						errFeatures.add(errFeature);
					}
				}
			}
		}
		if (errFeatures.size() > 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	private Geometry selfEntityPoint(Geometry geometryI, Geometry geometryJ) {

		String typeJ = geometryJ.getGeometryType();
		Geometry returnGeom = null;
		if (typeJ.equals("Point") || typeJ.equals("MultiPoint")) {
			if (geometryI.intersects(geometryJ)) {
				returnGeom = geometryI.intersection(geometryJ);
			}
		}
		if (typeJ.equals("LineString") || typeJ.equals("MultiLineString")) {
			if (geometryI.intersects(geometryJ) || geometryI.touches(geometryJ)) {
				returnGeom = geometryI.intersection(geometryJ);
			}
		}
		if (typeJ.equals("Polygon") || typeJ.equals("MultiPolygon")) {
			if (geometryI.within(geometryJ)) {
				returnGeom = geometryI.intersection(geometryJ);
			}
		}
		return returnGeom;
	}

	private Geometry selfEntityLineString(Geometry geometryI, Geometry geometryJ, double tolerance, String condition) {
		GeometryFactory geometryFactory = new GeometryFactory();
		String typeJ = geometryJ.getGeometryType();
		Geometry returnGeom = null;
		if (typeJ.equals("Point") || typeJ.equals("MultiPoint")) {
			if (geometryI.equals(geometryJ)) {
				returnGeom = geometryI.intersection(geometryJ);
			}
		}

		if (typeJ.equals("LineString") || typeJ.equals("MultiLineString")) {
			if (geometryI.crosses(geometryJ) && !geometryI.equals(geometryJ)) {
				Geometry lineReturnGeom = null;
				lineReturnGeom = geometryI.intersection(geometryJ);
				String upperType = lineReturnGeom.getGeometryType().toString().toUpperCase();

				Coordinate[] coors = geometryI.getCoordinates();
				Coordinate firstCoor = coors[0];
				Coordinate lastCoor = coors[coors.length - 1];
				Point firstPoint = geometryFactory.createPoint(firstCoor);
				Point lastPoint = geometryFactory.createPoint(lastCoor);
				Coordinate[] coorsJ = geometryJ.getCoordinates();
				Coordinate firstCoorJ = coorsJ[0];
				Coordinate lastCoorJ = coorsJ[coorsJ.length - 1];
				Point firstPointJ = geometryFactory.createPoint(firstCoorJ);
				Point lastPointJ = geometryFactory.createPoint(lastCoorJ);

				if (upperType.equals("POINT")) {
					double firstDistance = firstPoint.distance(lineReturnGeom);
					double lastDistance = lastPoint.distance(lineReturnGeom);
					double firstDistanceJ = firstPointJ.distance(lineReturnGeom);
					double lastDistanceJ = lastPointJ.distance(lineReturnGeom);
					if (firstPoint.equals(lastPoint) && !firstPointJ.equals(lastPointJ)) {
						if (condition.equals("over")) {
							if (firstDistanceJ < tolerance && lastDistanceJ < tolerance) {
								returnGeom = lineReturnGeom;
							}
						}
						if (condition.equals("under")) {
							if (firstDistanceJ > tolerance && lastDistanceJ > tolerance) {
								returnGeom = lineReturnGeom;
							}
						}
						if (condition.equals("equal")) {
							if (firstDistanceJ != tolerance && lastDistanceJ != tolerance) {
								returnGeom = lineReturnGeom;
							}
						}

					} else if (!firstPoint.equals(lastPoint) && firstPointJ.equals(lastPointJ)) {
						if (condition.equals("over")) {
							if (firstDistance < tolerance && lastDistance < tolerance) {
								returnGeom = lineReturnGeom;
							}
						}
						if (condition.equals("under")) {
							if (firstDistance > tolerance && lastDistance > tolerance) {
								returnGeom = lineReturnGeom;
							}
						}
						if (condition.equals("equal")) {
							if (firstDistance != tolerance && lastDistance != tolerance) {
								returnGeom = lineReturnGeom;
							}
						}
					} else if (!firstPoint.equals(lastPoint) && !firstPointJ.equals(lastPointJ)) {
						if (condition.equals("over")) {
							if (firstDistance < tolerance && lastDistance < tolerance && firstDistanceJ < tolerance
									&& lastDistanceJ < tolerance) {
								returnGeom = lineReturnGeom;
							}
						}
						if (condition.equals("under")) {
							if (firstDistance > tolerance && lastDistance > tolerance && firstDistanceJ > tolerance
									&& lastDistanceJ > tolerance) {
								returnGeom = lineReturnGeom;
							}
						}
						if (condition.equals("equal")) {
							if (firstDistance != tolerance && lastDistance != tolerance && firstDistanceJ != tolerance
									&& lastDistanceJ != tolerance) {
								returnGeom = lineReturnGeom;
							}
						}
					}
				} else {
					if (firstPoint.equals(lastPoint) && firstPointJ.equals(lastPointJ)) {
						LinearRing ringI = geometryFactory.createLinearRing(coors);
						LinearRing holesI[] = null;
						Polygon polygonI = geometryFactory.createPolygon(ringI, holesI);
						LinearRing ringJ = geometryFactory.createLinearRing(coorsJ);
						LinearRing holesJ[] = null;
						Polygon polygonJ = geometryFactory.createPolygon(ringJ, holesJ);
						Geometry intersectPolygon = polygonI.intersection(polygonJ);
						if (intersectPolygon.getArea() > tolerance) {
							returnGeom = lineReturnGeom;
						}
					} else if (firstPoint.equals(lastPoint) && !firstPointJ.equals(lastPointJ)) {
						List<Point> points = new ArrayList<Point>();
						Coordinate[] lineReturnCoor = lineReturnGeom.getCoordinates();
						for (int i = 0; i < lineReturnCoor.length; i++) {
							Point returnPoint = geometryFactory.createPoint(lineReturnCoor[i]);
							if (returnPoint.distance(firstPointJ) > tolerance
									&& returnPoint.distance(lastPointJ) > tolerance) {
								points.add(returnPoint);
							}
						}
						if (points.size() != 0) {
							Point[] pointList = new Point[points.size()];
							for (int j = 0; j < points.size(); j++) {
								pointList[j] = points.get(j);
							}
							returnGeom = geometryFactory.createMultiPoint(pointList);
						}

					} else if (!firstPoint.equals(lastPoint) && firstPointJ.equals(lastPointJ)) {
						List<Point> points = new ArrayList<Point>();
						Coordinate[] lineReturnCoor = lineReturnGeom.getCoordinates();
						for (int i = 0; i < lineReturnCoor.length; i++) {
							Point returnPoint = geometryFactory.createPoint(lineReturnCoor[i]);
							if (returnPoint.distance(firstPoint) > tolerance
									&& returnPoint.distance(lastPoint) > tolerance) {
								points.add(returnPoint);
							}
						}
						if (points.size() != 0) {
							Point[] pointList = new Point[points.size()];
							for (int j = 0; j < points.size(); j++) {
								pointList[j] = points.get(j);
							}
							returnGeom = geometryFactory.createMultiPoint(pointList);
						}
					}
				}
			}
		}
		if (typeJ.equals("Polygon") || typeJ.equals("MultiPolygon")) {

			if (geometryI.crosses(geometryJ.getBoundary()) || geometryI.within(geometryJ)) {
				Geometry geometry = geometryI.intersection(geometryJ);
				String upperType = geometry.getGeometryType().toUpperCase();
				if (upperType.equals("LINESTRING") || upperType.equals("MULTILINESTRING")) {
					if (geometryI.within(geometryJ)) {
						returnGeom = geometry;
					} else {
						if (geometry.getLength() > tolerance) {
							returnGeom = geometryI.intersection(geometryJ.getBoundary());
						}
					}
				}
			}
		}
		return returnGeom;
	}

	private Geometry selfEntityPolygon(Geometry geometryI, Geometry geometryJ, double tolerance, String condition) {

		String typeJ = geometryJ.getGeometryType();
		Geometry returnGeom = null;
		try {
			if (typeJ.equals("Point") || typeJ.equals("MultiPoint")) {
				if (geometryI.within(geometryJ)) {
					returnGeom = geometryI.intersection(geometryJ);
				}
			}
			if (typeJ.equals("LineString") || typeJ.equals("MultiLineString")) {
				Geometry geom = geometryI.intersection(geometryJ);
				String upperType = geom.getGeometryType().toUpperCase();
				if (upperType.equals("LINESTRING") || upperType.equals("MULTILINESTRING")) {
					if (condition.equals("over")) {
						if (geom.getLength() < tolerance) {
							if (geometryI.contains(geometryJ)) {
								returnGeom = geometryI.intersection(geometryJ);
							} else {
								returnGeom = geometryI.intersection(geometryJ.getBoundary());
							}
						}
					}
					if (condition.equals("under")) {
						if (geom.getLength() > tolerance) {
							if (geometryI.contains(geometryJ)) {
								returnGeom = geometryI.intersection(geometryJ);
							} else {
								returnGeom = geometryI.intersection(geometryJ.getBoundary());
							}
						}
					}
					if (condition.equals("equal")) {
						if (geom.getLength() != tolerance) {
							if (geometryI.contains(geometryJ)) {
								returnGeom = geometryI.intersection(geometryJ);
							} else {
								returnGeom = geometryI.intersection(geometryJ.getBoundary());
							}
						}
					}
				}
			}
			if (typeJ.equals("Polygon") || typeJ.equals("MultiPolygon")) {
				if (!geometryI.equals(geometryJ)) {
					if (geometryI.intersects(geometryJ) || geometryI.overlaps(geometryJ) || geometryI.within(geometryJ)
							|| geometryI.contains(geometryJ)) {
						Geometry geometry = geometryI.intersection(geometryJ);
						String upperType = geometry.getGeometryType().toUpperCase();
						if (upperType.equals("POLYGON") || upperType.equals("MULTIPOLYGON")) {
							if (condition.equals("over")) {
								if (geometry.getArea() < tolerance) {
									returnGeom = geometry;
								}
							}
							if (condition.equals("under")) {
								if (geometry.getArea() > tolerance) {
									returnGeom = geometry;
								}
							}
							if (condition.equals("equal")) {
								if (geometry.getArea() != tolerance) {
									returnGeom = geometry;
								}
							}
						}
					}
				}
			}
			return returnGeom;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public ErrorFeature validateOutBoundary(DTFeature dtFeature, DTLayer relationLayer, OptionTolerance tole) {

		// simplefeature : 터널, 지하도, 교량.....
		// relationSfc : 도로경계
		SimpleFeature sf = dtFeature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		String featureId = sf.getID();
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		Coordinate[] geomCoors = geom.getCoordinates();
		int geomCoorsLength = geomCoors.length;
		boolean isErr = false;
		String reFeatureId = null;
		if (isTrue) {
			Double value = tole.getValue();
			String condition = tole.getCondition();
			if (value != null) {
				SimpleFeatureCollection reSfc = relationLayer.getSimpleFeatureCollection();
				SimpleFeatureIterator iterator = reSfc.features();
				OptionFilter reFilter = relationLayer.getFilter();
				List<AttributeFilter> reAttrFilters = null;
				if (reFilter != null) {
					reAttrFilters = reFilter.getFilter();
				}
				while (iterator.hasNext()) {
					int trueCount = 0;
					// A001
					SimpleFeature relationSf = iterator.next();
					if (FeatureFilter.filter(relationSf, reAttrFilters)) {
						Geometry relationGeom = (Geometry) relationSf.getDefaultGeometry();
						String geomType = relationGeom.getGeometryType();
						if (!geomType.equals("Polygon") || !geomType.equals("MultiPolygon")) {
							return null;
						}
						if (geom.intersects(relationGeom)) {
							Coordinate[] rGeomCoors = relationGeom.getCoordinates();
							for (int i = 0; i < rGeomCoors.length - 1; i++) {
								Coordinate tmp1 = rGeomCoors[i];
								Coordinate tmp2 = rGeomCoors[i + 1];
								Coordinate[] tmpCoors = new Coordinate[] { tmp1, tmp2 };
								LineString tmpLn = new GeometryFactory().createLineString(tmpCoors);
								for (int j = 0; j < geomCoorsLength; j++) {
									Coordinate coor = geomCoors[j];
									double distance = tmpLn.distance(new GeometryFactory().createPoint(coor));
									if (condition.equals("equal")) {
										if (distance == value) {
											trueCount++;
										}
									} else if (condition.equals("under")) {
										if (distance < value) {
											trueCount++;
										}

									} else if (condition.equals("over")) {
										if (distance > value) {
											trueCount++;
										}
									}
								}
							}
						}
						if (trueCount >= geomCoorsLength) {
							isErr = true;
							reFeatureId = relationSf.getID();
							break;
						}
					}
				}
				iterator.close();
			}
		}
		if (!isErr) {
			Geometry returnGome = geom.getCentroid();
			ErrorFeature errFeature = new ErrorFeature(featureId, relationLayer.getLayerID(), reFeatureId,
					DMQAOptions.Type.OUTBOUNDARY.getErrCode(), DMQAOptions.Type.OUTBOUNDARY.getErrTypeE(),
					DMQAOptions.Type.OUTBOUNDARY.getErrName(langType), "", returnGome);
			return errFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateEntityDuplicated(DTFeature dtFeature, DTFeature reFeature) {

		SimpleFeature sfI = dtFeature.getSimefeature();
		SimpleFeature sfJ = reFeature.getSimefeature();

		boolean isTrue = false;
		List<AttributeFilter> filtersI = dtFeature.getFilter();
		if (filtersI != null) {
			isTrue = FeatureFilter.filter(sfI, filtersI);
		} else {
			isTrue = true;
		}
		List<AttributeFilter> filtersJ = reFeature.getFilter();
		if (filtersJ != null) {
			isTrue = FeatureFilter.filter(sfJ, filtersJ);
		} else {
			isTrue = true;
		}
		if (isTrue) {
			Geometry geometryI = (Geometry) sfI.getDefaultGeometry();
			Geometry geometryJ = (Geometry) sfJ.getDefaultGeometry();
			// geom 비교
			if (geometryI.equals(geometryJ)) {
				if (sfI.getAttributeCount() != 0 && sfJ.getAttributeCount() != 0) {
					FeatureAttributeValidator attributeValidator = new FeatureAttributeValidatorImpl(langType);
					return attributeValidator.validateEntityDuplicated(dtFeature, reFeature);
				} else {
					String featureID = sfI.getID();
					String reFeatrueId = sfJ.getID();
					ErrorFeature errFeature = new ErrorFeature(featureID, reFeatrueId,
							DMQAOptions.Type.ENTITYDUPLICATED.getErrCode(),
							DMQAOptions.Type.ENTITYDUPLICATED.getErrTypeE(),
							DMQAOptions.Type.ENTITYDUPLICATED.getErrName(langType), "", geometryI.getInteriorPoint());
					return errFeature;
				}
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateEntityOpenMiss(DTFeature dtFeature, DTLayer relationLayer, OptionTolerance tolerance) {

		SimpleFeature sf = dtFeature.getSimefeature();
		List<AttributeFilter> filters = dtFeature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		if (isTrue) {

			GeometryFactory geometryFactory = new GeometryFactory();
			Geometry geometry = (Geometry) sf.getDefaultGeometry();
			Coordinate[] coordinates = geometry.getCoordinates();
			int coorSize = coordinates.length;
			Coordinate start = coordinates[0];
			Coordinate end = coordinates[coorSize - 1];
			Geometry startGeom = geometryFactory.createPoint(start);
			Geometry endGeom = geometryFactory.createPoint(end);

			Double value = tolerance.getValue();
			String conditon = tolerance.getCondition();
			boolean isError = false;

			OptionFilter relationFilter = relationLayer.getFilter();
			List<AttributeFilter> relationConditions = null;
			SimpleFeatureCollection neatLineSfc = relationLayer.getSimpleFeatureCollection();

			if (relationFilter != null) {

				relationConditions = relationFilter.getFilter();
			}

			if (coorSize > 3) {

				if (!(start.equals2D(end))) {

					SimpleFeatureIterator iterator = neatLineSfc.features();

					while (iterator.hasNext()) {

						SimpleFeature aopSimpleFeature = iterator.next();

						if (FeatureFilter.filter(aopSimpleFeature, relationConditions)) {

							Geometry aopGeom = (Geometry) aopSimpleFeature.getDefaultGeometry();

							if (conditon.equals("over")) {

								if (Math.abs(aopGeom.distance(startGeom)) < value
										|| Math.abs(aopGeom.distance(endGeom)) < value) {

									isError = true;
									break;
								}
							} else if (conditon.equals("under")) {

								if (Math.abs(aopGeom.distance(startGeom)) > value
										|| Math.abs(aopGeom.distance(endGeom)) > value) {

									isError = true;
									break;
								}
							} else if (conditon.equals("equal")) {

								if (Math.abs(aopGeom.distance(startGeom)) != value
										|| Math.abs(aopGeom.distance(endGeom)) != value) {

									isError = true;
									break;
								}
							}
						}
					}
					iterator.close();
				}
			} else {

				SimpleFeatureIterator iterator = neatLineSfc.features();

				while (iterator.hasNext()) {

					SimpleFeature aopSimpleFeature = iterator.next();

					if (FeatureFilter.filter(aopSimpleFeature, relationConditions)) {

						Geometry aopGeom = (Geometry) aopSimpleFeature.getDefaultGeometry();

						if (conditon.equals("over")) {

							if (Math.abs(aopGeom.distance(startGeom)) < value
									|| Math.abs(aopGeom.distance(endGeom)) < value) {

								isError = true;
								break;
							}
						} else if (conditon.equals("under")) {

							if (Math.abs(aopGeom.distance(startGeom)) > value
									|| Math.abs(aopGeom.distance(endGeom)) > value) {

								isError = true;
								break;
							}
						} else if (conditon.equals("equal")) {

							if (Math.abs(aopGeom.distance(startGeom)) != value
									|| Math.abs(aopGeom.distance(endGeom)) != value) {

								isError = true;
								break;
							}
						}
					}
				}
				iterator.close();
			}

			if (isError) {

				String featureID = sf.getID();
				ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.ENTITYOPENMISS.getErrCode(),
						DMQAOptions.Type.ENTITYOPENMISS.getErrTypeE(),
						DMQAOptions.Type.ENTITYOPENMISS.getErrName(langType), "", geometry.getInteriorPoint());
				return errorFeature;
			} else {

				return null;
			}
		} else {

			return null;
		}
	}

	@Override
	public ErrorFeature validateLayerFixMiss(SimpleFeature simpleFeature, String typeNames) {

		Geometry geometry = (Geometry) simpleFeature.getDefaultGeometry();
		String upperTypeName = typeNames.toUpperCase();
		String geomType = geometry.getGeometryType().toUpperCase();
		// if (!geomType.equals(upperTypeName) &&
		// !upperTypeName.equals(geomType.replaceAll("MULTI", ""))) {
		if (!geomType.equals(upperTypeName)) {
			String featureID = simpleFeature.getID();
			ErrorFeature errorFeature = new ErrorFeature(featureID,
					LayerFieldOptions.Type.LAYERTYPEFIXMISS.getErrCode(),
					LayerFieldOptions.Type.LAYERTYPEFIXMISS.getErrTypeE(),
					LayerFieldOptions.Type.LAYERTYPEFIXMISS.getErrName(langType), "", geometry.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateTwistedPolygon(DTFeature simpleFeature) {

		SimpleFeature sf = simpleFeature.getSimefeature();
		List<AttributeFilter> filters = simpleFeature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (isTrue) {
			Geometry geometry = (Geometry) sf.getDefaultGeometry();
			if (!(geometry.isValid())) {
				GeometryFactory f = new GeometryFactory();
				Coordinate[] coordinates = geometry.getCoordinates();
				Geometry errGeometry = f.createPoint(coordinates[0]);

				String featureID = sf.getID();
				ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.TWISTEDPOLYGON.getErrCode(),
						DMQAOptions.Type.TWISTEDPOLYGON.getErrTypeE(),
						DMQAOptions.Type.TWISTEDPOLYGON.getErrName(langType), "", errGeometry);
				return errorFeature;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateNodeMiss(DTFeature dtFeature, SimpleFeatureCollection targetSfc,
			DTLayer relationLayer, OptionTolerance tolerance) {

		SimpleFeature sf = dtFeature.getSimefeature();

		boolean isTrue = false;

		List<AttributeFilter> filters = dtFeature.getFilter();

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		List<ErrorFeature> errorFeatures = new ArrayList<ErrorFeature>();
		String featureID = sf.getID();

		if (isTrue) {

			OptionFilter relationFilter = relationLayer.getFilter();
			List<AttributeFilter> relationConditions = null;

			if (relationFilter != null) {

				relationConditions = relationFilter.getFilter();
			}

			Double value = tolerance.getValue();
			String conditon = tolerance.getCondition();

			// a002
			Geometry tGeom = (Geometry) sf.getDefaultGeometry();
			Coordinate[] tCoors = tGeom.getCoordinates();
			Coordinate tFirCoor = tCoors[0];
			Coordinate tLasCoor = tCoors[tCoors.length - 1];

			GeometryFactory factory = new GeometryFactory();
			Geometry firPt = factory.createPoint(tFirCoor);
			Geometry lasPt = factory.createPoint(tLasCoor);

			boolean firTrue = false;
			boolean lasTrue = false;

			boolean firInter = true;
			boolean lasInter = true;

			boolean notInter = false;

			SimpleFeatureCollection relationSfc = relationLayer.getSimpleFeatureCollection();
			SimpleFeatureIterator rIterator = relationSfc.features();

			String reFeatureId = "";

			while (rIterator.hasNext()) {
				SimpleFeature rSf = rIterator.next();
				if (FeatureFilter.filter(rSf, relationConditions)) {
					Geometry rGeom = (Geometry) rSf.getDefaultGeometry();
					String rGeomType = rGeom.getGeometryType();
					if (!rGeomType.equals("Polygon") && !rGeomType.equals("MultiPolygon")) {
						continue;
					}
					if (!rGeom.intersects(tGeom)) {
						notInter = true;
					} else {
						notInter = false;
						Geometry boundary = rGeom.getBoundary();
						firInter = rGeom.intersects(firPt);
						if (boundary.intersects(firPt.buffer(value))) {
							firTrue = true;
							reFeatureId = rSf.getID();
						}
						lasInter = rGeom.intersects(lasPt);
						if (boundary.intersects(lasPt.buffer(value))) {
							lasTrue = true;
							reFeatureId = rSf.getID();
						}
						break;
					}
				}
			}
			rIterator.close();
			if (notInter) {
				return null;
			}
			if (firTrue && lasTrue) {
				return null;
			} else {
				boolean firErr = true;
				boolean lasErr = true;
				SimpleFeatureIterator tIterator = targetSfc.features();

				while (tIterator.hasNext()) {
					SimpleFeature tSimpleFeature = tIterator.next();
					if (featureID.equals(tSimpleFeature.getID())) {
						continue;
					}
					Geometry selfGeom = (Geometry) tSimpleFeature.getDefaultGeometry();
					if (!firTrue) {
						if (conditon.equals("over")) {
							if (Math.abs(firPt.distance(selfGeom)) > value) {
								firErr = false;
							}
						} else if (conditon.equals("under")) {
							if (Math.abs(firPt.distance(selfGeom)) < value) {
								firErr = false;
							}
						} else if (conditon.equals("equal")) {
							if (Math.abs(firPt.distance(selfGeom)) == value) {
								firErr = false;
							}
						}
					}
					if (!lasTrue) {
						if (conditon.equals("over")) {
							if (Math.abs(lasPt.distance(selfGeom)) > value) {
								lasErr = false;
							}
						} else if (conditon.equals("under")) {
							if (Math.abs(lasPt.distance(selfGeom)) < value) {
								lasErr = false;
							}
						} else if (conditon.equals("equal")) {
							if (Math.abs(lasPt.distance(selfGeom)) == value) {
								lasErr = false;
							}
						}
					}
				}
				tIterator.close();
				String reLayerId = relationLayer.getLayerID();
				if (!firTrue && firErr && firInter) {
					ErrorFeature errorFeature = new ErrorFeature(featureID, reLayerId, reFeatureId,
							DMQAOptions.Type.NODEMISS.getErrCode(), DMQAOptions.Type.NODEMISS.getErrTypeE(),
							DMQAOptions.Type.NODEMISS.getErrName(langType), "", firPt);
					errorFeatures.add(errorFeature);
				}

				if (!lasTrue && lasErr && lasInter) {
					ErrorFeature errorFeature = new ErrorFeature(featureID, reLayerId, reFeatureId,
							DMQAOptions.Type.NODEMISS.getErrCode(), DMQAOptions.Type.NODEMISS.getErrTypeE(),
							DMQAOptions.Type.NODEMISS.getErrName(langType), "", lasPt);
					errorFeatures.add(errorFeature);
				}

				if (errorFeatures.size() > 0) {
					return errorFeatures;
				} else {
					return null;
				}
			}
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validatePointDuplicated(DTFeature dtFeature) {

		SimpleFeature sf = dtFeature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
		if (isTrue) {
			String featureID = sf.getID();
			Geometry geometry = (Geometry) sf.getDefaultGeometry();
			int numGeom = geometry.getNumGeometries();
			for (int i = 0; i < numGeom; i++) {
				Geometry singleGeom = geometry.getGeometryN(i);
				if (singleGeom instanceof LineString) {
					LineString lineString = (LineString) singleGeom;
					errFeatures.addAll(pointDuplicated(lineString.getCoordinates(), featureID));
				}
				if (singleGeom instanceof Polygon) {
					Polygon polygon = (Polygon) singleGeom;
					LineString exteriorRing = polygon.getExteriorRing();
					errFeatures.addAll(pointDuplicated(exteriorRing.getCoordinates(), featureID));
					int numInnerRings = polygon.getNumInteriorRing();
					for (int in = 0; in < numInnerRings; in++) {
						LineString innerRing = polygon.getInteriorRingN(in);
						errFeatures.addAll(pointDuplicated(innerRing.getCoordinates(), featureID));
					}
				}
			}
		}
		if (errFeatures.size() > 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	private List<ErrorFeature> pointDuplicated(Coordinate[] coors, String featureId) {

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		int coorLength = coors.length;
		if (coorLength == 2) {
			Coordinate coor0 = coors[0];
			Coordinate coor1 = coors[1];
			if (coor0.equals3D(coor1)) {
				Geometry errGeom = new GeometryFactory().createPoint(coor1);
				ErrorFeature errorFeature = new ErrorFeature(featureId, DMQAOptions.Type.POINTDUPLICATED.getErrCode(),
						DMQAOptions.Type.POINTDUPLICATED.getErrTypeE(),
						DMQAOptions.Type.POINTDUPLICATED.getErrName(langType), "", errGeom);
				errFeatures.add(errorFeature);
			}
		}
		if (coorLength > 3) {
			for (int c = 0; c < coorLength - 1; c++) {
				Coordinate coor0 = coors[c];
				Coordinate coor1 = coors[c + 1];
				if (coor0.equals3D(coor1)) {
					Geometry errGeom = new GeometryFactory().createPoint(coor1);
					ErrorFeature errorFeature = new ErrorFeature(featureId,
							DMQAOptions.Type.POINTDUPLICATED.getErrCode(),
							DMQAOptions.Type.POINTDUPLICATED.getErrTypeE(),
							DMQAOptions.Type.POINTDUPLICATED.getErrName(langType), "", errGeom);
					errFeatures.add(errorFeature);
				}
			}
		}
		return errFeatures;
	}

	@Override
	public ErrorFeature validateOneAcre(DTFeature simpleFeature, SimpleFeatureCollection relationSfc) {

		SimpleFeature sf = simpleFeature.getSimefeature();
		String featureID = sf.getID();

		boolean isTrue = false;

		List<AttributeFilter> filters = simpleFeature.getFilter();

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		if (isTrue) {
			Geometry geometry = (Geometry) sf.getDefaultGeometry(); // D002
			int withinCount = 0;
			int intersCount = 0;
			double acreArea = geometry.getArea();
			double totalStageArea = 0;

			boolean isError = false;
			SimpleFeatureIterator sfIterator = relationSfc.features();
			while (sfIterator.hasNext()) {
				SimpleFeature relationSf = sfIterator.next(); // D001
				Geometry relationGeom = (Geometry) relationSf.getDefaultGeometry();
				if (relationGeom.within(geometry)) {
					withinCount++;
					totalStageArea += relationGeom.getArea();
				} else if (geometry.intersects(relationGeom)) {
					if (geometry.touches(relationGeom)) {
						continue;
					}
					boolean isWithin = true;
					Coordinate[] coors = relationGeom.getCoordinates();
					for (int i = 0; i < coors.length; i++) {
						Geometry pt = new GeometryFactory().createPoint(coors[i]);
						double distance = geometry.distance(pt);
						if (distance > 0.1) {
							isWithin = false;
						}
					}
					if (!isWithin) {
						intersCount++;
					} else {
						withinCount++;
						totalStageArea += relationGeom.getArea();
					}
				}
			}
			sfIterator.close();
			// 1개의 지류계에 1개의 경지계가 겹쳐있는 경우, 1개의 지류계에는 2개 이상의 경지계가 포합되어있어야 함
			String comment = "";
			if (withinCount > 1) {
				if (Math.abs(acreArea - totalStageArea) > 0.1) {
					comment += "farmland area miss";
					isError = true;
				}
			} else if (intersCount == 1 || withinCount == 1) {
				comment += "farmland within miss";
				isError = true;
			} else if (withinCount == 0) {
				return null;
			}

			if (isError) {

				Geometry returnGeom = ((Geometry) sf.getDefaultGeometry()).getCentroid();
				ErrorFeature errFeature = new ErrorFeature(featureID, DMQAOptions.Type.ONEACRE.getErrCode(),
						DMQAOptions.Type.ONEACRE.getErrTypeE(), DMQAOptions.Type.ONEACRE.getErrName(langType), comment,
						returnGeom);
				return errFeature;
			} else {
				return null;
			}
		}

		return null;
	}

	@Override
	public List<ErrorFeature> validateOneStage(DTFeature feature, DTLayer relationLayer) {

		// 경지계가 지류계와 겹쳐져 있지 않는 경우
		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
		// D001
		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		boolean isError = false;
		if (isTrue) {
			OptionFilter reFilter = relationLayer.getFilter();
			List<AttributeFilter> reAttrFilters = null;
			if (reFilter != null) {
				reAttrFilters = reFilter.getFilter();
			}
			SimpleFeatureCollection reSfc = relationLayer.getSimpleFeatureCollection();
			SimpleFeatureIterator iterator = reSfc.features();
			intterWhile: while (iterator.hasNext()) {
				// D002
				SimpleFeature reSf = iterator.next();
				if (FeatureFilter.filter(reSf, reAttrFilters)) {
					Geometry reGeom = (Geometry) reSf.getDefaultGeometry();
					if (geom.within(reGeom) || geom.overlaps(reGeom) || geom.intersects(reGeom)) {
						isError = true;
						break intterWhile;
					}
				}
			}
			iterator.close();
			if (!isError) {
				String featureID = sf.getID();
				Geometry returnGeom = ((Geometry) sf.getDefaultGeometry()).getCentroid();
				ErrorFeature errFeature = new ErrorFeature(featureID, DMQAOptions.Type.ONESTAGE.getErrCode(),
						DMQAOptions.Type.ONESTAGE.getErrTypeE(), DMQAOptions.Type.ONESTAGE.getErrName(langType), "",
						returnGeom);
				errFeatures.add(errFeature);
			}
		}
		if (errFeatures.size() > 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateBuildingSiteMiss(DTFeature dtFeature, DTLayerList relationLayers) {

		SimpleFeature sf = dtFeature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		String featureID = sf.getID();
		Geometry targetGeom = (Geometry) sf.getDefaultGeometry();
		if (isTrue) {
			boolean isError = true;
			for (DTLayer relationLayer : relationLayers) {
				OptionFilter reFilter = relationLayer.getFilter();
				List<AttributeFilter> reAttrFilters = null;
				if (reFilter != null) {
					reAttrFilters = reFilter.getFilter();
				}
				SimpleFeatureCollection reSfc = relationLayer.getSimpleFeatureCollection();
				SimpleFeatureIterator iterator = reSfc.features();
				while (iterator.hasNext()) {
					SimpleFeature relationSf = iterator.next();
					if (FeatureFilter.filter(relationSf, reAttrFilters)) {
						Geometry relationGeom = (Geometry) relationSf.getDefaultGeometry();
						if (targetGeom.intersects(relationGeom)) {
							isError = false;
						}
					}
				}
				iterator.close();
			}
			if (isError) {
				ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.BUILDINGSITEMISS.getErrCode(),
						DMQAOptions.Type.BUILDINGSITEMISS.getErrTypeE(),
						DMQAOptions.Type.BUILDINGSITEMISS.getErrName(langType), "", targetGeom.getInteriorPoint());
				return errorFeature;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateBoundaryMiss(DTFeature dtFeature, DTLayer relationLayer) {

		// 실폭하천, 도로중심선
		SimpleFeature sf = dtFeature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		boolean isError = false;
		String featureID = sf.getID();
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		if (isTrue) {
			boolean isIntersected = false;
			int coorSize = geometry.getNumPoints();
			Geometry innerPt = null;
			if (coorSize > 2) {
				innerPt = geometry.getInteriorPoint();
			} else {
				innerPt = geometry.getCentroid();
			}
			if (innerPt != null) {

				OptionFilter reFilter = relationLayer.getFilter();
				List<AttributeFilter> reAttrFilters = null;
				if (reFilter != null) {
					reAttrFilters = reFilter.getFilter();
				}
				SimpleFeatureCollection reSfc = relationLayer.getSimpleFeatureCollection();
				SimpleFeatureIterator iterator = reSfc.features();
				while (iterator.hasNext()) {
					SimpleFeature relationSf = iterator.next();
					if (FeatureFilter.filter(relationSf, reAttrFilters)) {
						Geometry relationGeom = (Geometry) relationSf.getDefaultGeometry();
						if (innerPt.intersects(relationGeom)) {
							isIntersected = true;
						}
					}
				}
				iterator.close();
			}

			if (!isIntersected) {
				isError = true;
			}

		}
		if (isError) {
			ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.BOUNDARYMISS.getErrCode(),
					DMQAOptions.Type.BOUNDARYMISS.getErrTypeE(), DMQAOptions.Type.BOUNDARYMISS.getErrName(langType),
					relationLayer.getLayerID() + " Miss", geometry.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateCenterLineMiss(DTFeature dtFeature, DTLayer relationLayer) {

		SimpleFeature sf = dtFeature.getSimefeature();
		String featureID = sf.getID();
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		// 도로경계
		boolean isIntersected = false;
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		String geomType = geometry.getGeometryType();
		if (!geomType.equals("Polygon") || !geomType.equals("MultiPolygon")) {
			return null;
		}
		if (isTrue) {
			// 도로중심선
			OptionFilter reFilter = relationLayer.getFilter();
			List<AttributeFilter> reAttrFilters = null;
			if (reFilter != null) {
				reAttrFilters = reFilter.getFilter();
			}
			SimpleFeatureCollection relationSfc = relationLayer.getSimpleFeatureCollection();
			SimpleFeatureIterator iterator = relationSfc.features();
			while (iterator.hasNext()) {
				SimpleFeature relationSf = iterator.next();
				if (FeatureFilter.filter(relationSf, reAttrFilters)) {
					Geometry relationGeom = (Geometry) relationSf.getDefaultGeometry();
					int coorSize = relationGeom.getNumPoints();
					Geometry innerPt = null;
					if (coorSize > 2) {
						innerPt = relationGeom.getInteriorPoint();
					} else {
						innerPt = relationGeom.getCentroid();
					}
					if (innerPt.intersects(geometry)) {
						isIntersected = true;
					}
				}
			}
			iterator.close();
		}
		if (!isIntersected) {
			ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.CENTERLINEMISS.getErrCode(),
					DMQAOptions.Type.CENTERLINEMISS.getErrTypeE(), DMQAOptions.Type.CENTERLINEMISS.getErrName(langType),
					relationLayer.getLayerID() + " Miss", geometry.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateHoleMissplacement(DTFeature dtFeature) {

		SimpleFeature sf = dtFeature.getSimefeature();
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		String geomType = geometry.getGeometryType();
		String featureID = sf.getID();
		List<ErrorFeature> errorFeatures = new ArrayList<>();

		Polygon polygon = null;
		if (geomType.equals("Polygon")) {
			polygon = (Polygon) geometry;
		} else if (geomType.equals("MultiPolygon")) {
			int geomNum = geometry.getNumGeometries();
			if (geomNum == 1) {
				polygon = (Polygon) geometry.getGeometryN(0);
			}
		}
		if (polygon != null) {
			int holeNum = polygon.getNumInteriorRing();
			boolean hasHoles = holeNum > 0;
			if (hasHoles) {
				for (int i = 0; i < holeNum; i++) {
					LineString lineString = polygon.getInteriorRingN(i);
					ErrorFeature errorFeature = new ErrorFeature(featureID,
							DMQAOptions.Type.HOLEMISPLACEMENT.getErrCode(),
							DMQAOptions.Type.HOLEMISPLACEMENT.getErrTypeE(),
							DMQAOptions.Type.HOLEMISPLACEMENT.getErrName(langType), dtFeature.getLayerID(),
							lineString.getCentroid());
					errorFeatures.add(errorFeature);
				}
			}
		}
		if (errorFeatures.size() > 0) {
			return errorFeatures;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateEntityInHole(DTFeature feature, DTLayer relationLayer, boolean isEquals) {

		SimpleFeature sf = feature.getSimefeature();
		// filter
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		String featureId = sf.getID();
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		List<ErrorFeature> errorFeatures = new ArrayList<>();
		if (isTrue) {
			GeometryFactory factory = new GeometryFactory();
			String geomType = geom.getGeometryType();
			Polygon polygon = null;
			if (geomType.equals("Polygon")) {
				polygon = (Polygon) geom;
			} else if (geomType.equals("MultiPolygon")) {
				int geomNum = geom.getNumGeometries();
				if (geomNum == 1) {
					polygon = (Polygon) geom;
				}
			}
			if (polygon != null) {
				OptionFilter reFilter = relationLayer.getFilter();
				List<AttributeFilter> reAttrFilters = null;
				if (reFilter != null) {
					reAttrFilters = reFilter.getFilter();
				}
				int holeNum = polygon.getNumInteriorRing();
				if (holeNum > 0) {
					for (int i = 0; i < holeNum; i++) {
						LinearRing interiorRing = (LinearRing) polygon.getInteriorRingN(i);
						LinearRing holes[] = null;
						Polygon interiorPolygon = factory.createPolygon(interiorRing, holes);
						SimpleFeatureCollection reSfc = relationLayer.getSimpleFeatureCollection();
						SimpleFeatureIterator relationSfcIterator = reSfc.features();
						while (relationSfcIterator.hasNext()) {
							SimpleFeature reSf = relationSfcIterator.next();
							if (FeatureFilter.filter(reSf, reAttrFilters)) {
								String featureIdR = reSf.getID();
								if (featureId.equals(featureIdR)) {
									continue;
								}
								Geometry relationGeometry = (Geometry) reSf.getDefaultGeometry();
								if (isEquals) {
									if (interiorPolygon.equals(relationGeometry)
											|| interiorPolygon.intersects(relationGeometry)) {
										// error
										ErrorFeature errorFeature = new ErrorFeature(featureId,
												DMQAOptions.Type.ENTITYINHOLE.getErrCode(),
												DMQAOptions.Type.ENTITYINHOLE.getErrTypeE(),
												DMQAOptions.Type.ENTITYINHOLE.getErrName(langType), "",
												interiorPolygon.getInteriorPoint());
										errorFeatures.add(errorFeature);
									}
								} else {
									if (interiorPolygon.equals(relationGeometry)) {
										// error
										ErrorFeature errorFeature = new ErrorFeature(featureId,
												DMQAOptions.Type.ENTITYINHOLE.getErrCode(),
												DMQAOptions.Type.ENTITYINHOLE.getErrTypeE(),
												DMQAOptions.Type.ENTITYINHOLE.getErrName(langType), "",
												interiorPolygon.getInteriorPoint());
										errorFeatures.add(errorFeature);
									}
								}
							}
						}
						relationSfcIterator.close();
					}
				}
			}
		}
		if (errorFeatures.size() > 0) {
			return errorFeatures;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateLinearDisconnection(DTFeature dtFeature, DTLayer relationlayer,
			OptionTolerance tole) {

		SimpleFeature sf = dtFeature.getSimefeature();
		// filter
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		List<ErrorFeature> errorFeatures = new ArrayList<>();
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		String featureID = sf.getID();

		if (isTrue) {
			GeometryFactory geometryFactory = new GeometryFactory();
			Coordinate[] coordinates = geometry.getCoordinates();
			Geometry firPt = geometryFactory.createPoint(coordinates[0]);
			Geometry lasPt = geometryFactory.createPoint(coordinates[coordinates.length - 1]);
			int coorSize = geometry.getNumPoints();
			Geometry innerPt = null;
			if (coorSize > 2) {
				innerPt = geometry.getInteriorPoint();
			} else {
				innerPt = geometry.getCentroid();
			}
			double value = tole.getValue();
			OptionFilter relationFilter = relationlayer.getFilter();
			List<AttributeFilter> relationConditions = null;
			if (relationFilter != null) {
				relationConditions = relationFilter.getFilter();
			}
			SimpleFeatureCollection relationSfc = relationlayer.getSimpleFeatureCollection();
			SimpleFeatureIterator simpleFeatureIterator = relationSfc.features();
			while (simpleFeatureIterator.hasNext()) {
				SimpleFeature reSf = simpleFeatureIterator.next();
				String reFeatureId = reSf.getID();
				if (relationConditions != null) {
					isTrue = FeatureFilter.filter(reSf, relationConditions);
				} else {
					isTrue = true;
				}
				if (isTrue) {
					Geometry relationGeometry = (Geometry) reSf.getDefaultGeometry();
					if (innerPt.intersects(relationGeometry)) {
						String reLayerId = relationlayer.getLayerID();
						Geometry boundary = relationGeometry.buffer(value);
						if (!boundary.intersects(firPt)) {
							ErrorFeature errorFeature = new ErrorFeature(featureID, reLayerId, reFeatureId,
									DMQAOptions.Type.LINEARDISCONNECTION.getErrCode(),
									DMQAOptions.Type.LINEARDISCONNECTION.getErrTypeE(),
									DMQAOptions.Type.LINEARDISCONNECTION.getErrName(langType), "", firPt);
							errorFeatures.add(errorFeature);
						}
						if (!boundary.intersects(lasPt)) {
							ErrorFeature errorFeature = new ErrorFeature(featureID, reLayerId, reFeatureId,
									DMQAOptions.Type.LINEARDISCONNECTION.getErrCode(),
									DMQAOptions.Type.LINEARDISCONNECTION.getErrTypeE(),
									DMQAOptions.Type.LINEARDISCONNECTION.getErrName(langType), "", lasPt);
							errorFeatures.add(errorFeature);
						}
					}
				}
			}
			simpleFeatureIterator.close();
		}
		if (errorFeatures.size() > 0) {
			return errorFeatures;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateMultiPart(DTFeature feature) {

		SimpleFeature sf = feature.getSimefeature();
		String featureID = sf.getID();
		// String osmId = sf.getAttribute("osm_id").toString();
		// filter
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		List<ErrorFeature> errFeatures = new ArrayList<>();
		if (isTrue) {
			Geometry geometry = (Geometry) sf.getDefaultGeometry();
			int geomNum = geometry.getNumGeometries();
			if (geomNum > 1) {
				for (int i = 0; i < geomNum; i++) {
					Geometry interGeom = geometry.getGeometryN(i);
					ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.MULTIPART.getErrCode(),
							DMQAOptions.Type.MULTIPART.getErrTypeE(), DMQAOptions.Type.MULTIPART.getErrName(langType),
							"", interGeom.getInteriorPoint());
					errFeatures.add(errorFeature);
				}
			}
		}
		if (errFeatures.size() > 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateUNodeMiss(DTFeature dtFeature, DTFeature reFeature) {

		SimpleFeature sf = dtFeature.getSimefeature();
		Geometry geom = (Geometry) sf.getDefaultGeometry();

		// filter
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		// relation filter
		SimpleFeature reSf = reFeature.getSimefeature();
		List<AttributeFilter> refilters = reFeature.getFilter();
		if (refilters != null) {
			isTrue = FeatureFilter.filter(reSf, refilters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		if (isTrue) {
			Geometry reGeom = (Geometry) reSf.getDefaultGeometry();
			if (geom.intersects(reGeom)) {
				boolean isIntersected = false;
				Coordinate[] geomCoors = geom.getCoordinates();
				Coordinate[] tmpCoors = reGeom.getCoordinates();
				for (Coordinate tmpCoor : tmpCoors) {
					Coordinate geomFirsCoor = geomCoors[0];
					Coordinate geomLastCoor = geomCoors[geomCoors.length - 1];
					if (tmpCoor.equals(geomFirsCoor) || tmpCoor.equals(geomLastCoor)) {
						isIntersected = true;
					}
				}
				if (!isIntersected) {
					// ERR
					isError = true;
				}
			}
		}
		if (isError) {
			String featureID = sf.getID();
			ErrorFeature errorFeature = new ErrorFeature(featureID, UFMQAOptions.Type.UNODEMISS.getErrCode(),
					UFMQAOptions.Type.UNODEMISS.getErrTypeE(), UFMQAOptions.Type.UNODEMISS.getErrName(langType), "",
					geom.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateULeaderLine(DTFeature feature, DTFeature reFeature) {

		// 지시선
		SimpleFeature sf = feature.getSimefeature();
		// filter
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		// 관로이력
		SimpleFeature reSf = reFeature.getSimefeature();
		// relation filter
		List<AttributeFilter> refilters = reFeature.getFilter();
		if (refilters != null) {
			isTrue = FeatureFilter.filter(reSf, refilters);
		}
		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
		if (isTrue) {
			// 관로이력
			Geometry relationGeom = (Geometry) reSf.getDefaultGeometry();
			Geometry rotateGeom = null;
			try {
				Geometry boundary = (Geometry) reSf.getAttribute("boundary");
				double rotate = (double) reSf.getAttribute("rotate");
				if (rotate != 0) {
					Point ancorPoint = (Point) relationGeom;
					AffineTransform affineTransform = AffineTransform.getRotateInstance(Math.toRadians(rotate),
							ancorPoint.getX(), ancorPoint.getY());
					MathTransform mathTransform = new AffineTransform2D(affineTransform);
					rotateGeom = JTS.transform(boundary, mathTransform);
				} else {
					rotateGeom = boundary;
				}
			} catch (MismatchedDimensionException | TransformException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String featureID = sf.getID();
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			Geometry returnGeom = rotateGeom.intersection(geom);
			if (!returnGeom.isEmpty()) {
				GeometryFactory factory = new GeometryFactory();
				Coordinate[] coors = returnGeom.getCoordinates();
				for (int i = 0; i < coors.length; i++) {
					errFeatures.add(new ErrorFeature(featureID, UFMQAOptions.Type.ULEADERLINE.getErrCode(),
							UFMQAOptions.Type.ULEADERLINE.getErrTypeE(),
							UFMQAOptions.Type.ULEADERLINE.getErrName(langType), "", factory.createPoint(coors[i])));
				}
			}
		}
		if (errFeatures.size() > 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateUSymbolOut(DTFeature feature, DTLayer relationLayer) {

		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (isTrue) {
			OptionFilter relationFilter = relationLayer.getFilter();
			List<AttributeFilter> relationConditions = null;
			if (relationFilter != null) {
				relationConditions = relationFilter.getFilter();
			}
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			Coordinate coordinate = new Coordinate((double) Math.round(geom.getCoordinate().x * 1000d) / 1000d,
					(double) Math.round(geom.getCoordinate().y * 1000d) / 1000d);

			// geom filter
			SimpleFeatureCollection sfc = relationLayer.getSimpleFeatureCollection();
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
			Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(geom.buffer(0.1)));

			SimpleFeatureSource source = DataUtilities.source(sfc);
			SimpleFeatureCollection filterSfc;

			try {
				filterSfc = source.getFeatures(filter);
				SimpleFeatureIterator relationIterator = filterSfc.features();
				SimpleFeature relationFeature;

				Geometry relationGeom;
				Coordinate[] rCoords;

				boolean isOut = true;

				while (relationIterator.hasNext()) {

					relationFeature = relationIterator.next();

					if (FeatureFilter.filter(relationFeature, relationConditions)) {

						relationGeom = (Geometry) relationFeature.getDefaultGeometry();
						rCoords = relationGeom.getCoordinates();

						for (Coordinate coord : rCoords) {
							Coordinate tempCoord = new Coordinate((double) Math.round(coord.x * 1000d) / 1000d,
									(double) Math.round(coord.y * 1000d) / 1000d);

							if (tempCoord.equals(coordinate)) {
								isOut = false;
								break;
							}
						}

						if (!isOut) {
							break;
						}
					}
				}
				relationIterator.close();
				if (isOut) {
					String featureID = sf.getID();
					ErrorFeature errorFeature = new ErrorFeature(featureID, UFMQAOptions.Type.USYMBOLOUT.getErrCode(),
							UFMQAOptions.Type.USYMBOLOUT.getErrTypeE(),
							UFMQAOptions.Type.USYMBOLOUT.getErrName(langType), "", geom);
					return errorFeature;
				} else {
					return null;
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateSymbolInLine(DTFeature dtFeature, DTLayerList relationLayers,
			List<OptionTolerance> reTolerances) {

		// relation filter
		DefaultFeatureCollection relationDfc = FeatureFilter.filter(relationLayers, reTolerances);

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		// filter
		SimpleFeature sf = dtFeature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (isTrue) {
			String featureID = sf.getID();
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			Coordinate[] coordinates = geom.getCoordinates();
			Coordinate coordinate;

			GeometryFactory factory = new GeometryFactory();

			SimpleFeatureIterator relationIterator;
			SimpleFeature relationFeature;
			Geometry relationGeom;
			Coordinate relationCoord;

			boolean isOut;

			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
			Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(geom.buffer(0.1)));

			SimpleFeatureSource source = DataUtilities.source(relationDfc);
			SimpleFeatureCollection filterSfc;
			try {
				filterSfc = source.getFeatures(filter);
				for (int i = 0; i < coordinates.length; i++) {
					coordinate = new Coordinate((double) Math.round(coordinates[i].x * 1000d) / 1000d,
							(double) Math.round(coordinates[i].y * 1000d) / 1000d);

					relationIterator = filterSfc.features();
					isOut = true;
					while (relationIterator.hasNext()) {
						relationFeature = relationIterator.next();
						relationGeom = (Geometry) relationFeature.getDefaultGeometry();
						relationCoord = relationGeom.getCoordinate();

						Coordinate tempCoord = new Coordinate((double) Math.round(relationCoord.x * 1000d) / 1000d,
								(double) Math.round(relationCoord.y * 1000d) / 1000d);

						if (tempCoord.equals(coordinate)) {
							isOut = false;
							break;
						}
					}
					relationIterator.close();
					if (isOut) {
						errFeatures.add(new ErrorFeature(featureID, UFMQAOptions.Type.SYMBOLINLINE.getErrCode(),
								UFMQAOptions.Type.SYMBOLINLINE.getErrTypeE(),
								UFMQAOptions.Type.SYMBOLINLINE.getErrName(langType), "",
								factory.createPoint(new Coordinate(coordinates[i].x, coordinates[i].y))));
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (errFeatures.size() > 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateSymbolsDistance(DTFeature dtFeature) {

		// filter
		SimpleFeature sf = dtFeature.getSimefeature();
		String featureID = sf.getID();
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
		if (isTrue) {
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			Coordinate[] coordinates = geom.getCoordinates();
			Coordinate coordinate;
			double distance;

			GeometryFactory factory = new GeometryFactory();
			Coordinate[] errLineCoords;
			LineString line;

			// boolean isOut;

			for (int i = 0; i < coordinates.length; i++) {

				coordinate = coordinates[i];

				if (i != coordinates.length - 1) {
					distance = coordinate.distance(coordinates[i + 1]);
					if (distance > 20) {
						errLineCoords = new Coordinate[] { coordinate, coordinates[i + 1] };
						line = factory.createLineString(errLineCoords);
						errFeatures.add(new ErrorFeature(featureID, UFMQAOptions.Type.SYMBOLSDISTANCE.getErrCode(),
								UFMQAOptions.Type.SYMBOLSDISTANCE.getErrTypeE(),
								UFMQAOptions.Type.SYMBOLSDISTANCE.getErrName(langType), "", line.getCentroid()));
					}
				}
			}
		}
		if (errFeatures.size() > 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateLineCross(DTFeature dtFeature, DTLayer relationLayer) {

		SimpleFeature sf = dtFeature.getSimefeature();
		String featureID = sf.getID();
		boolean isTrue = false;
		List<AttributeFilter> filters = dtFeature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		if (isTrue) {

			Coordinate[] geomCoords = geom.getCoordinates();
			GeometryFactory factory = new GeometryFactory();
			Point point;
			OptionFilter relationFilter = relationLayer.getFilter();
			List<AttributeFilter> relationConditions = null;
			if (relationFilter != null) {
				relationConditions = relationFilter.getFilter();
			}

			// geom filter
			SimpleFeatureCollection sfc = relationLayer.getSimpleFeatureCollection();
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
			Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(geom.getEnvelope()));

			SimpleFeatureSource source = DataUtilities.source(sfc);
			SimpleFeatureCollection filterSfc;
			SimpleFeatureIterator iterator;
			SimpleFeature relationSf;

			Geometry relationGeom;
			Geometry crossGeom;

			Coordinate[] crossCoords;
			Coordinate[] rCoords;
			boolean isCross;
			try {
				filterSfc = source.getFeatures(filter);
				iterator = filterSfc.features();
				while (iterator.hasNext()) {
					relationSf = iterator.next();
					if (FeatureFilter.filter(relationSf, relationConditions)) {
						relationGeom = (Geometry) relationSf.getDefaultGeometry();
						rCoords = relationGeom.getCoordinates();
						if (relationGeom.equals(geom)) {
							continue;
						}
						crossGeom = relationGeom.intersection(geom);
						if (crossGeom.getNumGeometries() != 0) {
							crossCoords = crossGeom.getCoordinates();
							isCross = false;
							for (int i = 0; i < crossCoords.length; i++) {
								if (crossCoords[i] != rCoords[0] && crossCoords[i] != rCoords[rCoords.length - 1]) {
									for (Coordinate temp : geomCoords) {
										if (temp.equals(crossCoords[i])) {
											isCross = true;
											break;
										}
									}
									if (!isCross) {
										point = factory.createPoint(crossCoords[i]);
										errFeatures.add(
												new ErrorFeature(featureID, UFMQAOptions.Type.LINECROSS.getErrCode(),
														UFMQAOptions.Type.LINECROSS.getErrTypeE(),
														UFMQAOptions.Type.LINECROSS.getErrName(langType),
														dtFeature.getLayerID(), point));
									}
								}
							}
						}
					}
				}
				iterator.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (errFeatures.size() > 0) {
			return errFeatures;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateUAvrgDPH10(DTFeature feature, DTLayer leaderLayer, DTLayer lineLayer,
			DTLayer textLayer) {

		boolean isError = false;

		// 지시선
		SimpleFeature sf = feature.getSimefeature();
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		// filter
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (isTrue) {
			// filter
			OptionFilter leaderFilter = leaderLayer.getFilter();
			OptionFilter lineFilter = lineLayer.getFilter();
			OptionFilter textFilter = textLayer.getFilter();
			List<AttributeFilter> leaderAttrFilters = null;
			List<AttributeFilter> lineAttrFilters = null;
			List<AttributeFilter> textAttrFilters = null;
			if (leaderFilter != null) {
				leaderAttrFilters = leaderFilter.getFilter();
			}
			if (lineFilter != null) {
				lineAttrFilters = lineFilter.getFilter();
			}
			if (textFilter != null) {
				textAttrFilters = textFilter.getFilter();
			}

			SimpleFeatureCollection leaderSfc = leaderLayer.getSimpleFeatureCollection();
			SimpleFeatureCollection lineSfc = lineLayer.getSimpleFeatureCollection();
			SimpleFeatureCollection textSfc = textLayer.getSimpleFeatureCollection();

			SimpleFeatureIterator leaderIter = leaderSfc.features();
			while (leaderIter.hasNext()) {
				// 지시선
				SimpleFeature leaderSf = leaderIter.next();
				if (FeatureFilter.filter(leaderSf, leaderAttrFilters)) {
					Geometry leaderGeom = (Geometry) leaderSf.getDefaultGeometry();
					if (geom.intersects(leaderGeom.getEnvelope())) {
						SimpleFeatureIterator lineIter = lineSfc.features();
						while (lineIter.hasNext()) {
							// 관로
							SimpleFeature lineSf = lineIter.next();
							if (FeatureFilter.filter(lineSf, lineAttrFilters)) {
								Geometry lineGeom = (Geometry) lineSf.getDefaultGeometry();
								if (lineGeom.intersects(leaderGeom)) {
									double total = 0.0;
									int count = 0;
									boolean isIntersected = false;
									SimpleFeatureIterator textIter = textSfc.features();
									while (textIter.hasNext()) {
										// 심도
										SimpleFeature textSf = textIter.next();
										if (FeatureFilter.filter(textSf, textAttrFilters)) {
											Geometry textGeom = (Geometry) textSf.getDefaultGeometry();
											if (lineGeom.intersects(textGeom)) {
												total += Double.parseDouble((String) textSf.getAttribute("textValue"));
												count++;
												isIntersected = true;
											}
										}
									}
									if (isIntersected) {
										double result = Math.round((total / count) * 10) / 10.0;
										String textValue = (String) sf.getAttribute("textValue");
										String[] strs = textValue.split("/");
										String dAvrg = strs[strs.length - 1];
										String avrgStr = dAvrg.substring(1);
										double avrg = Double.parseDouble(avrgStr);
										if (avrg != result) {
											// ERR
											isError = true;
										}
									}
								}
							}
						}
					}
				}
			}
			leaderIter.close();
		}
		if (isError) {
			String featureID = sf.getID();
			return new ErrorFeature(featureID, UFMQAOptions.Type.UAVRGDPH10.getErrCode(),
					UFMQAOptions.Type.UAVRGDPH10.getErrTypeE(), UFMQAOptions.Type.UAVRGDPH10.getErrName(langType), "",
					geom);
		} else {
			return null;
		}
	}

	/**
	 * @since 2018. 3. 23.
	 * @author DY.Oh
	 * @param feature
	 * @param validatorLayer
	 * @return List<ErrorFeature>
	 */
	@Override
	public List<ErrorFeature> validateFEntityInHole(DTFeature feature, DTLayer validatorLayer) {

		// 지시선
		SimpleFeature sf = feature.getSimefeature();
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		// filter
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		String featureId = sf.getID();
		List<ErrorFeature> errorFeatures = new ArrayList<ErrorFeature>();
		if (isTrue) {
			String geomType = geom.getGeometryType();
			Polygon polygon = null;
			if (geomType.equals("Polygon")) {
				polygon = (Polygon) geom;
			} else if (geomType.equals("MultiPolygon")) {
				int geomNum = geom.getNumGeometries();
				if (geomNum == 1) {
					polygon = (Polygon) geom.getGeometryN(0);
				}
			}
			if (polygon != null) {
				OptionFilter reFilter = validatorLayer.getFilter();
				List<AttributeFilter> reAttrFilters = null;
				if (reFilter != null) {
					reAttrFilters = reFilter.getFilter();
				}

				GeometryFactory factory = new GeometryFactory();
				int holeNum = polygon.getNumInteriorRing();
				if (holeNum > 0) {
					boolean isInter = false;
					for (int i = 0; i < holeNum; i++) {
						LinearRing interiorRing = (LinearRing) polygon.getInteriorRingN(i);
						LinearRing holes[] = null;
						Polygon interiorPolygon = factory.createPolygon(interiorRing, holes);

						FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
						Filter filter = ff.intersects(ff.property("the_geom"),
								ff.literal(interiorPolygon.getEnvelope()));

						SimpleFeatureSource source = DataUtilities.source(validatorLayer.getSimpleFeatureCollection());
						SimpleFeatureCollection filterSfc;
						SimpleFeatureIterator iter = null;
						try {
							filterSfc = source.getFeatures(filter);
							iter = filterSfc.features();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						List<Geometry> interGeoms = new ArrayList<>();
						while (iter.hasNext()) {
							SimpleFeature reSf = iter.next();
							if (featureId.equals(reSf.getID())) {
								continue;
							}
							if (FeatureFilter.filter(reSf, reAttrFilters)) {
								Geometry reGeom = (Geometry) reSf.getDefaultGeometry();
								if (interiorPolygon.intersects(reGeom)) {
									interGeoms.add(reGeom);
								}
							}
						}
						iter.close();
						double interArea = interiorPolygon.getArea();
						if (interGeoms.size() == 1) {
							Geometry interGeom = interGeoms.get(0);
							double interGeomArea = interGeom.getArea();
							if (interArea - interGeomArea < 0.1) {
								isInter = true;
							}
						}
						if (interGeoms.size() > 1) {
							Geometry unionGeom = interGeoms.get(0);
							for (int j = 1; j < interGeoms.size(); j++) {
								unionGeom = unionGeom.union(interGeoms.get(j));
							}
							double unionArea = unionGeom.getArea();
							if (interArea - unionArea < 0.1) {
								isInter = true;
							}
						}
						if (!isInter) {
							ErrorFeature errorFeature = new ErrorFeature(featureId,
									FTMQAOptions.Type.FENTITYINHOLE.getErrCode(),
									FTMQAOptions.Type.FENTITYINHOLE.getErrTypeE(),
									FTMQAOptions.Type.FENTITYINHOLE.getErrName(langType), "",
									interiorPolygon.getInteriorPoint());
							errorFeatures.add(errorFeature);
						}

					}
				}
			}
		}
		if (errorFeatures.size() > 0) {
			return errorFeatures;
		} else {
			return null;
		}
	}

	/**
	 * @since 2018. 4. 10.
	 * @author DY.Oh
	 * @param feature
	 * @param relationLayers
	 * @return
	 */
	@Override
	public ErrorFeature validateSymbolOut(DTFeature feature, DTLayerList relationLayers) {

		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		boolean isOut = true;
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		if (isTrue) {

			GeometryFactory factory = new GeometryFactory();

			for (DTLayer relationLayer : relationLayers) {
				OptionFilter relationFilter = relationLayer.getFilter();
				List<AttributeFilter> relationConditions = null;
				if (relationFilter != null) {
					relationConditions = relationFilter.getFilter();
				}
				SimpleFeatureCollection relationSfc = relationLayer.getSimpleFeatureCollection();
				SimpleFeatureIterator relationIterator = relationSfc.features();
				SimpleFeature relationFeature;

				Geometry relationGeom;
				while (relationIterator.hasNext()) {
					relationFeature = relationIterator.next();
					if (FeatureFilter.filter(relationFeature, relationConditions)) {
						relationGeom = (Geometry) relationFeature.getDefaultGeometry();
						Coordinate[] reCoors = relationGeom.getCoordinates();
						LinearRing ring = null;
						int length = reCoors.length;
						if (length < 3) {
							continue;
						}
						if (!reCoors[0].equals(reCoors[length - 1])) {
							Coordinate[] tmpCoors = new Coordinate[length + 1];
							for (int i = 0; i < length; i++) {
								tmpCoors[i] = reCoors[i];
							}
							tmpCoors[length] = reCoors[0];
							ring = factory.createLinearRing(tmpCoors);
						} else {
							ring = factory.createLinearRing(reCoors);
						}
						Polygon polygon = factory.createPolygon(ring, null);
						if (polygon.intersects(geom)) {
							isOut = false;
							break;
						}
					}
				}
				relationIterator.close();
			}
		}
		if (isOut) {
			String featureID = sf.getID();
			ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.SYMBOLOUT.getErrCode(),
					DMQAOptions.Type.SYMBOLOUT.getErrTypeE(), DMQAOptions.Type.SYMBOLOUT.getErrName(langType), "",
					geom);
			return errorFeature;
		} else {
			return null;
		}
	}
}