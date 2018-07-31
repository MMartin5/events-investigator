/*******************************************************************************
 * Copyright (c) 2016 Ecole Polytechnique de Montreal, Ericsson
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 ******************************************************************************/
package org.eclipse.tracecompass.incubator.coherence.core.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNull;
import org.eclipse.jdt.annotation.Nullable;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelAnalysisEventLayout;
import org.eclipse.tracecompass.analysis.os.linux.core.trace.IKernelTrace;
import org.eclipse.tracecompass.common.core.NonNullUtils;
import org.eclipse.tracecompass.incubator.coherence.core.module.IXmlStateSystemContainer;
import org.eclipse.tracecompass.incubator.coherence.core.newmodel.TmfXmlCpuScenarioModel;
import org.eclipse.tracecompass.incubator.coherence.core.newmodel.TmfXmlEvalScenarioModel;
import org.eclipse.tracecompass.incubator.coherence.core.newmodel.TmfXmlIrqScenarioModel;
import org.eclipse.tracecompass.incubator.coherence.core.newmodel.TmfXmlProcessScenarioModel;
import org.eclipse.tracecompass.incubator.coherence.core.newmodel.TmfXmlScenarioModel;
import org.eclipse.tracecompass.incubator.coherence.core.newmodel.TmfXmlSoftIrqScenarioModel;
import org.eclipse.tracecompass.incubator.coherence.core.pattern.stateprovider.XmlPatternStateProvider;
import org.eclipse.tracecompass.tmf.analysis.xml.core.module.TmfXmlStrings;
import org.eclipse.tracecompass.tmf.core.event.ITmfEvent;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;

/**
 * This Class implements a pattern handler tree in the XML-defined state system.
 * It receives events and dispatches it to Active finite state machines.
 *
 * @author Jean-Christian Kouame
 */
public class TmfXmlPatternEventHandler {
	
	public static String FSM_CPU_ID = "cpu_fsm";
	public static String FSM_PROCESS_ID = "process_fsm";
	public static String FSM_IRQ_ID = "irq_fsm";
	public static String FSM_SOFTIRQ_ID = "softirq_fsm";
	public static String FSM_EVAL_ID = "dummy_fsm";

    /* list of states changes */
    protected final XmlPatternStateProvider fParent;

    protected final List<String> fInitialFsm;
    protected final Map<String, TmfXmlTransitionValidator> fTestMap;
    protected final Map<String, ITmfXmlAction> fActionMap;
    protected final Map<String, TmfXmlFsm> fFsmMap = new LinkedHashMap<>();
    protected final List<TmfXmlFsm> fActiveFsmList = new ArrayList<>();
    protected final Map<String, TmfXmlScenarioModel> fFsmIds;
    
    protected boolean fStartChecking;				/* indicated if we should start checking the coherence */ 

    public boolean startChecking() {
		return fStartChecking;
	}

	public void setStartChecking(boolean value) {
		this.fStartChecking = value;
	}

	/**
     * Constructor
     *
     * @param modelFactory
     *            The factory used to create XML model elements
     * @param node
     *            The XML root of this event handler
     * @param parent
     *            The state system container this event handler belongs to
     */
    public TmfXmlPatternEventHandler(ITmfXmlModelFactory modelFactory, Element node, IXmlStateSystemContainer parent) {
        fParent = (XmlPatternStateProvider) parent;
        String initialFsm = node.getAttribute(TmfXmlStrings.INITIAL);
        fInitialFsm = initialFsm.isEmpty() ? Collections.EMPTY_LIST : Arrays.asList(initialFsm.split(TmfXmlStrings.AND_SEPARATOR));

        Map<String, TmfXmlTransitionValidator> testMap = new HashMap<>();
        NodeList nodesTest = node.getElementsByTagName(TmfXmlStrings.TEST);
        /* load transition input */
        for (int i = 0; i < nodesTest.getLength(); i++) {
            Element element = (Element) nodesTest.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlTransitionValidator test = modelFactory.createTransitionValidator(element, fParent);
            testMap.put(test.getId(), test);
        }
        fTestMap = Collections.unmodifiableMap(testMap);

        @NonNull Builder<String, ITmfXmlAction> builder = ImmutableMap.builder();
        NodeList nodesAction = node.getElementsByTagName(TmfXmlStrings.ACTION);
        /* load actions */
        for (int i = 0; i < nodesAction.getLength(); i++) {
            Element element = (Element) nodesAction.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlAction action = modelFactory.createAction(element, fParent);
            builder.put(action.getId(), action);
        }
        builder.put(TmfXmlStrings.CONSTANT_PREFIX + ITmfXmlAction.CLEAR_STORED_FIELDS_STRING, new ResetStoredFieldsAction(fParent));
        builder.put(TmfXmlStrings.CONSTANT_PREFIX + ITmfXmlAction.SAVE_STORED_FIELDS_STRING, new UpdateStoredFieldsAction(fParent));
        fActionMap = builder.build();
        
        fFsmIds = buildFsmIds(((IKernelTrace) fParent.getTrace()).getKernelEventLayout());

        NodeList nodesFsm = node.getElementsByTagName(TmfXmlStrings.FSM);
        /* load fsm */
        for (int i = 0; i < nodesFsm.getLength(); i++) {
            Element element = (Element) nodesFsm.item(i);
            if (element == null) {
                throw new IllegalArgumentException();
            }
            TmfXmlFsm fsm = modelFactory.createFsm(element, fParent, fFsmIds.get(element.getAttribute(TmfXmlStrings.ID)));
            fFsmMap.put(fsm.getId(), fsm);
        }
        
        fStartChecking = false;
    }
    
