import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import { useNavigate } from 'react-router-dom'
import axios from 'axios'
import API_URL from '../config/api'
import MedicalRecords from './MedicalRecords'
import Appointments from './Appointments'
import Prescriptions from './Prescriptions'
import Telemedicine from './Telemedicine'
import Notifications from './Notifications'
import './Dashboard.css'

const DoctorDashboard = () => {
  const { user, logout } = useAuth()
  const navigate = useNavigate()
  const [activeView, setActiveView] = useState('dashboard')
  const [unreadCount, setUnreadCount] = useState(0)

  useEffect(() => {
    loadUnreadCount()
    const interval = setInterval(loadUnreadCount, 30000) // Check every 30 seconds
    return () => clearInterval(interval)
  }, [])

  const loadUnreadCount = async () => {
    try {
      const response = await axios.get(`${API_URL}/api/notifications/unread/count`)
      setUnreadCount(response.data.count)
    } catch (error) {
      console.error('Error loading unread count:', error)
    }
  }

  const handleLogout = () => {
    logout()
    navigate('/login')
  }

  if (activeView === 'medical-records') {
    return (
      <div className="dashboard">
        <header className="dashboard-header">
          <button onClick={() => setActiveView('dashboard')} className="btn btn-secondary">
            ← Back to Dashboard
          </button>
          <div className="header-actions">
            <span>Welcome, {user?.email}</span>
            <button onClick={handleLogout} className="btn btn-secondary">
              Logout
            </button>
          </div>
        </header>
        <MedicalRecords />
      </div>
    )
  }

  if (activeView === 'appointments') {
    return (
      <div className="dashboard">
        <header className="dashboard-header">
          <button onClick={() => setActiveView('dashboard')} className="btn btn-secondary">
            ← Back to Dashboard
          </button>
          <div className="header-actions">
            <span>Welcome, {user?.email}</span>
            <button onClick={handleLogout} className="btn btn-secondary">
              Logout
            </button>
          </div>
        </header>
        <Appointments />
      </div>
    )
  }

  if (activeView === 'prescriptions') {
    return (
      <div className="dashboard">
        <header className="dashboard-header">
          <button onClick={() => setActiveView('dashboard')} className="btn btn-secondary">
            ← Back to Dashboard
          </button>
          <div className="header-actions">
            <span>Welcome, {user?.email}</span>
            <button onClick={handleLogout} className="btn btn-secondary">
              Logout
            </button>
          </div>
        </header>
        <Prescriptions />
      </div>
    )
  }

  if (activeView === 'telemedicine') {
    return (
      <div className="dashboard">
        <header className="dashboard-header">
          <button onClick={() => setActiveView('dashboard')} className="btn btn-secondary">
            ← Back to Dashboard
          </button>
          <div className="header-actions">
            <span>Welcome, {user?.email}</span>
            <button onClick={handleLogout} className="btn btn-secondary">
              Logout
            </button>
          </div>
        </header>
        <Telemedicine />
      </div>
    )
  }

  if (activeView === 'notifications') {
    return (
      <div className="dashboard">
        <header className="dashboard-header">
          <button onClick={() => setActiveView('dashboard')} className="btn btn-secondary">
            ← Back to Dashboard
          </button>
          <div className="header-actions">
            <span>Welcome, {user?.email}</span>
            <button onClick={handleLogout} className="btn btn-secondary">
              Logout
            </button>
          </div>
        </header>
        <Notifications />
      </div>
    )
  }

  return (
    <div className="dashboard">
      <header className="dashboard-header">
        <h1>HealthSphere - Doctor Dashboard</h1>
        <div className="header-actions">
          <button
            className="btn btn-secondary"
            onClick={() => setActiveView('notifications')}
            style={{ position: 'relative' }}
          >
            🔔 Notifications
            {unreadCount > 0 && (
              <span style={{
                position: 'absolute',
                top: '-5px',
                right: '-5px',
                background: '#dc3545',
                color: 'white',
                borderRadius: '50%',
                width: '20px',
                height: '20px',
                fontSize: '12px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center'
              }}>
                {unreadCount > 9 ? '9+' : unreadCount}
              </span>
            )}
          </button>
          <span>Welcome, {user?.email}</span>
          <button onClick={handleLogout} className="btn btn-secondary">
            Logout
          </button>
        </div>
      </header>

      <div className="dashboard-content">
        <div className="dashboard-grid">
          <div className="card">
            <h2>Medical Records</h2>
            <p>Create and update patient records</p>
            <button
              className="btn btn-primary"
              onClick={() => setActiveView('medical-records')}
            >
              Manage Records
            </button>
          </div>

          <div className="card">
            <h2>Appointments</h2>
            <p>Manage your appointments</p>
            <button
              className="btn btn-primary"
              onClick={() => setActiveView('appointments')}
            >
              Manage Appointments
            </button>
          </div>

          <div className="card">
            <h2>Prescriptions</h2>
            <p>Generate prescriptions for patients</p>
            <button
              className="btn btn-primary"
              onClick={() => setActiveView('prescriptions')}
            >
              Manage Prescriptions
            </button>
          </div>

          <div className="card">
            <h2>Telemedicine</h2>
            <p>Start video consultations</p>
            <button
              className="btn btn-primary"
              onClick={() => setActiveView('telemedicine')}
            >
              Start Consultation
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

export default DoctorDashboard

