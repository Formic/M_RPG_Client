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

public class CopyOfAndroidRPG extends BaseGameActivity {
	// ===========================================================
	// Constants
	// ===========================================================

	private static final int CAMERA_WIDTH = 720;
	private static final int CAMERA_HEIGHT = 480;

	// ===========================================================
	// Fields
	// ===========================================================

	private Camera mCamera;

	private RepeatingSpriteBackground mGrassBackground;

	private Texture mTexture;
	private TiledTextureRegion mPlayerTextureRegion;
	private Texture mOnScreenControlTexture;
	private TextureRegion mOnScreenControlBaseTextureRegion;
    private TextureRegion mOnScreenControlKnobTextureRegion;

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
		this.mCamera = new Camera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);
		
		return new Engine(new EngineOptions(true, ScreenOrientation.LANDSCAPE, new RatioResolutionPolicy(CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera));
	}

	@Override
	public void onLoadResources() {
		this.mTexture = new Texture(128, 128, TextureOptions.BILINEAR_PREMULTIPLYALPHA);
		this.mPlayerTextureRegion = TextureRegionFactory.createTiledFromAsset(this.mTexture, this, "gfx/player.png", 0, 0, 3, 4);
		this.mGrassBackground = new RepeatingSpriteBackground(CAMERA_WIDTH, CAMERA_HEIGHT, this.mEngine.getTextureManager(), new AssetTextureSource(this, "gfx/background_grass.png"));

		this.mOnScreenControlTexture = new Texture(256, 128, TextureOptions.BILINEAR);
        this.mOnScreenControlBaseTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "gfx/onscreen_control_base.png", 0, 0);
        this.mOnScreenControlKnobTextureRegion = TextureRegionFactory.createFromAsset(this.mOnScreenControlTexture, this, "gfx/onscreen_control_knob.png", 128, 0);

        this.mEngine.getTextureManager().loadTextures(this.mTexture, this.mOnScreenControlTexture);
	}
	
	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());

		final Scene scene = new Scene(1);
		scene.setBackground(this.mGrassBackground);

		/* Calculate the coordinates for the face, so its centered on the camera. */
		final int centerX = (CAMERA_WIDTH - this.mPlayerTextureRegion.getTileWidth()) / 2;
		final int centerY = (CAMERA_HEIGHT - this.mPlayerTextureRegion.getTileHeight()) / 2;

		/* Create the sprite and add it to the scene. */
		final AnimatedSprite player = new AnimatedSprite(centerX, centerY, 48, 64, this.mPlayerTextureRegion);
		scene.attachChild(player);
		
		final PhysicsHandler physicsHandler = new PhysicsHandler(player);
		player.registerUpdateHandler(physicsHandler);

		final AnalogOnScreenControl analogOnScreenControl = new AnalogOnScreenControl(0, CAMERA_HEIGHT - this.mOnScreenControlBaseTextureRegion.getHeight(), this.mCamera, this.mOnScreenControlBaseTextureRegion, this.mOnScreenControlKnobTextureRegion, 0.1f, 200, new IAnalogOnScreenControlListener() {

			@Override
			public void onControlChange( BaseOnScreenControl pBaseOnScreenControl, float pValueX, float pValueY) {
				if (Math.abs(pValueX) > Math.abs(pValueY)) {
					physicsHandler.setVelocity(pValueX * 200, 0);
					if (pValueX > 0) //going right
						player.animate(new long[]{200, 200, 200}, 3, 5, true);
					else //going left
						player.animate(new long[]{200, 200, 200}, 9, 11, true);
					
				}
				else { 
					physicsHandler.setVelocity(0, pValueY * 200);
					if (pValueY > 0) //going down
						player.animate(new long[]{200, 200, 200}, 6, 8, true);
					else //going up
						player.animate(new long[]{200, 200, 200}, 0, 2, true);
						
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

		openConnection();
		return scene;
	}

	@Override
	public void onLoadComplete() {

	}

	// ===========================================================
	// Methods
	// ===========================================================
	private void openConnection() {
		try {
			Socket s = new Socket("24.10.219.59", 2000);

			//outgoing stream redirect to socket
			//OutputStream out = s.getOutputStream();
			//BufferedReader input = new BufferedReader(new InputStreamReader(s.getInputStream()));
			//read line(s)
			//String st = input.readLine();

			//Close connection
			Toast.makeText(this, "Connected to server!", Toast.LENGTH_LONG).show();
			s.close();
		} catch (UnknownHostException e) {
			Toast.makeText(this, "Unknown Host: " + e.toString(), Toast.LENGTH_LONG).show();
		} catch (IOException e) {
			Toast.makeText(this, "IO Exeption: " + e.toString(), Toast.LENGTH_LONG).show();
		}
	}

	// ===========================================================
	// Inner and Anonymous Classes
	// ===========================================================

}