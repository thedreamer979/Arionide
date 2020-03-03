/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE used to conceive applications and algorithms in a three-dimensional environment. 
 * It is the work of Arion Zimmermann for his final high-school project at Calvin College (Geneva, Switzerland).
 * Copyright (C) 2016-2020 Innovazion. All rights reserved.
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
package ch.innovazion.arionide.lang.natives.instructions;

import java.util.List;

import ch.innovazion.arionide.lang.Data;
import ch.innovazion.arionide.lang.natives.NativeDataCommunicator;
import ch.innovazion.arionide.lang.symbols.Parameter;

public class Define implements NativeInstruction {
	
	private final Data name;
	private final Data value;
	private final Data local;
	
	public Define(Data name, Data value, Data local) {
		this.name = name;
		this.value = value;
		this.local = local;
	}
	
	public boolean execute(NativeDataCommunicator communicator, List<Integer> references) {
		if(!this.name.getDisplayValue().startsWith(Parameter.VAR)) {
			communicator.exception("The variable '" + this.name.getDisplayValue() + "' is not defined with the 'New variable' button but with the 'Custom string' one.");
			return false;
		}
		
		String value = this.value.getDisplayValue();
		
		if(value.startsWith(Parameter.VAR)) {
			value = communicator.getVariable(value.substring(4)).getDisplayValue();
		}
				
		boolean local = this.local.getDisplayValue().substring(1).equals("1");
		
		Data data = new Data(this.name.getDisplayValue().substring(4), value, this.value.getType());
		
		communicator.setVariable(data.getName(), local, data);
		
		return true;
	}
}