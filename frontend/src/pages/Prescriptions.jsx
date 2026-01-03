import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'
import API_URL from '../config/api'
import './Prescriptions.css'

const Prescriptions = () => {
  const { user } = useAuth()
  const [prescriptions, setPrescriptions] = useState([])
  const [patients, setPatients] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [formData, setFormData] = useState({
    patientId: '',
    medicines: '',
    dosage: '',
    instructions: '',
    validUntil: '',
    notes: ''
  })

  useEffect(() => {
    loadPrescriptions()
    if (user.role === 'DOCTOR') {
      loadPatients()
    }
  }, [])

  const loadPatients = async () => {
    // In a real app, you'd have a patients endpoint
    // For now, we'll use a placeholder
    try {
      // This would be replaced with actual patient list API
      setPatients([])
    } catch (error) {
      console.error('Error loading patients:', error)
    }
  }

  const loadPrescriptions = async () => {
    try {
      setLoading(true)
      const userInfoResponse = await axios.get(`${API_URL}/api/users/me`)
      const userInfo = userInfoResponse.data
      
      let response
      if (user.role === 'PATIENT') {
        response = await axios.get(`${API_URL}/api/prescriptions/patient/${userInfo.patientId}`)
      } else {
        response = await axios.get(`${API_URL}/api/prescriptions/doctor/${userInfo.doctorId}`)
      }
      setPrescriptions(response.data)
    } catch (error) {
      console.error('Error loading prescriptions:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleCreatePrescription = async (e) => {
    e.preventDefault()
    try {
      const data = {
        ...formData,
        prescriptionDate: new Date().toISOString()
      }
      await axios.post(`${API_URL}/api/prescriptions`, data)
      setShowForm(false)
      setFormData({
        patientId: '',
        medicines: '',
        dosage: '',
        instructions: '',
        validUntil: '',
        notes: ''
      })
      loadPrescriptions()
    } catch (error) {
      console.error('Error creating prescription:', error)
      alert('Error creating prescription')
    }
  }

  if (loading) {
    return <div className="loading">Loading prescriptions...</div>
  }

  return (
    <div className="prescriptions">
      <div className="page-header">
        <h1>Prescriptions</h1>
        {user.role === 'DOCTOR' && (
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : 'Create New Prescription'}
          </button>
        )}
      </div>

      {showForm && user.role === 'DOCTOR' && (
        <div className="card form-card">
          <h2>Create Prescription</h2>
          <form onSubmit={handleCreatePrescription}>
            <div className="form-group">
              <label>Patient ID</label>
              <input
                type="number"
                value={formData.patientId}
                onChange={(e) => setFormData({ ...formData, patientId: e.target.value })}
                required
                placeholder="Enter patient ID"
              />
            </div>
            <div className="form-group">
              <label>Medicines</label>
              <textarea
                value={formData.medicines}
                onChange={(e) => setFormData({ ...formData, medicines: e.target.value })}
                rows="4"
                required
                placeholder="List all medicines (e.g., Paracetamol 500mg, Amoxicillin 250mg)"
              />
            </div>
            <div className="form-group">
              <label>Dosage</label>
              <textarea
                value={formData.dosage}
                onChange={(e) => setFormData({ ...formData, dosage: e.target.value })}
                rows="3"
                placeholder="Dosage instructions (e.g., 1 tablet twice daily)"
              />
            </div>
            <div className="form-group">
              <label>Instructions</label>
              <textarea
                value={formData.instructions}
                onChange={(e) => setFormData({ ...formData, instructions: e.target.value })}
                rows="3"
                placeholder="Additional instructions for the patient"
              />
            </div>
            <div className="form-group">
              <label>Valid Until</label>
              <input
                type="date"
                value={formData.validUntil}
                onChange={(e) => setFormData({ ...formData, validUntil: e.target.value })}
              />
            </div>
            <div className="form-group">
              <label>Notes (Optional)</label>
              <textarea
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                rows="3"
                placeholder="Additional notes"
              />
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary">
                Create Prescription
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

      <div className="prescriptions-list">
        {prescriptions.length === 0 ? (
          <div className="card">
            <p>No prescriptions found.</p>
          </div>
        ) : (
          prescriptions.map((prescription) => (
            <div key={prescription.id} className="card prescription-card">
              <div className="prescription-header">
                <div>
                  <h3>
                    {user.role === 'PATIENT' ? prescription.doctorName : prescription.patientName}
                  </h3>
                  <p className="prescription-date">
                    Prescribed on: {new Date(prescription.prescriptionDate).toLocaleDateString()}
                  </p>
                  {prescription.validUntil && (
                    <p className="valid-until">
                      Valid until: {new Date(prescription.validUntil).toLocaleDateString()}
                    </p>
                  )}
                </div>
              </div>
              <div className="prescription-section">
                <strong>Medicines:</strong>
                <p className="medicines-text">{prescription.medicines}</p>
              </div>
              {prescription.dosage && (
                <div className="prescription-section">
                  <strong>Dosage:</strong>
                  <p>{prescription.dosage}</p>
                </div>
              )}
              {prescription.instructions && (
                <div className="prescription-section">
                  <strong>Instructions:</strong>
                  <p>{prescription.instructions}</p>
                </div>
              )}
              {prescription.notes && (
                <div className="prescription-section">
                  <strong>Notes:</strong>
                  <p>{prescription.notes}</p>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default Prescriptions

