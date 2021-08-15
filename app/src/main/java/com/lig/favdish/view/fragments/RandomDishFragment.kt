package com.lig.favdish.view.fragments

import android.os.Build
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.lig.favdish.R
import com.lig.favdish.application.FavDishApplication
import com.lig.favdish.databinding.FragmentRandomDishBinding
import com.lig.favdish.model.entities.FavDish
import com.lig.favdish.model.entities.RandomDish
import com.lig.favdish.util.Constants
import com.lig.favdish.viewmodel.FavDishViewModel
import com.lig.favdish.viewmodel.FavDishViewModelFactory
import com.lig.favdish.viewmodel.NotificationsViewModel
import com.lig.favdish.viewmodel.RandomDishViewModel

class RandomDishFragment : Fragment() {

    private lateinit var notificationsViewModel: NotificationsViewModel
    private var _binding: FragmentRandomDishBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var randomDishViewModel: RandomDishViewModel


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        notificationsViewModel =
            ViewModelProvider(this).get(NotificationsViewModel::class.java)

        _binding = FragmentRandomDishBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        randomDishViewModel = ViewModelProvider(this).get(RandomDishViewModel::class.java)
        randomDishViewModel.getRandomRecipeFromApi()

        randomDishViewModelObserver()

        binding.srlRandomDish.setOnRefreshListener {
            randomDishViewModel.getRandomRecipeFromApi()
        }
    }

    private fun randomDishViewModelObserver() {
        randomDishViewModel.randomDishResponse.observe(viewLifecycleOwner,
            { randomDishResponse ->
                randomDishResponse?.let {
                    Log.d("randomDish", "lig !!! $it")
                    if (binding.srlRandomDish.isRefreshing) {
                        binding.srlRandomDish.isRefreshing = false
                    }
                    setRandomDishResponseInUI(randomDishResponse.recipes[0])
                }
            }
        )
        randomDishViewModel.randomDishLoadingError.observe(viewLifecycleOwner,
            { error ->
                error?.let {
                    if (binding.srlRandomDish.isRefreshing) {
                        binding.srlRandomDish.isRefreshing = false
                    }
                }
            }
        )
        randomDishViewModel.loadRandomDish.observe(viewLifecycleOwner, { loadRandomDish ->
            loadRandomDish?.let {

            }
        })

    }

    private fun setRandomDishResponseInUI(recipe: RandomDish.Recipe) {
        Glide.with(requireActivity())
            .load(recipe.image)
            .centerCrop()
            .into(binding.ivDishImage)

        binding.apply {
            tvTitle.text = recipe.title
            var dishType: String = "other"
            if (recipe.dishTypes.isNotEmpty()) {
                dishType = recipe.dishTypes[0]
                tvType.text = dishType
            }
            tvCategory.text = "Other"
            var ingredients = ""
            for (value in recipe.extendedIngredients) {
                if (ingredients.isEmpty()) {
                    ingredients = value.original
                } else {
                    ingredients = ingredients + ", \n" + value.original
                }
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // read html format
                tvCookingDirection.text = Html.fromHtml(
                    recipe.instructions,
                    Html.FROM_HTML_MODE_COMPACT
                )
            } else {
                @Suppress("DEPRECATION")
                tvCookingDirection.text = Html.fromHtml(recipe.instructions)
            }

            binding.ivFavoriteDish.setImageDrawable(
                ContextCompat.getDrawable(
                    requireActivity(),
                    R.drawable.ic_favorite_unselected
                )
            )

            var addedToFavorites = false

            tvCookingTime.text = resources.getString(
                R.string.lbl_cooking_time_in_minutes,
                recipe.readyInMinutes.toString()
            )
            val mFavDishViewModel: FavDishViewModel by viewModels {
                FavDishViewModelFactory((requireActivity().application as FavDishApplication).repository)
            }

            ivFavoriteDish.setOnClickListener {
                if (addedToFavorites) {
                    Toast.makeText(
                        requireActivity(),
                        resources.getString(R.string.msg_already_added_to_favorites),
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    val randomDishDetails = FavDish(
                        recipe.image,
                        Constants.DISH_IMAGE_SOURCE_ONLINE,
                        recipe.title,
                        dishType,
                        "other",
                        ingredients,
                        recipe.readyInMinutes.toString(),
                        recipe.instructions,
                        true
                    )
                    mFavDishViewModel.insert(randomDishDetails)

                    addedToFavorites = true

                    ivFavoriteDish.setImageDrawable(
                        ContextCompat.getDrawable(
                            requireActivity(),
                            R.drawable.ic_favorite_selected
                        )
                    )
                }
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}