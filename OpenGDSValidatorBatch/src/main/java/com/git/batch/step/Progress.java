package com.git.batch.step;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.feature.SchemaException;
import org.json.simple.JSONArray;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.type.dt.collection.DTLayerCollectionList;
import com.git.gdsbuilder.type.dt.collection.MapSystemRule;
import com.git.gdsbuilder.type.dt.collection.MapSystemRule.MapSystemRuleType;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.dt.layer.BasicDTLayer;
import com.git.gdsbuilder.type.dt.layer.BasicDTLayerList;
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
import com.git.gdsbuilder.type.validate.option.standard.FixedValue;
import com.git.gdsbuilder.type.validate.option.standard.LayerFixMiss;
import com.git.gdsbuilder.validator.fileReader.shp.parser.SHPFileLayerParser;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class Progress {

	private static ProgressBar pb;
	private static int max = 0;
	double percentage = 100;
	static double current = 0;
	static int i = 0;
	DTLayerCollection collection;
	String epsg;
	JSONArray attrFilter;
	JSONArray stateFilter;

	public Progress() {
	}

	public Progress(String epsg, JSONArray attrFilter2, JSONArray stateFilter2) {
		this.epsg = epsg;
		this.attrFilter = attrFilter2;
		this.stateFilter = stateFilter2;
	}

	public void startProgress() {
		pb = new ProgressBar("Progressing", 100, 1000, System.out, ProgressBarStyle.ASCII, "", 1);
	}

	public static void modifyMax() {
		if (max > 0)
			max--;
	}

	public void countBasicTotalTask(String fileDir, QALayerTypeList validateLayerTypeList) {
		for (QALayerType qaType : validateLayerTypeList) {
			List<String> layerNames = qaType.getLayerIDList();
			for (String layerName : layerNames) {
				// target layer
				BasicDTLayer dtLayer = null;
				dtLayer = getDTLayer(fileDir, layerName);
				if (dtLayer == null) {
					continue;
				}
				// option
				QAOption qaOption = qaType.getOption();
				List<LayerFixMiss> layerFixMissArr = qaOption.getLayerMissOptions();
				if (layerFixMissArr != null) {
					for (LayerFixMiss fix : layerFixMissArr) {
						List<FixedValue> fixedList = fix.getFix();
						if (fixedList != null) {
							max++;
						}
					}
				}
				List<AttributeMiss> attrMissArr = qaOption.getAttributeMissOptions();
				if (attrMissArr != null) {
					for (AttributeMiss attrMiss : attrMissArr) {
						String optionName = attrMiss.getOption();
						List<OptionRelation> relations = attrMiss.getRetaion();
						if (optionName.equals("ZValueAmbiguous")) {
							max++;
						}
						if (optionName.equals("RefAttributeMiss")) {
							if (relations != null) {
								for (OptionRelation relation : relations) {
									BasicDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (BasicDTLayer reDTLayer : reDTLayers) {
											max++;
										}
									}
								}
							}
						}
					}
				}
				List<GraphicMiss> grapMissArr = qaOption.getGraphicMissOptions();
				if (grapMissArr != null) {
					for (GraphicMiss grapMiss : grapMissArr) {
						String optionName = grapMiss.getOption();
						List<OptionRelation> relations = grapMiss.getRetaion();
						if (optionName.equals("SmallLength")) {
							max++;
						}
						if (optionName.equals("SmallArea")) {
							max++;
						}
						if (optionName.equals("SelfEntity")) {
							if (relations != null) {
								for (OptionRelation relation : relations) {
									BasicDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (BasicDTLayer reDTLayer : reDTLayers) {
											max++;
										}
									}
								}
							}
						}
						if (optionName.equals("EntityDuplicated")) {
							max++;
						}
						if (optionName.equals("ConOverDegree")) {
							max++;
						}
						if (optionName.equals("ConIntersected")) {
							max++;
						}
						if (optionName.equals("ConBreak")) {
							max++;
						}
						if (optionName.equals("UselessPoint")) {
							max++;
						}
						if (optionName.equals("PointDuplicated")) {
							max++;
						}
						if (optionName.equals("OutBoundary")) {
							if (relations != null) {
								for (OptionRelation relation : relations) {
									BasicDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (BasicDTLayer reDTLayer : reDTLayers) {
											max++;
										}
									}
								}
							}
						}
						if (optionName.equals("NodeMiss")) {
							if (relations != null) {
								for (OptionRelation relation : relations) {
									BasicDTLayerList reDTLayers = getRelationTypeDTLayers(dtLayer, relation,
											validateLayerTypeList);
									if (reDTLayers != null) {
										for (BasicDTLayer reDTLayer : reDTLayers) {
											max++;
										}
									}
								}
							}
						}
						if (optionName.equals("OverShoot")) {
							max++;
						}
						if (optionName.equals("EntityTwisted")) {
							max++;
						}
					}
				}
			}
		}
	}

	public void countTotalTask(QALayerTypeList types, DTLayerCollection collection,
			DTLayerCollectionList collectionList) {
		try {
			this.collection = collection;
			layerMissValidate(types, collection);

			geometricValidate(types, collection);

			attributeValidate(types, collection);

			if (collectionList != null) {
				closeCollectionValidate(types, collection, collectionList);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void closeCollectionValidate(QALayerTypeList types, DTLayerCollection collection,
			DTLayerCollectionList closeCollections) {

		// DTLayer neatLine = collection.getNeatLine();
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

		if (isTrue) {
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
							max++;
						}
					}
				} else {
					continue;
				}
			}
		}
	}

	private void attributeValidate(QALayerTypeList types, DTLayerCollection layerCollection) throws SchemaException {
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
						max++;
					}
				}
			} else {
				continue;
			}
		}
	}

	// 그래픽 검수
	private void geometricValidate(QALayerTypeList types, DTLayerCollection layerCollection)
			throws SchemaException, NoSuchAuthorityCodeException, FactoryException, TransformException, IOException {
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
						max++;
					}
				}
			} else {
				continue;
			}
		}
	}

	@SuppressWarnings("unused")
	private void layerMissValidate(QALayerTypeList types, DTLayerCollection layerCollection) throws SchemaException {
		// TODO Auto-generated method stub
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
					max++;
				}
			}
		}
	}

	public long convertStepByMax() {
		double div = percentage / max;
		return (long) (current += div);
	}

	public void updateProgress() {
		// System.out.println("MAX : " + max + " CURR : " + ++i);
		if (pb != null) {
			long plus = convertStepByMax();
			if (plus < pb.getMax()) {
				pb.stepTo(plus);
			} else {
				pb.stepTo(100);
				pb.close();
				pb = null;
				i = 0;
			}
			if (i == max) {
				pb.stepTo(100);
				pb.close();
				pb = null;
				i = 0;
			}
		}
	}

	public void terminate() {
		if (pb != null) {
			pb.stepTo(100);
			pb.close();
			pb = null;
		}
	}

	public long getMax() {
		return Progress.max;
	}

	private BasicDTLayer getDTLayer(String filePath, String fileName) {

		BasicDTLayer dtLayer = new BasicDTLayer();
		File layerFile = new File(filePath + File.separator + fileName + ".shp");
		SimpleFeatureCollection sfc = new SHPFileLayerParser().getShpObject(layerFile);
		if (sfc != null) {
			SimpleFeatureType featureType = sfc.getSchema();
			GeometryType geometryType = featureType.getGeometryDescriptor().getType();
			String geomType = geometryType.getBinding().getSimpleName().toString();
			dtLayer.setLayerID(fileName);
			dtLayer.setLayerType(geomType);
			dtLayer.setSimpleFeatureCollection(sfc);
			return dtLayer;
		} else {
			return null;
		}
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
				OptionFilter filter = relation.getFilter(code);
				OptionFigure figure = relation.getFigure(code);
				if (filter != null) {
					tmp.setFilter(filter);
				}
				if (figure != null) {
					tmp.setFigure(figure);
				}
			}
			return reDTLayers;
		} else {
			return null;
		}
	}
}
