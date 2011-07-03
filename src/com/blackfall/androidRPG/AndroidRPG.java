package com.blackfall.androidRPG;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl;
import org.anddev.andengine.engine.camera.hud.controls.AnalogOnScreenControl.IAnalogOnScreenControlListener;
import org.anddev.andengine.engine.camera.hud.controls.BaseOnScreenControl;
import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.background.RepeatingSpriteBackground;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.opengl.texture.Texture;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegionFactory;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.opengl.texture.source.AssetTextureSource;
import org.anddev.andengine.ui.activity.BaseGameActivity;

import android.widget.Toast;

public class AndroidRPG extends BaseGameActivity {
	// ===========================================================
	// Enums
	// ===========================================================
	public enum Direction { NORTH, SOUTH, EAST, WEST }
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private Camera camera;
	private RepeatingSpriteBackground grassBackground;
	private Texture texture;
	private TiledTextureRegion charactersTextureRegion;
	private Texture onScreenControlTexture;
	private TextureRegion onScreenControlBaseTextureRegion;
    private TextureRegion onScreenControlKnobTextureRegion;
    private Scene scene;
    
    private GraphicsEngine graphicsEngine;
    private Client client;
    private float lastXVelocity;
    private float lastYVelocity;
    

	// ===========================================================
	// Constructors
	// ===========================================================

	// ===========================================================
	// Getter & Setter
	// ===========================================================

	// ===========================================================
	// Methods for/from SuperClass/Interfaces
	// ===========================================================

	@Override
	public Engine onLoadEngine() {
		this.camera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.camera));
	}

	@Override
	public void onLoadResources() {
		this.texture = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.charactersTextureRegion = TextureRegionFactory.createTiledFromAsset(this.texture, this, "gfx/player.png", 0, 0, 3, 4);
		this.grassBackground = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.mEngine.getTextureManager(), new AssetTextureSource(this, "gfx/background_grass.png"));

		this.onScreenControlTexture = new Texture(256, 128, TextureOptions.BILINEAR);
        this.onScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(this.onScreenControlTexture, this, "gfx/onscreen_control_base.png", 0, 0);
        this.onScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(this.onScreenControlTexture, this, "gfx/onscreen_control_knob.png", 128, 0);

        this.mEngine.getTextureManager().loadTextures(this.texture, this.onScreenControlTexture);
	}
	
	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		this.scene = new Scene(1);
		scene.setBackground(this.grassBackground);

		/* Calculate the coordinates for the face, so its centered on the camera. */
		final int centerX = (CAMERA_WIDTH - this.charactersTextureRegion.getTileWidth()) / 2;
		final int centerY = (CAMERA_HEIGHT - this.charactersTextureRegion.getTileHeight()) / 2;

		lastXVelocity = lastYVelocity = 0;
		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(0, CAMERA_HEIGHT - this.onScreenControlBaseTextureRegion.getHeight(), this.camera, this.onScreenControlBaseTextureRegion, this.onScreenControlKnobTextureRegion, 0.1f, 200, new IAnalogOnScreenControlListener() {

			@Override
			public void onControlChange( BaseOnScreenControl pBaseOnScreenControl, float pValueX, float pValueY) {
				if (client.getClientId() < 0) 
					return;
				
				//Ensures character doesn't forever walk if joystick snaps back too fast from an angle
				if (pValueX == 0 && pValueY == 0 && (lastXVelocity != 0 ||lastYVelocity != 0)) {
					lastXVelocity = lastYVelocity = 0;
					graphicsEngine.MoveCharacter(client.getClientId(), 0, 0);
					client.sendMove(0, 0);
					return;
				}
					
				
				if (Math.abs(pValueX) >= Math.abs(pValueY)) {
					if (lastXVelocity == pValueX)
						return;
					lastXVelocity = pValueX;
					graphicsEngine.MoveCharacter(client.getClientId(), pValueX * 200, 0);
					client.sendMove(pValueX * 200, 0);
				} else { 
					if (lastYVelocity == pValueY)
						return;
					lastYVelocity = pValueY;
					graphicsEngine.MoveCharacter(client.getClientId(), 0, pValueY * 200);
					client.sendMove(0, pValueY * 200);
				}
			}

			@Override
			public void onControlClick(
					AnalogOnScreenControl pAnalogOnScreenControl) {
				// TODO Auto-generated method stub
				
			}
		});
		analogOnScreenControl.getControlBase().setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		analogOnScreenControl.getControlBase().setAlpha(0.5f);
		analogOnScreenControl.getControlBase().setScaleCenter(0, 128);
		analogOnScreenControl.getControlBase().setScale(1.25f);
		analogOnScreenControl.getControlKnob().setScale(1.25f);
		analogOnScreenControl.refreshControlKnobPosition();

		scene.setChildScene(analogOnScreenControl);

		
		return scene;
	}

	@Override
	public void onLoadComplete() {
		this.graphicsEngine = new GraphicsEngine(this.scene, this.charactersTextureRegion);
		this.client = Client.getInstance(this.graphicsEngine);
	}

	// ===========================================================
	// Methods
	// ===========================================================
	

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}