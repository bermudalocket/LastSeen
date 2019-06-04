package com.bermudalocket.lastseen;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

public class DataStorage<T> {

    private final YamlConfiguration _yaml;

    private final File _file;

    // ------------------------------------------------------------------------
    /**
     * Constructor.
     *
     * @param id the string identifier for this store.
     * @param path the plugin path.
     */
    DataStorage(String id, String path) {
        _file = new File(path + "/" + id + ".yml");
        if (!_file.exists()) {
            try {
                _file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        _yaml = YamlConfiguration.loadConfiguration(_file);
    }

    // ------------------------------------------------------------------------
    /**
     * Returns (async) the data associated with the given key. If debug is true,
     * the request will be timed and result reported via console.
     *
     * @param key the YAML key.
     * @param debug debug state.
     * @return (async) the data associated with the given key.
     */
    @SuppressWarnings("unchecked")
    CompletableFuture<T> getData(String key, boolean debug) {
        if (debug) {
            return getDataDebug(key);
        }
        return CompletableFuture.supplyAsync(() -> (T) _yaml.get(key.toLowerCase(), 0));
    }

    // ------------------------------------------------------------------------
    /**
     * Returns (async) the data associated with the given key and reports timing
     * results to console.
     *
     * @param key the YAML key.
     * @return (async) the data associated with the given key.
     */
    @SuppressWarnings("unchecked")
    CompletableFuture<T> getDataDebug(String key) {
        long start = System.currentTimeMillis();
        return CompletableFuture.supplyAsync(() -> {
            Object o = _yaml.get(key, 0L);
            long diff = System.currentTimeMillis() - start;
            System.out.println("[LastSeen] [DEBUG] Completed " + diff + " ms later.");
            return (T) o;
        });
    }

    // ------------------------------------------------------------------------
    /**
     * Sets the data at the given key.
     *
     * @param key the key.
     * @param value the value.
     * @return (async) a CompletableFuture.
     */
    CompletableFuture<Void> setData(String key, T value) {
        return CompletableFuture.runAsync(() -> {
            _yaml.set(key.toLowerCase(), value);
            try {
                _yaml.save(_file);
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
