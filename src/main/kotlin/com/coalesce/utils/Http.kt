package com.coalesce.utils

import com.coalesce.Constants
import java.io.BufferedReader
import java.io.DataOutputStream
import java.io.InputStreamReader
import java.io.UnsupportedEncodingException
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.*

object Http {
    @Throws(UnsupportedEncodingException::class)
    fun urlEncode(url: String): String {
        return URLEncoder.encode(url, "UTF-8")
    }

    @Throws(Exception::class)
    fun sendGet(url: String): String {

        val obj = URL(url)
        val con = obj.openConnection() as HttpURLConnection

        // optional default is GET
        con.requestMethod = "GET"

        // add request header
        con.setRequestProperty("User-Agent", Constants.USER_AGENT)

        val input = BufferedReader(InputStreamReader(con.inputStream))
        var inputLine = input.readLine()
        val response = StringBuffer()

        while (inputLine != null) {
            response.append(inputLine)
            if (input.readLine() == null)
                break
            inputLine = input.readLine()
        }
        input.close()

        // return result
        return response.toString()

    }

    @Throws(Exception::class)
    fun sendPost(url: String, arguments: HashMap<String, String>): String {

        val obj = URL(url)
        val con = obj.openConnection() as HttpURLConnection

        // add reuqest header
        con.requestMethod = "POST"
        con.setRequestProperty("User-Agent", Constants.USER_AGENT)
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5")

        var urlParameters = ""
        val sb = StringBuilder()
        for (str in arguments.keys) {
            if (sb.length != 0)
                sb.append("&")
            sb.append(str + "=" + arguments[str])
        }
        urlParameters = sb.toString()

        // Send post request
        con.doOutput = true
        val wr = DataOutputStream(con.outputStream)
        wr.writeBytes(urlParameters)
        wr.flush()
        wr.close()

        val input = BufferedReader(InputStreamReader(con.inputStream))
        var inputLine = input.readLine()
        val response = StringBuffer()

        while (inputLine != null) {
            response.append(inputLine)
            if (input.readLine() == null)
                break
            inputLine = input.readLine()
        }
        input.close()

        // return result
        return response.toString()

    }
}