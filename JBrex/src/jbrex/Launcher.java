package jbrex;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.XMIResource;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.gmt.modisco.java.actions.DefaultDiscoverer;
import org.eclipse.gmt.modisco.java.actions.DiscoverJavaModelFromJavaProject;
import org.eclipse.gmt.modisco.java.generation.files.GenerateJavaExtended;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IInjector;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.IReferenceModel;
import org.eclipse.m2m.atl.core.ModelFactory;
import org.eclipse.m2m.atl.core.emf.EMFInjector;
import org.eclipse.m2m.atl.core.emf.EMFModelFactory;
import org.eclipse.m2m.atl.core.launch.ILauncher;
import org.eclipse.m2m.atl.core.service.LauncherService;
import org.eclipse.m2m.atl.engine.emfvm.launch.EMFVMLauncher;

import path.Path;
import path.PathFactory;
import path.PathPackage;

@SuppressWarnings("deprecation")
public class Launcher {
	
	private static final String bundleName = "JBrex";
	private static final String inputFolder = "input/";
	private static final String resourcesFolder = "resources/";
	private static final String metamodelsFolder = "metamodels/";
	
	private IProject project;
	private IProject annotatedProject;
	
	private String projectPath;
	private String projectName;
	private String inputModelPath;
	private String annotatedJavaProject;
	private String annotatedJavaProjectPath;
	
	private String annotatedModelPath;
	private String businessRuleModelPath;
	
	private String portolanRuleConnectionModelPath;
	private String graphMLRuleConnectionModelPath;
	private String traceRuleConnectionModelPath;
	
	private String portolanRulePartConnectionModelPath;
	private String graphMLRulePartConnectionModelPath;
	private String traceRulePartConnectionModelPath;
	
	private String rulePartGraphPath;
	private String ruleGraphPath;
	
	private static final String INPUT_RULES_TXT_MODEL_PATH = getLocation() + inputFolder + "rules_txt_path.xmi";
	
	private static final String JAVA_METAMODEL_PATH = getLocation() + metamodelsFolder + "java.ecore";
	private static final String BR_METAMODEL_PATH = getLocation() + metamodelsFolder + "brmodel.ecore";
	private static final String PATH_METAMODEL_PATH = getLocation() + metamodelsFolder + "path.ecore";
	private static final String PORTLAND_METAMODEL_PATH = getLocation() + metamodelsFolder + "PortolanCore.ecore";
	private static final String GRAPHML_METAMODEL_PATH = getLocation() + metamodelsFolder + "GraphML.ecore";
	private static final String TRACE_METAMODEL_PATH = getLocation() + metamodelsFolder + "Trace.ecore";
	private static final String XML_METAMODEL_PATH = getLocation() + metamodelsFolder + "xml.ecore";
	
	private static final String ANNOTATING_TRANSF = '/' + resourcesFolder + "annotatingcode.asm";
	private static final String ANNOTATEDCODE2RULES_TRANSF = '/' + resourcesFolder + "annotatedcode2rules.asm";
	private static final String RULES2TEXT_TRANSF = '/' + resourcesFolder + "rules2text.asm";
	private static final String RULES2RULEPARTS_PORTOLAN_TRANSF = '/' + resourcesFolder + "rules2rulePartsPortolan.asm";
	private static final String RULES2RULES_PORTOLAN_TRANSF = '/' + resourcesFolder + "rules2rulesPortolan.asm";
	private static final String PORTOLAN2GRAPHML_TRANSF = '/' + resourcesFolder + "Cartography2GraphMLSingle.asm";
	private static final String GRAPHML2XML_TRANSF = getLocation().replaceFirst("file:/", "") + resourcesFolder + "GraphML2XML.asm";
	
	private ILauncher launcher;
	private IProgressMonitor ip;
	
	
	public static final String getLocation() {	
		return Platform.getBundle(bundleName).getLocation().replaceFirst("reference:", "");
	}
	
