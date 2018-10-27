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
package ch.innovazion.arionide.ui.core.opengl;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.Function;
import java.util.function.Predicate;

import org.joml.Matrix4f;
import org.joml.Vector2d;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector4f;

import com.jogamp.newt.event.KeyEvent;
import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLContext;

import ch.innovazion.arionide.coders.CameraInfo;
import ch.innovazion.arionide.coders.Coder;
import ch.innovazion.arionide.debugging.Debug;
import ch.innovazion.arionide.events.ActionEvent;
import ch.innovazion.arionide.events.ActionType;
import ch.innovazion.arionide.events.ClickEvent;
import ch.innovazion.arionide.events.Event;
import ch.innovazion.arionide.events.EventHandler;
import ch.innovazion.arionide.events.MenuEvent;
import ch.innovazion.arionide.events.MessageEvent;
import ch.innovazion.arionide.events.MessageType;
import ch.innovazion.arionide.events.MoveEvent;
import ch.innovazion.arionide.events.PressureEvent;
import ch.innovazion.arionide.events.WheelEvent;
import ch.innovazion.arionide.events.dispatching.IEventDispatcher;
import ch.innovazion.arionide.lang.Data;
import ch.innovazion.arionide.lang.Reference;
import ch.innovazion.arionide.lang.SpecificationElement;
import ch.innovazion.arionide.project.HierarchyElement;
import ch.innovazion.arionide.project.Project;
import ch.innovazion.arionide.project.StructureMeta;
import ch.innovazion.arionide.ui.AppDrawingContext;
import ch.innovazion.arionide.ui.OpenGLContext;
import ch.innovazion.arionide.ui.core.CoreRenderer;
import ch.innovazion.arionide.ui.core.RenderingScene;
import ch.innovazion.arionide.ui.core.TeleportInfo;
import ch.innovazion.arionide.ui.core.geom.CodeGeometry;
import ch.innovazion.arionide.ui.core.geom.Connection;
import ch.innovazion.arionide.ui.core.geom.CoreGeometry;
import ch.innovazion.arionide.ui.core.geom.CurrentCodeGeometry;
import ch.innovazion.arionide.ui.core.geom.Geometry;
import ch.innovazion.arionide.ui.core.geom.GeometryException;
import ch.innovazion.arionide.ui.core.geom.HierarchicalGeometry;
import ch.innovazion.arionide.ui.core.geom.WorldElement;
import ch.innovazion.arionide.ui.menu.MainMenus;
import ch.innovazion.arionide.ui.menu.StructureList;
import ch.innovazion.arionide.ui.menu.code.CodeEditor;
import ch.innovazion.arionide.ui.menu.code.ReferenceEditor;
import ch.innovazion.arionide.ui.menu.code.TypeEditor;
import ch.innovazion.arionide.ui.render.GLBounds;
import ch.innovazion.arionide.ui.render.PrimitiveFactory;
import ch.innovazion.arionide.ui.render.Text;
import ch.innovazion.arionide.ui.shaders.Shaders;
import ch.innovazion.arionide.ui.shaders.preprocessor.DummySettings;
import ch.innovazion.arionide.ui.topology.Bounds;
import ch.innovazion.arionide.ui.topology.Point;

public class OpenGLCoreRenderer implements CoreRenderer, EventHandler {
		
	private static final int structureRenderingQuality = 32;
	private static final boolean crystalMode = false;
	private static final int crystalQuality = 2;
	
	private static final int smallStars = 2048;
	private static final int bigStars = 256;
	
	private static final int forward = KeyEvent.VK_W;
	private static final int backward = KeyEvent.VK_S;
	private static final int left = KeyEvent.VK_A;
	private static final int right = KeyEvent.VK_D;
	private static final int up = KeyEvent.VK_SPACE;
	private static final int down = KeyEvent.VK_SHIFT;
	private static final int worldToggle = KeyEvent.VK_R;
	private static final int spawnKey = KeyEvent.VK_C;

	private static final float initialAcceleration = 0.005f * HierarchicalGeometry.MAKE_THE_UNIVERSE_GREAT_AGAIN;
	private static final float spaceFriction = 0.075f;

	private static final float fov = (float) Math.toRadians(60.0f);
	
	private static final float skyDistance = 32.0f * HierarchicalGeometry.MAKE_THE_UNIVERSE_GREAT_AGAIN;
	
	private static final float timeResolution = 0.00000005f;
	
			
	private final OpenGLContext context;
	private final IEventDispatcher dispatcher;
	private final Geometry mainGeometry;
	private final CurrentCodeGeometry mainCodeGeometry;
	private List<CodeGeometry> codeGeometries;
	//private final Geometry inheritanceGeometry;
		
	private final FloatBuffer modelData = FloatBuffer.allocate(16);
	private final FloatBuffer viewData = FloatBuffer.allocate(16);
	private final FloatBuffer projectionData = FloatBuffer.allocate(16);
	private final FloatBuffer currentToPreviousViewportData = FloatBuffer.allocate(16);

	private final FloatBuffer clearColor = FloatBuffer.allocate(4);
	private final FloatBuffer clearDepth = FloatBuffer.allocate(1);
	
	private final Matrix4f viewMatrix = new Matrix4f();
	private final Matrix4f projectionMatrix = new Matrix4f();
	private Matrix4f previousViewProjectionMatrix = new Matrix4f();
	
	private int fxFBO;
	private int fxColorTexture;
	private int fxDepthTexture;
	private boolean fxEnabled = false;
		
	private final List<ScaledRenderingInfo> structures = new ArrayList<>();
	private int connectionVAO;
	private int connectionVBO;
	private int fxVAO;
	private int smallStarsVAO;
	private int bigStarsVAO;
	
	private float zNear = 1.0f * HierarchicalGeometry.MAKE_THE_UNIVERSE_GREAT_AGAIN;
	private float zFar = 100.0f * HierarchicalGeometry.MAKE_THE_UNIVERSE_GREAT_AGAIN;
	
	private int structuresShader;
	private int spaceShader;
	private int fxShader;
	
	/* Structures uniforms */
	private int structModel;
	private int structView;
	private int structProjection;
	private int color;
	private int camera;
	private int specularColor;
	private int structuresLightPosition;
	private int ambientFactor;
	
	/* Space uniforms */
	private int spaceModel;
	private int spaceView;
	private int spaceProjection;
	private int spaceLightPosition;
	private int windowDimensions;

	/* FX uniforms */
	private int colorTexture;
	private int depthTexture;
	private int currentToPreviousViewportMatrix;
	private int fxLightPosition;
	private int exposure;
	private int pixelSize;
	
	private Project project;

	private Bounds bounds;
	private boolean isInWorld = false;
	private boolean isControlDown = false;
	private boolean isFastBindingMode = false;
	
	private final List<WorldElement> inside = Collections.synchronizedList(new ArrayList<>());
	private final List<WorldElement> buffer = new ArrayList<>();
	private WorldElement current;
	private WorldElement lookingAt;
	private WorldElement selected;
	
	private RenderingScene scene = null;
	
	private boolean needMenuUpdate = false;

	private float yaw = 0.0f;
	private float pitch = 0.0f;
	private Vector3f player = new Vector3f();
	private Vector3f sun = new Vector3f(0.0f, 2 * skyDistance, 0.0f);
	
