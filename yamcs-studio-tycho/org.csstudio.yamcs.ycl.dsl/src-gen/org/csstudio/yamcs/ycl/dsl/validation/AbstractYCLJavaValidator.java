/*
 * generated by Xtext
 */
package org.csstudio.yamcs.ycl.dsl.validation;
 
import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;

public class AbstractYCLJavaValidator extends org.eclipse.xtext.validation.AbstractDeclarativeValidator {

	@Override
	protected List<EPackage> getEPackages() {
	    List<EPackage> result = new ArrayList<EPackage>();
	    result.add(org.csstudio.yamcs.ycl.dsl.ycl.YclPackage.eINSTANCE);
		return result;
	}

}
