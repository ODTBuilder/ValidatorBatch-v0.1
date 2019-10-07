package com.git.gdsbuilder.validator.collection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.util.NullProgressListener;
import org.json.simple.JSONArray;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.git.gdsbuilder.type.dt.layer.BasicDTLayer;
import com.git.gdsbuilder.type.dt.layer.BasicDTLayerList;
import com.git.gdsbuilder.type.validate.error.ErrorLayer;
import com.git.gdsbuilder.type.validate.layer.QALayerType;
import com.git.gdsbuilder.type.validate.layer.QALayerTypeList;
import com.git.gdsbuilder.type.validate.option.QAOption;
import com.git.gdsbuilder.type.validate.option.en.LangType;
import com.git.gdsbuilder.type.validate.option.specific.AttributeMiss;
import com.git.gdsbuilder.type.validate.option.specific.GraphicMiss;
import com.git.gdsbuilder.type.validate.option.specific.OptionFigure;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionRelation;
import com.git.gdsbuilder.type.validate.option.specific.OptionTolerance;
import com.git.gdsbuilder.type.validate.option.standard.LayerFixMiss;
import com.git.gdsbuilder.validator.fileReader.shp.parser.SHPFileLayerParser;
import com.git.gdsbuilder.validator.layer.OpenLayerValidator;
import com.git.gdsbuilder.validator.quad.OptimalEnvelopsOp;
import com.git.gdsbuilder.validator.quad.Quadtree;
import com.git.gdsbuilder.validator.quad.Root;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.TopologyException;

public class OpenCollectionValidator {

	String fileDir;
	QALayerTypeList validateLayerTypeList;
	String epsg;
	JSONArray attrFilterArry;
	JSONArray stateFilterArry;
	GeometryFactory f = new GeometryFactory();
	LangType langType;

	public OpenCollectionValidator(String fileDir, QALayerTypeList validateLayerTypeList, String epsg,
			JSONArray attrFilter, JSONArray stateFilter, LangType langType) throws IOException {
		this.fileDir = fileDir;
		this.validateLayerTypeList = validateLayerTypeList;
		this.epsg = epsg;
		this.attrFilterArry = attrFilter;
		this.stateFilterArry = stateFilter;
		this.langType = langType;
	}

	public String getFileDir() {
		return fileDir;
	}

	public void setFileDir(String fileDir) {
		this.fileDir = fileDir;
	}

	public QALayerTypeList getValidateLayerTypeList() {
		return validateLayerTypeList;
	}

	public void setValidateLayerTypeList(QALayerTypeList validateLayerTypeList) {
		this.validateLayerTypeList = validateLayerTypeList;
	}

	public String getEpsg() {
		return epsg;
	}

	public void setEpsg(String epsg) {
		this.epsg = epsg;
	}

	public JSONArray getAttrFilterArry() {
		return attrFilterArry;
	}

	public void setAttrFilterArry(JSONArray attrFilterArry) {
		this.attrFilterArry = attrFilterArry;
	}

	public JSONArray getStateFilterArry() {
		return stateFilterArry;
	}

	public void setStateFilterArry(JSONArray stateFilterArry) {
		this.stateFilterArry = stateFilterArry;
	}

	public LangType getLangType() {
		return langType;
	}

	public void setLangType(LangType langType) {
		this.langType = langType;
	}

