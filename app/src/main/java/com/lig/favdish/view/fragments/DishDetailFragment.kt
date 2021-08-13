package com.lig.favdish.view.fragments

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lig.favdish.R
import com.lig.favdish.databinding.FragmentDishDetailBinding
import java.io.IOException


class DishDetailFragment : Fragment() {

    private var mBinding: FragmentDishDetailBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mBinding = FragmentDishDetailBinding.inflate(inflater, container, false)
        return mBinding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val args: DishDetailFragmentArgs by navArgs()
        Log.i("Dish Title", args.dishDetails.title)
        args.let { detail ->
            try {
                Glide.with(requireActivity())
                    .load(detail.dishDetails.image)
                    .centerCrop()
                    .listener(object : RequestListener<Drawable> {
                        override fun onLoadFailed(
                            e: GlideException?,
                            model: Any?,
                            target: Target<Drawable>?,
                            isFirstResource: Boolean
                        ): Boolean {
                            Log.e("TAG", "Error loading image", e)
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
                                Palette.from(resource.toBitmap()).generate() { palette ->
                                    val inColor = palette?.vibrantSwatch?.rgb ?: 0
                                    mBinding!!.rlDishDetailMain.setBackgroundColor(inColor)
                                }
                            }
                            return false
                        }

                    })
                    .into(mBinding!!.ivDishImage)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            mBinding?.apply {
                tvTitle.text = detail.dishDetails.title
                tvType.text = detail.dishDetails.type.replaceFirstChar { it.uppercase() }
                tvCategory.text = detail.dishDetails.category
                tvIngredients.text = detail.dishDetails.ingredients
                tvCookingDirection.text = detail.dishDetails.directionToCook
                tvCookingTime.text = resources.getString(
                    R.string.lbl_cooking_time_in_minutes,
                    detail.dishDetails.cookingTime
                )
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        mBinding = null
    }

    companion object {

    }
}