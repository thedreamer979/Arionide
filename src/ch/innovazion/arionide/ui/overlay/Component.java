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
package ch.innovazion.arionide.ui.overlay;

import java.util.List;

import ch.innovazion.arionide.debugging.IAm;
import ch.innovazion.arionide.ui.AppManager;
import ch.innovazion.arionide.ui.layout.Surface;

public abstract class Component extends Surface {
	
	private final Container parent;
	
	private boolean enabled;
	
	@IAm("initializing a component")
	public Component(Container parent) {
		this.parent = parent;
	}
	
	public Component enclose(List<Component> container) {
		container.add(this);
		return this;
	}
		
	public Container getParent() {
		return this.parent;
	}
	
	public AppManager getAppManager() {
		return this.parent.getAppManager();
	}
	
	public void update() {
		;
	}
	
	public final Component setVisible(boolean visible) {		
		if(!visible) {
			componentWillDisappear();
		} else {
			componentWillAppear();
		}
		
		super.setVisible(visible);
		
		return this;
	}
	
	public Component setEnabled(boolean enabled) {
		this.enabled = enabled;
		return this;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	protected void componentWillAppear() {
		;
	}
	
	protected void componentWillDisappear() {
		;
	}
	
	public abstract boolean isFocusable();
}
