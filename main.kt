import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.URL
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Base64
import javax.imageio.ImageIO
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.awt.image.BufferedImage
import java.net.HttpURLConnection

import com.google.gson.*
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import com.sun.net.httpserver.HttpServer


fun main(args: Array<String>) {
    val server: HttpServer = HttpServer.create(InetSocketAddress(8080), 0)
    server.createContext("/ejercicio1", ejercicio1())
    server.createContext("/ejercicio2", ejercicio2())
    server.createContext("/ejercicio3", ejercicio3())
    //server.createContext("/ejercicio4", ejercicio4())
    server.setExecutor(null)
    server.start()
}

class ejercicio1 : HttpHandler {
    override fun handle(t: HttpExchange) {
        if (t.getRequestMethod() == "POST") {
            val os: OutputStream = t.getResponseBody()
            var response: ByteArray = t.getRequestBody().readBytes()
            val test: String = String(response)
            val idk = test.split("\"")
            val origen = idk[3].replace(" ", "+")
            val destino = idk[7].replace(" ", "+")
            val request_url = "https://maps.googleapis.com/maps/api/directions/json?origin="+origen+"&destination="+destino+"&key=AIzaSyDcONfTyjtc5vBc4xoIMKOVtDPYA5VXkjk"
            println(origen)
            println(destino)
            val url = URL(request_url)
            val br = BufferedReader(InputStreamReader(url.openStream()))
            var maps: String = ""
            var temp: String = String()
            while(br.ready()){
                temp = br.readLine()
                println(temp)
                maps = maps + temp
            }
            var splitted = maps.split("\"steps\" : [", "],               \"traffic_speed_entry\"");
            splitted = splitted[1].split("\"start_location\" : ", "\"end_location\" : ", ",                     \"html_instructions\"", ",                     \"travel_mode\"")
            var steps: MutableList<String> = ArrayList()
            var c = 0
            var x = 0
            while(c < splitted.size){
                if(c % 2 == 1){
                    steps.add(splitted[c])
                    x++
                    println(splitted[c])
                }
                c++
            }
            c = 3
            var json: String = String()
            if(steps.size == 1){json="{\"ruta\":["+steps[1]+"]}"; }
            else if(steps.size == 2){json="{\"ruta\":["+steps[1]+", "+steps[0]+"]}";}
            else if(steps.size == 2){json="{\"ruta\":["+steps[1]+", " + steps[0]+", "+steps[2]+"]}";}
            else{json="{\"ruta\":["+steps[1]+", "+steps[0]+", "+steps[2]+ ", "
                while(c < steps.size ){
                    if(c % 2 == 0){
                        json = json + steps[c] + ", "
                    }
                    c++
                }
            }
            json = json.subSequence(0, json.length - 2).toString()
            json = json + "]}"
            println(json)
            response = json.toByteArray()
            t.getResponseHeaders().add("content-type", "json")
            t.sendResponseHeaders(200, response.size.toLong())
            os.write(response)
            os.close()
        }
    }
}

