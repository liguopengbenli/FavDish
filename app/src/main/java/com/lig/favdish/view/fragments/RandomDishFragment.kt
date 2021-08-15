package com.lig.favdish.view.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.lig.favdish.databinding.FragmentRandomDishBinding
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
    }

    private fun randomDishViewModelObserver() {
        randomDishViewModel.randomDishResponse.observe(viewLifecycleOwner,
            { randomDishResponse ->
                randomDishResponse?.let {
                    Log.d("randomDish", "lig !!! $it")
                }
            }
        )
        randomDishViewModel.randomDishLoadingError.observe(viewLifecycleOwner,
            { error ->
                error?.let {

                }
            }
        )
        randomDishViewModel.loadRandomDish.observe(viewLifecycleOwner, { loadRandomDish ->
            loadRandomDish?.let {

            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}