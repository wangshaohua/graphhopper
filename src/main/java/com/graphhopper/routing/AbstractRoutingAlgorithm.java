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

import com.graphhopper.routing.util.ShortestCarCalc;
import com.graphhopper.routing.util.WeightCalculation;
import com.graphhopper.storage.EdgeEntry;
import com.graphhopper.storage.Graph;

/**
 * @author Peter Karich
 */
public abstract class AbstractRoutingAlgorithm implements RoutingAlgorithm {

    protected Graph graph;
    protected WeightCalculation weightCalc = ShortestCarCalc.DEFAULT;

    public AbstractRoutingAlgorithm(Graph graph) {
        this.graph = graph;
    }

    @Override public RoutingAlgorithm type(WeightCalculation wc) {
        this.weightCalc = wc;
        return this;
    }

    protected void updateShortest(EdgeEntry shortestDE, int currLoc) {
    }

    @Override public RoutingAlgorithm clear() {
        return this;
    }

    @Override public String toString() {
        return name() + "|" + weightCalc;
    }

    @Override public String name() {
        return getClass().getSimpleName();
    }
}
