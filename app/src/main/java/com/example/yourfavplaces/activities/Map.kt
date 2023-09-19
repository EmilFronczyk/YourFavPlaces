package com.example.yourfavplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.yourfavplaces.R
import com.example.yourfavplaces.models.YourFavPlaceModule
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_map.*

class Map : AppCompatActivity(), OnMapReadyCallback {

    private var mYourPlaceDetails: YourFavPlaceModule? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        if(intent.hasExtra(MainActivity.DETAILS)) {
            mYourPlaceDetails = intent.getSerializableExtra(MainActivity.DETAILS) as YourFavPlaceModule
        }

        if(mYourPlaceDetails != null){
            setSupportActionBar(toolbar_map)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mYourPlaceDetails!!.title

            toolbar_map.setNavigationOnClickListener { onBackPressed() }

            val supportMapFragment: SupportMapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
            supportMapFragment.getMapAsync(this)

        }


    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val position = LatLng(mYourPlaceDetails!!.latitude, mYourPlaceDetails!!.longitude)

        googleMap!!.addMarker(MarkerOptions().position(position).title(mYourPlaceDetails!!.location))
        val newLatLngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatLngZoom)
    }
}