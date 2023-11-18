package com.example.pushpull

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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


        // Assuming the user is already logged in and their email is verified
        val user = FirebaseAuth.getInstance().currentUser
        user?.let {
            // Update the TextView with the user's email
            binding.textViewAboveButton.text = getString(R.string.email_format, it.email)
        }

        binding.buttonChangePassword.setOnClickListener {
            user?.email?.let { email ->
                sendPasswordResetEmail(email)
            }
        }

        binding.buttonLogout.setOnClickListener {
            // Wylogowanie użytkownika
            FirebaseAuth.getInstance().signOut()
            navigateToSignIn()

        }
    }

    private fun sendPasswordResetEmail(email: String) {
        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Notify the user to check their email
                    Toast.makeText(context, "Mail ze zmianą hasła wysłany.", Toast.LENGTH_LONG).show()
                } else {
                    // Notify the user that there was an error
                    Toast.makeText(context, "Błąd z wysłaniem maila zmiany hasła.", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun navigateToSignIn() {
        val navOptions = NavOptions.Builder()
            .setPopUpTo(R.id.nav_graph, true)
            .build()
        findNavController().navigate(R.id.signInFragment, null, navOptions)
    }

}