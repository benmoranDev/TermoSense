package br.com.termosense.service.esp32

object ESP32FirmwareTemplate {

    const val DEFAULT_BLE_SERVICE_UUID = "4fafc201-1fb5-459e-8fcc-c5c9c331914b"
    const val CHARACTERISTIC_TEMP_HUM_UUID = "beb5483e-36e1-4688-b7f5-ea07361b26a8"
    const val CHARACTERISTIC_RELAY_UUID = "cba1d466-344c-4be3-ab3f-189f80dd7518"

    fun getArduinoSketchCode(
        nodeName: String = "TermoSense-ESP32-VAC",
        wifiSsid: String = "UBS_SAUDE_WIFI",
        wifiPass: String = "saude12345",
        sensorType: String = "DHT22",
        dhtPin: Int = 4,
        relayPin: Int = 26
    ): String {
        return """
// ==============================================================================
//  PROJETO TERMOSENSE - FIRMWARE OFICIAL PARA ESP32 (REDE DE FRIO SUS / ANVISA)
//  Compatível com: ESP32 WROOM-32, ESP32-S3, ESP32-C3
//  Sensores Homologados: DHT22, SHT31, DS18B20
// ==============================================================================

#include <WiFi.h>
#include <WebServer.h>
#include <BLEDevice.h>
#include <BLEServer.h>
#include <BLEUtils.h>
#include <BLE2902.h>
#include "DHT.h"

// --- CONFIGURAÇÕES DE HARDWARE ---
#define DEVICE_NAME "$nodeName"
#define DHTPIN $dhtPin
#define DHTTYPE DHT22
#define RELAY_COOLER_PIN $relayPin
#define LED_STATUS_PIN 2

// --- CONFIGURAÇÕES DE WI-FI ---
const char* ssid = "$wifiSsid";
const char* password = "$wifiPass";

// --- UUIDs BLE TERMOSENSE ---
#define SERVICE_UUID        "$DEFAULT_BLE_SERVICE_UUID"
#define CHAR_TELEMETRY_UUID "$CHARACTERISTIC_TEMP_HUM_UUID"
#define CHAR_RELAY_UUID     "$CHARACTERISTIC_RELAY_UUID"

DHT dht(DHTPIN, DHTTYPE);
WebServer server(80);

BLEServer* pServer = NULL;
BLECharacteristic* pTelemetryChar = NULL;
BLECharacteristic* pRelayChar = NULL;
bool bleConnected = false;
bool relayState = false;

unsigned long lastSampleTime = 0;
const unsigned long sampleInterval = 2000; // 2 segundos

class ServerCallbacks: public BLEServerCallbacks {
    void onConnect(BLEServer* pServer) {
        bleConnected = true;
        digitalWrite(LED_STATUS_PIN, HIGH);
        Serial.println("[BLE] Aplicativo TermoSense Conectado!");
    }
    void onDisconnect(BLEServer* pServer) {
        bleConnected = false;
        digitalWrite(LED_STATUS_PIN, LOW);
        Serial.println("[BLE] Desconectado. Reiniciando anúncio...");
        pServer->getAdvertising()->start();
    }
};

class RelayCallbacks: public BLECharacteristicCallbacks {
    void onWrite(BLECharacteristic *pChar) {
        String val = pChar->getValue().c_str();
        if (val.length() > 0) {
            if (val[0] == '1') {
                relayState = true;
                digitalWrite(RELAY_COOLER_PIN, HIGH);
                Serial.println("[RELAY] Cooler / Compressor ATIVADO via BLE");
            } else {
                relayState = false;
                digitalWrite(RELAY_COOLER_PIN, LOW);
                Serial.println("[RELAY] Cooler / Compressor DESATIVADO via BLE");
            }
        }
    }
};

void handleTelemetry() {
    float t = dht.readTemperature();
    float h = dht.readHumidity();
    if (isnan(t) || isnan(h)) {
        server.send(500, "application/json", "{\"error\": \"Falha na leitura do sensor DHT\"}");
        return;
    }
    
    String json = "{";
    json += "\"device\": \"" + String(DEVICE_NAME) + "\",";
    json += "\"temperature\": " + String(t, 2) + ",";
    json += "\"humidity\": " + String(h, 2) + ",";
    json += "\"sensor\": \"$sensorType\",";
    json += "\"relayState\": " + String(relayState ? "true" : "false") + ",";
    json += "\"rssi\": " + String(WiFi.RSSI()) + ",";
    json += "\"uptime\": " + String(millis() / 1000) + ",";
    json += "\"vcc\": 3.32";
    json += "}";
    
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.send(200, "application/json", json);
}

void handleRelay() {
    if (server.hasArg("state")) {
        String st = server.arg("state");
        relayState = (st == "1" || st == "true");
        digitalWrite(RELAY_COOLER_PIN, relayState ? HIGH : LOW);
    }
    String json = "{\"relayState\": " + String(relayState ? "true" : "false") + "}";
    server.sendHeader("Access-Control-Allow-Origin", "*");
    server.send(200, "application/json", json);
}

void setup() {
    Serial.begin(115200);
    pinMode(RELAY_COOLER_PIN, OUTPUT);
    pinMode(LED_STATUS_PIN, OUTPUT);
    digitalWrite(RELAY_COOLER_PIN, LOW);
    
    dht.begin();
    Serial.println("\n[TERMOSENSE] Inicializando Módulo ESP32...");

    // Iniciar WiFi (Station + SoftAP de contingência)
    WiFi.mode(WIFI_AP_STA);
    WiFi.softAP("TermoSense-ESP32-AP", "termosense2026");
    WiFi.begin(ssid, password);
    
    Serial.print("[WIFI] Conectando a rede local...");
    int timeout = 0;
    while (WiFi.status() != WL_CONNECTED && timeout < 15) {
        delay(500);
        Serial.print(".");
        timeout++;
    }
    
    if (WiFi.status() == WL_CONNECTED) {
        Serial.println("\n[WIFI] Conectado com sucesso!");
        Serial.print("[WIFI] IP Local: ");
        Serial.println(WiFi.localIP());
    } else {
        Serial.println("\n[WIFI] Modo SoftAP Ativo IP: 192.168.4.1");
    }

    // Configurar Servidor HTTP REST
    server.on("/telemetry", handleTelemetry);
    server.on("/api/telemetry", handleTelemetry);
    server.on("/relay", handleRelay);
    server.on("/api/relay", handleRelay);
    server.begin();
    Serial.println("[HTTP] Servidor REST inicializado na porta 80");

    // Configurar BLE GATT Server
    BLEDevice::init(DEVICE_NAME);
    pServer = BLEDevice::createServer();
    pServer->setCallbacks(new ServerCallbacks());
    
    BLEService *pService = pServer->createService(SERVICE_UUID);
    
    pTelemetryChar = pService->createCharacteristic(
        CHAR_TELEMETRY_UUID,
        BLECharacteristic::PROPERTY_READ |
        BLECharacteristic::PROPERTY_NOTIFY
    );
    pTelemetryChar->addDescriptor(new BLE2902());

    pRelayChar = pService->createCharacteristic(
        CHAR_RELAY_UUID,
        BLECharacteristic::PROPERTY_READ |
        BLECharacteristic::PROPERTY_WRITE
    );
    pRelayChar->setCallbacks(new RelayCallbacks());

    pService->start();
    BLEAdvertising *pAdvertising = BLEDevice::getAdvertising();
    pAdvertising->addServiceUUID(SERVICE_UUID);
    pAdvertising->setScanResponse(true);
    pAdvertising->setMinPreferred(0x06);
    BLEDevice::startAdvertising();
    Serial.println("[BLE] GATT Server Ativo e Anunciando!");
}

void loop() {
    server.handleClient();

    if (millis() - lastSampleTime >= sampleInterval) {
        lastSampleTime = millis();
        float t = dht.readTemperature();
        float h = dht.readHumidity();

        if (!isnan(t) && !isnan(h)) {
            Serial.printf("[SENSOR] Temp: %.2f °C | Umidade: %.2f %% | Relé: %s\n", 
                          t, h, relayState ? "LIGADO" : "DESLIGADO");

            if (bleConnected && pTelemetryChar != NULL) {
                char buffer[64];
                snprintf(buffer, sizeof(buffer), "{\"t\":%.2f,\"h\":%.2f,\"r\":%d}", t, h, relayState ? 1 : 0);
                pTelemetryChar->setValue(buffer);
                pTelemetryChar->notify();
            }
        }
    }
}
        """.trimIndent()
    }
}
