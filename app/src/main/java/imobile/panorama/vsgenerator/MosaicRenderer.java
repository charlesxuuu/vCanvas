package imobile.panorama.vsgenerator;

/**
 * The Java interface to JNI calls regarding mosaic preview rendering.
 *
 */
public class MosaicRenderer
{
     static
     {
         System.loadLibrary("jni_mosaic");
     }

     /**
      * Function to be called in onSurfaceCreated() to initialize
      * the GL context, load and link the shaders and create the
      * program. Returns a texture ID to be used for SurfaceTexture.
      *
      * @return textureID the texture ID of the newly generated texture to
      *          be assigned to the SurfaceTexture object.
      */
     public static native int init();

     /**
      * Pass the drawing surface's width and height to initialize the
      * renderer viewports and FBO dimensions.
      *
      * @param width width of the drawing surface in pixels.
      * @param height height of the drawing surface in pixels.
      * @param isLandscapeOrientation is the orientation of the activity layout in landscape.
      */
     public static native void reset(int width, int height, boolean isLandscapeOrientation);

     /**
      * Calling this function will render the SurfaceTexture to a new 2D texture
      * using the provided STMatrix.
      *
      * @param stMatrix texture coordinate transform matrix obtained from the
      *        Surface texture
      */
     public static native void preprocess(float[] stMatrix);

     /**
      * This function calls glReadPixels to transfer both the low-res and high-res
      * data from the GPU memory to the CPU memory for further processing by the
      * mosaicing library.
      */
     public static native void transferGPUtoCPU();

     /**
      * Function to be called in onDrawFrame() to update the screen with
      * the new frame data.
      */
     public static native void step();

     /**
      * Call this function when a new low-res frame has been processed by
      * the mosaicing library. This will tell the renderer library to
      * update its texture and warping transformation. Any calls to step()
      * after this call will use the new image frame and transformation data.
      */
     public static native void ready();

     /**
      * This function allows toggling between showing the input image data
      * (without applying any warp) and the warped image data. For running
      * the renderer as a viewfinder, we set the flag to false. To see the
      * preview mosaic, we set the flag to true.
      *
      * @param flag boolean flag to set the warping to true or false.
      */
     public static native void setWarping(boolean flag);
}
