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

import com.graphhopper.reader.PrinctonReader;
import com.graphhopper.routing.util.AlgorithmPreparation;
import com.graphhopper.routing.util.CarStreetType;
import com.graphhopper.routing.util.FastestCarCalc;
import com.graphhopper.routing.util.ShortestCarCalc;
import com.graphhopper.routing.util.WeightCalculation;
import com.graphhopper.storage.Graph;
import com.graphhopper.storage.GraphBuilder;
import com.graphhopper.util.DistanceCalc;
import com.graphhopper.util.Helper;
import com.graphhopper.util.StopWatch;
import gnu.trove.list.TIntList;
import java.io.IOException;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author Peter Karich,
 */
public abstract class AbstractRoutingAlgorithmTester {

    // problem is: matrix graph is expensive to create to cache it in a static variable
    private static Graph matrixGraph;

    protected Graph createGraph() {
        return new GraphBuilder().create();
    }

    public AlgorithmPreparation prepareGraph(Graph g) {
        return prepareGraph(g, ShortestCarCalc.DEFAULT);
    }

    public abstract AlgorithmPreparation prepareGraph(Graph g, WeightCalculation calc);

    @Test public void testCalcShortestPath() {
        Graph graph = createTestGraph();
        Path p = prepareGraph(graph).createAlgo().calcPath(0, 7);
        assertEquals(p.toString(), 13, p.weight(), 1e-4);
        assertEquals(p.toString(), 5, p.calcNodes().size());
    }

    // see calc-fastest-graph.svg
    @Test public void testCalcFastestPath() {
        Graph graphShortest = createGraph();
        initFastVsShort(graphShortest);
        Path p1 = prepareGraph(graphShortest, ShortestCarCalc.DEFAULT).createAlgo().calcPath(0, 3);
        assertEquals(p1.toString(), 24000, p1.weight(), 1e-6);
        assertEquals(p1.toString(), 24000, p1.distance(), 1e-6);
        assertEquals(p1.toString(), 8640, p1.time());
        assertEquals(p1.toString(), 5, p1.calcNodes().size());
        assertEquals(Helper.createTList(0, 1, 5, 2, 3), p1.calcNodes());

        Graph graphFastest = createGraph();
        initFastVsShort(graphFastest);
        Path p2 = prepareGraph(graphFastest, FastestCarCalc.DEFAULT).createAlgo().calcPath(0, 3);
        assertEquals(p2.toString(), 3100, p2.weight(), 1e-6);
        assertEquals(p2.toString(), 31000, p2.distance(), 1e-6);
        assertEquals(p2.toString(), 5580, p2.time());
        assertEquals(p2.toString(), 6, p2.calcNodes().size());
    }

    void initFastVsShort(Graph graph) {
        graph.edge(0, 1, 7000, CarStreetType.flags(10, false));
        graph.edge(0, 4, 5000, CarStreetType.flags(20, false));

        graph.edge(1, 4, 7000, CarStreetType.flags(10, true));
        graph.edge(1, 5, 7000, CarStreetType.flags(10, true));
        graph.edge(1, 2, 20000, CarStreetType.flags(10, true));

        graph.edge(5, 2, 5000, CarStreetType.flags(10, false));
        graph.edge(2, 3, 5000, CarStreetType.flags(10, false));

        graph.edge(5, 3, 11000, CarStreetType.flags(20, false));
        graph.edge(3, 7, 7000, CarStreetType.flags(10, false));

        graph.edge(4, 6, 5000, CarStreetType.flags(20, false));
        graph.edge(5, 4, 7000, CarStreetType.flags(10, false));

        graph.edge(5, 6, 7000, CarStreetType.flags(10, false));
        graph.edge(7, 5, 5000, CarStreetType.flags(20, false));

        graph.edge(6, 7, 5000, CarStreetType.flags(20, true));
    }

