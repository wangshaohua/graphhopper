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

import com.graphhopper.routing.util.CarStreetType;
import com.graphhopper.util.EdgeIterator;
import com.graphhopper.util.GraphUtility;
import static com.graphhopper.util.GraphUtility.*;
import com.graphhopper.util.Helper;
import com.graphhopper.util.PointList;
import com.graphhopper.util.shapes.BBox;
import java.io.File;
import java.util.Arrays;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract test class to be extended for implementations of the Graph
 * interface. Graphs implementing GraphStorage should extend GraphStorageTest
 * instead.
 *
 * @author Peter Karich,
 */
public abstract class AbstractGraphTester {

    private String location = "./target/graphstorage";
    protected int defaultSize = 100;
    protected String defaultGraph = "./target/graphstorage/default";

    protected Graph createGraph() {
        return createGraph(defaultGraph, defaultSize);
    }

    abstract Graph createGraph(String location, int size);

    @Before
    public void tearDown() {
        Helper.removeDir(new File(location));
    }

    @Before
    public void setUp() {
        Helper.removeDir(new File(location));
    }

    @Test
    public void testSetNodes() {
        Graph graph = createGraph();
        for (int i = 0; i < defaultSize * 2; i++) {
            graph.setNode(i, 2 * i, 3 * i);
        }
        graph.edge(defaultSize + 1, defaultSize + 2, 10, true);
        graph.edge(defaultSize + 1, defaultSize + 3, 10, true);
        assertEquals(2, GraphUtility.count(graph.getEdges(defaultSize + 1)));
    }

    @Test
    public void testCreateLocation() {
        Graph graph = createGraph();
        graph.edge(3, 1, 50, true);
        assertEquals(1, count(graph.getOutgoing(1)));

        graph.edge(1, 2, 100, true);
        assertEquals(2, count(graph.getOutgoing(1)));
    }

    @Test
    public void testEdges() {
        Graph graph = createGraph();
        graph.edge(2, 1, 12, true);
        assertEquals(1, count(graph.getOutgoing(2)));

        graph.edge(2, 3, 12, true);
        assertEquals(1, count(graph.getOutgoing(1)));
        assertEquals(2, count(graph.getOutgoing(2)));
        assertEquals(1, count(graph.getOutgoing(3)));
    }

    @Test
    public void testUnidirectional() {
        Graph g = createGraph();

        g.edge(1, 2, 12, false);
        g.edge(1, 11, 12, false);
        g.edge(11, 1, 12, false);
        g.edge(1, 12, 12, false);
        g.edge(3, 2, 112, false);
        EdgeIterator i = g.getOutgoing(2);
        assertFalse(i.next());

        assertEquals(1, GraphUtility.count(g.getIncoming(1)));
        assertEquals(2, GraphUtility.count(g.getIncoming(2)));
        assertEquals(0, GraphUtility.count(g.getIncoming(3)));

        assertEquals(3, GraphUtility.count(g.getOutgoing(1)));
        assertEquals(0, GraphUtility.count(g.getOutgoing(2)));
        assertEquals(1, GraphUtility.count(g.getOutgoing(3)));
        i = g.getOutgoing(3);
        i.next();
        assertEquals(2, i.node());

        i = g.getOutgoing(1);
        assertTrue(i.next());
        assertEquals(2, i.node());
        assertTrue(i.next());
        assertEquals(11, i.node());
        assertTrue(i.next());
        assertEquals(12, i.node());
        assertFalse(i.next());
    }

    @Test
    public void testUnidirectionalEdgeFilter() {
        Graph g = createGraph();

        g.edge(1, 2, 12, false);
        g.edge(1, 11, 12, false);
        g.edge(11, 1, 12, false);
        g.edge(1, 12, 12, false);
        g.edge(3, 2, 112, false);
        EdgeIterator i = g.getOutgoing(2);
        assertFalse(i.next());

        assertEquals(1, GraphUtility.count(g.getIncoming(1)));
        assertEquals(2, GraphUtility.count(g.getIncoming(2)));
        assertEquals(0, GraphUtility.count(g.getIncoming(3)));

        assertEquals(3, GraphUtility.count(g.getOutgoing(1)));
        assertEquals(0, GraphUtility.count(g.getOutgoing(2)));
        assertEquals(1, GraphUtility.count(g.getOutgoing(3)));
        i = g.getOutgoing(3);
        i.next();
        assertEquals(2, i.node());

        i = g.getOutgoing(1);
        assertTrue(i.next());
        assertEquals(2, i.node());
        assertTrue(i.next());
        assertEquals(11, i.node());
        assertTrue(i.next());
        assertEquals(12, i.node());
        assertFalse(i.next());
    }

