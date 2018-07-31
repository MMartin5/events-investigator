# events-investigator
This project contains a clean, usable version of my master degree's research project.

	src/		Source code of the Trace Compass modules (Eclipse plugins)
	docs/		FSMs, benchmark results and evaluation results
	scripts/	Scripts to generate usecase traces
	usecases/	Files related to the usecases

# How to use it
## Import project into Eclipse
1. Select File > Import > General > Existing Projects into Workspace
2. Add all the modules from src/

## Define the FSM
Write the FSM definition to an XML file, as used for XML analysis.
- Define an action to initialize a scenario (ie. setting the scenario attribute and adding this attribute to the list of existing scenarios)
- Define a condition to check if a scenario already exists (ie. checking if this scenario already has an attribute and checking if a scenario with this attribute already exists)
- Define an initial state, with every state change triggering the initialization action, and testing the initial condition
- Define an error state, similar to the initial state but without the initialization action and test

## Update the code
- Define your scenario model
	1. Create a new class in the plugin org.eclipse.tracecompass.incubator.coherence.core > src > org.eclipse.tracecompass.incubator.coherence.core.newmodel
	2. Override the method getAttributedForEvent and define how to get the attributes from the event
	3. Add a call to the constructor of this class to the map of scenario models (TmfXmlPatternEventHandler)

## Run
1. Run Trace Compass as an Eclipse application
2. Import your XML file containing the FSM definition
	- Tracing > Traces > Manage XML analyses...
	- Import
	- Select your file
3. Import a trace
	- You can select the Lost Events trace type to display the certainty markers (Right click on the trace > Select Trace Type... > LostEventTrace)
4. Open the coherence view
	- Window > Show View > Other...
	- Coherence View
5. To open the inference view, click on the red star in the toolbar from the coherence view
