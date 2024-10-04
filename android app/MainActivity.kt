import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.telephony.SmsManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.OnSuccessListener
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val emergencyButton: Button = findViewById(R.id.emergencyButton)
        emergencyButton.setOnClickListener {
            sendEmergencyAlert()
        }
    }

    private fun sendEmergencyAlert() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 100)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener(this, OnSuccessListener<Location> { location ->
            if (location != null) {
                val coordinates = "${location.latitude},${location.longitude}"
                val phoneNumber = "your_phone_number_here"  // Replace with actual user or contact phone number
                val message = "Emergency Alert! Please assist. Location: $coordinates"

                // Send location to the Flask backend
                sendToBackend(phoneNumber, coordinates)
            } else {
                Toast.makeText(this, "Unable to get location", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Function to send data to Flask backend
    private fun sendToBackend(phoneNumber: String, coordinates: String) {
        val url = "https://<ngrok_url>/ussd"  // Replace with your ngrok URL

        val json = JSONObject()
        json.put("phone_number", phoneNumber)
        json.put("location", coordinates)

        val body: RequestBody = RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Failed to send data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                runOnUiThread {
                    if (response.isSuccessful) {
                        Toast.makeText(this@MainActivity, "Alert sent to backend!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(this@MainActivity, "Backend error: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
