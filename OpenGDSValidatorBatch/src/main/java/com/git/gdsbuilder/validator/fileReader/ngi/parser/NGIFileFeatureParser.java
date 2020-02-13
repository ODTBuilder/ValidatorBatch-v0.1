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

package com.git.gdsbuilder.validator.fileReader.ngi.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.geotools.data.DataUtilities;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.git.gdsbuilder.validator.fileReader.ngi.header.NDAField;
import com.git.gdsbuilder.validator.fileReader.ngi.header.NDAHeader;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;

/**
 * ngi/nda 파일의 feature를 QA20Feature객체로 파싱하는 클래스
 * 
 * @author DY.Oh
 * @since 2017. 3. 11. 오전 11:18:11
 */
public class NGIFileFeatureParser {

	static final String defaultEpsg = "EPSG:32652";

	BufferedReader ngiReader;
	BufferedReader ndaReader;
	String epsg;

	/**
	 * QA20FileLayerParser 생성자
	 * 
	 * @param epsg
	 * 
	 * @param ngiReader
	 * @param ndaReader
	 */
	public NGIFileFeatureParser(String epsg, BufferedReader ngiReader, BufferedReader ndaReader) {
		this.ngiReader = ngiReader;
		this.ndaReader = ndaReader;
		this.epsg = epsg;
	}

	public NGIFileFeatureParser(String epsg, BufferedReader ngiReader) {
		this.ngiReader = ngiReader;
		this.epsg = epsg;
	}

	/**
	 * BufferedReader getter @author DY.Oh @since 2017. 3. 11. 오전
	 * 10:42:35 @return BufferedReader @throws
	 */
	public BufferedReader getNgiReader() {
		return ngiReader;
	}

	/**
	 * BufferedReader setter @author DY.Oh @since 2017. 3. 11. 오전 10:43:28 @param
	 * ngiReader void @throws
	 */
	public void setNgiReader(BufferedReader ngiReader) {
		this.ngiReader = ngiReader;
	}

	/**
	 * BufferedReader getter @author DY.Oh @since 2017. 3. 11. 오전
	 * 10:43:41 @return BufferedReader @throws
	 */
	public BufferedReader getNdaReader() {
		return ndaReader;
	}

	/**
	 * BufferedReader setter @author DY.Oh @since 2017. 3. 11. 오전 10:43:51 @param
	 * ndaReader void @throws
	 */
	public void setNdaReader(BufferedReader ndaReader) {
		this.ndaReader = ndaReader;
	}

	/**
	 * 문자열에서 특수문자 제거 @author DY.Oh @since 2017. 3. 11. 오전 10:44:42 @param
	 * str @return String @throws
	 */
	private String StringReplace(String str) {
		String match = "[^\uAC00-\uD7A3xfe0-9a-zA-Z\\s]";
		str = str.replaceAll(match, "");
		return str;
	}

	private String getQuotesText(String line) {
		Pattern p = Pattern.compile("\\\"(.*?)\\\"");
		Matcher m = p.matcher(line);
		if (m.find()) {
			String attr = m.group(1);
			return attr;
		} else {
			return null;
		}
	}

	public SimpleFeature parserDTFeature(String featureID) throws Exception {

		SimpleFeature feature = null;
		String id = StringReplace(featureID);
		String line = ngiReader.readLine();
		if (line.equalsIgnoreCase("POLYGON")) {
			feature = parserDTPolygonFeature(id, line);
		}
		if (line.equalsIgnoreCase("LINESTRING")) {
			feature = parserDTLineStringFeature(id, line);
		}
		if (line.equalsIgnoreCase("POINT")) {
			feature = parserDTPointFeature(id, line);
		}
		if (line.startsWith("TEXT")) {
			feature = parserDTTextFeature(id, line);
		}
		ngiReader.readLine();
		return feature;
	}

	public SimpleFeature parserDTFeature(String featureID, NDAHeader ndaHeader) throws Exception {

		SimpleFeature feature = null;
		String id = StringReplace(featureID);
		String line = ngiReader.readLine();
		if (line.equalsIgnoreCase("POLYGON")) {
			feature = parserDTPolygonFeature(id, line, ndaHeader);
		}
		if (line.equalsIgnoreCase("LINESTRING")) {
			feature = parserDTLineStringFeature(id, line, ndaHeader);
		}
		if (line.equalsIgnoreCase("POINT")) {
			feature = parserDTPointFeature(id, line, ndaHeader);
		}
		if (line.startsWith("TEXT")) {
			feature = parserDTTextFeature(id, line, ndaHeader);
		}
		ngiReader.readLine();
		return feature;
	}

