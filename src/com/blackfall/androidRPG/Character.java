package com.blackfall.androidRPG;

import org.anddev.andengine.engine.handler.physics.PhysicsHandler;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import com.blackfall.androidRPG.AndroidRPG.Direction;

public class Character {
	private int characterId;
	private int modelId;
	private float xPos;
	private float yPos;
	private Direction direction;
	private AnimatedSprite sprite;
	private PhysicsHandler physicsHandler;
	private TiledTextureRegion characterTextureRegion;
	
	public Character(int characterId, int modelId, float xPos, float yPos, TiledTextureRegion characterTextureRegion) {
		this.characterId = characterId;
		this.modelId = modelId;
		this.xPos = xPos;
		this.yPos = yPos;
		this.characterTextureRegion = characterTextureRegion.clone();
		this.sprite = new AnimatedSprite(xPos, yPos, 48, 64, this.characterTextureRegion);	
		this.physicsHandler = new PhysicsHandler(this.sprite);
		this.sprite.registerUpdateHandler(physicsHandler);
		this.direction = Direction.NORTH;
	}
	
	public void move(float xVelocity, float yVelocity) {
		this.physicsHandler.setVelocity(xVelocity, yVelocity);
	}
	
	public int getModelId() {
		return modelId;
	}
	public void setModelId(int modelId) {
		this.modelId = modelId;
	}
	public float getXPos() {
		return xPos;
	}
	public void setXPos(float xPos) {
		this.xPos = xPos;
	}
	public float getYPos() {
		return yPos;
	}
	public void setYPos(float yPos) {
		this.yPos = yPos;
	}
	public void setSprite(AnimatedSprite sprite) {
		this.sprite = sprite;
	}
	public AnimatedSprite getSprite() {
		return sprite;
	}
	public void setCharacterTextureRegion(TiledTextureRegion textureRegion) {
		characterTextureRegion = textureRegion;
	}
	public TiledTextureRegion getCharacterTextureRegion() {
		return characterTextureRegion;
	}
	public void setDirection(Direction direction) {
		this.direction = direction;
	}
	public Direction getDirection() {
		return direction;
	}
	public void setCharacterId(int characterId) {
		this.characterId = characterId;
	}
	public int getCharacterId() {
		return characterId;
	}
}
