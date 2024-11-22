package dev.eduardoroth.mediaplayer.utilities;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;

import androidx.mediarouter.app.MediaRouteButton;
import androidx.mediarouter.media.MediaRouter;

import com.google.android.gms.cast.framework.CastContext;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class CastServiceHelpers {

    private final Context context;
    private CastContext castContext;

    public CastServiceHelpers(Context context) {
        this.context = context;
    }

    /*
    /**
     * Sets the cast image in playerView when it is connected to a cast device
     *
    private class setCastImage extends AsyncTask<Void, Void, Bitmap> {

        protected Bitmap doInBackground(Void... params) {
            final String image = artwork;
            if (image != "") {
                try {
                    URL url = new URL(image);
                    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                    connection.setDoInput(true);
                    connection.connect();
                    InputStream input = connection.getInputStream();
                    Bitmap myBitmap = BitmapFactory.decodeStream(input);
                    return myBitmap;
                } catch (IOException e) {
                    e.printStackTrace();
                    return null;
                }
            } else {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap result) {
            cast_image.setImageBitmap(result);
        }
    }

    private final class EmptyCallback extends MediaRouter.Callback {
    }*/
}