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
package ch.innovazion.arionide.project.managers;

import java.util.List;

import ch.innovazion.arionide.events.MessageEvent;
import ch.innovazion.arionide.events.MessageType;
import ch.innovazion.arionide.project.Manager;
import ch.innovazion.arionide.project.Storage;
import ch.innovazion.arionide.project.mutables.MutableInheritanceElement;

public class InheritanceManager extends Manager {
	protected InheritanceManager(Storage storage) {
		super(storage);
	}

	public MessageEvent inherit(int child, int parent) {
		if(child != parent) {
			MutableInheritanceElement parentElement = getInheritance().get(parent);
			MutableInheritanceElement childElement = getInheritance().get(child);
			
			if(recursiveCheck(childElement, parent) && recursiveCheck(parentElement, child)) { /* Check for cyclicity */
				List<Integer> children = parentElement.getMutableChildren();
				List<Integer> parents = childElement.getMutableParents();
				
				if(!children.contains(child)) {
					children.add(child);
				}
		
				if(!parents.contains(parent)) {
					parents.add(parent);
				}
				
				saveInheritance();
		
				return success();
			} else {
				return new MessageEvent("Cyclic inheritance is not permitted", MessageType.ERROR);
			}
		} else {
			return new MessageEvent("A structure cannot inherit itself", MessageType.ERROR);
		}
	}
	
	private boolean recursiveCheck(MutableInheritanceElement object, int potentialParent) {		
		for(int nextGenParent : object.getParents()) {
			if(nextGenParent == potentialParent || !recursiveCheck(getInheritance().get(nextGenParent), potentialParent)) {
				return false;
			}
		}
		
		return true;
	}
	
	public MessageEvent disinherit(Integer parent, Integer child) {
		getInheritance().get(parent).getMutableChildren().remove(child);
		getInheritance().get(child).getMutableParents().remove(parent);
		
		saveInheritance();
		
		return success();
	}
}
