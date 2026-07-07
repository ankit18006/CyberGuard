package com.example.service

import com.example.data.ThreatAlert
import java.text.Normalizer
import java.util.Locale

object DetectionEngine {

    // Helper to normalize lookalike characters (Unicode homograph attack mitigation)
    fun normalize(text: String): String {
        val decomposed = Normalizer.normalize(text, Normalizer.Form.NFKD)
        val sb = StringBuilder()
        for (char in decomposed) {
            val mapped = when (char) {
                'а' -> 'a'
                'е' -> 'e'
                'о' -> 'o'
                'р' -> 'p'
                'с' -> 'c'
                'х' -> 'x'
                'у' -> 'y'
                'і' -> 'i'
                'ѕ' -> 's'
                '¡' -> '!'
                '|' -> 'l'
                'l' -> 'l'
                '1' -> '1'
                '0' -> '0'
                else -> char
            }
            sb.append(mapped)
        }
        return sb.toString().lowercase(Locale.ROOT)
    }

    fun analyzeNotification(
        appName: String,
        packageName: String,
        title: String,
        text: String
    ): ThreatAlert? {
        val normalizedTitle = normalize(title)
        val normalizedText = normalize(text)
        val fullContent = "$normalizedTitle $normalizedText"

        // 1. OTP Fraud (CRITICAL)
        // Triggers: "share otp", "otp batao", "call pe otp", "one time password"
        // Exception: safe bank messages like "do not share" or "never share" are NOT flagged as a threat
        val otpTriggers = listOf("share otp", "otp batao", "call pe otp", "send otp", "tell me otp", "one time password")
        val otpExemptions = listOf("do not share", "never share", "don't share", "do not tell", "never disclose", "dont share")
        
        val hasOtpTrigger = otpTriggers.any { fullContent.contains(it) } || 
                (fullContent.contains("otp") && (fullContent.contains("share") || fullContent.contains("batao") || fullContent.contains("call")))
        
        if (hasOtpTrigger) {
            val isExempt = otpExemptions.any { fullContent.contains(it) }
            if (!isExempt) {
                return ThreatAlert(
                    appName = appName,
                    packageName = packageName,
                    title = title,
                    messageSnippet = text.take(150),
                    fraudType = "OTP Fraud",
                    severity = "CRITICAL",
                    stepsToTake = "1. NEVER share or tell anyone the OTP, even if they claim to be a bank officer, customer care, or police.\n2. No authentic bank or government agency will ever ask for your OTP over a call, SMS, or WhatsApp.\n3. Call 1930 immediately to register a complaint if you suspect your account is compromised."
                )
            }
        }

        // 2. Remote Access Scam (CRITICAL)
        // Triggers: "anydesk", "teamviewer", "screen share", "screen share karo", "remotely fix", "rustdesk"
        val remoteTriggers = listOf("anydesk", "teamviewer", "screen share", "screenshare", "share screen", "remotely fix", "rustdesk", "quicksupport")
        if (remoteTriggers.any { fullContent.contains(it) }) {
            return ThreatAlert(
                appName = appName,
                packageName = packageName,
                title = title,
                messageSnippet = text.take(150),
                fraudType = "Remote Access Scam",
                severity = "CRITICAL",
                stepsToTake = "1. UNINSTALL AnyDesk, TeamViewer, RustDesk, or QuickSupport immediately.\n2. Turn off your Wi-Fi and Mobile Data immediately to sever the attacker's remote connection.\n3. NEVER open banking, UPI, or password-sensitive apps while screen sharing."
            )
        }

        // 3. Fake Authority (CBI / Police / ED) (CRITICAL)
        // Triggers: "cbi officer", "arrest warrant", "digital arrest", "money laundering case"
        val authorityTriggers = listOf(
            "cbi officer", "arrest warrant", "digital arrest", "money laundering", 
            "ed officer", "customs department", "police officer", "narcotics department", 
            "arrest proof", "court summons", "supreme court order"
        )
        if (authorityTriggers.any { fullContent.contains(it) }) {
            return ThreatAlert(
                appName = appName,
                packageName = packageName,
                title = title,
                messageSnippet = text.take(150),
                fraudType = "Fake Authority (CBI/Police)",
                severity = "CRITICAL",
                stepsToTake = "1. DO NOT PANIC. Indian police or CBI never conduct 'Digital Arrest' or demand money over WhatsApp/Skype/Zoom calls.\n2. Hang up immediately. Do not answer further video/voice calls.\n3. Report this number and message on the National Cyber Crime Portal (cybercrime.gov.in) or call 1930."
            )
        }

        // 4. KYC Scam (DANGEROUS)
        // Triggers: "kyc expired" + action words like "click", "link", "update", "verify"
        val kycKeywords = listOf("kyc expired", "kyc suspended", "kyc update", "verify kyc", "block bank account", "sim blocked")
        val actionKeywords = listOf("click", "link", "http", "update", "verify", "pay", "install")
        val hasKycKeyword = kycKeywords.any { fullContent.contains(it) }
        val hasActionKeyword = actionKeywords.any { fullContent.contains(it) }
        if (hasKycKeyword && hasActionKeyword) {
            return ThreatAlert(
                appName = appName,
                packageName = packageName,
                title = title,
                messageSnippet = text.take(150),
                fraudType = "KYC Scam",
                severity = "DANGEROUS",
                stepsToTake = "1. DO NOT click on any link or call back on the phone numbers provided.\n2. Note that banks never send SMS with shortened or random links for KYC updates.\n3. Visit your official bank branch or call their toll-free number printed on your debit card to verify."
            )
        }

        // 5. Investment Fraud (DANGEROUS)
        // Triggers: "guaranteed returns", "paise double", "secret trading group", "risk free investment"
        val investmentTriggers = listOf(
            "guaranteed returns", "paise double", "secret trading group", "risk free investment", 
            "risk-free investment", "earn money daily", "earn rs", "telegram task scam", 
            "part time job", "part-time job", "stock signals", "crypto double"
        )
        if (investmentTriggers.any { fullContent.contains(it) }) {
            return ThreatAlert(
                appName = appName,
                packageName = packageName,
                title = title,
                messageSnippet = text.take(150),
                fraudType = "Investment Fraud",
                severity = "DANGEROUS",
                stepsToTake = "1. There is no legitimate investment scheme offering guaranteed risk-free double returns.\n2. Block the WhatsApp/Telegram sender and exit any secret stock trading groups immediately.\n3. Do not transfer funds or UPI payments to personal or unverified business accounts."
            )
        }

        // 6. Phishing Links (SUSPICIOUS)
        // Triggers: shortened URL (bit.ly, tinyurl) + sensitive word (bank, password, otp)
        val shortUrlDomains = listOf("bit.ly", "tinyurl.com", "t.co", "is.gd", "buff.ly", "adf.ly", "short.io", "rebrand.ly", "goo.gl")
        val sensitiveWords = listOf("bank", "password", "otp", "upi", "pay", "wallet", "login", "income tax", "refund", "gift card")
        val containsShortUrl = shortUrlDomains.any { fullContent.contains(it) } || fullContent.contains("http://") || fullContent.contains("https://")
        val containsSensitiveWord = sensitiveWords.any { fullContent.contains(it) }
        
        if (containsShortUrl && containsSensitiveWord) {
            return ThreatAlert(
                appName = appName,
                packageName = packageName,
                title = title,
                messageSnippet = text.take(150),
                fraudType = "Phishing Link",
                severity = "SUSPICIOUS",
                stepsToTake = "1. Avoid clicking links in text messages or WhatsApp chats, especially those from unknown senders.\n2. Carefully examine the URL's exact spelling (e.g., in.icicibank-safe.com is a fake; only trust icicibank.com).\n3. Keep your phone's browser up to date and enable Safe Browsing options."
            )
        }

        return null
    }
}
