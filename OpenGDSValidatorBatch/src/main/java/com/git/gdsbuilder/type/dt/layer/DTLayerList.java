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

package com.git.gdsbuilder.type.dt.layer;

import java.util.ArrayList;

/**
 * LayerList 정보를 저장하는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 3. 11. 오후 1:32:25
 */
@SuppressWarnings("serial")
public class DTLayerList extends ArrayList<DTLayer> {

	public DTLayer getDTLayer(String layerID) {

		for (int i = 0; i < this.size(); i++) {
			DTLayer layer = this.get(i);
			if (layerID.equals(layer.getLayerID())) {
				return layer;
			}
		}
		return null;
	}

	public boolean isEqualsLayer(String id, String type) {

		for (int i = 0; i < this.size(); i++) {
			DTLayer layer = this.get(i);
			String layerID = layer.getLayerID();
			if (layerID.equals(id + "_" + type)) {
				return true;
			}
		}
		return false;
	}
}
