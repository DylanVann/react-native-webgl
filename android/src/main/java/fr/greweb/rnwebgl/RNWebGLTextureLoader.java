package fr.greweb.rnwebgl;

import android.util.Log;
import android.util.SparseArray;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReadableMap;

import java.util.ArrayList;
import java.util.List;

public class RNWebGLTextureLoader extends ReactContextBaseJavaModule {
    private List<RNWebGLTextureConfigLoader> mLoaders = null;
    private SparseArray<RNWebGLTexture> mObjects = new SparseArray<>();

    public RNWebGLTextureLoader(ReactApplicationContext reactContext) {
        super(reactContext);
    }

    @Override
    public String getName() {
        return "RNWebGLTextureLoader";
    }

    public RNWebGLTextureConfigLoader objectLoaderForConfig (final ReadableMap config) {
        if (mLoaders == null) {
            mLoaders = new ArrayList<>();
            for (NativeModule module: this.getReactApplicationContext().getCatalystInstance().getNativeModules()) {
                if (module instanceof RNWebGLTextureConfigLoader) {
                    mLoaders.add((RNWebGLTextureConfigLoader) module);
                }
            }
        }
        for (RNWebGLTextureConfigLoader loader : mLoaders) {
            if (loader.canLoadConfig(config)) {
                return loader;
            }
        }
        return null;
    }


    public void loadWithConfig (final ReadableMap config, final RNWebGLTextureCompletionBlock callback) {
        RNWebGLTextureConfigLoader loader = this.objectLoaderForConfig(config);
        if (loader == null) {
            Log.e("RNWebGL", "No suitable RNWebGLTextureLoader found for " + config);
            callback.call(new TextureLoaderNotFoundException(), null);
        }
        else {
            final RNWebGLTextureLoader self = this;
            loader.loadWithConfig(config, new RNWebGLTextureCompletionBlock() {
                public void call (Exception e, RNWebGLTexture obj) {
                    if (obj != null) {
                        self.mObjects.put(obj.objId, obj);
                    }
                    callback.call(e, obj);
                }
            });
        }
    }

    public void unloadWithObjId (int objId) {
        RNWebGLTexture obj = mObjects.get(objId);
        if (obj != null) {
            mObjects.remove(objId);
            obj.destroy();
        }
    }
}