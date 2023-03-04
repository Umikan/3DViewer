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

import viewer.model.image.Image;
import viewer.shader.Shader;

import static com.jogamp.opengl.GL4.*;

import java.awt.FileDialog;
import java.awt.Frame;

public class ImageViewer implements GLEventListener, MouseListener, KeyListener {

	private static String path = null;

	public static void selectFile() {
		FileDialog dialog = new FileDialog((Frame) null, "FileDialog (AWT)", FileDialog.LOAD);

		dialog.setVisible(true);
		path = dialog.getDirectory() + dialog.getFile();
	}

	public static void main(String[] args) {
		selectFile();
		new ImageViewer();
	}

	private final FPSAnimator animator;
	private final GLWindow glWindow;
	private static final char KEY_ESC = 0x1b;

	public ImageViewer() {
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL4));

		glWindow = GLWindow.create(caps);
		glWindow.setTitle("PMXViewer");
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

	Image img;
	int shaderprogram;
	@Override
	public void init(GLAutoDrawable drawable) {
		img = new Image(path);
		GL4 gl = drawable.getGL().getGL4();
		gl.glClearColor(0f, 1.0f, 0f, 1.0f);
		String v = "/shader/Image.vs";
		String f = "/shader/Image.fs";
		shaderprogram = Shader.loadShaderProgram(gl, v, f);
		gl.glUseProgram(shaderprogram);

        img.init(gl);
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
	}

	@Override
	public void display(GLAutoDrawable drawable) {
		GL4 gl = drawable.getGL().getGL4();
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glDepthFunc(GL_LESS);
		
		//gl.glEnable(GL_CULL_FACE);
		gl.glCullFace(GL_BACK);

		gl.glEnable(GL_BLEND);
		gl.glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);


		gl.glUniform1i(gl.glGetUniformLocation(shaderprogram, "tex"), 0);
        img.render(gl);

	}
	int count = 0;

	@Override
	public void dispose(GLAutoDrawable drawable) {
		if(animator != null) animator.stop();
	}
	
	@Override
	public void keyPressed(KeyEvent e) {
	}
	
	@Override
	public void keyReleased(KeyEvent e) {

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