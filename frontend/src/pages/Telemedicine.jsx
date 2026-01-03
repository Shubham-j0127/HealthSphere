import React, { useState, useEffect, useRef } from 'react'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'
import API_URL from '../config/api'
import './Telemedicine.css'

const Telemedicine = () => {
  const { user } = useAuth()
  const [sessionId, setSessionId] = useState(null)
  const [localStream, setLocalStream] = useState(null)
  const [remoteStream, setRemoteStream] = useState(null)
  const [isConnected, setIsConnected] = useState(false)
  const [partnerId, setPartnerId] = useState('')
  const [status, setStatus] = useState('idle')
  
  const localVideoRef = useRef(null)
  const remoteVideoRef = useRef(null)
  const peerConnectionRef = useRef(null)
  const pollingIntervalRef = useRef(null)

  useEffect(() => {
    return () => {
      if (localStream) {
        localStream.getTracks().forEach(track => track.stop())
      }
      if (pollingIntervalRef.current) {
        clearInterval(pollingIntervalRef.current)
      }
      if (sessionId) {
        endSession()
      }
    }
  }, [])

  const getUserMedia = async () => {
    try {
      const stream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
      })
      setLocalStream(stream)
      if (localVideoRef.current) {
        localVideoRef.current.srcObject = stream
      }
      return stream
    } catch (error) {
      console.error('Error accessing media devices:', error)
      alert('Error accessing camera/microphone. Please check permissions.')
      return null
    }
  }

  const createPeerConnection = () => {
    const configuration = {
      iceServers: [
        { urls: 'stun:stun.l.google.com:19302' }
      ]
    }
    
    const pc = new RTCPeerConnection(configuration)
    
    if (localStream) {
      localStream.getTracks().forEach(track => {
        pc.addTrack(track, localStream)
      })
    }

    pc.ontrack = (event) => {
      if (remoteVideoRef.current) {
        remoteVideoRef.current.srcObject = event.streams[0]
        setRemoteStream(event.streams[0])
      }
    }

    pc.onicecandidate = (event) => {
      if (event.candidate && sessionId) {
        axios.post(`${API_URL}/api/webrtc/ice`, {
          sessionId: sessionId,
          candidate: JSON.stringify(event.candidate),
          sdpMid: event.candidate.sdpMid,
          sdpMLineIndex: event.candidate.sdpMLineIndex
        }).catch(err => console.error('Error sending ICE candidate:', err))
      }
    }

    pc.onconnectionstatechange = () => {
      if (pc.connectionState === 'connected') {
        setIsConnected(true)
        setStatus('connected')
      } else if (pc.connectionState === 'disconnected' || pc.connectionState === 'failed') {
        setIsConnected(false)
        setStatus('disconnected')
      }
    }

    return pc
  }

  const startSession = async () => {
    try {
      const userInfoResponse = await axios.get(`${API_URL}/api/users/me`)
      const userInfo = userInfoResponse.data

      const doctorId = user.role === 'DOCTOR' ? userInfo.doctorId : parseInt(partnerId)
      const patientId = user.role === 'PATIENT' ? userInfo.patientId : parseInt(partnerId)

      if (!doctorId || !patientId) {
        alert('Please enter partner ID')
        return
      }

      const response = await axios.post(`${API_URL}/api/webrtc/session`, {
        doctorId: doctorId,
        patientId: patientId
      })

      const newSessionId = response.data.sessionId
      setSessionId(newSessionId)

      const stream = await getUserMedia()
      if (!stream) return

      const pc = createPeerConnection()
      peerConnectionRef.current = pc

      if (user.role === 'DOCTOR') {
        // Doctor creates offer
        const offer = await pc.createOffer()
        await pc.setLocalDescription(offer)

        await axios.post(`${API_URL}/api/webrtc/offer`, {
          sessionId: newSessionId,
          offer: JSON.stringify(offer),
          doctorId: doctorId,
          patientId: patientId
        })

        setStatus('waiting_for_answer')
        startPollingForAnswer(newSessionId, pc)
      } else {
        // Patient waits for offer
        setStatus('waiting_for_offer')
        startPollingForOffer(newSessionId, pc)
      }
    } catch (error) {
      console.error('Error starting session:', error)
      alert('Error starting session')
    }
  }

  const startPollingForOffer = async (sid, pc) => {
    pollingIntervalRef.current = setInterval(async () => {
      try {
        const response = await axios.get(`${API_URL}/api/webrtc/offer/${sid}`)
        if (response.data.offer) {
          clearInterval(pollingIntervalRef.current)
          
          const offer = JSON.parse(response.data.offer)
          await pc.setRemoteDescription(new RTCSessionDescription(offer))

          const answer = await pc.createAnswer()
          await pc.setLocalDescription(answer)

          await axios.post(`${API_URL}/api/webrtc/answer`, {
            sessionId: sid,
            answer: JSON.stringify(answer)
          })

          setStatus('connected')
        }
      } catch (error) {
        // Offer not ready yet, continue polling
      }
    }, 1000)
  }

  const startPollingForAnswer = async (sid, pc) => {
    pollingIntervalRef.current = setInterval(async () => {
      try {
        const response = await axios.get(`${API_URL}/api/webrtc/answer/${sid}`)
        if (response.data.answer) {
          clearInterval(pollingIntervalRef.current)
          
          const answer = JSON.parse(response.data.answer)
          await pc.setRemoteDescription(new RTCSessionDescription(answer))
          setStatus('connected')
        }
      } catch (error) {
        // Answer not ready yet, continue polling
      }
    }, 1000)
  }

  const endSession = async () => {
    try {
      if (sessionId) {
        await axios.post(`${API_URL}/api/webrtc/end/${sessionId}`)
      }
      if (localStream) {
        localStream.getTracks().forEach(track => track.stop())
      }
      if (remoteStream) {
        remoteStream.getTracks().forEach(track => track.stop())
      }
      if (peerConnectionRef.current) {
        peerConnectionRef.current.close()
      }
      if (pollingIntervalRef.current) {
        clearInterval(pollingIntervalRef.current)
      }
      
      setSessionId(null)
      setLocalStream(null)
      setRemoteStream(null)
      setIsConnected(false)
      setStatus('idle')
      
      if (localVideoRef.current) {
        localVideoRef.current.srcObject = null
      }
      if (remoteVideoRef.current) {
        remoteVideoRef.current.srcObject = null
      }
    } catch (error) {
      console.error('Error ending session:', error)
    }
  }

  return (
    <div className="telemedicine">
      <div className="page-header">
        <h1>Telemedicine Consultation</h1>
        {status === 'idle' && (
          <button className="btn btn-primary" onClick={startSession}>
            Start Consultation
          </button>
        )}
        {status !== 'idle' && (
          <button className="btn btn-danger" onClick={endSession}>
            End Consultation
          </button>
        )}
      </div>

      {status === 'idle' && (
        <div className="card">
          <p>Enter {user.role === 'DOCTOR' ? 'Patient' : 'Doctor'} ID to start consultation:</p>
          <input
            type="number"
            value={partnerId}
            onChange={(e) => setPartnerId(e.target.value)}
            placeholder={user.role === 'DOCTOR' ? 'Patient ID' : 'Doctor ID'}
            className="partner-input"
          />
        </div>
      )}

      <div className="video-container">
        <div className="video-section">
          <h3>Your Video</h3>
          <video
            ref={localVideoRef}
            autoPlay
            muted
            playsInline
            className="video-element"
          />
          {!localStream && status !== 'idle' && (
            <div className="video-placeholder">Starting camera...</div>
          )}
        </div>

        <div className="video-section">
          <h3>Remote Video</h3>
          <video
            ref={remoteVideoRef}
            autoPlay
            playsInline
            className="video-element"
          />
          {!remoteStream && status !== 'idle' && (
            <div className="video-placeholder">
              {status === 'waiting_for_offer' || status === 'waiting_for_answer' 
                ? 'Waiting for connection...' 
                : 'No remote video'}
            </div>
          )}
        </div>
      </div>

      <div className="status-indicator">
        <span className={`status-badge status-${status}`}>
          {status === 'idle' && 'Ready to start'}
          {status === 'waiting_for_offer' && 'Waiting for offer...'}
          {status === 'waiting_for_answer' && 'Waiting for answer...'}
          {status === 'connected' && 'Connected'}
          {status === 'disconnected' && 'Disconnected'}
        </span>
      </div>
    </div>
  )
}

export default Telemedicine