    @Test
    public void testUpdateUnidirectional() {
        Graph g = createGraph();

        g.edge(1, 2, 12, false);
        g.edge(3, 2, 112, false);
        EdgeIterator i = g.getOutgoing(2);
        assertFalse(i.next());
        i = g.getOutgoing(3);
        assertTrue(i.next());
        assertEquals(2, i.node());
        assertFalse(i.next());

        g.edge(2, 3, 112, false);
        i = g.getOutgoing(2);
        assertTrue(i.next());
        assertEquals(3, i.node());
        i = g.getOutgoing(3);
        i.next();
        assertEquals(2, i.node());
        assertFalse(i.next());
    }

    @Test
    public void testClone() {
        Graph g = createGraph();
        g.edge(1, 2, 10, true);
        g.setNode(0, 12, 23);
        g.setNode(1, 8, 13);
        g.setNode(2, 2, 10);
        g.setNode(3, 5, 9);
        g.edge(1, 3, 10, true);

        Graph clone = g.copyTo(createGraph(location + "/clone", defaultSize));
        assertEquals(g.nodes(), clone.nodes());
        assertEquals(count(g.getOutgoing(1)), count(clone.getOutgoing(1)));
        clone.edge(1, 4, 10, true);
        assertEquals(3, count(clone.getOutgoing(1)));
        assertEquals(g.bounds(), clone.bounds());
    }

    @Test
    public void testGetLocations() {
        Graph g = createGraph();
        g.setNode(0, 12, 23);
        g.setNode(1, 22, 23);
        assertEquals(2, g.nodes());

        g.edge(0, 1, 10, true);
        assertEquals(2, g.nodes());

        g.edge(0, 2, 10, true);
        assertEquals(3, g.nodes());

        g = createGraph();
        assertEquals(0, g.nodes());
    }

    protected void initExampleGraph(Graph g) {
        g.setNode(0, 12, 23);
        g.setNode(1, 38.33f, 235.3f);
        g.setNode(2, 6, 339);
        g.setNode(3, 78, 89);
        g.setNode(4, 2, 1);
        g.setNode(5, 7, 5);
        g.edge(0, 1, 12, true);
        g.edge(0, 2, 212, true);
        g.edge(0, 3, 212, true);
        g.edge(0, 4, 212, true);
        g.edge(0, 5, 212, true);
    }

    @Test
    public void testAddLocation() {
        Graph g = createGraph();
        initExampleGraph(g);

        assertEquals(12f, g.getLatitude(0), 1e-6);
        assertEquals(23f, g.getLongitude(0), 1e-6);

        assertEquals(38.33f, g.getLatitude(1), 1e-6);
        assertEquals(235.3f, g.getLongitude(1), 1e-6);

        assertEquals(6, g.getLatitude(2), 1e-6);
        assertEquals(339, g.getLongitude(2), 1e-6);

        assertEquals(78, g.getLatitude(3), 1e-6);
        assertEquals(89, g.getLongitude(3), 1e-6);

        assertEquals(1, count(g.getOutgoing(1)));
        assertEquals(5, count(g.getOutgoing(0)));
        try {
            assertEquals(0, count(g.getOutgoing(6)));
            // for now return empty iterator
            // assertFalse(true);
        } catch (Exception ex) {
        }
    }

