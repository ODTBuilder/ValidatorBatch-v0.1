package com.git.gdsbuilder.validator.fileReader.dxf.parser;

import java.util.Iterator;

import org.kabeja.dxf.DXFVertex;
import org.kabeja.dxf.helpers.Point;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.util.GeometricShapeFactory;

public class DXFFileGeomParser {

	private static final String defaultEPSG = "EPSG:32652";

	// ARC
	public static Geometry parseDTArc(String epsg, Point pt, double radius, double startAng, double angExtent)
			throws Exception {

		// CenterPt
		Coordinate coor = new Coordinate(pt.getX(), pt.getY(), pt.getZ());

		GeometricShapeFactory f = new GeometricShapeFactory();
		f.setCentre(coor);
		f.setSize(radius * 2);
		f.setNumPoints(100);
		f.setRotation(0);

		Geometry arc = f.createArc(Math.toRadians(startAng), Math.toRadians(angExtent));
		// CoordinateReferenceSystem dataCRS = CRS.decode(epsg);
		// CoordinateReferenceSystem deFaultCRS = CRS.decode(defaultEPSG);
		// MathTransform transform = CRS.findMathTransform(dataCRS, deFaultCRS);
		//
		// return JTS.transform(arc, transform);
		return arc;
	}

	// CIRCLE
	public static Geometry parseDTCircle(String epsg, Point pt, double radius) throws Exception {

		Coordinate coor = new Coordinate(pt.getX(), pt.getY(), pt.getZ());
		GeometricShapeFactory f = new GeometricShapeFactory();
		f.setCentre(coor);
		f.setSize(radius * 2);
		f.setNumPoints(100);
		f.setRotation(0);

		Geometry circle = f.createCircle().getBoundary();
		// CoordinateReferenceSystem dataCRS = CRS.decode(epsg);
		// CoordinateReferenceSystem deFaultCRS = CRS.decode(defaultEPSG);
		// MathTransform transform = CRS.findMathTransform(dataCRS, deFaultCRS);
		//
		// return JTS.transform(circle, transform);

		return circle;
	}

	// TEXT, INSERT
	public static Geometry parseDTPoint(String epsg, Point pt) throws Exception {

		Coordinate coor = new Coordinate(pt.getX(), pt.getY(), pt.getZ());
		GeometryFactory gf = new GeometryFactory();

		Geometry geom = gf.createPoint(coor);
		// CoordinateReferenceSystem dataCRS = CRS.decode(epsg);
		// CoordinateReferenceSystem deFaultCRS = CRS.decode(defaultEPSG);
		// MathTransform transform = CRS.findMathTransform(dataCRS, deFaultCRS);
		//
		// return JTS.transform(geom, transform);
		return geom;
	}

	// LINE
	public static Geometry parseDTLine(String epsg, Point startPt, Point endPt) throws Exception {

		Coordinate startCoor = new Coordinate(startPt.getX(), startPt.getY(), startPt.getZ());
		Coordinate endCoor = new Coordinate(endPt.getX(), endPt.getY(), endPt.getZ());
		Coordinate[] lineCoor = new Coordinate[2];
		lineCoor[0] = startCoor;
		lineCoor[1] = endCoor;

		GeometryFactory gf = new GeometryFactory();
		Geometry line = gf.createLineString(lineCoor);

		// CoordinateReferenceSystem dataCRS = CRS.decode(epsg);
		// CoordinateReferenceSystem deFaultCRS = CRS.decode(defaultEPSG);
		// MathTransform transform = CRS.findMathTransform(dataCRS, deFaultCRS);
		//
		// return JTS.transform(line, transform);

		return line;
	}

	// POLYLINE
	public static Geometry parseDTLineString(String epsg, boolean isClosed, Iterator<?> vertexIterator, int vertexCount)
			throws Exception {

		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coors;
		if (isClosed) {
			coors = new Coordinate[vertexCount + 1];
			int i = 0;
			while (vertexIterator.hasNext()) {
				if (i < vertexCount) {
					DXFVertex dxfVertex = (DXFVertex) vertexIterator.next();
					coors[i] = new Coordinate(dxfVertex.getX(), dxfVertex.getY(), dxfVertex.getZ());
					i++;
				} else {
					break;
				}
			}
			coors[vertexCount] = coors[0];
		} else {
			coors = new Coordinate[vertexCount];
			int i = 0;
			while (vertexIterator.hasNext()) {
				if (i < vertexCount) {
					DXFVertex dxfVertex = (DXFVertex) vertexIterator.next();
					coors[i] = new Coordinate(dxfVertex.getX(), dxfVertex.getY(), dxfVertex.getZ());
					i++;
				} else {
					break;
				}
			}
		}
		Geometry line = gf.createLineString(coors);
		// CoordinateReferenceSystem dataCRS = CRS.decode(epsg);
		// CoordinateReferenceSystem deFaultCRS = CRS.decode(defaultEPSG);
		// MathTransform transform = CRS.findMathTransform(dataCRS, deFaultCRS);
		//
		// return JTS.transform(line, transform);
		return line;
	}

