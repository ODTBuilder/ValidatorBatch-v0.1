package com.git.batch.step;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.feature.SchemaException;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.operation.TransformException;

import com.git.gdsbuilder.type.dt.collection.DTLayerCollection;
import com.git.gdsbuilder.type.dt.collection.DTLayerCollectionList;
import com.git.gdsbuilder.type.dt.collection.MapSystemRule;
import com.git.gdsbuilder.type.dt.collection.MapSystemRule.MapSystemRuleType;
import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.validate.error.ErrorLayer;
import com.git.gdsbuilder.type.validate.layer.QALayerType;
import com.git.gdsbuilder.type.validate.layer.QALayerTypeList;
import com.git.gdsbuilder.type.validate.option.QAOption;
import com.git.gdsbuilder.type.validate.option.specific.AttributeMiss;
import com.git.gdsbuilder.type.validate.option.specific.CloseMiss;
import com.git.gdsbuilder.type.validate.option.specific.GraphicMiss;
import com.git.gdsbuilder.type.validate.option.standard.LayerFixMiss;

import me.tongfei.progressbar.ProgressBar;
import me.tongfei.progressbar.ProgressBarStyle;

/**
 * 검수 Progress 관리 클래스
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

	/**
	 * 검수 시작시 Progress 객체 생성
	 * @author SG.LEE
	 */
	public void startProgress() {
		// System.out.println(max);
		// pb = new ProgressBar("진행중", 100, ProgressBarStyle.ASCII);
		// default : System.err 에서 변경
		pb = new ProgressBar("Proceeding", 100, 1000, System.out, ProgressBarStyle.ASCII, "", 1);
	}

	/**
	 * Max Size 수정 
	 * @author SG.LEE
	 */
	public static void modifyMax() {
		if (max > 0)
			max--;
	}

	/**
	 * 검수수행전 전체 Progress 크기 설정
	 * @author SG.LEE
	 * @param types {@link QALayerTypeList} 옵션 리스트
	 * @param collection {@link DTLayerCollection} 검수 대상도엽
	 * @param collectionList {@link DTLayerCollectionList} 인접도엽
	 */
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

	/**
	 * 전체 Max Size 백분율로 변환
	 * @author SG.LEE
	 * @return long Max Size 백분율
	 */
	public long convertStepByMax() {
		double div = percentage / max;
		return (long) (current += div);
	}

	/**
	 * 검수 한단계 진행완료시 {@link Progress} 업데이트
	 * @author SG.LEE
	 */
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

	/**
	 * {@link Progress} 진행 100 설정후 {@link Progress} 진행 종료
	 * @author SG.LEE
	 */
	public void terminate() {
		if (pb != null) {
			pb.stepTo(100);
			pb.close();
			pb = null;
		}
	}

	// 도엽 검수
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

	/**
	 * {@link Progress} Max Size Get
	 * @author SG.LEE
	 * @return long Max Size
	 */
	public long getMax() {
		return Progress.max;
	}
}
