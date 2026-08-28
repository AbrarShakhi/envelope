package com.abrarshakhi.envelope.engine.email.service

interface EmailService {
    fun sendOtpEmail(toEmail: String, otp: String, purpose: String)
}