	// LWPOLYLINE
	public static Geometry parseDT3DLineString(String epsg, boolean isClosed, Iterator<?> vertexIterator, int vertexCount,
			double elevation) throws Exception {

		GeometryFactory gf = new GeometryFactory();
		Coordinate[] coors;
		if (isClosed) {
			coors = new Coordinate[vertexCount + 1];
			int i = 0;
			while (vertexIterator.hasNext()) {
				if (i < vertexCount) {
					DXFVertex dxfVertex = (DXFVertex) vertexIterator.next();
					coors[i] = new Coordinate(dxfVertex.getX(), dxfVertex.getY(), elevation);
					i++;
				} else {
					break;
				}
			}
			coors[vertexCount] = coors[0];
		} else {
			coors = new Coordinate[vertexCount];
			int i = 0;
			while (vertexIterator.hasNext()) {
				if (i < vertexCount) {
					DXFVertex dxfVertex = (DXFVertex) vertexIterator.next();
					coors[i] = new Coordinate(dxfVertex.getX(), dxfVertex.getY(), elevation);
					i++;
				} else {
					break;
				}
			}
		}
		Geometry line = gf.createLineString(coors);
		// CoordinateReferenceSystem dataCRS = CRS.decode(epsg);
		// CoordinateReferenceSystem deFaultCRS = CRS.decode(defaultEPSG);
		// MathTransform transform = CRS.findMathTransform(dataCRS, deFaultCRS);
		//
		// return JTS.transform(line, transform);

		return line;
	}

	// SOLID
	public static Geometry parseDTPolygon(String epsg, Point point1, Point point2, Point point3, Point point4)
			throws Exception {

		GeometryFactory gf = new GeometryFactory();
		Coordinate coor1 = new Coordinate(point1.getX(), point1.getY(), point1.getZ());
		Coordinate coor2 = new Coordinate(point2.getX(), point2.getY(), point2.getZ());
		Coordinate coor3 = new Coordinate(point3.getX(), point3.getY(), point3.getZ());
		Coordinate coor4 = new Coordinate(point4.getX(), point4.getY(), point4.getZ());

		LinearRing lr;
		Geometry polygon;
		if (coor3.equals3D(coor4)) {
			coor4 = coor1;
			lr = gf.createLinearRing(new Coordinate[] { coor1, coor2, coor3, coor4 });
			polygon = gf.createPolygon(lr, null);
		} else {
			lr = gf.createLinearRing(new Coordinate[] { coor1, coor2, coor3, coor4 });
			polygon = gf.createPolygon(lr, null);
		}

		// CoordinateReferenceSystem dataCRS = CRS.decode(epsg);
		// CoordinateReferenceSystem deFaultCRS = CRS.decode(defaultEPSG);
		// MathTransform transform = CRS.findMathTransform(dataCRS, deFaultCRS);
		//
		// return JTS.transform(polygon, transform);

		return polygon;
	}

	/**
	 * @author DY.Oh
	 * @since 2018. 2. 26. 오후 3:30:10
	 * @param maximumX
	 * @param maximumY
	 * @param minimumX
	 * @param minimumY
	 * @return Geometry
	 * @decription
	 */
	public static Geometry parseDTPolygon(String epsg, double maximumX, double maximumY, double minimumX,
			double minimumY) throws Exception {

		GeometryFactory gf = new GeometryFactory();

		Coordinate coor1 = new Coordinate(maximumX, maximumY);
		Coordinate coor2 = new Coordinate(maximumX, minimumY);
		Coordinate coor3 = new Coordinate(minimumX, minimumY);
		Coordinate coor4 = new Coordinate(minimumX, maximumY);

		Coordinate[] coors = new Coordinate[] { coor1, coor2, coor3, coor4, coor1 };
		LinearRing lr = gf.createLinearRing(coors);

		Geometry polygon = gf.createPolygon(lr, null);
		// CoordinateReferenceSystem dataCRS = CRS.decode(epsg);
		// CoordinateReferenceSystem deFaultCRS = CRS.decode(defaultEPSG);
		// MathTransform transform = CRS.findMathTransform(dataCRS, deFaultCRS);
		//
		// return JTS.transform(polygon, transform);

		return polygon;
	}
}
