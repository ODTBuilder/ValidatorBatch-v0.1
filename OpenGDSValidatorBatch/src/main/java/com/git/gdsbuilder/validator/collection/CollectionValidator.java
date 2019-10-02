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
 *    OpenGDS_2018
 *    Lesser General Public License for more details.
 */

package com.git.gdsbuilder.validator.collection;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.geotools.feature.SchemaException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.git.batch.service.BathService;
import com.git.batch.step.Progress;
import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.type.dt.collection.DTLayerCollectionList;
import com.git.gdsbuilder.type.dt.collection.MapSystemRule;
import com.git.gdsbuilder.type.dt.collection.MapSystemRule.MapSystemRuleType;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.DTLayerList;
import com.git.gdsbuilder.type.validate.error.ErrorLayer;
import com.git.gdsbuilder.type.validate.layer.QALayerType;
import com.git.gdsbuilder.type.validate.layer.QALayerTypeList;
import com.git.gdsbuilder.type.validate.option.QAOption;
import com.git.gdsbuilder.type.validate.option.specific.AttributeMiss;
import com.git.gdsbuilder.type.validate.option.specific.CloseMiss;
import com.git.gdsbuilder.type.validate.option.specific.GraphicMiss;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionRelation;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.git.gdsbuilder.type.validate.option.standard.FixedValue;
import com.git.gdsbuilder.type.validate.option.standard.LayerFixMiss;
import com.git.gdsbuilder.validator.layer.LayerValidator;
import com.git.gdsbuilder.validator.layer.LayerValidatorImpl;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

/**
 * ValidateLayerCollectionList를 검수하는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 4. 18. 오후 3:30:17
 */
public class CollectionValidator {

	protected Progress pb;
	protected static double spatialAccuracyTolorence = 0.01; // 공간분석 정밀도 설정 (m)
	int geo_ = 0;
	int close_ = 0;
	int attr_ = 0;
	
	ErrorLayer errLayer;
	DTLayerCollection collection;
	DTLayerCollectionList closeCollections;
	QALayerTypeList types;
	Map<String, Object> progress;
	String collectionType;

	/**
	 * CollectionValidator 생성자
	 * 
	 * @param closeCollections
	 * 
	 * @param validateLayerCollectionList
	 * @throws NoSuchAuthorityCodeException
	 * @throws SchemaException
	 * @throws FactoryException
	 * @throws TransformException
	 * @throws IOException
	 */
	public CollectionValidator(DTLayerCollection collection, DTLayerCollectionList closeCollections,
			QALayerTypeList types)
			throws NoSuchAuthorityCodeException, SchemaException, FactoryException, TransformException, IOException {
		this.collection = collection;
		this.types = types;
		this.closeCollections = closeCollections;
		this.errLayer = new ErrorLayer();
		this.progress = new HashMap<String, Object>();
		collectionValidate();
		
	}

	public static double getSpatialAccuracyTolorence() {
		return spatialAccuracyTolorence;
	}

	public static void setSpatialAccuracyTolorence(double spatialAccuracyTolorence) {
		CollectionValidator.spatialAccuracyTolorence = spatialAccuracyTolorence;
	}

	public ErrorLayer getErrLayer() {
		return errLayer;
	}

	public void setErrLayer(ErrorLayer errLayer) {
		this.errLayer = errLayer;
	}

	public DTLayerCollection getCollection() {
		return collection;
	}

	public void setCollection(DTLayerCollection collection) {
		this.collection = collection;
	}

	public DTLayerCollectionList getCloseCollections() {
		return closeCollections;
	}

	public void setCloseCollections(DTLayerCollectionList closeCollections) {
		this.closeCollections = closeCollections;
	}

	public QALayerTypeList getTypes() {
		return types;
	}

	public void setTypes(QALayerTypeList types) {
		this.types = types;
	}

	public Map<String, Object> getProgress() {
		return progress;
	}

	public void setProgress(Map<String, Object> progress) {
		this.progress = progress;
	}

	public String getCollectionType() {
		return collectionType;
	}

	public void setCollectionType(String collectionType) {
		this.collectionType = collectionType;
	}

	public void collectionValidate() {

		DTLayerCollection collection = this.collection;
		QALayerTypeList types = this.types;

		try {
			this.errLayer = new ErrorLayer();
			errLayer.setCollectionName(collection.getCollectionName());
			errLayer.setCollectionType(this.collectionType);

			// layerMiss 검수
			layerMissValidate(types, collection);

			// geometric 검수
			geometricValidate(types, collection);

			// attribute 검수
			attributeValidate(types, collection);

			// 인접 검수
			if (closeCollections != null) {
				closeCollectionValidate(types, collection, closeCollections);
			}

			progress.put(collection.getCollectionName(), 2);
		} catch (Exception e) {
			e.printStackTrace();
			progress.put(collection.getCollectionName(), 3);
		}
	}

