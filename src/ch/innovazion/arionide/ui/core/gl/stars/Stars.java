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
package ch.innovazion.arionide.ui.core.gl.stars;

import java.util.Arrays;
import java.util.List;

import com.jogamp.opengl.GL4;

import ch.innovazion.arionide.ui.core.gl.BufferGenerator;
import ch.innovazion.arionide.ui.core.gl.BufferObject;

public class Stars extends BufferObject<StarsContext, StarsSettings> {

	private final int count;
	private final BufferGenerator generator;
	
	public Stars(int count, float size) {
		super(new StarsSettings(size));
		
		this.count = count;
		this.generator = new StarsGenerator(count);
		
		this.setBufferInitializer(this.generator, this::setupAttributes);
	}
	
	private void setupAttributes(StarsContext context) {
		setupFloatAttribute(context.getPositionAttribute(), 3, 6, 0);
		setupFloatAttribute(context.getColorAttribute(), 3, 6, 3);
	}

	protected void update(GL4 gl, StarsContext context, StarsSettings settings) {
		gl.glPointSize(settings.getSize());
		
		gl.glUniformMatrix4fv(context.getModelUniform(), 1, false, settings.getModel());
		gl.glUniformMatrix4fv(context.getViewUniform(), 1, false, settings.getView());
		gl.glUniformMatrix4fv(context.getProjectionUniform(), 1, false, settings.getProjection());
	}

	protected void renderObject(GL4 gl) {
		gl.glDrawArrays(GL4.GL_POINTS, 0, count);
	}

	protected List<BufferGenerator> getGenerators() {
		return Arrays.asList(generator);
	}
	
	protected int getBufferCount() {
		return 1;
	}
}