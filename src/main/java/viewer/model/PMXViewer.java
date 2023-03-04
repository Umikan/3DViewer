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
import viewer.model.pmx.PMX;
import viewer.model.vmd.VMD;
import viewer.shader.Memory;
import viewer.shader.Shader;
import viewer.system.Debug;
import viewer.system.Parameter;
import viewer.system.Engine.Time;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static com.jogamp.opengl.GL4.*;

import java.awt.FileDialog;
import java.awt.Frame;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class PMXViewer implements GLEventListener, MouseListener, KeyListener {

	private static String path = null;
	private static String path2 = null;

	public static String selectFile(String title) {
		FileDialog dialog = new FileDialog((Frame) null, title, FileDialog.LOAD);

		dialog.setVisible(true);
		return dialog.getDirectory() + dialog.getFile();
	}

	public static void main(String[] args) {
		path = selectFile("Choose your PMX file");
		path2 = selectFile("Choose your VMD file");
		new PMXViewer();
	}

	private final FPSAnimator animator;
	private final GLWindow glWindow;
	private static final char KEY_ESC = 0x1b;

	public PMXViewer() {
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));

		glWindow = GLWindow.create(caps);
		glWindow.setTitle("PMX Viewer");
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

	PMX pmx;
	VMD vmd;
	int shaderprogram;
	int shaderprogram2;
	int uniformLight;
	@Override
	public void init(GLAutoDrawable drawable) {
		pmx = new PMX(path);
		vmd = new VMD(path2);
		pmx.attachVMD(vmd);
		GL4 gl = drawable.getGL().getGL4();
		gl.glClearColor(0f, 1.0f, 0f, 1.0f);
		String v = "/shader/PMX.vs";
		String f = "/shader/PMX.fs";
		shaderprogram = Shader.loadShaderProgram(gl, v, f);

		String v2 = "/shader/pmx/Bone.vs";
		String f2 = "/shader/pmx/Bone.fs";
		shaderprogram2 = Shader.loadShaderProgram(gl, v2, f2);

		pmx.init(gl);

		uniformLight = gl.glGetUniformLocation(shaderprogram, "LightPosition_worldspace");
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	double time = 0.0d;
	double time2 = 0.0d;
	private void updateTime(){
		Time.update();
		time += Time.deltaTime;
		Parameter.t = (float)Math.abs(Math.sin(time));

		time2 += defaultSpeed * Time.deltaTime;
	}
	
	double prevTime = 0.0f;
	@Override
	public void display(GLAutoDrawable drawable) {
		updateTime();
		walkThrough();
		GL4 gl = drawable.getGL().getGL4();
		Debug.gl = gl;
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LESS);

		
		//gl.glEnable(GL_CULL_FACE);
		gl.glCullFace(GL_BACK);

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


		Vector3f eye = mode ? getEyePositionForward() : getEyePositionLookAtCenter();
		Vector3f center = mode ? 
			new Vector3f(eye.x, eye.y, eye.z + 1.0f)
			: new Vector3f(0.0f, eye.y, 0.0f);
		Vector3f up = new Vector3f(0.0f, 1.0f, 0.0f);
		Matrix4f V = new Matrix4f().lookAt(eye, center, up); 
		Matrix4f P = new Matrix4f().perspective((float) Math.toRadians(45.0f), 1.0f, 1.0f, 100000.0f);
		MVP.setVP(V, P);

		gl.glUseProgram(shaderprogram);
		Vector3f lightPos = eye; 
		lightPos.get(Memory.vec3);
		gl.glUniform3fv(uniformLight, 1, Memory.vec3);
		MVP.setUniformLocation(gl, shaderprogram, "MVP", "M", "V");
		MVP.uniformMVP(gl);
		if (!Parameter.printFlag) Parameter.printFlag = prevTime != time2;
		pmx.animation((float)time2);
		prevTime = time2;
		pmx.render(gl);
		Parameter.printFlag = false;

		gl.glDisable(GL_DEPTH_TEST);
		gl.glUseProgram(shaderprogram2);
		MVP.setUniformLocation(gl, shaderprogram2, "MVP", "M", "V");
		MVP.uniformMVP(gl);

		// for debug purpose
		//pmx.renderIK(gl);
		Debug.draw();
		//pmx.renderBones(gl);
	}
	int count = 0;

	@Override
	public void dispose(GLAutoDrawable drawable) {
		if(animator != null) animator.stop();
	}
	
	private Vector3f getEyePositionForward(){
		double x = angleV * 5.0f;
		double y = height;
		double z = -r;
		return new Vector3f((float)x, (float)y, (float)z);
	}
	private Vector3f getEyePositionLookAtCenter(){
		double x = r * Math.cos(angleV);
		double y = height;
		double z = -r * Math.sin(angleV);
		return new Vector3f((float)x, (float)y, (float)z);
	}
	private float height, angleV;
	private double r = 30.0d;
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
	private boolean mode = true;
	private static final int[] arrows = 
		{KeyEvent.VK_UP, KeyEvent.VK_DOWN, KeyEvent.VK_RIGHT, KeyEvent.VK_LEFT};
	@Override
	public void keyPressed(KeyEvent e) {
		int code = e.getKeyCode();
		for (int i = 0; i < arrows.length; i++){
			if (code == arrows[i]) arrowFlag[i] = true;
		}
		if (code == KeyEvent.VK_1) time2 -= 0.025f;
		if (code == KeyEvent.VK_2) time2 += 0.025f;
		if (code == KeyEvent.VK_3) time2 = 0.0f;
		char c  = e.getKeyChar();
		if (c == 'w') scaleDirection = 5;
		if (c == 's') scaleDirection = -5;
		if (c == 'e') scaleDirection = 50;
		if (c == 'd') scaleDirection = -50;
		if (c == 'm') mode = !mode;
		if (c == 'c') {
			Parameter.length = ++Parameter.length % 3;
			if (Parameter.length == 0){
				Parameter.count = ++Parameter.count % 40;
			}
			System.out.printf("%d loop/%d\n", Parameter.count, Parameter.length);
			Parameter.printFlag = true;
		}
		if (c == 'v') {
			Parameter.length = (--Parameter.length + 3) % 3;
			if (Parameter.length == 2){
				Parameter.count = (--Parameter.count + 40) % 40;
			}
			System.out.printf("%d loop/%d\n", Parameter.count, Parameter.length);
			Parameter.printFlag = true;
		}
		if (c == 'o') defaultSpeed = -1;
		if (c == 'p') defaultSpeed = 1;
		if (c == 'i') defaultSpeed = 0;

		if (c == 'f') {
			Parameter.angleFlag = !Parameter.angleFlag;
			System.out.println("angleFlag = " + Parameter.angleFlag);
		}
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
		if(keyChar == KEY_ESC) {
			glWindow.destroy();
		}
	}
		
	@Override
	public void mouseEntered(MouseEvent e) { }
	
	@Override
	public void mouseExited(MouseEvent e) {}
	
	@Override
	public void mousePressed(MouseEvent e) {
		//animator.resume();
	}
	
	private int defaultSpeed = 0;
	@Override
	public void mouseClicked(MouseEvent e) { 
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