	private void closeCollectionValidate(QALayerTypeList types, DTLayerCollection collection,
			DTLayerCollectionList closeCollections) {

		DTLayer neatLine = collection.getNeatLine();
		MapSystemRule mapSystemRule = collection.getMapRule();
		Map<MapSystemRuleType, DTLayerCollection> closeMap = new HashMap<>();

		DTLayerCollection topGeoCollection = null;
		DTLayerCollection bottomGeoCollection = null;
		DTLayerCollection leftGeoCollection = null;
		DTLayerCollection rightGeoCollection = null;

		boolean isTrue = false;
		if (mapSystemRule.getTop() != null) {
			topGeoCollection = closeCollections.getLayerCollection(String.valueOf(mapSystemRule.getTop()));
			closeMap.put(MapSystemRuleType.TOP, topGeoCollection);
			isTrue = true;
		}
		if (mapSystemRule.getBottom() != null) {
			bottomGeoCollection = closeCollections.getLayerCollection(String.valueOf(mapSystemRule.getBottom()));
			closeMap.put(MapSystemRuleType.BOTTOM, bottomGeoCollection);
			isTrue = true;
		}
		if (mapSystemRule.getLeft() != null) {
			leftGeoCollection = closeCollections.getLayerCollection(String.valueOf(mapSystemRule.getLeft()));
			closeMap.put(MapSystemRuleType.LEFT, leftGeoCollection);
			isTrue = true;
		}
		if (mapSystemRule.getRight() != null) {
			rightGeoCollection = closeCollections.getLayerCollection(String.valueOf(mapSystemRule.getRight()));
			closeMap.put(MapSystemRuleType.RIGHT, rightGeoCollection);
			isTrue = true;
		}
		ExecutorService executorService = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("CollectionValidator.인접검수-%d").build());
		CloseCollectionResult closeCollectionResult = new CloseCollectionResult();

		class Task implements Runnable {

			CloseCollectionResult closeCollectionResult;
			List<CloseMiss> closeMisses;
			DTLayer typeLayer;
			Map<MapSystemRuleType, DTLayerCollection> closeMap;

			Task(CloseCollectionResult closeCollectionResult, List<CloseMiss> closeMiss, DTLayer typeLayer,
					Map<MapSystemRuleType, DTLayerCollection> closeMap) {

				this.closeCollectionResult = closeCollectionResult;
				this.closeMisses = closeMiss;
				this.typeLayer = typeLayer;
				this.closeMap = closeMap;
			}