	/**
	 * 문자열에서 괄호( )안에 해당되는 문자열을 반환 @author DY.Oh @since 2017. 3. 11. 오전
	 * 11:09:53 @param line @return String @throws
	 */
	private Map<String, Object> getText(String line) {

		Map<String, Object> textMap = new HashMap<>();
		Pattern p = Pattern.compile("\\\"(.*?)\\\"");
		Matcher m = p.matcher(line);
		if (m.find()) {
			String text = m.group(0);
			textMap.put("text", text);
			String value = m.group(1);
			textMap.put("value", value);
			return textMap;
		} else {
			return null;
		}
	}

	/**
	 * ngi파일에서 객체의 numparts 정보를 반환 @author DY.Oh @since 2017. 3. 11. 오전
	 * 11:11:16 @return String @throws IOException @throws
	 */
	private String getLinearRingCount() throws IOException {

		String line = ngiReader.readLine();
		if (line.startsWith("NUMPARTS")) {
			return line;
		} else {
			return null;
		}
	}

	/**
	 * ngi파일에서 객체의 포인트 갯수를 반환 @author DY.Oh @since 2017. 3. 11. 오전
	 * 11:16:00 @return String @throws IOException @throws
	 */
	private String getCoordinateCount() throws IOException {
		return ngiReader.readLine();
	}

	/**
	 * ngi파일에서 객체의 그래픽 ID를 반환 @author DY.Oh @since 2017. 3. 11. 오전
	 * 11:16:02 @return String @throws IOException @throws
	 */
	private String getGraphicID() throws IOException {
		return ngiReader.readLine();
	}

	/**
	 * TEXT 타입의 객체를 QA20Feature객체로 파싱
	 * 
	 * @author DY.Oh
	 * @since 2017. 3. 11. 오전 10:59:30
	 * @param featureID
	 * @param text
	 * @param ndaHeader
	 * @return QA20Feature
	 * @throws IOException
	 * @throws SchemaException
	 */
	private SimpleFeature parserDTTextFeature(String featureID, String text) throws Exception {

		Map<String, Object> textValue = getText(text);
		String line = ngiReader.readLine();
		String[] strCoor = line.split(" ");
		Coordinate coor = new Coordinate(Double.parseDouble(strCoor[0]), Double.parseDouble(strCoor[1]));
		GeometryFactory factory = new GeometryFactory();
		Geometry geom = factory.createPoint(coor);

		CoordinateReferenceSystem inputProjection = CRS.decode(epsg);
		CoordinateReferenceSystem outputProjection = CRS.decode(defaultEpsg);
		MathTransform transform = CRS.findMathTransform(inputProjection, outputProjection, true);
		geom = JTS.transform(geom, transform);

		HashMap<String, Object> properties = new HashMap<>();
		properties.put("text", textValue.get("text").toString());
		return parseSimpleFeature(featureID, geom, textValue.get("value").toString(), properties);
	}

	private SimpleFeature parserDTPointFeature(String featureID, String originType) throws Exception {

		String line = ngiReader.readLine();
		String[] strCoor = line.split(" ");
		Coordinate coor = new Coordinate(Double.parseDouble(strCoor[0]), Double.parseDouble(strCoor[1]));
		GeometryFactory factory = new GeometryFactory();
		Geometry geom = factory.createPoint(coor);

		CoordinateReferenceSystem inputProjection = CRS.decode(epsg);
		CoordinateReferenceSystem outputProjection = CRS.decode(defaultEpsg);
		MathTransform transform = CRS.findMathTransform(inputProjection, outputProjection, true);
		geom = JTS.transform(geom, transform);

		return parseSimpleFeature(featureID, geom, originType);
	}

	private SimpleFeature parserDTLineStringFeature(String featureID, String originType) throws Exception {

		String strCount = getCoordinateCount();
		int i = 0;
		int count = Integer.valueOf(strCount);
		Coordinate[] coors = new Coordinate[count];
		while (i != count) {
			String line = ngiReader.readLine();
			String[] strCoor = line.split(" ");
			Coordinate coor = new Coordinate(Double.parseDouble(strCoor[0]), Double.parseDouble(strCoor[1]));
			coors[i] = coor;
			i++;
		}
		GeometryFactory factory = new GeometryFactory();
		Geometry geom = factory.createLineString(coors);

		CoordinateReferenceSystem inputProjection = CRS.decode(epsg);
		CoordinateReferenceSystem outputProjection = CRS.decode(defaultEpsg);
		MathTransform transform = CRS.findMathTransform(inputProjection, outputProjection, true);
		geom = JTS.transform(geom, transform);

		return parseSimpleFeature(featureID, geom, originType);
	}

