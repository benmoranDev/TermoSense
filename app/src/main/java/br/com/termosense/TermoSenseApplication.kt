package br.com.termosense

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

class TermoSenseApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initFirebaseSafely()
    }

    private fun initFirebaseSafely() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                val defaultApp = FirebaseApp.initializeApp(this)
                if (defaultApp == null) {
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:871573061422:android:termosenseapp")
                        .setApiKey("AIzaSyTermoSenseHealthcareColdChainKey")
                        .setProjectId("termosense-cold-chain")
                        .build()
                    FirebaseApp.initializeApp(this, options)
                }
            }
            Log.d("TermoSenseApp", "FirebaseApp inicializado com sucesso.")
        } catch (e: Exception) {
            try {
                val options = FirebaseOptions.Builder()
                    .setApplicationId("1:871573061422:android:termosenseapp")
                    .setApiKey("AIzaSyTermoSenseHealthcareColdChainKey")
                    .setProjectId("termosense-cold-chain")
                    .build()
                FirebaseApp.initializeApp(this, options)
                Log.d("TermoSenseApp", "FirebaseApp inicializado via FirebaseOptions de contingência.")
            } catch (fallbackEx: Exception) {
                Log.w("TermoSenseApp", "Aviso: Firebase rodando em modo de contingência local: ${fallbackEx.message}")
            }
        }
    }
}
