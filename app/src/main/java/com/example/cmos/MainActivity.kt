package com.example.cmos

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.example.cmos.ui.theme.CMOSTheme
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class MainActivity : ComponentActivity() {

    private var isDialogVisible by mutableStateOf(false)
    private var imageBitmapState by mutableStateOf<Bitmap?>(null)
    private var imageUriState by mutableStateOf<Uri?>(null)
    private var recognizedTextState by mutableStateOf("")
    private var textRecognizerState by mutableStateOf(TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS))

    private fun handleImageClick() {
        isDialogVisible = true
    }

    private val takePictureLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
            if (success) {
                val imageBitmap = BitmapFactory.decodeFile(imageFile?.absolutePath )
                imageBitmapState = imageBitmap
                imageUriState = imageFile?.toUri()
                // Implement process logic here
                processImage(imageBitmapState, imageUriState, textRecognizerState)
            } else {
                Log.e(TAG, "Error taking picture: Result is not success.")
                Toast.makeText(this, "Error taking picture. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }

    private var imageFile: File? = null


    private val pickPhotoLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            if (uri != null) {
                try {
                    val imageStream: InputStream? = contentResolver.openInputStream(uri)
                    val selectedImageBitmap = BitmapFactory.decodeStream(imageStream)
                    imageBitmapState = selectedImageBitmap
                    imageUriState = uri
                    processImage(imageBitmapState, imageUriState, textRecognizerState)
                } catch (e: FileNotFoundException) {
                    Log.e(TAG, "Error loading selected image: ${e.message}")
                    Toast.makeText(this, "Error loading selected image. Please try again.", Toast.LENGTH_SHORT).show()
                }
            } else {
                Log.e(TAG, "Error picking photo: Selected URI is null.")
                Toast.makeText(this, "Error picking photo. Please try again.", Toast.LENGTH_SHORT).show()
            }
        }
    private val TAG = "MainActivity"
    private val PERMISSION_REQUEST_CODE = 10
    private val REQUEST_IMAGE_CAPTURE = 1
    private val REQUEST_IMAGE_PICK = 2
    private val REQUIRED_PERMISSIONS = mutableListOf(
        android.Manifest.permission.CAMERA,
        android.Manifest.permission.READ_EXTERNAL_STORAGE
    ).toTypedArray()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (allPermissionsGranted()) {
            Log.d(TAG, "All permissions granted")
        } else {
            requestPermissions()
        }

        setContent {
            Options(
                imageBitmap = imageBitmapState,
                imageUri = imageUriState,
                textRecognizer = textRecognizerState,
                recognizedText = recognizedTextState
            )
        }
    }

    @Composable
    fun Options(
        imageBitmap: Bitmap?,
        imageUri: Uri?,
        textRecognizer: TextRecognizer,
        recognizedText: String,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                item {
                    Text(
                        text = "Hello! User",
                        modifier = Modifier
                            .padding(30.dp),
                        color = Color.Blue,
                        fontSize = 50.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    Text(
                        text = "Options",
                        modifier = Modifier
                            .padding(30.dp),
                        color = Color.DarkGray,
                        fontSize = 60.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.image_icon),
                            contentDescription = null,
                            modifier = Modifier
                                .size(150.dp)
                                .clickable {
                                    showImagePickerDialog()
                                }
                        )

                        Image(
                            painter = painterResource(id = R.drawable.url_search),
                            contentDescription = null,
                            modifier = Modifier
                                .size(180.dp)
                                .padding(20.dp)
                                .clickable {
                                    //run ml model
                                }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.size(16.dp))
                }
                item {
                    // Image display
                    imageBitmap?.let {
                        Image(
                            bitmap = it.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .size(100.dp)
                                .background(Color.Gray)
                                .clickable {
                                    // Handle image click here
                                    handleImageClick()
                                }
                        )
                    }
                }
                item {
                    Spacer(modifier = Modifier.size(16.dp))
                }
                item {
                    // Recognized text display
                    Text(
                        text = "Recognized Text: $recognizedText",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                item {
                    Spacer(modifier = Modifier.size(16.dp))
                }
                item {
                    if (isDialogVisible) {
                        ImageDialog(imageBitmap = imageBitmap, onClose = { isDialogVisible = false })
                    }
                }
            }
        }
    }


    private fun showImagePickerDialog() {
        val options = arrayOf<CharSequence>("Take Photo", "Choose from Gallery")
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Select Action")
        builder.setItems(options) { _, item ->
            when {
                options[item] == "Take Photo" -> {
                    imageFile = createImageFile()
                    imageFile?.let {
                        takePictureLauncher.launch(Uri.fromFile(it))
                    }
                }

                options[item] == "Choose from Gallery" -> {
                    pickPhotoLauncher.launch("image/*")
                }
            }
        }
        builder.show()
    }

    private fun createImageFile(): File? {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir: File? = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            // Save the file path for use with ACTION_VIEW intents
            imageFile = this
        }
    }

    private fun processImage(
        imageBitmap: Bitmap?,
        imageUri: Uri?,
        textRecognizer: TextRecognizer,
    ) {
        var image: InputImage? = null

        if (imageBitmap != null) {
            image = imageBitmap?.let {
                InputImage.fromBitmap(it, 0)
            }
        } else if (imageUri != null) {
            image = imageUri?.let {
                InputImage.fromFilePath(this, it)
            }
        } else {
            Toast.makeText(this, "No image found", Toast.LENGTH_SHORT).show()
        }

        recognizeText(textRecognizer, image)
    }

    private fun recognizeText(
        textRecognizer: TextRecognizer,
        image: InputImage?
        ) {

        image?.let {
            textRecognizer.process(it)
                .addOnSuccessListener { visionText ->
//                    val preprocessedText = preprocessInput(visionText.text)
//                    val modelOutput = invokeYourMLModel(preprocessedText)
//                    val postprocessedText = postprocessOutput(modelOutput)

                    //recognizedTextState = postprocessedText
                    recognizedTextState = visionText.text
                    Toast.makeText(this, "OCR completed", Toast.LENGTH_SHORT).show()

                    // Show the result in a dialog
//                    ShowResultDialog(postprocessedText)

                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
                }
        }
    }

    @Composable
    private fun ImageDialog(imageBitmap: Bitmap?, onClose: () -> Unit) {
        if (imageBitmap != null) {
            AlertDialog(
                onDismissRequest = { onClose() },
                properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true),
                title = { Text(text = "Large Image View") },
                confirmButton = {
                    Button(
                        onClick = { onClose() },
                        modifier = Modifier.background(Color.Gray)
                    ) {
                        Text("Close")
                    }
                },
                dismissButton = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.Gray)
                            .padding(16.dp)
                    ) {
                        Image(
                            bitmap = imageBitmap.asImageBitmap(),
                            contentDescription = null,
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.White)
                        )
                    }
                }
            )
        }
    }


    private fun requestPermissions() {
        ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, PERMISSION_REQUEST_CODE)
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(baseContext, it) == PackageManager.PERMISSION_GRANTED
    }

    //private fun preprocessInput(recognizedText: String): String {
        // Add your preprocessing logic here
        // Example: Tokenization
    //    return recognizedText.lowercase(Locale.ROOT)
    //}

    //private fun postprocessOutput(modelOutput: YourModelOutputType): String {
        // Add your postprocessing logic here
        // Example: Extract relevant information
    //    return "Detected Dark Patterns: ${modelOutput.detectedPatterns}"
    //}

    @Composable
    private fun ShowResultDialog(result: String) {
        AlertDialog(
            onDismissRequest = { /* Handle dialog dismissal if needed */ },
            title = { Text("ML Model Result") },
            text = { Text(result) },
            confirmButton = {
                Button(onClick = { /* Handle button click if needed */ }) {
                    Text("OK")
                }
            }
        )
    }



}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CMOSTheme {

    }
}


