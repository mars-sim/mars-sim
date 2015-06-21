package mdrs;

import com.jme3.app.SimpleApplication;
import com.jme3.bullet.BulletAppState;
import com.jme3.bullet.collision.shapes.CapsuleCollisionShape;
import com.jme3.bullet.collision.shapes.CollisionShape;
import com.jme3.bullet.control.CharacterControl;
import com.jme3.bullet.control.RigidBodyControl;
import com.jme3.bullet.util.CollisionShapeFactory;
import com.jme3.input.KeyInput;
import com.jme3.input.controls.ActionListener;
import com.jme3.input.controls.KeyTrigger;
import com.jme3.material.Material;
import com.jme3.math.Vector3f;
import com.jme3.renderer.Camera;
import com.jme3.renderer.queue.RenderQueue.ShadowMode;
import com.jme3.scene.Node;
import com.jme3.terrain.geomipmap.TerrainLodControl;
import com.jme3.terrain.geomipmap.TerrainQuad;
import com.jme3.terrain.heightmap.AbstractHeightMap;
import com.jme3.terrain.heightmap.ImageBasedHeightMap;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture.WrapMode;
import java.util.ArrayList;
import java.util.List;
import mdrs.sky.Sky;

public class MDRSTerrain extends SimpleApplication implements ActionListener {

    // Static members
    public static Vector3f INITIAL_POS = new Vector3f(-450f, 150f, 1200f);
    
    // Data members
    private BulletAppState bulletAppState;
    private RigidBodyControl landscape;
    private CharacterControl player;
    private Vector3f walkDirection = new Vector3f();
    private boolean left = false, right = false, up = false, down = false;
    private TerrainQuad terrain;
    private Material mat_terrain;
    private Sky sky;
    private boolean terrainWireframe = false;

    /**
     * Main method.
     * @param args command line arguments.
     */
    public static void main(String[] args) {
        
        MDRSTerrain app = new MDRSTerrain();
        app.start();
    }

    @Override
    public void simpleInitApp() {

        // Set up physics
        bulletAppState = new BulletAppState();
        stateManager.attach(bulletAppState);
        //bulletAppState.getPhysicsSpace().enableDebug(assetManager);
        bulletAppState.setDebugEnabled(true);

        flyCam.setMoveSpeed(100);

        // Set up keys.
        setUpKeys();

        // Set default shadow mode to off.
        rootNode.setShadowMode(ShadowMode.Off);
        
        // Set up sky.
        setUpSky();

        // Create terrain material and load all textures into it.
        setUpTerrainMaterial();

        // Create terrain.
        setUpTerrain();

        // Create the player POV and collision shape.
        setUpPlayer();

        // Create the level of detail for cameras.
        setUpLOD();
    }

    /**
     * Set up the sky box.
     */
    private void setUpSky() {

        // Create sky.
        sky = new Sky(assetManager, viewPort, rootNode);
        rootNode.attachChild(sky);
    }

