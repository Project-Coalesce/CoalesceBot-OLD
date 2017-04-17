package com.coalesce.punishments

enum class Reason(val description: String, val severity: Int) {
    //Severity 1
    SPAM("Spamming the chat", 1), LONG_LOG_POSTS("Long log posting, please use Pastebin", 1), WRONG_SECTION("Off topic conversation", 1),

    //Severity 2
    RUDENESS("General rudeness or lack of respect", 2), PORN("Porn", 2), GORE("Gore", 2)
}