package viewer.model;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.newt.event.KeyEvent;
import com.jogamp.newt.event.KeyListener;
import com.jogamp.newt.event.MouseEvent;
import com.jogamp.newt.event.MouseListener;
import com.jogamp.newt.event.WindowAdapter;
import com.jogamp.newt.event.WindowEvent;
import com.jogamp.newt.opengl.GLWindow;
import com.jogamp.opengl.util.FPSAnimator;
import com.jogamp.opengl.util.texture.Texture;

import viewer.model.common.*;
import viewer.model.obj.*;
import java.awt.FileDialog;
import java.awt.Frame;

import static com.jogamp.opengl.GL2.*;

public class ObjViewer implements GLEventListener, MouseListener, KeyListener {
	
	private static String path = null;

	public static String selectFile() {
		FileDialog dialog = new FileDialog((Frame) null, "Choose your OBJ File", FileDialog.LOAD);

		dialog.setVisible(true);
		return dialog.getDirectory() + dialog.getFile();
	}

	public static void main(String[] args){
		path = selectFile();
		new ObjViewer();
	}
	private final GLU glu;
	private final FPSAnimator animator; //(1)
	private final GLWindow glWindow;
	private static final char KEY_ESC = 0x1b;
	
	public ObjViewer() {
		GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));
		glu = new GLU();
		
		glWindow = GLWindow.create(caps);
		glWindow.setTitle("OBJ Viewer");
		glWindow.setSize(300, 300);
		glWindow.addGLEventListener(this);
		
		glWindow.addWindowListener(new WindowAdapter() {
			@Override
			public void windowDestroyed(WindowEvent evt) {
				System.exit(0);
			}
		});
		
		glWindow.addMouseListener(this);
		glWindow.addKeyListener(this);
		animator = new FPSAnimator(60);
		animator.add(glWindow);
		animator.start();
		animator.pause();
		glWindow.setVisible(true);
	}

	BaseModel model;
	TexManager tm;
	@Override
	public void init(GLAutoDrawable drawable) {
		model = new ObjParser(path);
		GL2 gl = drawable.getGL().getGL2();
		gl.glClearColor(1f, 1f, 1f, 1.0f);
		tm = new TexManager(model);
	}
	
	@Override
	public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
		GL2 gl = drawable.getGL().getGL2();
		
		gl.glMatrixMode(GL_PROJECTION);
		gl.glLoadIdentity();
		glu.gluPerspective(30.0, (double)width / (double)height, 1.0, 300.0);
		
		glu.gluLookAt(0.0f, 0.0f, 30.0f, 0.0f, 5.0f, 0.0f, 0.0f, 1.0f, 0.0f);
		gl.glMatrixMode(GL_MODELVIEW);
	}

	float r = 0.0f;
	boolean rotateFlag = false;
	@Override
	public void display(GLAutoDrawable drawable) {
		GL2 gl = drawable.getGL().getGL2();
		gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		gl.glEnable(GL_DEPTH_TEST);
		gl.glEnable(GL_LIGHTING);
		gl.glEnable(GL_LIGHT0);
		gl.glLoadIdentity();
		gl.glRotatef(r, 0.0f, 1.0f, 0.0f);

		for (MtlMesh f : model.f){
			Material mtl = f.mtl;
			if (mtl.map_Kd != null) {
				Texture texture = tm.get(mtl.map_Kd);
				texture.enable(gl);
				texture.bind(gl);
			}
			gl.glBegin(GL_TRIANGLES);
			for (Face face : f){
				for (Face.Index index : face){
					gl.glMaterialfv(GL_FRONT, GL_AMBIENT, mtl.toArray(mtl.Ka), 0);
					gl.glMaterialfv(GL_FRONT, GL_DIFFUSE, mtl.toArray(mtl.Kd), 0);
					gl.glMaterialfv(GL_FRONT, GL_SPECULAR, mtl.toArray(mtl.Ks), 0);
					gl.glMaterialf(GL_FRONT, GL_SHININESS, mtl.Ns);
					gl.glTexCoord2f(model.vt.get(index.vt).u, model.vt.get(index.vt).v);
					gl.glNormal3f(model.vn.get(index.vn).x, model.vn.get(index.vn).y, model.vn.get(index.vn).z);
					gl.glVertex3f(model.v.get(index.v).x, model.v.get(index.v).y, model.v.get(index.v).z);
				}
			}
			gl.glEnd();
		}
		gl.glDisable(GL_LIGHTING);

		if (rotateFlag){
			r += 10.0f;
			if (r > 360.0f) r -= 360.0f;
		}
	}

	
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
		rotateFlag = true;
		animator.resume();
	}
	
	@Override
	public void mouseReleased(MouseEvent e) {
		rotateFlag = false;
		animator.pause();
	}
	
	@Override
	public void mouseMoved(MouseEvent e) { }
	
	@Override
	public void mouseDragged(MouseEvent e) {}
	
	@Override
	public void mouseWheelMoved(MouseEvent e) {}
}