    // see test-graph.svg !
    protected Graph createTestGraph() {
        Graph graph = createGraph();

        graph.edge(0, 1, 7, true);
        graph.edge(0, 4, 6, true);

        graph.edge(1, 4, 1, true);
        graph.edge(1, 5, 8, true);
        graph.edge(1, 2, 2, true);

        graph.edge(2, 5, 5, true);
        graph.edge(2, 3, 2, true);

        graph.edge(3, 5, 2, true);
        graph.edge(3, 7, 10, true);

        graph.edge(4, 6, 4, true);
        graph.edge(4, 5, 7, true);

        graph.edge(5, 6, 2, true);
        graph.edge(5, 7, 1, true);

        graph.edge(6, 7, 5, true);
        return graph;
    }

    @Test public void testNoPathFound() {
        Graph graph = createGraph();
        assertFalse(prepareGraph(graph).createAlgo().calcPath(0, 1).found());
    }

    @Test public void testWikipediaShortestPath() {
        Graph graph = createWikipediaTestGraph();
        Path p = prepareGraph(graph).createAlgo().calcPath(0, 4);
        assertEquals(p.toString(), 20, p.weight(), 1e-4);
        assertEquals(p.toString(), 4, p.calcNodes().size());
    }

    @Test public void testCalcIfNoWay() {
        Graph graph = createTestGraph();
        Path p = prepareGraph(graph).createAlgo().calcPath(0, 0);
        assertEquals(p.toString(), 0, p.weight(), 1e-4);
        assertEquals(p.toString(), 0, p.calcNodes().size());
    }

    @Test public void testCalcIf1EdgeAway() {
        Graph graph = createTestGraph();
        Path p = prepareGraph(graph).createAlgo().calcPath(1, 2);
        assertEquals(Helper.createTList(1, 2), p.calcNodes());
        assertEquals(p.toString(), 2, p.weight(), 1e-4);
    }

    // see wikipedia-graph.svg !
    protected Graph createWikipediaTestGraph() {
        Graph graph = createGraph();
        graph.edge(0, 1, 7, true);
        graph.edge(0, 2, 9, true);
        graph.edge(0, 5, 14, true);
        graph.edge(1, 2, 10, true);
        graph.edge(1, 3, 15, true);
        graph.edge(2, 5, 2, true);
        graph.edge(2, 3, 11, true);
        graph.edge(3, 4, 6, true);
        graph.edge(4, 5, 9, true);
        return graph;
    }

    // 0-1-2-3-4
    // |     / |
    // |    8  |
    // \   /   /
    //  7-6-5-/
    public static void initBiGraph(Graph graph) {
        graph.edge(0, 1, 100, true);
        graph.edge(1, 2, 1, true);
        graph.edge(2, 3, 1, true);
        graph.edge(3, 4, 1, true);
        graph.edge(4, 5, 25, true);
        graph.edge(5, 6, 25, true);
        graph.edge(6, 7, 5, true);
        graph.edge(7, 0, 5, true);
        graph.edge(3, 8, 20, true);
        graph.edge(8, 6, 20, true);
    }

    @Test public void testBidirectional() {
        Graph graph = createGraph();
        initBiGraph(graph);

        // PrepareTowerNodesShortcutsTest.printEdges((LevelGraph) graph);
        Path p = prepareGraph(graph).createAlgo().calcPath(0, 4);
        // PrepareTowerNodesShortcutsTest.printEdges((LevelGraph) graph);
        assertEquals(p.toString(), Helper.createTList(0, 7, 6, 8, 3, 4), p.calcNodes());
        assertEquals(p.toString(), 51, p.weight(), 1e-4);

        p = prepareGraph(graph).createAlgo().calcPath(1, 2);
        assertEquals(p.toString(), 1, p.weight(), 1e-4);
        assertEquals(p.toString(), Helper.createTList(1, 2), p.calcNodes());
    }

    // 1-2-3-4-5
    // |     / |
    // |    9  |
    // \   /   /
    //  8-7-6-/
    @Test public void testBidirectional2() {
        Graph graph = createGraph();

        graph.edge(0, 1, 100, true);
        graph.edge(1, 2, 1, true);
        graph.edge(2, 3, 1, true);
        graph.edge(3, 4, 1, true);
        graph.edge(4, 5, 20, true);
        graph.edge(5, 6, 10, true);
        graph.edge(6, 7, 5, true);
        graph.edge(7, 0, 5, true);
        graph.edge(3, 8, 20, true);
        graph.edge(8, 6, 20, true);

        Path p = prepareGraph(graph).createAlgo().calcPath(0, 4);
        assertEquals(p.toString(), 40, p.weight(), 1e-4);
        assertEquals(p.toString(), 5, p.calcNodes().size());
        assertEquals(Helper.createTList(0, 7, 6, 5, 4), p.calcNodes());
    }

