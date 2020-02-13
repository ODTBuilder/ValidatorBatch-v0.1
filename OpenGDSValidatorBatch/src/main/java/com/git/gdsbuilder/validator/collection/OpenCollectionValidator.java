package com.git.gdsbuilder.validator.collection;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.factory.CommonFactoryFinder;
import org.geotools.feature.DefaultFeatureCollection;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.ReferencedEnvelope;
import org.geotools.grid.Grids;
import org.geotools.util.NullProgressListener;
import org.json.simple.JSONArray;
import org.opengis.feature.Feature;
import org.opengis.feature.FeatureVisitor;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.filter.Filter;
import org.opengis.filter.FilterFactory2;

import com.git.batch.service.BathService;
import com.git.batch.step.Progress;
import com.git.gdsbuilder.type.dt.layer.OpenDTLayer;
import com.git.gdsbuilder.type.dt.layer.OpenDTLayerList;
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
import com.git.gdsbuilder.validator.quad.Quadtree;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.TopologyException;

public class OpenCollectionValidator {

	protected Progress pb;
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
			List<String> layerNames = qaType.getLayerIDList();
			for (String layerName : layerNames) {
				// target layer
				OpenDTLayer dtLayer = getDTLayer(fileDir, layerName);
				if (dtLayer == null) {
					continue;
				}
				List<AttributeMiss> attrMissArr = qaOption.getAttributeMissOptions();
				if (attrMissArr != null) {
					for (AttributeMiss attrMiss : attrMissArr) {
						String optionName = attrMiss.getOption();
						System.out.print(layerName + "-" + optionName);
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
							BathService.pb.updateProgress();
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
							BathService.pb.updateProgress();
						}
						// 고도값오류 (Z-Value Abmiguous)
						if (optionName.equals("ZValueAmbiguous2")) {
							OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType);
							ErrorLayer typeErr = layerValidator.validateZvalueAmbiguous(filter, figure);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
							}
							layerValidator = null;
							BathService.pb.updateProgress();
						}
						// 인접속성오류 (RefAttributeMiss)
						if (optionName.equals("RefAttributeMissB")) {
							SimpleFeatureCollection targetQuadSfc = dtLayer.getSimpleFeatureCollection();
							Quadtree targetQuad = getQuadTree(targetQuadSfc);
							List<Envelope> gridEnvs = new ArrayList<>();
							ReferencedEnvelope tarBounds = targetQuadSfc.getBounds();
							SimpleFeatureSource grid = Grids.createSquareGrid(tarBounds, 0.05);
							SimpleFeatureIterator gridIter = grid.getFeatures().features();
							while (gridIter.hasNext()) {
								SimpleFeature sf = gridIter.next();
								Geometry gridGeom = (Geometry) sf.getDefaultGeometry();
								gridEnvs.add(gridGeom.getEnvelopeInternal());
							}
							FilterFactory2 ff = CommonFactoryFinder.getFilterFactory2();
							if (relations != null) {
								OpenDTLayer targetLayer = new OpenDTLayer();
								targetLayer.setLayerID(dtLayer.getLayerID());
								targetLayer.setTypeName(typeName);
								targetLayer.setLayerType(dtLayer.getLayerType());
								for (OptionRelation relation : relations) {
									String reTypeName = relation.getName();
									OpenDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (OpenDTLayer reDTLayer : reDTLayers) {
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
											OpenDTLayer retargetLayer = new OpenDTLayer();
											retargetLayer.setLayerID(reDTLayer.getLayerID());
											retargetLayer.setTypeName(reTypeName);
											retargetLayer.setFilter(reDTLayer.getFilter());
											retargetLayer.setLayerType(reDTLayer.getLayerType());
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
											BathService.pb.updateProgress();
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
							BathService.pb.updateProgress();
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
				OpenDTLayer dtLayer = getDTLayer(fileDir, layerName);
				if (dtLayer == null) {
					continue;
				}
				dtLayer.setTypeName(typeName);
				SimpleFeatureCollection targetQuadSfc = dtLayer.getSimpleFeatureCollection();
				Quadtree targetQuad = getQuadTree(targetQuadSfc);

				List<Envelope> gridEnvs = new ArrayList<>();
				ReferencedEnvelope tarBounds = targetQuadSfc.getBounds();
				double h = tarBounds.getHeight() / 150;
				SimpleFeatureSource grid = Grids.createSquareGrid(tarBounds, h);
				SimpleFeatureIterator gridIter = grid.getFeatures().features();
				while (gridIter.hasNext()) {
					SimpleFeature sf = gridIter.next();
					Geometry gridGeom = (Geometry) sf.getDefaultGeometry();
					gridEnvs.add(gridGeom.getEnvelopeInternal());
				}

				QAOption qaOption = qaType.getOption();
				List<GraphicMiss> grapMissArr = qaOption.getGraphicMissOptions();
				if (grapMissArr != null) {
					for (GraphicMiss grapMiss : grapMissArr) {
						String optionName = grapMiss.getOption();
						System.out.print(layerName + "-" + optionName);
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
							BathService.pb.updateProgress();
						}
						// 허용범위 이하 길이 (SmallLength)
						if (optionName.equals("SmallLengthB")) {
							OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType, epsg);
							ErrorLayer typeErr = layerValidator.validateSmallLength(filter, tolerance);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
								typeErr = null;
							}
							layerValidator = null;
							BathService.pb.updateProgress();
						}
						// 허용범위 이하 면적 (SmallArea)
						if (optionName.equals("SmallAreaB")) {
							OpenLayerValidator layerValidator = new OpenLayerValidator(dtLayer, langType, epsg);
							ErrorLayer typeErr = layerValidator.validateSmallArea(filter, tolerance);
							if (typeErr != null) {
								errLayer.mergeErrorLayer(typeErr);
								typeErr = null;
							}
							layerValidator = null;
							BathService.pb.updateProgress();
						}
						// 단독존재오류 (Self Entity)
						if (optionName.equals("SelfEntityB")) {
							if (relations != null) {
								OpenDTLayer targetLayer = new OpenDTLayer();
								targetLayer.setLayerID(dtLayer.getLayerID());
								targetLayer.setFilter(dtLayer.getFilter());
								targetLayer.setTypeName(typeName);
								targetLayer.setLayerType(dtLayer.getLayerType());
								for (OptionRelation relation : relations) {
									String reTypeName = relation.getName();
									OpenDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (OpenDTLayer reDTLayer : reDTLayers) {
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
											OpenDTLayer retargetLayer = new OpenDTLayer();
											retargetLayer.setLayerID(reDTLayer.getLayerID());
											retargetLayer.setTypeName(reTypeName);
											retargetLayer.setFilter(reDTLayer.getFilter());
											retargetLayer.setLayerType(reDTLayer.getLayerType());

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
																			targetLayer, langType);
																	if (typeName.equals(reTypeName)) {
																		ErrorLayer typeErr = layerValidator
																				.validateSelfEntity(filter, tolerance,
																						envelope, 100);
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
											retargetLayer = null;
											BathService.pb.updateProgress();
										}
										reDTLayers = null;
									}
								}
								targetLayer = null;
							}
						}
						// 요소중복오류 (EntityDuplicated)
						if (optionName.equals("EntityDuplicatedB")) {
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
									OpenDTLayer targetLayer = new OpenDTLayer();
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
							BathService.pb.updateProgress();
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
							BathService.pb.updateProgress();
						}
						// 등고선교차오류 (ConIntersected)
						if (optionName.equals("ConIntersectedB")) {
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

									OpenDTLayer targetLayer = new OpenDTLayer();
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
							BathService.pb.updateProgress();
						}
						// 등고선 끊김오류 (ConBreak)
						if (optionName.equals("ConBreakB")) {
							double bufferValue = 0.1;
							OpenDTLayerList reDTLayers = new OpenDTLayerList();
							for (OptionRelation relation : relations) {
								OpenDTLayerList typeReLayers = getRelationTypeDTLayers(dtLayer, relation,
										validateLayerTypeList);
								if (typeReLayers != null) {
									reDTLayers.addAll(typeReLayers);
								}
							}
							// 관계레이어 쿼드트리 생성
							for (OpenDTLayer reDtLayer : reDTLayers) {
								reDtLayer.buildQuad();
							}
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
										OpenDTLayer targetLayer = new OpenDTLayer();
										targetLayer.setSimpleFeatureCollection(tfc);
										targetLayer.setLayerID(dtLayer.getLayerID());
										targetLayer.setFilter(dtLayer.getFilter());
										targetLayer.setFigure(dtLayer.getFigure());
										targetLayer.setTypeName(typeName);
										targetLayer.setLayerType(dtLayer.getLayerType());
										// 관계레이어 생성
										OpenDTLayerList interReLayers = new OpenDTLayerList();
										Filter interFilter = ff.intersects(ff.property("the_geom"),
												ff.literal(bEnvelope));
										for (OpenDTLayer dtRLayer : reDTLayers) {
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
												OpenDTLayer interReLayer = new OpenDTLayer();
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
							BathService.pb.updateProgress();
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
							BathService.pb.updateProgress();
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
							BathService.pb.updateProgress();
						}
						// 경계초과오류 (OutBoundary)
						if (optionName.equals("OutBoundaryB")) {
							if (relations != null) {
								Map<String, Object> validateMap = new HashMap<>();
								OpenDTLayerList reDTLayers = new OpenDTLayerList();
								for (OptionRelation relation : relations) {
									OpenDTLayerList typeReLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (typeReLayers != null) {
										reDTLayers.addAll(typeReLayers);
									}
								}
								// 관계레이어 쿼드트리 생성
								for (OpenDTLayer reDtLayer : reDTLayers) {
									reDtLayer.buildQuad();
								}
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
												SimpleFeature isf = iter.next();
												String id = isf.getID();
												if (!validateMap.containsKey(id)) {
													validateMap.put(id, null);
													tfc.add(isf);
												}
											}
											iter.close();
											dfc = null;// 객체 초기화
											OpenDTLayer targetLayer = new OpenDTLayer();
											targetLayer.setSimpleFeatureCollection(tfc);
											targetLayer.setLayerID(dtLayer.getLayerID());
											targetLayer.setFilter(dtLayer.getFilter());
											targetLayer.setTypeName(typeName);
											targetLayer.setLayerType(dtLayer.getLayerType());
											OpenDTLayerList reTarDTLayers = new OpenDTLayerList();
											for (OpenDTLayer reDTLayer : reDTLayers) {
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
														OpenDTLayer reTarDTLayer = new OpenDTLayer();
														reTarDTLayer.setLayerID(reDTLayer.getLayerID());
														reTarDTLayer.setFigure(reDTLayer.getFigure());
														reTarDTLayer.setFilter(reDTLayer.getFilter());
														reTarDTLayer.setLayerType(reDTLayer.getLayerType());
														reTarDTLayer.setSimpleFeatureCollection(reDfc);
														reTarDTLayer.setTypeName(reDTLayer.getTypeName());
														reTarDTLayers.add(reTarDTLayer);
													}
												}
												reItems = null;
											}
											OpenLayerValidator targetLayerValidator = new OpenLayerValidator(
													targetLayer, langType);
											ErrorLayer typeErr = targetLayerValidator.validateOutBoundary(filter,
													tolerance, reTarDTLayers, envelope, 100);
											if (typeErr != null) {
												errLayer.mergeErrorLayer(typeErr);
												typeErr = null;
											}
											targetLayerValidator = null;
										}
									}
									items = null;
								}
								reDTLayers = null;
							}
							BathService.pb.updateProgress();
						}
						// 노드오류 (NodeMiss)
						if (optionName.equals("NodeMissB")) {
							if (relations != null) {
								OpenDTLayer targetLayer = new OpenDTLayer();
								targetLayer.setLayerID(dtLayer.getLayerID());
								targetLayer.setTypeName(typeName);
								targetLayer.setLayerType(dtLayer.getLayerType());
								for (OptionRelation relation : relations) {
									String reTypeName = relation.getName();
									OpenDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (OpenDTLayer reDTLayer : reDTLayers) {
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
											OpenDTLayer retargetLayer = new OpenDTLayer();
											retargetLayer.setLayerID(reDTLayer.getLayerID());
											retargetLayer.setTypeName(reTypeName);
											retargetLayer.setFilter(reDTLayer.getFilter());
											retargetLayer.setLayerType(reDTLayer.getLayerType());

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
											retargetLayer = null;
										}
										reDTLayers = null;
									}
									BathService.pb.updateProgress();
								}
								targetLayer = null;
							}
						}
						// 기준점 초과오류 (OverShoot)
						if (optionName.equals("OverShootB")) {
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
										OpenDTLayer targetLayer = new OpenDTLayer();
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
							BathService.pb.updateProgress();
						}
						// 폴리곤 꼬임 오류 (InvalidPolygon)
						if (optionName.equals("EntityTwisted")) {
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
									OpenDTLayer targetLayer = new OpenDTLayer();
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
							BathService.pb.updateProgress();
						}
						// 인접요소부재오류 (RefEntityNone)
						if (optionName.equals("RefEntityNone")) {
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
										OpenDTLayer targetLayer = new OpenDTLayer();
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
							BathService.pb.updateProgress();
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

	public OpenDTLayer getDTLayer(String filePath, String fileName) {

		OpenDTLayer dtLayer = new OpenDTLayer();
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
					if (geom != null) {
						Envelope env = geom.getEnvelopeInternal();
						if (!env.isNull()) {
							quad.insert(env, simpleFeature);
						}
//						Geometry transGeom = null;
//						if (transform != null) {
//							try {
//								transGeom = JTS.transform(geom, transform);
//							} catch (MismatchedDimensionException | TransformException e) {
//								// TODO Auto-generated catch block
//								e.printStackTrace();
//							}
//						} else {
//							transGeom = geom;
//						}
//						if (transGeom != null) {
//							Envelope env = transGeom.getEnvelopeInternal();
//							if (!env.isNull()) {
//								if (!geom.equals(transGeom)) {
//									simpleFeature.setDefaultGeometry(transGeom);
//								}
//								quad.insert(env, simpleFeature);
//							}
//						}
					}
				}
			}, new NullProgressListener());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return quad;
	}

	public OpenDTLayerList getRelationTypeDTLayers(OpenDTLayer dtLayer, OptionRelation relation,
			QALayerTypeList qaTypes) {

		OpenDTLayerList reDTLayers = new OpenDTLayerList();

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
						OpenDTLayer reDtLayer = dtLayer;
						reDTLayers.add(reDtLayer);
					} else {
						OpenDTLayer reDtLayer = getDTLayer(fileDir, refilterCode);
						if (reDtLayer != null) {
							reDTLayers.add(reDtLayer);
						}
					}
				}
			} else {
				List<String> reLayerIDs = reQaType.getLayerIDList();
				for (String reLayerId : reLayerIDs) {
					if (reLayerId.equals(layerName)) {
						OpenDTLayer reDtLayer = dtLayer;
						reDTLayers.add(reDtLayer);
					} else {
						OpenDTLayer reDtLayer = getDTLayer(fileDir, reLayerId);
						if (reDtLayer != null) {
							reDTLayers.add(reDtLayer);
						}
					}
				}
			}
		}
		if (reDTLayers.size() > 0) {
			for (OpenDTLayer tmp : reDTLayers) {
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