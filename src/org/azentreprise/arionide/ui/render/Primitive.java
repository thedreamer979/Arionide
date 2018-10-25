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
package org.azentreprise.arionide.ui.render;

import java.math.BigInteger;

public abstract class Primitive implements Comparable<Primitive>, Preparable {
	
	private int requestedActions = 0x0;
	
	public int compareTo(Primitive other) {
		return this.getStateFingerprint().compareTo(other.getStateFingerprint());
	}
	
	protected void requestAction(int identifier) {
		this.requestedActions |= identifier;
	}
	
	protected void clearAction(int identifier) {
		this.requestedActions &= ~identifier;
	}
	
	protected int getRequestedActions() {
		return this.requestedActions;
	}
	
	protected abstract BigInteger getStateFingerprint();
	protected abstract PrimitiveType getType();
	protected abstract void updateProperty(int identifier);
	protected abstract void processAction(int identifier);
	protected abstract void render();
}