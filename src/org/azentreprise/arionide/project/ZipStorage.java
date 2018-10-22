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
package org.azentreprise.arionide.project;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.azentreprise.arionide.debugging.Debug;
import org.azentreprise.arionide.debugging.IAm;

public class ZipStorage extends Storage {

	private static final Map<String, String> fileSystemEnvironment = new HashMap<>();

	static {
		fileSystemEnvironment.put("create", "true");
	}
	
	private final File location;
	
	private URI uri;
	private FileSystem fs;
	
	private Path metaPath;
	private Path hierarchyPath;
	private Path inheritancePath;
	private Path callGraphPath;
	private Path historyPath;
	private Path structureMetaPath;
	private Path dataPath;
		
	@IAm("initializing the project storage")
	public ZipStorage(File path) {
		this.location = path;
	}
	
	public void initFS() {
		try {
			try {
				Path backup = new File(this.location.getParentFile(), this.location.getName() + ".bak").toPath();
				
				if(this.location.exists()) {
					Files.copy(this.location.toPath(), backup, StandardCopyOption.REPLACE_EXISTING);
				}
				
				this.uri = URI.create("jar:" + this.location.toURI());
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			this.initFS0();
						
			this.metaPath = this.init(null, null, "meta", "project");
			this.hierarchyPath = this.init(ArrayList<HierarchyElement>::new, e -> this.hierarchy = e, "struct", "hierarchy");
			this.inheritancePath = this.init(HashMap<Integer, InheritanceElement>::new, e -> this.inheritance = e, "struct", "inheritance");
			this.callGraphPath = this.init(ArrayList<HierarchyElement>::new, e -> this.callGraph = e, "struct", "callgraph");
			this.structureMetaPath = this.init(HashMap<Integer, StructureMeta>::new, e -> this.structMeta = e, "meta", "structures");
			this.historyPath = this.init(ArrayList<HistoryElement>::new, e -> this.history = e, "history");
			this.dataPath = this.init(HashMap<Integer, List<HierarchyElement>>::new, e -> this.data = e, "data");
		} catch (IOException exception) {
			Debug.exception(exception);
		}
	}
	
	private void initFS0() throws IOException {
		try {
			this.fs = FileSystems.getFileSystem(this.uri);
		} catch(Exception e) {
			this.fs = FileSystems.newFileSystem(this.uri, fileSystemEnvironment);
		}
	}
	
	protected void closeFS() {
		if(this.fs != null && this.fs.isOpen()) {
			try {
				this.fs.close();
			} catch (IOException e) {
				// Do not use an AWT popup in a shutdown hook
				e.printStackTrace();
			}
		}
	}
	
	private <T> Path init(Supplier<T> allocator, Consumer<T> initializer, String first, String... more) throws IOException {
		Path path = this.fs.getPath(first, more);
		
		if(!Files.exists(path)) {
			if(more.length > 0) {
				String[] dirs = new String[more.length - 1];
				
				System.arraycopy(more, 0, dirs, 0, dirs.length);
				
				Files.createDirectories(this.fs.getPath(first, dirs));
			}
			
			Files.createFile(path);
						
			if(allocator != null) {
				T object = allocator.get();
				
				if(initializer != null) {
					initializer.accept(object);
				}
				
				this.save(path, object);
			}
		}
			
		return path;
	}
	
	public File getLocation() {
		return this.location;
	}
		
	public Path getMetaPath() {
		return this.metaPath;
	}
	
	public void loadHierarchy() {	
		this.hierarchy = this.load(this.hierarchyPath);
	}
	
	public void saveHierarchy() {
		this.save(this.hierarchyPath, this.hierarchy);
	}
	
	public void loadInheritance() {	
		this.inheritance = this.load(this.inheritancePath);
	}
	
	public void saveInheritance() {
		this.save(this.inheritancePath, this.inheritance);
	}
	
	public void loadCallGraph() {	
		this.callGraph = this.load(this.callGraphPath);
	}
	
	public void saveCallGraph() {
		this.save(this.callGraphPath, this.callGraph);
	}
	
	public void loadStructureMeta() {	
		this.structMeta = this.load(this.structureMetaPath);
	}
	
	public void saveStructureMeta() {
		this.save(this.structureMetaPath, this.structMeta);
	}
	
	public void loadHistory() {	
		this.history = this.load(this.historyPath);
	}
	
	public void saveHistory() {
		this.save(this.historyPath, this.history);
	}
	
	public void loadData() {
		this.data = this.load(this.dataPath);
	}
	
	public void saveData() {
		this.save(this.dataPath, this.data);
	}
	
	@SuppressWarnings("unchecked")
	private synchronized <T extends Serializable> T load(Path path) {
		try(ObjectInputStream input = new ObjectInputStream(Files.newInputStream(path))) {
			return (T) input.readObject();
		} catch (IOException | ClassNotFoundException exception) {
			Debug.exception(exception);
			return null;
		}
	}
	
	private synchronized void save(Path path, Object object) {
		assert object instanceof Serializable;

		try(ObjectOutputStream output = new ObjectOutputStream(Files.newOutputStream(path, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING))) {
			output.writeObject(object);
		} catch (IOException exception) {
			Debug.exception(exception);
		}
	}
}
