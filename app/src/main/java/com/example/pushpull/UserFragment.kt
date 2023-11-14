package com.example.pushpull

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.example.pushpull.databinding.FragmentSignUpBinding
import com.example.pushpull.databinding.FragmentTrainingBinding
import com.example.pushpull.databinding.FragmentUserBinding
import com.google.firebase.auth.FirebaseAuth


class UserFragment : Fragment() {
    private lateinit var binding: FragmentUserBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonLogout.setOnClickListener {
            // Wylogowanie użytkownika
            FirebaseAuth.getInstance().signOut()

            // Nawigacja do SignInFragment z wyczyszczeniem stosu nawigacyjnego
            val navOptions = NavOptions.Builder()
                .setPopUpTo(R.id.nav_graph, true) // Wyczyść cały stos nawigacyjny
                .build()
            findNavController().navigate(R.id.signInFragment, null, navOptions)
        }
    }


}