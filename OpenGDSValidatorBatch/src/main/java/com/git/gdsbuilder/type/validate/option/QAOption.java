/**
 * 
 */
package com.git.gdsbuilder.type.validate.option;

import java.util.List;

import com.git.gdsbuilder.type.validate.option.specific.AttributeMiss;
import com.git.gdsbuilder.type.validate.option.specific.CloseMiss;
import com.git.gdsbuilder.type.validate.option.specific.GraphicMiss;
import com.git.gdsbuilder.type.validate.option.standard.LayerFixMiss;

/**
 * @className QaOptionList.java
 * @description
 * @author DY.Oh
 * @since 2018. 3. 14. 오전 11:21:59
 */

public class QAOption {

	String name; // typeName
	List<LayerFixMiss> layerMissOptions;
	List<AttributeMiss> attributeMissOptions;
	List<GraphicMiss> graphicMissOptions;
	List<CloseMiss> closeMissOptions;

	public LayerFixMiss getLayerMissOption(String layerName) {

		if (layerMissOptions != null) {
			for (LayerFixMiss layfm : layerMissOptions) {
				if (layfm.getCode().equals(layerName)) {
					return layfm;
				}
			}
		}
		return null;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<LayerFixMiss> getLayerMissOptions() {
		return layerMissOptions;
	}

	public void setLayerMissOptions(List<LayerFixMiss> layerMissOptions) {
		this.layerMissOptions = layerMissOptions;
	}

	public List<AttributeMiss> getAttributeMissOptions() {
		return attributeMissOptions;
	}

	public void setAttributeMissOptions(List<AttributeMiss> attributeMissOptions) {
		this.attributeMissOptions = attributeMissOptions;
	}

	public List<GraphicMiss> getGraphicMissOptions() {
		return graphicMissOptions;
	}

	public void setGraphicMissOptions(List<GraphicMiss> graphicMissOptions) {
		this.graphicMissOptions = graphicMissOptions;
	}

	public List<CloseMiss> getCloseMissOptions() {
		return closeMissOptions;
	}

	public void setCloseMissOptions(List<CloseMiss> closeMissOptions) {
		this.closeMissOptions = closeMissOptions;
	}

}
