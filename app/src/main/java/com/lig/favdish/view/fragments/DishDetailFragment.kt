package com.lig.favdish.view.fragments

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.*
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.palette.graphics.Palette
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.lig.favdish.R
import com.lig.favdish.application.FavDishApplication
import com.lig.favdish.databinding.FragmentDishDetailBinding
import com.lig.favdish.model.entities.FavDish
import com.lig.favdish.util.Constants
import com.lig.favdish.viewmodel.FavDishViewModel
import com.lig.favdish.viewmodel.FavDishViewModelFactory
import java.io.IOException


class DishDetailFragment : Fragment() {

    private var mFavDishDetails: FavDish? = null
    private var mBinding: FragmentDishDetailBinding? = null
    private val mFavDishViewModel: FavDishViewModel by viewModels {
        FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_share, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_share_dish -> {
                val type = "text/plain"
                val subject = "Check out dish recipe"
                var extraText = ""
                val shareWith = "Share with"

                mFavDishDetails?.let {
                    var image = ""
                    if (it.imageSource == Constants.DISH_IMAGE_SOURCE_ONLINE) {
                        image = it.image
                    }
                    var cookingInstructions = ""
                    cookingInstructions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        Html.fromHtml(
                            it.directionToCook,
                            Html.FROM_HTML_MODE_COMPACT
                        ).toString()
                    } else {
                        @Suppress("DEPRECATION")
                        Html.fromHtml(it.directionToCook).toString()
                    }

                    extraText =
                        "$image \n" +
                                "\n Title:  ${it.title} \n\n Type: ${it.type} \n\n Category: ${it.category}" +
                                "\n\n Ingredients: \n ${it.ingredients} \n\n Instructions To Cook: \n $cookingInstructions" +
                                "\n\n Time required to cook the dish approx ${it.cookingTime} minutes."
                }
                val intent = Intent(Intent.ACTION_SEND)
                intent.type = type
                intent.putExtra(Intent.EXTRA_SUBJECT, subject)
                intent.putExtra(Intent.EXTRA_TEXT, extraText)
                startActivity(Intent.createChooser(intent, shareWith))
                return true
            }
        }
        return super.onOptionsItemSelected(item)

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
        mFavDishDetails = args.dishDetails
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
                tvCookingTime.text = resources.getString(
                    R.string.lbl_cooking_time_in_minutes,
                    detail.dishDetails.cookingTime
                )

                tvCookingDirection.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    Html.fromHtml(
                        detail.dishDetails.directionToCook,
                        Html.FROM_HTML_MODE_COMPACT
                    ).toString()
                } else {
                    @Suppress("DEPRECATION")
                    Html.fromHtml(detail.dishDetails.directionToCook).toString()
                }

                ivFavoriteDish.setImageResource(
                    when (args.dishDetails.favoriteDish) {
                        true -> R.drawable.ic_favorite_selected
                        false -> R.drawable.ic_favorite_unselected
                    }
                )

                ivFavoriteDish.setOnClickListener {
                    args.dishDetails.favoriteDish = !args.dishDetails.favoriteDish
                    mFavDishViewModel.update(args.dishDetails)

                    ivFavoriteDish.setImageResource(
                        when (args.dishDetails.favoriteDish) {
                            true -> R.drawable.ic_favorite_selected
                            false -> R.drawable.ic_favorite_unselected
                        }
                    )
                }
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