/*******************************************************************************
 * Copyright (c) 2014 École Polytechnique de Montréal
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Geneviève Bastien - Initial API and implementation
 *******************************************************************************/

package org.eclipse.tracecompass.incubator.coherence.core.model;

import java.util.List;

import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.incubator.coherence.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.incubator.coherence.core.newmodel.TmfXmlScenarioModel;
import org.w3c.dom.Element;

/**
 * Interface to create XML model elements in different contexts. This allows to
 * reuse the same XML syntax and parsers, but use the elements differently
 * depending on the what is intended to be done with them.
 *
 * @author Geneviève Bastien
 */
public interface ITmfXmlModelFactory {

    /**
     * Create a new XML state attribute
     *
     * @param attribute
     *            XML element of the attribute
     * @param container
     *            The state system container this state attribute belongs to
     * @return The new state attribute
     */
    ITmfXmlStateAttribute createStateAttribute(Element attribute, IXmlStateSystemContainer container);

    /**
     * Create a new state value where the value corresponds to a path of
     * {@link ITmfXmlStateAttribute}
     *
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param attributes
     *            The attributes representing the path to this value
     * @return The new state value
     */
    ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, List<ITmfXmlStateAttribute> attributes);

    /**
     * Create a new state value where the value corresponds to a field in an
     * event
     *
     * @param node
     *            The state value XML element
     * @param container
     *            The state system container this state value belongs to
     * @param eventField
     *            The event field where to get the value
     * @return The new state value
     */
    ITmfXmlStateValue createStateValue(Element node, IXmlStateSystemContainer container, String eventField);

    /**
     * Create a new XML condition
     *
     * @param node
     *            The XML root of this condition
     * @param container
     *            The state system container this condition belongs to
     * @return The new XML condition
     */
    TmfXmlCondition createCondition(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML event handler
     *
     * @param node
     *            The XML event handler element
     * @param container
     *            The state system container this state value belongs to
     * @return The new XML event handler
     */
    TmfXmlEventHandler createEventHandler(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML state change
     *
     * @param node
     *            The XML state change element
     * @param container
     *            The state system container this state change belongs to
     * @return The new XML state change
     */
    TmfXmlStateChange createStateChange(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML location
     *
     * @param node
     *            The XML location element
     * @param container
     *            The state system container this location belongs to
     * @return The new XML location
     */
    TmfXmlLocation createLocation(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML pattern event handler
     *
     * @param node
     *            The XML pattern event handler element
     * @param container
     *            The state system container this pattern event handler belongs to
     * @return The new XML pattern event handler
     */
    TmfXmlPatternEventHandler createPatternEventHandler(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML transition validator
     *
     * @param node
     *            The XML test element
     * @param container
     *            The state system container this test belongs to
     * @return The new {@link TmfXmlTransitionValidator}
     */
    TmfXmlTransitionValidator createTransitionValidator(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML action
     *
     * @param node
     *            The XML action element
     * @param container
     *            The state system container this action belongs to
     * @return The new {@link TmfXmlAction}
     */
    TmfXmlAction createAction(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML FSM
     *
     * @param node
     *            The XML FSM element
     * @param container
     *            The state system container this FSM belongs to
     * @return The new {@link TmfXmlFsm}
     */
    TmfXmlFsm createFsm(Element node, IXmlStateSystemContainer container, TmfXmlScenarioModel scenarioModel);

    /**
     * Create a new XML state
     *
     * @param node
     *            The XML state element
     * @param container
     *            The state system container this state belongs to
     * @param parent
     *            The parent state
     * @return The new {@link TmfXmlState}
     */
    TmfXmlState createState(Element node, IXmlStateSystemContainer container, @Nullable TmfXmlState parent);

    /**
     * Create a new XML state transition
     *
     * @param node
     *            The XML state transition element
     * @param container
     *            The state system container this state transition belongs to
     * @return The new XML {@link TmfXmlStateTransition}
     */
    TmfXmlStateTransition createStateTransition(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new XML timestamp condition
     *
     * @param node
     *            The XML timestamp condition element
     * @param container
     *            The state system container this timestamp condition belongs to
     * @return The new {@link TmfXmlTimestampCondition}
     */
    TmfXmlTimestampCondition createTimestampsCondition(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new pattern segment builder
     *
     * @param node
     *            The XML pattern segment builder
     * @param container
     *            The state system container this pattern segment builder belongs to
     * @return The new {@link TmfXmlPatternSegmentBuilder}
     */
    TmfXmlPatternSegmentBuilder createPatternSegmentBuilder(Element node, IXmlStateSystemContainer container);

    /**
     * Create a new pattern map entry
     *
     * @param node
     *            The XML map entry element
     * @param container
     *            The state system container this state value handler belongs to
     * @return The new {@link TmfXmlMapEntry}
     */
    TmfXmlMapEntry createMapEntry(Element node, IXmlStateSystemContainer container);
}
