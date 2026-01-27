package com.example.myapplication3

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.myapplication3.R.* // Note: This import should typically be just 'import com.example.myapplication3.R'

class SplashFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        // 1. Define NavOptions to configure the navigation behavior
        val navOptions = NavOptions.Builder()
            // Pop up to the splashFragment ID
            .setPopUpTo(R.id.splashFragment, true)
            // The 'true' argument ensures the splashFragment itself is removed (inclusive)
            .build()

        // 2. Delayed navigation using the configured NavOptions
        Handler(Looper.getMainLooper()).postDelayed({
            // Pass the action ID, arguments (null), and the NavOptions
            findNavController().navigate(R.id.action_splashFragment_to_uploadFragment, null, navOptions)
        }, 3000)

        // Inflate the layout for this fragment
        val view =  inflater.inflate(layout.fragment_splash, container, false)
        return view
    }

}