	public Launcher(IProject project) {
		this.project = project;
		
		String path = project.getLocation().toString();
		this.projectPath = path;
		this.projectName = this.getProjectName(projectPath); 
		this.inputModelPath = projectPath + "/" + projectName + ".javaxmi";
		this.annotatedModelPath = projectPath + "/" + projectName + "_annotated.xmi";
		
		this.annotatedJavaProject = this.projectName + "_annotated";
		this.annotatedJavaProjectPath = this.projectPath + "_annotated";
		
		this.businessRuleModelPath = annotatedJavaProjectPath + "/" + projectName + "_br.xmi";
		
		this.portolanRuleConnectionModelPath = annotatedJavaProjectPath + "/" + projectName + "_rule_connection_portland.xmi";
		this.graphMLRuleConnectionModelPath = annotatedJavaProjectPath + "/" + projectName + "_rule_connection_graphml.xmi";
		this.traceRuleConnectionModelPath = annotatedJavaProjectPath + "/" + projectName + "_rule_connection_graphml.trace.xmi";
		
		this.portolanRulePartConnectionModelPath = annotatedJavaProjectPath + "/" + projectName + "_rule_part_connection_portland.xmi";
		this.graphMLRulePartConnectionModelPath = annotatedJavaProjectPath + "/" + projectName + "_rule_part_connection_graphml.xmi";
		this.traceRulePartConnectionModelPath = annotatedJavaProjectPath + "/" + projectName + "_rule_part_connection_graphml.trace.xmi";
		
		this.ruleGraphPath = "/" + "ruleConnectionGraph.graphml";
		this.rulePartGraphPath = "/" + "rulePartConnectionGraph.graphml";	
		
		this.launcher = new EMFVMLauncher();
		this.ip = new NullProgressMonitor();
	}
	
	private String getProjectName(String path) {
		List<String> list = Arrays.asList(path.split("/"));
		return list.get(list.size() - 1);
	}
	
	public void run() throws IOException, CoreException, ATLCoreException {
		//create a model representation of the selected project
		generateModelCode();
		//annotated the model with business rule information
		annotatingCode();
		refreshProject(this.project);
		//generate in the workspace the selected project with business rule information
		generateAnnotatedCode();
		//initialize the the path where to serialize the text version of the business rules identified
		initializeModelPath(INPUT_RULES_TXT_MODEL_PATH, this.annotatedProject);
		//transform the annotated code to rules
		annotatedCode2Rules();
		//serialize the rules to a text file within the annotated project
		rules2text();
		//serialize the rules to a graphml file to analyse their relations
		rules2graph();
		refreshProject(annotatedProject);
	}
	
	public void refreshProject(IProject p) throws CoreException {
		p.refreshLocal(IResource.DEPTH_INFINITE, null);
	}
	
	public void annotatingCode() throws IOException, ATLCoreException {
		
		this.launcher = new EMFVMLauncher();
		this.ip = new NullProgressMonitor();
		
		Map<String,String> inoutModels = new HashMap<String,String>();
		inoutModels.put("IN", "JAVA");
		
		Map<String,String> paths = new HashMap<String,String>();
		paths.put("IN", URI.createFileURI(inputModelPath).toString());
		paths.put("OUT", URI.createFileURI(annotatedModelPath).toString());
		paths.put("REFINED#IN", URI.createFileURI(annotatedModelPath).toString());
		
		paths.put("JAVA", JAVA_METAMODEL_PATH);
		
		Map<String, Object> options = new HashMap<String, Object>();

		options.put("isRefiningTraceMode", new Boolean(true));
		
		URL transformation  = Launcher.class.getResource(ANNOTATING_TRANSF);
		
		LauncherService.launch(
				ILauncher.RUN_MODE, 
				ip, 
				launcher, 
				Collections.<String, String> emptyMap(), 
				inoutModels, 
				Collections.<String, String> emptyMap(),  
				paths, 
				options, 
				Collections.<String, InputStream> emptyMap(), 
				transformation.openStream()
		);

	}
	
