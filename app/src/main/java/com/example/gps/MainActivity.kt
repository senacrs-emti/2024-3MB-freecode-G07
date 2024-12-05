package com.example.gps

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.FirebaseApp
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FirebaseFirestore
import android.location.Location
import android.widget.ImageButton
import com.example.gps.databinding.ActivityMainBinding
import android.app.AlertDialog
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var btnSalvarLocalizacao: ImageButton
    private lateinit var btnSalvarLocalizacaoAleatoria: ImageButton
    private lateinit var inputsTextView: TextView

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var btnSalvarLocalizacao: ImageButton = findViewById(R.id.btnSalvarLocalizacao)


        // Inicializar localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // configurar fragmento do mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // salvar localização atual
        btnSalvarLocalizacao = findViewById(R.id.btnSalvarLocalizacao)
        btnSalvarLocalizacao.setOnClickListener {
            val dialog = AddDialogFragment()
            dialog.show(supportFragmentManager, "AddDialogFragment")
            obterEsalvarLocalizacaoAtual()
        }

        //localização aleatória
        btnSalvarLocalizacaoAleatoria = findViewById(R.id.btnSalvarLocalizacaoAleatoria)
    }




    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap


        if (checkLocationPermission()) {
            configurarMapaComLocalizacao()
        }

        mMap.setOnMapLongClickListener { latLng ->
            salvarLocalizacaoEspecifica(latLng)
        }

        // Configurar botão de localização aleatória
        btnSalvarLocalizacaoAleatoria.setOnClickListener {
            val intent = Intent(this, Requests::class.java)
            startActivity(intent)
        }

    }


    private fun checkLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Solicitar permissões se não estiverem concedidas
            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun configurarMapaComLocalizacao() {
        mMap.isMyLocationEnabled = true

        // Obter localização inicial
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)

                    // Mover câmera para a localização
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                }
            }
    }

    private fun obterEsalvarLocalizacaoAtual() {
        if (!checkLocationPermission()) return

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val currentLatLng = LatLng(it.latitude, it.longitude)

                    // Adicionar marcador na localização atual
                    mMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Localização Atual Salva")
                    )

                    // Salvar localização no Firebase
                    salvarLocalizacaoNoFirebase(currentLatLng)
                } ?: run {
                    Toast.makeText(this, "Não foi possível obter localização", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao obter localização", Toast.LENGTH_SHORT).show()
            }
    }

    private fun salvarLocalizacaoEspecifica(latLng: LatLng) {
        // Adicionar marcador na localização selecionada
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Localização Selecionada Salva")
        )

        // Salvar localização no Firebase
        salvarLocalizacaoNoFirebase(latLng)
    }



    private fun salvarLocalizacaoNoFirebase(latLng: LatLng) {
        // Referência do banco de dados Firebase
        val database = FirebaseDatabase.getInstance()
        val localizacoesRef = database.getReference("localizacoes")

        // Criar objeto de localização para salvar
        val localizacaoMap = hashMapOf(
            "latitude" to latLng.latitude,
            "longitude" to latLng.longitude,
            "timestamp" to System.currentTimeMillis(),
            "tipo" to "manual"
        )

        // Salvar localização
        localizacoesRef.push().setValue(localizacaoMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Localização salva com sucesso!", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Erro ao salvar localização", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Recarregar mapa se permissão foi concedida
                val mapFragment = supportFragmentManager
                    .findFragmentById(R.id.map_fragment) as SupportMapFragment
                mapFragment.getMapAsync(this)
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }
    }


}