    @Test
    public void testDirectional() {
        Graph g = createGraph();
        g.edge(1, 2, 12, true);
        g.edge(2, 3, 12, false);
        g.edge(3, 4, 12, false);
        g.edge(3, 5, 12, true);
        g.edge(6, 3, 12, false);

        assertEquals(1, count(g.getEdges(1)));
        assertEquals(1, count(g.getIncoming(1)));
        assertEquals(1, count(g.getOutgoing(1)));

        assertEquals(2, count(g.getEdges(2)));
        assertEquals(1, count(g.getIncoming(2)));
        assertEquals(2, count(g.getOutgoing(2)));

        assertEquals(4, count(g.getEdges(3)));
        assertEquals(3, count(g.getIncoming(3)));
        assertEquals(2, count(g.getOutgoing(3)));

        assertEquals(1, count(g.getEdges(4)));
        assertEquals(1, count(g.getIncoming(4)));
        assertEquals(0, count(g.getOutgoing(4)));

        assertEquals(1, count(g.getEdges(5)));
        assertEquals(1, count(g.getIncoming(5)));
        assertEquals(1, count(g.getOutgoing(5)));
    }

    @Test
    public void testDozendEdges() {
        Graph g = createGraph();
        g.edge(1, 2, 12, true);
        assertEquals(1, count(g.getEdges(1)));

        g.edge(1, 3, 13, false);
        assertEquals(2, count(g.getEdges(1)));

        g.edge(1, 4, 14, false);
        assertEquals(3, count(g.getEdges(1)));

        g.edge(1, 5, 15, false);
        assertEquals(4, count(g.getEdges(1)));

        g.edge(1, 6, 16, false);
        assertEquals(5, count(g.getEdges(1)));

        g.edge(1, 7, 16, false);
        assertEquals(6, count(g.getEdges(1)));

        g.edge(1, 8, 16, false);
        assertEquals(7, count(g.getEdges(1)));

        g.edge(1, 9, 16, false);
        assertEquals(8, count(g.getEdges(1)));
        assertEquals(8, count(g.getOutgoing(1)));
        assertEquals(1, count(g.getIncoming(1)));
        assertEquals(1, count(g.getIncoming(2)));
    }

    @Test
    public void testCheckFirstNode() {
        Graph g = createGraph();
        assertEquals(0, count(g.getEdges(1)));
        g.edge(0, 1, 12, true);
        assertEquals(1, count(g.getEdges(1)));
    }

    @Test
    public void testDeleteNodeForUnidir() {
        Graph g = createGraph();
        g.setNode(10, 10, 1);
        g.setNode(6, 6, 1);
        g.setNode(20, 20, 1);
        g.setNode(21, 21, 1);

        g.edge(10, 20, 10, false);
        g.edge(21, 6, 10, false);

        g.markNodeRemoved(0);
        g.markNodeRemoved(7);
        assertEquals(22, g.nodes());
        g.optimize();
        assertEquals(20, g.nodes());

        assertEquals(1, GraphUtility.count(g.getIncoming(getIdOf(g, 20))));
        assertEquals(0, GraphUtility.count(g.getOutgoing(getIdOf(g, 20))));

        assertEquals(1, GraphUtility.count(g.getOutgoing(getIdOf(g, 10))));
        assertEquals(0, GraphUtility.count(g.getIncoming(getIdOf(g, 10))));

        assertEquals(1, GraphUtility.count(g.getIncoming(getIdOf(g, 6))));
        assertEquals(0, GraphUtility.count(g.getOutgoing(getIdOf(g, 6))));

        assertEquals(1, GraphUtility.count(g.getOutgoing(getIdOf(g, 21))));
        assertEquals(0, GraphUtility.count(g.getIncoming(getIdOf(g, 21))));
    }

    @Test
    public void testComplexDeleteNode() {
        testDeleteNodes(21);
    }

    @Test
    public void testComplexDeleteNode2() {
        testDeleteNodes(6);
    }

