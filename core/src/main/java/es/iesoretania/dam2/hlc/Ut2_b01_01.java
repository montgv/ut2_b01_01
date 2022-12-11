package es.iesoretania.dam2.hlc;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

/** {@link com.badlogic.gdx.ApplicationListener} implementation shared by all platforms. */
public class Ut2_b01_01 extends ApplicationAdapter {
	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;

	private OrthographicCamera camera;
	private SpriteBatch batch;

	private Rectangle bucket;

	private Array<Rectangle> raindrops;
	private long lastDropTime;

	@Override
	public void create() {
		// carga las imágenes para la gota y el cubo, 64x64 píxeles cada uno
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// carga el efecto de sonido de caída y el fondo de lluvia "música"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// iniciar la reproducción de la música de fondo de inmediato
		rainMusic.setLooping(true);
		rainMusic.play();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, 800, 480);

		batch = new SpriteBatch();

		bucket = new Rectangle();
		bucket.width = 64;
		bucket.height = 64;
		bucket.x = (800 - bucket.width) / 2;
		bucket.y = 20;

		raindrops = new Array<Rectangle>();
		spawnRaindrop();
	}

	@Override
	public void render() {
		// borrar la pantalla con un color azul oscuro. Los argumentos.
		// a glClearColor son el componente rojo, verde.
		// azul y alfa en el rango [0,1].
		// del color que se utilizará para borrar la pantalla.
		Gdx.gl.glClearColor(0, 0, 0.2f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		// dile a la cámara que actualice sus matrices.
		camera.update();

		// dile al SpriteBatch que haga en el
		// sistema de coordenadas especificado por la cámara.
		batch.setProjectionMatrix(camera.combined);

		// comenzar un nuevo lote y dibujar el cubo y
		// todas las gotas
		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);
		for (Rectangle raindrop : raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
		batch.end();

		// entrada del usuario del proceso
		if (Gdx.input.isTouched()) {
			Vector3 touchPos = new Vector3();
			touchPos.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPos);
			bucket.x = touchPos.x - bucket.width / 2;
		}
		if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= 400 * Gdx.graphics.getDeltaTime();
		if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += 400 * Gdx.graphics.getDeltaTime();

		// asegúrese de que el cubo se mantiene dentro de los límites de la pantalla
		//aqui tengo que cambiar para que salga un mensaje diciendo que ha perdido
		if (bucket.x < 0) bucket.x = 0;
		if (bucket.x > 800 - bucket.width) bucket.x = 800 - bucket.width;

		// comprobar si necesitamos crear una nueva gota de lluvia
		if (TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();

		// mueva las gotas de lluvia, quite cualquier que esté debajo del borde inferior de
		// la pantalla o que golpeó el cubo. En este último caso, reproducimos
		// un efecto de sonido también.
		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
			if (raindrop.y + 64 < 0) iter.remove();
			if (raindrop.overlaps(bucket)) {
				dropSound.play();
				iter.remove();
			}
		}
	}

	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, 800 - raindrop.width);
		raindrop.y = 480;
		raindrop.width = 64;
		raindrop.height = 64;
		raindrops.add(raindrop);
		lastDropTime = TimeUtils.nanoTime();
	}
}