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
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.Texture.TextureFilter;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.CircleShape;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

public class Pidgin implements ApplicationListener {

	/**
	 * The time the last frame was rendered, used for throttling framerate
	 */
	private long lastRender;

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
	public static final float PIXELS_PER_METER = 60.0f;

	/**
	 * The screen's width and height. This may not match that computed by libgdx's gdx.graphics.getWidth() / getHeight()
	 * on devices that make use of on-screen menu buttons.
	 */
	private int screenWidth;
	private int screenHeight;
	boolean constantMove = true;

	private final Log log;

	private final PidginConfig config;

	private BitmapFont font;

	private float jumpVelocity;

	private float gravity;

	private float pidginWidth;

	private float pidginHeight;

	private float pidginDensity;

	public Pidgin(Log log, PidginConfig config) {
		super();
		this.log = log;
		this.config = config;

		// Defer until create() when Gdx is initialized.
		screenWidth = -1;
		screenHeight = -1;
	}

	public Pidgin(int width, int height, Log log, PidginConfig config) {
		super();

		screenWidth = width;
		screenHeight = height;
		this.log = log;
		this.config = config;
	}

	@Override
	public void create() {
		/**
		 * If the viewport's size is not yet known, determine it here.
		 */
		if (screenWidth == -1) {
			screenWidth = Gdx.graphics.getWidth();
			screenHeight = Gdx.graphics.getHeight();
		}

		tiledMapHelper = new TiledMapHelper();

		tiledMapHelper.setPackerDirectory("data/images");

		tiledMapHelper.loadMap("data/world/level.tmx");

		tiledMapHelper.prepareCamera(screenWidth, screenHeight);

		/**
		 * Load up the overall texture and chop it in to pieces. In this case, piece.
		 */
		overallTexture = new Texture(Gdx.files.internal("data/pidgin.png"));
		overallTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		crouchTexture = new Texture(Gdx.files.internal("data/pidgin-crouch.png"));
		crouchTexture.setFilter(TextureFilter.Linear, TextureFilter.Linear);

		jumperSprite = normalSprite = new Sprite(overallTexture, 0, 0, 21, 37);
		crouchSprite = new Sprite(crouchTexture, 0, 0, 21, 18);

		spriteBatch = new SpriteBatch();

		/**
		 * You can set the world's gravity in its constructor. Here, the gravity is negative in the y direction (as in,
		 * pulling things down).
		 */
		world = new World(new Vector2(0.0f, -10.0f), true);

		BodyDef jumperBodyDef = new BodyDef();
		jumperBodyDef.type = BodyDef.BodyType.DynamicBody;
		jumperBodyDef.position.set(1.0f, 5.0f);
		//						jumperBodyDef.position.set((tiledMapHelper.getWidth() - 200) / PIXELS_PER_METER - 1f, (tiledMapHelper.getHeight() - 64) / PIXELS_PER_METER);

		jumper = world.createBody(jumperBodyDef);

		/**
		 * Boxes are defined by their "half width" and "half height", hence the 2 multiplier.
		 */
		PolygonShape jumperShape = new PolygonShape();
		pidginWidth = jumperSprite.getWidth() / (2 * PIXELS_PER_METER);
		pidginHeight = jumperSprite.getHeight() / (2 * PIXELS_PER_METER);
		jumperShape.setAsBox(pidginWidth, pidginHeight);

		/**
		 * The character should not ever spin around on impact.
		 */
		jumper.setFixedRotation(true);
		//		jumper.setBullet(true);

		/**
		 * The density and friction of the jumper were found experimentally. Play with the numbers and watch how the
		 * character moves faster or slower.
		 */
		FixtureDef jumperFixtureDef = new FixtureDef();
		jumperFixtureDef.shape = jumperShape;
		pidginDensity = 1f;
		jumperFixtureDef.density = pidginDensity;
		jumperFixtureDef.friction = 0f;
		jumperFixtureDef.restitution = 0f;

		jumper.createFixture(jumperFixtureDef);
		jumperShape.dispose();

		createBall();

		tiledMapHelper.loadCollisions("data/images/level.collision", world, PIXELS_PER_METER);

		debugRenderer = new Box2DDebugRenderer();

		lastRender = System.currentTimeMillis();

		font = new BitmapFont();

		jumpVelocity = 0.8f;
		gravity = world.getGravity().y;
	}

