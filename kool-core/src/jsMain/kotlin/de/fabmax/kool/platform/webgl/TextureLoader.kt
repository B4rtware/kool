package de.fabmax.kool.platform.webgl

import de.fabmax.kool.pipeline.*
import de.fabmax.kool.platform.*
import de.fabmax.kool.util.Uint8BufferImpl
import org.khronos.webgl.WebGLRenderingContext
import org.khronos.webgl.WebGLRenderingContext.Companion.NONE
import org.khronos.webgl.WebGLRenderingContext.Companion.RGBA
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_2D
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_CUBE_MAP
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_CUBE_MAP_NEGATIVE_X
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_CUBE_MAP_NEGATIVE_Y
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_CUBE_MAP_NEGATIVE_Z
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_CUBE_MAP_POSITIVE_X
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_CUBE_MAP_POSITIVE_Y
import org.khronos.webgl.WebGLRenderingContext.Companion.TEXTURE_CUBE_MAP_POSITIVE_Z
import org.khronos.webgl.WebGLRenderingContext.Companion.UNPACK_COLORSPACE_CONVERSION_WEBGL
import org.khronos.webgl.WebGLRenderingContext.Companion.UNSIGNED_BYTE
import kotlin.math.floor
import kotlin.math.log2
import kotlin.math.max

object TextureLoader {
    fun loadTexture(ctx: JsContext, props: TextureProps, data: TextureData) : LoadedTextureWebGl {
        return when (data) {
            is BufferedTextureData -> loadTexture2d(ctx, props, data)
            is ImageTextureData -> loadTexture2d(ctx, props, data)
            is CubeMapTextureData -> loadTextureCube(ctx, props, data)
            else -> throw IllegalArgumentException("TextureData type not supported: $data")
        }
    }

    private fun loadTextureCube(ctx: JsContext, props: TextureProps, img: CubeMapTextureData) : LoadedTextureWebGl {
        val gl = ctx.gl
        val tex = LoadedTextureWebGl(ctx, TEXTURE_CUBE_MAP, gl.createTexture(), img.estimateTexSize())
        tex.setSize(img.width, img.height)
        tex.applySamplerProps(props)

        texImage2d(gl, TEXTURE_CUBE_MAP_POSITIVE_X, img.right)
        texImage2d(gl, TEXTURE_CUBE_MAP_NEGATIVE_X, img.left)
        texImage2d(gl, TEXTURE_CUBE_MAP_POSITIVE_Y, img.up)
        texImage2d(gl, TEXTURE_CUBE_MAP_NEGATIVE_Y, img.down)
        texImage2d(gl, TEXTURE_CUBE_MAP_POSITIVE_Z, img.back)
        texImage2d(gl, TEXTURE_CUBE_MAP_NEGATIVE_Z, img.front)
        if (props.mipMapping) {
            gl.generateMipmap(TEXTURE_CUBE_MAP)
        }
        return tex
    }

    private fun loadTexture2d(ctx: JsContext, props: TextureProps, img: TextureData) : LoadedTextureWebGl {
        val gl = ctx.gl
        val tex = LoadedTextureWebGl(ctx, TEXTURE_2D, gl.createTexture(), img.estimateTexSize())
        tex.setSize(img.width, img.height)
        tex.applySamplerProps(props)

        texImage2d(gl, TEXTURE_2D, img)
        if (props.mipMapping) {
            gl.generateMipmap(TEXTURE_2D)
        }
        return tex
    }

    private fun TextureData.estimateTexSize(): Int {
        val layers = if (this is CubeMapTextureData) 6 else 1
        val mipLevels = floor(log2(max(width, height).toDouble())).toInt() + 1
        return Texture.estimatedTexSize(width, height, format.pxSize, layers, mipLevels)
    }

    private fun texImage2d(gl: WebGLRenderingContext, target: Int, data: TextureData) {
        gl.pixelStorei(UNPACK_COLORSPACE_CONVERSION_WEBGL, NONE)
        when (data) {
            is BufferedTextureData -> {
                gl.texImage2D(target, 0, data.format.glInternalFormat, data.width, data.height, 0, data.format.glFormat, data.format.glType, (data.data as Uint8BufferImpl).buffer)
            }
            is ImageTextureData -> {
                gl.texImage2D(target, 0, RGBA, RGBA, UNSIGNED_BYTE, data.data)
            }
            else -> {
                throw IllegalArgumentException("Invalid TextureData type for texImage2d: $data")
            }
        }
    }
}