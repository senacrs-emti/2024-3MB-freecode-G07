package com.example.gps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.button.MaterialButton
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class AuthActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth)

        // Configuração do ViewPager e TabLayout
        val viewPager = findViewById<ViewPager2>(R.id.authViewPager)
        val tabLayout = findViewById<TabLayout>(R.id.authTabLayout)

        viewPager.adapter = AuthPagerAdapter(this)

        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = if (position == 0) "login" else "Register"
        }.attach()
    }

    class AuthPagerAdapter(fragmentActivity: FragmentActivity) :
        FragmentStateAdapter(fragmentActivity) {
        override fun getItemCount(): Int = 2

        override fun createFragment(position: Int): Fragment {
            return if (position == 0) LoginFragment() else RegistroFragment()
        }
    }

    class LoginFragment : Fragment() {
        private lateinit var emailInput: TextInputEditText
        private lateinit var senhaInput: TextInputEditText
        private lateinit var loginButton: MaterialButton

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.activity_login, container, false)

            emailInput = view.findViewById(R.id.loginEmailEditText)
            senhaInput = view.findViewById(R.id.loginSenhaEditText)
            loginButton = view.findViewById(R.id.loginButton)

            loginButton.setOnClickListener {
                val email = emailInput.text.toString().trim()
                val senha = senhaInput.text.toString().trim()

                if (validarLogin(email, senha)) {
                    realizarLogin(email, senha)
                }
            }

            return view
        }

        private fun validarLogin(email: String, senha: String): Boolean {
            var isValid = true

            if (email.isEmpty()) {
                emailInput.error = "Email é obrigatório"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Email inválido"
                isValid = false
            }

            if (senha.isEmpty()) {
                senhaInput.error = "Senha é obrigatória"
                isValid = false
            } else if (senha.length < 6) {
                senhaInput.error = "Senha deve ter no mínimo 6 caracteres"
                isValid = false
            }

            return isValid
        }

        private fun realizarLogin(email: String, senha: String) {
            // Implementação do login
            // Exemplo com Firebase Authentication ou sua própria lógica
            Toast.makeText(requireContext(),
                "Login tentado com $email",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    class RegistroFragment : Fragment() {
        private lateinit var nomeInput: TextInputEditText
        private lateinit var emailInput: TextInputEditText
        private lateinit var senhaInput: TextInputEditText
        private lateinit var confirmacaoSenhaInput: TextInputEditText
        private lateinit var registroButton: MaterialButton

        override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                                  savedInstanceState: Bundle?): View? {
            val view = inflater.inflate(R.layout.activity_register, container, false)

            nomeInput = view.findViewById(R.id.registroNomeEditText)
            emailInput = view.findViewById(R.id.registroEmailEditText)
            senhaInput = view.findViewById(R.id.registroSenhaEditText)
            confirmacaoSenhaInput = view.findViewById(R.id.registroConfirmacaoSenhaEditText)
            registroButton = view.findViewById(R.id.registroButton)

            registroButton.setOnClickListener {
                val nome = nomeInput.text.toString().trim()
                val email = emailInput.text.toString().trim()
                val senha = senhaInput.text.toString().trim()
                val confirmacaoSenha = confirmacaoSenhaInput.text.toString().trim()

                if (validarRegistro(nome, email, senha, confirmacaoSenha)) {
                    realizarRegistro(nome, email, senha)
                }
            }

            return view
        }

        private fun validarRegistro(nome: String, email: String,
                                    senha: String, confirmacaoSenha: String): Boolean {
            var isValid = true

            if (nome.isEmpty()) {
                nomeInput.error = "Nome é obrigatório"
                isValid = false
            }

            if (email.isEmpty()) {
                emailInput.error = "Email é obrigatório"
                isValid = false
            } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                emailInput.error = "Email inválido"
                isValid = false
            }

            if (senha.isEmpty()) {
                senhaInput.error = "Senha é obrigatória"
                isValid = false
            } else if (senha.length < 6) {
                senhaInput.error = "Senha deve ter no mínimo 6 caracteres"
                isValid = false
            }

            if (confirmacaoSenha.isEmpty()) {
                confirmacaoSenhaInput.error = "Confirmação de senha é obrigatória"
                isValid = false
            } else if (senha != confirmacaoSenha) {
                confirmacaoSenhaInput.error = "Senhas não coincidem"
                isValid = false
            }

            return isValid
        }

        private fun realizarRegistro(nome: String, email: String, senha: String) {
            // Implementação do registro
            // Exemplo com Firebase Authentication ou sua própria lógica
            Toast.makeText(requireContext(),
                "Registro tentado para $nome, $email",
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}