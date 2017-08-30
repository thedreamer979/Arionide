/*******************************************************************************
 * This file is part of Arionide.
 *
 * Arionide is an IDE whose purpose is to build a language from scratch. It is the work of Arion Zimmermann in context of his TM.
 * Copyright (C) 2017 AZEntreprise Corporation. All rights reserved.
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
package org.azentreprise.arionide.ui.menu;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.azentreprise.arionide.events.Event;
import org.azentreprise.arionide.events.MenuEvent;
import org.azentreprise.arionide.ui.AppManager;

public abstract class Menu {
	
	private final AppManager manager;
	private List<String> elements = new ArrayList<>();
	private int current;
	private boolean active;
	
	protected Menu(AppManager manager, String... elements) {
		this.manager = manager;
		this.elements.addAll(Arrays.asList(elements));
	}
	
	public AppManager getManager() {
		return this.manager;
	}
	
	public List<String> getElements() {
		return this.elements;
	}
	
	public void setElements(List<String> elements) {
		this.elements = elements;
	}
	
	protected void fire(Event event) {
		this.manager.getEventDispatcher().fire(event);
	}
	
	protected void show(Menu menu) {
		this.manager.getEventDispatcher().fire(new MenuEvent(menu));
	}
	
	public int getCurrentID() {
		return this.current;
	}
	
	public void setCurrentID(int id) {
		this.current = id;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return this.active;
	}
	
	public void select(int id) {
		this.setCurrentID(id);
		this.onSelect(id);
		this.onSelect(this.elements.get(id));
	}
	
	public void click() {
		this.onClick(this.current);
		this.onClick(this.elements.get(this.current));
	}
		
	protected void onClick(String element) {
		;
	}
	
	protected void onClick(int id) {
		;
	}
	
	protected void onSelect(String element) {
		;
	}
	
	protected void onSelect(int id) {
		;
	}
}
