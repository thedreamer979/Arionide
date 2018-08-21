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
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;

public class PrimitiveRenderingSystem {
	
	private final Map<PrimitiveType, RenderingContext> genericPrimitives = new HashMap<>();
	private final Queue<Primitive> renderingQueue = new PriorityQueue<>();
	
	public void registerGenericPrimitive(PrimitiveType type, RenderingContext context) {
		assert type != null;
		assert context != null;
		
		this.genericPrimitives.put(type, context);
		
		context.load();
	}
	
	public void renderLater(Primitive primitive) {
		assert primitive != null;
		assert primitive.getType() != null;
		
		this.renderingQueue.add(primitive);
	}
	
	public void processRenderingQueue() {
		Primitive lastPrimitive = null;
				
		while(!this.renderingQueue.isEmpty()) {
			Primitive primitive = this.renderingQueue.poll();
			RenderingContext context = this.genericPrimitives.get(primitive.getType());
						
			boolean typeUpdate = lastPrimitive == null || primitive.getType() != lastPrimitive.getType();
						
			if(typeUpdate) {
				if(lastPrimitive != null) {
					this.genericPrimitives.get(lastPrimitive.getType()).exit();
				}

				context.enter();
								
				for(BigInteger identifier : context.getIdentificationScheme()) {
					primitive.updateProperty(identifier.getLowestSetBit() / Identification.PARTITION_SIZE);
				}
			} else {
				BigInteger diff = lastPrimitive.getFingerprint().xor(primitive.getFingerprint());
								
				for(BigInteger identifier : context.getIdentificationScheme()) {
					if(!diff.and(identifier).equals(BigInteger.ZERO)) {
						primitive.updateProperty(context.getIdentificationScheme().length - 1 - identifier.getLowestSetBit() / Identification.PARTITION_SIZE);
					}
				}
			}
			
			primitive.render();
			
			lastPrimitive = primitive;
		}
		
		if(lastPrimitive != null) {
			this.genericPrimitives.get(lastPrimitive.getType()).exit();
		}
	}

	public void updateAspectRatio(float newRatio) {
		for(RenderingContext context : this.genericPrimitives.values()) {
			context.onAspectRatioUpdate(newRatio);
		}
	}
}