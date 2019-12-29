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
package ch.innovazion.arionide.ui.render;

import java.math.BigInteger;
import java.util.PriorityQueue;

public class RenderingQueue extends PriorityQueue<Primitive> {
	
	private static final long serialVersionUID = -4699975414525922255L;

	private final RenderingContext context;
	private BigInteger trace = BigInteger.ZERO;
	
	protected RenderingQueue(RenderingContext context) {
		this.context = context;
	}
	
	protected RenderingQueue(RenderingQueue parent) {
		this.context = parent.context;
		this.trace = parent.trace;
	}
	
	protected BigInteger getStateDifference(Primitive other) {
		return this.trace.xor(other.getStateFingerprint());
	}
	
	protected void synchroniseState(RenderingQueue other) {
		this.trace = other.trace;
	}
	
	protected void updateState(Primitive other) {
		this.trace = other.getStateFingerprint();
	}
	
	protected BigInteger getAndUpdateStateDifference(Primitive other) {
		BigInteger difference = this.getStateDifference(other);
		this.updateState(other);
		return difference;
	}
	
	protected RenderingContext getContext() {
		return this.context;
	}
}