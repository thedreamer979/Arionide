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
package org.azentreprise.arionide.ui.overlay;

import java.awt.Font;
import java.awt.FontFormatException;
import java.io.IOException;

import org.azentreprise.Debug;
import org.azentreprise.arionide.debugging.IAm;
import org.azentreprise.arionide.ui.layout.Surface;

public abstract class Component extends Surface {
	
	private final View parent;
	private Font font;
	
	@IAm("initializing a component")
	public Component(View parent) {
		this.parent = parent;
		
		try {
			this.font = Font.createFont(Font.TRUETYPE_FONT, this.parent.getAppManager().getResources().getResource("font"));
		} catch (FontFormatException | IOException exception) {
			Debug.exception(exception);
		}
	}
	
	public abstract boolean isFocusable();
	
	public View getParentView() {
		return this.parent;
	}
	
	public Component alterFont(int style, float size) {
		this.font = this.font.deriveFont(style, size);
		return this;
	}
	
	public Component alterFont(int style) {
		this.font = this.font.deriveFont(style);
		return this;
	}
	
	public Component alterFont(float size) {
		this.font = this.font.deriveFont(size);
		return this;
	}
	
	protected Font getFont() {
		return this.font;
	}
	
	public void focusGained() {
		return;
	}
	
	public void focusLost() {
		return;
	}
}
