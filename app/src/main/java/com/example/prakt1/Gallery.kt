package com.example.prakt1

import android.content.ContentUris
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager.widget.ViewPager


class Gallery : AppCompatActivity() {
    // creating object of ViewPager
    var mViewPager: ViewPager? = null

    // images array
    var images = ArrayList<Uri>()

    // Creating Object of ViewPagerAdapter
    var mViewPagerAdapter: ViewPagerAdapter? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gallery)
        this.title = "Gallery"


        data class Image(val uri: Uri, val name: String)
        val imgList = mutableListOf<Image>()

        val collection =
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
            }
        val projection = arrayOf(
            MediaStore.Images.Media._ID,
            MediaStore.Images.Media.DISPLAY_NAME
        )

        val sortOrder = "${MediaStore.Images.Media.DISPLAY_NAME} ASC"
        val query = contentResolver.query(collection, projection, null, null, null)

        query?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID)
            val nameColumn =
                cursor.getColumnIndexOrThrow((MediaStore.Images.Media.DISPLAY_NAME))

            while (cursor.moveToNext()) {
                val id = cursor.getLong(idColumn)
                val name = cursor.getString(nameColumn)

                val contentUri: Uri = ContentUris.withAppendedId(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    id
                )

                imgList += Image(contentUri, name)
                images = (images + contentUri) as ArrayList<Uri>
            }
        }








        // Initializing the ViewPager Object
        mViewPager = findViewById<View>(R.id.viewPagerMain) as ViewPager

        // Initializing the ViewPagerAdapter
        mViewPagerAdapter = ViewPagerAdapter(this@Gallery, images)

        // Adding the Adapter to the ViewPager
        mViewPager!!.adapter = mViewPagerAdapter
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menu.add(0, 0, 0, "Audio")
        menu.add(0, 1, 0, "Camera")
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        //when item selected by ID do this that whatever
        if (item.itemId == 0) {
            val intentAudio = Intent(this@Gallery, AudioActivity::class.java)
            startActivity(intentAudio)
        }
        if (item.itemId == 1) {
            val intentGallery = Intent(this@Gallery, MainActivity::class.java)
            startActivity(intentGallery)
        }
        return super.onOptionsItemSelected(item)
    }
}

