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
package ch.innovazion.arionide.ui.topology;

public class Affine extends Application {

	private Scalar scalar;
	private Translation translation;
	
	public Affine() {
		this(new Scalar(), new Translation());
	}
	
	public Affine(float scaleX, float scaleY, float translateX, float translateY) {
		this(new Scalar(scaleX, scaleY), new Translation(translateX, translateY));
	}
	
	public Affine(Scalar scalar, Translation translation) {
		this.scalar = scalar;
		this.translation = translation;
	}
	
	public void setScalar(Scalar scalar) {
		this.scalar = scalar;
	}
	
	public void setTranslation(Translation translation) {
		this.translation = translation;
	}
	
	public void setScalarAndTranslation(Scalar scalar, Translation translation) {
		this.scalar = scalar;
		this.translation = translation;
	}
	
	public Scalar getScalar() {
		return this.scalar;
	}
	
	public Translation getTranslation() {
		return this.translation;
	}
	
	public void apply(Set input) {
		this.scalar.apply(input);
		this.translation.apply(input);
	}

	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + this.scalar.hashCode();
		result = prime * result + this.translation.hashCode();
		return result;
	}

	public boolean equals(Object obj) {
		if(obj instanceof Affine) {
			Affine other = (Affine) obj;
			return this.scalar.equals(other.scalar) && this.translation.equals(other.translation);
		} else {
			return false;
		}
	}
}