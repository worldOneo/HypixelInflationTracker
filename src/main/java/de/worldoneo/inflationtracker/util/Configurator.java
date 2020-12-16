package de.worldoneo.inflationtracker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;

public class Configurator {
    public static Gson gson = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static <T> T loadJson(File file, Class<T> classOfT) {
        try (InputStream inputStream = new FileInputStream(file);
             Reader reader = new InputStreamReader(inputStream)) {
            return gson.fromJson(reader, classOfT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T loadJson(File file, Class<T> classOfT, Object defaultData) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            saveJson(file, defaultData);
        }
        return loadJson(file, classOfT);
    }

    public static void saveJson(File file, Object dataObject) throws IOException {
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        String data = gson.toJson(dataObject);
        Files.write(file.toPath(), data.getBytes());
    }
}