    private static Map<String, TmfXmlScenarioModel> buildFsmIds(IKernelAnalysisEventLayout layout) {
        ImmutableMap.Builder<String, TmfXmlScenarioModel> builder = ImmutableMap.builder();
        // TODO better fsm ids management
        builder.put(FSM_CPU_ID, new TmfXmlCpuScenarioModel());
        builder.put(FSM_PROCESS_ID, new TmfXmlProcessScenarioModel(layout));
        builder.put(FSM_IRQ_ID, new TmfXmlIrqScenarioModel());
        builder.put(FSM_SOFTIRQ_ID, new TmfXmlSoftIrqScenarioModel(layout));
        builder.put(FSM_EVAL_ID, new TmfXmlEvalScenarioModel(layout));

       return builder.build();
    }

    /**
     * Start a new scenario for this specific fsm id. If the fsm support only a
     * single instance and this instance already exist, no new scenario is then
     * started. If the scenario is created we handle the current event directly.
     *
     * @param fsmIds
     *            The IDs of the fsm to start
     * @param event
     *            The current event
     * @param force
     *            True to force the creation of the scenario, false otherwise
     */
    public void startScenario(List<String> fsmIds, @Nullable ITmfEvent event, boolean force, boolean isObserver) {
        for (String fsmId : fsmIds) {
            TmfXmlFsm fsm = NonNullUtils.checkNotNull(fFsmMap.get(fsmId));
            if (!fActiveFsmList.contains(fsm)) {
                fActiveFsmList.add(fsm);
            }
            fsm.createScenario(event, this, force, isObserver);
        }
    }

    /**
     * Get all the defined transition tests
     *
     * @return The tests in a map
     */
    public Map<String, TmfXmlTransitionValidator> getTestMap() {
        return fTestMap;
    }

    /**
     * Get all the defined actions
     *
     * @return The actions
     */
    public Map<String, ITmfXmlAction> getActionMap() {
        return fActionMap;
    }

    /**
     * If the pattern handler can handle the event, it send the event to all
     * finite state machines with ongoing scenarios
     *
     * @param event
     *            The trace event to handle
     */
    public void handleEvent(ITmfEvent event, boolean isObserver) {
        /*
         * Order is important so cannot be parallelized
         */
        final @NonNull List<@NonNull TmfXmlFsm> activeFsmList = fActiveFsmList;
        final @NonNull Map<@NonNull String, @NonNull TmfXmlFsm> fsmMap = fFsmMap;
        if (activeFsmList.isEmpty()) {
            List<String> fsmIds = fInitialFsm;
            if (fsmIds.isEmpty()) {
                fsmIds = new ArrayList<>();
                for (TmfXmlFsm fsm : fsmMap.values()) {
                    fsmIds.add(fsm.getId());
                }
            }
            if (!fsmIds.isEmpty()) {
                startScenario(fsmIds, null, true, isObserver);
            }
        } else {
            List<String> fsmToStart = new ArrayList<>();
            for (Map.Entry<String, TmfXmlFsm> entry : fsmMap.entrySet()) {
                if (entry.getValue().isNewScenarioAllowed()) {
                    fsmToStart.add(entry.getKey());
                }
            }
            if (!fsmToStart.isEmpty()) {
                startScenario(fsmToStart, null, false, isObserver);
            }
        }
        for (TmfXmlFsm fsm : activeFsmList) {
            fsm.handleEvent(event, fTestMap, fStartChecking);
        }
    }

    /**
     * Abandon all the ongoing scenarios
     */
    public void dispose() {
        for (TmfXmlFsm fsm : fActiveFsmList) {
            fsm.dispose();
        }
    }

    /**
     * Get the fsm corresponding to the specified id
     *
     * @param fsmId
     *            The id of the fsm
     * @return The fsm found, null if nothing found
     */
    public @Nullable TmfXmlFsm getFsm(String fsmId) {
        return fFsmMap.get(fsmId);
    }

    public Map<String, TmfXmlFsm> getFsmMap() {
        return fFsmMap;
    }
    
    public void computeInferences() {
    	for (TmfXmlFsm fsm : fActiveFsmList) {
    		fsm.setTransitions();
    	}
    }
}
