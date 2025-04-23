package com.example.demo123

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

// Класс адаптера для RecyclerView, отображающий список изобрвжений
class ImageAdapter(private val imageUris: List<Uri>) : RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {
    //Внутренний класс ViewHolder, хранит ссылку на ImageView для каждого элемента
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView1)
        //ImageView будет содержать изображение, загруженное через Uri
    }
    //Создает новый ImageHolter, раздувая макет item_image.xml
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image, parent, false)
        //раздуваем макет item_image.xml, для одного фото
        return ImageViewHolder(view)
    }
    //Привязывает данные (Uri) к ViewHolter для отображения изображения
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        val uri = imageUris[position] //Получаем Uri изображения из позиции
        Glide.with(holder.imageView.context) //Используется Glide для загрузки изображения
            .load(uri)  //ЗАгружаем изображение из Uri
            .centerCrop() //Обрезаем изображение по центру
            .into(holder.imageView) //Отображаем в ImageWiew
    }
    //Возвращает кол-во изображений в списке
    override fun getItemCount(): Int = imageUris.size
}