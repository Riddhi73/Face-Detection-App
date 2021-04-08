package com.example.facedetectionapp

import android.graphics.*
import android.graphics.Paint.Style
import android.graphics.drawable.BitmapDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.SparseArray
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.vision.Frame
import com.google.android.gms.vision.face.Face
import com.google.android.gms.vision.face.FaceDetector

private const val LEFT_EYE = 4
private const val RADIUS = 10f
private const val TEXT_SIZE = 50f
private const val CORNER_RADIUS = 2f
private const val STROKE_WIDTH = 5f

class MainActivity : AppCompatActivity() {

    lateinit var imageView: ImageView
    lateinit var defaultBitmap: Bitmap
    lateinit var temporaryBitmap: Bitmap
    lateinit var eyePatchBitmap: Bitmap
    lateinit var canvas: Canvas
    val rectPaint = Paint()
    val faceDetector : FaceDetector
        get() = initializeDetector()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        imageView = findViewById<View>(R.id.image_view) as ImageView

    }
    fun processImage(view: View){
        val bitmapOptions = BitmapFactory.Options().apply {
            inMutable = true
        }

        initializeBitmap(bitmapOptions)
        createRectanglePaint()
        canvas = Canvas(temporaryBitmap).apply {
            drawBitmap(defaultBitmap,0f,0f,null)
        }
        if (!faceDetector.isOperational){
            AlertDialog.Builder(this).setMessage("Face Detector cannot be setup on this device :(").show()
        }
        else
        {
            val frame = Frame.Builder().setBitmap(defaultBitmap).build()
            val sparseArray = faceDetector.detect(frame)
            detectFaces(sparseArray)
            imageView.setImageDrawable(BitmapDrawable(resources,temporaryBitmap))
            faceDetector.release()
        }
    }
    private fun initializeBitmap(bitmapOptions: BitmapFactory.Options){
        defaultBitmap = BitmapFactory.decodeResource(resources,R.drawable.soumik,bitmapOptions)
        temporaryBitmap = Bitmap.createBitmap(defaultBitmap.width,defaultBitmap.height,Bitmap.Config.RGB_565)
        eyePatchBitmap = BitmapFactory.decodeResource(resources,R.drawable.eye_patch,bitmapOptions)


    }
    private fun createRectanglePaint(){
        rectPaint.apply {
            strokeWidth = STROKE_WIDTH
            color = Color.CYAN
            style = Paint.Style.STROKE

        }
    }
    private fun initializeDetector(): FaceDetector{
        return FaceDetector.Builder(this)
            .setTrackingEnabled(false)
            .setLandmarkType(FaceDetector.ALL_LANDMARKS)
            .build()
    }
    private fun detectFaces(sparseArray: SparseArray<Face>){
        for (i in 0 until sparseArray.size()){
            val face = sparseArray.valueAt(i)
            val left = face.position.x
            val top = face.position.y
            val right = left+face.width
            val bottom = top + face.height
            val rectF = RectF(left, top, right, bottom)
            canvas.drawRoundRect(rectF, CORNER_RADIUS, CORNER_RADIUS,rectPaint)
            detectLandmarks(face)
        }
    }
    private fun detectLandmarks(face: Face){
        for (landmark in face.landmarks){
            val xCordinate = landmark.position.x
            val yCordinate = landmark.position.y
            canvas.drawCircle(xCordinate,yCordinate, RADIUS,rectPaint)
            drawLandmarkType(landmark.type,xCordinate,yCordinate)
            //draweyePatchBitmap(landmark.type,xCordinate,yCordinate)

        }
    }
    private fun drawLandmarkType(landmarkType: Int, xCordinate: Float, yCordinate: Float){
        val type = landmarkType.toString()
        rectPaint.textSize = TEXT_SIZE
        canvas.drawText(type,xCordinate,yCordinate,rectPaint)

    }



}