package com.example.yourfavplaces.activities

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.location.LocationManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.yourfavplaces.R
import com.example.yourfavplaces.database.DataBaseHandler
import com.example.yourfavplaces.models.YourFavPlaceModule
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.android.synthetic.main.activity_add_fav_place.*
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class AddFavPlace : AppCompatActivity(), View.OnClickListener {

    private var calen = Calendar.getInstance()
    private lateinit var dateSetListener: DatePickerDialog.OnDateSetListener

    private var saveTakenImageIntoStorage: Uri? = null

    private var defaultLatitude: Double = 0.0
    private var defaultLongitude: Double = 0.0

    private var mYourPlaceDetails: YourFavPlaceModule? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_fav_place)
        setSupportActionBar(toolbar_add_place)

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar_add_place.setNavigationOnClickListener {
            onBackPressed()
        }

        if(!Places.isInitialized()) {
            Places.initialize(this@AddFavPlace, resources.getString(R.string.google_maps_api_key))
        }

        if (intent.hasExtra(MainActivity.DETAILS)) {
            mYourPlaceDetails = intent.getSerializableExtra(MainActivity.DETAILS) as YourFavPlaceModule
        }

        dateSetListener = DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
            calen.set(Calendar.YEAR, year)
            calen.set(Calendar.MONTH, month)
            calen.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateDateInView()
        }

        updateDateInView()

        if (mYourPlaceDetails != null) { //Jesli nie jest null to edytujemy element
            supportActionBar?.title = "Edit Your place"
            et_title.setText(mYourPlaceDetails!!.title)
            et_description.setText(mYourPlaceDetails!!.description)
            et_date.setText(mYourPlaceDetails!!.date)
            et_location.setText(mYourPlaceDetails!!.location)
            defaultLatitude = mYourPlaceDetails!!.latitude
            defaultLongitude = mYourPlaceDetails!!.longitude

            saveTakenImageIntoStorage = Uri.parse(mYourPlaceDetails!!.pathOfImage)

            iv_place_image.setImageURI(saveTakenImageIntoStorage)

            btn_save.text = "UPDATE"
        }

        et_date.setOnClickListener(this)

        tv_add_image.setOnClickListener(this)

        btn_save.setOnClickListener(this)

        et_location.setOnClickListener(this)

        tv_select_current_location.setOnClickListener(this)
    }


    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.et_date -> {
                DatePickerDialog(
                    this@AddFavPlace,
                    dateSetListener, calen.get(Calendar.YEAR),
                    calen.get(Calendar.MONTH),
                    calen.get(Calendar.DAY_OF_MONTH)
                ).show()
            }

            R.id.tv_add_image -> {
                val pickerDialog = AlertDialog.Builder(this)
                pickerDialog.setTitle("Select Action")

                val pictureDialogItems = arrayOf("Select photo from gallery", "Take a picture")
                pickerDialog.setItems(pictureDialogItems) { _, which ->
                    when (which) {
                        0 -> choosePhotoFromGallery()
                        1 -> useCamera()
                    }
                }
                pickerDialog.show()
            }

            R.id.et_location -> {
                try {

                    val fields = listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG, Place.Field.ADDRESS)

                    val intent = Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).build(this@AddFavPlace)
                    startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)

                }catch (e:Exception){
                    e.printStackTrace()
                }
            }

            R.id.tv_select_current_location -> {
                if(!isLocationPermissionEnabled()) {
                    Toast.makeText(this@AddFavPlace, "The location permissions are disabled, change it in order to proceed", Toast.LENGTH_LONG).show()

                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else {
                    Dexter.withContext(this).withPermissions(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                            if (report!!.areAllPermissionsGranted()) {
                                Toast.makeText(
                                    this@AddFavPlace,
                                    "Location permission is granted. Now you can request for a current location.",
                                    Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            permissions: MutableList<PermissionRequest>?,
                            token: PermissionToken?
                        ) {
                            showRationalDialog()
                        }
                    }).onSameThread().check()
                }
            }

            R.id.btn_save -> {
                when {
                    et_title.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddFavPlace, "Enter the title in order to proceed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    et_description.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddFavPlace, "Enter the description in order to proceed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    et_location.text.isNullOrEmpty() -> {
                        Toast.makeText(this@AddFavPlace, "Enter the location in order to proceed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    saveTakenImageIntoStorage == null -> {
                        Toast.makeText(this@AddFavPlace, "Select the image in order to proceed",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                    else -> {
                        val yourFavPlaceModule = YourFavPlaceModule(
                            if (mYourPlaceDetails == null) 0 else mYourPlaceDetails!!.id, // sprawdzamy czy element istnieje
                            et_title.text.toString(),
                            saveTakenImageIntoStorage.toString(),
                            et_description.text.toString(),
                            et_date.text.toString(),
                            et_location.text.toString(),
                            defaultLatitude,
                            defaultLongitude
                        )
                        val dbHandler = DataBaseHandler(this)

                        if (mYourPlaceDetails == null) {
                            val addYourFavPlace = dbHandler.addYourFavPlace(yourFavPlaceModule)
                            if (addYourFavPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this@AddFavPlace, "The place added successfully",
                                    Toast.LENGTH_LONG
                                ).show()
                                finish()
                            }
                        } else {
                            val updateYourFavPlace = dbHandler.updateYourFavPlace(yourFavPlaceModule)
                            if (updateYourFavPlace > 0) {
                                setResult(Activity.RESULT_OK)
                                Toast.makeText(this@AddFavPlace, "The place updated successfully", Toast.LENGTH_LONG).show()
                                finish()

                            }
                        }
                    }
                }
            }

        }
    }

        private fun updateDateInView() {
            val format = "dd.MM.yyyy"
            val sdf = SimpleDateFormat(format, Locale.getDefault())
            et_date.setText(sdf.format(calen.time).toString())
        }

        private fun showRationalDialog() {
            AlertDialog.Builder(this)
                .setMessage("Permissions required for this app to work are turned off. Enable them in Application settings.")
                .setPositiveButton("Go to settings.") { _, _ ->
                    try {
                        val attempt =
                            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS) //proba wejscia do ustawien
                        val uri = Uri.fromParts("package", packageName, null)
                        attempt.data = uri
                        startActivity(attempt)
                    } catch (e: ActivityNotFoundException) {
                        e.printStackTrace()
                    }
                }.setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
        }

        public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
            super.onActivityResult(requestCode, resultCode, data)
            if (resultCode == Activity.RESULT_OK) {
                if (requestCode == GALLERY) {
                    if (data != null) {
                        val contentUri = data.data
                        try {
                            val selectedImage =
                                MediaStore.Images.Media.getBitmap(this.contentResolver, contentUri)
                            saveTakenImageIntoStorage = saveImageIntoStorage(selectedImage)
                            iv_place_image!!.setImageBitmap(selectedImage)
                        } catch (e: IOException) {
                            e.printStackTrace()
                            Toast.makeText(this@AddFavPlace, "Failed", Toast.LENGTH_LONG).show()
                        }
                    }
                } else if (requestCode == CAMERA) {
                    val cam: Bitmap = data!!.extras!!.get("data") as Bitmap //Pobieramy "data" z obrazu przechwyconego przez kamere i zamieniamy na bitmap'e
                    saveTakenImageIntoStorage = saveImageIntoStorage(cam)
                    iv_place_image!!.setImageBitmap(cam)

                } else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE) {
                    val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                    et_location.setText(place.address)
                    defaultLatitude = place.latLng!!.latitude
                    defaultLongitude = place.latLng!!.longitude

                }
            }

        }

        private fun useCamera() {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_MEDIA_IMAGES,
                Manifest.permission.CAMERA
            ).withListener(object :
                MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(cameraIntent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showRationalDialog()
                }
            }).onSameThread().check()
        }

        private fun choosePhotoFromGallery() {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_MEDIA_IMAGES
            ).withListener(object :
                MultiplePermissionsListener { //To do -> sprawdzic później czy działa
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    if (report!!.areAllPermissionsGranted()) {
                        val galleryIntent =
                            Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>,
                    token: PermissionToken
                ) {
                    showRationalDialog()
                }
            }).onSameThread().check()
        }

        private fun saveImageIntoStorage(bitmap: Bitmap): Uri {
            val wrapper =
                ContextWrapper(applicationContext) // wrapper jest potrzebny aby dostac konktreny miejsce w pamieci telefonu gdzie apka bedzie zapisywac rzeczy
            var file = wrapper.getDir(
                IMAGE_DIRECTORY,
                Context.MODE_PRIVATE
            ) //Mode private, inne aplikacje nie beda mialy dostepu do tego katalogu
            file = File(file, "${UUID.randomUUID()}.jpg") //randomowa, unikalna nazwa pliku

            try {
                val stream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                stream.flush()
                stream.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return Uri.parse(file.absolutePath)
        }

        private fun isLocationPermissionEnabled(): Boolean {
        val locationManager: LocationManager = getSystemService(Context.LOCALE_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        }

        companion object {
            private const val GALLERY = 1
            private const val CAMERA = 2
            private const val IMAGE_DIRECTORY = "YourFavPlacesImages"
            private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
        }

    }


