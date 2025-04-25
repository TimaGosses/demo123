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
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import kotlin.io.path.createTempFile

class ImageCarActivity : AppCompatActivity() {

    private lateinit var buttonPickFromGallery: Button
    private lateinit var buttonTakePhoto: Button
    private lateinit var recyclerViewImages: RecyclerView
    private lateinit var buttonUploadImages: Button
    private val supabaseClient by lazy { (application as MyApplication).supabase }
    private val imageUris = mutableListOf<Uri>() // Список Uri для хранения ссылок на выбранные фото
    private var tempPhotoFile: File? = null //Для хранения временного файла фото
    private val imageAdapter = ImageAdapter(imageUris)  //передаем список Uri в адаптер



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_image_car)
        buttonTakePhoto = findViewById(R.id.buttonTakePhoto)
        buttonPickFromGallery = findViewById(R.id.buttonPickFromGallery)
        //Инициализация RecycleView
        recyclerViewImages = findViewById(R.id.recyclerViewImages)
        recyclerViewImages.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false) //установка горизонтальной ориентации
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
            if (imageUris.isEmpty()){
                Toast.makeText(this,"Добавьте хотябы одно изобрадение", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            uploadImagesToSupabase()
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
            permission.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)//для записи файлов
        }
        permission.add(Manifest.permission.CAMERA)//для камеры

        //проверка, какие разрешения ещё не предоставлены
        val permissionToRequest = permission.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (permissionToRequest.isNotEmpty()){
            ActivityCompat.requestPermissions(this, permissionToRequest.toTypedArray(), REQUEST_PERMISSIONS)
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
            if (grantResults.all {it == PackageManager.PERMISSION_GRANTED }){
                //Все разрешения получены
                Log.d("ImageCarActivity","Все разрешения получены")
            }else
                //не все разрешения получены
                Toast.makeText(this@ImageCarActivity,"Не все разрешения получены",Toast.LENGTH_SHORT).show()
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
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this@ImageCarActivity,"Требуется разрешения на доступ к камере",Toast.LENGTH_SHORT).show()
            reqestPermissions()
            return
        }
        //intent для съемки фото
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        //проверка наличия приложения камеры
        if (intent.resolveActivity(packageManager) != null) {
            //создание времменого файла
            val photoFile: File? = try {
                createTempImageFile()
            } catch (e: IOException) {
                Log.e("ImageCarActivity", "Ошибка создания временного файла: ${e.message}", e)
                null
            }
            photoFile?.let {
                tempPhotoFile = it //сохранение ссылки на файл
                //Получение Uri через FileProvider
                val photoUri = FileProvider.getUriForFile(this, "${packageName}.fileprovider", it)
                //указываем куда сохранить фото
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri)
                //запуск камеры
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE)
            } ?: run {
                Toast.makeText(
                    this@ImageCarActivity,
                    "Не удалось создать файл для фото",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            Toast.makeText(this@ImageCarActivity, "Камера не доступна", Toast.LENGTH_SHORT).show()
        }
    }
    //Обрабатывает результаты выбора или съемки
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                REQUEST_IMAGE_PICK -> {
                    if (data?.clipData != null){
                        //Если выбрано несколько изображений
                        val clipData = data.clipData
                        for (i in 0 until clipData!!.itemCount){
                            val uri = clipData.getItemAt(i).uri
                            imageUris.add(uri)
                            Log.d("ImageCarActibity","Добавлен Uri: ${uri}, размер списка: ${imageUris.size}")
                            imageAdapter.notifyItemInserted(imageUris.size - 1)
                            recyclerViewImages.scrollToPosition(imageUris.size - 1)
                            Log.d("ImageCarActivity","Выбрано изображение: $uri")

                        }
                    }else{
                        //Если выбрано одно изображение
                        data?.data?.let { uri ->
                            imageUris.add(uri) //добавление uri  список
                            imageAdapter.notifyItemInserted(imageUris.size - 1) // Уведомляем адаптер
                            recyclerViewImages.scrollToPosition(imageUris.size - 1) // Прокручиваем к новому
                            Log.d("ImageUploadActivity", "Выбрано изображение: $uri")
                        }
                    }

                }

                REQUEST_IMAGE_CAPTURE -> {
                    // Обработка фото с камеры
                    tempPhotoFile?.let { file ->
                        val uri = FileProvider.getUriForFile(
                            this,
                            "${packageName}.fileprovider",
                            file
                        )
                        imageUris.add(uri) // Добавляем Uri в список
                        imageAdapter.notifyItemInserted(imageUris.size - 1) // Уведомляем адаптер
                        recyclerViewImages.scrollToPosition(imageUris.size - 1) // Прокручиваем
                        Log.d("ImageUploadActivity", "Снято фото: $uri")
                    }
                }
            }
        }
    }
    // Загружает изображения в Supabase Storage
    private fun uploadImagesToSupabase() {
        // Запускаем корутину для асинхронной загрузки
        lifecycleScope.launch {
            try {
                // Получаем доступ к бакету car-images
                val storage = supabaseClient.storage.from("carimage")
                // Перебираем все изображения
                imageUris.forEachIndexed { index, uri ->
                    // Формируем уникальное имя файла
                    val fileName = "image_${System.currentTimeMillis()}_$index.jpg"
                    Log.d("ImageUploadActivity", "Загружаем изображение: $fileName")

                    // Читаем байты из Uri
                    val inputStream = contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: throw IllegalStateException("Не удалось прочитать файл")
                    inputStream.close()

                    // Загружаем файл в Supabase
                    storage.upload(fileName, bytes)
                }
                // Обновляем UI в главном потоке
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ImageCarActivity, "Изображения успешно загружены", Toast.LENGTH_SHORT).show()
                    imageUris.clear() // Очищаем список
                    imageAdapter.notifyDataSetChanged() // Обновляем RecyclerView
                    //очистка времменого файла
                    tempPhotoFile?.delete()
                    tempPhotoFile = null

                }
            } catch (e: Exception) {
                // Обрабатываем ошибки
                withContext(Dispatchers.Main) {
                    Log.e("ImageUploadActivity", "Ошибка загрузки: ${e.message}", e)
                    Toast.makeText(this@ImageCarActivity, "Ошибка загрузки: ${e.message}", Toast.LENGTH_LONG).show()
                }
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
