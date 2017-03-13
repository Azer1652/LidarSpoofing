package worldview;

import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;
import com.jogamp.opengl.glu.GLU;
import com.jogamp.opengl.util.FPSAnimator;
import world.Segment;
import world.World;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import static com.jogamp.opengl.GL.*;
import static com.jogamp.opengl.GL2ES1.GL_PERSPECTIVE_CORRECTION_HINT;
import static com.jogamp.opengl.fixedfunc.GLLightingFunc.GL_SMOOTH;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_MODELVIEW;
import static com.jogamp.opengl.fixedfunc.GLMatrixFunc.GL_PROJECTION;
import static java.lang.Math.sin;

/**
 * Created by arthu on 13/03/2017.
 */
public class WorldViewer extends GLCanvas implements GLEventListener, KeyListener{

    World world = null;

    private GLU glu;
    JFrame frame = new JFrame();

    public WorldViewer(World world, int width, int height){
        this.world = world;

        frame.getContentPane().add(this);
        frame.addKeyListener(this);
        this.setPreferredSize(new Dimension(width, height));
        this.addGLEventListener(this);
        this.addKeyListener(this);

        // Create a animator that drives canvas' display() at the specified FPS.
        final FPSAnimator animator = new FPSAnimator(this, 60, true);

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                // Use a dedicate thread to run the stop() to ensure that the
                // animator stops before program exits.
                new Thread(() -> {
                    if (animator.isStarted()) animator.stop();
                    System.exit(0);
                }).start();
            }
        });
        frame.setTitle("Simulation View");
        frame.pack();
        frame.setVisible(true);
        animator.start(); // start the animation loop
    }

    @Override
    public void init(GLAutoDrawable drawable) {
        GL2 gl = drawable.getGL().getGL2();      // get the OpenGL graphics context
        glu = new GLU();                         // get GL Utilities
        gl.glClearColor(0.0f, 0.0f, 0.0f, 0.0f); // set background (clear) color
        gl.glClearDepth(1.0f);      // set clear depth value to farthest
        gl.glEnable(GL_DEPTH_TEST); // enables depth testing
        gl.glDepthFunc(GL_LEQUAL);  // the type of depth test to do
        gl.glHint(GL_PERSPECTIVE_CORRECTION_HINT, GL_NICEST); // best perspective correction
        gl.glShadeModel(GL_SMOOTH); // blends colors nicely, and smoothes out lighting
    }

    @Override
    public void dispose(GLAutoDrawable drawable) {

    }

    @Override
    public void display(GLAutoDrawable drawable) {
        render(drawable);
        updateWorld();

    }

    private void render(GLAutoDrawable drawable){
        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context
        gl.glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear color and depth buffers
        gl.glLoadIdentity();  // reset the model-view matrix

        gl.glEnable(GL_BLEND); // Turn Blending On
        gl.glDisable(GL_DEPTH_TEST); // Turn Depth Testing Off

        // Rotate up and down to look up and down
        gl.glRotated(0, 1.0f, 0, 0);

        // Player at headingY. Rotate the scene by -headingY instead (add 360 to get a
        // positive angle)
        gl.glRotated(-270-world.getCarHeadingDeg(), 0, 1.0f, 0);

        // Player is at (posX, 0, posZ). Translate the scene to (-posX, 0, -posZ)
        // instead.
        gl.glTranslated(-world.getCarLocation()[0], 0, world.getCarLocation()[1]);

        // Process each triangle
        for(Segment s : world.getSegments()){
            gl.glBegin(GL2.GL_QUADS);
            gl.glNormal3f(0.0f, 0.0f, 1.0f); // Normal pointing out of screen

            gl.glVertex3d(s.vertex.get(0)[0],
                    s.vertex.get(0)[1], s.vertex.get(0)[2]);

            gl.glVertex3d(s.vertex.get(1)[0],
                    s.vertex.get(1)[1], s.vertex.get(1)[2]);

            gl.glVertex3d(s.vertex.get(2)[0],
                    s.vertex.get(2)[1], s.vertex.get(2)[2]);

            gl.glVertex3d(s.vertex.get(3)[0],
                    s.vertex.get(3)[1], s.vertex.get(3)[2]);
        }
        
        /*
        // ----- Your OpenGL rendering code here (Render a white triangle for testing) -----
        gl.glTranslatef(0.0f, 0.0f, -6.0f); // translate into the screen
        gl.glBegin(GL_TRIANGLES); // draw using triangles
        gl.glVertex3f(0.0f, 1.0f, 0.0f);
        gl.glVertex3f(-1.0f, -1.0f, 0.0f);
        gl.glVertex3f(1.0f, -1.0f, 0.0f);
        */
        gl.glEnd();
    }

    private void updateWorld(){

    }

    @Override
    public void reshape(GLAutoDrawable drawable, int x, int y, int width, int height) {
        GL2 gl = drawable.getGL().getGL2();  // get the OpenGL 2 graphics context

        if (height == 0) height = 1;   // prevent divide by zero
        float aspect = (float)width / height;

        // Set the view port (display area) to cover the entire window
        gl.glViewport(0, 0, width, height);

        // Setup perspective projection, with aspect ratio matches viewport
        gl.glMatrixMode(GL_PROJECTION);  // choose projection matrix
        gl.glLoadIdentity();             // reset projection matrix
        glu.gluPerspective(45.0, aspect, 0.1, 30000.0); // fovy, aspect, zNear, zFar

        // Enable the model-view transform
        gl.glMatrixMode(GL_MODELVIEW);
        gl.glLoadIdentity(); // reset
    }

    @Override
    public void keyTyped(KeyEvent e) {
        world.move(e);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        world.setKey(e);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        world.removeKey(e);
    }
}
