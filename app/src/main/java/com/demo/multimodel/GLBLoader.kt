package com.demo.multimodel

import com.google.android.filament.Engine
import com.google.android.filament.EntityManager
import com.google.android.filament.gltfio.AssetLoader
import com.google.android.filament.gltfio.FilamentAsset
import com.google.android.filament.gltfio.ResourceLoader
import com.google.android.filament.gltfio.UbershaderLoader
import java.nio.Buffer

class GLBLoader(
    val engine: Engine,
    val assetLoader: AssetLoader = AssetLoader(
        engine,
        UbershaderLoader(engine),
        EntityManager.get()
    )
) {

    private val resourceLoader: ResourceLoader =
        ResourceLoader(engine, true, false, false)

    fun load(buffer: Buffer): FilamentAsset? {
        val asset = assetLoader.createAssetFromBinary(buffer)

        asset ?: return null

        resourceLoader.loadResources(asset)
        val animator = asset.animator
        asset.releaseSourceData()
        return asset
    }


    fun destroyModel(filamentAsset: FilamentAsset) {
        resourceLoader.evictResourceData()
        assetLoader.destroyAsset(filamentAsset)
    }

    fun destroy() {
        resourceLoader.destroy()
    }
}