class ejercicio2 : HttpHandler {
    override fun handle(t: HttpExchange) {
        if (t.getRequestMethod() == "POST") {
            val os: OutputStream = t.getResponseBody()
            val response: ByteArray = t.getRequestBody().readBytes()
            val jo_input: JsonObject = JsonParser().parse(String(response)).getAsJsonObject()
            val origen: String = jo_input.get("origen").getAsString().replace(" ", "+")
            println("Origen: " + origen)
            var url: String = "https://maps.googleapis.com/maps/api/geocode/json?address="+origen+"&key=AIzaSyABw1fPmzlTyeKS54b70u2hejFO0leai8I"
            var response_to_get: String = sendGet(url)
            val jsonObj = JsonParser().parse(response_to_get).getAsJsonObject().get("results").getAsJsonArray().get(0).getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject()
            val lat = jsonObj.get("lat").getAsString()
            val lng = jsonObj.get("lng").getAsString()
            println(lat + " |-| " + lng)
            url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location="+lat+","+lng+"&radius=5000&type=restaurant&key=AIzaSyBnRLyT7gvSGa4HvsDXyH9h3w_4u0-xYHo"
            response_to_get = sendGet(url)
            val jsonArray = JsonParser().parse(response_to_get).getAsJsonObject().get("results").getAsJsonArray()
            var response_map = mutableMapOf<String, ArrayList<JsonObject>>()
            response_map.put("restaurantes", ArrayList<JsonObject>())
            var tmp: JsonObject
            for(dir in jsonArray) {
                tmp = JsonObject()
                tmp.add("nombre", dir.getAsJsonObject().get("name"));
                tmp.add("lat", dir.getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lat"))
                tmp.add("lon", dir.getAsJsonObject().get("geometry").getAsJsonObject().get("location").getAsJsonObject().get("lng"))
                response_map["restaurantes"]!!.add(tmp)
            }
            val gson = GsonBuilder().create()
            val json = gson.toJson(response_map)
            println(json)
            t.getResponseHeaders().add("content-type", "json")
            t.sendResponseHeaders(200, json.toByteArray().size.toLong() )
            os.write(json.toByteArray())
            os.close()
        }
    }
}

fun sendGet(url: String): String{
    val obj: URL = URL(url)
    val con: HttpURLConnection = obj.openConnection() as HttpURLConnection
    con.setRequestMethod("GET")
    val responseCode: Int = con.getResponseCode()
    if (responseCode != 200)
        return "nil"
    return String(con.getInputStream().readBytes())
}

class ejercicio3 : HttpHandler {
    override fun handle(t: HttpExchange) {
        if (t.getRequestMethod() == "POST") {
            val os: OutputStream = t.getResponseBody()
            var response: ByteArray = t.getRequestBody().readBytes()
            val test: String = String(response)
            var idk = test.split("\"")
            val nombre = idk[3]
            var img_data = idk[7]
            var gray_img = ""
            try{
                var img: ByteArray = Base64.getDecoder().decode(img_data)
                var bais: ByteArrayInputStream = ByteArrayInputStream(img)
                var editable_img: BufferedImage = ImageIO.read(bais)
                println(editable_img.getHeight())
                println(editable_img.getWidth())
                for(x in 0..editable_img.getWidth() - 1){
                    for(y in 0..editable_img.getHeight() - 1){
                        var rgb = editable_img.getRGB(x, y)
                        var r = (rgb shr 16) and 0xFF
                        var g = (rgb shr 8) and 0xFF
                        var b = (rgb and 0xFF)
                        var grayLevel = (0.21 * r + 0.72 * g + 0.07 * b).toInt()
                        var gray = grayLevel shl 16 or (grayLevel shl 8) or grayLevel
                        editable_img.setRGB(x, y, gray)
                    }
                }
                var baos: ByteArrayOutputStream = ByteArrayOutputStream()
                ImageIO.write(editable_img, "bmp", baos)
                var new_img: ByteArray = baos.toByteArray()
                gray_img = Base64.getEncoder().encodeToString(new_img)
            }catch(e: IllegalArgumentException){
                var err = "{\"error\": \"Vuelvelo a intentar\"}"
                response = err.toByteArray()
                t.getResponseHeaders().add("content-type", "json")
                t.sendResponseHeaders(500, response.size.toLong())
                os.write(response)
                os.close()
            }
            var json: String = String()
            var name = nombre.split(".")
            json = "{\"nombre\":\"" + name[0] + "(blanco y negro)." + name[1] + "\", \"data\": \"" + gray_img + "\"}"
            response = json.toByteArray()
            println(json)
            t.getResponseHeaders().add("content-type", "json")
            t.sendResponseHeaders(200, response.size.toLong())
            os.write(response)
            os.close()
        }
    }
}