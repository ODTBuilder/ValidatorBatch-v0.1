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
 *    Lesser General Public License for more details.
 */

package com.git.gdsbuilder.validator.fileReader.ngi;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.logging.Level;

import org.geotools.data.DataUtilities;
import org.geotools.data.ngi.NGIDataStoreFactory;
import org.geotools.referencing.CRS;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.NoSuchAuthorityCodeException;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

/**
 * NGIDataStore 객체를 생성하는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 3. 11. 오전 11:40:41
 */
public class NGIDataStoreFactoryExtend extends NGIDataStoreFactory {

	/**
	 * @author DY.Oh
	 * @since 2018. 1. 30. 오후 1:47:49
	 * @param params
	 * @throws IOException
	 * @decription NGIDataStore 객체를 생성
	 */
	public void createDTNGIDataStore(Map<String, Serializable> params) throws IOException {

		URL url = (URL) PARAM_FILE.lookUp(params);
		String code = (String) PARAM_SRS.lookUp(params);
		String charset = (String) PARAM_CHARSET.lookUp(params);

		if (charset == null || charset.isEmpty()) {
			charset = (String) PARAM_CHARSET.sample;
		}

		CoordinateReferenceSystem crs = null;
		if (code == null || code.isEmpty()) {
			crs = null; // default??
		} else {
			try {
				crs = CRS.decode(code);
			} catch (NoSuchAuthorityCodeException e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			} catch (FactoryException e) {
				LOGGER.log(Level.WARNING, e.getMessage(), e);
			}
		}
		new NGIDataStore(DataUtilities.urlToFile(url), Charset.forName(charset), crs);
	}
}