    @Test
    public void testRekeyBugOfIntBinHeap() {
        // using DijkstraSimple + IntBinHeap then rekey loops endlessly
        Path p = prepareGraph(getMatrixGraph()).createAlgo().calcPath(36, 91);
        assertEquals(12, p.calcNodes().size());

        TIntList list = p.calcNodes();
        if (!Helper.createTList(36, 46, 56, 66, 76, 86, 85, 84, 94, 93, 92, 91).equals(list)
                && !Helper.createTList(36, 46, 56, 66, 76, 86, 85, 84, 83, 82, 92, 91).equals(list))
            assertTrue("wrong locations: " + list.toString(), false);
        assertEquals(66f, p.weight(), 1e-3);
    }

    @Test
    public void testBug1() {
        Path p = prepareGraph(getMatrixGraph()).createAlgo().calcPath(34, 36);
        assertEquals(Helper.createTList(34, 35, 36), p.calcNodes());
        assertEquals(3, p.calcNodes().size());
        assertEquals(17, p.weight(), 1e-5);
    }

    @Test
    public void testCorrectWeight() {
        Path p = prepareGraph(getMatrixGraph()).createAlgo().calcPath(45, 72);
        assertEquals(38f, p.weight(), 1e-3);
        assertEquals(Helper.createTList(45, 44, 54, 64, 74, 73, 72), p.calcNodes());
    }

    @Test
    public void testCannotCalculateSP() {
        Graph graph = createGraph();
        graph.edge(0, 1, 1, false);
        graph.edge(1, 2, 1, false);

        Path p = prepareGraph(graph).createAlgo().calcPath(0, 2);
        assertEquals(p.toString(), 3, p.calcNodes().size());
    }

    @Test
    public void testDirectedGraphBug1() {
        Graph graph = createGraph();
        graph.edge(0, 1, 3, false);
        graph.edge(1, 2, 2.99, false);

        graph.edge(0, 3, 2, false);
        graph.edge(3, 4, 3, false);
        graph.edge(4, 2, 1, false);

        Path p = prepareGraph(graph).createAlgo().calcPath(0, 2);
        assertEquals(Helper.createTList(0, 1, 2), p.calcNodes());
        assertEquals(p.toString(), 5.99, p.weight(), 1e-4);
        assertEquals(p.toString(), 3, p.calcNodes().size());
    }

    @Test
    public void testDirectedGraphBug2() {
        Graph graph = createGraph();
        graph.edge(0, 1, 1, false);
        graph.edge(1, 2, 1, false);
        graph.edge(2, 3, 1, false);

        graph.edge(3, 1, 4, true);

        Path p = prepareGraph(graph).createAlgo().calcPath(0, 3);
        assertEquals(Helper.createTList(0, 1, 2, 3), p.calcNodes());
    }

    // a-b-0-c-1
    // |   |  _/\
    // |  /  /  |
    // d-2--3-e-4
    @Test
    public void testWithCoordinates() {
        Graph graph = createGraph();
        graph.setNode(0, 0, 2);
        graph.setNode(1, 0, 3.5);
        graph.setNode(2, 1, 1);
        graph.setNode(3, 1.5, 2.5);
        graph.setNode(4, 0.5, 4.5);

        graph.edge(0, 1, 2, true).wayGeometry(Helper.createPointList(0, 3));
        graph.edge(2, 3, 2, true);
        graph.edge(3, 4, 2, true).wayGeometry(Helper.createPointList(1, 3.5));

        graph.edge(0, 2, 0.8, true).wayGeometry(Helper.createPointList(0, 1.6, 0, 0, 1, 0));
        graph.edge(0, 2, 1.2, true);
        graph.edge(1, 3, 1.3, true);
        graph.edge(1, 4, 1, true);

        AlgorithmPreparation prepare = prepareGraph(graph);
        Path p = prepare.createAlgo().calcPath(4, 0);
        assertEquals(Helper.createTList(4, 1, 0), p.calcNodes());
        assertEquals(Helper.createPointList(0.5, 4.5, 0, 3.5, 0, 3, 0, 2), p.calcPoints());
        assertEquals(291110, p.calcPoints().calculateDistance(new DistanceCalc()), 1);

        // PrepareTowerNodesShortcutsTest.printEdges((LevelGraph) graph);
        p = prepare.createAlgo().calcPath(2, 1);
        // System.out.println(p.toDetailsString());
        assertEquals(Helper.createTList(2, 0, 1), p.calcNodes());
        assertEquals(Helper.createPointList(1, 1, 1, 0, 0, 0, 0, 1.6, 0, 2, 0, 3, 0, 3.5), p.calcPoints());
        assertEquals(611555, p.calcPoints().calculateDistance(new DistanceCalc()), 1);
    }

