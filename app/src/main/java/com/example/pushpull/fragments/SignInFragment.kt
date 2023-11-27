package com.example.pushpull.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.pushpull.R
import com.example.pushpull.databinding.FragmentSignInBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException


class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private lateinit var firebaseAuth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val navController = findNavController()



        firebaseAuth = FirebaseAuth.getInstance()


        binding.buttonSingIn.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()
            val pass = binding.passET.text.toString().trim()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                // zaloguj z Firebase
                firebaseAuth.signInWithEmailAndPassword(email, pass).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Do Home gdy zalogowano sie
                        navController.navigate(R.id.action_signInFragment_to_homeFragment)

                        // Ustawienie podświetlenia na ikonkę Home w BottomNavigationView
                        binding.root.post {
                            val bottomNavigationView =
                                activity?.findViewById<BottomNavigationView>(R.id.bottomNav)
                            bottomNavigationView?.selectedItemId = R.id.homeFragment
                        }
                    } else {
                        // obsługa błędów
                        val exception = task.exception
                        if (exception is FirebaseAuthInvalidCredentialsException) {
                            if (exception.errorCode == "ERROR_INVALID_EMAIL") {
                                Toast.makeText(
                                    requireContext(),
                                    "Podany adres e-mail jest nieprawidłowy.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Błąd podczas logowania: Nieprawidłowe dane logowania.",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else if (exception is FirebaseAuthInvalidUserException) {
                            Toast.makeText(
                                requireContext(),
                                "Konto nie istnieje lub zostało usunięte.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else if (exception is FirebaseTooManyRequestsException) {
                            Toast.makeText(
                                requireContext(),
                                "Przekroczono limit prób logowania. Spróbuj ponownie później.",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Błędny email lub hasło",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Wypełnij puste pola!", Toast.LENGTH_SHORT).show()
            }
        }

        binding.textViewForgotPassword.setOnClickListener {
            val email = binding.emailEt.text.toString().trim()

            if (email.isNotEmpty()) {
                firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Toast.makeText(
                            requireContext(),
                            "Mail do resetowania hasła został wysłany.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Wystąpił błąd podczas wysyłania maila do resetowania hasła.",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Proszę wpisać adres e-mail.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }



        binding.textViewSignUp.setOnClickListener {
            navController.navigate(R.id.action_signInFragment_to_signUpFragment)
        }

    }


}