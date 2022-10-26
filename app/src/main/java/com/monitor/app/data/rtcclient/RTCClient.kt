package com.monitor.app.data.rtcclient

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.webrtc.*
import java.nio.ByteBuffer

class RTCClient(
    context: Application,
    private val observer: PeerConnection.Observer
) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
        private const val TAG = "RTCClient"
    }

    private val rootEglBase: EglBase = EglBase.create()
    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    var remoteSessionDescription: SessionDescription? = null
    val db = Firebase.firestore

    init {
        initPeerConnectionFactory(context)
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )

    private lateinit var dataChannel: DataChannel
    private val peerConnectionFactory by lazy { buildPeerConnectionFactory() }
    private val videoCapturer by lazy { getVideoCapturer(context) }
    private val audioSource by lazy { peerConnectionFactory.createAudioSource(MediaConstraints()) }
    private val localVideoSource by lazy { peerConnectionFactory.createVideoSource(false) }
    private val peerConnection by lazy { buildPeerConnection(observer) }

    private fun initPeerConnectionFactory(context: Application) {
        val options = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .setFieldTrials("WebRTC-H264HighProfile/Enabled/")
            .createInitializationOptions()
        PeerConnectionFactory.initialize(options)
    }

    private fun buildPeerConnectionFactory(): PeerConnectionFactory {
        return PeerConnectionFactory
            .builder()
            .setVideoDecoderFactory(DefaultVideoDecoderFactory(rootEglBase.eglBaseContext))
            .setVideoEncoderFactory(
                DefaultVideoEncoderFactory(
                    rootEglBase.eglBaseContext,
                    true,
                    true
                )
            )
            .setOptions(PeerConnectionFactory.Options().apply {
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer): PeerConnection? {
        return peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
        )?.also {
            val dcInit = DataChannel.Init().apply {
                id = 1
            }
            dataChannel = it.createDataChannel("testChannel", dcInit)
        }
    }

    private fun getVideoCapturer(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
//                isFrontFacing(it)
                it == deviceNames.last()
            }?.let {
                createCapturer(it, null)
            } ?: throw IllegalStateException()
        }

    fun initSurfaceView(view: SurfaceViewRenderer, mirror: Boolean = false) = view.run {
        setMirror(mirror)
        setEnableHardwareScaler(true)
        init(rootEglBase.eglBaseContext, null)
    }

    fun startLocalVideoCapture(localVideoOutput: SurfaceViewRenderer) {
        val surfaceTextureHelper =
            SurfaceTextureHelper.create(Thread.currentThread().name, rootEglBase.eglBaseContext)
        (videoCapturer as VideoCapturer).initialize(
            surfaceTextureHelper,
            localVideoOutput.context,
            localVideoSource.capturerObserver
        )
        videoCapturer.startCapture(640, 480, 60)
        localAudioTrack =
            peerConnectionFactory.createAudioTrack(LOCAL_TRACK_ID + "_audio", audioSource)
        localVideoTrack = peerConnectionFactory.createVideoTrack(LOCAL_TRACK_ID, localVideoSource)
        localVideoTrack?.addSink(localVideoOutput)
        val localStream = peerConnectionFactory.createLocalMediaStream(LOCAL_STREAM_ID)
        localStream.addTrack(localVideoTrack)
        localStream.addTrack(localAudioTrack)
        peerConnection?.addStream(localStream)
    }

    private fun PeerConnection.call(sdpObserver: SdpObserver, userID: String, sensorID: String) {
        Log.d(TAG, "Calling")
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true"))
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : AppSdpObserver() {
                    override fun onSetSuccess() {
                        super.onSetSuccess()
                        val offer = hashMapOf(
                            "sdp" to desc?.description,
                            "type" to desc?.type
                        )
                        db.collection(userID).document(sensorID)
                            .update(offer as Map<String, *>)
                            .addOnSuccessListener {
                                Log.d(TAG, "DocumentSnapshot added")
                            }
                            .addOnFailureListener { e ->
                                Log.e(TAG, "Error adding document", e)
                            }
                        Log.d(TAG, "onSetSuccess")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure-createOffer: $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure: $p0")
            }
        }, constraints)
    }

    private fun PeerConnection.answer(sdpObserver: SdpObserver, userID: String, sensorID: String) {
        Log.d(TAG, "Answering")
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
            mandatory.add(MediaConstraints.KeyValuePair("IceRestart", "true"))
        }
        createAnswer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                val answer = hashMapOf(
                    "sdp" to desc?.description,
                    "type" to desc?.type
                )
                db.collection(userID).document(sensorID)
                    .update(answer as Map<String, *>)
                    .addOnSuccessListener {
                        Log.d(TAG, "DocumentSnapshot added")
                    }
                    .addOnFailureListener { e ->
                        Log.e(TAG, "Error adding document", e)
                    }
                setLocalDescription(object : AppSdpObserver() {}, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailureRemote: $p0")
            }
        }, constraints)
    }

    fun call(sdpObserver: SdpObserver, userID: String, sensorID: String) =
        peerConnection?.call(sdpObserver, userID, sensorID)

    fun answer(sdpObserver: SdpObserver, userID: String, sensorID: String) =
        peerConnection?.answer(sdpObserver, userID, sensorID)

    fun onRemoteSessionReceived(sessionDescription: SessionDescription) {
        Log.d(TAG, "onRemoteSessionReceived")
        Log.d(TAG, "sessionDescription=${sessionDescription.type}")
        remoteSessionDescription = sessionDescription
        peerConnection?.setRemoteDescription(object : AppSdpObserver() {}, sessionDescription)
    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun endCall(userID: String, sensorID: String, reCall: Boolean) {
        if (reCall) {
            val sdpObserver = object : AppSdpObserver() {}
            call(sdpObserver, userID, sensorID)
            return
        }
        videoCapturer.stopCapture()
        db.collection(userID).document(sensorID).collection("candidates")
            .get().addOnSuccessListener {
                val iceCandidateArray: MutableList<IceCandidate> = mutableListOf()
                for (dataSnapshot in it) {
                    if (dataSnapshot.contains("type") && dataSnapshot["type"] == "offerCandidate") {
                        iceCandidateArray.add(
                            IceCandidate(
                                dataSnapshot["sdpMid"].toString(),
                                Math.toIntExact(dataSnapshot["sdpMLineIndex"] as Long),
                                dataSnapshot["sdp"].toString()
                            )
                        )
                    } else if (dataSnapshot.contains("type") && dataSnapshot["type"] == "answerCandidate") {
                        iceCandidateArray.add(
                            IceCandidate(
                                dataSnapshot["sdpMid"].toString(),
                                Math.toIntExact(dataSnapshot["sdpMLineIndex"] as Long),
                                dataSnapshot["sdp"].toString()
                            )
                        )
                    }
                }
                peerConnection?.removeIceCandidates(iceCandidateArray.toTypedArray())
            }
        val endCall = hashMapOf(
            "type" to "END_CALL"
        )
        db.collection(userID).document(sensorID)
            .update(endCall as Map<String, *>)
            .addOnSuccessListener {
                Log.d(TAG, "DocumentSnapshot added")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error adding document", e)
            }

        peerConnection?.close()
    }

    fun enableVideo(videoEnabled: Boolean) {
        if (localVideoTrack != null)
            localVideoTrack?.setEnabled(videoEnabled)
    }

    fun enableAudio(audioEnabled: Boolean) {
        if (localAudioTrack != null)
            localAudioTrack?.setEnabled(audioEnabled)
    }

    fun switchCamera() {
        videoCapturer.switchCamera(null)
    }

    fun sendData(data: String) {
        dataChannel.send(DataChannel.Buffer(ByteBuffer.wrap(data.toByteArray()), false))
    }
}