/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE whose purpose is to build a language from scratch. It is the work of Arion Zimmermann in context of his TM.
 * Copyright (C) 2018 AZEntreprise Corporation. All rights reserved.
 *
 * Arionide is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Arionide is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Arionide.  If not, see <http://www.gnu.org/licenses/>.
 *
 * The copy of the GNU General Public License can be found in the 'LICENSE.txt' file inside the src directory or inside the JAR archive.
 *******************************************************************************/
package ch.innovazion.arionide.lang.natives;

import ch.innovazion.arionide.lang.SpecificationElement;
import ch.innovazion.arionide.lang.Validator;

public class StructureValidator implements Validator {
	public boolean validate(String data) {
		if(data.startsWith(SpecificationElement.VAR)) {
			return true;
		} else {
			int index = data.indexOf(SpecificationElement.ALIAS);
			
			if(index > -1) {
				data = data.substring(index + 3);
			}
			
			String[] elements = data.split(";");
			
			try {
				for(String element : elements) {
					if(!element.isEmpty()) {
						Integer.parseInt(element.trim());
					}
				}
				
				return true;
			} catch(NumberFormatException e) {
				return false;
			}
		}
	}
}