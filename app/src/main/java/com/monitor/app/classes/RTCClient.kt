package com.monitor.app.classes

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import org.webrtc.*


class RTCClient(
    context: Application,
    observer: PeerConnection.Observer
) {

    companion object {
        private const val LOCAL_TRACK_ID = "local_track"
        private const val LOCAL_STREAM_ID = "local_track"
    }

    private val rootEglBase: EglBase = EglBase.create()

    private var localAudioTrack: AudioTrack? = null
    private var localVideoTrack: VideoTrack? = null
    val TAG = "RTCClient"

    var remoteSessionDescription: SessionDescription? = null

    val db = Firebase.firestore

    init {
        initPeerConnectionFactory(context)
    }

    private val iceServer = listOf(
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302")
            .createIceServer()
    )

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
                disableEncryption = true
                disableNetworkMonitor = true
            })
            .createPeerConnectionFactory()
    }

    private fun buildPeerConnection(observer: PeerConnection.Observer) =
        peerConnectionFactory.createPeerConnection(
            iceServer,
            observer
        )

    private fun getVideoCapturer(context: Context) =
        Camera2Enumerator(context).run {
            deviceNames.find {
                Log.d(TAG, "Camera: $it")
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
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
        }

        createOffer(object : SdpObserver by sdpObserver {
            override fun onCreateSuccess(desc: SessionDescription?) {
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        Log.d(TAG, "onSetSuccess")
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

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.d(TAG, "onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailure: $p0")
                    }
                }, desc)
                sdpObserver.onCreateSuccess(desc)
            }

            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure: $p0")
            }
        }, constraints)
    }

    private fun PeerConnection.answer(sdpObserver: SdpObserver, userID: String, sensorID: String) {
        val constraints = MediaConstraints().apply {
            mandatory.add(MediaConstraints.KeyValuePair("OfferToReceiveVideo", "true"))
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
                setLocalDescription(object : SdpObserver {
                    override fun onSetFailure(p0: String?) {
                        Log.e(TAG, "onSetFailure: $p0")
                    }

                    override fun onSetSuccess() {
                        Log.d(TAG, "onSetSuccess")
                    }

                    override fun onCreateSuccess(p0: SessionDescription?) {
                        Log.d(TAG, "onCreateSuccess: Description $p0")
                    }

                    override fun onCreateFailure(p0: String?) {
                        Log.e(TAG, "onCreateFailureLocal: $p0")
                    }
                }, desc)
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
        remoteSessionDescription = sessionDescription
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetFailure(p0: String?) {
                Log.e(TAG, "onSetFailure: $p0")
            }

            override fun onSetSuccess() {
                Log.d(TAG, "onSetSuccessRemoteSession")
            }

            override fun onCreateSuccess(p0: SessionDescription?) {
                Log.d(TAG, "onCreateSuccessRemoteSession: Description $p0")
            }

            override fun onCreateFailure(p0: String?) {
                Log.e(TAG, "onCreateFailure")
            }
        }, sessionDescription)

    }

    fun addIceCandidate(iceCandidate: IceCandidate?) {
        peerConnection?.addIceCandidate(iceCandidate)
    }

    fun endCall(meetingID: String) {
        db.collection("calls").document(meetingID).collection("candidates")
            .get().addOnSuccessListener {
                val iceCandidateArray: MutableList<IceCandidate> = mutableListOf()
                for (dataSnapshot in it) {
                    if (dataSnapshot.contains("type") && dataSnapshot["type"] == "sensor") {
                        val sensor = dataSnapshot
                        iceCandidateArray.add(
                            IceCandidate(
                                sensor["sdpMid"].toString(),
                                Math.toIntExact(sensor["sdpMLineIndex"] as Long),
                                sensor["sdp"].toString()
                            )
                        )
                    } else if (dataSnapshot.contains("type") && dataSnapshot["type"] == "answerCandidate") {
                        val answerCandidate = dataSnapshot
                        iceCandidateArray.add(
                            IceCandidate(
                                answerCandidate["sdpMid"].toString(),
                                Math.toIntExact(answerCandidate["sdpMLineIndex"] as Long),
                                answerCandidate["sdp"].toString()
                            )
                        )
                    }
                }
                peerConnection?.removeIceCandidates(iceCandidateArray.toTypedArray())
            }
        val endCall = hashMapOf(
            "type" to "END_CALL"
        )
        db.collection("calls").document(meetingID)
            .set(endCall)
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
}