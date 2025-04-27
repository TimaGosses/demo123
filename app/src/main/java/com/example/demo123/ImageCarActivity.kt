package com.example.demo123

import android.net.Uri
import android.os.Bundle
import android.widget.AdapterView
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Adapter
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import io.ktor.http.Url
import kotlinx.coroutines.MainScope
import android.Manifest
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.LinearLayout
import androidx.compose.ui.text.font.FontVariation
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import com.example.demo123.SplashActivity
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okio.IOException
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.io.path.createTempFile

class ImageCarActivity : AppCompatActivity() {

    private lateinit var buttonPickFromGallery: LinearLayout
    private lateinit var buttonTakePhoto: LinearLayout
    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var buttonUploadImages: Button
    private val supabaseClient by lazy { (application as MyApplication).supabase }
    private val imageUrls =
        mutableListOf<String>() // Список Uri для хранения ссылок на выбранные фото
    private var tempPhotoFile: File? = null //Для хранения временного файла фото
    private val imageAdapter = ImageAdapter()  //передаем список Uri в адаптер


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_car)
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto)
        buttonPickFromGallery = findViewById(R.id.buttonPickFromGallery)
        //Инициализация RecycleView
        recyclerViewImages = findViewById(R.id.recyclerViewImages)
        recyclerViewImages.layoutManager = LinearLayoutManager(
            this,
            LinearLayoutManager.HORIZONTAL,
            false
        ) //установка горизонтальной ориентации
        recyclerViewImages.adapter = imageAdapter

        reqestPermissions()

        //Кнопка выбора фото из галереи
        buttonPickFromGallery.setOnClickListener {
            pickImageFromGallery()
        }
        //Кнопка съемки фото
        buttonTakePhoto.setOnClickListener {
            takePhoto()
        }
        //Кнопка загрузки
        findViewById<Button>(R.id.buttonUploadImages).setOnClickListener {
            if (imageAdapter.getFiles().isEmpty()) {  // Используем imageAdapter.getFiles()
                Toast.makeText(this, "Добавьте хотя бы одно изображение", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadImagesToSupabase()
        }

    }
    }
    // Запрос разрешения на доступ к галерее и камере
    private fun reqestPermissions() {
        val permission = mutableListOf<String>()
        //Выбираем разрешение в зависимости от версии android
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU){
            permission.add(Manifest.permission.READ_MEDIA_IMAGES)//Для android 13+
        }else{
            permission.add(Manifest.permission.READ_EXTERNAL_STORAGE) //для чтения галереи
        }
        permission.add(Manifest.permission.CAMERA)//для камеры

        //проверка, какие разрешения ещё не предоставлены
        val permissionToRequest = permission.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionToRequest.isNotEmpty()){
            Log.d("ImageCarActivity","Запрашиваем разрешения: ${permissionToRequest}")
            ActivityCompat.requestPermissions(this, permissionToRequest.toTypedArray(), REQUEST_PERMISSIONS)
        }
        else{
            Log.d("ImageCarActivity","Все разрешения уже предоставлены")
        }

    }
    //Обработка релузьтата запроса разрешений
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permission: Array<out String>,
        grantResults: IntArray
    ){
        super.onRequestPermissionsResult(requestCode, permission, grantResults)
        if (requestCode == REQUEST_PERMISSIONS){
            val deniedPermissions = mutableListOf<String>()
            permission.forEachIndexed { index, permission ->
                if (grantResults[index] != PackageManager.PERMISSION_GRANTED){
                    deniedPermissions.add(permission)
                }
            }
            if (deniedPermissions.isEmpty()){
                Log.d("ImageCarActivity","Все разрешения получены")
            }else{
                Log.d("ImageCarActivity","Отклоненные разрешения: $deniedPermissions")
                Toast.makeText(this@ImageCarActivity,"Не все разрешения получены: $deniedPermissions",
                    Toast.LENGTH_SHORT).show()
            }
            //Если разрешение самеры отклоненно
            if(deniedPermissions.contains(Manifest.permission.CAMERA)){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)){
                    //Объяснение почему нужно разрешение
                    Toast.makeText(this@ImageCarActivity,"РАзрешение на камеру необходимо для съемки фото",Toast.LENGTH_SHORT).show()
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_PERMISSIONS)
                }
                else{
                    //Если пользователь выбрал не заправшивать снова
                    Toast.makeText(this@ImageCarActivity,"Пожалуйста, включите разрешение на доступ к камере",Toast.LENGTH_SHORT).show()
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("packege", packageName, null)
                    }
                    startActivity(intent)
                }
            }
        }
    }
    //запуск выбора изображений из галереи
    private fun pickImageFromGallery(){
        //intent для открытия галереи
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) //разрешение множественного выбора
        }
        //запуск активности
        startActivityForResult(Intent.createChooser(intent, "Выберите изображения"), REQUEST_IMAGE_PICK)

    }
    //запуск камеры для съемки
    private fun takePhoto() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this@ImageCarActivity, "Требуется разрешение на доступ к камере", Toast.LENGTH_SHORT).show()
            reqestPermissions()
            return
        }

        val startTime = System.currentTimeMillis()
        Log.d("ImageCarActivity","Начало takePhoto")

        // Intent для съемки фото
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
            //добавление категории и типа для повышения совместимости
            addCategory(Intent.CATEGORY_DEFAULT)
            type = "image/*"
           
        }
        Log.d("ImageCarActivity", "Создан Intent для камеры: $intent")

        // Проверка доступности камеры через PackageManager
        val hasCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
        Log.d("ImageCarActivity", "Устройство поддерживает камеру: $hasCamera")
        if (!hasCamera) {
            Toast.makeText(this@ImageCarActivity, "Устройство не поддерживает камеру", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка наличия приложений для камеры
        val activitiesStart = System.currentTimeMillis()
        val activities = packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        Log.d("ImageCarActivity", "Поиск приложений для камеры занял ${System.currentTimeMillis() - activitiesStart} мс")
        Log.d("ImageCarActivity", "Найдено приложений для камеры: ${activities.size}")
        activities.forEach { activity ->
            Log.d("ImageCarActivity", "Доступное приложение: ${activity.activityInfo.packageName}")
        }

        if (activities.isNotEmpty()) {
            lifecycleScope.launch(Dispatchers.Main) {
                // Показываем индикатор подготовки
                val progressDialog = ProgressDialog(this@ImageCarActivity).apply {
                    setMessage("Подготовка камеры...")
                    setCancelable(false)
                    show()
                }

                // Создание временного файла в фоновом потоке
                val photoFile = withContext(Dispatchers.IO) {
                    try {
                        createTempImageFile()
                    } catch (e: IOException) {
                        Log.e("ImageCarActivity", "Ошибка создания временного файла: ${e.message}", e)
                        null
                    }
                }

                progressDialog.dismiss()

                photoFile?.let {
                    tempPhotoFile = it
                    val photoUri = FileProvider.getUriForFile(this@ImageCarActivity, "${packageName}.fileprovider", it)
                    Log.d("ImageCarActivity", "Получен Uri через FileProvider: $photoUri")
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                    startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
                } ?: run {
                    Toast.makeText(
                        this@ImageCarActivity,
                        "Не удалось создать файл для фото",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        } else {
            Toast.makeText(this@ImageCarActivity, "Камера не доступна", Toast.LENGTH_SHORT).show()
        }
    }
    //Обрабатывает результаты выбора или съемки
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        Log.d("ImageCarActivity", "onActivityResult: requestCode=$requestCode, resultCode=$resultCode")
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    if (data?.clipData != null) {
                        val clipData = data.clipData
                        Log.d("ImageCarActivity", "Выбрано несколько изображений: ${clipData!!.itemCount}")
                        for (i in 0 until clipData.itemCount) {
                            val uri = clipData.getItemAt(i).uri
                            Log.d("ImageCarActivity", "Обрабатываем Uri: $uri")
                            copyUriToTempFile(uri)?.let { tempFile ->
                                imageAdapter.addFile(tempFile)
                                Log.d("ImageCarActivity", "Добавлен элемент, текущий размер адаптера: ${imageAdapter.itemCount}")
                            } ?: run {
                                Log.e("ImageCarActivity", "Не удалось скопировать Uri: $uri")
                            }
                        }
                    } else {
                        data?.data?.let { uri ->
                            Log.d("ImageCarActivity", "Обрабатываем одиночный Uri: $uri")
                            copyUriToTempFile(uri)?.let { tempFile ->
                                imageAdapter.addFile(tempFile)
                                Log.d("ImageCarActivity", "Добавлен элемент, текущий размер адаптера: ${imageAdapter.itemCount}")
                            } ?: run {
                                Log.e("ImageCarActivity", "Не удалось скопировать Uri: $uri")
                            }
                        }
                    }
                }
                REQUEST_IMAGE_CAPTURE -> {
                    tempPhotoFile?.let { file ->
                        imageAdapter.addFile(file)
                        Log.d("ImageCarActivity", "Добавлен элемент, текущий размер адаптера: ${imageAdapter.itemCount}")
                    }
                }
            }
        } else {
            Log.d("ImageCarActivity", "onActivityResult: resultCode не OK, requestCode=$requestCode, resultCode=$resultCode")
        }
    }
    private fun copyUriToTempFile(uri: Uri): File? {
        try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val storageDir = cacheDir
            Log.d("ImageCarActivity", "Директория для хранения: $storageDir")
            if (!storageDir.exists()) {
                storageDir.mkdirs()
                Log.d("ImageCarActivity", "Создана директория: $storageDir")
            }
            val tempFile = File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
            Log.d("ImageCarActivity", "Создан временный файл: $tempFile")
            contentResolver.openInputStream(uri)?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    val bytesCopied = input.copyTo(output)
                    Log.d("ImageCarActivity", "Скопировано байт: $bytesCopied для файла: $tempFile")
                }
            } ?: run {
                Log.e("ImageCarActivity", "Не удалось открыть InputStream для Uri: $uri")
                return null
            }
            return tempFile
        } catch (e: SecurityException) {
            Log.e("ImageCarActivity", "Ошибка безопасности при копировании Uri: ${e.message}", e)
            return null
        } catch (e: Exception) {
            Log.e("ImageCarActivity", "Ошибка копирования Uri: ${e.message}", e)
            return null
        }
    }
    // Загружает изображения в Supabase Storage
    private fun uploadImagesToSupabase() {
        val supabaseClient = (application as MyApplication).supabase
        lifecycleScope.launch {
            try {
                imageAdapter.getFiles().forEachIndexed { index, file ->
                    val byteArray = file.readBytes()
                    val fileName = "image_${System.currentTimeMillis()}_$index.jpg"
                    val response = supabaseClient.storage.from("car-images").upload(
                        path = fileName,
                        data = byteArray
                    ) {
                        upsert = false  // Перемещаем upsert внутрь лямбда-выражения
                    }
                    Log.d("ImageCarActivity", "Изображение загружено: $response")
                    val publicUrl = supabaseClient.storage.from("car-images").publicUrl(fileName)
                    imageUrls.add(publicUrl)
                }
                Toast.makeText(this@ImageCarActivity, "Изображения загружены", Toast.LENGTH_SHORT).show()
                val intent = Intent(this@ImageCarActivity, SplashActivity::class.java)
                intent.putStringArrayListExtra("imageUrls", ArrayList(imageUrls))
                startActivity(intent)
            } catch (e: Exception) {
                Log.e("ImageCarActivity", "Ошибка загрузки изображений: ${e.message}")
                Toast.makeText(this@ImageCarActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    // Создает временный файл для хранения фото
    private fun createTempImageFile(): File {
        // Формируем уникальное имя файла на основе текущей даты и времени
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        // Получаем директорию Pictures в приватном хранилище приложения
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        // Создаем временный файл с префиксом JPEG_ и расширением .jpg
        return File.createTempFile("JPEG_${timeStamp}_", ".jpg", storageDir)
    }

    // Константы для идентификации запросов
    companion object {
        private const val REQUEST_IMAGE_PICK = 100 // Для выбора из галереи
        private const val REQUEST_IMAGE_CAPTURE = 101 // Для съемки фото
        private const val REQUEST_PERMISSIONS = 102 // Для запроса разрешений
    }
}
