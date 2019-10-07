package com.git.batch.step;

import java.io.File;
import java.util.List;

import org.geotools.data.simple.SimpleFeatureCollection;
import org.json.simple.JSONArray;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;

import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.type.dt.layer.BasicDTLayer;
import com.git.gdsbuilder.type.dt.layer.BasicDTLayerList;
import com.git.gdsbuilder.type.validate.layer.QALayerType;
import com.git.gdsbuilder.type.validate.layer.QALayerTypeList;
import com.git.gdsbuilder.type.validate.option.QAOption;
import com.git.gdsbuilder.type.validate.option.specific.AttributeMiss;
import com.git.gdsbuilder.type.validate.option.specific.GraphicMiss;
import com.git.gdsbuilder.type.validate.option.specific.OptionFilter;
import com.git.gdsbuilder.type.validate.option.specific.OptionRelation;
import com.git.gdsbuilder.type.validate.option.standard.FixedValue;
import com.git.gdsbuilder.type.validate.option.standard.LayerFixMiss;
import com.git.gdsbuilder.validator.fileReader.shp.parser.SHPFileLayerParser;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

public class OpenProgress {

	private static ProgressBar pb;
	private static int max = 0;
	double percentage = 100;
	static double current = 0;
	static int i = 0;
	DTLayerCollection collection;
	String epsg;
	JSONArray attrFilter;
	JSONArray stateFilter;

	public OpenProgress() {
	}

	public OpenProgress(String epsg, JSONArray attrFilter2, JSONArray stateFilter2) {
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
		return OpenProgress.max;
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
				tmp.setFilter(relation.getFilter(code));
				tmp.setFigure(relation.getFigure(code));
			}
			return reDTLayers;
		} else {
			return null;
		}
	}
}
