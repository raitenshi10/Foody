package com.example.foodreciepes.bindingadapters

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.navigation.findNavController
import coil.load
import com.example.foodreciepes.R
import com.example.foodreciepes.models.Result
import com.example.foodreciepes.ui.fragments.recipes.RecipesFragmentArgs
import com.example.foodreciepes.ui.fragments.recipes.RecipesFragmentDirections
import org.jsoup.Jsoup

class RecipesRowBinding {

    companion object {

        @BindingAdapter("onRecipesClickListener")
        @JvmStatic
        fun onRecipesClickListener(recipeRowLayout: ConstraintLayout, result: Result) {
            recipeRowLayout.setOnClickListener {
                try {
                    val action =
                        RecipesFragmentDirections.actionRecipesFragmentToDetailsActivity(result)
                    recipeRowLayout.findNavController().navigate(action)
                } catch (e: Exception) {
                    Log.d("onRecipesClickListener", e.toString())
                }
            }
        }

        @BindingAdapter("loadImageFromUrlBinding")
        @JvmStatic
        fun loadImageFromUrl(imageView: ImageView, imageUrl: String) {
            imageView.load(imageUrl) {
                crossfade(600)
                error(R.drawable.ic_sentiment)
            }

        }

        @BindingAdapter("setNumberOfLikes")
        @JvmStatic
        fun setNumberOfLikes(textView: TextView, likes: Int) {
            textView.text = likes.toString()
        }


        @BindingAdapter("setNumberOfMinutes")
        @JvmStatic
        fun setNumberOfMinutes(textView: TextView, minutes: Int) {
            textView.text = minutes.toString()
        }


        @BindingAdapter("setVeganColor")
        @JvmStatic
        fun applyVeganColor(view: View, vegan: Boolean) {
            if (vegan) {
                when (view) {
                    is TextView -> {
                        view.setTextColor(ContextCompat.getColor(view.context, R.color.darkGreen))
                    }
                    is ImageView -> {
                        view.setColorFilter(ContextCompat.getColor(view.context, R.color.darkGreen))
                    }
                }
            }
        }

        @BindingAdapter("parseHtml")
        @JvmStatic
        fun parseHtml(textView: TextView, description: String?) {
            if (description != null) {
                val desc = Jsoup.parse(description).text()
                textView.text = desc
            }
        }
    }
}