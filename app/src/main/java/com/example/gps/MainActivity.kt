package com.example.gps

import android.Manifest
import android.annotation.SuppressLint
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import android.location.Location
import android.widget.Button
import android.widget.ImageButton


class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var mMap: GoogleMap
    private lateinit var btnSalvarLocalizacao: ImageButton
    private lateinit var btnSalvarLocalizacaoAleatoria: ImageButton

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar cliente de localização
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configurar fragmento do mapa
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Configurar botão de salvar localização atual
        btnSalvarLocalizacao = findViewById(R.id.btnSalvarLocalizacao)
        btnSalvarLocalizacao.setOnClickListener {
            obterEsalvarLocalizacaoAtual()
        }

        // Configurar botão de salvar localização aleatória
        btnSalvarLocalizacaoAleatoria = findViewById(R.id.btnSalvarLocalizacaoAleatoria)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // Verificar permissões de localização
        if (checkLocationPermission()) {
            configurarMapaComLocalizacao()
        }

        // Configurar long press para salvar localização
        mMap.setOnMapLongClickListener { latLng ->
            salvarLocalizacaoEspecifica(latLng)
        }

        // Configurar botão de localização aleatória
        btnSalvarLocalizacaoAleatoria.setOnClickListener {
            salvarLocalizacaoAleatoria()
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

    private fun salvarLocalizacaoAleatoria() {
        // Obter limite do mapa visível
        val projection = mMap.projection
        val visibleRegion = projection.visibleRegion
        val bounds = visibleRegion.latLngBounds

        // Gerar localização aleatória dentro dos limites do mapa
        val randomLat = bounds.southwest.latitude + Math.random() * (bounds.northeast.latitude - bounds.southwest.latitude)
        val randomLng = bounds.southwest.longitude + Math.random() * (bounds.northeast.longitude - bounds.southwest.longitude)

        val randomLatLng = LatLng(randomLat, randomLng)

        // Adicionar marcador na localização aleatória
        mMap.addMarker(
            MarkerOptions()
                .position(randomLatLng)
                .title("Localização Aleatória Salva")
        )

        // Salvar localização no Firebase
        salvarLocalizacaoNoFirebase(randomLatLng)
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