    /**
     * Set up the terrain material.
     */
    private void setUpTerrainMaterial() {

        // Create terrain material and load four textures into it.
        mat_terrain = new Material(assetManager,
                "Common/MatDefs/Terrain/TerrainLighting.j3md");

//        mat_terrain.setBoolean("isTerrainGrid",true);
//        mat_terrain.setBoolean("useTriPlanarMapping",false);
//        mat_terrain.setBoolean("WardIso",false);

        // Add ALPHA map (for red-blue-green coded splat textures).
        mat_terrain.setTexture("AlphaMap", assetManager.loadTexture(
                "Textures/Terrain/mdrs_region_alpha3_1.png"));

        // Add red splat color terrain.
        Texture texture0 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture2_seamless.jpg");
        texture0.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("DiffuseMap", texture0);
        mat_terrain.setFloat("DiffuseMap_0_scale", 512f);
        Texture normal0 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture2_seamless_normal.jpg");
        normal0.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("NormalMap", normal0);

        // Add green splat color terrain.
        Texture texture1 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture4_seamless.jpg");
        texture1.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("DiffuseMap_1", texture1);
        mat_terrain.setFloat("DiffuseMap_1_scale", 512f);
        Texture normal1 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture4_seamless_normal.jpg");
        normal1.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("NormalMap_1", normal1);

        // Add blue splat color terrain.
        Texture texture2 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture10_seamless.jpg");
        texture2.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("DiffuseMap_2", texture2);
        mat_terrain.setFloat("DiffuseMap_2_scale", 512f);
        Texture normal2 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture10_seamless_normal.jpg");
        normal2.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("NormalMap_2", normal2);

        // Add surface map for alpha value.
        Texture surfaceMap = assetManager.loadTexture(
                "Textures/Terrain/mdrs_region_surface4.png");
        mat_terrain.setTexture("DiffuseMap_3", surfaceMap);
        mat_terrain.setFloat("DiffuseMap_3_scale", 1f);

        // Add ALPHA map (for red-blue-green coded splat textures).
        mat_terrain.setTexture("AlphaMap_1", assetManager.loadTexture(
                "Textures/Terrain/mdrs_region_alpha4.png"));

        // Add red splat color terrain.
        Texture texture4 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture9_seamless.jpg");
        texture4.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("DiffuseMap_4", texture4);
        mat_terrain.setFloat("DiffuseMap_4_scale", 512f);
        Texture normal4 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture9_seamless_normal.jpg");
        normal4.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("NormalMap_4", normal4);

        // Add green splat color terrain.
        Texture texture5 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture5_seamless.jpg");
        texture5.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("DiffuseMap_5", texture5);
        mat_terrain.setFloat("DiffuseMap_5_scale", 512f);
        Texture normal5 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture5_seamless_normal.jpg");
        normal5.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("NormalMap_5", normal5);

        // Add blue splat color terrain.
        Texture texture6 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture3_seamless.jpg");
        texture6.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("DiffuseMap_6", texture6);
        mat_terrain.setFloat("DiffuseMap_6_scale", 512f);
        Texture normal6 = assetManager.loadTexture(
                "Textures/Terrain/splat/texture3_seamless_normal.jpg");
        normal6.setWrap(WrapMode.Repeat);
        mat_terrain.setTexture("NormalMap_6", normal6);

        // Add surface map for alpha value.
        mat_terrain.setTexture("DiffuseMap_7", surfaceMap);
        mat_terrain.setFloat("DiffuseMap_7_scale", 1f);
        
        //mat_terrain.getAdditionalRenderState().setWireframe(true);
    }

    /**
     * Set up height map and the terrain quad.
     */
    private void setUpTerrain() {

        // Create the height map.
        Texture heightMapImage = assetManager.loadTexture(
                "Textures/Terrain/mdrs_region_topo_2x.jpg");
        AbstractHeightMap heightmap = new ImageBasedHeightMap(
                heightMapImage.getImage());
        heightmap.load();
        heightmap.smooth(.9f);

        // Create the terrain quad based on the height map.
        int patchSize = 65;
        terrain = new TerrainQuad("my terrain", patchSize, 2049,
                heightmap.getHeightMap());
        terrain.setMaterial(mat_terrain);
        terrain.setLocalTranslation(0f, 0f, 0f);
        terrain.setLocalScale(2.5f, 1f, 2.5f);
        terrain.setShadowMode(ShadowMode.Receive);
        rootNode.attachChild(terrain);

        // Set up collision detection for the terrain by creating a compound 
        // collision shape and a static RigidBodyControl with mass zero.
        CollisionShape terrainShape =
                CollisionShapeFactory.createMeshShape((Node) terrain);
        landscape = new RigidBodyControl(terrainShape, 0);
        terrain.addControl(landscape);

        // Add terrain to the physics space.
        bulletAppState.getPhysicsSpace().add(terrain);
    }

    /**
     * Set up the player POV and collision shape.
     */
    private void setUpPlayer() {

        // Create collision detection for the player by creating
        // a capsule collision shape and a CharacterControl.
        CapsuleCollisionShape capsuleShape = new CapsuleCollisionShape(.5f, 2f,
                1);
        player = new CharacterControl(capsuleShape, 0.05f);
        player.setJumpSpeed(20);
        player.setFallSpeed(30);
        player.setGravity(30);

        // Put the player in its starting position.
        player.setPhysicsLocation(MDRSTerrain.INITIAL_POS);

        // Add player to the physics space.
        bulletAppState.getPhysicsSpace().add(player);
    }