    public void testDeleteNodes(int fillToSize) {
        Graph g = createGraph();
        g.setNode(0, 12, 23);
        g.setNode(1, 38.33f, 135.3f);
        g.setNode(2, 3, 3);
        g.setNode(3, 78, 89);
        g.setNode(4, 2, 1);
        g.setNode(5, 2.5f, 1);

        int deleted = 2;
        for (int i = 6; i < fillToSize; i++) {
            g.setNode(i, i * 1.5, i * 1.6);
            if (i % 3 == 0) {
                g.markNodeRemoved(i);
                deleted++;
            } else {
                // connect to
                // ... a deleted node
                g.edge(i, 0, 10 * i, true);
                // ... a non-deleted and non-moved node
                g.edge(i, 2, 10 * i, true);
                // ... a moved node
                g.edge(i, fillToSize - 1, 10 * i, true);
            }
        }

        g.edge(0, 1, 10, true);
        g.edge(0, 3, 20, false);
        g.edge(3, 5, 20, true);
        g.edge(1, 5, 20, false);

        g.markNodeRemoved(0);
        g.markNodeRemoved(2);
        // no deletion happend
        assertEquals(fillToSize, g.nodes());

        assertEquals(Arrays.<String>asList(), GraphUtility.getProblems(g));

        // now actually perform deletion
        g.optimize();

        assertEquals(Arrays.<String>asList(), GraphUtility.getProblems(g));

        assertEquals(fillToSize - deleted, g.nodes());
        int id1 = getIdOf(g, 38.33f);
        assertEquals(135.3f, g.getLongitude(id1), 1e-4);
        assertTrue(containsLatitude(g, g.getEdges(id1), 2.5));
        assertFalse(containsLatitude(g, g.getEdges(id1), 12));

        int id3 = getIdOf(g, 78);
        assertEquals(89, g.getLongitude(id3), 1e-4);
        assertTrue(containsLatitude(g, g.getEdges(id3), 2.5));
        assertFalse(containsLatitude(g, g.getEdges(id3), 12));
    }

    public boolean containsLatitude(Graph g, EdgeIterator iter, double latitude) {
        while (iter.next()) {
            if (Math.abs(g.getLatitude(iter.node()) - latitude) < 1e-4) {
                return true;
            }
        }
        return false;
    }

    public static int getIdOf(Graph g, double latitude) {
        int s = g.nodes();
        for (int i = 0; i < s; i++) {
            if (Math.abs(g.getLatitude(i) - latitude) < 1e-4) {
                return i;
            }
        }
        return -1;
    }

    public static void assertPList(PointList expected, PointList list) {
        assertEquals("size of point lists is not equal", expected.size(), list.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(expected.latitude(i), list.latitude(i), 1e-4);
            assertEquals(expected.longitude(i), list.longitude(i), 1e-4);
        }
    }

    @Test
    public void testSimpleDelete() {
        Graph g = createGraph();
        g.setNode(0, 12, 23);
        g.setNode(1, 38.33f, 135.3f);
        g.setNode(2, 3, 3);
        g.setNode(3, 78, 89);

        g.edge(3, 0, 21, true);
        g.edge(5, 0, 22, true);
        g.edge(5, 3, 23, true);

        g.markNodeRemoved(0);
        g.markNodeRemoved(3);

        assertEquals(6, g.nodes());
        assertEquals(Arrays.<String>asList(), GraphUtility.getProblems(g));

        // now actually perform deletion
        g.optimize();

        assertEquals(4, g.nodes());
        assertEquals(Arrays.<String>asList(), GraphUtility.getProblems(g));
        // shouldn't change anything
        g.optimize();
        assertEquals(4, g.nodes());
        assertEquals(Arrays.<String>asList(), GraphUtility.getProblems(g));
    }

    @Test
    public void testSimpleDelete2() {
        Graph g = createGraph();
        g.setNode(9, 9, 1);
        g.setNode(11, 11, 1);
        g.setNode(12, 12, 1);

        // mini subnetwork which gets completely removed:
        g.edge(5, 10, 510, true);
        g.markNodeRemoved(5);
        g.markNodeRemoved(10);

        g.edge(9, 11, 911, true);
        g.edge(9, 12, 912, true);

        assertEquals(13, g.nodes());
        assertEquals(Arrays.<String>asList(), GraphUtility.getProblems(g));

        // perform deletion
        g.optimize();

        assertEquals(11, g.nodes());
        assertEquals(Arrays.<String>asList(), GraphUtility.getProblems(g));
        assertEquals(2, GraphUtility.count(g.getEdges(getIdOf(g, 9))));
        assertEquals(1, GraphUtility.count(g.getEdges(getIdOf(g, 11))));
        assertEquals(1, GraphUtility.count(g.getEdges(getIdOf(g, 12))));
    }

