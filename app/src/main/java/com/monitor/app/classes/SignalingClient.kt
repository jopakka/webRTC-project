package com.monitor.app.classes

import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.monitor.app.Constants
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import org.webrtc.IceCandidate
import org.webrtc.SessionDescription

@ExperimentalCoroutinesApi
class SignalingClient(
    private val userID: String,
    private val sensorID: String,
    private val listener: SignalingClientListener
) : CoroutineScope {

    companion object {
        private const val TAG = "SignallingClient"
    }

    private val job = Job()
    private val db = Firebase.firestore
    private var SDPtype: String? = null
    override val coroutineContext = Dispatchers.IO + job

    @OptIn(ObsoleteCoroutinesApi::class)
    private val sendChannel = ConflatedBroadcastChannel<String>()

    init {
        connect()
    }

    @OptIn(ObsoleteCoroutinesApi::class)
    private fun connect() = launch {
        db.enableNetwork().addOnSuccessListener {
            listener.onConnectionEstablished()
        }
        val sendData = sendChannel.trySend("").isSuccess
        sendData.let {
            Log.v(this@SignalingClient.javaClass.simpleName, "Sending: $it")
        }
        try {
            db.collection(userID)
                .document(sensorID)
                .addSnapshotListener { snapshot, e ->

                    if (e != null) {
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {
                        val data = snapshot.data
                        Log.d(TAG, "type=${data?.get("type")}")
                        if (data?.containsKey("type")!! &&
                            data.getValue("type").toString() == "OFFER"
                        ) {
                            listener.onOfferReceived(
                                SessionDescription(
                                    SessionDescription.Type.OFFER, data["sdp"].toString()
                                )
                            )
                            SDPtype = "Offer"
                        } else if (data.containsKey("type") &&
                            data.getValue("type").toString() == "ANSWER"
                        ) {
                            listener.onAnswerReceived(
                                SessionDescription(
                                    SessionDescription.Type.ANSWER, data["sdp"].toString()
                                )
                            )
                            SDPtype = "Answer"
                        } else if (!Constants.isIntiatedNow && data.containsKey("type") &&
                            data.getValue("type").toString() == "END_CALL"
                        ) {
                            listener.onCallEnded()
                            SDPtype = "End Call"

                        }
                        Log.d(TAG, "Current data: ${snapshot.data}")
                    } else {
                        Log.d(TAG, "Current data: null")
                    }
                }
            db.collection(userID).document(sensorID)
                .collection("candidates").addSnapshotListener { querysnapshot, e ->
                    if (e != null) {
                        Log.w(TAG, "listen:error", e)
                        return@addSnapshotListener
                    }

                    if (querysnapshot != null && !querysnapshot.isEmpty) {
                        for (dataSnapShot in querysnapshot) {
                            val data = dataSnapShot.data
                            Log.d(TAG, "candidates type=${data["type"]}")
                            if (SDPtype == "Offer" && data.containsKey("type") && data["type"] == "offerCandidate") {
                                listener.onIceCandidateReceived(
                                    IceCandidate(
                                        data["sdpMid"].toString(),
                                        Math.toIntExact(data["sdpMLineIndex"] as Long),
                                        data["sdpCandidate"].toString()
                                    )
                                )
                            } else if (SDPtype == "Answer" && data.containsKey("type") && data["type"] == "answerCandidate") {
                                listener.onIceCandidateReceived(
                                    IceCandidate(
                                        data["sdpMid"].toString(),
                                        Math.toIntExact(data["sdpMLineIndex"] as Long),
                                        data["sdpCandidate"].toString()
                                    )
                                )
                            }
                            Log.d(TAG, "candidateQuery: $dataSnapShot")
                        }
                    }
                }

        } catch (exception: Exception) {
            Log.e(TAG, "connectException: $exception")

        }
    }

    fun sendIceCandidate(candidate: IceCandidate?, isSensor: Boolean) = runBlocking {
        val type = when {
            isSensor -> "offerCandidate"
            else -> "answerCandidate"
        }
        val candidateConstant = hashMapOf(
            "serverUrl" to candidate?.serverUrl,
            "sdpMid" to candidate?.sdpMid,
            "sdpMLineIndex" to candidate?.sdpMLineIndex,
            "sdpCandidate" to candidate?.sdp,
            "type" to type
        )
        db.collection(userID)
            .document(sensorID).collection("candidates").document(type)
            .set(candidateConstant as Map<String, *>)
            .addOnSuccessListener {
                Log.d(TAG, "sendIceCandidate: Success")
            }
            .addOnFailureListener {
                Log.e(TAG, "sendIceCandidate: Error $it")
            }
    }

    fun destroy() {
        job.complete()
    }
}
