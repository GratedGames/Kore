package com.gratedgames.graphics.opengl

import com.gratedgames.graphics.DesktopStatistics
import com.gratedgames.graphics.gpu.GraphicsBuffer
import com.gratedgames.memory.Memory
import org.lwjgl.opengl.GL15C.*
import kotlin.math.min

class GLGraphicsBuffer(usage: Usage) : GraphicsBuffer(usage) {
    override val size get() = internalSize

    private var internalSize = 0

    val handle = glGenBuffers()

    private val glUsage
        get() = when (usage) {
            Usage.STATIC -> GL_STATIC_DRAW
            Usage.DYNAMIC -> GL_STREAM_DRAW
        }

    init {
        DesktopStatistics.numBuffers++
    }

    override fun setSize(size: Int) {
        if (size == internalSize)
            return

        tempBind {
            glBufferData(GL_ARRAY_BUFFER, size.toLong(), glUsage)
        }

        internalSize = size
    }

    override fun setData(data: Memory, offset: Int, size: Int) {
        tempBind {
            nglBufferData(GL_ARRAY_BUFFER, size.toLong(), data.address + offset, glUsage)
        }

        internalSize = size
    }

    override fun setSubData(offset: Int, data: Memory, dataOffset: Int, size: Int) {
        tempBind {
            nglBufferSubData(GL_ARRAY_BUFFER, offset.toLong(), size.toLong(), data.address + dataOffset)
        }
    }

    override fun getData(data: Memory, offset: Int, size: Int) {
        tempBind {
            nglGetBufferSubData(GL_ARRAY_BUFFER, offset.toLong(), min(size, this.size).toLong(), data.address)
        }
    }

    override fun dispose() {
        glDeleteBuffers(handle)

        DesktopStatistics.numBuffers--
    }
}

fun GLGraphicsBuffer.tempBind(block: () -> Unit) {
    val previous = GLManager.boundVertexBuffer
    GLManager.bindVertexBuffer(handle)
    block()
    GLManager.bindVertexBuffer(previous)
}