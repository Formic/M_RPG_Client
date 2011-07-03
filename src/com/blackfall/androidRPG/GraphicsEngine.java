package com.blackfall.androidRPG;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import com.blackfall.androidRPG.AndroidRPG.Direction;


public class GraphicsEngine {
	private HashMap<Integer, Character> characters;
	private TiledTextureRegion textureRegion;
	private Scene scene;
	
	public GraphicsEngine(Scene scene, TiledTextureRegion charactersTextureRegion) {
		characters = new HashMap<Integer, Character>();
		this.textureRegion = charactersTextureRegion;
		this.scene = scene;
	}
	public void AddCharacter(int characterId, float xPos, float yPos, int modelId) {
		Character c = new Character(characterId, modelId, xPos, yPos, this.textureRegion);
		characters.put(characterId, c);
		this.scene.attachChild(c.getSprite());
	}
	public void MoveCharacter(int characterId, float xVelocity, float yVelocity) {
		Character c = characters.get(characterId);
		if (c == null)
			return;
		
		Direction direction = determineDirection(xVelocity, yVelocity);
		if (direction != c.getDirection()) {
			changeCharacterAnimation(characterId, direction);
			c.setDirection(direction);
		}
		
		c.move(xVelocity, yVelocity);
	}
	public void RemoveCharacter(int characterId) {
		Character c = characters.remove(characterId);
		this.scene.detachChild(c.getSprite());
	}
	public void changeCharacterAnimation(int characterId, Direction direction) {
		Character c = characters.get(characterId);
		if (c == null)
			return;
		
		if (direction == null) {
			c.getSprite().stopAnimation();
			return;
		}
		
		switch (direction) {
			case NORTH:
				c.getSprite().animate(new long[]{200, 200, 200}, 0, 2, true);
				break;
			case EAST:
				c.getSprite().animate(new long[]{200, 200, 200}, 3, 5, true);
				break;
			case SOUTH:
				c.getSprite().animate(new long[]{200, 200, 200}, 6, 8, true);
				break;
			case WEST:
				c.getSprite().animate(new long[]{200, 200, 200}, 9, 11, true);
				break;
		}
	}
	
	public void redrawAllCharacters() {
		@SuppressWarnings("unchecked")
		HashMap<Integer, Character> temp = (HashMap<Integer, Character>)characters.clone();
		for (Character c : temp.values()) {
			this.RemoveCharacter(c.getCharacterId()); 
			this.AddCharacter(c.getCharacterId(), c.getXPos(), c.getYPos(), c.getModelId());
		}
	}
	
	private Direction determineDirection(float xVelocity, float yVelocity) {
		if (xVelocity != 0) {
			if (xVelocity > 0)
				return Direction.EAST;
			else 
				return Direction.WEST;
		} else if (yVelocity != 0) {
			if (yVelocity > 0)
				return Direction.SOUTH;
			else 
				return Direction.NORTH;
		}
		return null;
	}
}