    @Test public void testPerformance() throws IOException {
        int N = 10;
        int noJvmWarming = N / 4;

        String name = getClass().getSimpleName();
        Random rand = new Random(0);
        Graph graph = createGraph();

        String bigFile = "10000EWD.txt.gz";
        new PrinctonReader(graph).stream(new GZIPInputStream(PrinctonReader.class.getResourceAsStream(bigFile), 8 * (1 << 10))).read();
        AlgorithmPreparation prepare = prepareGraph(graph);
        StopWatch sw = new StopWatch();
        for (int i = 0; i < N; i++) {
            int index1 = Math.abs(rand.nextInt(graph.nodes()));
            int index2 = Math.abs(rand.nextInt(graph.nodes()));
            RoutingAlgorithm d = prepare.createAlgo();
            if (i >= noJvmWarming)
                sw.start();
            Path p = d.calcPath(index1, index2);
            // avoid jvm optimization => call p.distance
            if (i >= noJvmWarming && p.distance() > -1)
                sw.stop();

            // System.out.println("#" + i + " " + name + ":" + sw.getSeconds() + " " + p.nodes());
        }

        float perRun = sw.stop().getSeconds() / ((float) (N - noJvmWarming));
        System.out.println("# " + name + ":" + sw.stop().getSeconds() + ", per run:" + perRun);
        assertTrue("speed to low!? " + perRun + " per run", perRun < 0.07);
    }

    public Graph getMatrixGraph() {
        return getMatrixAlikeGraph();
    }

    public static Graph getMatrixAlikeGraph() {
        if (matrixGraph == null)
            matrixGraph = createMatrixAlikeGraph();
        return matrixGraph;
    }

    private static Graph createMatrixAlikeGraph() {
        int WIDTH = 10;
        int HEIGHT = 15;
        Graph tmp = new GraphBuilder().create();
        int[][] matrix = new int[WIDTH][HEIGHT];
        int counter = 0;
        Random rand = new Random(12);
        boolean print = false;
        for (int h = 0; h < HEIGHT; h++) {
            if (print) {
                for (int w = 0; w < WIDTH; w++) {
                    System.out.print(" |\t           ");
                }
                System.out.println();
            }

            for (int w = 0; w < WIDTH; w++) {
                matrix[w][h] = counter++;

                if (h > 0) {
                    float dist = 5 + Math.abs(rand.nextInt(5));
                    if (print)
                        System.out.print(" " + (int) dist + "\t           ");
                    tmp.edge(matrix[w][h], matrix[w][h - 1], dist, true);
                }
            }
            if (print) {
                System.out.println();
                if (h > 0) {
                    for (int w = 0; w < WIDTH; w++) {
                        System.out.print(" |\t           ");
                    }
                    System.out.println();
                }
            }

            for (int w = 0; w < WIDTH; w++) {
                if (w > 0) {
                    float dist = 5 + Math.abs(rand.nextInt(5));
                    if (print)
                        System.out.print("-- " + (int) dist + "\t-- ");
                    tmp.edge(matrix[w][h], matrix[w - 1][h], dist, true);
                }
                if (print)
                    System.out.print("(" + matrix[w][h] + ")\t");
            }
            if (print)
                System.out.println();
        }

        return tmp;
    }
}