	public void annotatedCode2Rules() throws IOException, ATLCoreException {
		
		this.launcher = new EMFVMLauncher();
		this.ip = new NullProgressMonitor();
		
		Map<String,String> inModels = new HashMap<String,String>();
		inModels.put("IN", "JAVA");
		Map<String,String> outModels = new HashMap<String,String>();
		outModels.put("OUT", "BR");
		
		Map<String,String> paths = new HashMap<String,String>();
		paths.put("IN", URI.createFileURI(annotatedModelPath).toString());
		paths.put("OUT", URI.createFileURI(businessRuleModelPath).toString());
		
		paths.put("JAVA", JAVA_METAMODEL_PATH);
		paths.put("BR", BR_METAMODEL_PATH);
		
		Map<String, Object> options = new HashMap<String, Object>();

		options.put("allowInterModelReferences", new Boolean(true));
		
		URL transformation  = Launcher.class.getResource(ANNOTATEDCODE2RULES_TRANSF);
		
		LauncherService.launch(
				ILauncher.RUN_MODE, 
				ip, 
				launcher, 
				inModels,
				Collections.<String, String> emptyMap(), 
				outModels,
				paths, 
				options, 
				Collections.<String, InputStream> emptyMap(), 
				transformation.openStream()
		);

	}
	
	public void rules2text() throws IOException, ATLCoreException {
		this.launcher = new EMFVMLauncher();
		this.ip = new NullProgressMonitor();
		
		Map<String,String> inoutModels = new HashMap<String,String>();
		inoutModels.put("IN", "BR");
		inoutModels.put("IN1", "PATH");
		
		Map<String,String> paths = new HashMap<String,String>();
		paths.put("IN", URI.createFileURI(businessRuleModelPath).toString());
		paths.put("IN1", INPUT_RULES_TXT_MODEL_PATH);
		paths.put("OUT", URI.createFileURI(businessRuleModelPath).toString());
		paths.put("REFINED#IN", URI.createFileURI(businessRuleModelPath).toString());
		
		paths.put("BR", BR_METAMODEL_PATH);
		paths.put("PATH", PATH_METAMODEL_PATH);
		
		Map<String, Object> options = new HashMap<String, Object>();

		options.put("isRefiningTraceMode", new Boolean(true));
		
		URL transformation  = Launcher.class.getResource(RULES2TEXT_TRANSF);
		
		LauncherService.launch(
				ILauncher.RUN_MODE, 
				ip, 
				launcher, 
				Collections.<String, String> emptyMap(), 
				inoutModels, 
				Collections.<String, String> emptyMap(),  
				paths, 
				options, 
				Collections.<String, InputStream> emptyMap(), 
				transformation.openStream()
		);
			
	}
	
	public void rules2graph() throws ATLCoreException, IOException, CoreException {
		//generate graph at rule-level
		this.rules2rulePortolan(this.portolanRuleConnectionModelPath);
		this.portoland2graphml(this.portolanRuleConnectionModelPath, this.graphMLRuleConnectionModelPath, this.traceRuleConnectionModelPath);
		this.graphml2xml(this.graphMLRuleConnectionModelPath, this.ruleGraphPath);
		
		//generate graph at rule part-level
		this.rules2rulePartPortolan(this.portolanRulePartConnectionModelPath);
		this.portoland2graphml(this.portolanRulePartConnectionModelPath, this.graphMLRulePartConnectionModelPath, this.traceRulePartConnectionModelPath);
		this.graphml2xml(this.graphMLRulePartConnectionModelPath, this.rulePartGraphPath);
	}
	
	public void rules2rulePortolan(String targetPortolanModelPath) throws ATLCoreException, IOException {
		this.launcher = new EMFVMLauncher();
		this.ip = new NullProgressMonitor();
		
		Map<String,String> inModels = new HashMap<String,String>();
		inModels.put("IN", "BR");
		Map<String,String> outModels = new HashMap<String,String>();
		outModels.put("OUT", "PORT");
		
		Map<String,String> paths = new HashMap<String,String>();
		paths.put("IN", URI.createFileURI(businessRuleModelPath).toString());
		paths.put("OUT", URI.createFileURI(targetPortolanModelPath).toString());
		
		paths.put("BR", BR_METAMODEL_PATH);
		paths.put("PORT", PORTLAND_METAMODEL_PATH);
		
		Map<String, Object> options = new HashMap<String, Object>();
		
		URL transformation  = Launcher.class.getResource(RULES2RULES_PORTOLAN_TRANSF);
		
		LauncherService.launch(
				ILauncher.RUN_MODE, 
				ip, 
				launcher, 
				inModels,
				Collections.<String, String> emptyMap(), 
				outModels,
				paths, 
				options, 
				Collections.<String, InputStream> emptyMap(), 
				transformation.openStream()
		);
	}
	
