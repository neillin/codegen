/**
 * 
 */
package com.thwt.core.codegen.service.model;

import javax.lang.model.element.TypeElement;

import com.thwt.core.annotation.Jsonizable;
import com.thwt.core.codegen.Case;
import com.thwt.core.util.Utils;

/**
 * @author linzhenwu
 *
 */
public class EntityClassModel extends JsonizableModel {

	
	public EntityClassModel(TypeElement elem) {
		super(elem);
	}

	/* (non-Javadoc)
	 * @see com.thwt.core.codegen.service.model.JsonizableModel#processJsoniableAnnotation()
	 */
	@Override
	protected void processJsoniableAnnotation() {
		Jsonizable ann = getElement().getAnnotation(Jsonizable.class);
		if(ann != null) {
		    this.category = ann.category();
		    this.jsonName = Utils.isBlank(ann.jsonName()) ? getTypeInfo().getSimpleName(Case.LOWER_CAMEL) : ann.jsonName();
		    this.generateConverter = ann.generateConverter();
		}else {
			this.category = "JPAEntity";
			this.jsonName = getTypeInfo().getSimpleName(Case.LOWER_CAMEL);
			this.generateConverter = true;
		}
	}


	
}
