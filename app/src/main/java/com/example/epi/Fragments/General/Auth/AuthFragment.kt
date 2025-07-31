package com.example.epi.Fragments.General.Auth

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.epi.App
import com.example.epi.DataBase.User.User
import com.example.epi.R
import com.example.epi.ViewModel.GeneralViewModel
import com.example.epi.databinding.FragmentAuthBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mindrot.jbcrypt.BCrypt


class AuthFragment : Fragment() {
    private var _binding: FragmentAuthBinding? = null
    private val binding get() = _binding!!

    private val maxNumberLength = 4
    private val maxPasswordLength = 12

    private val viewModel: GeneralViewModel by viewModels()
    private val userRepository by lazy { (requireContext().applicationContext as App).userRepository }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentAuthBinding.inflate(inflater, container, false)

        buttons()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Табельный номер
        binding.textInputEditTextNumber.filters = arrayOf(android.text.InputFilter.LengthFilter(maxNumberLength))

        // Пароль
        binding.textInputEditTextPassword.filters = arrayOf(android.text.InputFilter.LengthFilter(maxPasswordLength))

    }

    private fun buttons() {
        binding.btnLogin.setOnClickListener {
            if (validateInputs()){
                Log.d("Tagg", "Validation")
//                Toast.makeText(requireContext(), "Здесь скоро будет авторизация", Toast.LENGTH_SHORT).show()
                loginUser()

            } else {
                Log.d("Tagg", "Validation failed")
            }

        }

        binding.tvRegister.setOnClickListener {
            findNavController().navigate(R.id.registrationFragment)
        }

        binding.btnBackToMenu.setOnClickListener {
            findNavController().navigate(R.id.StartFragment)
        }
    }

    private fun validateInputs(): Boolean {

        val number = binding.textInputEditTextNumber.text?.toString()?.trim()
        val password = binding.textInputEditTextPassword.text?.toString()?.trim()

        val errors = viewModel.validateAuthInputs(
            number,
            password
        )
        // Сброс старых ошибок
        binding.textInputLayoutNumber.error = null
        binding.textInputLayoutPassword.error = null

        // Установка новых ошибок
        binding.textInputLayoutNumber.isErrorEnabled = !errors["number"].isNullOrBlank()
        binding.textInputLayoutNumber.error = errors["number"]

        binding.textInputLayoutPassword.isErrorEnabled = !errors["password"].isNullOrBlank()
        binding.textInputLayoutPassword.error = errors["password"]

        return errors.isEmpty()
    }

    private fun loginUser() {
        val employeeNumber = binding.textInputEditTextNumber.text.toString().trim()
        val password = binding.textInputEditTextPassword.text.toString().trim()

        Log.d("Tagg", "Attempting login with employeeNumber: $employeeNumber, password: $password")

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val user = userRepository.getUserByCredentials(employeeNumber)
                Log.d("Tagg", "User found: ${user?.toString() ?: "null"}")

                requireActivity().runOnUiThread {
                    if (user != null) {
                        Log.d("Tagg", "Checking password, hashed: ${user.password}")
                        if (BCrypt.checkpw(password, user.password)) {
                            Toast.makeText(requireContext(), "Авторизация успешна", Toast.LENGTH_SHORT).show()
                            saveUserSession(user)
                            binding.textInputEditTextNumber.text?.clear()
                            binding.textInputEditTextPassword.text?.clear()
                            findNavController().navigate(R.id.StartFragment)
                        } else {
                            binding.textInputLayoutPassword.isErrorEnabled = true
                            binding.textInputLayoutPassword.error = "Неверный пароль"
                            Log.d("Tagg", "Password mismatch")
                        }
                    } else {
                        binding.textInputLayoutNumber.isErrorEnabled = true
                        binding.textInputLayoutNumber.error = "Пользователь с таким номером не найден"
                        Log.d("Tagg", "user not found for employeeNumber: $employeeNumber")
                    }
                }
            } catch (e: Exception) {
                requireActivity().runOnUiThread {
                    Toast.makeText(requireContext(), "Ошибка авторизации: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.d("Tagg", "Login error:", e)
                }
            }
        }
    }

    private fun saveUserSession(user: User) {
        val sharedPreferences = requireContext().getSharedPreferences("User_session", android.content.Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putInt("userId", user.id)
            putString("employeeNumber", user.employeeNumber)
            putString("secondName", user.secondName)
            putString("firstName", user.firstName)
            putString("thirdName", user.thirdName)
            putString("branch", user.branch)
            putString("pu", user.pu)
            apply()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}