package org.mars_sim.msp.ui.ogl.sandbox.scene;

import javax.media.opengl.GL2;
import javax.media.opengl.GLAutoDrawable;
import javax.media.opengl.GLEventListener;
import javax.media.opengl.glu.GLU;

public abstract class DisplayAbstract implements GLEventListener {

	protected final static double FOV = 45.0;
	protected final static double NEAR_CLIPPING = 1.0;
	protected final static double FAR_CLIPPING = 10000.0;
	
	protected final static long INTERVAL_UPDATE_NANOSEC   = 20000000;
	protected final static long INTERVAL_SHOW_FRAME_COUNT = 1000000000;
	
	protected final static boolean SMOOTH_LINES = false;
	protected final static double DELTA_WASD = 0.3;
	protected final static double ZOOM_FACTOR = 1.15;

	protected final static boolean SHOW_FRAME_COUNT = false;
	protected final static boolean SHOW_UPDATE_COUNT = false;
	
	protected long time;
	protected long timeExecution;
	protected long timeBeginning;
	protected long timeFrameCount;
	protected int countUpdates = 0;
	protected int countFrames = 0;
	protected int deltaUpdates = 0;
	protected int deltaFrames = 0;
	protected SceneGroup scene;
	protected GLU glu = new GLU();
	
	protected boolean showFrameCount = SHOW_FRAME_COUNT;
	protected boolean showUpdateCount = SHOW_UPDATE_COUNT;

	public void init(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		long time = time();
		this.time = time;
		this.timeBeginning = time;
		this.timeFrameCount = time;
		this.timeExecution = 0;
        // initialize the scene
        if (scene != null) {
			scene.init(gl,time - timeBeginning);
		}
	}
	
	protected long time() {
		return System.nanoTime();
	}

    public void displayChanged(GLAutoDrawable glDrawable, boolean b, boolean b1) {
    }

	public void display(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
        if (scene != null) {
        	scene.render(gl);
        	// measure time needed
        	long time2 = time();
			this.timeExecution += (time2 - time);
			this.time = time2;
			this.countFrames++;
			this.deltaFrames++;
			// update the scene
			if (this.timeExecution > INTERVAL_UPDATE_NANOSEC) {
				scene.update(INTERVAL_UPDATE_NANOSEC);

				this.timeExecution -= INTERVAL_UPDATE_NANOSEC;
				this.countUpdates++;
				this.deltaUpdates++;
				
				time2 = time();
				long delta = time2 - this.timeFrameCount;
				if (delta >= INTERVAL_SHOW_FRAME_COUNT) {
					if (this.showFrameCount || this.showUpdateCount) {
						double framerate = 1000000000.0f * (double) this.deltaFrames / (double) delta;
						double updaterate = 1000000000.0f * (double) this.deltaUpdates / (double) delta;
						StringBuffer s = new StringBuffer();
						if (this.showFrameCount) {
							s.append("f:");
							s.append(Double.toString(framerate));
						}
						if (this.showUpdateCount) {
							s.append(" u:");
							s.append(Double.toString(updaterate));
						}
						System.out.println(s.toString());
					}
					this.timeFrameCount = time2;
					this.deltaFrames = 0;
					this.deltaUpdates = 0;
				}
			}
//			System.out.println(time - timeBeginning + " " + (time - timeBeginning) / INTERVAL_UPDATE_NANOSEC);
		}
        // finish drawing
		gl.glFlush();
	}
	
	public void reshape(GLAutoDrawable glDrawable, int x, int y, int w, int h) {
		GL2 gl = glDrawable.getGL().getGL2();
		int hh;
		if (h <= 0) // avoid a division by zero error!
            hh = 1;
		else
			hh = h;
        final float a = (float) w / (float) hh;
        gl.glViewport(0, 0, w, hh);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        glu.gluPerspective(FOV, a, NEAR_CLIPPING, FAR_CLIPPING);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity(); // Reset The ModalView Matrix
	}

	public void dispose(GLAutoDrawable glDrawable) {
		GL2 gl = glDrawable.getGL().getGL2();
		scene.close(gl);
	}
	
	public SceneGroup getScene() {
		return scene;
	}

	public void setScene(SceneGroup scene) {
		this.scene = scene;
	}

	public boolean isShowFrameCount() {
		return showFrameCount;
	}

	public boolean isShowUpdateCount() {
		return showUpdateCount;
	}

	public void setShowFrameCount(boolean showFrameCount) {
		this.showFrameCount = showFrameCount;
	}

	public void setShowUpdateCount(boolean showUpdateCount) {
		this.showUpdateCount = showUpdateCount;
	}
}
