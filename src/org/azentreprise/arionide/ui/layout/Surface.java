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
 * The copy of the GNU General Public License can be found in the 'LICENSE.txt' file inside the JAR archive or in your personal directory as 'Arionide/LICENSE.txt'.
 *******************************************************************************/
package org.azentreprise.arionide.ui.layout;

import java.awt.Color;
import java.awt.Graphics2D;

import org.azentreprise.arionide.ui.Drawable;

public abstract class Surface implements Drawable {
	
	private int x;
	private int y;
	private int width;
	private int height;
	
	private Color backgroundColor;
		
	public void setLayoutBounds(int x, int y, int width, int height) {
		this.x = x;
		this.y = y;
		this.width = width;
		this.height = height;
	}
	
	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
	}
	
	public final void draw(Graphics2D g2d) {
		g2d = (Graphics2D) g2d.create(this.x, this.y, this.width, this.height);
	
		g2d.setColor(this.backgroundColor);
		g2d.fillRect(this.x, this.y, this.width, this.height);
		
		this.drawSurface(g2d);
	}
	
	public abstract void drawSurface(Graphics2D g2d);
}