	private Vector3f velocity = new Vector3f();
	private Vector3f acceleration = new Vector3f();
	private float generalAcceleration = initialAcceleration;
	private TeleportInfo teleport;
	
	private long lastPositionUpdate = System.nanoTime();
	
	public OpenGLCoreRenderer(AppDrawingContext context, IEventDispatcher dispatcher) {
		this.context = (OpenGLContext) context;
		this.dispatcher = dispatcher;
		this.mainGeometry = new CoreGeometry();
		this.mainCodeGeometry = new CurrentCodeGeometry();
		this.codeGeometries = new ArrayList<>();
		//this.inheritanceGeometry = null;
		
		dispatcher.registerHandler(this);
	}
	
	public void init(GL4 gl) {
		this.initShaders(gl);
		
		this.initFXFramebuffer(gl);
		
		this.clearColor.put(0, 0.0f).put(1, 0.0f).put(2, 0.0f).put(3, 1.0f);
		this.clearDepth.put(0, 1.0f);
		
		int structuresPositionAttribute = gl.glGetAttribLocation(this.structuresShader, "position");
		int fxPositionAttribute = gl.glGetAttribLocation(this.fxShader, "position");
		int starPositionAttribute = gl.glGetAttribLocation(this.spaceShader, "position");
		int starColorAttribute = gl.glGetAttribLocation(this.spaceShader, "color");

		IntBuffer vertexArrays = IntBuffer.allocate(5 + structureRenderingQuality);
		IntBuffer buffers = IntBuffer.allocate(6 + 2 * structureRenderingQuality);
		
		gl.glGenVertexArrays(vertexArrays.capacity(), vertexArrays);
		gl.glGenBuffers(buffers.capacity(), buffers);

		/* FX */
		GLBounds coords = new GLBounds(new Bounds(0, 0, 2, 2));		
		this.fxVAO = vertexArrays.get(1);
		
		gl.glBindVertexArray(this.fxVAO);

		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, buffers.get(2));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, 8 * Float.BYTES, coords.allocDataBuffer(8).putNorth().putSouth().getDataBuffer().flip(), GL4.GL_STATIC_DRAW);
		gl.glEnableVertexAttribArray(fxPositionAttribute);
		gl.glVertexAttribPointer(fxPositionAttribute, 2, GL4.GL_FLOAT, false, 0, 0);
		
		/* Connections */
		
		this.beginUsingVertexArray(gl, this.connectionVAO = vertexArrays.get(2));
		
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, this.connectionVBO = buffers.get(3));
		gl.glBufferData(GL4.GL_ARRAY_BUFFER, 6 * Double.BYTES, DoubleBuffer.allocate(6), GL4.GL_DYNAMIC_DRAW);
		
		this.setupTriAttribute(gl, structuresPositionAttribute, 3, 0);
		
		/* Stars */
				
		this.beginUsingVertexArray(gl, this.smallStarsVAO = vertexArrays.get(3));
		this.initBufferObject(gl, buffers.get(4), GL4.GL_ARRAY_BUFFER, smallStars, this::createStarsData);
		this.setupTriAttribute(gl, starPositionAttribute, 6, 0);
		this.setupTriAttribute(gl, starColorAttribute, 6, 3);

		this.beginUsingVertexArray(gl, this.bigStarsVAO = vertexArrays.get(4));
		this.initBufferObject(gl, buffers.get(5), GL4.GL_ARRAY_BUFFER, bigStars, this::createStarsData);
		this.setupTriAttribute(gl, starPositionAttribute, 6, 0);
		this.setupTriAttribute(gl, starColorAttribute, 6, 3);
		
		/* Structures */
		if(crystalMode) {
			int vertexArray = vertexArrays.get(5);
			int bufferID = buffers.get(6);
			
			this.structures.add(new ScaledRenderingInfo(vertexArray, bufferID + 1, crystalQuality + 2));
			
			this.beginUsingVertexArray(gl, vertexArray);
			this.initBufferObject(gl, bufferID, GL4.GL_ARRAY_BUFFER, crystalQuality + 2, this::createStructureShapeData);
			this.setupTriAttribute(gl, structuresPositionAttribute, 3, 0);
			 
			this.initBufferObject(gl, bufferID + 1, GL4.GL_ELEMENT_ARRAY_BUFFER, crystalQuality + 2, this::createStructureIndicesData);
		} else {
			for(int i = 0; i < structureRenderingQuality; i++) {
				int vertexArray = vertexArrays.get(i + 5);
				int bufferID = buffers.get(i * 2 + 6);
				int layers = i + 8;
				
				this.structures.add(new ScaledRenderingInfo(vertexArray, bufferID + 1, layers));
				
				this.beginUsingVertexArray(gl, vertexArray);
				this.initBufferObject(gl, bufferID, GL4.GL_ARRAY_BUFFER, layers, this::createStructureShapeData);
				this.setupTriAttribute(gl, structuresPositionAttribute, 3, 0);
				 
				this.initBufferObject(gl, bufferID + 1, GL4.GL_ELEMENT_ARRAY_BUFFER, layers, this::createStructureIndicesData);
			}
		}
		
		/* Init player */
		this.ajustAcceleration();
	}
	
	private void initShaders(GL4 gl) {		
		try {
			int structuresVert = Shaders.loadShader(gl, "structures.vert", DummySettings.VERTEX);
			int structuresFrag = Shaders.loadShader(gl, "structures.frag", DummySettings.FRAGMENT);
			int spaceVert = Shaders.loadShader(gl, "space.vert", DummySettings.VERTEX);
			int spaceFrag = Shaders.loadShader(gl, "space.frag", DummySettings.FRAGMENT);
			int fxVert = Shaders.loadShader(gl, "fx.vert", DummySettings.VERTEX);
			int fxFrag = Shaders.loadShader(gl, "fx.frag", DummySettings.FRAGMENT);
			
			this.structuresShader = gl.glCreateProgram();
			this.spaceShader = gl.glCreateProgram();
			this.fxShader = gl.glCreateProgram();
			
			gl.glAttachShader(this.structuresShader, structuresVert);
			gl.glAttachShader(this.structuresShader, structuresFrag);
			
			gl.glAttachShader(this.spaceShader, spaceVert);
			gl.glAttachShader(this.spaceShader, spaceFrag);
			
			gl.glAttachShader(this.fxShader, fxVert);
			gl.glAttachShader(this.fxShader, fxFrag);
		} catch(IOException exception) {
			Debug.exception(exception);
		}
		
		gl.glBindFragDataLocation(this.structuresShader, 0, "outColor");
		gl.glBindFragDataLocation(this.spaceShader, 0, "outColor");
		gl.glBindFragDataLocation(this.fxShader, 0, "outColor");

		gl.glLinkProgram(this.structuresShader);
		gl.glLinkProgram(this.spaceShader);
		gl.glLinkProgram(this.fxShader);

		this.loadStructuresUniforms(gl);
		this.loadSpaceUniforms(gl);
		this.loadFXUniforms(gl);
	}
	
	private void loadStructuresUniforms(GL4 gl) {
		this.structModel = gl.glGetUniformLocation(this.structuresShader, "model");
		this.structView = gl.glGetUniformLocation(this.structuresShader, "view");
		this.structProjection = gl.glGetUniformLocation(this.structuresShader, "projection");
		this.color = gl.glGetUniformLocation(this.structuresShader, "color");
		this.camera = gl.glGetUniformLocation(this.structuresShader, "camera");
		this.specularColor = gl.glGetUniformLocation(this.structuresShader, "specularColor");
		this.structuresLightPosition = gl.glGetUniformLocation(this.structuresShader, "lightPosition");
		this.ambientFactor = gl.glGetUniformLocation(this.structuresShader, "ambientFactor");
	}
	
	private void loadSpaceUniforms(GL4 gl) {
		this.spaceModel = gl.glGetUniformLocation(this.spaceShader, "model");
		this.spaceView = gl.glGetUniformLocation(this.spaceShader, "view");
		this.spaceProjection = gl.glGetUniformLocation(this.spaceShader, "projection");
		this.spaceLightPosition = gl.glGetUniformLocation(this.spaceShader, "lightPosition");
		this.windowDimensions = gl.glGetUniformLocation(this.spaceShader, "windowDimensions");
	}
	
	private void loadFXUniforms(GL4 gl) {
		this.colorTexture = gl.glGetUniformLocation(this.fxShader, "colorTexture");
		this.depthTexture = gl.glGetUniformLocation(this.fxShader, "depthTexture");
		this.currentToPreviousViewportMatrix = gl.glGetUniformLocation(this.fxShader, "currentToPreviousViewportMatrix");
		this.fxLightPosition = gl.glGetUniformLocation(this.fxShader, "lightPosition");
		this.exposure = gl.glGetUniformLocation(this.fxShader, "exposure");
		this.pixelSize = gl.glGetUniformLocation(this.fxShader, "pixelSize");
	}
	
	private void initFXFramebuffer(GL4 gl) {
		IntBuffer buffer = IntBuffer.allocate(1);
		gl.glGenFramebuffers(1, buffer);
		this.fxFBO = buffer.get(0);
		
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, this.fxFBO);
		gl.glDrawBuffer(GL4.GL_COLOR_ATTACHMENT0);

		this.fxColorTexture = this.initColorBuffer(gl);
		this.fxDepthTexture = this.initDepthBuffer(gl);
		
		gl.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_COLOR_ATTACHMENT0, this.fxColorTexture, 0);
		gl.glFramebufferTexture(GL4.GL_FRAMEBUFFER, GL4.GL_DEPTH_ATTACHMENT, this.fxDepthTexture, 0);
		
		if(gl.glCheckFramebufferStatus(GL4.GL_FRAMEBUFFER) != GL4.GL_FRAMEBUFFER_COMPLETE) {
			throw new RuntimeException("Failed to initialize the FX framebuffer");
		}
	
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
	}
	
	private int initColorBuffer(GL4 gl) {
		IntBuffer buffer = IntBuffer.allocate(1);
		gl.glGenTextures(1, buffer);
		int colorTextureID = buffer.get(0);
		
		gl.glActiveTexture(GL4.GL_TEXTURE2);
		
		gl.glBindTexture(GL4.GL_TEXTURE_2D, colorTextureID);

		gl.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGB, 128, 128, 0, GL4.GL_RGB, GL4.GL_UNSIGNED_BYTE, null);
		
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_MIRRORED_REPEAT);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_MIRRORED_REPEAT);
		
		gl.glBindTexture(GL4.GL_TEXTURE_2D, 0);
		
		return colorTextureID;
	}
	
	private int initDepthBuffer(GL4 gl) {
		IntBuffer buffer = IntBuffer.allocate(1);
		gl.glGenTextures(1, buffer);
		int depthTextureID = buffer.get(0);
		
		gl.glActiveTexture(GL4.GL_TEXTURE3);

		gl.glBindTexture(GL4.GL_TEXTURE_2D, depthTextureID);
		
		gl.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_DEPTH_COMPONENT32, 128, 128, 0, GL4.GL_DEPTH_COMPONENT, GL4.GL_FLOAT, null);
		
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MIN_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_MAG_FILTER, GL4.GL_LINEAR);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_S, GL4.GL_CLAMP_TO_BORDER);
		gl.glTexParameteri(GL4.GL_TEXTURE_2D, GL4.GL_TEXTURE_WRAP_T, GL4.GL_CLAMP_TO_BORDER);
				
		gl.glBindTexture(GL4.GL_TEXTURE_2D, 0);
		
		return depthTextureID;
	}
	
	private void beginUsingVertexArray(GL4 gl, int arrayID) {
		gl.glBindVertexArray(arrayID);
	}
	
	private void setupTriAttribute(GL4 gl, int attribute, int stride, int disp) {
		gl.glEnableVertexAttribArray(attribute);
		gl.glVertexAttribPointer(attribute, 3, GL4.GL_DOUBLE, false, stride * Double.BYTES, disp * Double.BYTES);
	}
	
	private void initBufferObject(GL4 gl, int bufferID, int bufferType, int param, Function<Integer, Buffer> shapeDataSupplier) {
		Buffer buffer = shapeDataSupplier.apply(param);
		
		gl.glBindBuffer(bufferType, bufferID);
		gl.glBufferData(bufferType, buffer.capacity() * Double.BYTES, buffer.flip(), GL4.GL_STATIC_DRAW);
	}

	private Buffer createStructureShapeData(int layers) {
		DoubleBuffer sphere = DoubleBuffer.allocate(2*layers * layers * 3);
				
		for(double theta = 0.0d; theta < Math.PI + 10E-4; theta += Math.PI / (layers - 1)) {
			for(double phi = 0.0d; phi < 2.0d * Math.PI - 10E-4; phi += Math.PI / layers) {
				sphere.put(Math.sin(theta) * Math.cos(phi));
				sphere.put(Math.cos(theta));
				sphere.put(Math.sin(theta) * Math.sin(phi));
			}
		}
		
		return sphere;
	}
	
	private Buffer createStructureIndicesData(int layers) {
		IntBuffer indices = IntBuffer.allocate(2*layers * layers * 2);
		
		for(int latitudeID = 0; latitudeID < layers; latitudeID++) {
			for(int longitudeID = 0; longitudeID < 2*layers; longitudeID++) {
				indices.put(latitudeID * 2*layers + longitudeID);
				indices.put((latitudeID + 1) * 2*layers + longitudeID);
			}
		}
				
		return indices;
	}
	
	private Buffer createStarsData(int stars) {
		DoubleBuffer data = DoubleBuffer.allocate(6 * stars);
		Random random = new Random();
		
		for(int i = 0; i < stars; i++) {
			float x = random.nextFloat() - 0.5f;
			float y = random.nextFloat() - 0.5f;
			float z = random.nextFloat() - 0.5f;
			
			float factor = 1.0f / (float) Math.sqrt(x * x + y * y + z * z);
			
			data.put(x * factor);
			data.put(y * factor);
			data.put(z * factor);
			
			float brightness = random.nextFloat();
			float red = (1 - brightness) * brightness * random.nextFloat();
			float blue = (1 - brightness) * brightness * random.nextFloat();
			
			data.put(brightness + red);
			data.put(brightness + 0);
			data.put(brightness + blue);
		}
				
		return data;
	}
	
	public void render3D(AppDrawingContext context) {
		assert context instanceof OpenGLContext;
		
		OpenGLContext glContext = (OpenGLContext) context;
		GL4 gl = glContext.getRenderer();
		
		this.loadGeometry();
		this.renderUniverse(gl);
		this.update();
	}
	
	private void loadGeometry() {
		if(this.project != null) {
			try {
				this.mainGeometry.processEventQueue();
							
				this.prepareCodeGeometry();
				
				for(Geometry geometry : this.codeGeometries) {					
					geometry.processEventQueue();
				}
			} catch (GeometryException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void prepareCodeGeometry() {		
		List<CodeGeometry> buffer = new ArrayList<>();
		List<WorldElement> geometry = this.mainGeometry.getElements();
		
		int parentGeneration = this.project.getDataManager().getHostStack().getGeneration() - 1;
		int childrenGeneration = parentGeneration + 2;
		
		float maxDistance = this.mainGeometry.getSize(parentGeneration) * 3.0f;
		float minSize = this.mainGeometry.getSize(childrenGeneration) * 0.99f;
		
		for(WorldElement element : geometry) {
			if(element.getCenter().distance(this.player) < maxDistance 				// Under a certain distance
			&& element.getSize() > minSize  										// Bigger than a certain threshold
			&& this.project.getStorage().getCode().containsKey(element.getID())) {  // Possessing code				
				CodeGeometry code = this.codeGeometries.stream().filter(geom -> geom.getContainer().equals(element)).findAny().orElseGet(() -> this.generateCodeGeometry(element));
				
				buffer.add(code);
				
				if(element.equals(this.current)) {
					this.mainCodeGeometry.updateCodeGeometry(code);
				}
			}
		}
		
		this.codeGeometries = buffer;
	}
	
	private CodeGeometry generateCodeGeometry(WorldElement container) {
		CodeGeometry code = new CodeGeometry(container);
		code.setProject(this.project);
		code.requestReconstruction();
		return code;
	}
	
	private void renderUniverse(GL4 gl) {
		if(this.fxEnabled) {
			gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, this.fxFBO);
		}
		
		gl.glClearBufferfv(GL4.GL_COLOR, 0, this.clearColor);
        gl.glClearBufferfv(GL4.GL_DEPTH, 0, this.clearDepth);

        this.setupSpace(gl);
        this.renderSpace(gl);
		
		if(this.project != null) {
			this.setupStructures(gl);
			this.renderStructures(gl);
		}
		
		if(this.fxEnabled) {
			this.setupFX(gl);
			this.postProcess(gl);
		}
	}
	
	private void setupSpace(GL4 gl) {
		gl.glUseProgram(this.spaceShader);
		
		this.loadMatrix(new Matrix4f().translate(this.player).scale(skyDistance), this.modelData);
		
		gl.glUniformMatrix4fv(this.spaceModel, 1, false, this.modelData);
		gl.glUniformMatrix4fv(this.spaceView, 1, false, this.viewData);
		gl.glUniformMatrix4fv(this.spaceProjection, 1, false, this.projectionData);
		gl.glUniform3f(this.spaceLightPosition, this.sun.x, this.sun.y, this.sun.z);

		if(this.bounds != null) {
			gl.glUniform2i(this.windowDimensions, this.bounds.getWidthAsInt(), this.bounds.getHeightAsInt());
		}
	}
	
	private void renderSpace(GL4 gl) {
		gl.glDepthFunc(GL4.GL_ALWAYS);
		
		gl.glPointSize(1.0f);
		
		gl.glBindVertexArray(this.smallStarsVAO);
		gl.glDrawArrays(GL4.GL_POINTS, 0, smallStars);
		
		gl.glPointSize(2.0f);
		
		gl.glBindVertexArray(this.bigStarsVAO);
		gl.glDrawArrays(GL4.GL_POINTS, 0, bigStars);
	}
	
	private void setupStructures(GL4 gl) {
		gl.glUseProgram(this.structuresShader);
		
		gl.glUniformMatrix4fv(this.structView, 1, false, this.viewData);
		gl.glUniformMatrix4fv(this.structProjection, 1, false, this.projectionData);
		gl.glUniform3f(this.camera, this.player.x, this.player.y, this.player.z);
		gl.glUniform1f(this.ambientFactor, 1.0f);
		gl.glUniform3f(this.specularColor, 1.0f, 1.0f, 1.0f);
		gl.glUniform3f(this.structuresLightPosition, this.sun.x, this.sun.y, this.sun.z);
	}
	
	private void renderStructures(GL4 gl) {
		gl.glEnable(GL4.GL_DEPTH_TEST);
		gl.glEnable(GL4.GL_BLEND);
		
		gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
						
		if(this.scene != RenderingScene.INHERITANCE) {			
			for(Geometry geometry : this.codeGeometries) {
				this.renderStructures0(gl, geometry.getElements(), true);
				this.renderConnections(gl, geometry.getConnections());
			}
		}
				
		this.mainGeometry.sort(this.player);

		this.renderStructures0(gl, this.mainGeometry.getElements(), false);
		this.renderConnections(gl, this.mainGeometry.getConnections());
		
		gl.glDisable(GL4.GL_BLEND);
		gl.glDisable(GL4.GL_DEPTH_TEST);
	}
	
	private void renderStructures0(GL4 gl, List<WorldElement> elements, boolean specialResolve) {		
		Map<String, List<WorldElement>> references = new HashMap<>();
		
		for(WorldElement element : elements) {
			Vector3f unitVector = new Vector3f(0.0f, 1.0f, 0.0f);
			Vector3f delta = new Vector3f(this.player).sub(element.getCenter());
			
			float angle = Geometry.PI + delta.angle(unitVector);
			Vector3f axis = unitVector.cross(delta).normalize();
							
			this.loadMatrix(new Matrix4f().translate(element.getCenter()).rotate(angle, axis).scale(element.getSize()), this.modelData); // For sorting
			gl.glUniformMatrix4fv(this.structModel, 1, false, this.modelData);
			
			if(element != this.selected) {
				gl.glUniform1f(this.ambientFactor, 0.5f);
			} else {
				gl.glUniform1f(this.ambientFactor, 1.0f);
			}
			
			Vector4f color = element.getColor();
			Vector3f spot = element.getSpotColor();
							
			if(this.inside.contains(element)) {
				gl.glUniform4f(this.color, color.x, color.y, color.z, color.w / 2);
			} else {
				gl.glUniform4f(this.color, color.x, color.y, color.z, color.w);
			}
			
			gl.glUniform3f(this.specularColor, spot.x, spot.y, spot.z);
			
			double viewHeight = 2.0d * Math.atan(fov / 2.0d) * this.player.distance(element.getCenter());
			int quality = (int) (this.structures.size() * Math.min(1.0f - 10E-5f, 2 * element.getSize() / viewHeight));
			
			ScaledRenderingInfo info = this.structures.get(quality);
			
			gl.glBindVertexArray(info.getVAO());
			gl.glBindBuffer(GL4.GL_ELEMENT_ARRAY_BUFFER, info.getEBO());
			gl.glDrawElements(GL4.GL_TRIANGLE_STRIP, 4 * info.getLayers() * (info.getLayers() - 1), GL4.GL_UNSIGNED_INT, 0);
			
			/* X-Resolve: Dynamic references rendering */
			String component = element.getDescription();

			if(specialResolve && component != null) {					
				Vector3f first = element.getCenter();
									
				this.loadMatrix(new Matrix4f(), this.modelData);
				gl.glUniformMatrix4fv(this.structModel, 1, false, this.modelData);
				gl.glDisable(GL4.GL_DEPTH_TEST);
				gl.glUniform4f(this.color, 1.0f, 1.0f, 1.0f, 1.0f);
				gl.glBindVertexArray(this.connectionVAO);
									
				try {
					int structRef = Integer.parseInt(component);
					WorldElement second = this.mainGeometry.getElementByID(structRef);
					
					Vector4f connectionColor = second.getColor();
					
					gl.glUniform4f(this.color, connectionColor.x, connectionColor.y, connectionColor.z, connectionColor.w);
					
					if(this.selected != element && this.selected != second) {
						gl.glUniform1f(this.ambientFactor, 0.5f);
					} else {
						gl.glUniform1f(this.ambientFactor, 1.0f);
					}
					
					this.connect(gl, first, second.getCenter());
				} catch(NumberFormatException e) {
					if(component.startsWith(SpecificationElement.VAR)) {
						/* Resolved to a variable reference */
						
						List<WorldElement> list = references.get(component);
						
						Random colorGenerator = new Random(component.hashCode());
						
						/* High contrast colors */
						int red = 0x80 + colorGenerator.nextInt(0x80);
						int green = 0x80 + colorGenerator.nextInt(0x80);
						int blue = 0x80 + colorGenerator.nextInt(0x80);

						Vector4f connectionColor = new Vector4f(red / 255.0f, green / 255.0f, blue / 255.0f, 1.0f);
						
						gl.glUniform4f(this.color, connectionColor.x, connectionColor.y, connectionColor.z, connectionColor.w);
						
						if(list != null) {
							for(WorldElement second : list) {
								if(this.selected != null && this.selected.getDescription() != null && this.selected.getDescription().contentEquals(component)) {
									gl.glUniform1f(this.ambientFactor, 1.0f);
								} else {
									gl.glUniform1f(this.ambientFactor, 0.0f);
								}
								
								this.connect(gl, first, second.getCenter());
							}
						} else {
							list = new ArrayList<>();
							references.put(component, list);
						}
						
						list.add(element);
					}
				}
				
				gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
				gl.glEnable(GL4.GL_DEPTH_TEST);
			}
		}
	}
	
	private void renderConnections(GL4 gl, List<Connection> connections) {			
		this.loadMatrix(new Matrix4f(), this.modelData);
		gl.glUniformMatrix4fv(this.structModel, 1, false, this.modelData);
		gl.glDepthFunc(GL4.GL_LEQUAL);
		gl.glUniform4f(this.color, 1.0f, 1.0f, 1.0f, 1.0f);
		gl.glUniform1f(this.ambientFactor, 1.0f);
		gl.glBindVertexArray(this.connectionVAO);

		for(Connection connection : connections) {
			WorldElement first = connection.getFirstElement();
			WorldElement second = connection.getSecondElement();
			
			this.connect(gl, first.getCenter(), second.getCenter());
		}
	}
	
	private void connect(GL4 gl, Vector3f first, Vector3f second) {
		float[] unwrapped = new float[] {first.x, first.y, first.z, second.x, second.y, second.z};
		
		FloatBuffer data = FloatBuffer.wrap(unwrapped);
						
		gl.glBindBuffer(GL4.GL_ARRAY_BUFFER, this.connectionVBO);
		gl.glBufferSubData(GL4.GL_ARRAY_BUFFER, 0, 6 * Float.BYTES, data);
		gl.glDrawArrays(GL4.GL_LINES, 0, 2);
	}
	
	private void setupFX(GL4 gl) {
		gl.glUseProgram(this.fxShader);
		
		/* Load sun position in screen coords */
		
		if(this.pitch > 0.0f) {
			Vector2f point = this.getHVCFrom3D(new Vector3f(this.sun).add(this.player.x, 0, this.player.z), this.projectionMatrix);
			gl.glUniform2f(this.fxLightPosition, (float) point.x + 0.5f, (float) point.y + 0.5f);
		} else {
			gl.glUniform2f(this.fxLightPosition, -123.0f, -123.0f);
		}
		
		/* Setup FX uniforms */
		
		gl.glActiveTexture(GL4.GL_TEXTURE2);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, this.fxColorTexture);
		gl.glUniform1i(this.colorTexture, 2);
		
		gl.glActiveTexture(GL4.GL_TEXTURE3);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, this.fxDepthTexture);
		gl.glUniform1i(this.depthTexture, 3);
		
		gl.glUniform1f(this.exposure, 0.001f);
		
		if(this.bounds != null) {
			gl.glUniform2f(this.pixelSize, 1 / this.bounds.getWidth(), 1 / this.bounds.getHeight());
		}
		
		this.loadMatrix(this.previousViewProjectionMatrix.mul(new Matrix4f(this.projectionMatrix).mul(this.viewMatrix).invert()), this.currentToPreviousViewportData);
		gl.glUniformMatrix4fv(this.currentToPreviousViewportMatrix, 1, false, this.currentToPreviousViewportData);
		
		this.previousViewProjectionMatrix = new Matrix4f(this.projectionMatrix).mul(this.viewMatrix); // Do not trash the projection matrix: it will be reused!
	}
	
	private void postProcess(GL4 gl) {		
		gl.glBindFramebuffer(GL4.GL_FRAMEBUFFER, 0);
		
		gl.glBindVertexArray(this.fxVAO);
		gl.glDrawArrays(GL4.GL_TRIANGLE_STRIP, 0, 4);
	}
	
	public void render2D(AppDrawingContext context) {
		if(this.project != null) {
			this.renderLabels(context, this.mainGeometry.getElements());
			
			for(Geometry geometry : this.codeGeometries) {
				this.renderLabels(context, geometry.getElements());
			}
		}
	}
	
	private void renderLabels(AppDrawingContext context, List<WorldElement> elements) {
		synchronized(this.inside) {
			// Construct a new perspective with a null near plane (I don't know why it has to be null ?!)
			Matrix4f proj = new Matrix4f().perspective(fov, this.bounds.getWidth() / this.bounds.getHeight(), 0.0f, 1.0f);

			for(WorldElement element : elements) {
				/* Context initialization */
				
				int alpha = 0xFF;
				
				if(!this.inside.isEmpty()) {
					WorldElement enclosing = this.inside.get(0);
					
					if(enclosing == element) {
						continue;
					}
					
					if(element.getCenter().distance(enclosing.getCenter()) > 2 * enclosing.getSize()) {
						alpha = 0x1F;
					}
				}
				
				GL4 gl = ((OpenGLContext) context).getRenderer();
				
				/* World ==> Screen-space calculation */
				Vector3f mainSpaceAnchor = element.getCenter().add(0.0f, 2.0f * element.getSize(), 0.0f);
				Vector3f subSpaceAnchor = element.getCenter().sub(0.0f, element.getSize(), 0.0f);

				boolean renderMain = this.checkRenderability(mainSpaceAnchor);
				boolean renderSub = this.checkRenderability(subSpaceAnchor);
				
				float height = -1.0f;
				
				if(renderMain && element.getName() != null) {
					Vector2f screenAnchor = this.getHVCFrom3D(mainSpaceAnchor, proj).mul(1.0f, -1.0f).add(0.75f, 1.0f);
					height = 1.0f - this.getHVCFrom3D(element.getCenter().add(0.0f, element.getSize(), 0.0f), proj).mul(1.0f).add(screenAnchor).y;
					
					this.renderLabel(context, element.getName(), 0xFFFFFF, alpha, screenAnchor, height);
				}
								
				if(renderSub && element.getDescription() != null && !element.getDescription().contentEquals("?")) {
					String description = element.getDescription().replace(SpecificationElement.VAR, "");
										
					Vector2f screenAnchor = this.getHVCFrom3D(subSpaceAnchor, proj).mul(1.0f, -1.0f).add(0.75f, 1.0f);

					if(height < 0.0f) { // Avoid double calculation (considering both heights approximately the same: lim(dist(player, object) -> infty) {dh -> 0})
						height = 1.0f - this.getHVCFrom3D(element.getCenter().add(0.0f, element.getSize(), 0.0f), proj).mul(1.0f).add(screenAnchor).y;
					}
					
					this.renderLabel(context, description, 0x888888, alpha, screenAnchor, height);
				}
				
				gl.glBlendFunc(GL4.GL_SRC_ALPHA, GL4.GL_ONE_MINUS_SRC_ALPHA);
			}
		}
	}
	
	private void renderLabel(AppDrawingContext context, String label, int color, int alpha, Vector2f screenAnchor, float height) {
		if(height > 0.01d) {
			Vector2d dimensions = new Vector2d(0.5d, height);

			if(screenAnchor.x + dimensions.x > 0  && screenAnchor.y + dimensions.y > 0 && screenAnchor.x < 2.0 && screenAnchor.y < 2.0) {
				Text text = PrimitiveFactory.instance().newText(label, color, alpha);
				text.updateBounds(new Bounds((float) screenAnchor.x, (float) screenAnchor.y, (float) dimensions.x, (float) dimensions.y));
				text.prepare(); // Although updating the bounds already toggles the "reprepare" bit, this may be useful for further implementations...
				context.getRenderingSystem().renderDirect(text);
			}
		}
	}
	
	private boolean checkRenderability(Vector3f object) {
		return new Vector3f(object).sub(this.player).normalize().angle(this.getDOF()) < Geometry.PI / 2;
	}
	
	private Vector2f getHVCFrom3D(Vector3f input, Matrix4f projection) { // HVC stands for Homogeneous vector coordinates
		Vector4f point = new Matrix4f(projection).mul(this.viewMatrix).transform(new Vector4f(input, 1.0f));
		return new Vector2f(point.x / point.z, point.y / point.z);
	}

	private void update() {
		this.processTeleportation();
		
		this.updatePlayer();
		this.updateCamera();

		this.detectCollisions();
		this.detectSightFocus();
		
		this.updateMenu();
		
		this.dispatcher.fire(new MessageEvent(this.player + " | Looking at " + this.getElementName(this.lookingAt) + " (" + (this.lookingAt != null ? this.lookingAt.getID() : -1) + ")", MessageType.DEBUG));
	}
	
	private void updatePlayer() {
		long deltaTime = System.nanoTime() - this.lastPositionUpdate;
		this.lastPositionUpdate += deltaTime;
		
		Vector3d acceleration = new Vector3d(this.acceleration).mul(deltaTime * timeResolution);
		
		this.velocity.x += -Math.cos(Math.PI / 2 + this.yaw) * acceleration.x - Math.cos(this.yaw) * acceleration.z;
		this.velocity.y += acceleration.y;
		this.velocity.z += -Math.sin(Math.PI / 2 + this.yaw) * acceleration.x - Math.sin(this.yaw) * acceleration.z;
		
		this.velocity.mul(1.0f - spaceFriction * deltaTime * timeResolution);
	
		this.player.add(new Vector3f(this.velocity).mul(deltaTime * timeResolution));
	}
	
	private void updateCamera() {
		this.loadMatrix(this.viewMatrix.identity()
				.rotate((float) -this.pitch, 1.0f, 0.0f, 0.0f)
				.rotate((float) this.yaw, 0.0f, 1.0f, 0.0f)
				.translate(-this.player.x, -this.player.y, -this.player.z), this.viewData);
		
		if(this.project != null && this.scene == RenderingScene.HIERARCHY) {
			CameraInfo info = new CameraInfo(this.player.x, this.player.y, this.player.z, (float) this.yaw, (float) this.pitch);
			this.project.setProperty("player", info, Coder.cameraEncoder);
		}
	}
	
	private void updatePerspective() {
		if(this.bounds != null) {
			this.loadMatrix(this.projectionMatrix.identity().perspective(fov, this.bounds.getWidth() / this.bounds.getHeight(), this.zNear, this.zFar), this.projectionData);
		}
	}
		
	private void ajustAcceleration() {
		if(this.scene != RenderingScene.INHERITANCE) {
			this.generalAcceleration = initialAcceleration * Math.max(10E-10f, this.mainGeometry.getRelativeSize(this.inside.size()));
		} else {
			this.generalAcceleration = initialAcceleration;
		}
		
		this.zNear = this.generalAcceleration;

		this.updatePerspective();
	}
	
	// Requires the geometries to be sorted
	private void detectCollisions() {
		List<WorldElement> worldElements = this.mainGeometry.getCollisions(this.player);
		worldElements.addAll(this.mainCodeGeometry.getCollisions(this.player));
			
		Collections.reverse(worldElements); // From furtherest to nearest
		
		synchronized(this.inside) {
			for(WorldElement element : worldElements) {
				if(!this.inside.remove(element)) {
					if(this.enterElement(element)) {
						this.buffer.add(element);
					}
				} else {
					this.buffer.add(element);
				}
			}

			this.inside.stream().filter(((Predicate<WorldElement>) this::exitElement).negate()).forEach(this.buffer::add);
			
			this.buffer.sort((a, b) -> Float.compare(Geometry.distance(this.player, b), Geometry.distance(this.player, a)));
			
			if(!this.buffer.isEmpty()) {
				this.current = this.buffer.get(0);
			}
			
			this.inside.clear();
			this.inside.addAll(this.buffer);
			this.buffer.clear();
		}
	}
	
	private boolean enterElement(WorldElement element) {
		if(element.isAccessAllowed()) {
			this.current = element;
			this.needMenuUpdate = true;
			this.project.getDataManager().getHostStack().push(element.getID());
			this.prepareCodeGeometry();
			return true;
		} else {
			this.repulseFrom(element.getCenter());
			return false;
		}
	}
	
	private boolean exitElement(WorldElement element) {
		this.needMenuUpdate = true;
		this.project.getDataManager().getHostStack().pop();
		this.prepareCodeGeometry();
		return true;
	}
	
 	private void repulseFrom(Vector3f position) {
		Vector3f normal = new Vector3f(this.player).sub(position).normalize();
		
		if(new Vector3f(this.velocity).normalize().dot(normal) < 0.0d) {
			this.velocity.reflect(normal).normalize(this.generalAcceleration * 32.0f);
		}
 	}
	
	private void detectSightFocus() {
		Vector3f cameraDirection = this.getDOF();
		float size = this.mainGeometry.getSize(this.inside.size());
		
		WorldElement found = null;
		float distance = Float.MAX_VALUE;
		
		for(WorldElement element : this.mainGeometry.getElements()) {
			boolean isInsideSameStruct = this.current == null || this.current.getCenter().distance(element.getCenter()) < this.mainGeometry.getSize(this.inside.size() - 1);
			boolean isSameSize = Math.abs(element.getSize() - size) < Math.ulp(size);
						
			if(isSameSize && isInsideSameStruct) {
				float currentDistance = this.player.distance(element.getCenter());
				
				if(currentDistance < distance) {
					if(element.collidesWith(new Vector3f(cameraDirection).normalize(currentDistance).add(this.player))) {
						found = element;
						distance = currentDistance;
					}
				}
			}
		}
				
		for(WorldElement element : this.mainCodeGeometry.getElements()) {
			float currentDistance = this.player.distance(element.getCenter());
			if(currentDistance < distance) {
				if(element.collidesWith(new Vector3f(cameraDirection).normalize(currentDistance).add(this.player))) {
					found = element;
					distance = currentDistance;
				}
			}
		}

		this.lookingAt = found;
	}
	
	private Vector3f getDOF() {
		return new Vector3f(-this.viewData.get(2), -this.viewData.get(6), -this.viewData.get(10));
	}
	
	private void updateMenu() {
		if(this.needMenuUpdate) {
			StructureList menu = MainMenus.getStructureList();
			
			menu.setMenuCursor(0);
			
			this.dispatcher.fire(new MenuEvent(menu));
			this.ajustAcceleration();
			
			this.selected = null;
			this.needMenuUpdate = false;
		}
	}
	
	private String getElementName(WorldElement element) {
		if(element != null) {
			if(element.getName() == null || element.getName().isEmpty()) {
				return "a mysterious structure";
			} else {
				return "'" + element.getName() + "'";
			}
		} else {
			return "the space";
		}
	}
	
	/* Hack */
	private void loadMatrix(Matrix4f matrix, FloatBuffer modelData) {
		modelData.put(0, matrix.m00());
		modelData.put(1, matrix.m01());
		modelData.put(2, matrix.m02());
		modelData.put(3, matrix.m03());
		modelData.put(4, matrix.m10());
		modelData.put(5, matrix.m11());
		modelData.put(6, matrix.m12());
		modelData.put(7, matrix.m13());
		modelData.put(8, matrix.m20());
		modelData.put(9, matrix.m21());
		modelData.put(10, matrix.m22());
		modelData.put(11, matrix.m23());
		modelData.put(12, matrix.m30());
		modelData.put(13, matrix.m31());
		modelData.put(14, matrix.m32());
		modelData.put(15, matrix.m33());
	}

 	public void teleport(TeleportInfo info) {
 		this.teleport = info;
 		info.updateLifeTime(500L);
 	}
 	
	private void processTeleportation() {
		if(this.teleport != null && this.teleport.isAlive()) {
			WorldElement teleportElement = this.mainGeometry.getElementByID(this.teleport.getDestination());
			WorldElement lookAtElement = this.mainCodeGeometry.getElementByID(this.teleport.getFocus());
	
			if(teleportElement != null) {
				this.processTeleportation(teleportElement.getCenter().add(0.0f, teleportElement.getSize() * 0.8f, 0.0f));
				this.teleport.updateDestination(-1);
			}

			if(lookAtElement != null) {
				int instructionID = -1;
						
				List<? extends HierarchyElement> code = this.project.getDataManager().getCodeManager().getCurrentCode();
				
				for(int i = 0; i < code.size(); i++) {
					if(code.get(i).getID() == this.teleport.getFocus()) {
						instructionID = i;
					}
				}
							
				if(instructionID >= 0) {
					CodeEditor menu = MainMenus.getCodeEditor();
	
					menu.setTargetInstruction(instructionID);
					
					menu.show();
					menu.select(instructionID);
				}
					
				Vector3f lookAtVector = lookAtElement.getCenter().sub(this.player).normalize();
				
				this.yaw = Geometry.PI - (float) Math.atan2(lookAtVector.x, lookAtVector.z);
				this.pitch = (float) Math.asin(lookAtVector.y);
				
				this.selected = lookAtElement;
				
				this.teleport.updateFocus(-1);
			}
		}
	}

	private void processTeleportation(Vector3f position) {
		this.updatePerspective();
		this.player.set(position);
		
		this.detectCollisions();
		this.ajustAcceleration();
	}
	
	public void setScene(RenderingScene scene) {
		this.scene = scene;
		
		synchronized(this.inside) {
			this.inside.clear();
		}
		
		this.codeGeometries.clear();
		
		long seed = project.getProperty("seed", Coder.integerDecoder);
		
		this.mainGeometry.updateSeed(seed);
		
		this.mainCodeGeometry.setProject(project);
		this.mainGeometry.setProject(project);
		
		this.mainGeometry.requestReconstruction();
		
		if(scene != RenderingScene.HIERARCHY) {
			this.processTeleportation(new Vector3f(0.0f, 0.0f, 5.0f));
			
			this.yaw = 0.0f;
			this.pitch = 0.0f;
		} else {
			CameraInfo info = this.project.getProperty("player", Coder.cameraDecoder);
			
			this.processTeleportation(new Vector3f(info.getX(), info.getY(), info.getZ()));
			
			this.yaw = info.getYaw();
			this.pitch = info.getPitch();
		}

		this.ajustAcceleration();
	}
	
	public void selectInstruction(int id) {		
		this.selected = this.mainCodeGeometry.getElementByID(id);
	}

	public void loadProject(Project project) {
		this.project = project;

		if(project != null) {
			this.needMenuUpdate = this.selected == null;			
			this.setScene(RenderingScene.HIERARCHY);
		} else {
			this.isInWorld = false;
			this.context.setCursorVisible(true);
		}
	}

	public void update(Bounds bounds) {
		this.bounds = bounds;

		this.updatePerspective();
		
		GL4 gl = (GL4) GLContext.getCurrentGL();
		
		/*
		 * Resize buffers
		 */
		gl.glActiveTexture(GL4.GL_TEXTURE2);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, this.fxColorTexture);
		gl.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_RGB, bounds.getWidthAsInt(), bounds.getHeightAsInt(), 0, GL4.GL_RGB, GL4.GL_UNSIGNED_BYTE, null);
		
		gl.glActiveTexture(GL4.GL_TEXTURE3);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, this.fxDepthTexture);
		gl.glTexImage2D(GL4.GL_TEXTURE_2D, 0, GL4.GL_DEPTH_COMPONENT32, bounds.getWidthAsInt(), bounds.getHeightAsInt(), 0, GL4.GL_DEPTH_COMPONENT, GL4.GL_FLOAT, null);
		gl.glBindTexture(GL4.GL_TEXTURE_2D, 0);
	}

	public <T extends Event> void handleEvent(T event) {
		if(this.project == null) {
			return;
		}
		
		if(event instanceof MoveEvent) {
			if(this.isInWorld) {
				Point position = ((MoveEvent) event).getPoint();
				this.updateMouse(position);
				event.abortDispatching();
			}
		} else if(event instanceof PressureEvent) {
			PressureEvent pressure = (PressureEvent) event;

			if(this.isInWorld || pressure.getKeycode() == worldToggle) {
				switch(pressure.getKeycode()) {
					case forward:
						this.acceleration.x = (pressure.isDown() ? this.generalAcceleration : 0.0f);
						break;
					case backward:
						this.acceleration.x = (pressure.isDown() ? -this.generalAcceleration : 0.0f);
						break;
					case left:
						this.acceleration.z = (pressure.isDown() ? this.generalAcceleration : 0.0f);
						break;
					case right:
						this.acceleration.z = (pressure.isDown() ? -this.generalAcceleration : 0.0f);
						break;
					case up:
						this.acceleration.y = (pressure.isDown() ? this.generalAcceleration : 0.0f);
						break;
					case down:
						this.acceleration.y = (pressure.isDown() ? -this.generalAcceleration : 0.0f);
						break;
					case worldToggle:
						if(pressure.isDown()) {
							this.isInWorld = !this.isInWorld;
							
							this.acceleration.set(0.0f, 0.0f, 0.0f);
							
							if(this.isInWorld) {
								this.context.setCursorVisible(false);
							} else {
								this.context.setCursorVisible(true);
							}
						}
	
						break;
					case spawnKey:
						if(this.isControlDown && pressure.isDown()) {
							this.processTeleportation(new Vector3f(0.0f, 0.0f, 0.0f));
						}
						
						break;
					case KeyEvent.VK_CONTROL:
						this.isControlDown = pressure.isDown();
						break;
				}
			}
		} else if(event instanceof WheelEvent) {
			if(this.isControlDown) {
				WheelEvent wheel = (WheelEvent) event;
				this.updateAcceleration(wheel.getDelta());
				wheel.abortDispatching();
			}
		} else if(event instanceof ActionEvent) {
			ActionEvent action = (ActionEvent) event;
			
			if(this.isInWorld && action.getType() == ActionType.CLICK) {
				if(action.isButton(ActionEvent.BUTTON_LEFT)) {
					this.onLeftClick();
				} else if(action.isButton(ActionEvent.BUTTON_RIGHT)) {
					this.onRightClick();
				}
				
				action.abortDispatching();
			}
		}
	}
	
	private void updateMouse(Point position) {
		this.yaw += (position.getX() - 1.0f) * 0.1f;
		this.pitch -= (position.getY() - 1.0f) * 0.1f;
		
		float halfPI = Geometry.PI / 2.0f;
		
		if(this.pitch > halfPI) {
			this.pitch = halfPI;
		} else if(this.pitch < -halfPI) {
			this.pitch = -halfPI;
		}
		
		this.yaw %= 4.0f * halfPI;
		
		this.context.moveCursor(this.bounds.getWidthAsInt() / 2, this.bounds.getHeightAsInt() / 2);
	}
	
	private void updateAcceleration(double wheelDelta) {
		this.generalAcceleration = this.generalAcceleration * (float) Math.pow(1.01f, 2 * wheelDelta);
		
		this.zNear = this.generalAcceleration;

		this.updatePerspective();
	}
	
	private void onLeftClick() {
		if(this.lookingAt != null) {
			List<WorldElement> elements = this.mainCodeGeometry.getElements();
			
			if(elements.contains(this.lookingAt) && (this.lookingAt.getID() & 0xFF000000) != 0) {
				if(this.isFastBindingMode) {
					MainMenus.getStructureList().show();
					
					Map<Integer, StructureMeta> meta = this.project.getStorage().getStructureMeta();
					
					StructureMeta sourceMeta = meta.get(this.selected.getID() & 0xFFFFFF);
					StructureMeta targetMeta = meta.get(this.lookingAt.getID() & 0xFFFFFF);
					
					if(sourceMeta != null && targetMeta != null) {
						SpecificationElement sourceParam = sourceMeta.getSpecification().getElements().get((this.selected.getID() >>> 24) - 1);
						SpecificationElement targetParam = targetMeta.getSpecification().getElements().get((this.lookingAt.getID() >>> 24) - 1);

						this.dispatcher.fire(this.project.getDataManager().getSpecificationManager().bind(sourceParam, targetParam));
						
						this.mainCodeGeometry.requestReconstruction();
					} else {
						this.dispatcher.fire(new MessageEvent("A problem occured during fast binding", MessageType.ERROR));
					}
				} else {
					this.selected = this.lookingAt;
					this.dispatcher.fire(new MessageEvent("Fast binding: left-click on the target", MessageType.SUCCESS));	
				}
				
				this.isFastBindingMode = !this.isFastBindingMode;
				
				return;
			}
		} else {
			this.isFastBindingMode = false;
		}
		
		this.dispatcher.fire(new ClickEvent(null, "menuScroll"));
	}
	
	private void onRightClick() {
		if(MainMenus.getStructureEditor().getColoring().isActive()) {
			/* Validate choice */
			this.dispatcher.fire(new ClickEvent(null, "menuScroll"));
		}

		if(this.selected != this.lookingAt) {
			this.selected = this.lookingAt; // Note that lookingAt can be null.
		} else {
			this.selected = null;
		}
		
		if(this.selected != null) {
			List<WorldElement> elements = this.mainCodeGeometry.getElements();
			
			if(elements.contains(this.selected)) {
				int id = this.selected.getID();
				
				if((id & 0xFF000000) == 0) {
					int index = elements.indexOf(this.selected);
					
					if(index >= 0) {
						CodeEditor menu = MainMenus.getCodeEditor();
						menu.setTargetInstruction(index);
						menu.show();
					}
				} else {
					int instructionID = this.selected.getID() & 0xFFFFFF;
					int paramID = (this.selected.getID() >>> 24) - 1;
					
					StructureMeta instructionMeta = this.project.getStorage().getStructureMeta().get(instructionID);
			
					if(instructionMeta != null) {							
						SpecificationElement spec = instructionMeta.getSpecification().getElements().get(paramID);
					
						if(spec != null) {
							if(spec instanceof Data) {
								TypeEditor menu = MainMenus.getTypeEditor();
								menu.setTarget((Data) spec);
								menu.show();
							} else if(spec instanceof Reference) {
								ReferenceEditor menu = MainMenus.getReferenceEditor();
								menu.setTarget((Reference) spec);
								menu.show();
							} else {
								throw new RuntimeException("Strange object found");
							}		
						}
					}
				}
			} else {				
				MainMenus.getStructureEditor().setCurrent(this.selected);
				
				this.dispatcher.fire(new MessageEvent(this.getElementName(this.selected) + " is selected", MessageType.INFO));
				this.dispatcher.fire(new MenuEvent(MainMenus.getStructureEditor()));
			}
		} else {
			this.dispatcher.fire(new MenuEvent(MainMenus.getStructureList()));
		}
	}
	
	public Geometry getStructuresGeometry() {
		return this.mainGeometry;
	}
	
	public Geometry getCodeGeometry() {
		return this.mainCodeGeometry;
	}
	
	public List<Class<? extends Event>> getHandleableEvents() {
		return Arrays.asList(MoveEvent.class, PressureEvent.class, WheelEvent.class, ActionEvent.class);
	}
}