package com.rekoj134.argamedemo.ar_game

import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import android.view.View
import android.view.animation.AlphaAnimation
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.accurate.AccuratePoseDetectorOptions
import com.rekoj134.argamedemo.R
import com.rekoj134.argamedemo.databinding.ActivityArgameBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.hypot
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ARGameActivity : AppCompatActivity() {
    private lateinit var binding: ActivityArgameBinding
    private lateinit var imageCapture: ImageCapture

    private var referencePose: Pose? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArgameBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the reference pose from the test image
        loadReferencePose()

        setupCamera()

        binding.btnPlay.setOnClickListener {
            binding.imgTest.visibility = View.VISIBLE
            binding.btnPlay.visibility = View.GONE
            CoroutineScope(Dispatchers.Main).launch {
                delay(4000)
                binding.imgTest.visibility = View.GONE
                Toast.makeText(this@ARGameActivity, "Dem nguoc 3", Toast.LENGTH_SHORT).show()
                delay(1000)
                Toast.makeText(this@ARGameActivity, "Dem nguoc 2", Toast.LENGTH_SHORT).show()
                delay(1000)
                Toast.makeText(this@ARGameActivity, "Dem nguoc 1", Toast.LENGTH_SHORT).show()
                playGame()
            }
//            startStampAnimation(binding.imgTest)
//            startFlashAnimation(binding.stampImageView)
        }
    }

    private fun startStampAnimation(stampImageView: ImageView) {
        // Tạo hiệu ứng phóng to nhanh chóng
        val scaleX = ObjectAnimator.ofFloat(stampImageView, "scaleX", 0f, 1.2f, 1f) // Scale mạnh rồi trở lại bình thường
        val scaleY = ObjectAnimator.ofFloat(stampImageView, "scaleY", 0f, 1.2f, 1f)

        // Tạo hiệu ứng alpha (fade-in) nhanh
        val fadeIn = ObjectAnimator.ofFloat(stampImageView, "alpha", 0f, 1f)

        // Đặt thời gian cho tất cả các animation (nhanh và mạnh)
        scaleX.duration = 300 // 300ms để phóng to mạnh
        scaleY.duration = 300
        fadeIn.duration = 300

        // Bắt đầu các animation
        scaleX.start()
        scaleY.start()
        fadeIn.start()
    }

    private fun startFlashAnimation(stampImageView: ImageView) {
        // Tạo hiệu ứng di chuyển từ ngoài màn hình vào giữa 1/4 màn hình
        val moveDown = ObjectAnimator.ofFloat(stampImageView, "translationY", -1000f, 0f)

        // Tạo hiệu ứng phóng to (scale) và làm ảnh dần hiện lên (fade-in)
        val fadeIn = ObjectAnimator.ofFloat(stampImageView, "alpha", 0f, 1f)
        val scaleX = ObjectAnimator.ofFloat(stampImageView, "scaleX", 0.5f, 1f)
        val scaleY = ObjectAnimator.ofFloat(stampImageView, "scaleY", 0.5f, 1f)

        // Đặt thời gian cho tất cả các animation (chạy nhanh nhưng mượt)
        moveDown.duration = 800 // 800ms để di chuyển xuống
        fadeIn.duration = 500   // 500ms để fade-in nhanh chóng
        scaleX.duration = 800   // 800ms để phóng to từ 50% đến 100%
        scaleY.duration = 800

        // Bắt đầu các animation
        moveDown.start()
        fadeIn.start()
        scaleX.start()
        scaleY.start()

        // Hiển thị ảnh khi animation bắt đầu
        stampImageView.visibility = View.VISIBLE
    }

