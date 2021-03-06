/*
 *  Licensed to Peter Karich under one or more contributor license 
 *  agreements. See the NOTICE file distributed with this work for 
 *  additional information regarding copyright ownership.
 * 
 *  Peter Karich licenses this file to you under the Apache License, 
 *  Version 2.0 (the "License"); you may not use this file except 
 *  in compliance with the License. You may obtain a copy of the 
 *  License at
 * 
 *       http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.graphhopper.storage;

import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.EdgeSkipIterator;

/**
 * A Graph necessary for shortcut algorithms like Contraction Hierarchies. This
 * class enables the storage to hold the level of a node and a shortcut edge per
 * edge.
 *
 * @see GraphBuilder
 * @author Peter Karich
 */
public class LevelGraphStorage extends GraphStorage implements LevelGraph {

    private final int I_SKIP_EDGE1;
    private final int I_SKIP_EDGE2;
    private final int I_LEVEL;

    public LevelGraphStorage(Directory dir) {
        super(dir);
        I_SKIP_EDGE1 = nextEdgeEntryIndex();
        I_SKIP_EDGE2 = nextEdgeEntryIndex();
        I_LEVEL = nextNodeEntryIndex();
        initNodeAndEdgeEntrySize();
    }

    @Override public final void setLevel(int index, int level) {
        ensureNodeIndex(index);
        nodes.setInt((long) index * nodeEntrySize + I_LEVEL, level);
    }

    @Override public final int getLevel(int index) {
        ensureNodeIndex(index);
        return nodes.getInt((long) index * nodeEntrySize + I_LEVEL);
    }

    @Override protected GraphStorage newThis(Directory dir) {
        return new LevelGraphStorage(dir);
    }

    @Override public EdgeSkipIterator edge(int a, int b, double distance, boolean bothDir) {
        return (EdgeSkipIterator) super.edge(a, b, distance, bothDir);
    }

    @Override public EdgeSkipIterator edge(int a, int b, double distance, int flags) {
        ensureNodeIndex(Math.max(a, b));
        int edgeId = internalEdgeAdd(a, b, distance, flags);
        EdgeSkipIterator iter = new EdgeSkipIteratorImpl(edgeId, a, false, false);
        iter.next();
        iter.skippedEdges(EdgeIterator.NO_EDGE, EdgeIterator.NO_EDGE);
        return iter;
    }

    @Override
    public EdgeSkipIterator getEdges(int node) {
        return createEdgeIterable(node, true, true);
    }

    @Override
    public EdgeSkipIterator getIncoming(int node) {
        return createEdgeIterable(node, true, false);
    }

    @Override
    public EdgeSkipIterator getOutgoing(int node) {
        return createEdgeIterable(node, false, true);
    }

    @Override
    protected EdgeSkipIterator createEdgeIterable(int baseNode, boolean in, boolean out) {
        int edge = nodes.getInt((long) baseNode * nodeEntrySize + N_EDGE_REF);
        return new EdgeSkipIteratorImpl(edge, baseNode, in, out);
    }

    class EdgeSkipIteratorImpl extends EdgeIterable implements EdgeSkipIterator {

        public EdgeSkipIteratorImpl(int edge, int node, boolean in, boolean out) {
            super(edge, node, in, out);
        }

        @Override public void skippedEdges(int edge1, int edge2) {
            if (EdgeIterator.Edge.isValid(edge1) != EdgeIterator.Edge.isValid(edge2))
                throw new IllegalStateException("Skipped edges of a shortcuts needs "
                        + "to be both valid but wasn't " + edge1 + ", " + edge2);
            edges.setInt(edgePointer + I_SKIP_EDGE1, edge1);
            edges.setInt(edgePointer + I_SKIP_EDGE2, edge2);
        }

        @Override public int skippedEdge1() {
            return edges.getInt(edgePointer + I_SKIP_EDGE1);
        }

        @Override public int skippedEdge2() {
            return edges.getInt(edgePointer + I_SKIP_EDGE2);
        }

        @Override public boolean isShortcut() {
            return EdgeIterator.Edge.isValid(skippedEdge1());
        }
    }

    /**
     * TODO hide this lower level API somehow. Removes the edge in one
     * direction.
     */
    public int disconnect(EdgeIterator iter, long prevEdgePointer, boolean sameDirection) {
        // open up package protected API for now ...
        if (sameDirection)
            internalEdgeDisconnect(iter.edge(), prevEdgePointer, iter.baseNode(), iter.node());
        else {
            // prevEdgePointer belongs to baseNode ... but now we need it for node()!
            EdgeSkipIterator tmpIter = getEdges(iter.node());
            int tmpPrevEdge = EdgeIterator.NO_EDGE;
            boolean found = false;
            while (tmpIter.next()) {
                if (tmpIter.edge() == iter.edge()) {
                    found = true;
                    break;
                }

                tmpPrevEdge = tmpIter.edge();
            }
            if (found) {
                // System.out.println("disconnect " + iter.node() + "->" + iter.baseNode() + " (" + iter.edge() + ") " + GraphUtility.count(getEdges(iter.node())));
                internalEdgeDisconnect(iter.edge(), (long) tmpPrevEdge * edgeEntrySize, iter.node(), iter.baseNode());
                // System.out.println(" .. " + GraphUtility.count(getEdges(iter.node())));
            }
        }
        return iter.edge();
    }

    @Override
    public EdgeSkipIterator getEdgeProps(int edgeId, int endNode) {
        return (EdgeSkipIterator) super.getEdgeProps(edgeId, endNode);
    }

    @Override
    protected SingleEdge createSingleEdge(int edge, int nodeId) {
        return new SingleLevelEdge(edge, nodeId);
    }

    class SingleLevelEdge extends SingleEdge implements EdgeSkipIterator {

        public SingleLevelEdge(int edge, int nodeId) {
            super(edge, nodeId);
        }

        @Override public void skippedEdges(int edge1, int edge2) {
            edges.setInt(edgePointer + I_SKIP_EDGE1, edge1);
            edges.setInt(edgePointer + I_SKIP_EDGE2, edge2);
        }

        @Override public int skippedEdge1() {
            return edges.getInt(edgePointer + I_SKIP_EDGE1);
        }

        @Override public int skippedEdge2() {
            return edges.getInt(edgePointer + I_SKIP_EDGE2);
        }

        @Override public boolean isShortcut() {
            return EdgeIterator.Edge.isValid(skippedEdge1());
        }
    }
}