	public void rules2rulePartPortolan(String targetPortolanModelPath) throws ATLCoreException, IOException {
		this.launcher = new EMFVMLauncher();
		this.ip = new NullProgressMonitor();
		
		Map<String,String> inModels = new HashMap<String,String>();
		inModels.put("IN", "BR");
		Map<String,String> outModels = new HashMap<String,String>();
		outModels.put("OUT", "PORT");
		
		Map<String,String> paths = new HashMap<String,String>();
		paths.put("IN", URI.createFileURI(businessRuleModelPath).toString());
		paths.put("OUT", URI.createFileURI(targetPortolanModelPath).toString());
		
		paths.put("BR", BR_METAMODEL_PATH);
		paths.put("PORT", PORTLAND_METAMODEL_PATH);
		
		Map<String, Object> options = new HashMap<String, Object>();
		
		URL transformation  = Launcher.class.getResource(RULES2RULEPARTS_PORTOLAN_TRANSF);
		
		LauncherService.launch(
				ILauncher.RUN_MODE, 
				ip, 
				launcher, 
				inModels,
				Collections.<String, String> emptyMap(), 
				outModels,
				paths, 
				options, 
				Collections.<String, InputStream> emptyMap(), 
				transformation.openStream()
		);

	}
	
	public void portoland2graphml(String portolanModelPath, String graphMLModelPath, String traceModelPath) throws ATLCoreException, IOException {
		this.launcher = new EMFVMLauncher();
		this.ip = new NullProgressMonitor();
		
		Map<String,String> inModels = new HashMap<String,String>();
		inModels.put("IN", "Cartography");
		Map<String,String> outModels = new HashMap<String,String>();
		outModels.put("OUT", "GraphML");
		outModels.put("OUT1", "TRC");
		
		Map<String,String> paths = new HashMap<String,String>();
		paths.put("IN", URI.createFileURI(portolanModelPath).toString());
		paths.put("OUT", URI.createFileURI(graphMLModelPath).toString());
		paths.put("OUT1", URI.createFileURI(traceModelPath).toString());
		
		paths.put("Cartography", PORTLAND_METAMODEL_PATH);
		paths.put("GraphML", GRAPHML_METAMODEL_PATH);
		paths.put("TRC", TRACE_METAMODEL_PATH);
		
		Map<String, Object> options = new HashMap<String, Object>();
		options.put("isRefiningTraceMode", new Boolean(true));
		options.put("allowInterModelReferences", new Boolean(true));
		
		URL transformation  = Launcher.class.getResource(PORTOLAN2GRAPHML_TRANSF);
		
		LauncherService.launch(
				ILauncher.RUN_MODE, 
				ip, 
				launcher, 
				inModels,
				Collections.<String, String> emptyMap(), 
				outModels,
				paths, 
				options, 
				Collections.<String, InputStream> emptyMap(), 
				transformation.openStream()
		);
			
	}
	