    @Test
    public void testSimpleDelete3() {
        Graph g = createGraph();
        g.setNode(7, 7, 1);
        g.setNode(8, 8, 1);
        g.setNode(9, 9, 1);
        g.setNode(11, 11, 1);

        // mini subnetwork which gets completely removed:
        g.edge(5, 10, 510, true);
        g.markNodeRemoved(3);
        g.markNodeRemoved(4);
        g.markNodeRemoved(5);
        g.markNodeRemoved(10);

        g.edge(9, 11, 911, true);
        g.edge(7, 9, 78, true);
        g.edge(8, 9, 89, true);

        // perform deletion
        g.optimize();

        assertEquals(Arrays.<String>asList(), GraphUtility.getProblems(g));

        assertEquals(3, GraphUtility.count(g.getEdges(getIdOf(g, 9))));
        assertEquals(1, GraphUtility.count(g.getEdges(getIdOf(g, 7))));
        assertEquals(1, GraphUtility.count(g.getEdges(getIdOf(g, 8))));
        assertEquals(1, GraphUtility.count(g.getEdges(getIdOf(g, 11))));
    }

    @Test
    public void testDeleteAndOptimize() {
        Graph g = createGraph();
        g.setNode(20, 10, 10);
        g.setNode(21, 10, 11);
        g.markNodeRemoved(20);
        g.optimize();
        assertEquals(11, g.getLongitude(20), 1e-5);
    }

    @Test
    public void testBounds() {
        Graph graph = createGraph();
        BBox b = graph.bounds();
        assertEquals(BBox.INVERSE.maxLat, b.maxLat, 1e-6);

        graph.setNode(0, 10, 20);
        assertEquals(10, b.maxLat, 1e-6);
        assertEquals(20, b.maxLon, 1e-6);

        graph.setNode(0, 15, -15);
        assertEquals(15, b.maxLat, 1e-6);
        assertEquals(20, b.maxLon, 1e-6);
        assertEquals(10, b.minLat, 1e-6);
        assertEquals(-15, b.minLon, 1e-6);
    }

    @Test
    public void testFlags() {
        Graph graph = createGraph();
        graph.edge(0, 1, 10, CarStreetType.flags(120, true));
        graph.edge(2, 3, 10, CarStreetType.flags(10, false));

        EdgeIterator iter = graph.getEdges(0);
        assertTrue(iter.next());
        assertEquals(CarStreetType.flags(120, true), iter.flags());

        iter = graph.getEdges(2);
        assertTrue(iter.next());
        assertEquals(CarStreetType.flags(10, false), iter.flags());
    }

    @Test
    public void testCopyTo() {
        Graph someGraphImpl = createGraph();        
        initExampleGraph(someGraphImpl);
        Graph gs = new GraphStorage(new RAMDirectory()).segmentSize(8000).createNew(10);
        try {
            someGraphImpl.copyTo(gs);
        } catch (Exception ex) {
            assertTrue(false);
        }

        try {
            gs.copyTo(someGraphImpl);
        } catch (Exception ex) {
            assertTrue(ex.toString(), false);
        }
    }

    @Test
    public void testEnsureSize() {
        Directory dir = new RAMDirectory();
        Graph gs = new GraphStorage(dir).createNew(defaultSize);
        int testIndex = dir.findCreate("edges").segmentSize() * 3;
        gs.edge(0, testIndex, 10, true);
    }

    @Test
    public void testEdgeProperties() {
        Graph someGraphImpl = createGraph();
        EdgeIterator iter1 = someGraphImpl.edge(0, 1, 10, true);
        EdgeIterator iter2 = someGraphImpl.edge(0, 2, 20, true);

        int edgeId = iter1.edge();
        EdgeIterator iter = someGraphImpl.getEdgeProps(edgeId, 0);
        assertEquals(10, iter.distance(), 1e-5);

        edgeId = iter2.edge();
        iter = someGraphImpl.getEdgeProps(edgeId, 0);
        assertEquals(2, iter.baseNode());
        assertEquals(0, iter.node());
        assertEquals(20, iter.distance(), 1e-5);

        iter = someGraphImpl.getEdgeProps(edgeId, 2);
        assertEquals(0, iter.baseNode());
        assertEquals(2, iter.node());
        assertEquals(20, iter.distance(), 1e-5);

        try {
            someGraphImpl.getEdgeProps(edgeId, -1);
            assertTrue(false);
        } catch (Exception ex) {
        }

        iter = someGraphImpl.getEdgeProps(edgeId, 1);
        assertTrue(iter.isEmpty());
    }

