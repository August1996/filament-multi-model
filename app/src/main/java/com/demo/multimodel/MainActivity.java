package com.demo.multimodel;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.util.Pair;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;

import com.google.android.filament.Camera;
import com.google.android.filament.Engine;
import com.google.android.filament.Renderer;
import com.google.android.filament.Scene;
import com.google.android.filament.SwapChain;
import com.google.android.filament.View;
import com.google.android.filament.Viewport;
import com.google.android.filament.gltfio.Animator;
import com.google.android.filament.gltfio.AssetLoader;
import com.google.android.filament.gltfio.FilamentAsset;
import com.google.android.filament.gltfio.Gltfio;
import com.google.android.filament.gltfio.UbershaderLoader;
import com.google.android.filament.utils.KTXLoader;
import com.google.android.filament.utils.KTXLoader.Options;
import com.google.android.filament.utils.Utils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    static {
        Gltfio.init();
        Utils.INSTANCE.init();

        System.loadLibrary("filament-utils-jni");
    }

    private Engine engine;
    private Scene scene;
    private View view;
    private Camera camera;
    private Renderer renderer;
    private SwapChain swapChain;

    private AssetLoader shareAssetLoader;

    private final List<Pair<GLBLoader, FilamentAsset>> assets = new ArrayList<>();

    private long animationStartTime;

    private final FrameDrawer frameDrawer = new FrameDrawer(this, aLong -> {
        SwapChain swapChain = MainActivity.this.swapChain;
        Renderer renderer = MainActivity.this.renderer;
        View view = MainActivity.this.view;
        if (swapChain != null & renderer != null && view != null) {
            updateAnimation(aLong);

            if (renderer.beginFrame(swapChain, aLong)) {
                renderer.render(view);
                renderer.endFrame();
            }
        }
        return null;
    });

    private void updateAnimation(Long aLong) {
        double time = (aLong - animationStartTime) / 1_000_000_000.0;

        for (Pair<GLBLoader, FilamentAsset> pair : assets) {
            FilamentAsset asset = pair.second;

            Animator animator = asset.getAnimator();

            animator.applyAnimation(0, (float) time);

            animator.updateBoneMatrices();

        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        initFilament();
        initEnv();

        TextureView textureView = findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surfaceTexture, int width,
                                                  int height) {
                swapChain = engine.createSwapChain(new Surface(surfaceTexture));
                updateFilamentViewport(width, height);
            }

            @Override
            public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surfaceTexture,
                                                    int width,
                                                    int height) {
                updateFilamentViewport(width, height);
            }

            @Override
            public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surfaceTexture) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surfaceTexture) {

            }
        });

        frameDrawer.init();
    }

    private void initEnv() {
        ByteBuffer iblBuffer = ByteBuffer.wrap(IOUtils.INSTANCE.readAsset(getAssets(),
                "venice_sunset_4k_ibl.ktx"));
        ByteBuffer skyboxBuffer = ByteBuffer.wrap(IOUtils.INSTANCE.readAsset(getAssets(),
                "venice_sunset_4k_skybox.ktx"));

        scene.setIndirectLight(KTXLoader.INSTANCE.createIndirectLight(engine, iblBuffer,
                new Options()));
        scene.setSkybox(KTXLoader.INSTANCE.createSkybox(engine, skyboxBuffer, new Options()));
    }

    private void initFilament() {
        engine = Engine.create();
        scene = engine.createScene();
        view = engine.createView();
        camera = engine.createCamera(engine.getEntityManager().create());
        renderer = engine.createRenderer();

        shareAssetLoader = new AssetLoader(engine, new UbershaderLoader(engine),
                engine.getEntityManager());

        view.setCamera(camera);
        view.setScene(scene);

        camera.lookAt(0, 1, 4,
                0, 0, 0,
                0, 1, 0);
    }

    private void updateFilamentViewport(int width, int height) {
        view.setViewport(new Viewport(0, 0, width, height));
        camera.setProjection(
                45.0,
                width * 1.0 / height,
                0.05,
                1000,
                Camera.Fov.VERTICAL
        );
    }

    public void addModels(android.view.View view) {
        if (assets.size() != 0) {
            return;
        }

        animationStartTime = System.nanoTime();

        ByteBuffer wolfBuffer = ByteBuffer.wrap(IOUtils.INSTANCE.readAsset(getAssets(), "model.glb" ));

        int size = 32;


        for (int i = 0; i < size; i++) {

            GLBLoader glbLoader = new GLBLoader(engine, shareAssetLoader);
            FilamentAsset asset = glbLoader.load(wolfBuffer);

            scene.addEntities(asset.getEntities());
            scene.addEntities(asset.getLightEntities());
            scene.addEntities(asset.getCameraEntities());

            assets.add(new Pair<>(glbLoader, asset));
        }


    }
}