    /**
     * Over-write some navigational key mappings to add 
     * physics-controlled walking and jumping.
     */
    private void setUpKeys() {
        // Configure navigational keys.
        inputManager.addMapping("Left", new KeyTrigger(KeyInput.KEY_A));
        inputManager.addMapping("Right", new KeyTrigger(KeyInput.KEY_D));
        inputManager.addMapping("Up", new KeyTrigger(KeyInput.KEY_W));
        inputManager.addMapping("Down", new KeyTrigger(KeyInput.KEY_S));
        inputManager.addMapping("Jump", new KeyTrigger(KeyInput.KEY_SPACE));
        inputManager.addListener(this, "Left");
        inputManager.addListener(this, "Right");
        inputManager.addListener(this, "Up");
        inputManager.addListener(this, "Down");
        inputManager.addListener(this, "Jump");
        
        // Configure time control keys.
        inputManager.addMapping("Slow Time", new KeyTrigger(KeyInput.KEY_J));
        inputManager.addMapping("Normal Time", new KeyTrigger(KeyInput.KEY_K));
        inputManager.addMapping("Speed Time", new KeyTrigger(KeyInput.KEY_L));
        inputManager.addListener(this, "Slow Time");
        inputManager.addListener(this, "Normal Time");
        inputManager.addListener(this, "Speed Time");
        
        // Configure terrain wireframe toggle key.
        inputManager.addMapping("Toggle Wireframe", new KeyTrigger(KeyInput.KEY_T));
        inputManager.addListener(this, "Toggle Wireframe");
    }

    /**
     * Set up the level of detail over distance for cameras.
     */
    private void setUpLOD() {

        List<Camera> cameras = new ArrayList<Camera>();
        cameras.add(getCamera());
        getCamera().setFrustumFar(2048f * 2.5f * 2f);
        TerrainLodControl control = new TerrainLodControl(terrain, cameras);
        terrain.addControl(control);
    }

    /**
     * Keep track of the direction the user pressed.
     */
    public void onAction(String binding, boolean pressed, float tpf) {
        if (binding.equals("Left")) {
            if (pressed) {
                left = true;
            } else {
                left = false;
            }
        } else if (binding.equals("Right")) {
            if (pressed) {
                right = true;
            } else {
                right = false;
            }
        } else if (binding.equals("Up")) {
            if (pressed) {
                up = true;
            } else {
                up = false;
            }
        } else if (binding.equals("Down")) {
            if (pressed) {
                down = true;
            } else {
                down = false;
            }
        } else if (binding.equals("Jump") && pressed) {
            player.jump();
        } else if (binding.equals("Slow Time") && pressed) {
            slowTime();
        } else if (binding.equals("Normal Time") && pressed) {
            normalTime();
        } else if (binding.equals("Speed Time") && pressed) {
            speedTime();
        } else if (binding.equals("Toggle Wireframe") && pressed) {
            toggleTerrainWireframe();
        }
    }
    
    private void slowTime() {
        float currentTC = sky.getTimeCompression();
        if (currentTC > 1F) {
            sky.setTimeCompression(currentTC / 4F);
        }
        else if (currentTC <= -1F) {
            sky.setTimeCompression(currentTC * 4F);
        }
        else if ((currentTC <=1F) && (currentTC > 0F)) {
            sky.setTimeCompression(0F);
        }
        else {
            sky.setTimeCompression(-1F);
        }
    }
    
    private void normalTime() {
        sky.setTimeCompression(1F);
    }
    
    private void speedTime() {
        float currentTC = sky.getTimeCompression();
        if (currentTC >= 1F) {
            sky.setTimeCompression(currentTC * 4F);
        }
        else if (currentTC < -1F) {
            sky.setTimeCompression(currentTC / 4F);
        }
        else if ((currentTC >= -1F) && (currentTC < 0F)) {
            sky.setTimeCompression(0F);
        }
        else {
            sky.setTimeCompression(1F);
        }
    }
    
    private void toggleTerrainWireframe() {
        terrainWireframe = !terrainWireframe;
        mat_terrain.getAdditionalRenderState().setWireframe(terrainWireframe);
    }

    /**
     * This is the main event loop--walking happens here. We check in 
     * which direction the player is walking by interpreting the camera 
     * direction forward (camDir) and to the side (camLeft). The 
     * setWalkDirection() command is what lets a physics-controlled player walk. 
     * We also make sure here that the camera moves with player.
     */
    @Override
    public void simpleUpdate(float tpf) {
        Vector3f camDir = cam.getDirection().clone().multLocal(0.6f);
        Vector3f camLeft = cam.getLeft().clone().multLocal(0.4f);
        walkDirection.set(0, 0, 0);
        if (left) {
            walkDirection.addLocal(camLeft);
        }
        if (right) {
            walkDirection.addLocal(camLeft.negate());
        }
        if (up) {
            walkDirection.addLocal(camDir);
        }
        if (down) {
            walkDirection.addLocal(camDir.negate());
        }
        player.setWalkDirection(walkDirection);
        cam.setLocation(player.getPhysicsLocation());
        
        // Update sky.
        sky.update(tpf, player);
    }
}