//    private fun startFlashAnimation(flashView: View) {
//        // Hiển thị flash trắng nhanh
//        flashView.visibility = View.VISIBLE
//
//        // Fade in nhanh
//        val fadeIn = ObjectAnimator.ofFloat(flashView, "alpha", 0f, 1f)
//        fadeIn.duration = 100 // 100ms để flash hiện lên
//
//        // Fade out nhanh (biến mất)
//        val fadeOut = ObjectAnimator.ofFloat(flashView, "alpha", 1f, 0f)
//        fadeOut.duration = 100 // 100ms để flash biến mất
//
//        // Kết hợp hiệu ứng fade in và fade out
//        fadeIn.start()
//        fadeIn.addListener(object : android.animation.AnimatorListenerAdapter() {
//            override fun onAnimationEnd(animation: android.animation.Animator) {
//                fadeOut.start()
//            }
//        })
//    }

    private fun loadReferencePose() {
        // Load the reference image (test image) and extract pose landmarks
        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.test)
        val image = InputImage.fromBitmap(bitmap, 0)

        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()

        val poseDetector = PoseDetection.getClient(options)

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                referencePose = pose
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to process reference pose", Toast.LENGTH_SHORT).show()
            }
    }

    private fun playGame() {
        takePhotoAndCalculateScore()
    }

    private fun takePhotoAndCalculateScore() {
        // Capture photo and process the pose in the same way
        val outputOptions = ImageCapture.OutputFileOptions.Builder(
            createTempFile("pose_temp_", ".jpg", cacheDir)
        ).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val bitmap = BitmapFactory.decodeFile(output.savedUri?.path)
                    if (bitmap != null) {
                        calculateScore(bitmap)
                    } else {
                        Toast.makeText(this@ARGameActivity, "Bitmap null!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@ARGameActivity, "Chụp ảnh lỗi: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    private fun calculateScore(bitmap: Bitmap) {
        val options = AccuratePoseDetectorOptions.Builder()
            .setDetectorMode(AccuratePoseDetectorOptions.SINGLE_IMAGE_MODE)
            .build()

        val poseDetector = PoseDetection.getClient(options)
        val image = InputImage.fromBitmap(bitmap, 0)

        poseDetector.process(image)
            .addOnSuccessListener { pose ->
                val similarity = compareSkeletonPose(pose, referencePose)
                val score = (similarity * 100).roundToInt()
                Toast.makeText(this, "Score: $score", Toast.LENGTH_SHORT).show()
                binding.btnPlay.visibility = View.VISIBLE
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Pose detection failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun compareSkeletonPose(pose1: Pose, pose2: Pose?): Double {
        if (pose2 == null) return 0.0

        val bones = listOf(
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.LEFT_ELBOW),
            Pair(PoseLandmark.LEFT_ELBOW, PoseLandmark.LEFT_WRIST),
            Pair(PoseLandmark.RIGHT_SHOULDER, PoseLandmark.RIGHT_ELBOW),
            Pair(PoseLandmark.RIGHT_ELBOW, PoseLandmark.RIGHT_WRIST),
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.LEFT_KNEE),
            Pair(PoseLandmark.LEFT_KNEE, PoseLandmark.LEFT_ANKLE),
            Pair(PoseLandmark.RIGHT_HIP, PoseLandmark.RIGHT_KNEE),
            Pair(PoseLandmark.RIGHT_KNEE, PoseLandmark.RIGHT_ANKLE),
            Pair(PoseLandmark.LEFT_SHOULDER, PoseLandmark.RIGHT_SHOULDER),
            Pair(PoseLandmark.LEFT_HIP, PoseLandmark.RIGHT_HIP)
        )

        var totalSimilarity = 0.0
        var count = 0

        for ((start, end) in bones) {
            val p1Start = pose1.getPoseLandmark(start)?.position
            val p1End = pose1.getPoseLandmark(end)?.position
            val p2Start = pose2.getPoseLandmark(start)?.position
            val p2End = pose2.getPoseLandmark(end)?.position

            if (p1Start != null && p1End != null && p2Start != null && p2End != null) {
                val v1 = PointF(p1End.x - p1Start.x, p1End.y - p1Start.y)
                val v2 = PointF(p2End.x - p2Start.x, p2End.y - p2Start.y)
                val sim = cosineSimilarity(v1, v2)
                totalSimilarity += sim
                count++
            }
        }

        return if (count > 0) (totalSimilarity / count).coerceIn(0.0, 1.0) else 0.0
    }

    private fun cosineSimilarity(v1: PointF, v2: PointF): Double {
        val dot = v1.x * v2.x + v1.y * v2.y
        val norm1 = hypot(v1.x.toDouble(), v1.y.toDouble())
        val norm2 = hypot(v2.x.toDouble(), v2.y.toDouble())
        return if (norm1 > 0 && norm2 > 0) dot / (norm1 * norm2) else 0.0
    }

    // Load camera and start capturing
    private fun setupCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.viewFinder.surfaceProvider
            }

            imageCapture = ImageCapture.Builder()
                .setTargetRotation(binding.viewFinder.display.rotation)
                .build()

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Camera binding failed: ${exc.message}", Toast.LENGTH_SHORT).show()
            }

        }, ContextCompat.getMainExecutor(this))
    }
}

