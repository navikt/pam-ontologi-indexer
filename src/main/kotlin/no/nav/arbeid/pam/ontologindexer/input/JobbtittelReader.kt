package no.nav.arbeid.pam.ontologindexer.input

import java.io.File
import java.io.InputStream

class JobbtittelReader {
    fun read() : String {
        try {
            val text = File("jobbtitler.json").readText()
            println(text)
            return text
        } catch (e: Exception) {
            println(e)
        }
        return ""
    }

}