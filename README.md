jbrex
=====

A business rule extraction tool for Java applications.
This prototype identifies the business rules in a Java project and extracts them to text and graph artifacts

How to use the tool
===================
-Download Eclipse Modeling Project (http://eclipse.org/modeling/)

-Copy in the workspace the four projects (org.eclipse.m2m.atl.engine.emfvm, JBrex, brex.path.model, fr.inria.portolan.visualization.prefuse)

-Right click on the JBrex folder and run it as Eclipse Application (it will open a new workspace, ignore the errors)

-Import or select a Java project in the new workspace

-Choose in the code of the selected project the variables to analyse (on top of the variable(s) you want to analyse add the comment //SELECT-VARIABLE)

-Click on the button "start JBREX" in the tool bar

-Select the project to analyse

Output
===================
in the selected project you will find:

-the model of the project and its version containing the business rule annotations 
	
-a new project called "the-name-of-your-project"_annotated will be created in the workspace. It will contain:

-the Java files of your project annotated with business rule information, 

-a text file called rules.txt that contains the rules identified

-two graph files that show the connections between the rules (ruleConnectionGraph) and the connections between the parts composing the rules (rulePartConnectionGraph)

-a file (.*_br.xmi) containing the model representing the identified rules

-several files .*(_graphml|_trace|_portland).xmi containing the models used to generated the graph artifacts

How to interpret the annotations
================================
The annotations are:

'SV' 		stands for 	'SLICED-VARIABLE'	 The variable that has been analysed

'RelV'		stands for 	'RELATED-VARIABLE'   The variables related to SV

'R'			stands for	'Rule				 The statements that modify or can be used to reach the variable SV

'G' 		stands for 	'Granularity' 		 The distance between the statements "X" that modify SV and the statements that allow to reach "X"
'RelStat' 	stands for  'Related Statement'  The statements related to R

'RelM' 		stands for 	'Related Method'	 The methods that contain R

'ReaM' 		stands for 	'Reachable Method'   The methods that are called in RelM

'D' 		stands for 	'Distance'			 The distance between a given RelM and its corresponding ReaMs 

About the graphs
================

The graphs can be visualized by right-clicking on the files with extension .graphml and selecting "View GraphML file"

The rules that have connections with other ones are clustered together

The animation can be activated/disactivated by pressing the key "t"

The scroll wheel of the mouse can be used to zoom in/out

The graph can be navigated by clicking on an empty space and dragging

The nodes can be moved by clicking on them and dragging.

The path between two nodes in the graph can be highlighted by clicking on them. The source node is colored red, the target node green, and the possible paths yellow


ruleConnectionGraph.graphml:

	the nodes are the rules, the text inside is the code composing the rule
	
	the edges are the connections between rules that share each other some part of their code
	
	
rulePartConnectionGraph.graphml:

	the nodes are the rule parts of a rule, the text inside is the code composing the rule part
	
	rule parts that belong to the same rule are colored with the same color
	
	each rule part is connected by an edge to the next rule part
	
	rule parts used by different rules are connected together
