package com.example.memry.recorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop()
}