    @Test
    public void testCreateDuplicateEdges() {
        Graph graph = createGraph();
        graph.edge(2, 1, 12, true);
        graph.edge(2, 3, 12, true);
        graph.edge(2, 3, 13, false);
        assertEquals(3, GraphUtility.count(graph.getOutgoing(2)));

        // no exception        
        graph.getEdgeProps(1, 3);

        // raise exception
        try {
            graph.getEdgeProps(4, 3);
            assertTrue(false);
        } catch (Exception ex) {
        }
        try {
            graph.getEdgeProps(-1, 3);
            assertTrue(false);
        } catch (Exception ex) {
        }

        EdgeIterator iter = graph.getOutgoing(2);
        iter.next();
        iter.next();
        assertTrue(iter.next());
        EdgeIterator oneIter = graph.getEdgeProps(iter.edge(), 3);
        assertEquals(13, oneIter.distance(), 1e-6);
        assertEquals(2, oneIter.baseNode());
        assertTrue(CarStreetType.isForward(oneIter.flags()));
        assertFalse(CarStreetType.isBoth(oneIter.flags()));

        oneIter = graph.getEdgeProps(iter.edge(), 2);
        assertEquals(13, oneIter.distance(), 1e-6);
        assertEquals(3, oneIter.baseNode());
        assertTrue(CarStreetType.isBackward(oneIter.flags()));
        assertFalse(CarStreetType.isBoth(oneIter.flags()));

        graph.edge(3, 2, 14, true);
        assertEquals(4, GraphUtility.count(graph.getOutgoing(2)));
    }

    @Test
    public void testIdenticalNodes() {
        Graph g = createGraph();
        g.edge(0, 0, 100, true);
        assertEquals(1, GraphUtility.count(g.getEdges(0)));
    }

    @Test
    public void testIdenticalNodes2() {
        Graph g = createGraph();
        g.edge(0, 0, 100, false);
        g.edge(0, 0, 100, false);
        assertEquals(2, GraphUtility.count(g.getEdges(0)));
    }

    @Test
    public void testEdgeReturn() {
        Graph g = createGraph();
        EdgeIterator iter = g.edge(4, 10, 100, CarStreetType.flags(10, false));
        assertEquals(4, iter.baseNode());
        assertEquals(10, iter.node());
        iter = g.edge(14, 10, 100, CarStreetType.flags(10, false));
        assertEquals(14, iter.baseNode());
        assertEquals(10, iter.node());
    }

    @Test
    public void testPillarNodes() {
        Graph g = createGraph();
        PointList pointList = Helper.createPointList(1, 1, 1, 2, 1, 3);
        g.edge(0, 4, 100, CarStreetType.flags(10, false)).wayGeometry(pointList);
        pointList = Helper.createPointList(1, 5, 1, 6, 1, 7, 1, 8, 1, 9);
        g.edge(4, 10, 100, CarStreetType.flags(10, false)).wayGeometry(pointList);
        pointList = Helper.createPointList(1, 13, 1, 12, 1, 11);
        g.edge(14, 0, 100, CarStreetType.flags(10, false)).wayGeometry(pointList);

        // if tower node requested => return only tower nodes
        EdgeIterator iter = g.getEdges(0);
        assertTrue(iter.next());
        assertEquals(4, iter.node());
        assertPList(Helper.createPointList(1, 1, 1, 2, 1, 3), iter.wayGeometry());
        assertTrue(iter.next());
        assertPList(Helper.createPointList(1, 11, 1, 12, 1, 13.0), iter.wayGeometry());
        assertEquals(14, iter.node());
        assertFalse(iter.next());

        iter = g.getOutgoing(0);
        assertTrue(iter.next());
        assertPList(Helper.createPointList(1, 1, 1, 2, 1, 3), iter.wayGeometry());
        assertEquals(4, iter.node());
        assertFalse(iter.next());

        iter = g.getIncoming(10);
        assertTrue(iter.next());
        assertPList(Helper.createPointList(1, 9, 1, 8, 1, 7, 1, 6, 1, 5), iter.wayGeometry());
        assertEquals(4, iter.node());
        assertFalse(iter.next());
    }
}
