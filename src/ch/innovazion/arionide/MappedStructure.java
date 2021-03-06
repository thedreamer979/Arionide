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
package ch.innovazion.arionide;

import java.util.Map;

import ch.innovazion.arionide.coders.Decoder;
import ch.innovazion.arionide.coders.Encoder;
import ch.innovazion.arionide.debugging.IAm;

public interface MappedStructure {
	@IAm("getting a property")
	public <T> T getProperty(String key, Decoder<T> decoder);
	
	@IAm("setting a property")
	public <T> void setProperty(String key, T value, Encoder<T> encoder);
	
	@IAm("getting the protocol mapping")
	public Map<?, ?> getProtocolMapping();
}
