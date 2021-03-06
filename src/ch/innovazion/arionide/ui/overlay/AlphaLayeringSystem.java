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

import java.util.Stack;

public class AlphaLayeringSystem {
	
	private final Object[] layers = new Object[AlphaLayer.values().length];

	private int currentAlpha;
	
	public AlphaLayeringSystem() {		
		for(int i = 0; i < this.layers.length; i++) {
			Stack<Integer> layer = new Stack<>();
			layer.push(0xFF);
			this.layers[i] = layer;
		}
	}
	
	/* Returns the resulting alpha value */
	@SuppressWarnings("unchecked")
	public int push(AlphaLayer layer, int value) {
		((Stack<Integer>) this.layers[layer.ordinal()]).push(value);
		return this.commit();
	}
	
	/* Returns the resulting alpha value */
	@SuppressWarnings("unchecked")
	public int pop(AlphaLayer layer) {		
		((Stack<Integer>) this.layers[layer.ordinal()]).pop();
		return this.commit();
	}
	
	@SuppressWarnings("unchecked")
	private int commit() {
		int alpha = 0xFF;
		
		for(int i = 0; i < this.layers.length; i++) {
			alpha *= ((Stack<Integer>) this.layers[i]).lastElement();
			alpha /= 255;
		}
				
		return this.currentAlpha = alpha;
	}
	
	public int getCurrentAlpha() {
		return this.currentAlpha;
	}
}