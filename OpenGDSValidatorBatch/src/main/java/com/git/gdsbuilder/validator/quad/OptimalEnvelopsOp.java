package com.git.gdsbuilder.validator.quad;

import java.util.ArrayList;
import java.util.List;

import com.vividsolutions.jts.geom.Envelope;

public class OptimalEnvelopsOp {

	private Quadtree quadTree;

	private int maxSize;

	private int maxLevel;

	private int optimalLevel;

	public OptimalEnvelopsOp(Quadtree quadTree, int maxLevel, int maxSize) {
		this.quadTree = quadTree;
		this.maxLevel = maxLevel;
		this.maxSize = maxSize;
	}

	public List<Envelope> getOptimalEnvelops(int level) {

		boolean isOptimal = true;
		Node[] nodes = quadTree.getRoot().getSubnode();
		List<Envelope> envelopeList = getNodeEnvelopeList(nodes, level);
//		DefaultFeatureCollection dfc = new DefaultFeatureCollection();
//		SimpleFeatureType sfType = null;
//		try {
//			sfType = DataUtilities.createType("envelop", "ID:String,the_geom:Polygon");
//		} catch (SchemaException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//		int i = 1;
//		GeometryFactory f = new GeometryFactory();
//		for (Object result : envelopeList) {
//			Envelope envelope = (Envelope) result;
//			SimpleFeature sfI = SimpleFeatureBuilder.build(sfType,
//					new Object[] { String.valueOf(i), f.toGeometry(envelope) }, String.valueOf(i));
//			dfc.add(sfI);
//			i++;
//		}
//		try {
//			SHPFileWriter.writeSHP("EPSG:4326", dfc, "C:\\Users\\GIT\\Desktop\\남수단_\\test" + level + ".shp");
//		} catch (IOException | SchemaException | FactoryException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}

		for (Object result : envelopeList) {
			Envelope envelope = (Envelope) result;
			List items = quadTree.query(envelope);
			int size = items.size();
			if (size > maxSize || size == 0) {
				isOptimal = false;
				break;
			}
		}
		if (isOptimal) {
			this.optimalLevel = level;
			return envelopeList;
		} else {
			return getOptimalEnvelops(level - 1);
		}
	}

	public List<Envelope> getNodeEnvelopeList(Node[] nodes, int level) {

		List<Envelope> envelopeList = new ArrayList<>();

		int length = nodes.length;
		Node[] levelNodes = new Node[quadTree.size()];
		int n = 0;
		for (int i = 0; i < length; i++) {
			Node node = nodes[i];
			if (node != null) {
				int subLevel = node.getLevel();
				if (subLevel == level) {
					envelopeList.add(node.getEnvelope());
				} else if (subLevel > level) {
					Node[] subLodes = node.getSubnode();
					int subLength = subLodes.length;
					for (int s = 0; s < subLength; s++) {
						Node subNode = subLodes[s];
						if (subNode != null) {
							levelNodes[n] = subLodes[s];
							n++;
						}
					}
				} else {
					break;
				}
			}
		}
		if (n > 0) {
			return getNodeEnvelopeList(levelNodes, level);
		} else {
			return envelopeList;
		}
	}

	public Quadtree getQuadTree() {
		return quadTree;
	}

	public void setQuadTree(Quadtree quadTree) {
		this.quadTree = quadTree;
	}

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxSize) {
		this.maxSize = maxSize;
	}

	public int getLevel() {
		return maxLevel;
	}

	public void setLevel(int level) {
		this.maxLevel = level;
	}

}
