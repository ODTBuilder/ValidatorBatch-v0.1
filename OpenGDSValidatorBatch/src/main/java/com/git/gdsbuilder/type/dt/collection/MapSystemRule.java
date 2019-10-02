package com.git.gdsbuilder.type.dt.collection;

import java.util.HashMap;
import java.util.Map;

import com.git.gdsbuilder.type.dt.layer.DTLayer;
import com.git.gdsbuilder.type.validate.layer.QALayerType;

/**
 * @className MapSystemRule.java
 * @description DTLayerCollection의 인접 DTLayerCollection의 정보를 담고 있는 클래스
 * @author DY.Oh
 * @since 2018. 1. 30. 오후 2:02:11
 */
public class MapSystemRule {

	private Integer bottom = 0;
	private Integer top = 0;
	private Integer left = 0;
	private Integer right = 0;

	/**
	 * @className MapSystemRule.java
	 * @description MapSystemRule의 Type
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:02:56
	 */
	public enum MapSystemRuleType {
		BOTTOM("BOTTOM"), LEFT("LEFT"), RIGHT("RIGHT"), TOP("TOP"), UNKNOWN(null);
		private String typeName;

		private MapSystemRuleType(String typeName) {
			this.typeName = typeName;
		}

		public static MapSystemRuleType get(String typeName) {
			for (MapSystemRuleType type : values()) {
				if (type == UNKNOWN)
					continue;
				if (type.typeName.equals(typeName))
					return type;
			}
			return UNKNOWN;
		}

		public String getTypeName() {
			return this.typeName;
		}
	};

	public MapSystemRule setMapSystemRule(String collectionName) {

		if (collectionName.matches("^[0-9]*$")) {
			Integer center = Integer.parseInt(collectionName);
			if (collectionName.endsWith("1")) {
				if (collectionName.endsWith("01")) {
					this.left = null;
					this.right = center + 1;
					this.top = null;
					this.bottom = center + 10;
				} else if (collectionName.endsWith("91")) {
					this.left = null;
					this.right = center + 1;
					this.top = center - 10;
					this.bottom = null;
				} else {
					this.left = null;
					this.right = center + 1;
					this.top = center - 10;
					this.bottom = center + 10;
				}
			} else if (collectionName.endsWith("0")) {
				if (collectionName.endsWith("10")) {
					this.left = center - 1;
					this.right = null;
					this.top = null;
					this.bottom = center + 10;
				} else if (collectionName.endsWith("100")) {
					this.left = center - 1;
					this.right = null;
					this.top = center - 10;
					this.bottom = null;
				} else {
					this.left = center - 1;
					this.right = null;
					this.top = center - 10;
					this.bottom = center + 10;
				}
			} else {
				this.left = center - 1;
				this.right = center + 1;
				this.top = center - 10;
				this.bottom = center + 10;

			}
			return this;
		} else {
			return null;
		}
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 2:03:13
	 * @param ruleType
	 * @return int
	 * @decription ruleType에 해당하는 인접도엽 정보를 반환
	 */
	public int getMapSystemlRule(MapSystemRuleType ruleType) {
		int value = 0;
		if (ruleType != null) {
			if (ruleType == MapSystemRuleType.BOTTOM) {
				value = this.bottom;
			} else if (ruleType == MapSystemRuleType.LEFT) {
				value = this.left;
			} else if (ruleType == MapSystemRuleType.RIGHT) {
				value = this.right;
			} else if (ruleType == MapSystemRuleType.TOP) {
				value = this.top;
			}
		}
		return value;
	}

	public Integer getBottom() {
		return bottom;
	}

	public void setBottom(Integer bottom) {
		this.bottom = bottom;
	}

	public Integer getTop() {
		return top;
	}

	public void setTop(Integer top) {
		this.top = top;
	}

	public Integer getLeft() {
		return left;
	}

	public void setLeft(Integer left) {
		this.left = left;
	}

	public Integer getRight() {
		return right;
	}

	public void setRight(Integer right) {
		this.right = right;
	}

	/**
	 * @author DY.Oh
	 * @param types
	 * @since 2018. 3. 30. 오후 1:29:44
	 * @return MapSystemRule
	 * @decription
	 */
	public Map<String, DTLayer> getMapSystemRuleMap(QALayerType type, DTLayerCollection colleciton) {

		Map<String, DTLayer> ruleMap = new HashMap<>();

		boolean isTrue = false;
		if (this.top != null) {
			DTLayer topLayer = type.getTypeLayer(this.top.toString(), colleciton);
			ruleMap.put("top", topLayer);
			isTrue = true;
		}
		if (this.bottom != null) {
			DTLayer bottomLayer = type.getTypeLayer(this.bottom.toString(), colleciton);
			ruleMap.put("bottom", bottomLayer);
			isTrue = true;
		}
		if (this.left != null) {
			DTLayer leftLayer = type.getTypeLayer(this.left.toString(), colleciton);
			ruleMap.put("left", leftLayer);
			isTrue = true;
		}
		if (this.right != null) {
			DTLayer rightLayer = type.getTypeLayer(this.right.toString(), colleciton);
			ruleMap.put("right", rightLayer);
			isTrue = true;
		}
		if (isTrue) {
			return ruleMap;
		} else {
			return null;
		}
	}
}
