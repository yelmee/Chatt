package com.example.chatt.Server

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import org.json.JSONObject


class SocketServer private constructor() {


    companion object {

        var SERVER_URL: String = "http://192.168.73.135:3000"
        private var socket: Socket? = null

        @Synchronized
        fun getSocket(): Socket {

            if (socket == null)
                socket = IO.socket(SERVER_URL)

            if (!socket?.connected()!!)
                socket!!.connect()

            return socket as Socket

        }

        fun closeSocket() {
            if (socket != null) {
                socket!!.close()
                socket!!.disconnect()
            }
        }
//
//        fun printIn(){
//
//            Log.e( "mytag", socket.hashCode().toString())
//        }


    }

}