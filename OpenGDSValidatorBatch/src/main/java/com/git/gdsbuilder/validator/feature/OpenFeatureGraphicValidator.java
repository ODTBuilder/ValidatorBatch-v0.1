package com.git.gdsbuilder.validator.feature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.geometry.MismatchedDimensionException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;
import org.opengis.referencing.operation.TransformException;

import com.git.gdsbuilder.type.dt.feature.DTFeature;
import com.git.gdsbuilder.type.dt.layer.OpenDTLayer;
import com.git.gdsbuilder.type.dt.layer.OpenDTLayerList;
import com.git.gdsbuilder.type.validate.error.ErrorFeature;
import com.git.gdsbuilder.type.validate.option.en.LangType;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFigure;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.git.gdsbuilder.type.validate.option.type.OpenDMQAOptions;
import com.git.gdsbuilder.validator.feature.filter.FeatureFilter;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

public class OpenFeatureGraphicValidator {

	LangType langType;

	public OpenFeatureGraphicValidator(LangType langType) {
		this.langType = langType;
	}

	// 허용범위 이하 길이 (SmallLength)
	public List<ErrorFeature> validateSmallLength(DTFeature feature, OptionTolerance tolerance, String epsg) {

		SimpleFeature sf = feature.getSimefeature();
		List<AttributeFilter> filters = feature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (tolerance == null) {
			return null;
		}
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		if (epsg.equals("EPSG:4326")) {
			try {
				CoordinateReferenceSystem dataCRS = CRS.decode("EPSG:4326");
				CoordinateReferenceSystem worldCRS = CRS.decode("EPSG:32652");
				MathTransform transform = CRS.findMathTransform(dataCRS, worldCRS, true);
				try {
					geom = JTS.transform(geom, transform);
				} catch (MismatchedDimensionException | TransformException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Double value = tolerance.getValue();
		String conditon = tolerance.getCondition();
		List<ErrorFeature> errList = new ArrayList<>();
		if (isTrue) {
			if (geom.getGeometryType().equals("MultiLineString") || geom.getGeometryType().equals("LineString")) {
				for (int i = 0; i < geom.getNumGeometries(); i++) {
					boolean isError = false;
					Geometry innerGeom = geom.getGeometryN(i);
					double geomLength = innerGeom.getLength();
					if (conditon.equals("over")) {
						if (geomLength <= value) {
							isError = true;
						}
					} else if (conditon.equals("under")) {
						if (geomLength >= value) {
							isError = true;
						}
					} else if (conditon.equals("equal")) {
						if (geomLength != value) {
							isError = true;
						}
					} else if (conditon.equals("andover")) {
						if (geomLength < value) {
							isError = true;
						}
					} else if (conditon.equals("andunder")) {
						if (geomLength > value) {
							isError = true;
						}
					}
					if (isError) {
						Geometry errPt = null;
						try {
							errPt = innerGeom.getInteriorPoint();
						} catch (TopologyException e) {
							Coordinate[] coors = innerGeom.getCoordinates();
							errPt = new GeometryFactory().createPoint(coors[0]);
						}
						String layerID = feature.getLayerID();
						ErrorFeature errFeature = new ErrorFeature();
						errFeature.setLayerID(layerID);
						errFeature.setErrCode(OpenDMQAOptions.QAType.SMALLLENGTH.getErrCode());
						errFeature.setErrType(OpenDMQAOptions.QAType.SMALLLENGTH.getErrType(langType));
						errFeature.setErrName(OpenDMQAOptions.QAType.SMALLLENGTH.getErrName(langType));
						errFeature.setErrPoint(errPt);
						errList.add(errFeature);
					}
				}
			}
			if (errList.size() > 0) {
				return errList;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	// 허용범위 이하 면적 (SmallArea)
	public List<ErrorFeature> validateSmallArea(DTFeature feature, OptionTolerance tolerance, String epsg) {

		SimpleFeature sf = feature.getSimefeature();

		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (tolerance == null) {
			return null;
		}
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		if (epsg.equals("EPSG:4326")) {
			try {
				CoordinateReferenceSystem dataCRS = CRS.decode("EPSG:4326");
				CoordinateReferenceSystem worldCRS = CRS.decode("EPSG:32652");
				MathTransform transform = CRS.findMathTransform(dataCRS, worldCRS, true);
				try {
					geom = JTS.transform(geom, transform);
				} catch (MismatchedDimensionException | TransformException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} catch (FactoryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Double value = tolerance.getValue();
		String conditon = tolerance.getCondition();
		List<ErrorFeature> errList = new ArrayList<>();
		if (isTrue) {
			if (geom == null || geom.isEmpty()) {
				return null;
			}
			if (geom.getGeometryType().equals("MultiPolygon") || geom.getGeometryType().equals("Polygon")) {
				for (int i = 0; i < geom.getNumGeometries(); i++) {
					boolean isError = false;
					Geometry innerGeom = geom.getGeometryN(i);
					double geomArea = innerGeom.getArea();
					if (conditon.equals("over")) {
						if (geomArea <= value) {
							isError = true;
						}
					} else if (conditon.equals("under")) {
						if (geomArea >= value) {
							isError = true;
						}
					} else if (conditon.equals("equal")) {
						if (geomArea != value) {
							isError = true;
						}
					} else if (conditon.equals("andover")) {
						if (geomArea < value) {
							isError = true;
						}
					} else if (conditon.equals("andunder")) {
						if (geomArea > value) {
							isError = true;
						}
					}
					if (isError) {
						Geometry errPt = null;
						try {
							errPt = innerGeom.getInteriorPoint();
						} catch (TopologyException e) {
							Coordinate[] coors = innerGeom.getCoordinates();
							errPt = new GeometryFactory().createPoint(coors[0]);
						}
						String layerID = feature.getLayerID();
						ErrorFeature errFeature = new ErrorFeature();
						errFeature.setLayerID(layerID);
						errFeature.setErrCode(OpenDMQAOptions.QAType.SMALLAREA.getErrCode());
						errFeature.setErrType(OpenDMQAOptions.QAType.SMALLAREA.getErrType(langType));
						errFeature.setErrName(OpenDMQAOptions.QAType.SMALLAREA.getErrName(langType));
						errFeature.setErrPoint(errPt);
						errList.add(errFeature);
					}
				}
			}
			if (errList.size() > 0) {
				return errList;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	// 단독존재오류 (Self Entity)
	public List<ErrorFeature> validateSelfEntity(DTFeature feature, DTFeature reFeature, OptionTolerance tolerance) {

		boolean isTrue = false;
		SimpleFeature sf = feature.getSimefeature();

		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (!isTrue) {
			return null;
		}
		// relation filter
		SimpleFeature reSf = reFeature.getSimefeature();
		List<AttributeFilter> refilters = reFeature.getFilter();
		if (refilters != null) {
			isTrue = FeatureFilter.filter(reSf, refilters);
		} else {
			isTrue = true;
		}
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		Geometry reGeom = (Geometry) reSf.getDefaultGeometry();

		if (geom == null || reGeom == null) {
			return null;
		}
		if (!geom.isValid() || !reGeom.isValid()) {
			return null;
		}
		if (!geom.isSimple() || !reGeom.isSimple()) {
			return null;
		}
		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
		GeometryFactory geometryFactory = new GeometryFactory();
		if (isTrue) {
			Geometry returnGeom = geom.intersection(reGeom);
			if (returnGeom != null && !returnGeom.isEmpty()) {
				String layerID = feature.getLayerID();
				String refLayerID = reFeature.getLayerID();
				int numGeom = reGeom.getNumGeometries();

				String conditon = tolerance.getCondition();
				Double value = tolerance.getValue();
				for (int n = 0; n < numGeom; n++) {
					Geometry innerGeom = returnGeom.getGeometryN(n);
					String returnGeomType = innerGeom.getGeometryType();
					if (conditon == null || value == null) {
						ErrorFeature errFeature = new ErrorFeature();
						errFeature.setLayerID(layerID);
						errFeature.setRefLayerId(refLayerID);
						errFeature.setErrCode(OpenDMQAOptions.QAType.SELFENTITY.getErrCode());
						errFeature.setErrType(OpenDMQAOptions.QAType.SELFENTITY.getErrType(langType));
						errFeature.setErrName(OpenDMQAOptions.QAType.SELFENTITY.getErrName(langType));
						errFeature.setErrPoint(innerGeom.getInteriorPoint());
						errFeatures.add(errFeature);
					} else {
						if (returnGeomType.equals("LineString")) {
							boolean isError = false;
							double geomLength = returnGeom.getLength();
							if (conditon.equals("over")) {
								if (geomLength <= value) {
									isError = true;
								}
							} else if (conditon.equals("under")) {
								if (geomLength >= value) {
									isError = true;
								}
							} else if (conditon.equals("equal")) {
								if (geomLength != value) {
									isError = true;
								}
							} else if (conditon.equals("andover")) {
								if (geomLength < value) {
									isError = true;
								}
							} else if (conditon.equals("andunder")) {
								if (geomLength > value) {
									isError = true;
								}
							}
							if (isError) {
								Coordinate[] coordinates = innerGeom.getCoordinates();
								Point startPoint = geometryFactory.createPoint(coordinates[0]);
								ErrorFeature errFeature = new ErrorFeature();
								errFeature.setLayerID(layerID);
								errFeature.setRefLayerId(refLayerID);
								errFeature.setErrCode(OpenDMQAOptions.QAType.SELFENTITY.getErrCode());
								errFeature.setErrType(OpenDMQAOptions.QAType.SELFENTITY.getErrType(langType));
								errFeature.setErrName(OpenDMQAOptions.QAType.SELFENTITY.getErrName(langType));
								errFeature.setErrPoint(startPoint);
								errFeatures.add(errFeature);
							}
						} else if (returnGeomType.equals("Point")) {
							if (conditon.equals("equal") && value == 0) {
								continue;
							} else {
								ErrorFeature errFeature = new ErrorFeature();
								errFeature.setLayerID(layerID);
								errFeature.setRefLayerId(refLayerID);
								errFeature.setErrCode(OpenDMQAOptions.QAType.SELFENTITY.getErrCode());
								errFeature.setErrType(OpenDMQAOptions.QAType.SELFENTITY.getErrType(langType));
								errFeature.setErrName(OpenDMQAOptions.QAType.SELFENTITY.getErrName(langType));
								errFeature.setErrPoint((Point) innerGeom);
								errFeatures.add(errFeature);
							}
						} else if (returnGeomType.equals("Polygon")) {
							boolean isError = false;
							double geomLength = returnGeom.getArea();
							if (conditon.equals("over")) {
								if (geomLength <= value) {
									isError = true;
								}
							} else if (conditon.equals("under")) {
								if (geomLength >= value) {
									isError = true;
								}
							} else if (conditon.equals("equal")) {
								if (geomLength != value) {
									isError = true;
								}
							} else if (conditon.equals("andover")) {
								if (geomLength < value) {
									isError = true;
								}
							} else if (conditon.equals("andunder")) {
								if (geomLength > value) {
									isError = true;
								}
							}
							if (isError) {
								ErrorFeature errFeature = new ErrorFeature();
								errFeature.setLayerID(layerID);
								errFeature.setRefLayerId(refLayerID);
								errFeature.setErrCode(OpenDMQAOptions.QAType.SELFENTITY.getErrCode());
								errFeature.setErrType(OpenDMQAOptions.QAType.SELFENTITY.getErrType(langType));
								errFeature.setErrName(OpenDMQAOptions.QAType.SELFENTITY.getErrName(langType));
								errFeature.setErrPoint(innerGeom.getInteriorPoint());
								errFeatures.add(errFeature);
							}
						}
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

	// 요소중복오류 (EntityDuplicated)
	public ErrorFeature validateEntityDuplicated(DTFeature feature) {

		SimpleFeature sf = feature.getSimefeature();

		Geometry geom = (Geometry) sf.getDefaultGeometry();
		Geometry errPt = null;
		try {
			errPt = geom.getInteriorPoint();
		} catch (TopologyException e) {
			Coordinate[] coors = geom.getCoordinates();
			errPt = new GeometryFactory().createPoint(coors[0]);
		}
		String layerID = feature.getLayerID();
		ErrorFeature errFeature = new ErrorFeature();
		errFeature.setLayerID(layerID);
		errFeature.setErrCode(OpenDMQAOptions.QAType.SELFENTITY.getErrCode());
		errFeature.setErrType(OpenDMQAOptions.QAType.SELFENTITY.getErrType(langType));
		errFeature.setErrName(OpenDMQAOptions.QAType.SELFENTITY.getErrName(langType));
		errFeature.setErrPoint(errPt);
		return errFeature;
	}

	// 등고선 꺾임 오류 (ConOverDegree)
	public List<ErrorFeature> validateConOverDegree(DTFeature feature, OptionTolerance tolerance) {

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		SimpleFeature sf = feature.getSimefeature();
		List<AttributeFilter> filters = feature.getFilter();
		boolean isTrue = false;

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (isTrue) {
			Double value = tolerance.getValue();
			String conditon = tolerance.getCondition();
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			String layerID = feature.getLayerID();
			int geomNum = geom.getNumGeometries();
			for (int g = 0; g < geomNum; g++) {
				Geometry innerGeom = geom.getGeometryN(g);
				Coordinate[] coordinates = innerGeom.getCoordinates();
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
						}
						if (conditon.equals("andover")) {
							if (angle <= value) {
								isError = true;
							}
						} else if (conditon.equals("andunder")) {
							if (angle >= value) {
								isError = true;
							}
						} else if (conditon.equals("equal")) {
							if (angle != value) {
								isError = true;
							}
						}
						if (isError) {
							GeometryFactory factory = new GeometryFactory();
							Point errPoint = factory.createPoint(b);
							ErrorFeature errFeature = new ErrorFeature();
							errFeature.setLayerID(layerID);
							errFeature.setErrCode(OpenDMQAOptions.QAType.CONOVERDEGREE.getErrCode());
							errFeature.setErrType(OpenDMQAOptions.QAType.CONOVERDEGREE.getErrType(langType));
							errFeature.setErrName(OpenDMQAOptions.QAType.CONOVERDEGREE.getErrName(langType));
							errFeature.setErrPoint(errPoint);
							errFeatures.add(errFeature);
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

	// 등고선교차오류 (ConIntersected)
	public List<ErrorFeature> validateConIntersected(DTFeature feature, DTFeature reFeature) {

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		SimpleFeature sfi = feature.getSimefeature();
		SimpleFeature sfj = reFeature.getSimefeature();

		List<AttributeFilter> filters = feature.getFilter();
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
			String layerID = feature.getLayerID();
			GeometryFactory geometryFactory = new GeometryFactory();
			Geometry geom = (Geometry) sfi.getDefaultGeometry();
			Geometry reGeom = (Geometry) sfj.getDefaultGeometry();
			if (geom.crosses(reGeom)) {
				Geometry returnGeom = geom.intersection(reGeom);
				Coordinate[] coordinates = returnGeom.getCoordinates();
				for (int i = 0; i < coordinates.length; i++) {
					Coordinate coordinate = coordinates[i];
					Geometry errPoint = geometryFactory.createPoint(coordinate);
					ErrorFeature errFeature = new ErrorFeature();
					errFeature.setLayerID(layerID);
					errFeature.setErrCode(OpenDMQAOptions.QAType.CONINTERSECTED.getErrCode());
					errFeature.setErrType(OpenDMQAOptions.QAType.CONINTERSECTED.getErrType(langType));
					errFeature.setErrName(OpenDMQAOptions.QAType.CONINTERSECTED.getErrName(langType));
					errFeature.setErrPoint(errPoint);
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

	// 등고선 끊김오류 (ConBreak)
	public List<ErrorFeature> validateConBreak(DTFeature feature, OptionTolerance tolerance, OptionFigure figure,
			SimpleFeatureCollection sfc, OpenDTLayerList reDTLayers, Geometry bEnvelope) throws IOException {

		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = true;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		}
		if (isTrue) {
			List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
			double value = tolerance.getValue();
			// String conditon = tolerance.getCondition();
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			Coordinate[] coors = geom.getCoordinates();
			GeometryFactory factory = new GeometryFactory();
			Geometry firPt = factory.createPoint(coors[0]);
			Geometry lasPt = factory.createPoint(coors[coors.length - 1]);

			Geometry firPtBf = firPt.buffer(value);
			Geometry lasPtBf = lasPt.buffer(value);

			boolean firErr = true;
			boolean lasErr = true;

			String layerID = feature.getLayerID();

			SimpleFeatureIterator iter = sfc.features();
			List<AttributeFigure> attrFigures = figure.getFigure();
			// 1. 등고선의 첫점과 끝점이 같은지 확인
			if (!firPt.equals(lasPt)) {
				// 2. 등고선의 첫점, 끝점이 envelop에 맞닿아있는지 확인
				Geometry bEnvelopeBdr = bEnvelope.getBoundary();
				if (firPt.distance(bEnvelopeBdr) == 0) {
					firErr = false;
				}
				if (lasPt.distance(bEnvelopeBdr) == 0) {
					lasErr = false;
				}
				if (firErr || lasErr) {
					// 3. 등고선의 첫점, 끝점과 맞닿아 있는 다른 등고선 객체 확인
					selfFor: while (iter.hasNext()) {
						SimpleFeature seSf = iter.next();
						Geometry seGeom = (Geometry) seSf.getDefaultGeometry();
						boolean isReTrue = true;
						if (filters != null) {
							isReTrue = FeatureFilter.filter(seSf, filters);
						}
						if (!isReTrue || geom.equals(seGeom)) {
							continue;
						}
						if (isReTrue) {
							// 4. 등고선의 첫점, 끝점과 맞닿아 있는 다른 등고선 객체와 속성이 같은지 확인
							boolean atrrErr = false;
							for (AttributeFigure attrFigure : attrFigures) {
								String key = attrFigure.getKey();
								Object attributeObj = sf.getAttribute(key);
								Object reAttributeObj = seSf.getAttribute(key);
								if (attributeObj == null) {
									if (reAttributeObj != null) {
										atrrErr = true;
										break;
									}
								} else {
									if (reAttributeObj == null) {
										atrrErr = true;
										break;
									}
									if (!attributeObj.toString().equals(reAttributeObj.toString())) {
										atrrErr = true;
										break;
									}
								}
							}

							Coordinate[] seCoors = seGeom.getCoordinates();
							Geometry reFirPt = factory.createPoint(seCoors[0]);
							Geometry reLasPt = factory.createPoint(seCoors[seCoors.length - 1]);

							if (firPtBf.distance(reFirPt) < value || firPtBf.distance(reLasPt) < value) {
								// 4.1. 등고선의 첫점이 맞닿아 있는 다른 등고선 객체와 속성이 다름
								firErr = false;
								if (atrrErr) {
									ErrorFeature errFeature = new ErrorFeature();
									errFeature.setLayerID(layerID);
									errFeature.setErrCode(OpenDMQAOptions.QAType.CONBREAK.getErrCode());
									errFeature.setErrType(OpenDMQAOptions.QAType.CONBREAK.getErrType(langType));
									errFeature.setErrName(OpenDMQAOptions.QAType.CONBREAK.getErrName(langType));
									errFeature.setErrPoint(firPt);
									errFeatures.add(errFeature);
								}
							}
							if (lasPtBf.distance(reFirPt) < value || lasPtBf.distance(reLasPt) < value) {
								// 4.2. 등고선의 끝점이 맞닿아 있는 다른 등고선 객체와 속성이 다름
								lasErr = false;
								if (atrrErr) {
									ErrorFeature errFeature = new ErrorFeature();
									errFeature.setLayerID(layerID);
									errFeature.setErrCode(OpenDMQAOptions.QAType.CONBREAK.getErrCode());
									errFeature.setErrType(OpenDMQAOptions.QAType.CONBREAK.getErrType(langType));
									errFeature.setErrName(OpenDMQAOptions.QAType.CONBREAK.getErrName(langType));
									errFeature.setErrPoint(firPt);
									errFeatures.add(errFeature);
								}
							}
						}
					}
					iter.close();
				}
			} else {
				return null;
			}
			if (!firErr && !lasErr) {
				if (errFeatures.size() > 0) {
					return errFeatures;
				} else {
					return null;
				}
			}

			// 5. 관계 레이어 검수
			reFor: for (OpenDTLayer reDTLayer : reDTLayers) {
				OptionFilter reAttrFilter = reDTLayer.getFilter();
				List<AttributeFilter> reAttrFilters = null;
				if (reAttrFilter != null) {
					reAttrFilters = reAttrFilter.getFilter();
				}
				SimpleFeatureCollection reSfc = reDTLayer.getSimpleFeatureCollection();
				SimpleFeatureIterator reIter = reSfc.features();
				while (reIter.hasNext()) {
					SimpleFeature reSf = reIter.next();
					boolean isReTrue = true;
					if (reAttrFilters != null) {
						isReTrue = FeatureFilter.filter(reSf, reAttrFilters);
					}
					if (!isReTrue) {
						continue;
					}
					Geometry reGeom = (Geometry) reSf.getDefaultGeometry();
					if (reGeom.getGeometryType().equals("Polygon") || reGeom.getGeometryType().equals("MultiPolygon")) {
						Geometry boundary = reGeom.getBoundary();
						if (firPtBf.intersects(boundary)) {
							firErr = false;
						}
						if (lasPtBf.intersects(boundary)) {
							lasErr = false;
						}
					} else {
						if (firPtBf.intersects(reGeom)) {
							firErr = false;
						}
						if (lasPtBf.intersects(reGeom)) {
							lasErr = false;
						}
					}
					if (!firErr && !lasErr) {
						reIter.close();
						break reFor;
					}
				}
				if (firErr || lasErr) {
					reIter.close();
				}
			}
			if (firErr) {
				ErrorFeature errFeature = new ErrorFeature();
				errFeature.setLayerID(layerID);
				errFeature.setErrCode(OpenDMQAOptions.QAType.CONBREAK.getErrCode());
				errFeature.setErrType(OpenDMQAOptions.QAType.CONBREAK.getErrType(langType));
				errFeature.setErrName(OpenDMQAOptions.QAType.CONBREAK.getErrName(langType));
				errFeature.setErrPoint(firPt);
				errFeatures.add(errFeature);
			}
			if (lasErr) {
				ErrorFeature errFeature = new ErrorFeature();
				errFeature.setLayerID(layerID);
				errFeature.setErrCode(OpenDMQAOptions.QAType.CONBREAK.getErrCode());
				errFeature.setErrType(OpenDMQAOptions.QAType.CONBREAK.getErrType(langType));
				errFeature.setErrName(OpenDMQAOptions.QAType.CONBREAK.getErrName(langType));
				errFeature.setErrPoint(lasPt);
				errFeatures.add(errFeature);
			}
			if (errFeatures.size() > 0) {
				return errFeatures;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	// 등고선 직선화미처리오류(UselessPoint)
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
			String layerID = dtFeature.getLayerID();
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
						double tmpLength = a.distance(b);
						double distance = JTS.orthodromicDistance(a, b, crs);
						if (tmpLength < 0.01) {
							isDistError = true;
						}
					}
					if (isDistError && isAngError) {
						GeometryFactory gFactory = new GeometryFactory();
						Geometry returnGeom = gFactory.createPoint(b);
						ErrorFeature errFeature = new ErrorFeature();
						errFeature.setLayerID(layerID);
						errFeature.setErrCode(OpenDMQAOptions.QAType.USELESSPOINT.getErrCode());
						errFeature.setErrType(OpenDMQAOptions.QAType.USELESSPOINT.getErrType(langType));
						errFeature.setErrName(OpenDMQAOptions.QAType.USELESSPOINT.getErrName(langType));
						errFeature.setErrPoint(returnGeom);
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

	// 중복점오류(DuplicatedPoint)
	public List<ErrorFeature> validatePointDuplicated(DTFeature feature) {

		SimpleFeature sf = feature.getSimefeature();

		if (sf.getAttribute("osm_id").toString().equals("310762455")) {
			System.out.println("");
		}

		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
		if (isTrue) {
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			String layerID = feature.getLayerID();
			int numGeom = geom.getNumGeometries();
			for (int i = 0; i < numGeom; i++) {
				Geometry singleGeom = geom.getGeometryN(i);
				if (singleGeom instanceof LineString) {
					LineString lineString = (LineString) singleGeom;
					errFeatures.addAll(pointDuplicated(lineString.getCoordinates(), geom, layerID));
				}
				if (singleGeom instanceof Polygon) {
					Polygon polygon = (Polygon) singleGeom;
					LineString exteriorRing = polygon.getExteriorRing();
					errFeatures.addAll(pointDuplicated(exteriorRing.getCoordinates(), geom, layerID));
					int numInnerRings = polygon.getNumInteriorRing();
					for (int in = 0; in < numInnerRings; in++) {
						LineString innerRing = polygon.getInteriorRingN(in);
						errFeatures.addAll(pointDuplicated(innerRing.getCoordinates(), geom, layerID));
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

	private List<ErrorFeature> pointDuplicated(Coordinate[] coors, Geometry geom, String layerID) {

		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();

		int coorLength = coors.length;
		if (coorLength == 2) {
			Coordinate coor0 = coors[0];
			Coordinate coor1 = coors[1];
			if (coor0.equals3D(coor1)) {
				Geometry errLasPt = new GeometryFactory().createPoint(coor1);
				ErrorFeature errFeature = new ErrorFeature();
				errFeature.setLayerID(layerID);
				errFeature.setErrCode(OpenDMQAOptions.QAType.POINTDUPLICATED.getErrCode());
				errFeature.setErrType(OpenDMQAOptions.QAType.POINTDUPLICATED.getErrType(langType));
				errFeature.setErrName(OpenDMQAOptions.QAType.POINTDUPLICATED.getErrName(langType));
				errFeature.setErrPoint(errLasPt);
				errFeatures.add(errFeature);
			}
		}
		if (coorLength > 2) {
			Map<String, Object> errMap = new HashMap<>();
			for (int c = 0; c < coorLength - 1; c++) {
				Coordinate coor0 = coors[c];
				Coordinate coor1 = coors[c + 1];
				if (coor0.equals3D(coor1)) {
					Geometry errFirPt = new GeometryFactory().createPoint(coor0);
					Geometry errLasPt = new GeometryFactory().createPoint(coor1);

					ErrorFeature errFir = new ErrorFeature();
					errFir.setLayerID(layerID);
					errFir.setErrCode(OpenDMQAOptions.QAType.POINTDUPLICATED.getErrCode());
					errFir.setErrType(OpenDMQAOptions.QAType.POINTDUPLICATED.getErrType(langType));
					errFir.setErrName(OpenDMQAOptions.QAType.POINTDUPLICATED.getErrName(langType));
					errFir.setErrPoint(errFirPt);
					errFeatures.add(errFir);

					ErrorFeature errLas = new ErrorFeature();
					errLas.setLayerID(layerID);
					errLas.setErrCode(OpenDMQAOptions.QAType.POINTDUPLICATED.getErrCode());
					errLas.setErrType(OpenDMQAOptions.QAType.POINTDUPLICATED.getErrType(langType));
					errLas.setErrName(OpenDMQAOptions.QAType.POINTDUPLICATED.getErrName(langType));
					errLas.setErrPoint(errLasPt);

					errMap.put(String.valueOf(c), errFir);
					errMap.put(String.valueOf(c + 1), errLas);
				}
			}
			// 중복 제거
			Set keyset = errMap.keySet();
			if (keyset.size() > 0) {
				Iterator iter = keyset.iterator();
				while (iter.hasNext()) {
					String key = (String) iter.next();
					errFeatures.add((ErrorFeature) errMap.get(key));
				}
			}
		}
		return errFeatures;
	}

	// 경계초과오류 (OutBoundary)
//	public ErrorFeature validateOutBoundary(DTFeature feature, OptionTolerance tolerance, BasicDTLayerList reDTLayers) {
//
//		// simplefeature : 터널, 지하도, 교량.....
//		// relationSfc : 도로경계
//		SimpleFeature sf = feature.getSimefeature();
//		boolean isTrue = true;
//		List<AttributeFilter> filters = feature.getFilter();
//		if (filters != null) {
//			isTrue = FeatureFilter.filter(sf, filters);
//		}
//		if (isTrue) {
//			Geometry geom = (Geometry) sf.getDefaultGeometry();
//			Coordinate[] geomCoors = geom.getCoordinates();
//			int geomCoorsLength = geomCoors.length;
//			Double value = tolerance.getValue();
//			String layerID = feature.getLayerID();
//
//			boolean isErr = true;
//			reFor: for (BasicDTLayer reDTLayer : reDTLayers) {
//				SimpleFeatureCollection reSfc = reDTLayer.getSimpleFeatureCollection();
//				SimpleFeatureIterator iterator = reSfc.features();
//				OptionFilter reFilter = reDTLayer.getFilter();
//				List<AttributeFilter> reAttrFilters = null;
//				if (reFilter != null) {
//					reAttrFilters = reFilter.getFilter();
//				}
//				while (iterator.hasNext()) {
//					// A001
//					SimpleFeature relationSf = iterator.next();
//					if (FeatureFilter.filter(relationSf, reAttrFilters)) {
//						Geometry relationGeom = (Geometry) relationSf.getDefaultGeometry();
//						if (relationGeom == null) {
//							continue;
//						}
//						boolean allContains = true;
//						if (geom.intersects(relationGeom)) {
//							for (int j = 0; j < geomCoorsLength; j++) {
//								Coordinate coor = geomCoors[j];
//								Geometry pt = new GeometryFactory().createPoint(coor);
//								if (!pt.intersects(relationGeom.buffer(value))) {
//									allContains = false;
//								}
//							}
//						} else {
//							allContains = false;
//						}
//						if (allContains) {
//							isErr = false;
//							break reFor;
//						}
//					}
//				}
//				iterator.close();
//			}
//			if (isErr) {
//				Geometry errPt = null;
//				try {
//					errPt = geom.getInteriorPoint();
//				} catch (TopologyException e) {
//					Coordinate[] coors = geom.getCoordinates();
//					errPt = new GeometryFactory().createPoint(coors[0]);
//				}
//				ErrorFeature errFeature = new ErrorFeature();
//				errFeature.setLayerID(layerID);
//				errFeature.setErrCode(OpenDMQAOptions.QAType.OUTBOUNDARY.getErrCode());
//				errFeature.setErrType(OpenDMQAOptions.QAType.OUTBOUNDARY.getErrType(langType));
//				errFeature.setErrName(OpenDMQAOptions.QAType.OUTBOUNDARY.getErrName(langType));
//				errFeature.setErrPoint(errPt);
//				return errFeature;
//			} else {
//				return null;
//			}
//		} else {
//			return null;
//		}
//	}

	public ErrorFeature validateOutBoundary(DTFeature feature, OptionTolerance tolerance,
			SimpleFeatureCollection reValSfc) {

		// relationSfc : 도로경계
		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = true;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		}
		if (isTrue) {
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			Coordinate[] geomCoors = geom.getCoordinates();
			int geomCoorsLength = geomCoors.length;
			String layerID = feature.getLayerID();

			String condition = tolerance.getCondition();
			Double value = tolerance.getValue();

			boolean isErr = true;
			SimpleFeatureIterator reIterator = reValSfc.features();
			while (reIterator.hasNext()) {
				// A001
				SimpleFeature relationSf = reIterator.next();
				Geometry relationGeom = (Geometry) relationSf.getDefaultGeometry();
				if (relationGeom == null) {
					continue;
				}
				boolean allContains = true;
				if (condition == null || value == null) {
					if (!geom.equals(relationGeom)) {
						allContains = false;
					}
				} else {
					Geometry bufferGeom = relationGeom.buffer(value);
					if (geom.intersects(relationGeom)) {
						for (int j = 0; j < geomCoorsLength; j++) {
							Coordinate coor = geomCoors[j];
							Geometry pt = new GeometryFactory().createPoint(coor);
							if (!pt.intersects(bufferGeom)) {
								allContains = false;
							}
						}
					} else {
						allContains = false;
					}
					if (allContains) {
						isErr = false;
						break;
					}
				}
			}
			reIterator.close();
			if (isErr) {
				Geometry errPt = null;
				try {
					errPt = geom.getInteriorPoint();
				} catch (TopologyException e) {
					Coordinate[] coors = geom.getCoordinates();
					errPt = new GeometryFactory().createPoint(coors[0]);
				}
				ErrorFeature errFeature = new ErrorFeature();
				errFeature.setLayerID(layerID);
				errFeature.setRefLayerId(reValSfc.getSchema().getName().getLocalPart());
				errFeature.setErrCode(OpenDMQAOptions.QAType.OUTBOUNDARY.getErrCode());
				errFeature.setErrType(OpenDMQAOptions.QAType.OUTBOUNDARY.getErrType(langType));
				errFeature.setErrName(OpenDMQAOptions.QAType.OUTBOUNDARY.getErrName(langType));
				errFeature.setErrPoint(errPt);
				return errFeature;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	// 노드오류 (NodeMiss)
	public List<ErrorFeature> validateNodeMiss(DTFeature feature, SimpleFeatureCollection sfc, OpenDTLayer reLayer,
			OptionTolerance tolerance, OptionFigure figure, Geometry envelopBdr) {

		// tar 중심선
		// re 경계면
		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = true;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		}
		List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
		if (isTrue) {
			OptionFilter relationFilter = reLayer.getFilter();
			List<AttributeFilter> relationConditions = null;
			if (relationFilter != null) {
				relationConditions = relationFilter.getFilter();
			}
			Double value = tolerance.getValue();
			String conditon = tolerance.getCondition();
			Geometry geom = (Geometry) sf.getDefaultGeometry();

			String layerID = feature.getLayerID();

			String refLayerID = reLayer.getLayerID();
			Coordinate[] tCoors = geom.getCoordinates();
			int coorLength = tCoors.length;

			Coordinate tFirCoor = tCoors[0];
			Coordinate tLasCoor = tCoors[coorLength - 1];

			GeometryFactory factory = new GeometryFactory();
			Geometry firPt = factory.createPoint(tFirCoor);
			Geometry lasPt = factory.createPoint(tLasCoor);
			Geometry innerPt = null;
			int coorSize = geom.getNumPoints();
			if (coorSize > 2) {
				innerPt = geom.getInteriorPoint();
			} else {
				innerPt = geom.getCentroid();
			}
			if (firPt.equals(lasPt)) {
				return null;
			}
			boolean notInter = true;
			boolean firTrue = false;
			boolean lasTrue = false;
			boolean firInter = false;
			boolean lasInter = false;

			if (firPt.distance(envelopBdr) == 0) {
				firTrue = true;
			}
			if (lasPt.distance(envelopBdr) == 0) {
				lasTrue = true;
			}

			Geometry firPtBf = firPt.buffer(value);
			Geometry lasPtBf = lasPt.buffer(value);
			// 중심선 누락
			SimpleFeatureCollection relationSfc = reLayer.getSimpleFeatureCollection();
			SimpleFeatureIterator rIterator = relationSfc.features();
			while (rIterator.hasNext()) {
				SimpleFeature reSf = rIterator.next();
				if (FeatureFilter.filter(reSf, relationConditions)) {
					Geometry rGeom = (Geometry) reSf.getDefaultGeometry();
					if (rGeom == null) {
						continue;
					}
					String rGeomType = rGeom.getGeometryType();
					if (!rGeomType.equals("Polygon") && !rGeomType.equals("MultiPolygon")) {
						continue;
					}
					if (rGeom.intersects(innerPt)) {
						notInter = false;
						firInter = rGeom.intersects(firPt);
						lasInter = rGeom.intersects(lasPt);

						Geometry boundary = rGeom.getBoundary();
						if (boundary.intersects(firPtBf)) {
							firTrue = true;
						}
						if (boundary.intersects(lasPtBf)) {
							lasTrue = true;
						}
					}
				}
			}
			rIterator.close();
			if (notInter) {
				boolean figures = false;
				if (figure != null) {
					List<AttributeFigure> attrFigures = figure.getFigure();
					for (AttributeFigure attrFigure : attrFigures) {
						String key = attrFigure.getKey();
						double num = attrFigure.getNumber();
						String condition = attrFigure.getCondition();
						Object valueObj = sf.getAttribute(key);
						if (valueObj != null) {
							double valueDble = (double) valueObj;
							if (condition.equals("over")) {
								figures = valueDble > num;
							}
							if (condition.equals("under")) {
								figures = valueDble < num;
							}
							if (condition.equals("andover")) {
								figures = valueDble >= num;
							}
							if (condition.equals("andunder")) {
								figures = valueDble <= num;
							}
							if (condition.equals("equal")) {
								figures = valueDble == num;
							}
						}
					}
				} else {
					figures = true;
				}
				if (figures) {
					ErrorFeature errFeature = new ErrorFeature();
					errFeature.setLayerID(layerID);
					errFeature.setRefLayerId(refLayerID);
					errFeature.setErrCode(OpenDMQAOptions.QAType.NODEMISS.getErrCode());
					errFeature.setErrType(OpenDMQAOptions.QAType.NODEMISS.getErrType(langType));
					errFeature.setErrName(OpenDMQAOptions.QAType.NODEMISS.getErrName(langType));
					errFeature.setErrPoint(innerPt);
					errFeatures.add(errFeature);
					return errFeatures;
				}
			}
			if (firTrue && lasTrue) {
				return null;
			}
			if (!firTrue || !lasTrue) {
				String featureID = sf.getID();
				boolean firErr = true;
				boolean lasErr = true;
				SimpleFeatureIterator sfIter = sfc.features();
				while (sfIter.hasNext()) {
					SimpleFeature tmpSf = sfIter.next();
					if (featureID.equals(tmpSf.getID())) {
						continue;
					}
					Geometry selfGeom = (Geometry) tmpSf.getDefaultGeometry();
					if (selfGeom.intersects(firPtBf)) {
						if (!firTrue) {
							double distance = Math.abs(firPt.distance(selfGeom));
							if (conditon.equals("over")) {
								if (distance > value) {
									firErr = false;
								}
							} else if (conditon.equals("under")) {
								if (distance < value) {
									firErr = false;
								}
							}
							if (conditon.equals("andover")) {
								if (distance >= value) {
									firErr = false;
								}
							} else if (conditon.equals("andunder")) {
								if (distance <= value) {
									firErr = false;
								}
							} else if (conditon.equals("equal")) {
								if (distance == value) {
									firErr = false;
								}
							}
						}
					}
					if (selfGeom.intersects(lasPtBf)) {
						if (!lasTrue) {
							double distance = Math.abs(lasPt.distance(selfGeom));
							if (conditon.equals("over")) {
								if (distance > value) {
									lasErr = false;
								}
							} else if (conditon.equals("under")) {
								if (distance < value) {
									lasErr = false;
								}
							}
							if (conditon.equals("andover")) {
								if (distance >= value) {
									lasErr = false;
								}
							} else if (conditon.equals("andunder")) {
								if (distance <= value) {
									lasErr = false;
								}
							} else if (conditon.equals("equal")) {
								if (distance == value) {
									lasErr = false;
								}
							}
						}
					}
				}
				sfIter.close();
				if (!firTrue && firErr && firInter) {
					ErrorFeature errFeature = new ErrorFeature();
					errFeature.setLayerID(layerID);
					errFeature.setRefLayerId(refLayerID);
					errFeature.setErrCode(OpenDMQAOptions.QAType.NODEMISS.getErrCode());
					errFeature.setErrType(OpenDMQAOptions.QAType.NODEMISS.getErrType(langType));
					errFeature.setErrName(OpenDMQAOptions.QAType.NODEMISS.getErrName(langType));
					errFeature.setErrPoint(firPt);
					errFeatures.add(errFeature);
				}
				if (!lasTrue && lasErr && lasInter) {
					ErrorFeature errFeature = new ErrorFeature();
					errFeature.setLayerID(layerID);
					errFeature.setRefLayerId(refLayerID);
					errFeature.setErrCode(OpenDMQAOptions.QAType.NODEMISS.getErrCode());
					errFeature.setErrType(OpenDMQAOptions.QAType.NODEMISS.getErrType(langType));
					errFeature.setErrName(OpenDMQAOptions.QAType.NODEMISS.getErrName(langType));
					errFeature.setErrPoint(lasPt);
					errFeatures.add(errFeature);
				}

				if (!firTrue && firErr && !firInter) {
					ErrorFeature errFeature = new ErrorFeature();
					errFeature.setLayerID(layerID);
					errFeature.setRefLayerId(refLayerID);
					errFeature.setErrCode(OpenDMQAOptions.QAType.NODEMISS.getErrCode());
					errFeature.setErrType(OpenDMQAOptions.QAType.NODEMISS.getErrType(langType));
					errFeature.setErrName(OpenDMQAOptions.QAType.NODEMISS.getErrName(langType));
					errFeature.setErrPoint(firPt);
					errFeatures.add(errFeature);
				}
				if (!lasTrue && lasErr && !lasInter) {
					ErrorFeature errFeature = new ErrorFeature();
					errFeature.setLayerID(layerID);
					errFeature.setRefLayerId(refLayerID);
					errFeature.setErrCode(OpenDMQAOptions.QAType.NODEMISS.getErrCode());
					errFeature.setErrType(OpenDMQAOptions.QAType.NODEMISS.getErrType(langType));
					errFeature.setErrName(OpenDMQAOptions.QAType.NODEMISS.getErrName(langType));
					errFeature.setErrPoint(lasPt);
					errFeatures.add(errFeature);
				}
			}
			if (errFeatures.size() > 0) {
				return errFeatures;
			} else {
				return null;
			}
		}
		return null;
	}

	// 계층오류 (LayerMiss)
	public ErrorFeature validateLayerFixMiss(DTFeature feature, String geomType) {

		// 객체타입 불일치
		SimpleFeature sf = feature.getSimefeature();
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		if (geom == null || geom.isEmpty()) {
			return null;
		}
		if (!geom.getGeometryType().toUpperCase().equals(geomType.toUpperCase())
				&& !geom.getGeometryType().toUpperCase().equals("MULTI" + geomType.toUpperCase())) {
			Geometry errPt = null;
			try {
				errPt = geom.getInteriorPoint();
			} catch (TopologyException e) {
				Coordinate[] coors = geom.getCoordinates();
				errPt = new GeometryFactory().createPoint(coors[0]);
			}
			String layerID = feature.getLayerID();
			ErrorFeature errFeature = new ErrorFeature();
			errFeature.setLayerID(layerID);
			errFeature.setErrCode(OpenDMQAOptions.QAType.LAYERMISS.getErrCode());
			errFeature.setErrType(OpenDMQAOptions.QAType.LAYERMISS.getErrType(langType));
			errFeature.setErrName(OpenDMQAOptions.QAType.LAYERMISS.getErrName(langType));
			errFeature.setErrPoint(errPt);
			return errFeature;
		} else {
			return null;
		}
	}

	// 기준점 초과오류 (OverShoot)
	public List<ErrorFeature> validateOverShoot(DTFeature feature, SimpleFeatureCollection sfc,
			OptionTolerance tolerance) {

		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = true;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		}
		if (isTrue) {
			double value = tolerance.getValue();
			List<ErrorFeature> errFeatures = new ArrayList<ErrorFeature>();
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			String layerID = feature.getLayerID();

			Coordinate[] coors = geom.getCoordinates();
			Coordinate firCoor = coors[0];
			Coordinate lasCoor = coors[coors.length - 1];

			GeometryFactory factory = new GeometryFactory();
			Geometry firPt = factory.createPoint(firCoor);
			Geometry lasPt = factory.createPoint(lasCoor);

			boolean firErr = false;
			boolean lasErr = false;
			boolean isNon = true;

			SimpleFeatureIterator firIter = sfc.features();
			while (firIter.hasNext()) {
				SimpleFeature reSf = firIter.next();
				boolean isReTrue = true;
				if (filters != null) {
					isReTrue = FeatureFilter.filter(reSf, filters);
				}
				if (!isReTrue || sf.getID().equals(reSf.getID())) {
					continue;
				}
				isNon = false;
				Geometry reGeom = (Geometry) reSf.getDefaultGeometry();

				Coordinate[] reCoors = reGeom.getCoordinates();
				Coordinate refirCoor = reCoors[0];
				Coordinate relasCoor = reCoors[reCoors.length - 1];
				Geometry refirPt = factory.createPoint(refirCoor);
				Geometry relasPt = factory.createPoint(relasCoor);

				if (firPt.intersects(reGeom)) {
					double refirDist = firPt.distance(refirPt);
					double relasDist = firPt.distance(relasPt);
					if (refirDist == 0 || relasDist == 0) {
						break;
					}
					if (refirDist > 0 && refirDist < value) {
						firErr = true;
					}
					if (relasDist > 0 && relasDist < value) {
						firErr = true;
					}
				}
			}
			firIter.close();
			SimpleFeatureIterator lasIter = sfc.features();
			while (lasIter.hasNext()) {
				SimpleFeature reSf = lasIter.next();
				boolean isReTrue = false;
				if (filters != null) {
					isReTrue = FeatureFilter.filter(reSf, filters);
				} else {
					isReTrue = true;
				}
				if (!isReTrue || sf.getID().equals(reSf.getID())) {
					continue;
				}
				isNon = false;
				Geometry reGeom = (Geometry) reSf.getDefaultGeometry();

				Coordinate[] reCoors = reGeom.getCoordinates();
				Coordinate refirCoor = reCoors[0];
				Coordinate relasCoor = reCoors[reCoors.length - 1];
				Geometry refirPt = factory.createPoint(refirCoor);
				Geometry relasPt = factory.createPoint(relasCoor);

				if (lasPt.intersects(reGeom)) {
					double refirDist = lasPt.distance(refirPt);
					double relasDist = lasPt.distance(relasPt);
					if (refirDist == 0 || relasDist == 0) {
						break;
					}
					if (refirDist > 0 && refirDist < value) {
						lasErr = true;
					}
					if (relasDist > 0 && relasDist < value) {
						lasErr = true;
					}
				}
			}
			if (isNon) {
				return null;
			}
			if (firErr) {
				ErrorFeature errFeature = new ErrorFeature();
				errFeature.setLayerID(layerID);
				errFeature.setErrCode(OpenDMQAOptions.QAType.OVERSHOOT.getErrCode());
				errFeature.setErrType(OpenDMQAOptions.QAType.OVERSHOOT.getErrType(langType));
				errFeature.setErrName(OpenDMQAOptions.QAType.OVERSHOOT.getErrName(langType));
				errFeature.setErrPoint(firPt);
				errFeatures.add(errFeature);
			}
			if (lasErr) {
				ErrorFeature errFeature = new ErrorFeature();
				errFeature.setLayerID(layerID);
				errFeature.setErrCode(OpenDMQAOptions.QAType.OVERSHOOT.getErrCode());
				errFeature.setErrType(OpenDMQAOptions.QAType.OVERSHOOT.getErrType(langType));
				errFeature.setErrName(OpenDMQAOptions.QAType.OVERSHOOT.getErrName(langType));
				errFeature.setErrPoint(lasPt);
				errFeatures.add(errFeature);
			}
			if (errFeatures.size() > 0) {
				return errFeatures;
			} else {
				return null;
			}
		}
		return null;
	}

	// 폴리곤 꼬임 오류 (InvalidPolygon)
	public List<ErrorFeature> validateEntityTwisted(DTFeature feature) {

		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = true;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		}
		if (isTrue) {
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			String layerID = feature.getLayerID();
			List<ErrorFeature> errList = new ArrayList<>();
			int geomNum = geom.getNumGeometries();
			for (int i = 0; i < geomNum; i++) {
				Geometry innerGeom = geom.getGeometryN(i);
				String geomType = innerGeom.getGeometryType();
				if (geomType.equals("LineString")) {
					if (!innerGeom.isSimple()) {
						List<ErrorFeature> lineErrList = validateEntityTwisted(innerGeom);
						if (lineErrList != null) {
							for (ErrorFeature err : lineErrList) {
								err.setLayerID(layerID);
								err.setRefLayerId(layerID);
								errList.add(err);
							}
						}
					}
				}
				if (geomType.equals("Polygon")) {
					if (!geom.isValid()) {
						Polygon polyg = (Polygon) innerGeom;
						LineString exteriorRing = polyg.getExteriorRing();
						List<ErrorFeature> exErrList = validateEntityTwisted(exteriorRing);
						if (exErrList != null) {
							for (ErrorFeature exErr : exErrList) {
								exErr.setLayerID(layerID);
								exErr.setRefLayerId(layerID);
								errList.add(exErr);
							}
						}

						int interiorRingNum = polyg.getNumInteriorRing();
						if (interiorRingNum > 0) {
							for (int r = 0; r < interiorRingNum; r++) {
								LineString interiorRing = polyg.getInteriorRingN(r);
								List<ErrorFeature> inErrList = validateEntityTwisted(interiorRing);
								if (inErrList != null) {
									for (ErrorFeature inErr : inErrList) {
										inErr.setLayerID(layerID);
										inErr.setRefLayerId(layerID);
										errList.add(inErr);
									}
								}
							}
						}
					}
				}
			}
			if (errList.size() > 0) {
				return errList;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}

	private List<ErrorFeature> validateEntityTwisted(Geometry geom) {

		GeometryFactory factory = new GeometryFactory();
		geom.normalize();

		Geometry errPt = null;
		List<ErrorFeature> errList = new ArrayList<>();
		Coordinate[] coordinates = geom.getCoordinates();
		for (int i = 0; i < coordinates.length - 1; i++) {
			Coordinate[] coordI = new Coordinate[] { new Coordinate(coordinates[i]),
					new Coordinate(coordinates[i + 1]) };
			LineString lineI = factory.createLineString(coordI);
			for (int j = i + 1; j < coordinates.length - 1; j++) {
				Coordinate[] coordJ = new Coordinate[] { new Coordinate(coordinates[j]),
						new Coordinate(coordinates[j + 1]) };
				LineString lineJ = factory.createLineString(coordJ);
				if (lineI.equals(lineJ)) {
					continue;
				}
				if (coordinates[i + 1].equals(coordinates[j])) {
					continue;
				}
				if (coordinates[i].equals(coordinates[j + 1])) {
					continue;
				}
				if (lineI.intersects(lineJ)) {
					try {
						errPt = lineI.intersection(lineJ);
					} catch (TopologyException e) {
						Coordinate[] coors = geom.getCoordinates();
						errPt = new GeometryFactory().createPoint(coors[0]);
					}
					ErrorFeature errFeature = new ErrorFeature();
					errFeature.setErrCode(OpenDMQAOptions.QAType.ENTITYTWISTED.getErrCode());
					errFeature.setErrType(OpenDMQAOptions.QAType.ENTITYTWISTED.getErrType(langType));
					errFeature.setErrName(OpenDMQAOptions.QAType.ENTITYTWISTED.getErrName(langType));
					errFeature.setErrPoint(errPt);
					errList.add(errFeature);
				}
			}
		}
		if (errList.size() > 0) {
			return errList;
		} else {
			return null;
		}
	}

	public List<ErrorFeature> validateRefEntityNone(DTFeature feature, SimpleFeatureCollection sfc,
			OptionTolerance tolerance, Geometry envelop) {
		SimpleFeature sf = feature.getSimefeature();

		boolean isTrue = true;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		}
		if (isTrue) {
			double value = tolerance.getValue();
			List<ErrorFeature> errFeatures = new ArrayList<>();
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			String layerID = feature.getLayerID();

			boolean firErr = false;
			boolean lasErr = false;

			boolean isNon = true;

			boolean firEvInter = false;
			boolean lasEvInter = false;

			boolean firInter = false;
			boolean lasInter = false;

			Coordinate[] coors = geom.getCoordinates();
			Coordinate firCoor = coors[0];
			Coordinate lasCoor = coors[coors.length - 1];

			GeometryFactory factory = new GeometryFactory();
			Geometry firPt = factory.createPoint(firCoor);
			Geometry lasPt = factory.createPoint(lasCoor);

			Geometry envelopeBdr = envelop.getBoundary();
			if (firPt.distance(envelopeBdr) == 0) {
				firEvInter = true;
			}
			if (lasPt.distance(envelopeBdr) == 0) {
				lasEvInter = true;
			}

			if (!firEvInter) {
				Geometry firPtMinBf = firPt.buffer(0.1);
				Geometry firPtMaxBf = firPt.buffer(value);
				SimpleFeatureIterator firIter = sfc.features();
				List<SimpleFeature> firNonIterSfs = new ArrayList<>();
				while (firIter.hasNext()) {
					SimpleFeature reSf = firIter.next();
					boolean isReTrue = true;
					if (filters != null) {
						isReTrue = FeatureFilter.filter(reSf, filters);
					}
					if (!isReTrue || sf.getID().equals(reSf.getID())) {
						continue;
					}
					isNon = false;
					Geometry reGeom = (Geometry) reSf.getDefaultGeometry();
					if (firPtMinBf.intersects(reGeom)) {
						firInter = true;
						break;
					} else {
						firNonIterSfs.add(reSf);
					}
				}
				firIter.close();
				if (!firInter) {
					for (SimpleFeature nonInterSf : firNonIterSfs) {
						Geometry reGeom = (Geometry) nonInterSf.getDefaultGeometry();
						if (firPtMaxBf.intersects(reGeom) && !firPtMaxBf.intersects(lasPt)) {
							Coordinate[] reCoors = reGeom.getCoordinates();
							Coordinate refirCoor = reCoors[0];
							Coordinate relasCoor = reCoors[reCoors.length - 1];
							Geometry refirPt = factory.createPoint(refirCoor);
							Geometry relasPt = factory.createPoint(relasCoor);
							double refirDist = firPt.distance(refirPt);
							double relasDist = firPt.distance(relasPt);
							if (refirDist > 0.1 && refirDist < value) {
								firErr = true;
							}
							if (relasDist > 0.1 && relasDist < value) {
								firErr = true;
							}
						}
					}
				}
			}

			if (!lasEvInter) {
				Geometry lasPtMinBf = lasPt.buffer(0.1);
				Geometry lasPtMaxBf = lasPt.buffer(value);

				SimpleFeatureIterator lasIter = sfc.features();
				List<SimpleFeature> lasNonIterSfs = new ArrayList<>();
				while (lasIter.hasNext()) {
					SimpleFeature reSf = lasIter.next();
					boolean isReTrue = true;
					if (filters != null) {
						isReTrue = FeatureFilter.filter(reSf, filters);
					}
					if (!isReTrue || sf.getID().equals(reSf.getID())) {
						continue;
					}
					isNon = false;
					Geometry reGeom = (Geometry) reSf.getDefaultGeometry();
					if (lasPtMinBf.intersects(reGeom)) {
						lasInter = true;
						break;
					} else {
						lasNonIterSfs.add(reSf);
					}
				}
				lasIter.close();
				if (!lasInter) {
					for (SimpleFeature nonInterSf : lasNonIterSfs) {
						Geometry reGeom = (Geometry) nonInterSf.getDefaultGeometry();
						if (lasPtMaxBf.intersects(reGeom) && !lasPtMaxBf.intersects(firPt)) {
							Coordinate[] reCoors = reGeom.getCoordinates();
							Coordinate refirCoor = reCoors[0];
							Coordinate relasCoor = reCoors[reCoors.length - 1];
							Geometry refirPt = factory.createPoint(refirCoor);
							Geometry relasPt = factory.createPoint(relasCoor);
							double refirDist = lasPt.distance(refirPt);
							double relasDist = lasPt.distance(relasPt);
							if (refirDist > 0.1 && refirDist < value) {
								lasErr = true;
							}
							if (relasDist > 0.1 && relasDist < value) {
								lasErr = true;
							}
						}
					}
				}
			}

			if (isNon) {
				return null;
			}
			if (firErr) {
				ErrorFeature errFeature = new ErrorFeature();
				errFeature.setLayerID(layerID);
				errFeature.setErrCode(OpenDMQAOptions.QAType.REFENTITYNONE.getErrCode());
				errFeature.setErrType(OpenDMQAOptions.QAType.REFENTITYNONE.getErrType(langType));
				errFeature.setErrName(OpenDMQAOptions.QAType.REFENTITYNONE.getErrName(langType));
				errFeature.setErrPoint(firPt);
				errFeatures.add(errFeature);
			}
			if (lasErr) {
				ErrorFeature errFeature = new ErrorFeature();
				errFeature.setLayerID(layerID);
				errFeature.setErrCode(OpenDMQAOptions.QAType.REFENTITYNONE.getErrCode());
				errFeature.setErrType(OpenDMQAOptions.QAType.REFENTITYNONE.getErrType(langType));
				errFeature.setErrName(OpenDMQAOptions.QAType.REFENTITYNONE.getErrName(langType));
				errFeature.setErrPoint(lasPt);
				errFeatures.add(errFeature);
			}
			if (errFeatures.size() > 0) {
				return errFeatures;
			} else {
				return null;
			}
		}
		return null;
	}

}
