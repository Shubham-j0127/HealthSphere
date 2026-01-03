import React, { useState, useEffect } from 'react'
import { useAuth } from '../context/AuthContext'
import axios from 'axios'
import API_URL from '../config/api'
import './MedicalRecords.css'

const MedicalRecords = () => {
  const { user } = useAuth()
  const [records, setRecords] = useState([])
  const [loading, setLoading] = useState(true)
  const [showForm, setShowForm] = useState(false)
  const [editingRecord, setEditingRecord] = useState(null)
  const [formData, setFormData] = useState({
    patientId: '',
    diagnosis: '',
    medications: '',
    labResults: '',
    notes: '',
    symptoms: '',
    treatmentPlan: ''
  })

  useEffect(() => {
    loadRecords()
  }, [])

  const loadRecords = async () => {
    try {
      setLoading(true)
      const userInfoResponse = await axios.get(`${API_URL}/api/users/me`)
      const userInfo = userInfoResponse.data
      
      let response
      if (user.role === 'PATIENT') {
        response = await axios.get(`${API_URL}/api/medical-records/patient/${userInfo.patientId}`)
      } else {
        response = await axios.get(`${API_URL}/api/medical-records/doctor/${userInfo.doctorId}`)
      }
      setRecords(response.data)
    } catch (error) {
      console.error('Error loading records:', error)
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    try {
      const data = {
        ...formData,
        recordDate: new Date().toISOString()
      }

      if (editingRecord) {
        await axios.put(`${API_URL}/api/medical-records/${editingRecord.id}`, data)
      } else {
        await axios.post(`${API_URL}/api/medical-records`, data)
      }

      setShowForm(false)
      setEditingRecord(null)
      setFormData({
        patientId: '',
        diagnosis: '',
        medications: '',
        labResults: '',
        notes: '',
        symptoms: '',
        treatmentPlan: ''
      })
      loadRecords()
    } catch (error) {
      console.error('Error saving record:', error)
      alert('Error saving medical record')
    }
  }

  const handleEdit = (record) => {
    setEditingRecord(record)
    setFormData({
      patientId: record.patientId,
      diagnosis: record.diagnosis || '',
      medications: record.medications || '',
      labResults: record.labResults || '',
      notes: record.notes || '',
      symptoms: record.symptoms || '',
      treatmentPlan: record.treatmentPlan || ''
    })
    setShowForm(true)
  }

  if (loading) {
    return <div className="loading">Loading medical records...</div>
  }

  return (
    <div className="medical-records">
      <div className="page-header">
        <h1>Medical Records</h1>
        {user.role === 'DOCTOR' && (
          <button className="btn btn-primary" onClick={() => setShowForm(!showForm)}>
            {showForm ? 'Cancel' : 'Create New Record'}
          </button>
        )}
      </div>

      {showForm && user.role === 'DOCTOR' && (
        <div className="card form-card">
          <h2>{editingRecord ? 'Edit' : 'Create'} Medical Record</h2>
          <form onSubmit={handleSubmit}>
            <div className="form-group">
              <label>Patient ID</label>
              <input
                type="number"
                value={formData.patientId}
                onChange={(e) => setFormData({ ...formData, patientId: e.target.value })}
                required
              />
            </div>
            <div className="form-group">
              <label>Diagnosis</label>
              <textarea
                value={formData.diagnosis}
                onChange={(e) => setFormData({ ...formData, diagnosis: e.target.value })}
                rows="3"
              />
            </div>
            <div className="form-group">
              <label>Symptoms</label>
              <textarea
                value={formData.symptoms}
                onChange={(e) => setFormData({ ...formData, symptoms: e.target.value })}
                rows="3"
              />
            </div>
            <div className="form-group">
              <label>Medications</label>
              <textarea
                value={formData.medications}
                onChange={(e) => setFormData({ ...formData, medications: e.target.value })}
                rows="3"
              />
            </div>
            <div className="form-group">
              <label>Lab Results</label>
              <textarea
                value={formData.labResults}
                onChange={(e) => setFormData({ ...formData, labResults: e.target.value })}
                rows="3"
              />
            </div>
            <div className="form-group">
              <label>Treatment Plan</label>
              <textarea
                value={formData.treatmentPlan}
                onChange={(e) => setFormData({ ...formData, treatmentPlan: e.target.value })}
                rows="3"
              />
            </div>
            <div className="form-group">
              <label>Notes</label>
              <textarea
                value={formData.notes}
                onChange={(e) => setFormData({ ...formData, notes: e.target.value })}
                rows="3"
              />
            </div>
            <div className="form-actions">
              <button type="submit" className="btn btn-primary">
                {editingRecord ? 'Update' : 'Create'} Record
              </button>
              <button
                type="button"
                className="btn btn-secondary"
                onClick={() => {
                  setShowForm(false)
                  setEditingRecord(null)
                }}
              >
                Cancel
              </button>
            </div>
          </form>
        </div>
      )}

      <div className="records-list">
        {records.length === 0 ? (
          <div className="card">
            <p>No medical records found.</p>
          </div>
        ) : (
          records.map((record) => (
            <div key={record.id} className="card record-card">
              <div className="record-header">
                <div>
                  <h3>
                    {user.role === 'PATIENT' ? record.doctorName : record.patientName}
                  </h3>
                  <p className="record-date">
                    {new Date(record.recordDate).toLocaleDateString()}
                  </p>
                </div>
                {user.role === 'DOCTOR' && (
                  <button
                    className="btn btn-secondary"
                    onClick={() => handleEdit(record)}
                  >
                    Edit
                  </button>
                )}
              </div>
              {record.diagnosis && (
                <div className="record-section">
                  <strong>Diagnosis:</strong>
                  <p>{record.diagnosis}</p>
                </div>
              )}
              {record.symptoms && (
                <div className="record-section">
                  <strong>Symptoms:</strong>
                  <p>{record.symptoms}</p>
                </div>
              )}
              {record.medications && (
                <div className="record-section">
                  <strong>Medications:</strong>
                  <p>{record.medications}</p>
                </div>
              )}
              {record.labResults && (
                <div className="record-section">
                  <strong>Lab Results:</strong>
                  <p>{record.labResults}</p>
                </div>
              )}
              {record.treatmentPlan && (
                <div className="record-section">
                  <strong>Treatment Plan:</strong>
                  <p>{record.treatmentPlan}</p>
                </div>
              )}
              {record.notes && (
                <div className="record-section">
                  <strong>Notes:</strong>
                  <p>{record.notes}</p>
                </div>
              )}
            </div>
          ))
        )}
      </div>
    </div>
  )
}

export default MedicalRecords

