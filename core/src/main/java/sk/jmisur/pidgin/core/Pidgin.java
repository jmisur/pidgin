package sk.jmisur.pidgin.core;

/**
 * Copyright 2011 David Kirchner dpk@dpk.net Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions and limitations under the
 * License. Source: http://dpk.net/2011/05/08/libgdx-box2d-tiled-maps-full-working-example-part-2/
 */

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Pidgin implements ApplicationListener {

	private TiledMapHelper tiledMapHelper;

	/**
	 * Holder of the texture for the various non-map sprites the game will have.
	 */
	private Texture overallTexture;
	private Texture crouchTexture;

	/**
	 * As the name implies, this is the sprite for the jumper character. The boolean is just to track which direction
	 * the jumper is facing. There are better ways to handle this, but in a real game you would handle the character
	 * sprite a lot differently (with animations and all that) so let's call that outside the scope of this example.
	 */
	private Sprite jumperSprite;
	private Sprite normalSprite;
	private Sprite crouchSprite;
	private boolean jumperFacingRight = true;

	/**
	 * The libgdx SpriteBatch -- used to optimize sprite drawing.
	 */
	private SpriteBatch spriteBatch;

	/**
	 * This is the main box2d "container" object. All bodies will be loaded in this object and will be simulated through
	 * calls to this object.
	 */
	private World world;

	/**
	 * This is the player character. It will be created as a dynamic object.
	 */
	private Body jumper;

	/**
	 * This box2d debug renderer comes from libgdx test code. It draws lines over all collision boundaries, so it is
	 * immensely useful for verifying that the world collisions are as you expect them to be. It is, however, slow, so
	 * only use it for testing.
	 */
	private Box2DDebugRenderer debugRenderer;

	/**
	 * Box2d works best with small values. If you use pixels directly you will get weird results -- speeds and
	 * accelerations not feeling quite right. Common practice is to use a constant to convert pixels to and from
	 * "meters".
	 */
	public static float PIXELS_PER_METER;

	/**
	 * The screen's width and height. This may not match that computed by libgdx's gdx.graphics.getWidth() / getHeight()
	 * on devices that make use of on-screen menu buttons.
	 */
	private int screenWidth;
	private int screenHeight;
	boolean constantMove = true;

	private final Log log;

	private float jumpVelocity;

	private float gravity;

	private float pidginWidth;

	private float pidginHeight;

	private boolean doJump;

	private boolean doCrouch;

	private boolean moveRight;

	private boolean moveLeft;

	private float moveVelocity;

	private final String map;

	public Pidgin(Log log) {
		super();
		this.log = log;
		this.map = "ulica";

		// Defer until create() when Gdx is initialized.
		screenWidth = -1;
		screenHeight = -1;
	}

	private OrthographicCamera camera;

	@Override
	public void create() {
		screenWidth = Gdx.graphics.getWidth();
		screenHeight = Gdx.graphics.getHeight();

		PIXELS_PER_METER = 60f;

		camera = new OrthographicCamera(screenWidth, screenHeight);
		camera.position.set(0, 0, 0);
		camera.zoom = 1920f / screenWidth;

		tiledMapHelper = new TiledMapHelper();
		tiledMapHelper.setPackerDirectory("data/maps/");
		tiledMapHelper.loadMap("data/maps/" + map + ".tmx", 1f);
		tiledMapHelper.getRenderer().setView(camera);

		overallTexture = new Texture(Gdx.files.internal("data/images/characters/pidgin/pidgin.png"));
		overallTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		crouchTexture = new Texture(Gdx.files.internal("data/images/characters/pidgin/pidgin-crouch.png"));
		crouchTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		pidginWidth = 3.2f; // 192
		pidginHeight = 4.8f; // 288

		jumperSprite = normalSprite = new Sprite(overallTexture, 0, 0, (int) (pidginWidth * PIXELS_PER_METER), (int) (pidginHeight * PIXELS_PER_METER));
		crouchSprite = new Sprite(crouchTexture, 0, 0, (int) (pidginWidth * PIXELS_PER_METER), (int) (pidginHeight / 2 * PIXELS_PER_METER));

		spriteBatch = new SpriteBatch();

		gravity = -30f;
		world = new World(new Vector2(0.0f, gravity), true);

		BodyDef jumperBodyDef = new BodyDef();
		jumperBodyDef.type = BodyDef.BodyType.DynamicBody;
		jumperBodyDef.position.set(1.0f, 30.0f);

		jumper = world.createBody(jumperBodyDef);

		PolygonShape jumperShape = new PolygonShape();
		jumperShape.setAsBox(pidginWidth / 2, pidginHeight / 2);

		jumper.setFixedRotation(true);

		FixtureDef jumperFixtureDef = new FixtureDef();
		jumperFixtureDef.shape = jumperShape;
		jumperFixtureDef.density = 0.1f;
		jumperFixtureDef.friction = 0;
		jumperFixtureDef.restitution = 0;

		jumper.createFixture(jumperFixtureDef);
		jumperShape.dispose();

		tiledMapHelper.loadCollisions("data/images/tiles/" + map + ".collision", world, PIXELS_PER_METER);

		debugRenderer = new Box2DDebugRenderer();

		jumpVelocity = 25;
		moveVelocity = 30f;
	}

	@Override
	public void resume() {
	}

	@Override
	public void render() {
		doJump = false;
		doCrouch = false;
		moveRight = false;
		moveLeft = false;

		getInput();
		doMoves();

		world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

		Gdx.gl.glClearColor(0, 0.5f, 0.9f, 0);
		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);

		controlCamera();

		camera.update();
		tiledMapHelper.getRenderer().setView(camera);
		tiledMapHelper.getRenderer().render();

		spriteBatch.setProjectionMatrix(camera.combined);
		spriteBatch.begin();

		if (jumper.getPosition().x * PIXELS_PER_METER > tiledMapHelper.getWidth() - jumperSprite.getWidth()) {
			jumper.setTransform(1.0f, 30.0f, 0f);
		}

		jumperSprite.setPosition(PIXELS_PER_METER * jumper.getPosition().x - jumperSprite.getWidth() / 2, PIXELS_PER_METER * jumper.getPosition().y
				- jumperSprite.getHeight() / 2);
		jumperSprite.draw(spriteBatch);

		spriteBatch.end();
		debugRenderer.render(world, camera.combined.scale(Pidgin.PIXELS_PER_METER, Pidgin.PIXELS_PER_METER, Pidgin.PIXELS_PER_METER));
	}

	private void getInput() {
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_RIGHT)) {
			moveRight = true;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_LEFT)) {
			moveLeft = true;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_UP)) {
			doJump = true;
		} else {
			for (int i = 0; i < 2; i++) {
				if (Gdx.input.isTouched(i) && Gdx.input.getX(i) > screenWidth * 0.8f) {
					doJump = true;
				}
			}
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN)) {
			doCrouch = true;
		} else {
			for (int i = 0; i < 2; i++) {
				if (Gdx.input.isTouched(i) && Gdx.input.getX(i) < screenHeight * 0.2f) {
					doCrouch = true;
				}
			}
		}
		if (Gdx.input.isKeyPressed(Input.Keys.Q)) {
			reset();
		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			backoff(2f, 0f);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			backoff(0f, -2f);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.M)) {
			constantMove = !constantMove;
			jumper.setLinearVelocity(0f, 0f);
		}

	}

	private void doMoves() {
		if (!constantMove && moveRight) {
			jumper.applyLinearImpulse(new Vector2(0.05f, 0.0f), jumper.getWorldCenter(), true);
			if (jumperFacingRight == false) {
				jumperSprite.flip(true, false);
			}
			jumperFacingRight = true;
		} else if (!constantMove && moveLeft) {
			jumper.applyLinearImpulse(new Vector2(-0.05f, 0.0f), jumper.getWorldCenter(), true);
			if (jumperFacingRight == true) {
				jumperSprite.flip(true, false);
			}
			jumperFacingRight = false;
		}

		if (doCrouch) {
			jumper.getFixtureList().get(0).setDensity(0.25f);
			((PolygonShape) jumper.getFixtureList().get(0).getShape()).setAsBox(pidginWidth / 2, pidginHeight / 4);
			jumperSprite = crouchSprite;
		} else {
			jumper.getFixtureList().get(0).setDensity(0.1f);
			((PolygonShape) jumper.getFixtureList().get(0).getShape()).setAsBox(pidginWidth / 2, pidginHeight / 2);
			jumperSprite = normalSprite;
		}
		jumper.resetMassData();

		if (doJump && Math.abs(jumper.getLinearVelocity().y) < 1e-4) {
			jumper.applyLinearImpulse(new Vector2(0.0f, jumpVelocity), jumper.getWorldCenter(), true);
		}

		if (constantMove) {
			if (jumper.getLinearVelocity().x < moveVelocity) jumper.applyForceToCenter(moveVelocity, 0, true);
		}
		//		if (jumper.getLinearVelocity().x > 1f) camera.zoom = jumper.getLinearVelocity().x;
	}

	private void controlCamera() {
		/**
		 * The camera is now controlled primarily by the position of the main character, and secondarily by the map
		 * boundaries.
		 */
		camera.position.x = PIXELS_PER_METER * (jumper.getPosition().x - pidginWidth * 1.5f) + screenWidth / 2 * camera.zoom;
		camera.position.y = PIXELS_PER_METER * jumper.getPosition().y;

		/**
		 * Ensure that the camera is only showing the map, nothing outside.
		 */
		if (camera.position.x < screenWidth / 2 * camera.zoom) {
			camera.position.x = screenWidth / 2 * camera.zoom;
		}
		if (camera.position.x >= tiledMapHelper.getWidth() - screenWidth / 2 * camera.zoom) {
			camera.position.x = tiledMapHelper.getWidth() - screenWidth / 2 * camera.zoom;
		}

		if (camera.position.y < screenHeight / 2 * camera.zoom) {
			camera.position.y = screenHeight / 2 * camera.zoom;
		}
		if (camera.position.y >= tiledMapHelper.getHeight() - screenHeight / 2 * camera.zoom) {
			camera.position.y = tiledMapHelper.getHeight() - screenHeight / 2 * camera.zoom;
		}
	}

	private void reset() {
		jumper.setTransform(1f, 5f, 0);
	}

	private void backoff(float x, float y) {
		float newX = jumper.getPosition().x - x;
		if (newX * PIXELS_PER_METER < 1f) newX = 1f;
		float newY = jumper.getPosition().y - y;
		if (newY * PIXELS_PER_METER > tiledMapHelper.getHeight()) newY = (tiledMapHelper.getHeight() - 32) / PIXELS_PER_METER;
		jumper.setTransform(newX, newY, 0);
	}

	@Override
	public void resize(int width, int height) {
	}

	@Override
	public void pause() {
	}

	@Override
	public void dispose() {
	}
}