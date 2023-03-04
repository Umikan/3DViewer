package viewer.model;

import com.jogamp.opengl.GL4;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.nativewindow.WindowClosingProtocol.WindowClosingMode;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;

import viewer.model.common.MVP;
import viewer.model.gltf.GLTF;
import viewer.model.gltf.GLTFScene;
import viewer.model.gltf.JSON;
import viewer.shader.Memory;
import viewer.shader.Shader;
import viewer.system.Parameter;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.jogamp.opengl.GL4.*;
import static viewer.system.Engine.*;

import java.awt.FileDialog;
import java.awt.Frame;

public class GLTFViewer implements GLEventListener, MouseListener, KeyListener {

	private static String path = null;

	public static void selectFile() {
		FileDialog dialog = new FileDialog((Frame) null, "Choose your GLTF File", FileDialog.LOAD);

		dialog.setVisible(true);
		path = dialog.getDirectory() + dialog.getFile();
	}

	public static void main(String[] args) {
		selectFile();
		new GLTFViewer();
	}

	private final FPSAnimator animator;
	private final GLWindow glWindow;
	private static final char KEY_ESC = 0x1b;

	public GLTFViewer() {
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));

		glWindow = GLWindow.create(caps);
		glWindow.setTitle("GLTF Viewer");
		glWindow.setSize(720, 720);
		glWindow.addGLEventListener(this);

		glWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDestroyed(WindowEvent evt) {
				System.exit(0);
			}
		});
		glWindow.setDefaultCloseOperation(WindowClosingMode.DISPOSE_ON_CLOSE);
		glWindow.addMouseListener(this);
		glWindow.addKeyListener(this);
		animator = new FPSAnimator(60);
		animator.add(glWindow);
		animator.start();
		glWindow.setVisible(true);
	}

	GLTFScene duck;


	int shaderprogram;
	int uniformLight;
	@Override
	public void init(GLAutoDrawable drawable) {
		GLTF gltf = JSON.deserialize(path, GLTF.class);
		duck = new GLTFScene(JSON.getParentDirectory(path), gltf);
		GL4 gl = drawable.getGL().getGL4();
		duck.init(gl);
		gl.glClearColor(0f, 1.0f, 0f, 1.0f);
		String v = "/shader/GLTF.vs";
		String f = "/shader/GLTF.fs";
		shaderprogram = Shader.loadShaderProgram(gl, v, f);
		gl.glUseProgram(shaderprogram);

		MVP.setUniformLocation(gl, shaderprogram, "MVP", "M", "V");
		uniformLight = gl.glGetUniformLocation(shaderprogram, "LightPosition_worldspace");
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	double time = 0.0d;
	private void updateTime(){
		Time.update();
		time += Time.deltaTime;
		Parameter.t = (float)Math.abs(Math.sin(time));
	}
	@Override
	public void display(GLAutoDrawable drawable) {
		updateTime();
		walkThrough();
		GL4 gl = drawable.getGL().getGL4();
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LESS);

		
		//gl.glEnable(GL_CULL_FACE);
		gl.glCullFace(GL_BACK);

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		Matrix4f transform = new Matrix4f();
		duck.setWorldTransform(transform);

		Vector3f eye = getEyePosition();
		Vector3f center = transform.transformPosition(new Vector3f(0.0f, height, 0.0f));
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Matrix4f V = new Matrix4f().lookAt(eye, center, up); 
		Matrix4f P = new Matrix4f().perspective((float) Math.toRadians(45.0f), 1.0f, 0.1f, 5000.0f);
		//"znear" must be much bigger than 0.01.

		MVP.setVP(V, P);

		Vector3f lightPos = eye; 
		lightPos.get(Memory.vec3);
		gl.glUniform3fv(uniformLight, 1, Memory.vec3);

		gl.glUniform1i(gl.glGetUniformLocation(shaderprogram, "tex"), 0);
		
		float t = (float)time % 5.0f;
		//GLTFCamera.tryToSetFirstCamera();
		duck.setAnimation(gl, t, 0);
		duck.render(gl);

		//Releasing direct buffers
		//if (count++ %300 == 0)System.gc();
	}
	int count = 0;

	@Override
	public void dispose(GLAutoDrawable drawable) {
		if(animator != null) animator.stop();
	}
	
	private Vector3f getEyePosition(){
		double x = r * Math.sin(angleV * 0.5d);
		double y = height;
		double z = -r * Math.cos(angleV * 0.5d);
		return new Vector3f((float)x, (float)y, (float)z);
	}
	private float height, angleV;
	private double r = 10.0d;
	private static final float move = 0.1f;
	private void walkThrough(){
		if (arrowFlag[0]) height += move * r / 10.0d;
		if (arrowFlag[1]) height -= move * r / 10.0d;
		if (arrowFlag[2]) angleV -= move;
		if (arrowFlag[3]) angleV += move;
		r += scaleDirection * 0.25f;
	}

	private boolean[] arrowFlag = new boolean[4];
	private int scaleDirection = 0;
	private static final int[] arrows = 
		{KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT};
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		for (int i = 0; i < arrows.length; i++){
			if (code == arrows[i]) arrowFlag[i] = true;
		}
		char c  = e.getKeyChar();
		if (c == 'w') scaleDirection = 1;
		if (c == 's') scaleDirection = -1;
		if (c == 'e') scaleDirection = 50;
		if (c == 'd') scaleDirection = -50;
	}
	
	@Override
	public void keyReleased(KeyEvent e) {
		int code = e.getKeyCode();
		for (int i = 0; i < arrows.length; i++){
			if (code == arrows[i]) arrowFlag[i] = false;
		}
		char c  = e.getKeyChar();
		if (c == 'w') scaleDirection = 0;
		if (c == 's') scaleDirection = 0;
		if (c == 'e') scaleDirection = 0;
		if (c == 'd') scaleDirection = 0;

		char keyChar = e.getKeyChar();
		if(keyChar == KEY_ESC || keyChar == 'q' || keyChar == 'Q') {
			glWindow.destroy();
		}
	}
	
	@Override
	public void mouseClicked(MouseEvent e) { }
	
	@Override
	public void mouseEntered(MouseEvent e) { }
	
	@Override
	public void mouseExited(MouseEvent e) {}
	
	@Override
	public void mousePressed(MouseEvent e) {
		animator.resume();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		//animator.pause();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) { }
	
	@Override
	public void mouseDragged(MouseEvent e) {}
	
	@Override
	public void mouseWheelMoved(MouseEvent e) {}
}