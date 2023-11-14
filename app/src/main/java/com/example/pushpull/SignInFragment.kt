package com.example.pushpull

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.pushpull.databinding.FragmentHomeBinding
import com.example.pushpull.databinding.FragmentSearchBinding
import com.example.pushpull.databinding.FragmentSignInBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException


class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController() // Initialize navController here



        firebaseAuth = FirebaseAuth.getInstance()


        binding.buttonSingIn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passET.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // Sign in with Firebase
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Navigate to HomeFragment if sign in is successful
                        navController.navigate(R.id.action_signInFragment_to_homeFragment)

                        // Ustawienie podświetlenia na ikonkę Home w BottomNavigationView
                        binding.root.post {
                            val bottomNavigationView = activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
                            bottomNavigationView?.selectedItemId = R.id.homeFragment
                        }
                    } else {
                        // Display custom error message for badly formatted email
                        if (task.exception is FirebaseAuthInvalidCredentialsException &&
                            (task.exception as FirebaseAuthInvalidCredentialsException).errorCode == "ERROR_INVALID_EMAIL") {
                            Toast.makeText(requireContext(), "Podany adres e-mail jest nieprawidłowy.", Toast.LENGTH_SHORT).show()
                        } else {
                            // Display error message for other errors
                            Toast.makeText(requireContext(), task.exception?.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            } else {
                // Display message when fields are empty
                Toast.makeText(requireContext(), "Wypełnij puste pola!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewSignUp.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_signUpFragment)
        }




    }


}