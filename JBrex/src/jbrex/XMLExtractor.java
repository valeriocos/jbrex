package jbrex;

import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.m2m.atl.core.ATLCoreException;
import org.eclipse.m2m.atl.core.IExtractor;
import org.eclipse.m2m.atl.core.IModel;
import org.eclipse.m2m.atl.core.emf.EMFModel;

/**
 * Implementation of an XML extractor using EMF
 * @author Hugo Bruneliere
 */
public class XMLExtractor implements IExtractor {

	/**
	 * {@inheritDoc}
	 * 
	 * @see org.eclipse.m2m.atl.core.IExtractor#extract(org.eclipse.m2m.atl.core.IModel, java.lang.String)
	 */
	public void extract(IModel sourceModel, String target) throws ATLCoreException {
		extract(sourceModel, target, Collections.<String, Object> emptyMap());
	}

	/**
	 * {@inheritDoc} Below the target parameter semantic.
	 * <ul>
	 * <li><b>File system Resource: </b><code>file:/<i>path</i></code></li>
	 * <li><b>Workspace Resource: </b><code>platform:/resource/<i>path</i></code></li>
	 * </ul>
	 * 
	 * @see org.eclipse.m2m.atl.core.IExtractor#extract(org.eclipse.m2m.atl.core.IModel, java.lang.String,
	 *      java.util.Map)
	 */
	public void extract(IModel sourceModel, String target, Map<String, Object> options)
			throws ATLCoreException {
		try {
			String formattedTarget = target;
			if( target.startsWith("file:") )
				formattedTarget = target.substring(target.indexOf(":")+2);
			OutputStream out = new FileOutputStream(formattedTarget);
			extract(sourceModel, out, options);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Extracts an {@link EMFModel} (which conforms to the XML metamodel) to an {@link OutputStream}.
	 * 
	 * @param sourceModel
	 *            the source {@link EMFModel} (which conforms to the XML metamodel) to be extracted
	 * @param target
	 *            the target {@link OutputStream} where the source model must be extracted
	 * @param options
	 *            the extraction parameters (none are required there)
	 */
	public void extract(IModel sourceModel, OutputStream target, Map<String, Object> options)
			throws ATLCoreException {
		PrintStream outStream = new PrintStream(new BufferedOutputStream(target));
		outStream.println("<?xml version = '1.0' encoding = 'ISO-8859-1' ?>");
		
		EMFModel xmlModel = (EMFModel) sourceModel;
		EClass rootClass = (EClass) xmlModel.getReferenceModel().getMetaElementByName("Root");
		Set<EObject> rootElements = xmlModel.getElementsByType(rootClass);
		serializeContent(
				rootElements.iterator().next(), 
				xmlModel, outStream, "");

		outStream.close();
	}

	/**
	 * The recursive method effectively serializing the content of the XML model.
	 * 
	 * @param xmlModelElement
	 * 				the current XML model element ({@link EObject}) to be serialized
	 * @param xmlModel
	 * 				the XML model ({@link EMFModel}) to be serialized	
	 * @param outStream 
	 * 				the target {@link PrintStream} where the source model must be extracted
	 * @param indent
	 * 				the current indentation level
	 */
	@SuppressWarnings("unchecked")
	private void serializeContent(EObject xmlModelElement, EMFModel xmlModel, PrintStream outStream, String indent) {
		String oTypeName = getStringValue(xmlModelElement.eClass(), "name");
		if(oTypeName.equals("Element") || oTypeName.equals("Root")) {
			String name = getStringValue(xmlModelElement, "name");
			outStream.print(indent + "<" + name);

			EList<EObject> children = 
				(EList<EObject>) xmlModelElement.eGet(xmlModelElement.eClass().getEStructuralFeature("children"));
			boolean hasElements = false;
			boolean hasTexts = false;
			for(Iterator<EObject> i = children.iterator() ; i.hasNext() ; ) {
				EObject currentModelElement = i.next();
				String typeName = getStringValue(currentModelElement.eClass(), "name");
				if(typeName.equals("Attribute")) {
					outStream.print(" " + getStringValue(currentModelElement, "name") + "=\"" 
										+ convertText(getStringValue(currentModelElement, "value"), true) + "\"");
				} else if(typeName.equals("Element")) {
					hasElements = true;
				} else if(typeName.equals("Text")) {
					hasTexts = true;
				}
			}
			if(hasElements) {
				outStream.println(">");

				for(Iterator<EObject> i = children.iterator() ; i.hasNext() ; ) {
					EObject currentModelElement = i.next();
					String typeName = getStringValue(currentModelElement.eClass(), "name");
					if(typeName.equals("Element")) {
						this.serializeContent(currentModelElement, xmlModel, outStream, indent + "  ");
					} else if(typeName.equals("Text")) {
						outStream.print(convertText(getStringValue(currentModelElement, "value"), false));
					}
				}

				outStream.println(indent + "</" + name + ">");
			} else if(hasTexts) {
				outStream.print(">");

				for(Iterator<EObject> i = children.iterator() ; i.hasNext() ; ) {
					EObject currentModelElement = i.next();
					String typeName = getStringValue(currentModelElement.eClass(), "name");
					if(typeName.equals("Text")) {
						outStream.print(convertText(getStringValue(currentModelElement, "value"), false));
					}
				}

				outStream.println("</" + name + ">");
			} else {
				outStream.println("/>");
			}
		}
	}

	/**
	 * Returns the string value of a specified structural feature (from a given model element). 
	 * @param modelElement
	 * @param structuralFeatureName
	 * @return
	 */
	private String getStringValue(EObject modelElement, String structuralFeatureName) {
		Object ret = modelElement.eGet(modelElement.eClass().getEStructuralFeature(structuralFeatureName));
		
		if(!(ret instanceof String))
			throw new RuntimeException("could not read " + structuralFeatureName + " of " + modelElement + " : " + modelElement.eClass());
		
		return (String) ret;
	}

	/**
	 * Converts the input string into an XML compatible string format (special character's conversion).
	 * @param in
	 * @param inAttr
	 * @return
	 */
	private String convertText(String in, boolean inAttr) {
		String ret = in.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;");
		
		if(inAttr) {
			ret = ret.replaceAll("\n", "&#10;").replaceAll("\t", "&#9;").replaceAll("'", "&apos;").replaceAll("\"", "&quot;");
		}
		
		return ret;
	}
		
}