	public SimpleFeature parserDTPolygonFeature(String featureID, String originType) throws Exception {

		// polygon 일 경우 linearing의 갯수
		String ringCountStr = getLinearRingCount().replace("NUMPARTS ", "");
		int ringCount = Integer.valueOf(ringCountStr);

		Geometry geom = null;
		if (ringCount == 1) {
			geom = createPolygon();
		}
		if (ringCount > 1) {
			geom = createHolePolygon(ringCount);
		}
		// CoordinateReferenceSystem inputProjection = CRS.decode(epsg);
		// CoordinateReferenceSystem outputProjection = CRS.decode(defaultEpsg);
		// MathTransform transform = CRS.findMathTransform(inputProjection,
		// outputProjection, true);
		// geom = JTS.transform(geom, transform);
		return parseSimpleFeature(featureID, geom, originType);
	}

	/**
	 * TEXT 타입의 객체를 QA20Feature객체로 파싱
	 * 
	 * @author DY.Oh
	 * @since 2017. 3. 11. 오전 10:59:30
	 * @param featureID
	 * @param text
	 * @param ndaHeader
	 * @return QA20Feature
	 * @throws IOException
	 * @throws SchemaException
	 */
	private SimpleFeature parserDTTextFeature(String featureID, String text, NDAHeader ndaHeader) throws Exception {

		Map<String, Object> textValue = getText(text);
		String line = ngiReader.readLine();
		String[] strCoor = line.split(" ");
		Coordinate coor = new Coordinate(Double.parseDouble(strCoor[0]), Double.parseDouble(strCoor[1]));
		GeometryFactory factory = new GeometryFactory();
		Geometry geom = factory.createPoint(coor);

		// CoordinateReferenceSystem inputProjection = CRS.decode(epsg);
		// CoordinateReferenceSystem outputProjection = CRS.decode(defaultEpsg);
		// MathTransform transform = CRS.findMathTransform(inputProjection,
		// outputProjection, true);
		// geom = JTS.transform(geom, transform);

		HashMap<String, Object> properties = getFeatureAttrib(ndaHeader, true);
		properties.put("text", textValue.get("value").toString());
		return parseSimpleFeature(featureID, geom, textValue.get("text").toString(), properties);
	}

	/**
	 * POINT 타입의 객체를 QA20Feature객체로 파싱
	 * 
	 * @author DY.Oh
	 * @since 2017. 5. 11. 오전 10:59:30
	 * @param featureID
	 * @param text
	 * @return QA20Feature
	 * @throws IOException
	 * @throws SchemaException
	 */
	private SimpleFeature parserDTPointFeature(String featureID, String originType, NDAHeader ndaHeader)
			throws Exception {

		String line = ngiReader.readLine();
		String[] strCoor = line.split(" ");
		Coordinate coor = new Coordinate(Double.parseDouble(strCoor[0]), Double.parseDouble(strCoor[1]));
		GeometryFactory factory = new GeometryFactory();
		Geometry geom = factory.createPoint(coor);

		// CoordinateReferenceSystem inputProjection = CRS.decode(epsg);
		// CoordinateReferenceSystem outputProjection = CRS.decode(defaultEpsg);
		// MathTransform transform = CRS.findMathTransform(inputProjection,
		// outputProjection, true);
		// geom = JTS.transform(geom, transform);

		HashMap<String, Object> properties = getFeatureAttrib(ndaHeader, true);
		return parseSimpleFeature(featureID, geom, originType, properties);
	}

	private SimpleFeature parserDTLineStringFeature(String featureID, String originType, NDAHeader ndaHeader)
			throws Exception {

		String strCount = getCoordinateCount();
		int i = 0;
		int count = Integer.valueOf(strCount);
		Coordinate[] coors = new Coordinate[count];
		while (i != count) {
			String line = ngiReader.readLine();
			String[] strCoor = line.split(" ");
			Coordinate coor = new Coordinate(Double.parseDouble(strCoor[0]), Double.parseDouble(strCoor[1]));
			coors[i] = coor;
			i++;
		}
		GeometryFactory factory = new GeometryFactory();
		Geometry geom = factory.createLineString(coors);

		// CoordinateReferenceSystem inputProjection = CRS.decode(epsg);
		// CoordinateReferenceSystem outputProjection = CRS.decode(defaultEpsg);
		// MathTransform transform = CRS.findMathTransform(inputProjection,
		// outputProjection, true);
		// geom = JTS.transform(geom, transform);

		HashMap<String, Object> properties = getFeatureAttrib(ndaHeader, true);
		return parseSimpleFeature(featureID, geom, originType, properties);
	}

