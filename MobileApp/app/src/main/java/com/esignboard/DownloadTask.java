package com.esignboard;

import android.content.Context;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.webkit.URLUtil;
import android.widget.Toast;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import net.lingala.zip4j.exception.ZipException;
import net.lingala.zip4j.core.ZipFile;

import com.esignboard.R;

// Klasa zajmujaca sie pobieraniem i wypakowywaniem
class DownloadTask extends AsyncTask<String, Integer, String> {

    private Context context;
    private PowerManager.WakeLock mWakeLock;
    private String path = null;

    DownloadTask(MainActivity context) {
        this.context = context;
    }

    @Override
    protected String doInBackground(String... sUrl) {
        InputStream input = null;
        OutputStream output = null;
        HttpURLConnection connection = null;
        try {
            URL url = new URL(sUrl[0]);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();

            // expect HTTP 200 OK, so we don't mistakenly save error report
            // instead of the file
            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                return "Server returned HTTP " + connection.getResponseCode()
                        + " " + connection.getResponseMessage();
            }

            // download the file
            input = connection.getInputStream();
            path = context.getFilesDir().toString() + "/" + URLUtil.guessFileName(url.toString(), null, null);
            output = new FileOutputStream(path);
            byte data[] = new byte[4096];

            int count;
            while ((count = input.read(data)) != -1) {
                output.write(data, 0, count);
            }
        } catch (Exception e) {
            return e.toString();
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }

            if (connection != null)
                connection.disconnect();
        }
        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        // take CPU lock to prevent CPU from going off if the user
        // presses the power button during download
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                getClass().getName());
        mWakeLock.acquire();
    }

    @Override
    protected void onPostExecute(String result) {
        mWakeLock.release();
        if (result != null) {
            Toast.makeText(context, "Download error: " + result,
                    Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(context, path,
                    Toast.LENGTH_LONG).show();
            if (path.equals(context.getFilesDir().toString() + "/esignboard_data.zip")) {
                unzip();
            }
        }
    }



    protected void unzip() {
        String source = context.getFilesDir().toString() + "/esignboard_data.zip";
        String destination = context.getFilesDir().toString();

        try {
            ZipFile zipFile = new ZipFile(source);

            zipFile.extractAll(destination);
        } catch (ZipException e) {
            e.printStackTrace();
        }
    }


}
