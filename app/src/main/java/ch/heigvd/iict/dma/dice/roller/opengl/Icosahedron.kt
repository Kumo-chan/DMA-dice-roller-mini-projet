package ch.heigvd.iict.dma.dice.roller.opengl

import android.opengl.GLES20
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.FloatBuffer
import java.nio.ShortBuffer
import kotlin.math.sqrt

class Icosahedron {
    private val vertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "uniform mat4 uMVMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec3 vNormal;" +
        "varying vec3 fNormal;" +
        "void main() {" +
        "  gl_Position = uMVPMatrix * vPosition;" +
        "  fNormal = normalize(mat3(uMVMatrix) * vNormal);" +
        "}"

    private val fragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "varying vec3 fNormal;" +
        "uniform vec3 uLightDirection;" +
        "void main() {" +
        "  float diffuse = max(dot(fNormal, uLightDirection), 0.0);" +
        "  gl_FragColor = vColor * diffuse;" +
        "}"

    val color = floatArrayOf(1f, 1f, 1f, 1.0f)

    // t is the golden ratio
    private val t = (1.0f + sqrt(5.0f)) / 2.0f
    private val vertices = floatArrayOf(
        -1.0f, t, 0.0f,
        1.0f, t, 0.0f,
        -1.0f, -t, 0.0f,
        1.0f, -t, 0.0f,

        0.0f, -1.0f, t,
        0.0f, 1.0f, t,
        0.0f, -1.0f, -t,
        0.0f, 1.0f, -t,

        t, 0.0f, -1.0f,
        t, 0.0f, 1.0f,
        -t, 0.0f, -1.0f,
        -t, 0.0f, 1.0f
    )

    private val indices = shortArrayOf(
        0, 11, 5,
        0, 5, 1,
        0, 1, 7,
        0, 7, 10,
        0, 10, 11,

        1, 5, 9,
        5, 11, 4,
        11, 10, 2,
        10, 7, 6,
        7, 1, 8,

        3, 9, 4,
        3, 4, 2,
        3, 2, 6,
        3, 6, 8,
        3, 8, 9,

        4, 9, 5,
        2, 4, 11,
        6, 2, 10,
        8, 6, 7,
        9, 8, 1
    )

    private var mProgram: Int
    private var vertexBuffer: FloatBuffer
    private var indexBuffer: ShortBuffer
    private lateinit var normalBuffer: FloatBuffer
    private var vPMatrixHandle: Int = 0
    private var vMMatrixHandle: Int = 0
    private var positionHandle: Int = 0
    private var normalHandle: Int = 0
    private var colorHandle: Int = 0
    private var lightDirHandle: Int = 0

    init {
        normalizeVertices()

        vertexBuffer = ByteBuffer.allocateDirect(vertices.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(vertices)
                position(0)
            }
        }

        indexBuffer = ByteBuffer.allocateDirect(indices.size * 2).run {
            order(ByteOrder.nativeOrder())
            asShortBuffer().apply {
                put(indices)
                position(0)
            }
        }

        val vertexShader = loadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode)
        val fragmentShader = loadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode)

        mProgram = GLES20.glCreateProgram().also {
            GLES20.glAttachShader(it, vertexShader)
            GLES20.glAttachShader(it, fragmentShader)
            GLES20.glLinkProgram(it)
        }
    }

    private fun normalizeVertices() {
        var length = 0.0f
        for (i in vertices.indices step 3) {
            val x = vertices[i]
            val y = vertices[i + 1]
            val z = vertices[i + 2]
            length = sqrt(x * x + y * y + z * z)

            vertices[i] /= length
            vertices[i + 1] /= length
            vertices[i + 2] /= length
        }

        val normals = FloatArray(vertices.size)
        for (i in vertices.indices step 3) {
            normals[i] = vertices[i]
            normals[i + 1] = vertices[i + 1]
            normals[i + 2] = vertices[i + 2]
        }

        normalBuffer = ByteBuffer.allocateDirect(normals.size * 4).run {
            order(ByteOrder.nativeOrder())
            asFloatBuffer().apply {
                put(normals)
                position(0)
            }
        }
    }

    fun draw(mvpMatrix: FloatArray) {
        val mvMatrix = FloatArray(16)
        System.arraycopy(mvpMatrix, 0, mvMatrix, 0, 16)

        GLES20.glUseProgram(mProgram)

        positionHandle = GLES20.glGetAttribLocation(mProgram, "vPosition")
        GLES20.glEnableVertexAttribArray(positionHandle)
        GLES20.glVertexAttribPointer(
            positionHandle,
            3,           // 3 coordinates per vertex
            GLES20.GL_FLOAT,
            false,
            0,
            vertexBuffer
        )

        normalHandle = GLES20.glGetAttribLocation(mProgram, "vNormal")
        GLES20.glEnableVertexAttribArray(normalHandle)
        GLES20.glVertexAttribPointer(
            normalHandle,
            3,           // 3 coordinates per normal
            GLES20.GL_FLOAT,
            false,
            0,
            normalBuffer
        )

        colorHandle = GLES20.glGetUniformLocation(mProgram, "vColor")
        GLES20.glUniform4fv(colorHandle, 1, color, 0)

        lightDirHandle = GLES20.glGetUniformLocation(mProgram, "uLightDirection")
        GLES20.glUniform3f(lightDirHandle, 0.0f, 0.3f, -1.0f)

        vPMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVPMatrix")
        GLES20.glUniformMatrix4fv(vPMatrixHandle, 1, false, mvpMatrix, 0)

        vMMatrixHandle = GLES20.glGetUniformLocation(mProgram, "uMVMatrix")
        GLES20.glUniformMatrix4fv(vMMatrixHandle, 1, false, mvMatrix, 0)

        GLES20.glDrawElements(
            GLES20.GL_TRIANGLES,
            indices.size,
            GLES20.GL_UNSIGNED_SHORT,
            indexBuffer
        )

        GLES20.glDisableVertexAttribArray(positionHandle)
        GLES20.glDisableVertexAttribArray(normalHandle)
    }
}