package de.tillhub.paymentengine.opi.data

import org.simpleframework.xml.core.Persister
import org.simpleframework.xml.stream.Format

class OPISerializer : Persister(FORMAT) {
    companion object {
        private val FORMAT = Format("<?xml version=\"1.0\" encoding= \"UTF-8\" ?>")
    }
}