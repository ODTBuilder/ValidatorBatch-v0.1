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
import com.git.gdsbuilder.type.dt.layer.OpenDTLayer;
import com.git.gdsbuilder.type.dt.layer.OpenDTLayerList;
import com.git.gdsbuilder.type.validate.error.ErrorLayer;
import com.git.gdsbuilder.type.validate.layer.QALayerType;
import com.git.gdsbuilder.type.validate.layer.QALayerTypeList;
import com.git.gdsbuilder.type.validate.option.QAOption;
import com.git.gdsbuilder.type.validate.option.en.LangType;
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

/**
 * 검수 Progress 관리 클래스
 * 
 * @author SG.LEE
 *
 */
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
	LangType langType;

	/**
	 * 검수 시작시 Progress 객체 생성
	 * 
	 * @author SG.LEE
	 */
	public Progress() {
	}

	public Progress(String epsg, JSONArray attrFilter2, JSONArray stateFilter2, LangType langType) {
		this.epsg = epsg;
		this.attrFilter = attrFilter2;
		this.stateFilter = stateFilter2;
		this.langType = langType;
	}

	public Progress(LangType langType) {
		this.langType = langType;
	}

	public void startProgress() {
		if (langType.getLang().equals("ko")) {
			pb = new ProgressBar("검수 진행 중", 100, 1000, System.out, ProgressBarStyle.ASCII, "", 1);
		} else if (langType.getLang().equals("en")) {
			pb = new ProgressBar("Progressing", 100, 1000, System.out, ProgressBarStyle.ASCII, "", 1);
		}
	}

	/**
	 * Max Size 수정
	 * 
	 * @author SG.LEE
	 */
	public static void modifyMax() {
		if (max > 0)
			max--;
	}

	public void countOpenTotalTask(String fileDir, QALayerTypeList validateLayerTypeList) {
		for (QALayerType qaType : validateLayerTypeList) {
			List<String> layerNames = qaType.getLayerIDList();
			for (String layerName : layerNames) {
				// target layer
				OpenDTLayer dtLayer = null;
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
						// 속성오류 (AttributeMiss)
						if (optionName.equals("AttributeMiss")) {
							LayerFixMiss fix = qaOption.getLayerMissOption(layerName);
							if (fix != null) {
								max++;
							}
						}
						// 필수속성오류 (AttributeFixMiss)
						if (optionName.equals("AttributeFixMiss")) {
							LayerFixMiss fix = qaOption.getLayerMissOption(layerName);
							if (fix != null) {
								max++;
							}
						}
						// 고도값오류 (Z-Value Abmiguous)
						if (optionName.equals("ZValueAmbiguous2")) {
							max++;
						}
						// 인접속성오류 (RefAttributeMiss)
						if (optionName.equals("RefAttributeMissB")) {
							if (relations != null) {
								for (OptionRelation relation : relations) {
									max++;
								}
							}
						}
						// 문자의 정확성(Text Accuracy)
						if (optionName.equals("FixValues")) {
							max++;
						}
					}
				}
				List<GraphicMiss> grapMissArr = qaOption.getGraphicMissOptions();
				if (grapMissArr != null) {
					for (GraphicMiss grapMiss : grapMissArr) {
						String optionName = grapMiss.getOption();
						List<OptionRelation> relations = grapMiss.getRetaion();
						// 계층오류 (LayerFix)
						if (optionName.equals("LayerMiss")) {
							LayerFixMiss fix = qaOption.getLayerMissOption(layerName);
							if (fix != null) {
								max++;
							}
						}
						// 허용범위 이하 길이 (SmallLength)
						if (optionName.equals("SmallLengthB")) {
							max++;
						}
						// 허용범위 이하 면적 (SmallArea)
						if (optionName.equals("SmallAreaB")) {
							max++;
						}
						// 단독존재오류 (Self Entity)
						if (optionName.equals("SelfEntityB")) {
							if (relations != null) {
								for (OptionRelation relation : relations) {
									max++;
								}
							}
						}
						// 요소중복오류 (EntityDuplicated)
						if (optionName.equals("EntityDuplicatedB")) {
							max++;
						}
						// 등고선 꺾임 오류 (ConOverDegree)
						if (optionName.equals("ConOverDegreeB")) {
							max++;
						}
						// 등고선교차오류 (ConIntersected)
						if (optionName.equals("ConIntersectedB")) {
							max++;
						}
						// 등고선 끊김오류 (ConBreak)
						if (optionName.equals("ConBreakB")) {
							max++;
						}
						// 등고선 직선화미처리오류(UselessPoint)
						if (optionName.equals("UselessPoint")) {
							max++;
						}
						// 중복점오류(DuplicatedPoint)
						if (optionName.equals("PointDuplicatedB")) {
							max++;
						}
						// 경계초과오류 (OutBoundary)
						if (optionName.equals("OutBoundaryB")) {
							if (relations != null) {
								for (OptionRelation relation : relations) {
								}
							}
							max++;
						}
						// 노드오류 (NodeMiss)
						if (optionName.equals("NodeMissB")) {
							if (relations != null) {
								for (OptionRelation relation : relations) {
									max++;
								}
							}
						}
						// 기준점 초과오류 (OverShoot)
						if (optionName.equals("OverShootB")) {
							max++;
						}
						// 폴리곤 꼬임 오류 (InvalidPolygon)
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

	private OpenDTLayer getDTLayer(String filePath, String fileName) {

		OpenDTLayer dtLayer = new OpenDTLayer();
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
					}
				}
			} else {
				List<String> reLayerIDs = reQaType.getLayerIDList();
				for (String reLayerId : reLayerIDs) {
					if (reLayerId.equals(layerName)) {
						OpenDTLayer reDtLayer = dtLayer;
						reDTLayers.add(reDtLayer);
					}
				}
			}
		}
		if (reDTLayers.size() > 0) {
			for (OpenDTLayer tmp : reDTLayers) {
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
