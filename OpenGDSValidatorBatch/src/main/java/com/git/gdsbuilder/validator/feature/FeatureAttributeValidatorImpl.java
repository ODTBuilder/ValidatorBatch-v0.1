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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.geotools.data.DataUtilities;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.git.gdsbuilder.type.dt.feature.DTFeature;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.validate.error.ErrorFeature;
import com.git.gdsbuilder.type.validate.option.en.LangType;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFigure;
import com.git.gdsbuilder.type.validate.option.specific.AttributeFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.standard.FixedValue;
import com.git.gdsbuilder.type.validate.option.type.DMQAOptions;
import com.git.gdsbuilder.type.validate.option.type.FTMQAOptions;
import com.git.gdsbuilder.type.validate.option.type.LayerFieldOptions;
import com.git.gdsbuilder.type.validate.option.type.UFMQAOptions;
import com.git.gdsbuilder.validator.feature.filter.FeatureFilter;
import com.vividsolutions.jts.algorithm.Angle;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;

public class FeatureAttributeValidatorImpl implements FeatureAttributeValidator {

	LangType langType;

	public FeatureAttributeValidatorImpl(LangType langType) {
		this.langType = langType;
	}

	@Override
	public ErrorFeature validateZvalueAmbiguous(DTFeature feature, OptionFigure figure) {

		// attributeKey {"elevation":["0"]}
		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		String featureID = sf.getID();
		if (isTrue) {
			List<AttributeFigure> attrFigures = figure.getFigure();
			for (AttributeFigure attrFigure : attrFigures) {
				String key = attrFigure.getKey();
				Object attributeValue = sf.getAttribute(key);
				if (attributeValue != null) {
					Double number = attrFigure.getNumber();
					String condition = attrFigure.getCondition();
					Double interval = attrFigure.getInterval();
					String valueStr = attributeValue.toString();
					Double valueD = Double.valueOf(valueStr);
					if (condition != null) {
						if (condition.equals("equal")) {
							if (!attributeValue.toString().equals(number.toString())
									|| !(valueStr + ".0").equals(number.toString())) {
								isError = true;
							}
						}
						if (condition.equals("over")) {
							if (valueD < number) {
								isError = true;
							}
						}
						if (condition.equals("under")) {
							if (valueD > number) {
								isError = true;
							}
						}
					}
					if (interval != null) {
						Double result = valueD % interval;
						if (!(result == 0.0)) {
							isError = true;
						}
					}
				}
			}
		}
		if (isError) {
			ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.ZVALUEAMBIGUOUS.getErrCode(),
					DMQAOptions.Type.ZVALUEAMBIGUOUS.getErrTypeE(),
					DMQAOptions.Type.ZVALUEAMBIGUOUS.getErrName(langType), "", geometry.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateBridgeNameMiss(DTFeature feature, DTFeature reFeature, OptionFigure figure,
			OptionFigure reFigure) {

		boolean isTrue = false;

		SimpleFeature sf = feature.getSimefeature();
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		SimpleFeature relaionSf = reFeature.getSimefeature();
		List<AttributeFilter> refilters = feature.getFilter();
		if (refilters != null) {
			isTrue = FeatureFilter.filter(sf, refilters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		Geometry relGeometry = (Geometry) relaionSf.getDefaultGeometry();
		if (isTrue) {
			List<AttributeFigure> attrFigures = figure.getFigure();
			if (geometry.intersects(relGeometry) || geometry.crosses(relGeometry) || geometry.overlaps(relGeometry)) {
				for (AttributeFigure attrFigure : attrFigures) {
					String key = attrFigure.getKey();
					// A007
					Object simValue = sf.getAttribute(key);
					List<AttributeFigure> attrReFigures = reFigure.getFigure();
					for (AttributeFigure attrReFigure : attrReFigures) {
						String reKey = attrReFigure.getKey();
						// E002
						Object relValue = relaionSf.getAttribute(reKey);
						if (simValue != null && relValue != null) {
							String simValueStr = (String) simValue;
							String relValueStr = (String) relValue;
							if (simValueStr.equals("") || relValueStr.equals("")) {
								isError = true;
							} else {
								if (!(simValueStr.equals(relValueStr))) {
									isError = true;
								}
							}
						}
					}
				}
			}
		}
		if (isError) {
			Geometry returnGeom = geometry.intersection(relGeometry);
			String featureID = sf.getID();
			ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.BRIDGENAME.getErrCode(),
					DMQAOptions.Type.BRIDGENAME.getErrTypeE(), DMQAOptions.Type.BRIDGENAME.getErrName(langType), "",
					returnGeom.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateAdminBoundaryMiss(DTFeature feature, OptionFigure figure) {

		boolean isTrue = false;
		SimpleFeature sf = feature.getSimefeature();
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		boolean isError = false;
		Geometry geometry = (Geometry) sf.getDefaultGeometry();
		String featureID = sf.getID();
		if (isTrue) {
			List<AttributeFigure> attrFigures = figure.getFigure();
			AttributeFigure nameFigure = attrFigures.get(0);
			String nameKey = nameFigure.getKey();
			AttributeFigure divFigure = attrFigures.get(1);
			String divKey = divFigure.getKey();

			Object nameObj = sf.getAttribute(nameKey);
			Object divObj = sf.getAttribute(divKey);

			if (nameObj == null || divObj == null) {
				isError = true;
			} else {
				String title = nameObj.toString();
				int titleLength = title.length();
				String division = divObj.toString();
				int divisionLength = division.length();
				if (titleLength < 2) {
					isError = true;
				} else {
					if (titleLength > divisionLength) {
						int length = titleLength - divisionLength;
						String title_end = title.substring(length);
						if (!(title_end.equals(division))) {
							isError = true;
						}
					}
				}
			}
		}
		if (isError) {
			ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.ADMINMISS.getErrCode(),
					DMQAOptions.Type.ADMINMISS.getErrTypeE(), DMQAOptions.Type.ADMINMISS.getErrName(langType), "",
					geometry.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	/**
	 * @since 2018. 3. 6.
	 * @author DY.Oh
	 * @param DTFeature
	 * @param fiedMap
	 * @return ErrorFeature
	 */
	@Override
	public ErrorFeature validateLayerFixMiss(DTFeature DTFeature, List<FixedValue> fixArry) {

		boolean isErr = false;
		SimpleFeature sf = DTFeature.getSimefeature();

		for (FixedValue fix : fixArry) {
			// fixed
			String name = fix.getName();
			String type = fix.getType();
			String length = fix.getLength();
			List<Object> valuesObj = fix.getValues();
			Boolean flag = fix.isIsnull();

			// attr value
			Object attrObj = sf.getAttribute(name);
			if (flag != null) {
				if (flag == false) {
					if (attrObj == null) {
						isErr = true;
						break;
					} else if (attrObj.toString().equals("")) {
						isErr = true;
						break;
					}
				} else {
					return null;
				}
			}
			String valueType = attrObj.getClass().getSimpleName();
			if (type != null) {
				if (valueType.equals("Long")) {
					if (!type.equals("INTEGER") && !type.equals("NUMBER")) {
						isErr = true;
						break;
					}
				}
				if (valueType.equals("String")) {
					if (!type.startsWith("VARCHAR")) {
						isErr = true;
						break;
					}
				}
				// if (!type.equals(valueType)) {
				// isErr = true;
				// break;
				// }
			}
			if (valuesObj != null) {
				if (valuesObj != null) {
					boolean isTrueValue = false;
					for (Object value : valuesObj) {
						if (value == null) {
							String attrObjStr = attrObj.toString();
							if (attrObjStr.equals("")) {
								isTrueValue = true;
							}
						} else {
							String valueStr = value.toString();
							String attrStr = attrObj.toString();
							if (valueStr.equals(attrStr)) {
								isTrueValue = true;
								if (length != null) {
									if (attrStr.length() != Long.valueOf(length)) {
										isErr = true;
										break;
									}
								}
							}
						}
					}
					if (!isTrueValue) {
						isErr = true;
						break;
					}
				}
			}
		}
		if (isErr) {
			// error
			String featureID = sf.getID();
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			if (geom.isEmpty()) {
				return null;
			}

			ErrorFeature errorFeature = new ErrorFeature(featureID,
					LayerFieldOptions.Type.LAYERFIELDFIXMISS.getErrCode(),
					LayerFieldOptions.Type.LAYERFIELDFIXMISS.getErrTypeE(),
					LayerFieldOptions.Type.LAYERFIELDFIXMISS.getErrName(langType), "", geom.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateUFIDMiss(DTFeature feature, String collectionName, String layerName,
			OptionFigure figure, DTLayer relationLayer) {

		boolean isTrue = false;
		SimpleFeature sf = feature.getSimefeature();
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}

		String featureID = sf.getID();
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		String comment = "";
		boolean isError = false;
		if (!isTrue) {
			return null;
		}
		List<AttributeFigure> attrFigures = figure.getFigure();
		for (AttributeFigure attrFigure : attrFigures) {
			String key = attrFigure.getKey();
			Object ufidObj = sf.getAttribute(key);
			if (ufidObj == null) {
				isError = true;
				break;
			}
			String ufid = (String) ufidObj;
			// 중복 검사
			if (relationLayer != null) {
				OptionFilter reFilter = relationLayer.getFilter();
				List<AttributeFilter> reAttrFilters = null;
				if (reFilter != null) {
					reAttrFilters = feature.getFilter();
				}
				SimpleFeatureCollection relationSfc = relationLayer.getSimpleFeatureCollection();
				SimpleFeatureIterator iterator = relationSfc.features();
				outer: while (iterator.hasNext()) {
					SimpleFeature relationSf = iterator.next();
					if (reAttrFilters != null) {
						isTrue = FeatureFilter.filter(relationSf, reAttrFilters);
					} else {
						isTrue = true;
					}
					if (isTrue) {
						String relationSfID = relationSf.getID();
						if (featureID.equals(relationSfID)) {
							continue;
						} else {
							Object reUfidObj = relationSf.getAttribute("UFID");
							if (reUfidObj == null) {
								continue;
							} else {
								String reUfid = reUfidObj.toString();
								String uniqueUfid = ufid.substring(18, 34);
								String uniqueReUfid = reUfid.substring(18, 34);
								if (uniqueUfid.equals(uniqueReUfid)) {
									isError = true;
									comment += "UFID duplication";
									String relayerId = relationLayer.getLayerID();
									ErrorFeature errorFeature = new ErrorFeature(featureID, relayerId, relationSfID,
											DMQAOptions.Type.UFIDMISS.getErrCode(),
											DMQAOptions.Type.UFIDMISS.getErrTypeE(),
											DMQAOptions.Type.UFIDMISS.getErrName(langType), comment,
											geom.getInteriorPoint());
									return errorFeature;
								}
							}
						}
					}
				}
				iterator.close();
			}
			// 규칙 검사
			if (!isError) {
				// 길이
				int ufidLength = ufid.length();
				if (ufidLength != 34) {
					comment += "UFID regulations miss";
					isError = true;
				} else {
					// 규칙
					String agencyCode = "1000"; // 관리기관 코드 4자리 (지리원 1000)
					String to4 = ufid.substring(0, 4);
					if (!to4.equals(agencyCode)) {
						isError = true;
						comment += "UFID regulations miss";
					}
					String to13 = ufid.substring(5, 13); // 도엽코드 9자리
					if (!to13.equals(collectionName)) {
						isError = true;
						comment += "UFID regulations miss";
					}
					String layerCode = layerName.substring(0, 4);
					String to17 = ufid.substring(13, 17); // 레이어 코드 4자리
					if (!to17.equals(layerCode)) {
						isError = true;
						comment += "UFID regulations miss";
					}
					String field = ufid.substring(17, 18); // 결정조건 코드 1자리
					String errorField = ufid.substring(33, 34); // parity 코드
				}
			}
		}
		if (isError) {
			ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.UFIDMISS.getErrCode(),
					DMQAOptions.Type.UFIDMISS.getErrTypeE(), DMQAOptions.Type.UFIDMISS.getErrName(langType), comment,
					geom.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateNumericalValue(DTFeature feature, OptionFigure figure) {

		boolean isTrue = false;
		SimpleFeature sf = feature.getSimefeature();
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		String featureID = sf.getID();
		Geometry geometry = (Geometry) sf.getDefaultGeometry();

		boolean isError = false;
		if (isTrue) {
			List<AttributeFigure> attrFigures = figure.getFigure();
			for (AttributeFigure attrFigure : attrFigures) {
				String key = attrFigure.getKey();
				String condition = attrFigure.getCondition();
				Double number = attrFigure.getNumber();

				Object attributeObj = sf.getAttribute(key);
				if (attributeObj != null) {
					String attributeStr = attributeObj.toString();
					if (attributeStr.equals("")) {
						isError = true;
						break;
					}
					Double attributeDou = Double.parseDouble(attributeStr);
					if (condition.equals("over")) {
						if (attributeDou > number) {
							isError = true;
							break;
						}
					} else if (condition.equals("under")) {
						if (attributeDou < number) {
							isError = true;
							break;
						}
					} else if (condition.equals("equal")) {
						if (attributeDou != number) {
							if (attributeDou < number) {
								isError = true;
								break;
							}
						}
					}
				}
			}
		}
		if (isError) {
			ErrorFeature errorFeature = new ErrorFeature(featureID, DMQAOptions.Type.NUMERICALVALUE.getErrCode(),
					DMQAOptions.Type.NUMERICALVALUE.getErrTypeE(), DMQAOptions.Type.NUMERICALVALUE.getErrName(langType),
					"", geometry.getInteriorPoint());
			return errorFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateEntityDuplicated(DTFeature featureI, DTFeature featureJ) {

		SimpleFeature sfI = featureI.getSimefeature();
		SimpleFeature sfJ = featureJ.getSimefeature();

		List attrIList = sfI.getAttributes();
		List attrJList = sfJ.getAttributes();

		int equalsCount = 0;
		for (int i = 1; i < attrIList.size(); i++) {
			Object attrI = attrIList.get(i);
			breakOut: for (int j = 1; j < attrJList.size(); j++) {
				Object attrJ = attrJList.get(j);
				if (attrI != null && attrJ != null) {
					if (attrI.toString().equals(attrJ.toString())) {
						equalsCount++;
						break breakOut;
					}
				} else if (attrI == null) {
					if (attrJ == null) {
						equalsCount++;
						break breakOut;
					}
				}
			}
		}
		if (equalsCount == attrIList.size() - 1) {
			String featureID = sfI.getID();
			Geometry geometryI = (Geometry) sfI.getDefaultGeometry();
			ErrorFeature errFeature = new ErrorFeature(featureID, DMQAOptions.Type.ENTITYDUPLICATED.getErrCode(),
					DMQAOptions.Type.ENTITYDUPLICATED.getErrTypeE(),
					DMQAOptions.Type.ENTITYDUPLICATED.getErrName(langType), "", geometryI.getInteriorPoint());

			return errFeature;
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateUAvrgDPH20(DTFeature feature, List<AttributeFigure> attrFigures, DTLayer relationLayer,
			List<AttributeFigure> reAttrFigures) {

		SimpleFeature sf = feature.getSimefeature();
		Geometry geom = (Geometry) sf.getDefaultGeometry();

		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		if (isTrue) {
			boolean isIntersected = false;
			double total = 0.0;
			int count = 0;
			// get relation
			OptionFilter relationFilter = relationLayer.getFilter();
			List<AttributeFilter> relationConditions = null;
			if (relationFilter != null) {
				relationConditions = relationFilter.getFilter();
			}
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
			Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(geom.getEnvelope()));
			SimpleFeatureSource source = DataUtilities.source(relationLayer.getSimpleFeatureCollection());
			SimpleFeatureCollection filterSfc;
			SimpleFeatureIterator iter = null;
			try {
				filterSfc = source.getFeatures(filter);
				iter = filterSfc.features();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (iter.hasNext()) {
				SimpleFeature relationSf = iter.next();
				if (FeatureFilter.filter(relationSf, relationConditions)) {
					Geometry relationGeom = (Geometry) relationSf.getDefaultGeometry();
					if (geom.intersects(relationGeom)) {
						for (AttributeFigure reAttrFigure : reAttrFigures) {
							double relationValue = (double) relationSf.getAttribute(reAttrFigure.getKey());
							total += relationValue;
							count++;
						}
						isIntersected = true;
					}
				}
			}
			if (isIntersected) {
				double lineAvrg = 0;

				for (AttributeFigure attrFigure : attrFigures) {
					lineAvrg += (double) sf.getAttribute(attrFigure.getKey());
				}

				lineAvrg = Math.round((lineAvrg / attrFigures.size()) * 10) / 10.0;

				double result = Math.round((total / count) * 10) / 10.0;

				if (lineAvrg != result) {
					isError = true;
				}
			}
		}
		if (isError) {
			String featureID = sf.getID();
			return new ErrorFeature(featureID, UFMQAOptions.Type.UAVRGDPH20.getErrCode(),
					UFMQAOptions.Type.UAVRGDPH20.getErrTypeE(), UFMQAOptions.Type.UAVRGDPH20.getErrName(langType), "",
					geom.getInteriorPoint());
		} else {
			return null;
		}
	}

	@Override
	public ErrorFeature validateUSymbolDirection(DTFeature feature, List<AttributeFigure> attrFigures,
			DTFeature reFeature) {

		// symbol
		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		boolean isErr = false;
		Geometry symbol = (Geometry) sf.getDefaultGeometry();
		if (isTrue) {
			// line
			SimpleFeature reSf = reFeature.getSimefeature();
			Geometry line = (Geometry) reSf.getDefaultGeometry();
			Coordinate[] lineCoors = line.getCoordinates();
			Coordinate symbolCoor = symbol.getCoordinate();
			Coordinate firCoor = lineCoors[0];
			if (symbolCoor.equals(firCoor)) {
				Coordinate secCoor = lineCoors[1];
				double tmpX = secCoor.x;
				double tmpY = firCoor.y;
				Coordinate tmpCoor = new Coordinate(tmpX, tmpY);
				double tmpRadians = Angle.angleBetween(tmpCoor, firCoor, secCoor);
				int lineDegree = (int) Math.round(Angle.toDegrees(tmpRadians));
				int revertDegree = 360 - lineDegree;

				String degreeKey;
				for (AttributeFigure attrFigure : attrFigures) {
					degreeKey = attrFigure.getKey();
					int symbolDegree = (int) sf.getAttribute(degreeKey);
					if (lineDegree != symbolDegree || revertDegree != symbolDegree) {
						isErr = true;
					}
				}
			}
		}
		if (isErr) {
			String featureID = sf.getID();
			ErrorFeature errorFeature = new ErrorFeature(featureID, UFMQAOptions.Type.SYMBOLDIRECTION.getErrCode(),
					UFMQAOptions.Type.SYMBOLDIRECTION.getErrTypeE(),
					UFMQAOptions.Type.SYMBOLDIRECTION.getErrName(langType), "", symbol);
			return errorFeature;
		} else {
			return null;
		}
	}

	/**
	 * @since 2018. 3. 21.
	 * @author DY.Oh
	 * @param feature
	 * @return ErrorFeature
	 */
	@Override
	public ErrorFeature validateFcodeLogicalAttribute(DTFeature feature, OptionFigure figure) {

		boolean isError = false;
		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (isTrue) {
			List<AttributeFigure> attrFigures = figure.getFigure();
			AttributeFigure firAttrFigure = attrFigures.get(0);
			String firKey = firAttrFigure.getKey();
			Object firValue = sf.getAttribute(firKey);
			if (firValue != null) {
				String firValueStr = firValue.toString();
				String othersValueStr = "J";
				for (int i = 1; i < attrFigures.size(); i++) {
					String otherKey = attrFigures.get(i).getKey();
					Object otherValue = sf.getAttribute(otherKey);
					if (otherValue == null) {
						break;
					}
					String otherValueStr = otherValue.toString();
					othersValueStr += otherValueStr;
				}
				if (!firValueStr.equals(othersValueStr)) {
					isError = true;
				}
			}
		}
		if (isError) {
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			if (geom.isEmpty()) {
				return null;
			}
			ErrorFeature errFeature = new ErrorFeature(sf.getID(), FTMQAOptions.Type.FCODELOGICALATTRIBUTE.getErrCode(),
					FTMQAOptions.Type.FCODELOGICALATTRIBUTE.getErrTypeE(),
					FTMQAOptions.Type.FCODELOGICALATTRIBUTE.getErrName(langType), "", geom.getCentroid());
			return errFeature;
		} else {
			return null;
		}
	}

	/**
	 * @since 2018. 3. 21.
	 * @author DY.Oh
	 * @param feature
	 * @param figure
	 * @return ErrorFeature
	 */
	@Override
	public ErrorFeature validateFLabelLogicalAttribute(DTFeature feature, OptionFigure figure) {

		boolean isError = false;
		SimpleFeature sf = feature.getSimefeature();
		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();
		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		if (isTrue) {
			List<AttributeFigure> attrFigures = figure.getFigure();
			AttributeFigure firAttrFigure = attrFigures.get(0);
			String firKey = firAttrFigure.getKey();
			Object firValue = sf.getAttribute(firKey);

			if (firValue != null) {
				String firValueStr = firValue.toString();
				String othersValueStrF = "";
				String othersValueStrB = "";
				for (int i = 1; i < attrFigures.size(); i++) {
					String otherKey = attrFigures.get(i).getKey();
					Object sfValue = sf.getAttribute(otherKey);
					if (sfValue == null) {
						break;
					}
					String sfValueStr = sfValue.toString();
					List<Object> otherValues = attrFigures.get(i).getValues();
					if (otherValues != null) {
						for (Object otherValue : otherValues) {
							String otherValueStr = otherValue.toString();
							if (otherValueStr.startsWith(sfValueStr)) {
								othersValueStrF += otherValueStr.substring(otherValueStr.indexOf("=") + 1);
							}
						}
					} else {
						othersValueStrB += sfValueStr;
					}
				}
				String othersValueStr = "";
				if (othersValueStrB.equals("")) {
					othersValueStr = othersValueStrF;
				} else {
					othersValueStr = othersValueStrF + "-" + othersValueStrB;
				}
				if (!firValueStr.equals(othersValueStr)) {
					isError = true;
				}
			}
		}
		if (isError) {
			Geometry geom = (Geometry) sf.getDefaultGeometry();
			if (geom.isEmpty()) {
				return null;
			}
			ErrorFeature errFeature = new ErrorFeature(sf.getID(),
					FTMQAOptions.Type.FLABELLOGICALATTRIBUTE.getErrCode(),
					FTMQAOptions.Type.FLABELLOGICALATTRIBUTE.getErrTypeE(),
					FTMQAOptions.Type.FLABELLOGICALATTRIBUTE.getErrName(langType), "", geom.getCentroid());
			return errFeature;
		} else {
			return null;
		}
	}

	@Override
	public List<ErrorFeature> validateDissolve(DTFeature feature, SimpleFeatureCollection selfSfc,
			OptionFigure figure) {

		SimpleFeature sf = feature.getSimefeature();
		List<ErrorFeature> errFeatures = new ArrayList<>();

		boolean isTrue = false;
		List<AttributeFilter> filters = feature.getFilter();

		if (filters != null) {
			isTrue = FeatureFilter.filter(sf, filters);
		} else {
			isTrue = true;
		}
		boolean isError = false;
		Geometry geom = (Geometry) sf.getDefaultGeometry();
		if (isTrue) {
			FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
			Filter filter = ff.intersects(ff.property("the_geom"), ff.literal(geom));

			SimpleFeatureSource source = DataUtilities.source(selfSfc);
			SimpleFeatureCollection filterSfc;
			SimpleFeatureIterator iter;
			SimpleFeature selfSf;
			try {

				filterSfc = source.getFeatures(filter);
				iter = filterSfc.features();

				while (iter.hasNext()) {
					selfSf = iter.next();
					if (selfSf.equals(sf) || !FeatureFilter.filter(selfSf, filters)) {
						continue;
					}
					boolean isEqual = true;
					List<AttributeFigure> attrFigures = figure.getFigure();
					for (int i = 0; i < attrFigures.size(); i++) {
						String otherKey = attrFigures.get(i).getKey();
						if (selfSf.getAttribute(otherKey) == null || sf.getAttribute(otherKey) == null) {
							continue;
						}
						if (!selfSf.getAttribute(otherKey).equals(sf.getAttribute(otherKey))) {
							isEqual = false;
						}
					}
					if (isEqual) {
						isError = true;
						Geometry selfGeom = (Geometry) selfSf.getDefaultGeometry();
						errFeatures.add(new ErrorFeature(sf.getID(), FTMQAOptions.Type.DISSOLVE.getErrCode(),
								FTMQAOptions.Type.DISSOLVE.getErrTypeE(),
								FTMQAOptions.Type.DISSOLVE.getErrName(langType), feature.getLayerID(),
								selfGeom.getInteriorPoint()));
					}
				}
				iter.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (isError) {
			errFeatures.add(new ErrorFeature(sf.getID(), FTMQAOptions.Type.DISSOLVE.getErrCode(),
					FTMQAOptions.Type.DISSOLVE.getErrTypeE(), FTMQAOptions.Type.DISSOLVE.getErrName(langType), "",
					geom.getInteriorPoint()));
			return errFeatures;
		} else {
			return null;
		}
	}
}