	/**
	 * POLYGON 타입의 객체를 QA20Feature객체로 파싱
	 * 
	 * @author DY.Oh
	 * @since 2017. 5. 11. 오전 10:59:30
	 * @param featureID
	 * @param ndaHeader
	 * @param text
	 * @return QA20Feature
	 * @throws IOException
	 * @throws SchemaException
	 */
	public SimpleFeature parserDTPolygonFeature(String featureID, String originType, NDAHeader ndaHeader)
			throws Exception {

		// polygon 일 경우 linearing의 갯수
		String ringCountStr = getLinearRingCount().replace("NUMPARTS ", "");
		int ringCount = Integer.valueOf(ringCountStr);

		Geometry geom = null;
		if (ringCount == 1) {
			geom = createPolygon();
		}
		if (ringCount > 1) {
			geom = createHolePolygon(ringCount);
		}
		// CoordinateReferenceSystem inputProjection = CRS.decode(epsg);
		// CoordinateReferenceSystem outputProjection = CRS.decode(defaultEpsg);
		// MathTransform transform = CRS.findMathTransform(inputProjection,
		// outputProjection, true);
		// geom = JTS.transform(geom, transform);

		HashMap<String, Object> properties = getFeatureAttrib(ndaHeader, true);
		return parseSimpleFeature(featureID, geom, originType, properties);
	}

	/**
	 * POLYGON 타입의 좌표값을 Geometry 객체로 생성 @author DY.Oh @since 2017. 5. 11. 오전
	 * 11:04:10 @return @throws IOException Geometry @throws
	 */
	private Geometry createPolygon() throws IOException {

		String strCount = getCoordinateCount();
		int count = Integer.valueOf(strCount);
		int i = 0;
		Coordinate[] coors = new Coordinate[count + 1];
		while (i != count) {
			String line = ngiReader.readLine();
			String[] strCoor = line.split(" ");
			Coordinate coor = new Coordinate(Double.parseDouble(strCoor[0]), Double.parseDouble(strCoor[1]));
			coors[i] = coor;
			i++;
		}
		coors[i] = coors[0];
		GeometryFactory factory = new GeometryFactory();
		Geometry linearRing = factory.createLinearRing(coors);
		Geometry polygon = factory.createPolygon((LinearRing) linearRing, null);

		return polygon;
	}

	/**
	 * HULL POLYGON 타입의 좌표값을 Geometry 객체로 생성 @author DY.Oh @since 2017. 5. 11. 오전
	 * 11:04:10 @return @throws IOException Geometry @throws
	 */
	private Geometry createHolePolygon(int ringCount) throws IOException {

		LinearRing shell = null;
		LinearRing[] holes = new LinearRing[ringCount - 1];
		holes[0] = shell;
		for (int j = 0; j < ringCount; j++) {
			String strCount = getCoordinateCount();
			int count = Integer.valueOf(strCount);
			int i = 0;
			Coordinate[] coors = new Coordinate[count + 1];
			while (i != count) {
				String line = ngiReader.readLine();
				String[] strCoor = line.split(" ");
				Coordinate coor = new Coordinate(Double.parseDouble(strCoor[0]), Double.parseDouble(strCoor[1]));
				coors[i] = coor;
				i++;
			}
			coors[i] = coors[0];
			GeometryFactory factory = new GeometryFactory();
			LinearRing linearRing = factory.createLinearRing(coors);
			if (j == 0) {
				shell = linearRing;
			} else {
				holes[j - 1] = linearRing;
			}
		}
		GeometryFactory factory = new GeometryFactory();
		Geometry polygon = factory.createPolygon(shell, holes);
		return polygon;
	}

