package com.lig.favdish.view.activities

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionDeniedResponse
import com.karumi.dexter.listener.PermissionGrantedResponse
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.karumi.dexter.listener.single.PermissionListener
import com.lig.favdish.R
import com.lig.favdish.application.FavDishApplication
import com.lig.favdish.databinding.ActivityAddUpdateDishBinding
import com.lig.favdish.databinding.DialogCustomImageSelectionBinding
import com.lig.favdish.databinding.DialogCustomListBinding
import com.lig.favdish.model.entities.FavDish
import com.lig.favdish.util.Constants
import com.lig.favdish.view.adapters.CustomListItemAdapter
import com.lig.favdish.viewmodel.FavDishViewModel
import com.lig.favdish.viewmodel.FavDishViewModelFactory
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.util.*

class AddUpdateDishActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var mBinding: ActivityAddUpdateDishBinding
    private var mImagePath: String = ""

    private lateinit var mCustomListDialog: Dialog

    private var mFavDishDetails: FavDish? = null

    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((application as FavDishApplication).repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityAddUpdateDishBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        if (intent.hasExtra(Constants.EXTRA_DISH_DETAILS)) {
            mFavDishDetails = intent.getParcelableExtra(Constants.EXTRA_DISH_DETAILS)
        }

        setActionBar()
        mFavDishDetails?.let {
            if (it.id != 0) {
                mImagePath = it.image
                Glide.with(this@AddUpdateDishActivity)
                    .load(mImagePath)
                    .centerCrop()
                    .into(mBinding.ivDishImage)

                mBinding.etTitle.setText(it.title)
                mBinding.etType.setText(it.type)
                mBinding.etCategory.setText(it.category)
                mBinding.etIngredients.setText(it.ingredients)
                mBinding.etCookingTime.setText(it.cookingTime)
                mBinding.etDirectionToCook.setText(it.directionToCook)
                mBinding.btnAddDish.text = resources.getString(R.string.lbl_update_dish)
            }
        }
        mBinding.ivAddDishImage.setOnClickListener(this)
        mBinding.etType.setOnClickListener(this)
        mBinding.etCategory.setOnClickListener(this)
        mBinding.etCookingTime.setOnClickListener(this)
        mBinding.btnAddDish.setOnClickListener(this)

    }


    private fun setActionBar() {
        setSupportActionBar(mBinding.toolbarAddDishActivity)
        supportActionBar?.let {
            it.title = if (mFavDishDetails != null && mFavDishDetails!!.id != 0) {
                resources.getString(R.string.title_edit_dish)
            } else {
                resources.getString(R.string.title_add_dish)
            }
        }

        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)
        mBinding.toolbarAddDishActivity.setNavigationOnClickListener {
            onBackPressed()
        }
    }

    override fun onClick(v: View?) {
        if (v != null) {
            when (v.id) {
                R.id.iv_add_dish_image -> {
                    customImageSelectionDialog()
                    return
                }
                R.id.et_type -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_type),
                        Constants.dishTypes(),
                        Constants.DISH_TYPE
                    )
                }
                R.id.et_category -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_category),
                        Constants.dishCategories(),
                        Constants.DISH_CATEGORY
                    )
                }
                R.id.et_cooking_time -> {
                    customItemsListDialog(
                        resources.getString(R.string.title_select_dish_cooking_time),
                        Constants.dishCookTime(),
                        Constants.DISH_COOKING_TIME
                    )
                }
                R.id.btn_add_dish -> {
                    // remove empty space
                    val title = mBinding.etTitle.text.toString().trim { it <= ' ' }
                    val type = mBinding.etType.text.toString().trim { it <= ' ' }
                    val category = mBinding.etCategory.text.toString().trim { it <= ' ' }
                    val ingredients = mBinding.etIngredients.text.toString().trim { it <= ' ' }
                    val cookingTimeInMinutes =
                        mBinding.etCookingTime.text.toString().trim { it <= ' ' }
                    val cookingDirection =
                        mBinding.etDirectionToCook.text.toString().trim { it <= ' ' }

                    when {
                        TextUtils.isEmpty(mImagePath) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_image),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(title) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_title),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(type) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_type),
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                        TextUtils.isEmpty(category) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_category),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(ingredients) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_ingredients),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingTimeInMinutes) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_select_dish_cooking_time),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        TextUtils.isEmpty(cookingDirection) -> {
                            Toast.makeText(
                                this@AddUpdateDishActivity,
                                resources.getString(R.string.err_msg_enter_dish_cooking_instructions),
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            var dishID = 0
                            var imageSource = Constants.DISH_IMAGE_SOURCE_LOCAL
                            var favoriteDish = false

                            mFavDishDetails?.let {
                                if (it.id != 0) {
                                    dishID = it.id
                                    imageSource = it.imageSource
                                    favoriteDish = it.favoriteDish
                                }
                            }

                            val favDishDetails: FavDish = FavDish(
                                mImagePath,
                                imageSource,
                                title,
                                type,
                                category,
                                ingredients,
                                cookingTimeInMinutes,
                                cookingDirection,
                                favoriteDish,
                                dishID
                            )

                            if (dishID == 0) {
                                mFavDishViewModel.insert(favDishDetails)
                                Toast.makeText(
                                    this@AddUpdateDishActivity,
                                    resources.getString(R.string.title_msg_enter_dish_add),
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                               mFavDishViewModel.update(favDishDetails)
                                Toast.makeText(
                                    this@AddUpdateDishActivity,
                                    resources.getString(R.string.title_msg_enter_dish_update),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }

                            finish()
                        }
                    }
                }
            }
        }
    }


    val startForCameraResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                result?.data?.extras?.let {
                    val thumbnail: Bitmap = result.data!!.extras!!.get("data") as Bitmap
                    Glide.with(this)
                        .load(thumbnail)
                        .centerCrop()
                        .into(mBinding.ivDishImage)
                    mImagePath = saveImageToInternalStorage(thumbnail)
                    Log.i("ImagePath", mImagePath)
                    mBinding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_edit
                        )
                    )
                }
            }
        }

    val startForGalleryResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult())
        { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {

                result?.data?.let {
                    val selectedPhototUri = result.data!!.data
                    Log.i("ImagePath0:", selectedPhototUri.toString())

                    Glide.with(this)
                        .load(selectedPhototUri)
                        .centerCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .listener(object : RequestListener<Drawable> {
                            override fun onLoadFailed(
                                e: GlideException?,
                                model: Any?,
                                target: Target<Drawable>?,
                                isFirstResource: Boolean
                            ): Boolean {
                                return false
                            }

                            override fun onResourceReady(
                                resource: Drawable?,
                                model: Any?,
                                target: Target<Drawable>?,
                                dataSource: DataSource?,
                                isFirstResource: Boolean
                            ): Boolean {
                                resource?.let {
                                    val bitmap: Bitmap = resource.toBitmap()
                                    mImagePath = saveImageToInternalStorage(bitmap)
                                    Log.i("ImagePath", mImagePath)

                                }
                                //true if the listener has handled setting the resource on the target,
                                // false to allow Glide's request to update the target.
                                // Setting the resource includes handling animations, be sure to take that into account.
                                return false
                            }

                        })
                        .into(mBinding.ivDishImage)

                    mBinding.ivAddDishImage.setImageDrawable(
                        ContextCompat.getDrawable(
                            this,
                            R.drawable.ic_edit
                        )
                    )
                }
            } else if (result.resultCode == Activity.RESULT_CANCELED) {
                Log.e("Cancelled", "User cancl selection")
            }
        }


    private fun customImageSelectionDialog() {
        val dialog = Dialog(this)
        val binding: DialogCustomImageSelectionBinding =
            DialogCustomImageSelectionBinding.inflate(layoutInflater)
        dialog.setContentView(binding.root)

        binding.tvCamera.setOnClickListener {
            Dexter.withContext(this).withPermissions(
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                    report?.let {
                        if (report.areAllPermissionsGranted()) {
                            val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                            startForCameraResult.launch(intent)
                        }
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: MutableList<PermissionRequest>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }

            ).onSameThread().check()

            dialog.dismiss()
        }
        binding.tvGallery.setOnClickListener {
            Dexter.withContext(this).withPermission(
                Manifest.permission.READ_EXTERNAL_STORAGE,
            ).withListener(object : PermissionListener {
                override fun onPermissionGranted(p0: PermissionGrantedResponse?) {
                    val galleryIntent =
                        Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startForGalleryResult.launch(galleryIntent)
                }

                override fun onPermissionDenied(p0: PermissionDeniedResponse?) {
                    Toast.makeText(
                        this@AddUpdateDishActivity,
                        "You have denied gallery permission",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onPermissionRationaleShouldBeShown(
                    p0: PermissionRequest?,
                    p1: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }

            }

            ).onSameThread().check()
            dialog.dismiss()

        }
        dialog.show()
    }


    private fun showRationalDialogForPermissions() {
        AlertDialog.Builder(this)
            .setMessage("You can enable the permission under applications settings")
            .setPositiveButton("GO TO SETTINGS") { _, _ ->
                try {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    // find the package directly in the setting
                    val uri = Uri.fromParts("package", packageName, null)
                    intent.data = uri
                    startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    e.printStackTrace()
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }.show()
    }

    private fun saveImageToInternalStorage(bitmap: Bitmap): String {
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file, "${UUID.randomUUID()}.jpg")

        try {
            val stream: OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
            stream.flush()
            stream.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }

        return file.absolutePath

    }

    private fun customItemsListDialog(
        titie: String,
        itemList: List<String>,
        selection: String
    ) {
        mCustomListDialog = Dialog(this)
        val binding: DialogCustomListBinding = DialogCustomListBinding.inflate(layoutInflater)
        mCustomListDialog.setContentView(binding.root)
        binding.tvTitle.text = titie
        binding.rvList.layoutManager = LinearLayoutManager(this)
        val adapter = CustomListItemAdapter(this, null, itemList, selection)
        binding.rvList.adapter = adapter
        mCustomListDialog.show()

    }


    fun selectedListItem(item: String, selection: String) {
        when (selection) {
            Constants.DISH_TYPE -> {
                mCustomListDialog.dismiss()
                mBinding.etType.setText(item)
            }
            Constants.DISH_COOKING_TIME -> {
                mCustomListDialog.dismiss()
                mBinding.etCookingTime.setText(item)
            }
            Constants.DISH_CATEGORY -> {
                mCustomListDialog.dismiss()
                mBinding.etCategory.setText(item)
            }
            else -> throw UnknownError("Unknown item selection")
        }
    }

    companion object {
        private const val IMAGE_DIRECTORY = "FavDishImage"
    }

}