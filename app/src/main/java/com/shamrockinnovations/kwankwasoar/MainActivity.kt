package com.shamrockinnovations.kwankwasoar

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.regula.facesdk.FaceSDK
import com.regula.facesdk.configuration.FaceCaptureConfiguration
import com.regula.facesdk.enums.ImageType
import com.regula.facesdk.model.MatchFacesImage
import com.regula.facesdk.model.results.matchfaces.MatchFacesSimilarityThresholdSplit
import com.regula.facesdk.request.MatchFacesRequest
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {

    private lateinit var scannedFaces: MutableList<Int>
    private lateinit var pointsText: TextView
    private var points: Int = 0

    private lateinit var pointsPannel: ImageView

    private lateinit var compareImage: Bitmap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Window Flags
//        window.decorView.apply {
//            systemUiVisibility = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//        }

        scannedFaces = mutableListOf()
        pointsText = findViewById<TextView>(R.id.pointsText)
        pointsText.text = "POINTS: ${points}"

        pointsPannel = findViewById<ImageView>(R.id.pointsPannel)

        pointsPannel.setOnClickListener() {
            Toast.makeText(this@MainActivity, "You must have 1000 points to claim prise", Toast.LENGTH_SHORT).show()
        }

        compareImage = BitmapFactory.decodeResource(resources, R.drawable.kwankwaso1);

        val captureBtn = findViewById<Button>(R.id.captureBtn)

        captureBtn.setOnClickListener() {
            captureFace()
        }
    }

    private fun captureFace() {

        val configuration = FaceCaptureConfiguration.Builder()
            .setCameraId(0)
            .setShowHelpTextAnimation(false)
            .setTorchButtonEnabled(true)
            .setCameraSwitchEnabled(true)
            .setCloseButtonEnabled(true)
            .build()

        FaceSDK.Instance().presentFaceCaptureActivity(this@MainActivity, configuration) { response ->
            if (response.exception == null) {
                Toast.makeText(this, "Face Captured", Toast.LENGTH_SHORT).show()
                response.image?.let { matchFace(it.bitmap) }
            } else {
                Toast.makeText(this, "Face Capture Error: ${response.exception}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun matchFace(image: Bitmap) {

        val firstImage = MatchFacesImage(compareImage, ImageType.PRINTED)
        val secondImage = MatchFacesImage(image, ImageType.PRINTED)

        val request = MatchFacesRequest(listOf(firstImage, secondImage))

        Toast.makeText(this@MainActivity, "Checking if it was kwankwaso...", Toast.LENGTH_SHORT).show()

        FaceSDK.Instance().matchFaces(request) { response ->

            val split = MatchFacesSimilarityThresholdSplit(response.results, 0.75)

            if (split.matchedFaces.size > 0) {

                val similarity = split.matchedFaces[0].similarity
                val similarityText: String = String.format("%.2f", similarity * 100)
                val similarityInt: Int = similarityText.toFloat().roundToInt()

                if (scannedFaces.contains(similarityInt)) {
                    Toast.makeText(this, "You already scanned this image", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "That was Kwankwaso ${similarityText}%", Toast.LENGTH_SHORT).show()
                    scannedFaces.add(similarityInt)
                    points += 1
                    pointsText.text = "POINTS: ${points}"
                }

            } else {
                if (response.exception == null) {
                    Toast.makeText(this, "Sorry, that was not Kwankwaso.", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Face Matching Error: . ${response.exception}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}