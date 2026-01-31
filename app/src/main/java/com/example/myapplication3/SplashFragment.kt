package com.example.myapplication3

// In SplashFragment.kt

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Just inflate the view here
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // All UI logic and navigation should go in onViewCreated
        Handler(Looper.getMainLooper()).postDelayed({
            // Check if the fragment is still added to the activity before navigating
            if (isAdded) {
                // THIS IS NOW SAFE
                findNavController().navigate(R.id.action_splashFragment_to_uploadFragment)
            }
        }, 3000) // Example delay of 3 seconds
    }
}
