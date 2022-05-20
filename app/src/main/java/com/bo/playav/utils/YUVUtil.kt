package com.bo.playav.utils

import android.os.Environment
import java.io.FileOutputStream
import java.io.IOException

class YUVUtil {
    companion object {
        /**
         * 原地替换
         */
        fun convertNV21ToI420(data: ByteArray, width: Int, height: Int):ByteArray {
            val frameSize = width * height
            val qFrameSize = frameSize / 4
            //nv21中y不需要处理，直接从UV开始处理
            var index = frameSize
            val ret = ByteArray(data.size)
            data.copyInto(ret, 0, 0, index-1)

            for (i in 0 until qFrameSize) {
                ret[frameSize + i] = data[frameSize + i*2 + 1] // Cb
                ret[frameSize + i + qFrameSize] = data[frameSize + i*2] //Cr
            }
            return ret
        }


        /**
         * 原地替换
         */
        fun convertNV21ToNV12(data: ByteArray):ByteArray {
            val size = data.size
            //nv21中y不需要处理，直接从UV开始处理
            var index = size * 2/3
            val ret = ByteArray(size)
            data.copyInto(ret, 0, 0, index-1)

            val defaultByte = 128
            defaultByte.toByte()
            while (index < size-1) {
                ret[index] = data[index+1]
                ret[index+1] = data[index]
                index+=2
            }
            return ret
        }

        fun writeBytes(array: ByteArray?) {
            var writer: FileOutputStream? = null
            try {
                // 打开一个写文件器，构造函数中的第二个参数true表示以追加形式写文件
                writer = FileOutputStream(
                    Environment.getExternalStorageDirectory().toString() + "/codec.h264", true
                )
                writer.write(array)
                writer.write('\n'.toInt())
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                try {
                    writer?.close()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * 顺时针旋转90度
         */
        fun rotate90(data:ByteArray, width: Int, height: Int): ByteArray {
            val size = data.size
            val ySize = width * height
            var k = 0
            val ret = ByteArray(data.size)

            //rotate Y
            for (i in 0 until width) {
                for (j in height-1 downTo 0) {
                    ret[k++] = data[width*j+i]
                }
            }

            //rotate U
            for (i in 0 until width/2) {
                for (j in 1 until  height/2 + 1) {
                    ret[k++] = data[ySize + ((height/2 - j) * (width / 2) + i)]
                }
            }

            //rotate V
            for (i in 0 until width/2) {
                for (j in 1 until  height/2 + 1) {
                    ret[k++] = data[ySize + ySize / 4 + ((height/2 - j) * (width / 2) + i)]
                }
            }
            return ret
        }
    }
}