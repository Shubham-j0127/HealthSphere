import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'
import API_URL from '../config/api'
import './Appointments.css'

const Appointments = () => {
  const { user } = useAuth()
  const [appointments, setAppointments] = useState([])
  const [doctors, setDoctors] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState({
    doctorId: '',
    appointmentDate: '',
    reason: '',
    notes: ''
  })

  useEffect(() => {
    loadAppointments()
    if (user.role === 'PATIENT') {
      loadDoctors()
    }
  }, [])

  const loadDoctors = async () => {
    try {
      const response = await axios.get(`${API_URL}/api/users/doctors`)
      setDoctors(response.data)
    } catch (error) {
      console.error('Error loading doctors:', error)
    }
  }

  const loadAppointments = async () => {
    try {
      setLoading(true)
      const userInfoResponse = await axios.get(`${API_URL}/api/users/me`)
      const userInfo = userInfoResponse.data
      
      let response
      if (user.role === 'PATIENT') {
        response = await axios.get(`${API_URL}/api/appointments/patient/${userInfo.patientId}`)
      } else {
        response = await axios.get(`${API_URL}/api/appointments/doctor/${userInfo.doctorId}`)
      }
      setAppointments(response.data)
    } catch (error) {
      console.error('Error loading appointments:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleBookAppointment = async (e) => {
    e.preventDefault()
    try {
      const data = {
        ...formData,
        appointmentDate: new Date(formData.appointmentDate).toISOString()
      }
      await axios.post(`${API_URL}/api/appointments`, data)
      setShowForm(false)
      setFormData({
        doctorId: '',
        appointmentDate: '',
        reason: '',
        notes: ''
      })
      loadAppointments()
    } catch (error) {
      console.error('Error booking appointment:', error)
      alert('Error booking appointment')
    }
  }

  const handleStatusUpdate = async (appointmentId, newStatus) => {
    try {
      await axios.put(`${API_URL}/api/appointments/${appointmentId}/status`, newStatus)
      loadAppointments()
    } catch (error) {
      console.error('Error updating status:', error)
      alert('Error updating appointment status')
    }
  }

  const getStatusColor = (status) => {
    const colors = {
      SCHEDULED: '#ffc107',
      CONFIRMED: '#17a2b8',
      IN_PROGRESS: '#007bff',
      COMPLETED: '#28a745',
      CANCELLED: '#dc3545'
    }
    return colors[status] || '#6c757d'
  }

  if (loading) {
    return <div className="loading">Loading appointments...</div>
  }

  return (
    <div className="appointments">
      <div className="page-header">
        <h1>Appointments</h1>
        {user.role === 'PATIENT' && (
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : 'Book Appointment'}
          </button>
        )}
      </div>

      {showForm && user.role === 'PATIENT' && (
        <div className="card form-card">
          <h2>Book New Appointment</h2>
          <form onSubmit={handleBookAppointment}>
            <div className="form-group">
              <label>Doctor</label>
              <select
                value={formData.doctorId}
                onChange={(e) => setFormData({ ...formData, doctorId: e.target.value })}
                required
              >
                <option value="">Select a doctor</option>
                {doctors.map((doctor) => (
                  <option key={doctor.id} value={doctor.id}>
                    {doctor.name} - {doctor.specialization}
                  </option>
                ))}
              </select>
            </div>
            <div className="form-group">
              <label>Date & Time</label>
              <input
                type="datetime-local"
                value={formData.appointmentDate}
                onChange={(e) => setFormData({ ...formData, appointmentDate: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Reason</label>
              <textarea
                value={formData.reason}
                onChange={(e) => setFormData({ ...formData, reason: e.target.value })}
                rows="3"
                required
              />
            </div>
            <div className="form-group">
              <label>Notes (Optional)</label>
              <textarea
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                rows="3"
              />
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary">
                Book Appointment
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => setShowForm(false)}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="appointments-list">
        {appointments.length === 0 ? (
          <div className="card">
            <p>No appointments found.</p>
          </div>
        ) : (
          appointments.map((appointment) => (
            <div key={appointment.id} className="card appointment-card">
              <div className="appointment-header">
                <div>
                  <h3>
                    {user.role === 'PATIENT' ? appointment.doctorName : appointment.patientName}
                  </h3>
                  <p className="appointment-date">
                    {new Date(appointment.appointmentDate).toLocaleString()}
                  </p>
                </div>
                <span
                  className="status-badge"
                  style={{ backgroundColor: getStatusColor(appointment.status) }}
                >
                  {appointment.status}
                </span>
              </div>
              {appointment.reason && (
                <div className="appointment-section">
                  <strong>Reason:</strong>
                  <p>{appointment.reason}</p>
                </div>
              )}
              {appointment.notes && (
                <div className="appointment-section">
                  <strong>Notes:</strong>
                  <p>{appointment.notes}</p>
                </div>
              )}
              <div className="appointment-actions">
                {user.role === 'PATIENT' && appointment.status === 'SCHEDULED' && (
                  <button
                    className="btn btn-danger"
                    onClick={() => handleStatusUpdate(appointment.id, 'CANCELLED')}
                  >
                    Cancel Appointment
                  </button>
                )}
                {user.role === 'DOCTOR' && (
                  <div className="doctor-actions">
                    {appointment.status === 'SCHEDULED' && (
                      <>
                        <button
                          className="btn btn-primary"
                          onClick={() => handleStatusUpdate(appointment.id, 'CONFIRMED')}
                        >
                          Confirm
                        </button>
                        <button
                          className="btn btn-danger"
                          onClick={() => handleStatusUpdate(appointment.id, 'CANCELLED')}
                        >
                          Cancel
                        </button>
                      </>
                    )}
                    {appointment.status === 'CONFIRMED' && (
                      <button
                        className="btn btn-primary"
                        onClick={() => handleStatusUpdate(appointment.id, 'IN_PROGRESS')}
                      >
                        Start Consultation
                      </button>
                    )}
                    {appointment.status === 'IN_PROGRESS' && (
                      <button
                        className="btn btn-primary"
                        onClick={() => handleStatusUpdate(appointment.id, 'COMPLETED')}
                      >
                        Complete
                      </button>
                    )}
                  </div>
                )}
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default Appointments

