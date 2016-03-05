package com.laloosh.textmuse.datamodel;


import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.laloosh.textmuse.app.Constants;
import com.laloosh.textmuse.R;
import com.laloosh.textmuse.datamodel.gson.GsonConverter;

import org.apache.commons.io.IOUtils;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DataPersistenceHelper {

    public static <T> boolean save (Context context, String filepath, T obj) {

        if (context == null) {
            Log.d(Constants.TAG, "Attempt to save data after context has been destroyed.");
            return false;
        }

        FileOutputStream fos = null;
        BufferedOutputStream bos = null;
        try {
            fos = context.openFileOutput(filepath, Context.MODE_PRIVATE);
            bos = new BufferedOutputStream(fos);

            Gson gson = GsonConverter.registerDateTime(new GsonBuilder()).create();
            String json = gson.toJson(obj);

            if (json != null && json.length() > 0) {
                bos.write(json.getBytes());
            } else {
                Log.e(Constants.TAG, "Could not convert object data to json for filepath " + filepath);
                return false;
            }
        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Problem opening file for saving data", e);
            return false;
        } catch (IOException e) {
            Log.e(Constants.TAG, "IOException when writing to file for saving data", e);
            return false;
        } finally {
            if (bos != null) {
                IOUtils.closeQuietly(bos);
            }
            if (fos != null) {
                IOUtils.closeQuietly(fos);
            }
        }

        return true;
    }

    public static <T> T load(Context context, String filepath, Class<T> classType) {

        if (context == null) {
            Log.d(Constants.TAG, "Attempt to load data with no context.");
            return null;
        }

        T convertedData = null;
        StringBuilder data = new StringBuilder();
        InputStreamReader isr = null;
        BufferedReader buffreader = null;

        try {
            FileInputStream fis = context.openFileInput(filepath);
            isr = new InputStreamReader(fis);
            buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                data.append(readString);
                readString = buffreader.readLine();
            }

        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Could not open cached data file", e);
            return null;
        } catch (IOException e) {
            Log.e(Constants.TAG, "IOException reading cache file", e);
            return null;
        } finally {
            if (buffreader != null) {
                IOUtils.closeQuietly(buffreader);
            }
            if (isr != null) {
                IOUtils.closeQuietly(isr);
            }
        }

        String jsonData = data.toString();
        Gson gson = GsonConverter.registerDateTime(new GsonBuilder()).create();

        try {
            convertedData = gson.fromJson(jsonData, classType);
        } catch (Exception e) {
            Log.e(Constants.TAG, "Could not read in text muse cache data ", e);
            return null;
        }

        return convertedData;

    }

    public static String loadRawContent(Context context) {
        if (context == null) {
            Log.d(Constants.TAG, "Attempt to load raw content with no context");
            return null;
        }

        StringBuilder data = new StringBuilder();
        InputStreamReader isr = null;
        BufferedReader buffreader = null;

        try {
            InputStream is = context.getResources().openRawResource(R.raw.notes_original_fallback);
            isr = new InputStreamReader(is);
            buffreader = new BufferedReader(isr);

            String readString = buffreader.readLine();
            while (readString != null) {
                data.append(readString);
                readString = buffreader.readLine();
            }

        } catch (FileNotFoundException e) {
            Log.e(Constants.TAG, "Could not open cached data file", e);
            return null;
        } catch (IOException e) {
            Log.e(Constants.TAG, "IOException reading cache file", e);
            return null;
        } finally {
            if (buffreader != null) {
                IOUtils.closeQuietly(buffreader);
            }
            if (isr != null) {
                IOUtils.closeQuietly(isr);
            }
        }

        return data.toString();
    }
}
