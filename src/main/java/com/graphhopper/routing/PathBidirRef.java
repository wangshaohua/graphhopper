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
package com.graphhopper.routing;

import com.graphhopper.routing.util.WeightCalculation;
import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.Graph;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.GraphUtility;

/**
 * This class creates a DijkstraPath from two Edge's resulting from a
 * BidirectionalDijkstra
 *
 * @author Peter Karich,
 */
public class PathBidirRef extends Path {

    protected EdgeEntry edgeTo;
    private boolean switchWrapper = false;

    public PathBidirRef(Graph g, WeightCalculation weightCalculation) {
        super(g, weightCalculation);
        weight = INIT_VALUE;
    }

    PathBidirRef(PathBidirRef p) {
        super(p);
        edgeTo = p.edgeTo;
        switchWrapper = p.switchWrapper;
    }

    public PathBidirRef switchToFrom(boolean b) {
        switchWrapper = b;
        return this;
    }

    public PathBidirRef edgeEntryTo(EdgeEntry edgeTo) {
        this.edgeTo = edgeTo;
        return this;
    }

    /**
     * Extracts path from two shortest-path-tree
     */
    @Override
    public Path extract() {
        weight = 0;
        if (edgeEntry == null || edgeTo == null)
            return this;

        int from = GraphUtility.getToNode(graph, edgeEntry.edge, edgeEntry.endNode);
        int to = GraphUtility.getToNode(graph, edgeTo.edge, edgeTo.endNode);
        if (from != to)
            throw new IllegalStateException("Locations of the 'to'- and 'from'-Edge has to be the same." + toString());

        sw.start();
        if (switchWrapper) {
            EdgeEntry ee = edgeEntry;
            edgeEntry = edgeTo;
            edgeTo = ee;
        }

        EdgeEntry currEdge = edgeEntry;
        while (EdgeIterator.Edge.isValid(currEdge.edge)) {
            processWeight(currEdge.edge, currEdge.endNode);
            currEdge = currEdge.parent;
        }
        fromNode(currEdge.endNode);
        reverseOrder();
        currEdge = edgeTo;
        int tmpEdge = currEdge.edge;
        while (EdgeIterator.Edge.isValid(tmpEdge)) {
            currEdge = currEdge.parent;
            processWeight(tmpEdge, currEdge.endNode);
            tmpEdge = currEdge.edge;
        }
        sw.stop();
        return found(true);
    }
}
