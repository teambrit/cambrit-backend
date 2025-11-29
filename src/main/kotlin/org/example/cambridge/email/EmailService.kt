package org.example.cambridge.email

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import jakarta.mail.internet.MimeMessage

@Service
class EmailService(
    private val mailSender: JavaMailSender
) {
    /**
     * 간단한 텍스트 이메일 전송
     */
    fun sendSimpleEmail(to: String, subject: String, text: String) {
        val message = SimpleMailMessage()
        message.setTo(to)
        message.subject = subject
        message.text = text
        mailSender.send(message)
    }

    /**
     * HTML 이메일 전송
     */
    fun sendHtmlEmail(to: String, subject: String, htmlContent: String) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(htmlContent, true) // true = HTML 형식

        mailSender.send(message)
    }

    /**
     * 여러 수신자에게 이메일 전송
     */
    fun sendEmailToMultiple(recipients: List<String>, subject: String, text: String) {
        val message = SimpleMailMessage()
        message.setTo(*recipients.toTypedArray())
        message.subject = subject
        message.text = text
        mailSender.send(message)
    }

    /**
     * 첨부파일이 있는 이메일 전송
     */
    fun sendEmailWithAttachment(
        to: String,
        subject: String,
        text: String,
        attachmentName: String,
        attachmentData: ByteArray
    ) {
        val message: MimeMessage = mailSender.createMimeMessage()
        val helper = MimeMessageHelper(message, true, "UTF-8")

        helper.setTo(to)
        helper.setSubject(subject)
        helper.setText(text)

        // 첨부파일 추가
        helper.addAttachment(attachmentName, org.springframework.core.io.ByteArrayResource(attachmentData))

        mailSender.send(message)
    }
}