	public ErrorLayer collectionAttributeValidate() throws IOException, SchemaException {

		ErrorLayer errLayer = new ErrorLayer();
		for (QALayerType qaType : validateLayerTypeList) {
			String typeName = qaType.getName();
			// option
			QAOption qaOption = qaType.getOption();
//			List<LayerFixMiss> layerFixMissArr = qaOption.getLayerMissOptions();
//			if (layerFixMissArr != null) {
//				for (LayerFixMiss fix : layerFixMissArr) {
//					String geometry = fix.getGeometry();
//					List<FixedValue> fixedList = fix.getFix();
//					if (fixedList != null) {
//						String layerName = fix.getCode();
//						// target layer
//						BasicDTLayer dtLayer = getDTLayer(fileDir, layerName);
//						if (dtLayer == null) {
//							continue;
//						}
//						dtLayer.setTypeName(typeName);
//						OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
//						// layer fix
//						ErrorLayer layerFixErr = layerValidator.validateLayerFixMiss(geometry);
//						if (layerFixErr != null) {
//							errLayer.mergeErrorLayer(layerFixErr);
//						}
//						// attribute fix
//						ErrorLayer attrFixErr = layerValidator.validateAttributeFixMiss(geometry, fixedList);
//						if (attrFixErr != null) {
//							errLayer.mergeErrorLayer(attrFixErr);
//						}
//						// attreibute
//						ErrorLayer attrErr = layerValidator.validateAttributeMiss(geometry, fixedList);
//						if (attrErr != null) {
//							errLayer.mergeErrorLayer(attrErr);
//						}
//						layerValidator = null;
//						dtLayer = null;
//					}
//				}
//			}
			List<String> layerNames = qaType.getLayerIDList();
			for (String layerName : layerNames) {
				// target layer
				BasicDTLayer dtLayer = getDTLayer(fileDir, layerName);
				if (dtLayer == null) {
					continue;
				}
				List<AttributeMiss> attrMissArr = qaOption.getAttributeMissOptions();
				if (attrMissArr != null) {
					for (AttributeMiss attrMiss : attrMissArr) {
						String optionName = attrMiss.getOption();

						System.out.println(layerName + "-" + optionName);

						OptionFilter filter = attrMiss.getLayerFilter(layerName);
						OptionFigure figure = attrMiss.getLayerFigure(layerName);
						OptionTolerance tolerance = attrMiss.getLayerTolerance(layerName);
						List<OptionRelation> relations = attrMiss.getRetaion();
						// 속성오류 (AttributeMiss)
						if (optionName.equals("AttributeMiss")) {
							LayerFixMiss fix = qaOption.getLayerMissOption(layerName);
							if (fix != null) {
								OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
								ErrorLayer attrErr = layerValidator.validateAttributeMiss(fix.getFix());
								if (attrErr != null) {
									errLayer.mergeErrorLayer(attrErr);
								}
							}
						}
						// 필수속성오류 (AttributeFixMiss)
						if (optionName.equals("AttributeFixMiss")) {
							LayerFixMiss fix = qaOption.getLayerMissOption(layerName);
							if (fix != null) {
								OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
								ErrorLayer attrFixErr = layerValidator.validateAttributeFixMiss(fix.getFix());
								if (attrFixErr != null) {
									errLayer.mergeErrorLayer(attrFixErr);
								}
							}
						}
						// 고도값오류 (Z-Value Abmiguous)
						if (optionName.equals("ZValueAmbiguous2")) {
							OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
							ErrorLayer typeErr = layerValidator.validateZvalueAmbiguous(filter, figure);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
							}
							layerValidator = null;
						}
						// 인접속성오류 (RefAttributeMiss)
						if (optionName.equals("RefAttributeMissB")) {
							SimpleFeatureCollection targetQuadSfc = dtLayer.getSimpleFeatureCollection();
							Quadtree targetQuad = getQuadTree(targetQuadSfc);
							Root root = targetQuad.getRoot();
							int maxLevel = root.maxLevel();
							OptimalEnvelopsOp op = new OptimalEnvelopsOp(targetQuad, maxLevel, 100);
							List<Envelope> results = op.getOptimalEnvelops(maxLevel);
							Envelope tarBounds = targetQuadSfc.getBounds();
							double quadIndexWidth = 0;
							for (Object result : results) {
								Envelope envelope = (Envelope) result;
								if (envelope != null) {
									quadIndexWidth = envelope.getHeight();
									break;
								}
							}
							FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
							if (relations != null) {
								BasicDTLayer targetLayer = new BasicDTLayer();
								targetLayer.setLayerID(dtLayer.getLayerID());
								targetLayer.setTypeName(typeName);
								targetLayer.setLayerType(dtLayer.getLayerType());
								for (OptionRelation relation : relations) {
									String reTypeName = relation.getName();
									BasicDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (BasicDTLayer reDTLayer : reDTLayers) {
											SimpleFeatureCollection reQuadSfc = null;
											Quadtree reQuadtree = null;
											if (reDTLayer.getLayerID().equals(layerName)) {
												reQuadtree = targetQuad;
												reQuadSfc = targetQuadSfc;
											} else {
												reQuadSfc = reDTLayer.getSimpleFeatureCollection();
												Quadtree quad = getQuadTree(reQuadSfc);
												reQuadtree = quad;
											}
											BasicDTLayer retargetLayer = new BasicDTLayer();
											retargetLayer.setLayerID(reDTLayer.getLayerID());
											retargetLayer.setTypeName(reTypeName);
											retargetLayer.setFilter(reDTLayer.getFilter());
											retargetLayer.setLayerType(reDTLayer.getLayerType());

											Envelope reBounds = reQuadSfc.getBounds();
											Geometry wholeEnvGeom = f.toGeometry(tarBounds)
													.intersection(f.toGeometry(reBounds));
											Envelope wholeEnv = wholeEnvGeom.getEnvelopeInternal();
											List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);

											for (Envelope envelope : gridEnvs) {
												List items = targetQuad.query(envelope);
												SimpleFeatureCollection dfc = new DefaultFeatureCollection();
												int size = items.size();
												if (size != 0) {
													for (int i = 0; i < size; i++) {
														SimpleFeature sf = (SimpleFeature) items.get(i);
														if (sf != null) {
															((DefaultFeatureCollection) dfc).add(sf);
														}
													}
													Filter intFilter = ff.intersects(ff.property("the_geom"),
															ff.literal(envelope));
													dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);
													DefaultFeatureCollection tfc = new DefaultFeatureCollection();
													SimpleFeatureIterator iter = dfc.features();
													int dfcSize = dfc.size();
													if (dfcSize != 0) {
														while (iter.hasNext()) {
															SimpleFeature isf = getIntersection(envelope,
																	(SimpleFeature) iter.next());
															if (isf != null) {
																tfc.add(isf);
															}
														}
														iter.close();
														dfc = null;
														targetLayer.setSimpleFeatureCollection(tfc);
														List reItems = reQuadtree.query(envelope);
														SimpleFeatureCollection reDfc = new DefaultFeatureCollection();
														int reSize = reItems.size();
														if (reSize > 0) {
															for (int i = 0; i < reSize; i++) {
																SimpleFeature sf = (SimpleFeature) reItems.get(i);
																if (sf != null) {
																	((DefaultFeatureCollection) reDfc).add(sf);
																}
															}
															Filter reFilter = ff.intersects(ff.property("the_geom"),
																	ff.literal(envelope));
															reDfc = (SimpleFeatureCollection) reDfc
																	.subCollection(reFilter);
															int reDfcSize = reDfc.size();
															if (reDfcSize != 0) {
																DefaultFeatureCollection retDfc = new DefaultFeatureCollection();
																SimpleFeatureIterator reiter = reDfc.features();
																while (reiter.hasNext()) {
																	SimpleFeature isf = getIntersection(envelope,
																			(SimpleFeature) reiter.next());
																	if (isf != null) {
																		retDfc.add(isf);
																	}
																}
																reiter.close();
																reDfc = null;
																retargetLayer.setSimpleFeatureCollection(retDfc);
																OpenLayerValidator layerValidator = new OpenLayerValidator(
																		dtLayer, langType);
																ErrorLayer typeErr = layerValidator
																		.validateRefAttributeMiss(filter, figure,
																				tolerance, retargetLayer);
																if (typeErr != null) {
																	errLayer.mergeErrorLayer(typeErr);
																}
																layerValidator = null;
															}
														}
														reItems = null;
													}
												}
												items = null;
											}
										}
										reDTLayers = null;
									}
								}
								targetLayer = null;
								relations = null;
							}
						}
						// 문자의 정확성(Text Accuracy)
						if (optionName.equals("FixValues")) {
							OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
							ErrorLayer typeErr = layerValidator.validateFixValues(filter, figure);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
							}
							layerValidator = null;
						}
					}
				}
			}
		}
		if (errLayer.getErrFeatureList().size() > 0) {
			return errLayer;
		} else {
			return null;
		}
	}

	public ErrorLayer collectionGraphicValidate() throws IOException, SchemaException {

		FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();

		ErrorLayer errLayer = new ErrorLayer();
		for (QALayerType qaType : validateLayerTypeList) {
			String typeName = qaType.getName();
			List<String> layerNames = qaType.getLayerIDList();
			for (String layerName : layerNames) {
				// target layer
				BasicDTLayer dtLayer = getDTLayer(fileDir, layerName);
				if (dtLayer == null) {
					continue;
				}
				dtLayer.setTypeName(typeName);
				SimpleFeatureCollection targetQuadSfc = dtLayer.getSimpleFeatureCollection();
				Quadtree targetQuad = getQuadTree(targetQuadSfc);

				Root root = targetQuad.getRoot();
				int maxLevel = root.maxLevel();
				OptimalEnvelopsOp op = new OptimalEnvelopsOp(targetQuad, maxLevel, 10000);
				List<Envelope> results = op.getOptimalEnvelops(maxLevel);
				Envelope tarBounds = targetQuadSfc.getBounds();

				double quadIndexWidth = 0;
				for (Object result : results) {
					Envelope envelope = (Envelope) result;
					if (envelope != null) {
						quadIndexWidth = envelope.getHeight();
						break;
					}
				}
				// option
				QAOption qaOption = qaType.getOption();
				List<GraphicMiss> grapMissArr = qaOption.getGraphicMissOptions();
				if (grapMissArr != null) {
					for (GraphicMiss grapMiss : grapMissArr) {
						String optionName = grapMiss.getOption();
						System.out.println(layerName + "-" + optionName);
						OptionFilter filter = grapMiss.getLayerFilter(layerName);
						OptionFigure figure = grapMiss.getLayerFigure(layerName);
						OptionTolerance tolerance = grapMiss.getLayerTolerance(layerName);
						List<OptionRelation> relations = grapMiss.getRetaion();
						// 계층오류 (LayerFix)
						if (optionName.equals("LayerMiss")) {
							LayerFixMiss fix = qaOption.getLayerMissOption(layerName);
							if (fix != null) {
								OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
								ErrorLayer typeErr = layerValidator.validateLayerFixMiss(fix.getGeometry());
								if (typeErr != null) {
									errLayer.mergeErrorLayer(typeErr);
									typeErr = null;
								}
								layerValidator = null;
							}
						}
						// 허용범위 이하 길이 (SmallLength)
						if (optionName.equals("SmallLengthB")) {
							OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
							ErrorLayer typeErr = layerValidator.validateSmallLength(filter, tolerance);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
								typeErr = null;
							}
							layerValidator = null;
						}
						// 허용범위 이하 면적 (SmallArea)
						if (optionName.equals("SmallAreaB")) {
							OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
							ErrorLayer typeErr = layerValidator.validateSmallArea(filter, tolerance);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
								typeErr = null;
							}
							layerValidator = null;
						}
						// 단독존재오류 (Self Entity)
						if (optionName.equals("SelfEntityB")) {
							if (relations != null) {
								BasicDTLayer targetLayer = new BasicDTLayer();
								targetLayer.setLayerID(dtLayer.getLayerID());
								targetLayer.setFilter(dtLayer.getFilter());
								targetLayer.setTypeName(typeName);
								targetLayer.setLayerType(dtLayer.getLayerType());
								for (OptionRelation relation : relations) {
									String reTypeName = relation.getName();
									BasicDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (BasicDTLayer reDTLayer : reDTLayers) {
											SimpleFeatureCollection reQuadSfc = null;
											Quadtree reQuadtree = null;
											if (reDTLayer.getLayerID().equals(layerName)) {
												reQuadtree = targetQuad;
												reQuadSfc = targetQuadSfc;
											} else {
												reQuadSfc = reDTLayer.getSimpleFeatureCollection();
												Quadtree quad = getQuadTree(reQuadSfc);
												reQuadtree = quad;
											}
											BasicDTLayer retargetLayer = new BasicDTLayer();
											retargetLayer.setLayerID(reDTLayer.getLayerID());
											retargetLayer.setTypeName(reTypeName);
											retargetLayer.setFilter(reDTLayer.getFilter());
											retargetLayer.setLayerType(reDTLayer.getLayerType());

											Envelope reBounds = reQuadSfc.getBounds();
											Geometry wholeEnvGeom = f.toGeometry(tarBounds)
													.intersection(f.toGeometry(reBounds));
											Envelope wholeEnv = wholeEnvGeom.getEnvelopeInternal();

											List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);
											for (Envelope envelope : gridEnvs) {
												try {
													List items = targetQuad.query(envelope);
													SimpleFeatureCollection dfc = new DefaultFeatureCollection();
													int size = items.size();
													if (size != 0) {
														for (int i = 0; i < size; i++) {
															SimpleFeature sf = (SimpleFeature) items.get(i);
															if (sf != null) {
																((DefaultFeatureCollection) dfc).add(sf);
															}
														}
														Filter intFilter = ff.intersects(ff.property("the_geom"),
																ff.literal(envelope));
														dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);
														DefaultFeatureCollection tfc = new DefaultFeatureCollection();
														SimpleFeatureIterator iter = dfc.features();
														int dfcSize = dfc.size();
														if (dfcSize != 0) {
															while (iter.hasNext()) {
																SimpleFeature isf = getIntersection(envelope,
																		(SimpleFeature) iter.next());
																if (isf != null) {
																	tfc.add(isf);
																}
															}
															iter.close();
															dfc = null;// 객체 초기화
															targetLayer.setSimpleFeatureCollection(tfc);
															List reItems = reQuadtree.query(envelope);
															SimpleFeatureCollection reDfc = new DefaultFeatureCollection();
															int reSize = reItems.size();
															if (reSize > 0) {
																for (int i = 0; i < reSize; i++) {
																	SimpleFeature sf = (SimpleFeature) reItems.get(i);
																	if (sf != null) {
																		((DefaultFeatureCollection) reDfc).add(sf);
																	}
																}
																Filter reFilter = ff.intersects(ff.property("the_geom"),
																		ff.literal(envelope));
																reDfc = (SimpleFeatureCollection) reDfc
																		.subCollection(reFilter);

																int reDfcSize = reDfc.size();
																if (reDfcSize != 0) {
																	DefaultFeatureCollection retDfc = new DefaultFeatureCollection();
																	SimpleFeatureIterator reiter = reDfc.features();

																	while (reiter.hasNext()) {
																		SimpleFeature isf = getIntersection(envelope,
																				(SimpleFeature) reiter.next());
																		if (isf != null) {
																			retDfc.add(isf);
																		}
																	}
																	reiter.close();
																	reDfc = null;

																	retargetLayer.setSimpleFeatureCollection(retDfc);
																	OpenLayerValidator layerValidator = new OpenLayerValidator(
																			dtLayer, langType);
																	if (typeName.equals(reTypeName)) {
																		ErrorLayer typeErr = layerValidator
																				.validateSelfEntity(filter, envelope,
																						100);
																		if (typeErr != null) {
																			errLayer.mergeErrorLayer(typeErr);
																			typeErr = null;
																		}
																	} else {
																		ErrorLayer typeErr = layerValidator
																				.validateSelfEntity(filter, tolerance,
																						retargetLayer, envelope, 100);
																		if (typeErr != null) {
																			errLayer.mergeErrorLayer(typeErr);
																			typeErr = null;
																		}
																	}
																	retDfc = null;
																	layerValidator = null;
																}
															}
															reItems = null;
														}
														tfc = null;
													}
													items = null;
												} catch (Exception e) {
													// TODO: handle exception
												}
											}
											gridEnvs = null;
											retargetLayer = null;
										}
										reDTLayers = null;
									}
								}
								targetLayer = null;
							}
						}
						// 요소중복오류 (EntityDuplicated)
						if (optionName.equals("EntityDuplicatedB")) {
							Envelope wholeEnv = tarBounds;
							for (Object result : results) {
								Envelope envelope = (Envelope) result;
								if (envelope != null) {
									quadIndexWidth = envelope.getMaxX() - envelope.getMinX();
									break;
								}
							}
							List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);
							Map<String, Object> validateMap = new HashMap<>();
							for (Envelope envelope : gridEnvs) {
								List items = targetQuad.query(envelope);
								SimpleFeatureCollection dfc = new DefaultFeatureCollection();
								int size = items.size();
								if (size != 0) {
									for (int i = 0; i < size; i++) {
										SimpleFeature sf = (SimpleFeature) items.get(i);
										if (sf != null) {
											((DefaultFeatureCollection) dfc).add(sf);
										}
									}
									Filter intFilter = ff.intersects(ff.property("the_geom"), ff.literal(envelope));
									dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);

									DefaultFeatureCollection tfc = new DefaultFeatureCollection();
									SimpleFeatureIterator iter = dfc.features();
									while (iter.hasNext()) {
										SimpleFeature sf = (SimpleFeature) iter.next();
										String id = sf.getID();
										if (!validateMap.containsKey(id)) {
											validateMap.put(id, null);
											tfc.add(sf);
										}
									}
									iter.close();
									dfc = null;// 객체 초기화
									BasicDTLayer targetLayer = new BasicDTLayer();
									targetLayer.setSimpleFeatureCollection(tfc);
									targetLayer.setLayerID(dtLayer.getLayerID());
									targetLayer.setFilter(dtLayer.getFilter());
									targetLayer.setTypeName(typeName);
									targetLayer.setLayerType(dtLayer.getLayerType());
									OpenLayerValidator targetLayerValidator = new OpenLayerValidator(targetLayer,
											langType);
									ErrorLayer typeErr = targetLayerValidator.validateEntityDuplicated(filter);
									if (typeErr != null) {
										errLayer.mergeErrorLayer(typeErr);
										typeErr = null;
									}
									targetLayerValidator = null;
									targetLayer = null;
								}
								items = null;
							}
							validateMap = null;
							gridEnvs = null;
						}
						// 등고선 꺾임 오류 (ConOverDegree)
						if (optionName.equals("ConOverDegreeB")) {
							OpenLayerValidator targetLayerValidator = new OpenLayerValidator(dtLayer, langType);
							ErrorLayer typeErr = targetLayerValidator.validateConOverDegree(tolerance);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
								typeErr = null;
							}
							targetLayerValidator = null;
						}
						// 등고선교차오류 (ConIntersected)
						if (optionName.equals("ConIntersectedB")) {
							Envelope wholeEnv = tarBounds;
							List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);
							for (Envelope envelope : gridEnvs) {
								List items = targetQuad.query(envelope);
								SimpleFeatureCollection dfc = new DefaultFeatureCollection();
								int size = items.size();
								if (size != 0) {
									for (int i = 0; i < size; i++) {
										SimpleFeature sf = (SimpleFeature) items.get(i);
										if (sf != null) {
											((DefaultFeatureCollection) dfc).add(sf);
										}
									}
									Filter intFilter = ff.intersects(ff.property("the_geom"), ff.literal(envelope));
									dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);

									DefaultFeatureCollection tfc = new DefaultFeatureCollection();
									SimpleFeatureIterator iter = dfc.features();
									while (iter.hasNext()) {
										SimpleFeature isf = getIntersection(envelope, (SimpleFeature) iter.next());
										if (isf != null) {
											tfc.add(isf);
										}
									}
									iter.close();
									dfc = null;// 객체 초기화

									BasicDTLayer targetLayer = new BasicDTLayer();
									targetLayer.setSimpleFeatureCollection(tfc);
									targetLayer.setLayerID(dtLayer.getLayerID());
									targetLayer.setFilter(dtLayer.getFilter());
									targetLayer.setTypeName(typeName);
									targetLayer.setLayerType(dtLayer.getLayerType());
									targetLayer.setQuadTree(targetQuad);
									OpenLayerValidator targetLayerValidator = new OpenLayerValidator(targetLayer,
											langType);
									ErrorLayer typeErr = targetLayerValidator.validateConIntersected(filter, envelope,
											100);
									if (typeErr != null) {
										errLayer.mergeErrorLayer(typeErr);
										typeErr = null;
									}
									targetLayerValidator = null;
									tfc = null;
									targetLayer = null;
								}
								items = null;
							}
							gridEnvs = null;
						}
						// 등고선 끊김오류 (ConBreak)
						if (optionName.equals("ConBreakB")) {
							double bufferValue = 0.1;
							BasicDTLayerList reDTLayers = new BasicDTLayerList();
							for (OptionRelation relation : relations) {
								BasicDTLayerList typeReLayers = getRelationTypeDTLayers(dtLayer, relation,
										validateLayerTypeList);
								if (typeReLayers != null) {
									reDTLayers.addAll(typeReLayers);
								}
							}
							// 관계레이어 쿼드트리 생성
							for (BasicDTLayer reDtLayer : reDTLayers) {
								reDtLayer.buildQuad();
							}
							Envelope wholeEnv = tarBounds;
							List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);
							for (Envelope envelope : gridEnvs) {
								List items = targetQuad.query(envelope);
								SimpleFeatureCollection dfc = new DefaultFeatureCollection();
								int size = items.size();
								if (size != 0) {
									for (int i = 0; i < size; i++) {
										SimpleFeature sf = (SimpleFeature) items.get(i);
										if (sf != null) {
											((DefaultFeatureCollection) dfc).add(sf);
										}
									}
									Filter intFilter = ff.intersects(ff.property("the_geom"), ff.literal(envelope));
									dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);

									int dfcSize = dfc.size();
									if (dfcSize != 0) {
										Geometry geom = new GeometryFactory().toGeometry(envelope);
										Geometry bEnvelope = geom.buffer(bufferValue);
										DefaultFeatureCollection tfc = new DefaultFeatureCollection();
										SimpleFeatureIterator iter = dfc.features();
										while (iter.hasNext()) {
											SimpleFeature isf = getGeomIntersection(bEnvelope,
													(SimpleFeature) iter.next());
											if (isf != null) {
												tfc.add(isf);
											}
										}
										iter.close();
										dfc = null;// 객체 초기화
										// target레이어 생성
										BasicDTLayer targetLayer = new BasicDTLayer();
										targetLayer.setSimpleFeatureCollection(tfc);
										targetLayer.setLayerID(dtLayer.getLayerID());
										targetLayer.setFilter(dtLayer.getFilter());
										targetLayer.setFigure(dtLayer.getFigure());
										targetLayer.setTypeName(typeName);
										targetLayer.setLayerType(dtLayer.getLayerType());
										// 관계레이어 생성
										BasicDTLayerList interReLayers = new BasicDTLayerList();
										Filter interFilter = ff.intersects(ff.property("the_geom"),
												ff.literal(bEnvelope));
										for (BasicDTLayer dtRLayer : reDTLayers) {
											List reItems = dtRLayer.getQuadTree().query(envelope);
											SimpleFeatureCollection reDfc = new DefaultFeatureCollection();
											int reSize = reItems.size();
											if (reSize > 0) {
												for (int i = 0; i < reSize; i++) {
													SimpleFeature sf = (SimpleFeature) reItems.get(i);
													if (sf != null) {
														((DefaultFeatureCollection) reDfc).add(sf);
													}
												}
											}
											reDfc = (SimpleFeatureCollection) reDfc.subCollection(interFilter);
											int reDfcSize = reDfc.size();
											if (reDfcSize != 0) {
												BasicDTLayer interReLayer = new BasicDTLayer();
												SimpleFeatureIterator reIter = reDfc.features();
												DefaultFeatureCollection reTarDfc = new DefaultFeatureCollection();
												while (reIter.hasNext()) {
													SimpleFeature sf = getIntersection(envelope,
															(SimpleFeature) reIter.next());
													if (sf != null) {
														reTarDfc.add(sf);
													}
												}
												reIter.close();
												reDfc = null;
												interReLayer.setLayerID(dtRLayer.getLayerID());
												interReLayer.setFilter(dtRLayer.getFilter());
												interReLayer.setLayerType(dtRLayer.getLayerType());
												interReLayer.setTypeName(dtRLayer.getTypeName());
												interReLayer.setSimpleFeatureCollection(reTarDfc);
												interReLayers.add(interReLayer);
											}
											reItems = null;
										}
										if (reDTLayers != null) {
											OpenLayerValidator layerValidator = new OpenLayerValidator(targetLayer,
													langType);
											ErrorLayer typeErr = layerValidator.validateConBreak(filter, tolerance,
													figure, interReLayers, bEnvelope);
											if (typeErr != null) {
												errLayer.mergeErrorLayer(typeErr);
												typeErr = null;
											}
											interReLayers = null;
											layerValidator = null;
										}
									}
								}
								items = null;
							}
							reDTLayers = null;
							gridEnvs = null;
						}
						// 등고선 직선화미처리오류(UselessPoint)
						if (optionName.equals("UselessPoint")) {
							OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
							ErrorLayer typeErr = layerValidator.validateUselessPoint(filter);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
								typeErr = null;
							}
							layerValidator = null;
						}
						// 중복점오류(DuplicatedPoint)
						if (optionName.equals("PointDuplicatedB")) {
							OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
							ErrorLayer typeErr = layerValidator.validatePointDuplicated(filter);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
								typeErr = null;
							}
							layerValidator = null;
						}
						// 경계초과오류 (OutBoundary)
						if (optionName.equals("OutBoundaryB")) {
							if (relations != null) {
								BasicDTLayerList reDTLayers = new BasicDTLayerList();
								for (OptionRelation relation : relations) {
									BasicDTLayerList typeReLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (typeReLayers != null) {
										reDTLayers.addAll(typeReLayers);
									}
								}
								// 관계레이어 쿼드트리 생성
								for (BasicDTLayer reDtLayer : reDTLayers) {
									reDtLayer.buildQuad();
								}
								Envelope wholeEnv = tarBounds;
								List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);
								for (Envelope envelope : gridEnvs) {
									List items = targetQuad.query(envelope);
									SimpleFeatureCollection dfc = new DefaultFeatureCollection();
									int size = items.size();
									if (size != 0) {
										for (int i = 0; i < size; i++) {
											SimpleFeature sf = (SimpleFeature) items.get(i);
											if (sf != null) {
												((DefaultFeatureCollection) dfc).add(sf);
											}
										}
										Filter intFilter = ff.intersects(ff.property("the_geom"), ff.literal(envelope));
										dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);
										DefaultFeatureCollection tfc = new DefaultFeatureCollection();
										SimpleFeatureIterator iter = dfc.features();
										int dfcSize = dfc.size();
										if (dfcSize != 0) {
											while (iter.hasNext()) {
												SimpleFeature isf = getIntersection(envelope,
														(SimpleFeature) iter.next());
												if (isf != null) {
													tfc.add(isf);
												}
											}
											iter.close();
											dfc = null;// 객체 초기화
											BasicDTLayer targetLayer = new BasicDTLayer();
											targetLayer.setSimpleFeatureCollection(tfc);
											targetLayer.setLayerID(dtLayer.getLayerID());
											targetLayer.setFilter(dtLayer.getFilter());
											targetLayer.setTypeName(typeName);
											targetLayer.setLayerType(dtLayer.getLayerType());
											BasicDTLayerList reTarDTLayers = new BasicDTLayerList();
											for (BasicDTLayer reDTLayer : reDTLayers) {
												List reItems = reDTLayer.getQuadTree().query(envelope);
												SimpleFeatureCollection reDfc = new DefaultFeatureCollection();
												int reSize = reItems.size();
												if (reSize > 0) {
													for (int i = 0; i < reSize; i++) {
														SimpleFeature sf = (SimpleFeature) reItems.get(i);
														if (sf != null) {
															((DefaultFeatureCollection) reDfc).add(sf);
														}
													}
													Filter reFilter = ff.intersects(ff.property("the_geom"),
															ff.literal(envelope));

													reDfc = (SimpleFeatureCollection) reDfc.subCollection(reFilter);
													int reDfcSize = reDfc.size();
													if (reDfcSize != 0) {
														SimpleFeatureIterator reIter = reDfc.features();
														DefaultFeatureCollection reTarDfc = new DefaultFeatureCollection();
														while (reIter.hasNext()) {
															SimpleFeature sf = getIntersection(envelope,
																	(SimpleFeature) reIter.next());
															if (sf != null) {
																reTarDfc.add(sf);
															}
														}
														reIter.close();
														reDfc = null;
														BasicDTLayer reTarDTLayer = new BasicDTLayer();
														reTarDTLayer.setLayerID(reDTLayer.getLayerID());
														reTarDTLayer.setFigure(reDTLayer.getFigure());
														reTarDTLayer.setFilter(reDTLayer.getFilter());
														reTarDTLayer.setLayerType(reDTLayer.getLayerType());
														reTarDTLayer.setSimpleFeatureCollection(reTarDfc);
														reTarDTLayer.setTypeName(reDTLayer.getTypeName());
														reTarDTLayers.add(reTarDTLayer);
													}
												}
												reItems = null;
											}
											OpenLayerValidator targetLayerValidator = new OpenLayerValidator(
													targetLayer, langType);
											ErrorLayer typeErr = targetLayerValidator.validateOutBoundary(filter,
													tolerance, reTarDTLayers);
											if (typeErr != null) {
												errLayer.mergeErrorLayer(typeErr);
												typeErr = null;
											}
											targetLayerValidator = null;
										}
									}
									items = null;
								}
								gridEnvs = null;
								reDTLayers = null;
							}
						}
						// 노드오류 (NodeMiss)
						if (optionName.equals("NodeMissB")) {
							if (relations != null) {
								BasicDTLayer targetLayer = new BasicDTLayer();
								targetLayer.setLayerID(dtLayer.getLayerID());
								targetLayer.setTypeName(typeName);
								targetLayer.setLayerType(dtLayer.getLayerType());
								Envelope wholeEnv = tarBounds;
								for (OptionRelation relation : relations) {
									String reTypeName = relation.getName();
									BasicDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (BasicDTLayer reDTLayer : reDTLayers) {
											SimpleFeatureCollection reQuadSfc = null;
											Quadtree reQuadtree = null;
											if (reDTLayer.getLayerID().equals(layerName)) {
												reQuadtree = targetQuad;
												reQuadSfc = targetQuadSfc;
											} else {
												reQuadSfc = reDTLayer.getSimpleFeatureCollection();
												Quadtree quad = getQuadTree(reQuadSfc);
												reQuadtree = quad;
											}
											BasicDTLayer retargetLayer = new BasicDTLayer();
											retargetLayer.setLayerID(reDTLayer.getLayerID());
											retargetLayer.setTypeName(reTypeName);
											retargetLayer.setFilter(reDTLayer.getFilter());
											retargetLayer.setLayerType(reDTLayer.getLayerType());

											List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);
											for (Envelope envelope : gridEnvs) {
												Geometry envelpoeGeom = new GeometryFactory().toGeometry(envelope);
												List items = targetQuad.query(envelope);
												SimpleFeatureCollection dfc = new DefaultFeatureCollection();
												int size = items.size();
												if (size != 0) {
													for (int i = 0; i < size; i++) {
														SimpleFeature sf = (SimpleFeature) items.get(i);
														if (sf != null) {
															((DefaultFeatureCollection) dfc).add(sf);
														}
													}
													Filter intFilter = ff.intersects(ff.property("the_geom"),
															ff.literal(envelope));
													dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);

													int dfcSize = dfc.size();
													if (dfcSize != 0) {
														DefaultFeatureCollection tfc = new DefaultFeatureCollection();
														SimpleFeatureIterator iter = dfc.features();
														while (iter.hasNext()) {
															SimpleFeature isf = getIntersection(envelope,
																	(SimpleFeature) iter.next());
															if (isf != null) {
																tfc.add(isf);
															}
														}
														iter.close();
														dfc = null;// 객체 초기화

														targetLayer.setSimpleFeatureCollection(tfc);
														List reItems = reQuadtree.query(envelope);
														SimpleFeatureCollection reDfc = new DefaultFeatureCollection();
														int reSize = reItems.size();
														if (reSize > 0) {
															for (int i = 0; i < reSize; i++) {
																SimpleFeature sf = (SimpleFeature) reItems.get(i);
																if (sf != null) {
																	((DefaultFeatureCollection) reDfc).add(sf);
																}
															}
															Filter reFilter = ff.intersects(ff.property("the_geom"),
																	ff.literal(envelope));
															reDfc = (SimpleFeatureCollection) reDfc
																	.subCollection(reFilter);

															int reDfcSize = reDfc.size();
															if (reDfcSize != 0) {
																DefaultFeatureCollection retDfc = new DefaultFeatureCollection();
																SimpleFeatureIterator reiter = reDfc.features();

																while (reiter.hasNext()) {
																	SimpleFeature isf = getIntersection(envelope,
																			(SimpleFeature) reiter.next());
																	if (isf != null) {
																		retDfc.add(isf);
																	}
																}
																reiter.close();
																reDfc = null;
																retargetLayer.setSimpleFeatureCollection(retDfc);
																OpenLayerValidator layerValidator = new OpenLayerValidator(
																		targetLayer, langType);
																ErrorLayer typeErr = layerValidator.validateNodeMiss(
																		filter, tolerance, figure, retargetLayer,
																		envelpoeGeom);
																if (typeErr != null) {
																	errLayer.mergeErrorLayer(typeErr);
																	typeErr = null;
																}
																layerValidator = null;
															}
														}
														reItems = null;
													}
												}
												items = null;
											}
											gridEnvs = null;
											retargetLayer = null;
										}
										reDTLayers = null;
									}
								}
								targetLayer = null;
							}
						}
						// 기준점 초과오류 (OverShoot)
						if (optionName.equals("OverShootB")) {
							Envelope wholeEnv = tarBounds;
							for (Object result : results) {
								Envelope envelope = (Envelope) result;
								if (envelope != null) {
									quadIndexWidth = envelope.getMaxX() - envelope.getMinX();
									break;
								}
							}
							List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);
							for (Envelope envelope : gridEnvs) {
								List items = targetQuad.query(envelope);
								SimpleFeatureCollection dfc = new DefaultFeatureCollection();
								int size = items.size();
								if (size != 0) {
									for (int i = 0; i < size; i++) {
										SimpleFeature sf = (SimpleFeature) items.get(i);
										if (sf != null) {
											((DefaultFeatureCollection) dfc).add(sf);
										}
									}
									Filter intFilter = ff.intersects(ff.property("the_geom"), ff.literal(envelope));
									dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);
									int dfcSize = dfc.size();
									if (dfcSize != 0) {
										DefaultFeatureCollection tfc = new DefaultFeatureCollection();
										SimpleFeatureIterator iter = dfc.features();
										while (iter.hasNext()) {
											tfc.add((SimpleFeature) iter.next());
										}
										iter.close();
										dfc = null;// 객체 초기화
										BasicDTLayer targetLayer = new BasicDTLayer();
										targetLayer.setSimpleFeatureCollection(tfc);
										targetLayer.setLayerID(dtLayer.getLayerID());
										targetLayer.setFilter(dtLayer.getFilter());
										targetLayer.setTypeName(typeName);
										targetLayer.setLayerType(dtLayer.getLayerType());
										OpenLayerValidator targetLayerValidator = new OpenLayerValidator(targetLayer,
												langType);
										ErrorLayer typeErr = targetLayerValidator.validateOverShoot(filter, tolerance);
										if (typeErr != null) {
											errLayer.mergeErrorLayer(typeErr);
											typeErr = null;
										}
										targetLayerValidator = null;
										targetLayer = null;
									}
								}
								items = null;
							}
							gridEnvs = null;
						}
						// 폴리곤 꼬임 오류 (InvalidPolygon)
						if (optionName.equals("EntityTwisted")) {
							Envelope wholeEnv = tarBounds;
							List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);
							Map<String, Object> validateMap = new HashMap<>();
							for (Envelope envelope : gridEnvs) {
								List items = targetQuad.query(envelope);
								SimpleFeatureCollection dfc = new DefaultFeatureCollection();
								int size = items.size();
								if (size != 0) {
									for (int i = 0; i < size; i++) {
										SimpleFeature sf = (SimpleFeature) items.get(i);
										if (sf != null) {
											((DefaultFeatureCollection) dfc).add(sf);
										}
									}
									Filter intFilter = ff.intersects(ff.property("the_geom"), ff.literal(envelope));
									dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);

									DefaultFeatureCollection tfc = new DefaultFeatureCollection();
									SimpleFeatureIterator iter = dfc.features();
									while (iter.hasNext()) {
										SimpleFeature sf = (SimpleFeature) iter.next();
										String id = sf.getID();
										if (!validateMap.containsKey(id)) {
											validateMap.put(id, null);
											tfc.add(sf);
										}
									}
									iter.close();
									dfc = null;// 객체 초기화
									BasicDTLayer targetLayer = new BasicDTLayer();
									targetLayer.setSimpleFeatureCollection(tfc);
									targetLayer.setLayerID(dtLayer.getLayerID());
									targetLayer.setFilter(dtLayer.getFilter());
									targetLayer.setTypeName(typeName);
									targetLayer.setLayerType(dtLayer.getLayerType());
									OpenLayerValidator targetLayerValidator = new OpenLayerValidator(targetLayer,
											langType);
									ErrorLayer typeErr = targetLayerValidator.validateEntityTwisted(filter);
									if (typeErr != null) {
										errLayer.mergeErrorLayer(typeErr);
										typeErr = null;
									}
									targetLayerValidator = null;
									targetLayer = null;
								}
								items = null;
							}
							validateMap = null;
							gridEnvs = null;
						}
						// 인접요소부재오류 (RefEntityNone)
						if (optionName.equals("RefEntityNone")) {
							Envelope wholeEnv = tarBounds;
							List<Envelope> gridEnvs = getGrids(wholeEnv, quadIndexWidth);
							for (Envelope envelope : gridEnvs) {
								Geometry geom = new GeometryFactory().toGeometry(envelope);
								List items = targetQuad.query(envelope);
								SimpleFeatureCollection dfc = new DefaultFeatureCollection();
								int size = items.size();
								if (size != 0) {
									for (int i = 0; i < size; i++) {
										SimpleFeature sf = (SimpleFeature) items.get(i);
										if (sf != null) {
											((DefaultFeatureCollection) dfc).add(sf);
										}
									}
									Filter intFilter = ff.intersects(ff.property("the_geom"), ff.literal(envelope));
									dfc = (SimpleFeatureCollection) dfc.subCollection(intFilter);
									int dfcSize = dfc.size();
									if (dfcSize != 0) {
										DefaultFeatureCollection tfc = new DefaultFeatureCollection();
										SimpleFeatureIterator iter = dfc.features();
										while (iter.hasNext()) {
											SimpleFeature isf = getIntersection(envelope, (SimpleFeature) iter.next());
											if (isf != null) {
												tfc.add(isf);
											}
										}
										iter.close();
										dfc = null;// 객체 초기화
										BasicDTLayer targetLayer = new BasicDTLayer();
										targetLayer.setSimpleFeatureCollection(tfc);
										targetLayer.setLayerID(dtLayer.getLayerID());
										targetLayer.setFilter(dtLayer.getFilter());
										targetLayer.setTypeName(typeName);
										targetLayer.setLayerType(dtLayer.getLayerType());
										OpenLayerValidator targetLayerValidator = new OpenLayerValidator(targetLayer,
												langType);
										ErrorLayer typeErr = targetLayerValidator.validateRefEntityNone(filter,
												tolerance, geom);
										if (typeErr != null) {
											errLayer.mergeErrorLayer(typeErr);
											typeErr = null;
										}
										targetLayerValidator = null;
										targetLayer = null;
									}
								}
								items = null;
							}
							gridEnvs = null;
						}
					}
				}
				targetQuad = null;
				targetQuadSfc = null;
				dtLayer = null;
			}
		}
		if (errLayer.getErrFeatureList().size() > 0) {
			return errLayer;
		} else {
			return null;
		}
	}

	public BasicDTLayer getDTLayer(String filePath, String fileName) {

		BasicDTLayer dtLayer = new BasicDTLayer();
		File layerFile = new File(filePath + File.separator + fileName + ".shp");
		SimpleFeatureCollection sfc = new SHPFileLayerParser().getShpObject(layerFile);
		if (sfc != null) {
			try {
				if (sfc.size() > 0) {
					SimpleFeatureType featureType = sfc.getSchema();
					GeometryType geometryType = featureType.getGeometryDescriptor().getType();
					String geomType = geometryType.getBinding().getSimpleName().toString();
					dtLayer.setLayerID(fileName);
					dtLayer.setLayerType(geomType);
					dtLayer.setSimpleFeatureCollection(sfc);
					return dtLayer;
				} else {
					sfc = null;
					return null;
				}
			} catch (Exception e) {
				return null;
			}
		} else {
			return null;
		}
	}

	private Quadtree getQuadTree(SimpleFeatureCollection sfc) {

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

	public BasicDTLayerList getRelationTypeDTLayers(BasicDTLayer dtLayer, OptionRelation relation,
			QALayerTypeList qaTypes) {

		BasicDTLayerList reDTLayers = new BasicDTLayerList();

		String layerName = dtLayer.getLayerID();
		String reLayerType = relation.getName();
		QALayerType reQaType = null;
		for (QALayerType innerQaType : qaTypes) {
			String innerTypeName = innerQaType.getName();
			if (reLayerType.equals(innerTypeName)) {
				reQaType = innerQaType;
				break;
			}
		}
		if (reQaType != null) {
			// other type
			List<OptionFilter> reFilters = relation.getFilters();
			if (reFilters != null) {
				for (OptionFilter refilter : reFilters) {
					String refilterCode = refilter.getCode();
					if (refilterCode.equals(layerName)) {
						BasicDTLayer reDtLayer = dtLayer;
						reDTLayers.add(reDtLayer);
					}
				}
			} else {
				List<String> reLayerIDs = reQaType.getLayerIDList();
				for (String reLayerId : reLayerIDs) {
					if (reLayerId.equals(layerName)) {
						BasicDTLayer reDtLayer = dtLayer;
						reDTLayers.add(reDtLayer);
					}
				}
			}
		}
		if (reDTLayers.size() > 0) {
			for (BasicDTLayer tmp : reDTLayers) {
				String code = tmp.getLayerID();
				tmp.setFilter(relation.getFilter(code));
				tmp.setFigure(relation.getFigure(code));
			}
			return reDTLayers;
		} else {
			return null;
		}
	}

	private SimpleFeature getIntersection(Envelope envelope, SimpleFeature sf) {

		SimpleFeature resultSF = SimpleFeatureBuilder.copy(sf);
		Geometry envelpoeGeom = f.toGeometry(envelope);
		Geometry sfGeom = (Geometry) sf.getDefaultGeometry();
		Geometry interGeom = null;
		try {
			interGeom = envelpoeGeom.intersection(sfGeom);
			if (interGeom != null) {
				resultSF.setDefaultGeometry(interGeom);
			}
		} catch (TopologyException e) {
			return resultSF;
		} finally {
			return resultSF;
		}
	}

	@SuppressWarnings({ "finally", "unused" })
	private SimpleFeature getGeomIntersection(Geometry envelope, SimpleFeature sf) {

		SimpleFeature resultSF = SimpleFeatureBuilder.copy(sf);
		Geometry sfGeom = (Geometry) sf.getDefaultGeometry();
		Geometry interGeom = null;
		try {
			interGeom = envelope.intersection(sfGeom);
			if (interGeom != null) {
				resultSF.setDefaultGeometry(interGeom);
			}
		} catch (TopologyException e) {
			return resultSF;
		} finally {
			return resultSF;
		}
	}

	private List<Envelope> getGrids(Envelope envel, double value) throws SchemaException {

		List<Envelope> resultRefEnl = new ArrayList<Envelope>();
		if (value < 512 || 2048 > value) {
			value = 2048;// 기본값 설정
		}

		// tmp
		value = 0.5;

		for (double y = envel.getMinY(); y < envel.getMaxY(); y += value) {
			for (double x = envel.getMinX(); x < envel.getMaxX(); x += value) {
				Envelope envelope = new Envelope(x, x + value, y, y + value);
				resultRefEnl.add(envelope);
			}
		}
		return resultRefEnl;
	}
}