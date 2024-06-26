package com.example.pushpull.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pushpull.R
import com.example.pushpull.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException


class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()



        firebaseAuth = FirebaseAuth.getInstance()

        binding.buttonSignUp.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passET.text.toString().trim()
            val confirmPass = binding.confirmPassEt.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty() && confirmPass.isNotEmpty()) {
                if (pass == confirmPass) {
                    // Utworz uzytkownika w Firebase
                    firebaseAuth.createUserWithEmailAndPassword(email, pass)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                // jesli sukces nawiguj do home
                                if (isAdded) {
                                    navController.navigate(R.id.action_signUpFragment_to_signInFragment)
                                }
                            } else {
                                when {
                                    task.exception is FirebaseAuthWeakPasswordException -> {
                                        Toast.makeText(
                                            requireContext(),
                                            "Hasło musi mieć co najmniej 6 znaków.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    task.exception is FirebaseAuthInvalidCredentialsException &&
                                            (task.exception as FirebaseAuthInvalidCredentialsException).errorCode == "ERROR_INVALID_EMAIL" -> {
                                        Toast.makeText(
                                            requireContext(),
                                            "Podany adres e-mail jest nieprawidłowy.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    task.exception is FirebaseAuthUserCollisionException -> {
                                        Toast.makeText(
                                            requireContext(),
                                            "Konto z tym adresem e-mail już istnieje.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    else -> {
                                        Toast.makeText(
                                            requireContext(),
                                            task.exception?.localizedMessage ?: "Wystąpił błąd.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                } else {
                    Toast.makeText(requireContext(), "Hasła nie pasują", Toast.LENGTH_SHORT)
                        .show()
                }
            } else {
                Toast.makeText(requireContext(), "Wypełnij puste pola!", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        binding.textViewSignIn.setOnClickListener {
            navController.navigate(R.id.action_signUpFragment_to_signInFragment)
        }

    }
}


