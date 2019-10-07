package com.git.gdsbuilder.validator.quad;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.index.quadtree.IntervalSize;
import com.vividsolutions.jts.util.Assert;

/**
 * QuadRoot is the root of a single Quadtree. It is centred at the origin, and
 * does not have a defined extent.
 *
 * @version 1.7
 */
public class Root extends NodeBase {

	// the singleton root quad is centred at the origin.
	private static final Coordinate origin = new Coordinate(0.0, 0.0);

	public Root() {
	}

	/**
	 * Insert an item into the quadtree this is the root of.
	 */
	public void insert(Envelope itemEnv, Object item) {
		int index = getSubnodeIndex(itemEnv, origin.x, origin.y);
		// if index is -1, itemEnv must cross the X or Y axis.
		if (index == -1) {
			add(item);
			return;
		}
		/**
		 * the item must be contained in one quadrant, so insert it into the tree for
		 * that quadrant (which may not yet exist)
		 */
		Node node = subnode[index];
		/**
		 * If the subquad doesn't exist or this item is not contained in it, have to
		 * expand the tree upward to contain the item.
		 */

		if (node == null || !node.getEnvelope().contains(itemEnv)) {
			Node largerNode = Node.createExpanded(node, itemEnv);
			subnode[index] = largerNode;
		}
		/**
		 * At this point we have a subquad which exists and must contain contains the
		 * env for the item. Insert the item into the tree.
		 */
		insertContained(subnode[index], itemEnv, item);
		// System.out.println("depth = " + root.depth() + " size = " + root.size());
		// System.out.println(" size = " + size());
	}

	/**
	 * insert an item which is known to be contained in the tree rooted at the given
	 * QuadNode root. Lower levels of the tree will be created if necessary to hold
	 * the item.
	 */
	private void insertContained(Node tree, Envelope itemEnv, Object item) {
		Assert.isTrue(tree.getEnvelope().contains(itemEnv));
		/**
		 * Do NOT create a new quad for zero-area envelopes - this would lead to
		 * infinite recursion. Instead, use a heuristic of simply returning the smallest
		 * existing quad containing the query
		 */
		boolean isZeroX = IntervalSize.isZeroWidth(itemEnv.getMinX(), itemEnv.getMaxX());
		boolean isZeroY = IntervalSize.isZeroWidth(itemEnv.getMinY(), itemEnv.getMaxY());
		NodeBase node;
		if (isZeroX || isZeroY)
			node = tree.find(itemEnv);
		else
			node = tree.getNode(itemEnv);
		node.add(item);
	}

	protected boolean isSearchMatch(Envelope searchEnv) {
		return true;
	}

	public int maxLevel() {

		int level = 0;
		Node[] subNodes = this.getSubnode();
		int length = subNodes.length;
		for (int i = 0; i < length; i++) {
			Node subNode = subNodes[i];
			if (subNode != null) {
				level = subNode.getLevel();
			}
		}
		return level;
	}

}
