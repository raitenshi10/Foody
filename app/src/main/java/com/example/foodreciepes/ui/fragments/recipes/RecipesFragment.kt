package com.example.foodreciepes.ui.fragments.recipes

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.foodreciepes.viewmodel.MainViewModel
import com.example.foodreciepes.R
import com.example.foodreciepes.viewmodel.RecipesViewModel
import com.example.foodreciepes.adapters.RecipesAdapter
import com.example.foodreciepes.databinding.FragmentRecipesBinding
import com.example.foodreciepes.util.MyExtensionFunction.Companion.observeOnce
import com.example.foodreciepes.util.MyExtensionFunction.Companion.showToast
import com.example.foodreciepes.util.NetworkListener
import com.example.foodreciepes.util.NetworkResult
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class RecipesFragment : Fragment(), SearchView.OnQueryTextListener {


    // SafeArgs initialization
    private val args by navArgs<RecipesFragmentArgs>()

    // DataBinding Initialization
    private var _binding: FragmentRecipesBinding? = null
    private val binding get() = _binding!!

    // ViewModel and RecyclerView initialization
    private lateinit var mainViewModel: MainViewModel
    private lateinit var recipeViewModel: RecipesViewModel
    private val mAdapter by lazy { RecipesAdapter() }

    // NetworkListener initialization
    private lateinit var networkListener: NetworkListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mainViewModel = ViewModelProvider(requireActivity())[MainViewModel::class.java]
        recipeViewModel = ViewModelProvider(requireActivity())[RecipesViewModel::class.java]
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRecipesBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = this
        binding.mainViewModel = mainViewModel
        setHasOptionsMenu(true)


        setUpRecyclerView()

        recipeViewModel.readBackOnline.observe(viewLifecycleOwner, {
            recipeViewModel.backOnline = it
        })

        lifecycleScope.launch {
            networkListener = NetworkListener()
            networkListener.checkNetworkAvailability(requireContext())
                .collect { status ->
                    recipeViewModel.networkStatus = status
                    recipeViewModel.showNetworkStatus()
                    readDatabase()
                }
        }


        binding.recipesFab.setOnClickListener {
            if (recipeViewModel.networkStatus) {
                findNavController().navigate(R.id.action_recipesFragment_to_recipesBottomSheet)
            } else {
                recipeViewModel.showNetworkStatus()
            }
        }
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.recipes_menu, menu)

        val search = menu.findItem(R.id.menu_search)
        val searchView = search.actionView as? SearchView
        searchView?.isSubmitButtonEnabled = true
        searchView?.setOnQueryTextListener(this)

    }

    override fun onQueryTextSubmit(query: String?): Boolean {
        if (query != null) {
            searchApiData(query)
        }
        return true
    }

    override fun onQueryTextChange(newText: String?): Boolean {
        return false
    }

    private fun readDatabase() {
        lifecycleScope.launch {
            mainViewModel.readRecipes.observeOnce(viewLifecycleOwner, { database ->
                if (database.isNotEmpty() && !args.backFromBottomSheet) {
                    mAdapter.setData(database[0].foodRecipe)
                    hideShimmerEffect()
                } else {
                    requestApiData()
                }
            })
        }
    }

    private fun requestApiData() {
        mainViewModel.getRecipes(recipeViewModel.applyQueries())
        mainViewModel.recipesResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is NetworkResult.Success -> {
                    hideShimmerEffect()
                    response.data?.let { mAdapter.setData(it) }
                }
                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    loadFataFromCache()
                    Toast.makeText(
                        requireContext(),
                        response.message.toString(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
                is NetworkResult.Loading -> {
                    showShimmerEffect()
                }
            }
        })
    }

    private fun searchApiData(searchQuery: String) {
        showShimmerEffect()
        mainViewModel.searchRecipes(recipeViewModel.applySearchQueries(searchQuery))
        mainViewModel.searchedRecipesResponse.observe(viewLifecycleOwner, { response ->
            when (response) {
                is NetworkResult.Success -> {
                    hideShimmerEffect()
                    val foodRecipe = response.data
                    foodRecipe?.let { mAdapter.setData(it) }
                }
                is NetworkResult.Error -> {
                    hideShimmerEffect()
                    loadFataFromCache()
                    showToast(requireContext(), response.message.toString())
                }
                is NetworkResult.Loading -> {
                    showShimmerEffect()
                }
            }
        })
    }

    private fun loadFataFromCache() {
        lifecycleScope.launch {
            mainViewModel.readRecipes.observe(viewLifecycleOwner, { database ->
                if (database.isNotEmpty()) {
                    mAdapter.setData(database[0].foodRecipe)
                }
            })
        }
    }

    private fun setUpRecyclerView() {
        binding.rvShimmer.adapter = mAdapter
        binding.rvShimmer.layoutManager = LinearLayoutManager(requireContext())
        showShimmerEffect()
    }

    private fun showShimmerEffect() {
        binding.rvShimmer.showShimmer()
    }

    private fun hideShimmerEffect() {
        binding.rvShimmer.hideShimmer()
    }


}