			@Override
			public void run() {
				int cate = types.getCategory();
				ErrorLayer typeErrorLayer = null;
				String layerID = typeLayer.getLayerID();
				Iterator<?> iterator = closeMap.keySet().iterator();
				while (iterator.hasNext()) {
					MapSystemRuleType type = (MapSystemRuleType) iterator.next();
					if (type == null) {
						continue;
					}
					DTLayerCollection closeCollection = closeMap.get(type);
					if (closeCollection == null) {
						continue;
					}
					try {
						for (CloseMiss closeMisse : closeMisses) {
							// filter
							List<OptionFilter> filters = closeMisse.getFilter();
							if (filters != null) {
								for (OptionFilter filter : filters) {
									String code = filter.getCode();
									if (code.equals(layerID)) {
										typeLayer.setFilter(filter);
									}
								}
							}
							// figure
							List<OptionFigure> figures = closeMisse.getFigure();
							// tolerance
							List<OptionTolerance> tolerances = closeMisse.getTolerance();
							// option
							String option = closeMisse.getOption();
							if (option.equals("RefAttributeMiss")) {
								for (OptionFigure figure : figures) {
									String fCode = figure.getCode();
									if (fCode != null && !fCode.equals(layerID)) {
										continue;
									}
									for (OptionTolerance tolerance : tolerances) {
										String tCode = tolerance.getCode();
										DTLayer closeLayer = null;
										CloseLayerOp layerOp = new CloseLayerOp();
										if (cate == 1 || cate == 2) {
											if (tCode == null) {
												closeLayer = closeCollection.getLayer(layerID);
											} else {
												closeLayer = collection.getLayer(tCode);
											}
											if (closeLayer != null) {
												layerOp.closeLayerOp(type, neatLine, typeLayer, closeLayer, tolerance);
											}
										} else if (cate == 5 || cate == 6) {
											if (tCode == null) {
												closeLayer = closeCollection.getLayer(layerID);
											} else {
												closeLayer = collection.getLayer(tCode);
											}
											if (closeLayer != null) {
												layerOp.closeLayerOpF2(type, neatLine, typeLayer, closeLayer,
														tolerance);
											}
										}
										if (closeLayer != null) {
											DTLayer reFilterLayer = layerOp.getCloseLayer();
											DTLayer filterLayer = new DTLayer();
											filterLayer.setLayerID(typeLayer.getLayerID());
											filterLayer.setLayerType(typeLayer.getLayerType());
											filterLayer.setSimpleFeatureCollection(
													layerOp.getTypeLayer().getSimpleFeatureCollection());
											LayerValidator layerValidator = new LayerValidatorImpl(filterLayer);
											if (filters != null) {
												String closeLayerId = closeLayer.getLayerID();
												for (OptionFilter filter : filters) {
													String code = filter.getCode();
													if (code.equals(closeLayerId)) {
														closeLayer.setFilter(filter);
													}
												}
											}
											typeErrorLayer = layerValidator.validateRefAttributeMiss(reFilterLayer,
													layerOp.closeBoundary, figure, tolerance);
											if (typeErrorLayer != null) {
												closeCollectionResult.mergeErrorLayer(typeErrorLayer);
											}
										}
									}
								}
							}
							if (option.equals("FRefEntityNone")) {
								for (OptionTolerance tolerance : tolerances) {
									String tCode = tolerance.getCode();
									DTLayer closeLayer = null;
									CloseLayerOp layerOp = new CloseLayerOp();
									if (cate == 1 || cate == 2) {
										if (tCode == null) {
											closeLayer = closeCollection.getLayer(layerID);
										} else {
											closeLayer = collection.getLayer(tCode);
										}
										if (closeLayer != null) {
											layerOp.closeLayerOp(type, neatLine, typeLayer, closeLayer, tolerance);
										}
									} else if (cate == 5 || cate == 6) {
										if (tCode == null) {
											closeLayer = closeCollection.getLayer(layerID);
										} else {
											closeLayer = collection.getLayer(tCode);
										}
										if (closeLayer != null) {
											layerOp.closeLayerOpF2(type, neatLine, typeLayer, closeLayer, tolerance);
										}
									}
									if (closeLayer != null) {
										DTLayer reFilterLayer = layerOp.getCloseLayer();
										DTLayer filterLayer = new DTLayer();
										filterLayer.setLayerID(typeLayer.getLayerID());
										filterLayer.setLayerType(typeLayer.getLayerType());
										filterLayer.setSimpleFeatureCollection(
												layerOp.getTypeLayer().getSimpleFeatureCollection());
										LayerValidator layerValidator = new LayerValidatorImpl(filterLayer);
										if (filters != null) {
											String closeLayerId = closeLayer.getLayerID();
											for (OptionFilter filter : filters) {
												String code = filter.getCode();
												if (code.equals(closeLayerId)) {
													closeLayer.setFilter(filter);
												}
											}
										}
										typeErrorLayer = layerValidator.validateFRefEntityNone(reFilterLayer,
												layerOp.closeBoundary, tolerance);
										if (typeErrorLayer != null) {
											closeCollectionResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
							if (option.equals("DRefEntityNone")) {
								for (OptionTolerance tolerance : tolerances) {
									String tCode = tolerance.getCode();
									DTLayer closeLayer = null;
									CloseLayerOp layerOp = new CloseLayerOp();
									if (cate == 1 || cate == 2) {
										if (tCode == null) {
											closeLayer = closeCollection.getLayer(layerID);
										} else {
											closeLayer = collection.getLayer(tCode);
										}
										if (closeLayer != null) {
											layerOp.closeLayerOp(type, neatLine, typeLayer, closeLayer, tolerance);
										}
									} else if (cate == 5 || cate == 6) {
										if (tCode == null) {
											closeLayer = closeCollection.getLayer(layerID);
										} else {
											closeLayer = collection.getLayer(tCode);
										}
										if (closeLayer != null) {
											layerOp.closeLayerOpF2(type, neatLine, typeLayer, closeLayer, tolerance);
										}
									}
									if (closeLayer != null) {
										DTLayer reFilterLayer = layerOp.getCloseLayer();
										DTLayer filterLayer = new DTLayer();
										filterLayer.setLayerID(typeLayer.getLayerID());
										filterLayer.setLayerType(typeLayer.getLayerType());
										filterLayer.setSimpleFeatureCollection(
												layerOp.getTypeLayer().getSimpleFeatureCollection());
										LayerValidator layerValidator = new LayerValidatorImpl(filterLayer);
										if (filters != null) {
											String closeLayerId = closeLayer.getLayerID();
											for (OptionFilter filter : filters) {
												String code = filter.getCode();
												if (code.equals(closeLayerId)) {
													closeLayer.setFilter(filter);
												}
											}
										}
										typeErrorLayer = layerValidator.validateDRefEntityNone(reFilterLayer,
												layerOp.closeBoundary, tolerance);
										if (typeErrorLayer != null) {
											closeCollectionResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
							if (option.equals("RefZValueMiss")) {
								for (OptionFigure figure : figures) {
									String fCode = figure.getCode();
									for (OptionTolerance tolerance : tolerances) {
										String tCode = tolerance.getCode();
										if (fCode != null && !fCode.equals(layerID)) {
											continue;
										}
										if (tCode != null && !tCode.equals(layerID)) {
											continue;
										}
										DTLayer closeLayer = closeCollection.getLayer(fCode);
										if (closeLayer != null) {
											CloseLayerOp layerOp = new CloseLayerOp();
											if (cate == 1 || cate == 2) {
												if (tCode == null) {
													closeLayer = closeCollection.getLayer(layerID);
												} else {
													closeLayer = collection.getLayer(tCode);
												}
												if (closeLayer != null) {
													layerOp.closeLayerOp(type, neatLine, typeLayer, closeLayer,
															tolerance);
												}
											} else if (cate == 5 || cate == 6) {
												if (tCode == null) {
													closeLayer = closeCollection.getLayer(layerID);
												} else {
													closeLayer = collection.getLayer(tCode);
												}
												if (closeLayer != null) {
													layerOp.closeLayerOpF2(type, neatLine, typeLayer, closeLayer,
															tolerance);
												}
											}
											DTLayer reFilterLayer = layerOp.getCloseLayer();
											DTLayer filterLayer = new DTLayer();
											filterLayer.setLayerID(typeLayer.getLayerID());
											filterLayer.setLayerType(typeLayer.getLayerType());
											filterLayer.setSimpleFeatureCollection(
													layerOp.getTypeLayer().getSimpleFeatureCollection());
											LayerValidator layerValidator = new LayerValidatorImpl(filterLayer);
											if (filters != null) {
												String closeLayerId = closeLayer.getLayerID();
												for (OptionFilter filter : filters) {
													String code = filter.getCode();
													if (code.equals(closeLayerId)) {
														closeLayer.setFilter(filter);
													}
												}
											}
											typeErrorLayer = layerValidator.validateRefZValueMiss(reFilterLayer,
													layerOp.closeBoundary, figure, tolerance);
											if (typeErrorLayer != null) {
												closeCollectionResult.mergeErrorLayer(typeErrorLayer);
											}
										}
									}
								}
							}
							typeLayer.setFilter(null);
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if (isTrue) {
			List<Future<CloseCollectionResult>> futures = new ArrayList<Future<CloseCollectionResult>>();
			for (QALayerType type : types) {
				// getTypeOption
				QAOption options = type.getOption();
				if (options != null) {
					List<CloseMiss> closeMiss = options.getCloseMissOptions();
					if (closeMiss == null) {
						continue;
					}
					List<String> layerCodes = type.getLayerIDList();
					for (String code : layerCodes) {
						DTLayer layer = collection.getLayer(code);
						if (layer != null) {
							Runnable task = new Task(closeCollectionResult, closeMiss, layer, closeMap);
							Future<CloseCollectionResult> future = executorService.submit(task, closeCollectionResult);
							if (future != null) {
								futures.add(future);
							}else{
								Progress.modifyMax();
							}
						}
					}
					for (Future<CloseCollectionResult> future : futures) {
						try {
							if (future != null) {
								closeCollectionResult = future.get();
								if (close_ < futures.size()){
									BathService.pb.updateProgress();
									close_++;
								}
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				} else {
					continue;
				}
			}
			executorService.shutdown();
			errLayer.mergeErrorLayer(closeCollectionResult.treadErrorLayer);
		}
	}

	@SuppressWarnings("unused")
	private void attributeValidate(QALayerTypeList types, DTLayerCollection layerCollection) throws SchemaException {

		DTLayer neatLayer = layerCollection.getNeatLine();

		ExecutorService executorService = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("CollectionValidator.attribute 검수-%d").build());
		AttResult attrResult = new AttResult();

		class Task implements Runnable {

			QALayerType type;
			AttResult attrResult;
			DTLayer typeLayer;
			List<AttributeMiss> attributeMisses;

			Task(QALayerType type, AttResult attrResult, List<AttributeMiss> attributeMisses, DTLayer typeLayer) {
				this.type = type;
				this.attrResult = attrResult;
				this.typeLayer = typeLayer;
				this.attributeMisses = attributeMisses;
			}

			@Override
			public void run() {

				ErrorLayer typeErrorLayer = null;
				LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
				String layerID = typeLayer.getLayerID();
				try {
					for (AttributeMiss attributeMiss : attributeMisses) {
						// filter
						List<OptionFilter> filters = attributeMiss.getFilter();
						if (filters != null) {
							for (OptionFilter filter : filters) {
								String code = filter.getCode();
								if (code.equals(layerID)) {
									typeLayer.setFilter(filter);
								}
							}
						}
						// figure
						List<OptionFigure> figures = attributeMiss.getFigure();
						// option
						String option = attributeMiss.getOption();
						// 지하시설물
						if (option.equals("UAvrgDPH20")) {
							// relation
							List<OptionRelation> relations = attributeMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									List<OptionFigure> reFigure = relation.getFigures();
									if (reFigure != null && figures != null) {
										typeErrorLayer = layerValidator.validateUAvrgDPH20(figures, relationLayers,
												reFigure);
										if (typeErrorLayer != null) {
											attrResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
						}
						if (option.equals("USymbolDirection")) {
							// relation
							List<OptionRelation> relations = attributeMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									List<OptionFigure> reFigure = relation.getFigures();
									if (reFigure != null && figures != null) {
										typeErrorLayer = layerValidator.validateUSymbolDirection(figures,
												relationLayers, reFigure);
										if (typeErrorLayer != null) {
											attrResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
						}
						// 임상도
						if (option.equals("FCodeLogicalAttribute")) {
							if (figures != null) {
								typeErrorLayer = layerValidator.validateFFcodeLogicalAttribute(figures);
								if (typeErrorLayer != null) {
									attrResult.mergeErrorLayer(typeErrorLayer);
								}
							}
						}
						if (option.equals("FLabelLogicalAttribute")) {
							if (figures != null) {
								typeErrorLayer = layerValidator.validateFLabelLogicalAttribute(figures);
								if (typeErrorLayer != null) {
									attrResult.mergeErrorLayer(typeErrorLayer);
								}
							}
						}
						if (option.equals("Dissolve")) {
							if (figures != null) {
								// self
								typeErrorLayer = layerValidator.validateDissolve(figures);
								if (typeErrorLayer != null) {
									attrResult.mergeErrorLayer(typeErrorLayer);
								}
								// close
								// MapSystemRule layerRule = new
								// MapSystemRule().setMapSystemRule(layerID);
								// Map<String, DTLayer> ruleMap =
								// layerRule.getMapSystemRuleMap(type,
								// collection);
								// if (ruleMap != null) {
								// Iterator ruleIter =
								// ruleMap.keySet().iterator();
								// while (ruleIter.hasNext()) {
								// String key = (String) ruleIter.next();
								// DTLayer closeLayer = ruleMap.get(key);
								// if (closeLayer != null) {
								// typeErrorLayer =
								// layerValidator.validateDissolve(figures,
								// closeLayer);
								// if (typeErrorLayer != null) {
								// attrResult.mergeErrorLayer(typeErrorLayer);
								// }
								// }
								// }
								// }
							}
						}
						// 수치지도
						if (option.equals("ZValueAmbiguous")) {
							if (figures != null) {
								for (OptionFigure figure : figures) {
									String code = figure.getCode();
									if (layerID.equals(code)) {
										typeErrorLayer = layerValidator.validateZValueAmbiguous(figure);
										if (typeErrorLayer != null) {
											attrResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
						}
						if (option.equals("BridgeName")) {
							for (OptionFigure figure : figures) {
								String code = figure.getCode();
								if (layerID.equals(code)) {
									List<OptionRelation> relations = attributeMiss.getRetaion();
									DTLayerList relationLayers = null;
									if (relations != null) {
										relationLayers = new DTLayerList();
										for (OptionRelation relation : relations) {
											String relationName = relation.getName();
											List<OptionFilter> reFilters = relation.getFilters();
											if (reFilters != null) {
												for (OptionFilter filter : reFilters) {
													String filterCode = filter.getCode();
													DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
															layerCollection);
													relationLayer.setFilter(filter);
													relationLayers.add(relationLayer);
												}
											} else {
												relationLayers = types.getTypeLayers(relationName, layerCollection);
											}
											List<OptionFigure> reFigures = relation.getFigures();
											if (reFigures != null) {
												typeErrorLayer = layerValidator.validateBridgeNameMiss(figure,
														relationLayers, reFigures);
												if (typeErrorLayer != null) {
													attrResult.mergeErrorLayer(typeErrorLayer);
												}
											}
										}
									}
								}
							}
						}
						if (option.equals("AdminMiss")) {
							if (figures != null) {
								for (OptionFigure figure : figures) {
									typeErrorLayer = layerValidator.validateAdminBoundaryMiss(figure);
									if (typeErrorLayer != null) {
										attrResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("NumericalValue")) {
							if (figures != null) {
								for (OptionFigure figure : figures) {
									String code = figure.getCode();
									if (code == null) {
										typeErrorLayer = layerValidator.validateNumericalValue(figure);
									} else {
										if (code.equals(layerID)) {
											typeErrorLayer = layerValidator.validateNumericalValue(figure);
										}
									}
									if (typeErrorLayer != null) {
										attrResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("UFIDMiss")) {
							if (figures != null) {
								for (OptionFigure figure : figures) {
									List<OptionRelation> relations = attributeMiss.getRetaion();
									DTLayerList relationLayers = null;
									if (relations != null) {
										relationLayers = new DTLayerList();
										for (OptionRelation relation : relations) {
											String relationName = relation.getName();
											List<OptionFilter> reFilters = relation.getFilters();
											if (reFilters != null) {
												for (OptionFilter filter : reFilters) {
													String filterCode = filter.getCode();
													DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
															layerCollection);
													relationLayer.setFilter(filter);
													relationLayers.add(relationLayer);
												}
											} else {
												relationLayers = types.getTypeLayers(relationName, layerCollection);
											}
											typeErrorLayer = layerValidator.validateUFIDMiss(
													collection.getCollectionName(), layerID, figure, relationLayers);
											if (typeErrorLayer != null) {
												attrResult.mergeErrorLayer(typeErrorLayer);
											}
										}
									} else {
										typeErrorLayer = layerValidator.validateUFIDMiss(collection.getCollectionName(),
												layerID, figure, null);
										if (typeErrorLayer != null) {
											attrResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
						}
						typeLayer.setFilter(null);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		List<Future<AttResult>> futures = new ArrayList<Future<AttResult>>();
		for (QALayerType type : types) {
			// getTypeOption
			QAOption options = type.getOption();
			if (options != null) {
				List<AttributeMiss> attributeMiss = options.getAttributeMissOptions();
				if (attributeMiss == null) {
					continue;
				}
				List<String> layerCodes = type.getLayerIDList();
				for (String code : layerCodes) {
					DTLayer layer = collection.getLayer(code);
					if (layer != null) {
						Runnable task = new Task(type, attrResult, attributeMiss, layer);
						Future<AttResult> future = executorService.submit(task, attrResult);
						if (future != null) {
							futures.add(future);
						}else{
							Progress.modifyMax();
						}
					}
				}
				for (Future<AttResult> future : futures) {
					try {
						attrResult = future.get();
						if (attr_ < futures.size()){
							BathService.pb.updateProgress();
							attr_++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				continue;
			}
		}
		executorService.shutdown();
		errLayer.mergeErrorLayer(attrResult.treadErrorLayer);
	}

	@SuppressWarnings("unused")
	private void geometricValidate(QALayerTypeList types, DTLayerCollection layerCollection)
			throws SchemaException, NoSuchAuthorityCodeException, FactoryException, TransformException, IOException {

		DTLayer neatLayer = layerCollection.getNeatLine();

		ExecutorService executorService = Executors.newFixedThreadPool(5, new ThreadFactoryBuilder().setNameFormat("CollectionValidator.geometric 검수-%d").build());
		GeometricResult geometricResult = new GeometricResult();

		class Task implements Runnable {
			GeometricResult geometricResult;
			DTLayer typeLayer;
			List<GraphicMiss> geometricMisses;

			Task(GeometricResult geometricResult, List<GraphicMiss> geometricMisses, DTLayer typeLayer) {
				this.geometricResult = geometricResult;
				this.typeLayer = typeLayer;
				this.geometricMisses = geometricMisses;
			}

			@Override
			public void run() {
				ErrorLayer typeErrorLayer = null;
				String layerID = typeLayer.getLayerID();
				try {
					for (GraphicMiss geometricMiss : geometricMisses) {
						// filter
						List<OptionFilter> filters = geometricMiss.getFilter();
						if (filters != null) {
							for (OptionFilter filter : filters) {
								String code = filter.getCode();
								if (code.equals(layerID)) {
									typeLayer.setFilter(filter);
								}
							}
						}
						// tolerance
						List<OptionTolerance> tolerance = geometricMiss.getTolerance();
						// option
						String option = geometricMiss.getOption();

						// 지하시설물 2.0
						if (option.equals("USymbolInLine")) {
							// relation
							List<OptionRelation> relations = geometricMiss.getRetaion();
							List<OptionTolerance> totalTolerances = null;
							DTLayerList totalLayers = new DTLayerList();
							DTLayerList relationLayers = null;

							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											totalLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
										for (DTLayer layer : relationLayers) {
											totalLayers.add(layer);
										}
									}
									List<OptionTolerance> reTolerances = relation.getTolerances();

									if (reTolerances != null) {
										if (totalTolerances == null) {
											totalTolerances = reTolerances;
										} else {
											for (OptionTolerance tol : reTolerances) {
												totalTolerances.add(tol);
											}
										}
									}
								}
								LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
								typeErrorLayer = layerValidator.validateSymbolInLine(totalLayers, totalTolerances);
								if (typeErrorLayer != null) {
									geometricResult.mergeErrorLayer(typeErrorLayer);
								}
							}
						}
						if (option.equals("ULineCross")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validateLineCross();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("USymbolsDistance")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validateSymbolsDistance();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("SymbolOut")) {
							// relation
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateSymbolOut(relationLayers);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("USymbolOut")) {
							// relation
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateUSymbolOut(relationLayers);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("UNodeMiss")) {
							// relation
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateUNodeMiss(relationLayers);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						// 지하시설물 1.0
						if (option.equals("UAvrgDPH10")) {
							// relation
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								List<OptionTolerance> toleranceList = new ArrayList<>();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionTolerance> reTolerances = relation.getTolerances();
									if (reTolerances != null) {
										toleranceList.addAll(reTolerances);
									}
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers.addAll(types.getTypeLayers(relationName, layerCollection));
									}
								}
								if (toleranceList != null) {
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateUAvrgDPH10(relationLayers, toleranceList);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("ULeaderline")) {
							// relation
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateULeaderline(relationLayers);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("MultiPart")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validateMultiPart();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("SmallArea")) {
							for (OptionTolerance tole : tolerance) {
								String code = tole.getCode();
								if (code != null) {
									if (!code.equals(layerID)) {
										continue;
									}
								}
								LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
								typeErrorLayer = layerValidator.validateSmallArea(tole);
								if (typeErrorLayer != null) {
									geometricResult.mergeErrorLayer(typeErrorLayer);
								}
							}
						}
						if (option.equals("SelfEntity")) {
							for (OptionTolerance tole : tolerance) {
								String code = tole.getCode();
								if (code != null) {
									if (!code.equals(layerID)) {
										continue;
									}
								}
								// relation
								List<OptionRelation> relations = geometricMiss.getRetaion();
								DTLayerList relationLayers = null;
								if (relations != null) {
									relationLayers = new DTLayerList();
									for (OptionRelation relation : relations) {
										String relationName = relation.getName();
										List<OptionFilter> reFilters = relation.getFilters();
										if (reFilters != null) {
											for (OptionFilter filter : reFilters) {
												String filterCode = filter.getCode();
												DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
														layerCollection);
												relationLayer.setFilter(filter);
												relationLayers.add(relationLayer);
											}
										} else {
											relationLayers = types.getTypeLayers(relationName, layerCollection);
										}
										LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
										typeErrorLayer = layerValidator.validateSelfEntity(relationLayers, tole);
										if (typeErrorLayer != null) {
											geometricResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
						}
						if (option.equals("HoleMisplacement")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validateHoleMissplacement();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("LinearDisconnection")) {
							for (OptionTolerance tole : tolerance) {
								String code = tole.getCode();
								if (code != null) {
									if (!code.equals(layerID)) {
										continue;
									}
								}
								// relation
								List<OptionRelation> relations = geometricMiss.getRetaion();
								DTLayerList relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.valildateLinearDisconnection(relationLayers, tole);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("EntityInHole")) {
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateEntityInHole(relationLayers);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("FEntityInHole")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validateFEntityInHole();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("CenterLineMiss")) {
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateCenterLineMiss(relationLayers);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("BoundaryMiss")) {
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateBoundaryMiss(relationLayers);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("BuildingSiteMiss")) {
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers.addAll(types.getTypeLayers(relationName, layerCollection));
									}
								}
								LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
								typeErrorLayer = layerValidator.validateBuildingSiteMiss(relationLayers);
								if (typeErrorLayer != null) {
									geometricResult.mergeErrorLayer(typeErrorLayer);
								}
							}
						}
						if (option.equals("OneStage")) {
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;
							if (relations != null) {
								relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateOneStage(relationLayers);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("EntityDuplicated")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validateEntityDuplicated();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("OutBoundary")) {
							for (OptionTolerance tole : tolerance) {
								String code = tole.getCode();
								if (code != null) {
									if (!code.equals(layerID)) {
										continue;
									}
								}
								// relation
								List<OptionRelation> relations = geometricMiss.getRetaion();
								DTLayerList relationLayers = new DTLayerList();
								for (OptionRelation relation : relations) {
									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();
									if (reFilters != null) {
										for (OptionFilter filter : reFilters) {
											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {
										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateOutBoundary(relationLayers, tole);
									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						if (option.equals("ConBreak")) {
							for (OptionTolerance tole : tolerance) {

								String code = tole.getCode();

								if (code != null) {

									if (!code.equals(layerID)) {

										continue;
									}
								}

								// relation
								List<OptionRelation> relations = geometricMiss.getRetaion();
								DTLayerList relationLayers = null;

								if (relations != null) {

									relationLayers = new DTLayerList();

									for (OptionRelation relation : relations) {

										String relationName = relation.getName();
										List<OptionFilter> reFilters = relation.getFilters();

										if (reFilters != null) {

											for (OptionFilter filter : reFilters) {

												String filterCode = filter.getCode();
												DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
														layerCollection);
												relationLayer.setFilter(filter);
												relationLayers.add(relationLayer);
											}
										} else {

											relationLayers = types.getTypeLayers(relationName, layerCollection);
										}
										LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
										typeErrorLayer = layerValidator.validateConBreak(relationLayers, tole);

										if (typeErrorLayer != null) {
											geometricResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
						}
						if (option.equals("ConIntersected")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validateConIntersected();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("ConOverDegree")) {
							for (OptionTolerance tole : tolerance) {

								String code = tole.getCode();

								if (code != null) {

									if (!code.equals(layerID)) {
										continue;
									}
								}
								LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
								typeErrorLayer = layerValidator.validateConOverDegree(tole);

								if (typeErrorLayer != null) {
									geometricResult.mergeErrorLayer(typeErrorLayer);
								}
							}
						}
						if (option.equals("UselessPoint")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validateUselessPoint();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("SmallLength")) {
							for (OptionTolerance tole : tolerance) {

								String code = tole.getCode();

								if (code != null) {

									if (!code.equals(layerID)) {
										continue;
									}
								}
								LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
								typeErrorLayer = layerValidator.validateSmallLength(tole);

								if (typeErrorLayer != null) {
									geometricResult.mergeErrorLayer(typeErrorLayer);
								}
							}
						}
						if (option.equals("Overshoot")) {
							for (OptionTolerance tole : tolerance) {

								String code = tole.getCode();

								if (code != null) {

									if (!code.equals(layerID)) {

										continue;
									}
								}

								// relation
								List<OptionRelation> relations = geometricMiss.getRetaion();
								DTLayerList relationLayers = null;

								if (relations != null) {

									relationLayers = new DTLayerList();

									for (OptionRelation relation : relations) {

										String relationName = relation.getName();
										List<OptionFilter> reFilters = relation.getFilters();

										if (reFilters != null) {

											for (OptionFilter filter : reFilters) {

												String filterCode = filter.getCode();
												DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
														layerCollection);
												relationLayer.setFilter(filter);
												relationLayers.add(relationLayer);
											}
										} else {

											relationLayers = types.getTypeLayers(relationName, layerCollection);
										}
										LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
										typeErrorLayer = layerValidator.validateOverShoot(relationLayers, tole);

										if (typeErrorLayer != null) {
											geometricResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
						}
						if (option.equals("EntityOpenMiss")) {
							for (OptionTolerance tole : tolerance) {

								String code = tole.getCode();

								if (code != null) {

									if (!code.equals(layerID)) {

										continue;
									}
								}

								// relation
								List<OptionRelation> relations = geometricMiss.getRetaion();
								DTLayerList relationLayers = null;

								if (relations != null) {

									relationLayers = new DTLayerList();

									for (OptionRelation relation : relations) {

										String relationName = relation.getName();
										List<OptionFilter> reFilters = relation.getFilters();

										if (reFilters != null) {

											for (OptionFilter filter : reFilters) {

												String filterCode = filter.getCode();
												DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
														layerCollection);
												relationLayer.setFilter(filter);
												relationLayers.add(relationLayer);
											}
										} else {

											relationLayers = types.getTypeLayers(relationName, layerCollection);
										}
										LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
										typeErrorLayer = layerValidator.validateEntityOpenMiss(relationLayers, tole);

										if (typeErrorLayer != null) {
											geometricResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
						}
						if (option.equals("TwistedPolygon")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validateTwistedPolygon();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("NodeMiss")) {
							for (OptionTolerance tole : tolerance) {

								String code = tole.getCode();

								if (code != null) {

									if (!code.equals(layerID)) {

										continue;
									}
								}
								// relation
								List<OptionRelation> relations = geometricMiss.getRetaion();
								DTLayerList relationLayers = null;

								if (relations != null) {

									relationLayers = new DTLayerList();

									for (OptionRelation relation : relations) {

										String relationName = relation.getName();
										List<OptionFilter> reFilters = relation.getFilters();

										if (reFilters != null) {

											for (OptionFilter filter : reFilters) {

												String filterCode = filter.getCode();
												DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
														layerCollection);
												relationLayer.setFilter(filter);
												relationLayers.add(relationLayer);
											}
										} else {

											relationLayers = types.getTypeLayers(relationName, layerCollection);
										}
										LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
										typeErrorLayer = layerValidator.validateNodeMiss(relationLayers, "", tole);

										if (typeErrorLayer != null) {
											geometricResult.mergeErrorLayer(typeErrorLayer);
										}
									}
								}
							}
						}
						if (option.equals("PointDuplicated")) {
							LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
							typeErrorLayer = layerValidator.validatePointDuplicated();
							if (typeErrorLayer != null) {
								geometricResult.mergeErrorLayer(typeErrorLayer);
							}
						}
						if (option.equals("OneAcre")) {
							// relation
							List<OptionRelation> relations = geometricMiss.getRetaion();
							DTLayerList relationLayers = null;

							if (relations != null) {

								relationLayers = new DTLayerList();

								for (OptionRelation relation : relations) {

									String relationName = relation.getName();
									List<OptionFilter> reFilters = relation.getFilters();

									if (reFilters != null) {

										for (OptionFilter filter : reFilters) {

											String filterCode = filter.getCode();
											DTLayer relationLayer = types.getTypeLayer(relationName, filterCode,
													layerCollection);
											relationLayer.setFilter(filter);
											relationLayers.add(relationLayer);
										}
									} else {

										relationLayers = types.getTypeLayers(relationName, layerCollection);
									}
									LayerValidator layerValidator = new LayerValidatorImpl(typeLayer);
									typeErrorLayer = layerValidator.validateOneAcre(relationLayers, 0.0);

									if (typeErrorLayer != null) {
										geometricResult.mergeErrorLayer(typeErrorLayer);
									}
								}
							}
						}
						typeLayer.setFilter(null);
					}
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		List<Future<GeometricResult>> futures = new ArrayList<Future<GeometricResult>>();
		for (QALayerType type : types) {
			// getTypeOption
			QAOption options = type.getOption();
			if (options != null) {
				List<GraphicMiss> graphicMiss = options.getGraphicMissOptions();
				if (graphicMiss == null) {
					continue;
				}
				List<String> layerCodes = type.getLayerIDList();
				for (String code : layerCodes) {
					DTLayer layer = collection.getLayer(code);
					if (layer != null) {
						Runnable task = new Task(geometricResult, graphicMiss, layer);
						Future<GeometricResult> future = executorService.submit(task, geometricResult);
						if (future != null) {
							futures.add(future);
						}else{
							Progress.modifyMax();
						}
					}
				}
				for (Future<GeometricResult> future : futures) {
					try {
						
						geometricResult = future.get();							
						if (geo_ < futures.size()){
							BathService.pb.updateProgress();
							geo_++;
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			} else {
				continue;
			}
		}
		executorService.shutdown();
		errLayer.mergeErrorLayer(geometricResult.treadErrorLayer);
	}

	private void layerMissValidate(QALayerTypeList types, DTLayerCollection layerCollection) throws SchemaException {

		for (QALayerType type : types) {
			QAOption options = type.getOption();
			if (options != null) {
				ErrorLayer typeErrorLayer = null;
				List<LayerFixMiss> layerFixMissArr = options.getLayerMissOptions();
				for (LayerFixMiss layerFixMiss : layerFixMissArr) {
					String code = layerFixMiss.getCode();
					String option = layerFixMiss.getOption();
					DTLayer codeLayer = layerCollection.getLayer(code);
					if (codeLayer == null) {
						continue;
					}
					BathService.pb.updateProgress();
					LayerValidatorImpl layerValidator = new LayerValidatorImpl(codeLayer);
					if (option.equals("LayerFixMiss")) { // tmp -> enum으로 대체
						String geometry = layerFixMiss.getGeometry();
						List<FixedValue> fixedValue = layerFixMiss.getFix();
						typeErrorLayer = layerValidator.validateLayerFixMiss(geometry, fixedValue);
						if (typeErrorLayer != null) {
							errLayer.mergeErrorLayer(typeErrorLayer);
						}
					}
				}
			}
		}
	}
}

/**
 * 쓰레드 AttResult 클래스
 * 
 * @author SG.Lee
 * @since 2017. 9. 6. 오후 3:09:38
 */
class AttResult {
	ErrorLayer treadErrorLayer = new ErrorLayer();

	synchronized void mergeErrorLayer(ErrorLayer typeErrorLayer) {

		if (typeErrorLayer != null) {
			treadErrorLayer.mergeErrorLayer(typeErrorLayer);
		}
	}
}

/**
 * 쓰레드 GeomResult 클래스
 * 
 * @author SG.Lee
 * @since 2017. 9. 6. 오후 3:09:38
 */
class GeometricResult {
	ErrorLayer treadErrorLayer = new ErrorLayer();

	synchronized void mergeErrorLayer(ErrorLayer typeErrorLayer) {

		if (typeErrorLayer != null) {
			treadErrorLayer.mergeErrorLayer(typeErrorLayer);
		}
	}
}

/**
 * 쓰레드 CloseCollectionResult 클래스
 * 
 * @author SG.Lee
 * @since 2017. 9. 6. 오후 3:09:38
 */
class CloseCollectionResult {
	ErrorLayer treadErrorLayer = new ErrorLayer();

	synchronized void mergeErrorLayer(ErrorLayer typeErrorLayer) {

		if (typeErrorLayer != null) {
			treadErrorLayer.mergeErrorLayer(typeErrorLayer);
		}
	}
}
