package com.example.yourfavplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.yourfavplaces.R
import com.example.yourfavplaces.models.YourFavPlaceModule
import kotlinx.android.synthetic.main.activity_your_place_details.*

class YourPlaceDetails : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_your_place_details)

        var yourPlaceDetails: YourFavPlaceModule? = null

        if (intent.hasExtra(MainActivity.DETAILS)) {
            yourPlaceDetails = intent.getSerializableExtra(MainActivity.DETAILS) as YourFavPlaceModule
        }

        if(yourPlaceDetails != null) {
            setSupportActionBar(toolbar_your_place_details)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = yourPlaceDetails.title

            toolbar_your_place_details.setNavigationOnClickListener {
                onBackPressed()
            }

            iv_place_image.setImageURI(Uri.parse(yourPlaceDetails.pathOfImage))
            tv_description.text = yourPlaceDetails.description
            tv_location.text = yourPlaceDetails.location

            btn_view_on_map.setOnClickListener{
                val intent = Intent(this, Map::class.java)

                intent.putExtra(MainActivity.DETAILS, yourPlaceDetails)
                startActivity(intent)
            }
        }
    }
}