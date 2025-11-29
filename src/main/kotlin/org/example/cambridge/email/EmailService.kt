package org.example.cambridge.email

import org.springframework.mail.SimpleMailMessage
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service
import org.thymeleaf.TemplateEngine
import org.thymeleaf.context.Context
import jakarta.mail.internet.MimeMessage
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Service
class EmailService(
    private val mailSender: JavaMailSender,
    private val templateEngine: TemplateEngine
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
     * 템플릿을 사용한 이메일 전송
     */
    fun sendTemplateEmail(to: String, subject: String, templateName: String, variables: Map<String, Any>) {
        val context = Context()
        variables.forEach { (key, value) -> context.setVariable(key, value) }
        context.setVariable("currentYear", LocalDateTime.now().year)

        val htmlContent = templateEngine.process("email/$templateName", context)
        sendHtmlEmail(to, subject, htmlContent)
    }

    /**
     * 새로운 지원 알림 이메일 전송 (기업에게)
     */
    fun sendApplicationNotification(
        companyEmail: String,
        companyName: String,
        postingTitle: String,
        applicantName: String,
        applicantEmail: String,
        university: String,
        major: String,
        appliedAt: LocalDateTime,
        managementLink: String
    ) {
        val variables = mapOf(
            "companyName" to companyName,
            "postingTitle" to postingTitle,
            "applicantName" to applicantName,
            "applicantEmail" to applicantEmail,
            "university" to university,
            "major" to major,
            "appliedAt" to appliedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "managementLink" to managementLink
        )

        sendTemplateEmail(
            to = companyEmail,
            subject = "[$postingTitle] 새로운 지원자가 있습니다",
            templateName = "application-notification.html",
            variables = variables
        )
    }

    /**
     * 선발 축하 이메일 전송 (학생에게)
     */
    fun sendSelectionNotification(
        applicantEmail: String,
        applicantName: String,
        postingTitle: String,
        companyName: String,
        compensation: Long,
        activityStartDate: LocalDateTime?,
        activityEndDate: LocalDateTime?,
        detailLink: String,
        applicationsLink: String
    ) {
        val variables = mutableMapOf<String, Any>(
            "applicantName" to applicantName,
            "postingTitle" to postingTitle,
            "companyName" to companyName,
            "compensation" to String.format("%,d", compensation),
            "detailLink" to detailLink,
            "applicationsLink" to applicationsLink
        )

        activityStartDate?.let {
            variables["activityStartDate"] = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }
        activityEndDate?.let {
            variables["activityEndDate"] = it.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
        }

        sendTemplateEmail(
            to = applicantEmail,
            subject = "[$postingTitle] 축하합니다! 선발되셨습니다",
            templateName = "selection-notification.html",
            variables = variables
        )
    }

    /**
     * 인증 파일 업로드 알림 이메일 전송 (기업에게)
     */
    fun sendVerificationNotification(
        companyEmail: String,
        companyName: String,
        postingTitle: String,
        applicantName: String,
        university: String,
        major: String,
        compensation: Long,
        uploadedAt: LocalDateTime,
        managementLink: String
    ) {
        val variables = mapOf(
            "companyName" to companyName,
            "postingTitle" to postingTitle,
            "applicantName" to applicantName,
            "university" to university,
            "major" to major,
            "compensation" to String.format("%,d", compensation),
            "uploadedAt" to uploadedAt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")),
            "managementLink" to managementLink
        )

        sendTemplateEmail(
            to = companyEmail,
            subject = "[$postingTitle] 인증 파일이 업로드되었습니다",
            templateName = "verification-notification.html",
            variables = variables
        )
    }
}