	public void graphml2xml(String graphMLModelPath, String targetFilePath) throws ATLCoreException, FileNotFoundException, CoreException  {
		this.launcher = new EMFVMLauncher();
		
		ModelFactory modelFactory = new EMFModelFactory();
		IInjector injector = new EMFInjector();
		IReferenceModel graphMLMetamodel;
		IReferenceModel xmlMetamodel;
		
		//LOAD METAMODELS
		graphMLMetamodel = modelFactory.newReferenceModel();
		injector.inject(graphMLMetamodel, GRAPHML_METAMODEL_PATH);
		
		xmlMetamodel = modelFactory.newReferenceModel();
		injector.inject(xmlMetamodel, XML_METAMODEL_PATH);
		
		IModel graphMLModel = modelFactory.newModel(graphMLMetamodel);
		injector.inject(graphMLModel, URI.createFileURI(graphMLModelPath).toString());
		
		IModel xmlModel = modelFactory.newModel(xmlMetamodel);
		
		HashMap<String, Object> params = new HashMap<String, Object>();
		
		this.launcher.initialize(new HashMap<String, Object>());
		
		this.launcher.addInModel(graphMLModel, "IN", "GraphML");
		this.launcher.addOutModel(xmlModel, "OUT", "XML");
		
		this.launcher.launch(
										ILauncher.RUN_MODE, 
										new NullProgressMonitor(), 
										params, 
										new FileInputStream(GRAPHML2XML_TRANSF)
									);
		
		xmlModel2xmlText(xmlModel, targetFilePath);
		
	}
	
	private void xmlModel2xmlText(IModel xmlModel, String targetFilePath) throws CoreException, ATLCoreException {
		//transform xml model to text
	    IFile graphOutputFile = this.annotatedProject.getFile(targetFilePath);	
	
		if (!graphOutputFile.exists())
			graphOutputFile.create(null, false, null);
		
		XMLExtractor xmlExtractor = new XMLExtractor();
		xmlExtractor.extract(xmlModel, graphOutputFile.getLocation().toString(), Collections.<String, Object> emptyMap());
	}
	
	public void initializeModelPath(String modelPath, IProject project) throws IOException {
		
		ResourceSet resSet = new ResourceSetImpl();
		resSet.getPackageRegistry().put(PathPackage.eNS_URI, PathPackage.eINSTANCE);
		resSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new EcoreResourceFactoryImpl());
		Resource res = resSet.createResource(URI.createURI(modelPath));
		
		//initialize path value
		PathFactory factory = PathFactory.eINSTANCE;
		Path path = factory.createPath();
		
		//save the rules in the annotated project just created
		this.projectPath = project.getLocation().toString() + '/';
		path.setValue(this.projectPath + "rules.txt");
		
		res.getContents().add(path);
		
		res.save(Collections.EMPTY_MAP);
		
	}
	
	public void generateAnnotatedCode() throws IOException, CoreException {
		
		//create project
		IProgressMonitor progressMonitor = new NullProgressMonitor();
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IProject project = root.getProject(annotatedJavaProject);
		project.create(progressMonitor);
	
		XMIResourceFactoryImpl xmiResourceFactoryImpl = new XMIResourceFactoryImpl() {
			public Resource createResource(URI uri) {
				XMIResource xmiResource = new XMIResourceImpl(uri);
				return xmiResource;
			}
		};

		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("xmi", xmiResourceFactoryImpl);

		GenerateJavaExtended javaGenerator = new GenerateJavaExtended(
				URI.createFileURI(annotatedModelPath),
				new File(project.getLocationURI().getRawPath() + "/src"), new ArrayList<Object>());

		javaGenerator.doGenerate(null);
		
		//open project
		project.open(progressMonitor);
		//assign Java Nature
//		IProjectDescription description = project.getDescription();
//		description.setNatureIds(new String[] {JavaCore.NATURE_ID});
//		project.setDescription(description, this.ip);
		
		this.annotatedProject = project;
	}
	
	@SuppressWarnings("unchecked")
	public void generateModelCode() throws IOException {
		IJavaProject javaProject = JavaCore.create(this.project);
		
		//Create a discoverer for a Java project
		DiscoverJavaModelFromJavaProject javaDiscoverer = new DiscoverJavaModelFromJavaProject();

		//Parameters of the discoverer
		@SuppressWarnings("rawtypes")
		Map javaDiscoveryParameters = new HashMap();
		javaDiscoveryParameters.put(DefaultDiscoverer.PARAMETER_SILENT_MODE, true);
		javaDiscoveryParameters.put(DefaultDiscoverer.PARAMETER_BROWSE_RESULT, false);

		//Execute the discoverer (javaProject is a IJavaProject) and serialize the model to <project_name>.javaxmi
		javaDiscoverer.discoverElement(javaProject, javaDiscoveryParameters);
	}
	
}