	private void createBall() {
		BodyDef ballBodyDef = new BodyDef();
		ballBodyDef.type = BodyDef.BodyType.DynamicBody;
		ballBodyDef.position.set((tiledMapHelper.getWidth() - 320) / PIXELS_PER_METER, (tiledMapHelper.getHeight() - 64) / PIXELS_PER_METER);
		Body ball = world.createBody(ballBodyDef);

		CircleShape ballShape = new CircleShape();
		ballShape.setRadius(0.2f);

		FixtureDef ballFixtureDef = new FixtureDef();
		ballFixtureDef.shape = ballShape;
		ballFixtureDef.density = 0.1f;
		ballFixtureDef.friction = 0.1f;
		ballFixtureDef.restitution = 0.8f;

		ball.createFixture(ballFixtureDef);
		ballShape.dispose();
	}

	@Override
	public void resume() {
	}

	@Override
	public void render() {
		long now = System.currentTimeMillis();

		boolean doJump = false;
		boolean doCrouch = false;
		boolean moveRight = false;
		boolean moveLeft = false;

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
				if (Gdx.input.isTouched(i) && Gdx.input.getX(i) > Gdx.graphics.getWidth() * 0.8f) {
					doJump = true;
				}
			}
		}
		if (Gdx.input.isKeyPressed(Input.Keys.DPAD_DOWN)) {
			doCrouch = true;
		} else {
			for (int i = 0; i < 2; i++) {
				if (Gdx.input.isTouched(i) && Gdx.input.getX(i) < Gdx.graphics.getHeight() * 0.2f) {
					doCrouch = true;
				}
			}
		}
		//		if (Gdx.input.isKeyPressed(Input.Keys.R)) {
		//			reset();
		//		}
		if (Gdx.input.isKeyPressed(Input.Keys.A)) {
			backoff(1f, 0f);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.W)) {
			backoff(0f, -0.4f);
		}
		if (Gdx.input.isKeyPressed(Input.Keys.M)) {
			constantMove = !constantMove;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.E)) {
			gravity = gravity - 0.1f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.S)) {
			gravity = gravity + 0.1f;
			if (gravity > 0) gravity = 0;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.D)) {
			jumpVelocity = jumpVelocity - 0.01f;
			if (jumpVelocity < 0) jumpVelocity = 0;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.R)) {
			jumpVelocity = jumpVelocity + 0.01f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.F)) {
			pidginWidth = pidginWidth - 0.01f;
			if (pidginWidth < 0) pidginWidth = 0;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.T)) {
			pidginWidth = pidginWidth + 0.01f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.G)) {
			pidginHeight = pidginHeight - 0.01f;
			if (pidginHeight < 0) pidginHeight = 0;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.Y)) {
			pidginHeight = pidginHeight + 0.01f;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.H)) {
			pidginDensity = pidginDensity - 0.01f;
			if (pidginDensity < 0) pidginDensity = 0;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.U)) {
			pidginDensity = pidginDensity + 0.01f;
		}
		if (moveRight) {
			jumper.applyLinearImpulse(new Vector2(0.05f, 0.0f), jumper.getWorldCenter());
			if (jumperFacingRight == false) {
				jumperSprite.flip(true, false);
			}
			jumperFacingRight = true;
		} else if (moveLeft) {
			jumper.applyLinearImpulse(new Vector2(-0.05f, 0.0f), jumper.getWorldCenter());
			if (jumperFacingRight == true) {
				jumperSprite.flip(true, false);
			}
			jumperFacingRight = false;
		}

		if (doCrouch) {
			((PolygonShape) jumper.getFixtureList().get(0).getShape()).setAsBox(pidginWidth, pidginHeight / 2);
			jumperSprite = crouchSprite;
		} else {
			((PolygonShape) jumper.getFixtureList().get(0).getShape()).setAsBox(pidginWidth, pidginHeight);
			jumper.resetMassData();
			jumperSprite = normalSprite;
		}

		/**
		 * The jumper dude can only jump while on the ground. There are better ways to detect ground contact, but for
		 * our purposes it is sufficient to test that the vertical velocity is zero (or close to it). As in the above
		 * code, the vertical figure here was found through experimentation. It's enough to get the guy off the ground.
		 * As before, impulse is applied to the center of the jumper.
		 */
		if (doJump && Math.abs(jumper.getLinearVelocity().y) < 1e-4) {
			jumper.applyLinearImpulse(new Vector2(0.0f, jumpVelocity), jumper.getWorldCenter());
		}

		if (constantMove) {
			if (jumper.getLinearVelocity().x < 5f) jumper.applyForceToCenter(5f - jumper.getLinearVelocity().x, 0);
		}

		/**
		 * Have box2d update the positions and velocities (and etc) of all tracked objects. The second and third
		 * argument specify the number of iterations of velocity and position tests to perform -- higher is more
		 * accurate but is also slower.
		 */
		world.step(Gdx.app.getGraphics().getDeltaTime(), 3, 3);

		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
		/**
		 * A nice(?), blue backdrop.
		 */
		Gdx.gl.glClearColor(0, 0.5f, 0.9f, 0);

		/**
		 * The camera is now controlled primarily by the position of the main character, and secondarily by the map
		 * boundaries.
		 */

		tiledMapHelper.getCamera().position.x = PIXELS_PER_METER * jumper.getPosition().x;
		tiledMapHelper.getCamera().position.y = PIXELS_PER_METER * jumper.getPosition().y;

		/**
		 * Ensure that the camera is only showing the map, nothing outside.
		 */
		if (tiledMapHelper.getCamera().position.x < Gdx.graphics.getWidth() / 2) {
			tiledMapHelper.getCamera().position.x = Gdx.graphics.getWidth() / 2;
		}
		if (tiledMapHelper.getCamera().position.x >= tiledMapHelper.getWidth() - Gdx.graphics.getWidth() / 2) {
			tiledMapHelper.getCamera().position.x = tiledMapHelper.getWidth() - Gdx.graphics.getWidth() / 2;
		}

		if (tiledMapHelper.getCamera().position.y < Gdx.graphics.getHeight() / 2) {
			tiledMapHelper.getCamera().position.y = Gdx.graphics.getHeight() / 2;
		}
		if (tiledMapHelper.getCamera().position.y >= tiledMapHelper.getHeight() - Gdx.graphics.getHeight() / 2) {
			tiledMapHelper.getCamera().position.y = tiledMapHelper.getHeight() - Gdx.graphics.getHeight() / 2;
		}

		if (jumper.getPosition().x * PIXELS_PER_METER > tiledMapHelper.getWidth() - 30) reset();

		tiledMapHelper.getCamera().update();
		tiledMapHelper.render();

		/**
		 * Prepare the SpriteBatch for drawing.
		 */
		spriteBatch.setProjectionMatrix(tiledMapHelper.getCamera().combined);
		spriteBatch.begin();

		jumperSprite.setPosition(PIXELS_PER_METER * jumper.getPosition().x - jumperSprite.getWidth() / 2, PIXELS_PER_METER * jumper.getPosition().y
				- jumperSprite.getHeight() / 2);
		jumperSprite.draw(spriteBatch);

		print(gravity, jumpVelocity);

		/**
		 * "Flush" the sprites to screen.
		 */
		spriteBatch.end();

		/**
		 * Draw this last, so we can see the collision boundaries on top of the sprites and map.
		 */
		debugRenderer.render(world, tiledMapHelper.getCamera().combined.scale(Pidgin.PIXELS_PER_METER, Pidgin.PIXELS_PER_METER, Pidgin.PIXELS_PER_METER));

		now = System.currentTimeMillis();
		if (now - lastRender < 30000000) { // 30 ms, ~33FPS
			try {
				Thread.sleep(30 - (now - lastRender) / 1000000);
			} catch (InterruptedException e) {}
		}

		lastRender = now;
	}

	private void print(float gravity, float jumpVelocity) {
		font.draw(spriteBatch, "gravitacia: " + -world.getGravity().y, tiledMapHelper.getCamera().position.x - Gdx.graphics.getWidth() / 2,
				tiledMapHelper.getCamera().position.y + Gdx.graphics.getHeight() / 2);
		if (world.getGravity().y != gravity) world.setGravity(new Vector2(0, gravity));
		font.draw(spriteBatch, "jump: " + jumpVelocity, tiledMapHelper.getCamera().position.x - Gdx.graphics.getWidth() / 2,
				tiledMapHelper.getCamera().position.y + Gdx.graphics.getHeight() / 2 - 20);

		Vector2 rozmery = new Vector2();
		((PolygonShape) jumper.getFixtureList().get(0).getShape()).getVertex(2, rozmery);

		font.draw(spriteBatch, "vyska vtaka: " + rozmery.y, tiledMapHelper.getCamera().position.x - Gdx.graphics.getWidth() / 2,
				tiledMapHelper.getCamera().position.y + Gdx.graphics.getHeight() / 2 - 40);
		font.draw(spriteBatch, "sirka vtaka: " + rozmery.x, tiledMapHelper.getCamera().position.x - Gdx.graphics.getWidth() / 2,
				tiledMapHelper.getCamera().position.y + Gdx.graphics.getHeight() / 2 - 60);

		float density = jumper.getFixtureList().get(0).getDensity();
		font.draw(spriteBatch, "objem vtaka: " + density, tiledMapHelper.getCamera().position.x - Gdx.graphics.getWidth() / 2,
				tiledMapHelper.getCamera().position.y + Gdx.graphics.getHeight() / 2 - 80);
		if (density != pidginDensity) jumper.getFixtureList().get(0).setDensity(pidginDensity);

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