	/**
	 * nda파일에서 객체의 속성값과 데이터 타입을 읽어 Hashtable 형태로 반환
	 * 
	 * @author DY.Oh
	 * @since 2017. 5. 11. 오전 10:45:08
	 * @param ndaHeader
	 * @return Hashtable<String, Object>
	 * @throws IOException
	 */
	public HashMap<String, Object> getFeatureAttrib(NDAHeader ndaHeader, boolean isInvalid) throws IOException {

		if (isInvalid) {
			String line = ndaReader.readLine();
			String tmpLine = line.replaceAll(" ", "");

			HashMap<String, Object> properties = new HashMap<String, Object>();
			List<NDAField> fields = ndaHeader.getAspatial_field_def();

			for (NDAField field : fields) {
				String field_name = field.getFieldName();
				String valueType = field.getType();
				String decimal = field.getDecimal();
				if (valueType.equalsIgnoreCase("NUMERIC")) {
					int cutIndex = tmpLine.indexOf(",");
					String valueStr = tmpLine.substring(0, cutIndex);
					if (valueStr.equals("")) {
						properties.put(field_name, null);
						continue;
					}
					if (Integer.parseInt(decimal) > 0) {
						double doubleValue = Double.parseDouble(valueStr);
						properties.put(field_name, doubleValue);
						tmpLine = tmpLine.substring(cutIndex);
					} else if (Integer.parseInt(decimal) == 0) {
						int intValue = Integer.parseInt(valueStr);
						properties.put(field_name, intValue);
						tmpLine = tmpLine.substring(cutIndex);
					}
				} else if (valueType.equalsIgnoreCase("STRING")) {
					String valueStr = getQuotesText(tmpLine);
					if (valueStr.equals("")) {
						properties.put(field_name, "");
					} else {
						properties.put(field_name, valueStr);
					}
					String replacedStr = "";
					for (int idx = 0; idx < valueStr.length(); ++idx) {
						String strOne = new String(new char[] { valueStr.charAt(idx) }, 0, 1);
						if (strOne.equals("(") || strOne.equals(")") || strOne.equals("{") || strOne.equals("}")
								|| strOne.equals("^") || strOne.equals("[") || strOne.equals("'" + "]")) {
							replacedStr += "\\" + strOne;
						} else if (strOne.equals("*") || strOne.equals("+") || strOne.equals("$") || strOne.equals("|")
								|| strOne.equals("?")) {
							replacedStr += "[" + strOne + "]";
						} else {
							replacedStr += strOne;
						}
					}
					tmpLine = tmpLine.replaceFirst(replacedStr, "");
				} else {
					properties.put(field_name, null);
				}
				int cutIndex2 = tmpLine.indexOf(",");
				tmpLine = tmpLine.substring(cutIndex2 + 1);
			}
			return properties;
		} else {
			HashMap<String, Object> properties = new HashMap<String, Object>();
			List<NDAField> fields = ndaHeader.getAspatial_field_def();
			for (NDAField field : fields) {
				String field_name = field.getFieldName();
				properties.put(field_name, null);
			}
			return properties;
		}
	}

	private SimpleFeature parseSimpleFeature(String featureID, Geometry geom, String originType,
			HashMap<String, Object> properties) throws SchemaException {

		String geomType = geom.getGeometryType();
		int size = properties.size();
		Object[] propertyObj = new Object[size + 2];
		propertyObj[0] = geom;
		int j = 1;
		String temp = "";

		Iterator<?> iterator = properties.keySet().iterator();
		while (iterator.hasNext()) {
			String key = iterator.next().toString();
			if (key.equals("the_geom")) {
				continue;
			} else {
				Object value = properties.get(key);
				String valueType = value.getClass().getSimpleName();
				if (valueType.equals("Long")) {
					valueType = "String";
					propertyObj[j] = value.toString();
					temp += key + ":" + valueType + ",";
				} else {
					propertyObj[j] = value;
					temp += key + ":" + valueType + ",";
				}
			}
			j++;
		}
		temp += "originType:String";
		propertyObj[j] = originType;
		// create sf
		SimpleFeatureType simpleFeatureType = DataUtilities.createType(featureID.toString(),
				"the_geom:" + geomType + "," + temp);
		SimpleFeature simpleFeature = SimpleFeatureBuilder.build(simpleFeatureType, propertyObj, featureID);
		return simpleFeature;
	}

	private SimpleFeature parseSimpleFeature(String featureID, Geometry geom, String originType)
			throws SchemaException {

		String geomType = geom.getGeometryType();
		// create sf
		SimpleFeatureType simpleFeatureType = DataUtilities.createType(featureID.toString(),
				"featureID:String,the_geom:" + geomType + "originType:" + originType);
		SimpleFeature simpleFeature = SimpleFeatureBuilder.build(simpleFeatureType,
				new Object[] { featureID, geom, originType }, featureID);
		return simpleFeature;
	}
}
