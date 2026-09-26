package com.yes.flashcamera.presentation.ui

import android.Manifest
import android.app.Activity.CAMERA_SERVICE
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.media.ImageReader
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.view.Surface
import android.view.TextureView
import android.view.View
import android.widget.Button
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.yes.flashcamera.R
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Arrays
@Deprecated("not used")
class tmp {
    /*
    var myCameras: Array<CameraService?>? = null

    private var mCameraManager: CameraManager? = null
    private val CAMERA1 = 0
    private val CAMERA2 = 1

    private var mButtonOpenCamera1: Button? = null
    private var mButtonOpenCamera2: Button? = null
    private var mButtonToMakeShot: Button? = null
    private var mImageView: TextureView? = null
    private var mBackgroundThread: HandlerThread? = null
    private var mBackgroundHandler: Handler? = null


    private fun startBackgroundThread() {
        mBackgroundThread = HandlerThread("CameraBackground")
        mBackgroundThread!!.start()
        mBackgroundHandler = Handler(mBackgroundThread!!.looper)
    }

    private fun stopBackgroundThread() {
        mBackgroundThread!!.quitSafely()
        try {
            mBackgroundThread!!.join()
            mBackgroundThread = null
            mBackgroundHandler = null
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main)
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
            ||
            (ContextCompat.checkSelfPermission(
                this@MainActivity,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            requestPermissions(
                arrayOf<String>(
                    Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ), 1
            )
        }


        mButtonOpenCamera1 = findViewById(R.id.button1)
        mButtonOpenCamera2 = findViewById(R.id.button2)
        mButtonToMakeShot = findViewById(R.id.button3)
        mImageView = findViewById<TextureView>(R.id.textureView)

        (mButtonOpenCamera1 as Button).setOnClickListener(View.OnClickListener {
            if (myCameras!![CAMERA2]!!.isOpen) {
                myCameras!![CAMERA2]!!.closeCamera()
            }
            if (myCameras!![CAMERA1] != null) {
                if (!myCameras!![CAMERA1]!!.isOpen)
                    myCameras!![CAMERA1]!!.openCamera()
            }
        })

        (mButtonOpenCamera2 as Button).setOnClickListener(View.OnClickListener {
            if (myCameras!![CAMERA1]!!.isOpen) {
                myCameras!![CAMERA1]!!.closeCamera()
            }
            if (myCameras!![CAMERA2] != null) {
                if (!myCameras!![CAMERA2]!!.isOpen) myCameras!![CAMERA2]!!.openCamera()
            }
        })


        (mButtonToMakeShot as Button).setOnClickListener(View.OnClickListener {
            if (myCameras!![CAMERA1]!!.isOpen) myCameras!![CAMERA1]!!.makePhoto()
            if (myCameras!![CAMERA2]!!.isOpen) myCameras!![CAMERA2]!!.makePhoto()
        })


        mCameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
        try {

            myCameras = arrayOfNulls(mCameraManager!!.cameraIdList.size)



            for (cameraID in mCameraManager!!.cameraIdList) {
                val id = cameraID.toInt()
                myCameras!![id] = CameraService(mCameraManager, cameraID)
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }


    inner class CameraService(cameraManager: CameraManager?, cameraID: String) {
        private val mFile = File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM),
            "test1.jpg"
        )
        private val mCameraID: String
        private var mCameraDevice: CameraDevice? = null
        private var mCaptureSession: CameraCaptureSession? = null
        private var mImageReader: ImageReader? = null


        fun makePhoto() {
            try {
                // This is the CaptureRequest.Builder that we use to take a picture.
                val captureBuilder =
                    mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
                captureBuilder.addTarget(mImageReader!!.surface)
                val captureCallback: CameraCaptureSession.CaptureCallback =
                    object : CameraCaptureSession.CaptureCallback() {
                        override fun onCaptureCompleted(
                            session: CameraCaptureSession,
                            request: CaptureRequest,
                            result: TotalCaptureResult
                        ) {
                        }
                    }

                mCaptureSession!!.stopRepeating()
                mCaptureSession!!.abortCaptures()
                mCaptureSession!!.capture(
                    captureBuilder.build(),
                    captureCallback,
                    mBackgroundHandler
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }


        private val mOnImageAvailableListener =
            ImageReader.OnImageAvailableListener { reader ->
                mBackgroundHandler?.post(
                    ImageSaver(
                        reader.acquireNextImage(),
                        mFile
                    )
                )
            }


        private val mCameraCallback: CameraDevice.StateCallback =
            object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    mCameraDevice = camera


                    createCameraPreviewSession()
                }

                override fun onDisconnected(camera: CameraDevice) {
                    mCameraDevice!!.close()


                    mCameraDevice = null
                }

                override fun onError(camera: CameraDevice, error: Int) {
                }
            }


        init {
            mCameraManager = cameraManager
            mCameraID = cameraID
        }

        private fun createCameraPreviewSession() {
            mImageReader = ImageReader.newInstance(1920, 1080, ImageFormat.JPEG, 1)
            mImageReader!!.setOnImageAvailableListener(mOnImageAvailableListener, null)

            val texture = mImageView!!.surfaceTexture

            texture!!.setDefaultBufferSize(1920, 1080)
            val surface: Surface = Surface(texture)

            try {
                val builder =
                    mCameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)

                builder.addTarget(surface)




                mCameraDevice!!.createCaptureSession(
                    Arrays.asList(surface, mImageReader!!.surface),
                    object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            mCaptureSession = session
                            try {
                                mCaptureSession!!.setRepeatingRequest(
                                    builder.build(),
                                    null,
                                    mBackgroundHandler
                                )
                            } catch (e: CameraAccessException) {
                                e.printStackTrace()
                            }
                        }

                        override fun onConfigureFailed(session: CameraCaptureSession) {}
                    }, mBackgroundHandler
                )
            } catch (e: CameraAccessException) {
                e.printStackTrace()
            }
        }


        val isOpen: Boolean
            get() = if (mCameraDevice == null) {
                false
            } else {
                true
            }

        fun openCamera() {
            try {
                if (checkSelfPermission(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    mCameraManager!!.openCamera(mCameraID, mCameraCallback, mBackgroundHandler)
                }
            } catch (e: CameraAccessException) {

            }
        }

        fun closeCamera() {
            if (mCameraDevice != null) {
                mCameraDevice!!.close()
                mCameraDevice = null
            }
        }
    }


    public override fun onPause() {
        if (myCameras!![CAMERA1]!!.isOpen) {
            myCameras!![CAMERA1]!!.closeCamera()
        }
        if (myCameras!![CAMERA2]!!.isOpen) {
            myCameras!![CAMERA2]!!.closeCamera()
        }
        stopBackgroundThread()
        super.onPause()
    }

    public override fun onResume() {
        super.onResume()
        startBackgroundThread()
    }


    private class ImageSaver(
        /**
         * The JPEG image
         */
        private val mImage: Image,
        /**
         * The file we save the image into.
         */
        private val mFile: File
    ) :
        Runnable {
        override fun run() {
            val buffer = mImage.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer[bytes]
            var output: FileOutputStream? = null
            try {
                output = FileOutputStream(mFile)
                output.write(bytes)
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                mImage.close()
                if (null != output) {
                    try {
                        output.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }

*/
}